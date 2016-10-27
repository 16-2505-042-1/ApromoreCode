package au.edu.qut.processmining.miners.heuristic.net;

import au.edu.qut.processmining.log.SimpleLog;
import au.edu.qut.processmining.miners.heuristic.ui.HMPlusUIResult;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Adriano on 24/10/2016.
 */
public class HeuristicNet {

    private static DecimalFormat dFormat = new DecimalFormat(".###");

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
        positiveObservations = HMPlusUIResult.POSITIVE_OBSERVATIONS;
        relative2BestThreshold = HMPlusUIResult.RELATIVE2BEST_THRESHOLD;
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
        evaluateDependencies();
        evaluateParallelismsAndConflicts();
        evaluateDependecyScores();
        pruneHeuristicNet();
    }

    private void evaluateDependencies() {
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
        nodes.put(startcode, autogenStart);
        autogenStart.increaseFrequency(log.size()); //we will always skip to analyse this event later, so we set now the maximum frequency because it is an artificial start event

        autogenEnd = new HeuristicNode(events.get(endcode), endcode);
        nodes.put(endcode, autogenEnd);

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
                    nodes.put(event, node);
                } else node = nodes.get(event);

                node.increaseFrequency(traceFrequency); //increasing frequency of this event occurrence

                if( !net.containsKey(prevEvent) ) net.put(prevEvent, new HashMap<Integer, HeuristicEdge>());
                if( !net.get(prevEvent).containsKey(event) ) {
                    edge = new HeuristicEdge(prevNode, node);
                    edges.add(edge);
                    if( !incoming.containsKey(event) ) incoming.put(event, new HashSet<HeuristicEdge>());
                    if( !outgoing.containsKey(prevEvent) ) outgoing.put(prevEvent, new HashSet<HeuristicEdge>());
                    incoming.get(event).add(edge);
                    outgoing.get(prevEvent).add(edge);
                    net.get(prevEvent).put(event, edge);
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
    }

    private boolean areConcurrent(int A, int B) {
        return (parallelisms.containsKey(A) && parallelisms.get(A).contains(B));
    }

    private boolean areInConflict(int A, int B) {
        return (conflicts.containsKey(A) && conflicts.get(A).contains(B));
    }

    public boolean isDirectlyFollow(int A, int B) {
        return (net.containsKey(A) && net.get(A).containsKey(B));
    }

    private void evaluateDependecyScores() {
        int src2tgt_frequency;
        int tgt2src_frequency;
        double localDependency;

        int src;
        int tgt;

        candidateSuccessor = new HashMap<>();
        candidatePredecessor = new HashMap<>();

        for( HeuristicEdge e : edges ) {
            src2tgt_frequency = e.getFrequency();
            src = e.getSource().getCode();
            tgt = e.getTarget().getCode();

            if( src == tgt ) {
                localDependency = (double) (e.getFrequency())/(e.getFrequency() + 1);
                e.setLocalDependencyScore(localDependency);
            } else {
                if( net.containsKey(tgt) && net.get(tgt).containsKey(src) ) {
                    // this means there is a reverse edge
                    tgt2src_frequency = net.get(tgt).get(src).getFrequency();
                } else tgt2src_frequency = 0;

                localDependency = (double) (src2tgt_frequency - tgt2src_frequency) / (src2tgt_frequency + tgt2src_frequency + 1);
                e.setLocalDependencyScore(localDependency);

                if( !candidateSuccessor.containsKey(src) ) candidateSuccessor.put(src, e);
                else if( candidateSuccessor.get(src).getLocalDependencyScore() < localDependency ) candidateSuccessor.put(src, e);

                if( !candidatePredecessor.containsKey(tgt) ) candidatePredecessor.put(tgt, e);
                else if( candidatePredecessor.get(tgt).getLocalDependencyScore() < localDependency ) candidatePredecessor.put(tgt, e);
            }
            System.out.println("DEBUG - #" + src + " => " + tgt + " : " + localDependency);
        }
    }

    private void pruneHeuristicNet() {
        HashSet<HeuristicEdge> toBeRemoved = new HashSet<>();
        String edgeID;
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
            edgeID = e.getID();
            src = e.getSource().getCode();
            tgt = e.getTarget().getCode();
//            if(src == tgt) continue; //loop guard

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

        for( HeuristicEdge e : toBeRemoved ) {
            edges.remove(e);
            System.out.println("DEBUG - removing edge: " + e.getSource().getLabel() + " > " + e.getTarget().getLabel());
        }

        System.out.println("DEBUG - edges after pruning: " + edges.size());
    }

    public BPMNDiagram getHeuristicNet() {
        BPMNDiagram diagram = new BPMNDiagramImpl("heuristic-net");
        HashMap<Integer, BPMNNode> mapping = new HashMap<>();
        String label;
        Activity task;
        BPMNNode src, tgt;

        System.out.println("DEBUG - building the Heuristic net with [ nodes : edges ] = [" + nodes.size() + " : " + edges.size() + " ]");

        for( int event : nodes.keySet() ) {
            label = events.get(event) + "\n(" + nodes.get(event).getFrequency() + ")";
            task = diagram.addActivity(label, false, false, false, false, false);
            mapping.put(event, task);
        }

        for( HeuristicEdge edge : edges ) {
            src = mapping.get(edge.getSource().getCode());
            tgt = mapping.get(edge.getTarget().getCode());
            label =  dFormat.format(edge.getLocalDependencyScore()) + "/" + edge.getFrequency();
            diagram.addFlow(src, tgt, label);
        }

        return diagram;
    }


    /* DEBUG methods */

    public void printFrequencies() {
        System.out.println("DEBUG - printing frequencies:");
        for( HeuristicNode node : nodes.values() ) System.out.println("DEBUG - #" + node.getLabel() + " = " + node.getFrequency());
    }

    public void printParallelisms() {
        System.out.println("DEBUG - printing parallelisms:");
        for( int A : parallelisms.keySet() ) {
            System.out.print("DEBUG - " + events.get(A) + " || " );
            for( int B : parallelisms.get(A) ) System.out.print( events.get(B) + ",");
            System.out.println();
        }
    }

    public void printConflicts() {
        System.out.println("DEBUG - printing conflicts:");
        for( int A : conflicts.keySet() ) {
            System.out.print("DEBUG - " + events.get(A) + " # " );
            for( int B : conflicts.get(A) ) System.out.print( events.get(B) + ",");
            System.out.println();
        }
    }
}
