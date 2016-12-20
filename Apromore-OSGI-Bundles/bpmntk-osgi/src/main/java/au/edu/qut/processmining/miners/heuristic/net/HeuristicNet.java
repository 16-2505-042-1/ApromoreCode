package au.edu.qut.processmining.miners.heuristic.net;

import au.edu.qut.processmining.log.SimpleLog;
import au.edu.qut.processmining.miners.heuristic.ui.HMPlusUIResult;
import org.apache.commons.lang3.StringUtils;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;

import java.util.*;

/**
 * Created by Adriano on 24/10/2016.
 */
public class HeuristicNet {

    private SimpleLog log;
    private int startcode;
    private int endcode;

    private Map<String, Integer> traces;
    private Map<Integer, String> events;

    private Set<HeuristicEdge> edges;
    private Map<Integer, HeuristicNode> nodes;

    private Map<Integer, HeuristicEdge> candidateSuccessor;
    private Map<Integer, HeuristicEdge> candidatePredecessor;

    private Map<Integer, HashSet<HeuristicEdge>> outgoing;
    private Map<Integer, HashSet<HeuristicEdge>> incoming;

    private Map<Integer, HashSet<Integer>> parallelisms;
    private Map<Integer, HashSet<Integer>> conflicts;

    private Set<Integer> loopsL1;
    private Set<HeuristicEdge> loopsL2;

    private Map<Integer, HashMap<Integer, HeuristicEdge>> net;

    private double dependencyThreshold;
    private double positiveObservations;
    private double relative2BestThreshold;

    public HeuristicNet(SimpleLog log) {
        this.log = log;

        traces = log.getTraces();
        events = log.getEvents();

        startcode = log.getStartcode();
        endcode = log.getEndcode();

        dependencyThreshold = HMPlusUIResult.DEPENDENCY_THRESHOLD;
        positiveObservations = 0.0;
        relative2BestThreshold = 1.0;
    }

    public HeuristicNet(SimpleLog log, double dependencyThreshold, double positiveObservations, double relative2BestThreshold) {
        this.log = log;

        traces = log.getTraces();
        events = log.getEvents();

        startcode = log.getStartcode();
        endcode = log.getEndcode();

        this.dependencyThreshold = dependencyThreshold;
        this.positiveObservations = positiveObservations;
        this.relative2BestThreshold = relative2BestThreshold;


    }

    public void generateHeuristicNet() {
        evaluateDirectlyFollowDependencies();
        evaluateParallelismsAndConflicts(); //depends on evaluateDirectlyFollowDependencies()
        evaluateLoops();    //depends on evaluateParallelismsAndConflicts()
        evaluateDependencyScores();  //depends on evaluateLoops()
        pruneHeuristicNet(); //depends on evaluateDependecyScores()
        checkIntegrity();   //repairs the net if disconnected
        removeWeakParallelisms();   //last thing to run ever
    }

    private void checkIntegrity() {
        System.out.println("DEBUG - recovering edges:");
        for(int n : nodes.keySet()) {
            if( incoming.get(n).isEmpty() && (n != startcode) ) addEdge(candidatePredecessor.get(n));
            if( outgoing.get(n).isEmpty() && (n != endcode) ) addEdge(candidateSuccessor.get(n));
        }
    }

    private void removeWeakParallelisms() {
        int src;
        int tgt;

        System.out.println("DEBUG - evaluating dependency scores ...");
        for( HeuristicEdge e : edges ) {
            src = e.getSource().getCode();
            tgt = e.getTarget().getCode();

            if( parallelisms.containsKey(src) && parallelisms.get(src).remove(tgt) ) {
                System.out.println("DEBUG - successfully removed weak parallelism: " + src + " || " + tgt);
            }

            if( parallelisms.containsKey(tgt) && parallelisms.get(tgt).remove(src) ) {
                System.out.println("DEBUG - successfully removed weak reverse parallelism: " + tgt + " || " + src);
            }
        }
    }

    private void evaluateDirectlyFollowDependencies() {
        StringTokenizer trace;
        int traceFrequency;

        int event;
        int prevEvent;

        HeuristicNode node;
        HeuristicNode prevNode;
        HeuristicEdge edge;

        HeuristicNode autogenStart;
        HeuristicNode autogenEnd;

        nodes = new HashMap<>();
        edges = new HashSet<>();
        outgoing = new HashMap<>();
        incoming = new HashMap<>();
        net = new HashMap<>();

        autogenStart = new HeuristicNode(events.get(startcode), startcode);
        this.addNode(autogenStart);
        autogenStart.increaseFrequency(log.size()); //we will always skip to analyse this event later, so we set now the maximum frequency because it is an artificial start event

        autogenEnd = new HeuristicNode(events.get(endcode), endcode);
        this.addNode(autogenEnd);

        for( String t : traces.keySet() ) {
            trace = new StringTokenizer(t, "::");
            traceFrequency = traces.get(t);

            trace.nextToken(); //consuming the start event that is always 0
            prevEvent = startcode;
            prevNode = autogenStart;

            while( trace.hasMoreTokens() ) {
                event = Integer.valueOf(trace.nextToken());

                if( !nodes.containsKey(event) ) {
                    node =  new HeuristicNode(events.get(event), event);
                    this.addNode(node);
                } else node = nodes.get(event);

                node.increaseFrequency(traceFrequency); //increasing frequency of this event occurrence

                if( !net.containsKey(prevEvent) || !net.get(prevEvent).containsKey(event) ) {
                    edge = new HeuristicEdge(prevNode, node);
                    this.addEdge(edge);
                }

                net.get(prevEvent).get(event).increaseFrequency(traceFrequency); //increasing frequency of this directly following relationship

                prevEvent = event;
                prevNode = node;
            }
        }
    }


    private void evaluateParallelismsAndConflicts() {
        parallelisms = new HashMap<>();
        conflicts = new HashMap<>();

        System.out.println("DEBUG - evaluating parallelism and conflicts ...");
        for( int src : net.keySet() )
            for( int tgt : net.get(src).keySet() ) {
                if( net.containsKey(tgt) && (net.get(tgt).containsKey(src)) ) {
                    if( !parallelisms.containsKey(src) ) parallelisms.put(src, new HashSet<Integer>());
                    parallelisms.get(src).add(tgt);
                } else {
                    if( !conflicts.containsKey(src) ) conflicts.put(src, new HashSet<Integer>());
                    conflicts.get(src).add(tgt);
                }
            }
//        printParallelisms();
    }

    public boolean areConcurrent(int A, int B) {
        return (parallelisms.containsKey(A) && parallelisms.get(A).contains(B));
    }

    public boolean areInConflict(int A, int B) {
        return (conflicts.containsKey(A) && conflicts.get(A).contains(B));
    }

    public boolean isDirectlyFollow(int A, int B) {
        return (net.containsKey(A) && net.get(A).containsKey(B));
    }

    private void evaluateLoops() {
        HashSet<HeuristicEdge> removableLoopEdges = new HashSet();
        HashMap<Integer, HashMap<Integer, Double>> loop2Frequencies = new HashMap<>();
        int src, tgt;

        String src2tgt_loop2Pattern;
        String tgt2src_loop2Pattern;

        int src2tgt_loop2Frequency;
        int tgt2src_loop2Frequency;

        double loop2DependencyScore;

        loopsL1 = new HashSet<>();
        loopsL2 = new HashSet<>();

        System.out.println("DEBUG - evaluating loops length ONE ...");
        for( HeuristicEdge e : edges ) {
            src = e.getSource().getCode();
            tgt = e.getTarget().getCode();
            if( src == tgt ) {
                loopsL1.add(src);
                removableLoopEdges.add(e);
            }
        }

        System.out.println("DEBUG - found " + loopsL1.size() + " self-loops:");
        for( int code : loopsL1 ) System.out.println("DEBUG - self-loop: " + code);

        System.out.println("DEBUG - evaluating loops length TWO ...");
        for( HeuristicEdge e : edges ) {
            src = e.getSource().getCode();
            tgt = e.getTarget().getCode();
            /* if both src and tgt are length 1 loops, we do not evaluate length 2 loops for this edge */
            if( loopsL1.contains(src) && loopsL1.contains(tgt) ) continue;

            if( loop2Frequencies.containsKey(tgt) && loop2Frequencies.get(tgt).containsKey(src) ) {
                loop2DependencyScore = loop2Frequencies.get(tgt).get(src);
            } else {
                src2tgt_loop2Pattern = "::" + src + "::" + tgt + "::" + src + "::";
                tgt2src_loop2Pattern = "::" + tgt + "::" + src + "::" + tgt + "::";
                src2tgt_loop2Frequency = 0;
                tgt2src_loop2Frequency = 0;

                for( String trace : traces.keySet() ) {
                    src2tgt_loop2Frequency += (StringUtils.countMatches(trace, src2tgt_loop2Pattern)*traces.get(trace));
                    tgt2src_loop2Frequency += (StringUtils.countMatches(trace, tgt2src_loop2Pattern)*traces.get(trace));
                }

                loop2DependencyScore = (double)(src2tgt_loop2Frequency + tgt2src_loop2Frequency)/(src2tgt_loop2Frequency + tgt2src_loop2Frequency + 1);

                if( !loop2Frequencies.containsKey(src) ) loop2Frequencies.put(src, new HashMap<Integer, Double>());
                if( !loop2Frequencies.get(src).containsKey(tgt) ) loop2Frequencies.get(src).put(tgt, loop2DependencyScore);
            }

            System.out.println("DEBUG - #" + src + " >> " + tgt + " : " + loop2DependencyScore);
            if( loop2DependencyScore == 0 ) continue;
            loopsL2.add(e);
            e.setLocalDependencyScore(loop2DependencyScore);
            if( parallelisms.containsKey(src) && parallelisms.get(src).remove(tgt) )
                System.out.println("DEBUG - successfully removed short-loop parallelism: " + src + " || " + tgt);
        }


        System.out.println("DEBUG - removing loops length ONE ...");
        for( HeuristicEdge e : removableLoopEdges ) this.removeEdge(e);
    }

    private void evaluateDependencyScores() {
        int src2tgt_frequency;
        int tgt2src_frequency;
        double localDependency;

        int src;
        int tgt;

        candidateSuccessor = new HashMap<>();
        candidatePredecessor = new HashMap<>();

        System.out.println("DEBUG - evaluating dependency scores ...");
        for( HeuristicEdge e : edges ) {
            src = e.getSource().getCode();
            tgt = e.getTarget().getCode();

            if( !loopsL2.contains(e) ) {
                if (src == tgt) {
                    localDependency = (double) (e.getFrequency()) / (e.getFrequency() + 1);
                    e.setLocalDependencyScore(localDependency);
                } else {
                    src2tgt_frequency = e.getFrequency();

                    if (net.containsKey(tgt) && net.get(tgt).containsKey(src)) {
                        // this means there is a reverse edge
                        tgt2src_frequency = net.get(tgt).get(src).getFrequency();
                    } else tgt2src_frequency = 0;

                /* computing the heuristic formula */
                    localDependency = (double) (src2tgt_frequency - tgt2src_frequency) / (src2tgt_frequency + tgt2src_frequency + 1);
                    e.setLocalDependencyScore(localDependency);
                }
                System.out.println("DEBUG - #" + src + " => " + tgt + " : " + localDependency);
            } else localDependency = e.getLocalDependencyScore();

            if( !candidateSuccessor.containsKey(src) ) candidateSuccessor.put(src, e);
            else if( candidateSuccessor.get(src).getLocalDependencyScore() < localDependency ) candidateSuccessor.put(src, e);

            if( !candidatePredecessor.containsKey(tgt) ) candidatePredecessor.put(tgt, e);
            else if( candidatePredecessor.get(tgt).getLocalDependencyScore() < localDependency ) candidatePredecessor.put(tgt, e);
        }
    }

    private void pruneHeuristicNet() {
        HashSet<HeuristicEdge> toBeRemoved = new HashSet<>();
        int src, tgt;
        double maxDependencyScore;
        double dsThreshold;

        boolean underDependencyThreshold;
        boolean underPositiveObservations;
        boolean overRelative2BestThreshold;

        boolean firstCheck;
        boolean secondCheck;

        System.out.println("DEBUG - edges before pruning: " + edges.size());

        for( HeuristicEdge e : edges ) {
            src = e.getSource().getCode();
            tgt = e.getTarget().getCode();
//            if(src == tgt) continue; //loop guard

//            if( (parallelisms.containsKey(src) && parallelisms.get(src).contains(tgt)) ||
//                    (parallelisms.containsKey(tgt) && parallelisms.get(tgt).contains(src)) ) {
//                toBeRemoved.add(e);
//                System.out.println("DEBUG - forcing the removal of the edge: " + src + " -> " + tgt);
//                continue;
//            }

            /* firstly we evaluate the edge for the successor */
            maxDependencyScore = candidateSuccessor.get(src).getLocalDependencyScore();
            dsThreshold = maxDependencyScore*dependencyThreshold;

            underDependencyThreshold =  e.getLocalDependencyScore() < dsThreshold;
            underPositiveObservations = e.getFrequency() < (log.size()*positiveObservations);
            overRelative2BestThreshold = (maxDependencyScore - e.getLocalDependencyScore()) > relative2BestThreshold;

            firstCheck = underDependencyThreshold || underPositiveObservations || overRelative2BestThreshold;

            /* secondly we evaluate the edge for the predecessor */
            maxDependencyScore = candidatePredecessor.get(tgt).getLocalDependencyScore();
            dsThreshold = maxDependencyScore*dependencyThreshold;

            underDependencyThreshold =  e.getLocalDependencyScore() < dsThreshold;
            underPositiveObservations = e.getFrequency() < (log.size()*positiveObservations);
            overRelative2BestThreshold = (maxDependencyScore - e.getLocalDependencyScore()) > relative2BestThreshold;

            secondCheck = underDependencyThreshold || underPositiveObservations || overRelative2BestThreshold;

            if( firstCheck && secondCheck ) toBeRemoved.add(e);
        }

        for( HeuristicEdge e : toBeRemoved ) this.removeEdge(e);
        System.out.println("DEBUG - edges after pruning: " + edges.size());
    }

    public BPMNDiagram getHeuristicDiagram(boolean labels) {
        BPMNDiagram diagram = new BPMNDiagramImpl("heuristic-net");
        HashMap<Integer, BPMNNode> mapping = new HashMap<>();
        String label;
        Activity task;
        BPMNNode src, tgt;

        System.out.println("DEBUG - building the Heuristic net with [ nodes : edges ] = [" + nodes.size() + " : " + edges.size() + " ]");

        for( int event : nodes.keySet() ) {
            label = events.get(event) + "\n(" + nodes.get(event).getFrequency() + ")";
            task = diagram.addActivity( (labels ? label : Integer.toString(event)), false, false, false, false, false);
            mapping.put(event, task);
        }

        for( HeuristicEdge edge : edges ) {
            src = mapping.get(edge.getSource().getCode());
            tgt = mapping.get(edge.getTarget().getCode());
            diagram.addFlow(src, tgt, edge.toString());
        }

        return diagram;
    }

    public BPMNDiagram convertIntoBPMNDiagram() {
        BPMNDiagram diagram = new BPMNDiagramImpl("bpmn-diagram");
        HashMap<Integer, BPMNNode> mapping = new HashMap<>();
        String label;
        BPMNNode node;
        BPMNNode src, tgt;

        for( int event : nodes.keySet() ) {
            label = Integer.toString(event);

            if( event == startcode || event == endcode )
                node = diagram.addEvent(label, (event == startcode ? Event.EventType.START : Event.EventType.END), Event.EventTrigger.NONE, (event == startcode ? Event.EventUse.CATCH : Event.EventUse.THROW), true, null);
            else
                node = diagram.addActivity(label, loopsL1.contains(event), false, false, false, false);

            mapping.put(event, node);
        }

        for( HeuristicEdge edge : edges ) {
            src = mapping.get(edge.getSource().getCode());
            tgt = mapping.get(edge.getTarget().getCode());
            diagram.addFlow(src, tgt, edge.toString());
        }

        return diagram;
    }


    /* data objects management */

    private void addNode(HeuristicNode n) {
        int code = n.getCode();

        nodes.put(code, n);
        if( !incoming.containsKey(code) ) incoming.put(code, new HashSet<HeuristicEdge>());
        if( !outgoing.containsKey(code) ) outgoing.put(code, new HashSet<HeuristicEdge>());
    }

    private void addEdge(HeuristicEdge e) {
        int src = e.getSource().getCode();
        int tgt = e.getTarget().getCode();

        edges.add(e);
        incoming.get(tgt).add(e);
        outgoing.get(src).add(e);

        if( !net.containsKey(src) ) net.put(src, new HashMap<Integer, HeuristicEdge>());
        net.get(src).put(tgt, e);

        System.out.println("DEBUG - added edge: " + src + " -> " + tgt);
    }

    private void removeEdge(HeuristicEdge e) {
        int src = e.getSource().getCode();
        int tgt = e.getTarget().getCode();
        incoming.get(tgt).remove(e);
        outgoing.get(src).remove(e);
        net.get(src).remove(tgt);
        edges.remove(e);
//        System.out.println("DEBUG - removed edge: " + src + " -> " + tgt);
    }

    /* DEBUG methods */

    public void printFrequencies() {
        System.out.println("DEBUG - printing frequencies:");
        for( HeuristicNode node : nodes.values() ) System.out.println("DEBUG - #" + node.getCode() + " = " + node.getFrequency());
    }

    public void printParallelisms() {
        System.out.println("DEBUG - printing parallelisms:");
        for( int A : parallelisms.keySet() ) {
            System.out.print("DEBUG - " + A + " || " );
            for( int B : parallelisms.get(A) ) System.out.print( B + ",");
            System.out.println();
        }
    }

    public void printConflicts() {
        System.out.println("DEBUG - printing conflicts:");
        for( int A : conflicts.keySet() ) {
            System.out.print("DEBUG - " + A + " # " );
            for( int B : conflicts.get(A) ) System.out.print( B + ",");
            System.out.println();
        }
    }
}
