/*
 * Copyright © 2009-2016 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package org.apromore.service.logvisualizer.impl;

import com.raffaeleconforti.log.util.LogImporter;
import org.apromore.plugin.DefaultParameterAwarePlugin;
import org.apromore.service.logvisualizer.LogVisualizerService;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.api.block.predicate.primitive.IntPredicate;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.ObjectIntPair;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.Tuples;
//import org.jgrapht.EdgeFactory;
//import org.jgrapht.Graph;
//import org.jgrapht.ext.DOTExporter;
//import org.jgrapht.graph.ClassBasedEdgeFactory;
//import org.jgrapht.graph.DefaultDirectedGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.plugins.bpmn.BpmnDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by Raffaele Conforti on 01/12/2016.
 */
@Service
public class LogVisualizerServiceImpl extends DefaultParameterAwarePlugin implements LogVisualizerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogVisualizerServiceImpl.class);

    private XEventClassifier full_classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
    private XEventClassifier name_classifier = new XEventNameClassifier();
    private XEventClassifier lifecycle_classifier = new XEventLifeTransClassifier();

    private boolean contain_start_events = false;

    private HashBiMap<String, Integer> simplified_names;
    private IntIntHashMap activity_frequency;
    private IntIntHashMap real_activity_frequency;
    private MutableList<IntIntPair> sorted_activity_frequency;

    private ObjectIntHashMap<Pair<Integer, Integer>> arcs_frequency;
    private MutableList<ObjectIntPair<Pair<Integer, Integer>>> sorted_arcs_frequency;

    private IntHashSet retained_activities;
    private Set<Pair<Integer, Integer>> retained_arcs;

    private int start = 1;
    private int end = 2;

    private final String START = "#C1C9B0";
    private final String END = "#C0A3A1";

    private final String BLUE_1 = "#F1EEF6";
    private final String BLUE_2 = "#BDC9E1";
    private final String BLUE_3 = "#74A9CF";
    private final String BLUE_4 = "#2B8CBE";
    private final String BLUE_5 = "#045A8D";

    private final String RED_1 = "#FEF0D9";
    private final String RED_2 = "#FDCC8A";
    private final String RED_3 = "#FC8D59";
    private final String RED_4 = "#E34A33";
    private final String RED_5 = "#B30000";

    public static void main(String[] args) {
        LogVisualizerServiceImpl l = new LogVisualizerServiceImpl();
        XLog log = null;
        try {
//            log = ImportEventLog.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/IdeaProjects/ApromoreCodeServerNew/Compare-Logic/src/test/resources/CAUSCONC-1/bpLog3.xes");
//            log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/Raw data after import.xes.gz");
            log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Dropbox/Demonstration examples/Discover Process Model/Synthetic Log with Subprocesses.xes.gz");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONArray s = l.generateJSONArrayFromLog(log, 0, 0, true);
        System.out.println(s);
//        l.generateDOTFromLog(log, 0, 0, true);
    }

//    public void generateDOTFromLog(XLog log, double activities, double arcs, boolean selected_frequency) {
//        try {
//            BPMNDiagram bpmnDiagram = generateDiagramFromLog(log, activities, arcs);
//            generateDOTFromBPMN(bpmnDiagram, selected_frequency);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void generateDOTFromBPMN(BPMNDiagram bpmnDiagram, boolean selected_frequency) throws JSONException, IOException {
//        DOTExporter dotExporter = new DOTExporter();
//        DefaultDirectedGraph graph = new DefaultDirectedGraph(String.class);
//
//        for(BPMNNode node : bpmnDiagram.getNodes()) {
//            graph.addVertex(node.getLabel());
//        }
//
//        for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> edge : bpmnDiagram.getEdges()) {
//            graph.addEdge(edge.getSource().getLabel(), edge.getTarget().getLabel(), edge.getSource().getLabel() + " " + edge.getTarget().getLabel());
//        }
//
//        dotExporter.export(new FileWriter("dot.dot"), graph);
//    }

    @Override
    public String visualizeLog(XLog log, double activities, double arcs) {
        try {
            BPMNDiagram bpmnDiagram = generateDiagramFromLog(log, activities, arcs);

            UIContext context = new UIContext();
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIPluginContext uiPluginContext = context.getMainPluginContext();
            BpmnDefinitions.BpmnDefinitionsBuilder definitionsBuilder = new BpmnDefinitions.BpmnDefinitionsBuilder(uiPluginContext, bpmnDiagram);
            BpmnDefinitions definitions = new BpmnDefinitions("definitions", definitionsBuilder);

            String sb = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n " +
                    "xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\"\n " +
                    "xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n " +
                    "xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"\n " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n " +
                    "targetNamespace=\"http://www.omg.org/bpmn20\"\n " +
                    "xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">") +
                    definitions.exportElements() +
                    "</definitions>";

            return sb;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONArray generateJSONArrayFromLog(XLog log, double activities, double arcs, boolean selected_frequency) {
        try {
            BPMNDiagram bpmnDiagram = generateDiagramFromLog(log, activities, arcs);
            return generateJSONFromBPMN(bpmnDiagram, selected_frequency);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private BPMNDiagram generateDiagramFromLog(XLog log, double activities, double arcs) {
        initializeDatastructures();
        List<IntList> simplified_log = simplifyLog(log);
        filterLog(simplified_log, activities);
        retained_arcs = selectArcs(arcs);

        BPMNDiagram bpmnDiagram = new BPMNDiagramImpl("");
        IntObjectHashMap<BPMNNode> map = new IntObjectHashMap();
        for(int i : retained_activities.toArray()) {
            BPMNNode node = bpmnDiagram.addActivity(simplified_names.inverse().get(i), false, false, false, false, false);
            map.put(i, node);
        }

        for(Pair<Integer, Integer> arc : retained_arcs) {
            BPMNNode source = map.get(arc.getOne());
            BPMNNode target = map.get(arc.getTwo());
            bpmnDiagram.addFlow(source, target, "[" + arcs_frequency.get(arc) + "]");
        }
        return collapseStartCompleteActivities(bpmnDiagram);
    }

    private BPMNDiagram collapseStartCompleteActivities(BPMNDiagram bpmnDiagram) {
        BPMNDiagram diagram = new BPMNDiagramImpl("");

        Map<String, BPMNNode> nodes_map = new HashMap<>();
        for(BPMNNode node : bpmnDiagram.getNodes()) {
            String collapsed_name = getCollapsedEvent(node.getLabel());
            if(!nodes_map.containsKey(collapsed_name)) {
                BPMNNode collapsed_node = diagram.addActivity(collapsed_name, false, false, false, false, false);
                nodes_map.put(collapsed_name, collapsed_node);
            }
        }

        Set<Pair<String, String>> edges = new HashSet<>();
        for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> edge : bpmnDiagram.getEdges()) {
            String source_name = edge.getSource().getLabel();
            String target_name = edge.getTarget().getLabel();

            String collapsed_source_name = getCollapsedEvent(source_name);
            String collapsed_target_name = getCollapsedEvent(target_name);

            BPMNNode source = nodes_map.get(collapsed_source_name);
            BPMNNode target = nodes_map.get(collapsed_target_name);

            Pair pair = Tuples.pair(collapsed_source_name, collapsed_target_name);
            if(!collapsed_source_name.equals(collapsed_target_name) || isSingleTypeEvent(getEventNumber(source_name)) || isCompleteEvent(source_name)) {
                if(!edges.contains(pair)) {
                    diagram.addFlow(source, target, edge.getLabel());
                    edges.add(pair);
                }
            }
        }
        return diagram;
    }

    private void initializeDatastructures() {
        simplified_names = new HashBiMap<>();
        activity_frequency = new IntIntHashMap();
        real_activity_frequency = new IntIntHashMap();
        arcs_frequency = new ObjectIntHashMap<>();
    }

    private List<IntList> simplifyLog(XLog log) {
        List<IntList> simplified_log = new ArrayList<>();

        simplified_names.put("Start", start);
        simplified_names.put("End", end);

        for(XTrace trace : log) {
            IntArrayList simplified_trace = new IntArrayList(trace.size());

            activity_frequency.addToValue(start, 1);
            simplified_trace.add(start);

            for(XEvent event : trace) {
                String name = full_classifier.getClassIdentity(event);
                if(isStartEvent(name)) contain_start_events = true;

                Integer simplified_event;
                if((simplified_event = getEventNumber(name)) == null) {
                    simplified_event = simplified_names.size() + 1;
                    simplified_names.put(name, simplified_event);
                }

                activity_frequency.addToValue(simplified_event, 1);
                real_activity_frequency.addToValue(simplified_event, 1);
                simplified_trace.add(simplified_event);
            }
            activity_frequency.addToValue(end, 1);
            simplified_trace.add(end);

            simplified_log.add(simplified_trace);
        }

        sorted_activity_frequency = activity_frequency.keyValuesView().toList();
        sorted_activity_frequency.sort(new Comparator<IntIntPair>() {
            @Override
            public int compare(IntIntPair o1, IntIntPair o2) {
                return Integer.compare(o2.getTwo(), o1.getTwo());
            }
        });

        return simplified_log;
    }

    private List<IntList> filterLog(List<IntList> log, double activities) {
        List<IntList> filtered_log = new ArrayList<>();
        retained_activities = selectActivities(activities);

        for(IntList trace : log) {
            IntArrayList filtered_trace = (IntArrayList) trace.reject(new IntPredicate() {
                @Override
                public boolean accept(int i) {
                    return !retained_activities.contains(i);
                }
            });

//            for(int i = 0; i < filtered_trace.size() - 1; i++) {
//                arcs_frequency.addToValue(Tuples.pair(filtered_trace.get(i), filtered_trace.get(i + 1)), 1);
//            }
            for(int i = 0; i < filtered_trace.size() - 1; i++) {
                for(int j = i + 1; j < filtered_trace.size(); j++) {
                    if (isAcceptableTarget(filtered_trace.get(i), filtered_trace.get(j))) {
                        arcs_frequency.addToValue(Tuples.pair(filtered_trace.get(i), filtered_trace.get(j)), 1);
//                        System.out.println(getEventFullName(filtered_trace.get(i)) + "->" + getEventFullName(filtered_trace.get(j)));
                        break;
//                    }else {
//                        System.out.println(getEventFullName(filtered_trace.get(i)) + "-X" + getEventFullName(filtered_trace.get(j)));
                    }
                }
            }

            filtered_log.add(filtered_trace);
        }

        sorted_arcs_frequency = arcs_frequency.keyValuesView().toList();
        sorted_arcs_frequency.sort(new Comparator<ObjectIntPair<Pair<Integer, Integer>>>() {
            @Override
            public int compare(ObjectIntPair<Pair<Integer, Integer>> o1, ObjectIntPair<Pair<Integer, Integer>> o2) {
                return Integer.compare(o2.getTwo(), o1.getTwo());
            }
        });

        return filtered_log;
    }

    private boolean isAcceptableTarget(int source_event, int target_event) {
        if(source_event == 1 && target_event == 2) return false;

        String source_name = getEventFullName(source_event);
        String target_name = getEventFullName(target_event);

        if(source_event == 1) return (isStartEvent(target_name) || isSingleTypeEvent(target_event));
        if(target_event == 2) return (isCompleteEvent(source_name) || isSingleTypeEvent(source_event));

        if(isStartEvent(source_name)) {
            String expected_target_name = getCompleteEvent(source_name);
            if (!isSingleTypeEvent(source_event)) return getEventNumber(expected_target_name) == target_event;
            else return isStartEvent(target_name) || isSingleTypeEvent(target_event);
        }else if(isCompleteEvent(source_name)) {
            return (isStartEvent(target_name) || isSingleTypeEvent(target_event));
        }
        return false;
    }

    private IntHashSet selectActivities(double activities) {
        IntHashSet retained_activities = new IntHashSet();
        retained_activities.add(start);
        retained_activities.add(end);

        long max = real_activity_frequency.max();
        for(int i = 0; i < sorted_activity_frequency.size(); i++) {
            double current = sorted_activity_frequency.get(i).getTwo();
            if(current >= max * activities) {
                retained_activities.add(sorted_activity_frequency.get(i).getOne());
//            }else {
//                return retained_activities;
            }
        }

        if(contain_start_events) {
            MutableIntIterator iterator = retained_activities.intIterator();
            while (iterator.hasNext()) {
                int i = iterator.next();
                String name = getEventFullName(i);
                String name_to_check = "";

                if (isStartEvent(name)) name_to_check = getCompleteEvent(name);
                else if (isCompleteEvent(name)) name_to_check = getStartEvent(name);

                if (!isSingleTypeEvent(getEventNumber(name)) && !retained_activities.contains(getEventNumber(name_to_check))) {
                    iterator.remove();
                }
            }
        }
        return retained_activities;
    }

    private HashSet<Pair<Integer, Integer>> selectArcs(double arcs) {
        HashSet<Pair<Integer, Integer>> retained_arcs = new HashSet();

        double threshold = arcs_frequency.max() * arcs;

        retained_arcs.addAll(arcs_frequency.keySet());

        for(int i = sorted_arcs_frequency.size() - 1; i >= 0; i--) {
            double current = sorted_arcs_frequency.get(i).getTwo();
            Pair<Integer, Integer> arc = sorted_arcs_frequency.get(i).getOne();
            if(current < threshold) {
                if(retained_arcs.contains(arc)) {
                    retained_arcs.remove(arc);
                    if (!reachable(arc.getTwo(), retained_arcs) || !reaching(arc.getOne(), retained_arcs)) {
                        retained_arcs.add(arc);
                    }
                }
            }else {
                return retained_arcs;
            }
        }

        return retained_arcs;
    }

    private boolean reachable(int node, HashSet<Pair<Integer, Integer>> retained_arcs) {
        if(node == 1) return true;

        IntHashSet visited = new IntHashSet();
        IntArrayList reached = new IntArrayList();
        reached.add(1);

        while (reached.size() > 0) {
            int current = reached.removeAtIndex(0);
            for (Pair<Integer, Integer> arc : retained_arcs) {
                if(arc.getOne() == current) {
                    if(arc.getTwo() == node) {
                        return true;
                    }else if(!visited.contains(arc.getTwo())) {
                        visited.add(arc.getTwo());
                        reached.add(arc.getTwo());
                    }
                }
            }
        }

        return false;
    }

    private boolean reaching(int node, HashSet<Pair<Integer, Integer>> retained_arcs) {
        if(node == 2) return true;

        IntHashSet visited = new IntHashSet();
        IntArrayList reached = new IntArrayList();
        reached.add(2);

        while (reached.size() > 0) {
            int current = reached.removeAtIndex(0);
            for (Pair<Integer, Integer> arc : retained_arcs) {
                if(arc.getTwo() == current) {
                    if(arc.getOne() == node) {
                        return true;
                    }else if(!visited.contains(arc.getOne())) {
                        visited.add(arc.getOne());
                        reached.add(arc.getOne());
                    }
                }
            }
        }

        return false;
    }

    private JSONArray generateJSONFromBPMN(BPMNDiagram bpmnDiagram, boolean selected_frequency) throws JSONException {
        JSONArray graph = new JSONArray();
        Map<BPMNNode, Integer> mapping = new HashMap<>();
        int i = 1;
        int start_node = -1;
        int end_node = -1;
        for(BPMNNode node : bpmnDiagram.getNodes()) {
            JSONObject jsonOneNode = new JSONObject();
            mapping.put(node, i);
            jsonOneNode.put("id", i);
            if(node.getLabel().equals("Start")) {
                start_node = i;
                jsonOneNode.put("name", "");
                jsonOneNode.put("shape", "ellipse");
                jsonOneNode.put("color", START);
                jsonOneNode.put("width", "15px");
                jsonOneNode.put("height", "15px");
            }else if(node.getLabel().equals("End")) {
                end_node = i;
                jsonOneNode.put("name", "");
                jsonOneNode.put("shape", "ellipse");
                jsonOneNode.put("color", END);
                jsonOneNode.put("width", "15px");
                jsonOneNode.put("height", "15px");
            }else {
                jsonOneNode.put("name", node.getLabel());
                jsonOneNode.put("shape", "roundrectangle");
                if(selected_frequency) jsonOneNode.put("color", getFrequencyColor(node, bpmnDiagram.getNodes()));
                else jsonOneNode.put("color", getDurationColor(node, bpmnDiagram.getNodes()));
                jsonOneNode.put("width", "60px");
                jsonOneNode.put("height", "20px");
            }
            JSONObject jsonDataNode = new JSONObject();
            jsonDataNode.put("data", jsonOneNode);
            graph.put(jsonDataNode);
            i++;
        }

        double maxWeight = 0.0;
        for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> edge : bpmnDiagram.getEdges()) {
            String number = edge.getLabel();
            if (number.contains("[")) {
                number = number.substring(1, number.length() - 1);
            } else {
                number = "0";
            }
            maxWeight = Math.max(maxWeight, Double.parseDouble(number));
        }

        for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> edge : bpmnDiagram.getEdges()) {
            int source = mapping.get(edge.getSource());
            int target = mapping.get(edge.getTarget());
            String number = edge.getLabel();
            if(number.contains("[")) {
                number = number.substring(1, number.length() - 1);
            }else {
                number = "1";
            }

            JSONObject jsonOneLink = new JSONObject();
            jsonOneLink.put("source", source);
            jsonOneLink.put("target", target);

            if(source == start_node) jsonOneLink.put("style", "dashed");
            else if(target == end_node) jsonOneLink.put("style", "dashed");
            else jsonOneLink.put("style", "solid");

            BigDecimal bd = new BigDecimal((Double.parseDouble(number) * 100.0 / maxWeight));
            bd = bd.setScale(2, RoundingMode.HALF_UP);;
            jsonOneLink.put("strength", bd.doubleValue());
            jsonOneLink.put("label", number);
            JSONObject jsonDataLink = new JSONObject();
            jsonDataLink.put("data", jsonOneLink);
            graph.put(jsonDataLink);
        }

        return graph;
    }

    private int getEventFrequency(String event) {
        if(getEventNumber(event) == null) {
            String start_event = event + "+start";
            String complete_event = event + "+complete";
            if(getEventNumber(start_event) != null && getEventNumber(complete_event) != null) {
                return Math.min(getEventFrequency(start_event), getEventFrequency(complete_event));
            }else if(getEventNumber(start_event) != null) {
                return getEventFrequency(start_event);
            }else {
                return getEventFrequency(complete_event);
            }
        }else {
            return activity_frequency.get(getEventNumber(event));
        }
    }

    private String getFrequencyColor(BPMNNode node, Set<BPMNNode> nodes) {
        int max = 0;
        for(BPMNNode n : nodes) {
            max = Math.max(max, getEventFrequency(node.getLabel()));
        }
        int step = max / 5;
        int node_frequency = getEventFrequency(node.getLabel());
        if(node_frequency >= (max - (1 * step))) return BLUE_5;
        if(node_frequency >= (max - (2 * step))) return BLUE_4;
        if(node_frequency >= (max - (3 * step))) return BLUE_3;
        if(node_frequency >= (max - (4 * step))) return BLUE_2;
        return BLUE_1;
    }

    private String getDurationColor(BPMNNode node, Set<BPMNNode> nodes) {
        int max = 0;
        for(BPMNNode n : nodes) {
            max = Math.max(max, activity_frequency.get(getEventNumber(n.getLabel())));
        }
        int step = max / 5;
        int node_frequency = activity_frequency.get(getEventNumber(node.getLabel()));
        if(node_frequency >= (max - (1 * step))) return RED_5;
        if(node_frequency >= (max - (2 * step))) return RED_4;
        if(node_frequency >= (max - (3 * step))) return RED_3;
        if(node_frequency >= (max - (4 * step))) return RED_2;
        return RED_1;
    }

    private String getEventFullName(int event) {
        return simplified_names.inverse().get(event);
    }

    private Integer getEventNumber(String event) {
        return simplified_names.get(event);
    }
    
    private boolean isSingleTypeEvent(int event) {
        String name = getEventFullName(event);
        if(isStartEvent(name) && getEventNumber(getCompleteEvent(name)) != null) return false;
        if(isCompleteEvent(name) && getEventNumber(getStartEvent(name)) != null) return false;
        return true;
    }

    private boolean isStartEvent(String name) {
        return name.endsWith("+start");
    }

    private boolean isCompleteEvent(String name) {
        return name.endsWith("+complete");
    }

    private String getStartEvent(String name) {
        return name.substring(0, name.length() - 8) + "start";
    }

    private String getCompleteEvent(String name) {
        return name.substring(0, name.length() - 5) + "complete";
    }

    private String getCollapsedEvent(String name) {
        if(isStartEvent(name)) return name.substring(0, name.length() - 6);
        if(isCompleteEvent(name)) return name.substring(0, name.length() - 9);
        return name;
    }

}
