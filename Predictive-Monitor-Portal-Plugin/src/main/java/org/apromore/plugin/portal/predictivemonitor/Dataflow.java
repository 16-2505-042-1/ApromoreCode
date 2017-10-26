/*
 * Copyright © 2009-2017 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package org.apromore.plugin.portal.predictivemonitor;

// Java 2 Standard Edition
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

// Third party packages
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dataflow implements Closeable {

    final private static Logger LOGGER = LoggerFactory.getLogger(Dataflow.class.getCanonicalName());

    final private Consumer<String, String> consumer;
    final private Thread                   consumerThread;
    final private List<Process>            processors = new ArrayList<>();
    final private String                   eventsTopic;
    final private Set<String>              topicNames = new HashSet<>();
    final private String                   kafkaHost;
    final private File                     nirdizatiDirectory;
    final private String                   pythonCommand;
    final private AtomicBoolean            closed = new AtomicBoolean(false);
    final private String                   name;

    final List<DataflowListener>       listeners = Collections.synchronizedList(new ArrayList<>());
    final private List<Predictor>      predictors;
    final private Set<DataflowElement> elements = new HashSet<>();

    // TODO: give these proper accessors, and reactive
    int caseCount = 0;
    int completedEventCount = 0;
    int completedCaseCount = 0;
    int completedCaseEventCount = 0;
    Duration totalCompletedCaseDuration = Duration.ZERO;
    boolean fed = false;

    /**
     * Create a Nirdizati dataflow.
     *
     * The dataflow consists of various Kafka topics and python-based processor processes.
     *
     * @param logName  suffix for generated Kafka topics, e.g. "bpi_12"
     * @param kafkaHost  colon-delimited address and port, e.g. "localhost:9092"
     * @param nirdizatiDirectory
     * @param pythonCommand
     * @param desktop
     * @param predictors  may be zero-length, but never <code>null</code>
     */
    Dataflow(String logName, String kafkaHost, File nirdizatiDirectory, String pythonCommand, List<Predictor> predictors) {
        LOGGER.info("Create dataflow for log named " + logName);

        this.kafkaHost          = kafkaHost;
        this.nirdizatiDirectory = nirdizatiDirectory;
        this.pythonCommand      = pythonCommand;
        this.predictors         = predictors;
        elements.addAll(predictors);

        this.eventsTopic        = logName + "_events";
        String prefixesTopic    = logName + "_prefixes";
        String predictionsTopic = logName + "_predictions";
        String resultsTopic     = logName + "_events_with_predictions";

        this.name = logName;

        // Create the topics
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaHost);

        try (AdminClient adminClient = AdminClient.create(props)) {
            ListTopicsResult result = adminClient.listTopics();
            Set<String> nameSet = result.names().get();
            LOGGER.info("Topic names: " + nameSet);
            topicNames.addAll(Arrays.asList(eventsTopic, prefixesTopic, predictionsTopic, resultsTopic));
            LOGGER.info("Creating topics " + topicNames);
            List<NewTopic> topics = new ArrayList<>();
            for (String topicName: topicNames) {
                if (nameSet.contains(topicName)) {
                    LOGGER.info("Topic " + topicName + " already exists");
                } else {
                    topics.add(new NewTopic(topicName, 1, (short) 1));
                }
            }
            adminClient.createTopics(topics);
            LOGGER.info("Created topics " + topics);

        } catch (ExecutionException | InterruptedException e) {
            LOGGER.warn("Unable to list topics", e);
        }

        // Listen to the source topic
        props.put("group.id", "join(" + eventsTopic + "," + predictionsTopic + ")");
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", StringDeserializer.class);
        consumer = new KafkaConsumer(props);
        consumerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Map<String, DataflowEvent> latestEventInCaseMap = new HashMap<>();

                    consumer.subscribe(Arrays.asList(resultsTopic));
                    while (!closed.get()) {
                        ConsumerRecords<String, String> records = consumer.poll(10000);
                        for (ConsumerRecord<String, String> record: records) {
                            try {
                                JSONObject json = new JSONObject(record.value());
                                DataflowEvent event = new DataflowEvent(json, latestEventInCaseMap);

                                // TODO: synchronize all the following accesses to the parent instance's member fields
                                String caseId = event.getCaseId();
                                DataflowEvent latestEvent = latestEventInCaseMap.get(caseId);
                                if (latestEvent == null || latestEvent.getIndex() < event.getIndex()) {
                                    if (latestEventInCaseMap.put(caseId, event) == null) {
                                        caseCount++;
                                    }
                                }

                                completedEventCount++;
                                if (event.isLast()) {
                                    Duration eventDuration = event.getDuration();
                                    if (eventDuration == null) {  // TODO: figure out how to cope with out-of-order events
                                        LOGGER.warn("Last event of case " + caseId + " did not have a duration; skipping it");
                                    } else {
                                        completedCaseCount++;
                                        completedCaseEventCount += event.getIndex();
                                        totalCompletedCaseDuration = totalCompletedCaseDuration.plus(event.getDuration());
                                    }
                                }

                                for (DataflowListener listener: listeners) {
                                    try {
                                        listener.notify(event);
                                    } catch (Throwable t) {
                                        LOGGER.warn("Mishap notifying event listener " + listener, t);
                                    }
                                }

                            } catch (JSONException e) {
                                LOGGER.warn("Unable to parse consumer record as JSON object: " + record.value(), e);
                            }
                        }
                    }

                } catch (WakeupException e) {
                    if (!closed.get()) {
                        LOGGER.error("Wakeup exception while Kafka consumer thread wasn't closed", e);
                    }
                } catch (Throwable e) {
                    LOGGER.error("Unexpected failure in Kafka consumer thread", e);
                } finally {
                    consumer.close();
                }
            }
        });
        consumerThread.start();

        // Create the processors
        LOGGER.info("Creating dataflow with " + predictors.size() + " predictor(s): " + predictors);
        elements.add(new ProcessDataflowElement(nirdizatiDirectory, pythonCommand, "PredictiveMethods/collate-events.py", kafkaHost, eventsTopic, prefixesTopic));
        elements.add(new ProcessDataflowElement(nirdizatiDirectory, pythonCommand, "PredictiveMethods/join-events-to-predictions.py", kafkaHost, eventsTopic, predictionsTopic, resultsTopic, Integer.toUnsignedString(predictors.size())));
        for (DataflowElement element: elements) {
            try {
                element.start(kafkaHost, prefixesTopic, predictionsTopic);
            } catch (PredictorException e) {
                LOGGER.error("Unable to create dataflow element", e);
            }
        }
    }

    List<Predictor> getPredictors() { return predictors; }

    public String getName() { return name; }

    /**
     * Feed a log into the sink topic of the dataflow.
     *
     * @param log  an OpenXES log in Ilya's particular format
     */
    void exportLog(XLog log) {
    
        LOGGER.info("Exporting log events");

        // Configuration
        Properties props = new Properties();
        props.put("bootstrap.servers",  kafkaHost);
        props.put("acks",               "all");
        props.put("retries",            0);
        props.put("batch.size",         16384);
        props.put("linger.ms",          1);
        //props.put("timeout.ms",         5000);
        props.put("max.block.ms",       5000);
        //props.put("metadata.fetch.timeout.ms", 5000);
        //props.put("request.timeout.ms", 5000);
        props.put("buffer.memory",      33554432);
        props.put("key.serializer",     StringSerializer.class);
        props.put("value.serializer",   StringSerializer.class);
        
        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            // TODO: create the Kafka topic if it doesn't exist
            
            // Merge the XES events of all traces into a single list of JSON objects
            List<JSONObject> jsons = new ArrayList<>();
            for (XTrace trace: log) {
                for (XEvent event: trace) {
                    JSONObject json = new JSONObject();
                    json.put("log", "bpi_12");
                    json.put("case_id", trace.getAttributes().get("concept:name").toString());
                    json.put("remtime", "0"); 
                    addAttributes(log, json);
                    addAttributes(trace, json);
                    addAttributes(event, json);
                    jsons.add(json);
                }   
            }   
            
            // Sort the events by time
            final DatatypeFactory f = DatatypeFactory.newInstance();
            Collections.sort(jsons, new Comparator<JSONObject>() {
                public int compare(JSONObject a, JSONObject b) { 
                    return f.newXMLGregorianCalendar(a.optString("time")).compare(f.newXMLGregorianCalendar(b.optString("time")));
                }   
            }); 
            
            // Export to the Kafka topic
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            for (JSONObject json: jsons) {
                json.put("time", dateFormat.format(f.newXMLGregorianCalendar(json.optString("time")).toGregorianCalendar().getTime()));
                producer.send(new ProducerRecord<String,String>(eventsTopic, json.toString()));
            }
            fed = true;
            LOGGER.info("Exported " + jsons.size() + " log events");
            
        } catch (DatatypeConfigurationException | JSONException e) {
            //Messagebox.show("Unable to export log", "Attention", Messagebox.OK, Messagebox.ERROR);
            LOGGER.error("Unable to export log events", e);
            return;
        }   
    }  

    private static void addAttributes(XAttributable attributable, JSONObject json) throws JSONException {
        for (Map.Entry<String, XAttribute> entry: attributable.getAttributes().entrySet()) {
            switch (entry.getKey()) {
            case "concept:name":
            case "creator":
            case "library":
            case "lifecycle:model":
            case "lifecycle:transition":
            case "org:resource":
            case "variant":
            case "variant-index":
                break;
            case "time:timestamp":
                json.put("time", entry.getValue().toString());
                break;
            default:
                json.put(entry.getKey(), entry.getValue().toString());
                break;
            }
        }
    }

    // Implementation of Closeable

    /**
     * Kill all the processors and delete all the topics.
     */
    public void close() {

        // Kill the processors
        while (!processors.isEmpty()) {
            Process p = processors.get(0);
            LOGGER.info("Killing process " + p + ", alive? " + p.isAlive());
            p.destroy();
            try {
                int code = p.waitFor();
                LOGGER.info("Killed process with return code " + code);
                processors.remove(p);

            } catch (InterruptedException | RuntimeException e) {
                LOGGER.warn("Unable to kill process", e);
            }
        }
        for (DataflowElement element: elements) {
            element.stop();
        }

        // Kill the consumer thread
        closed.set(true);
        consumer.wakeup();

        // Delete the topics
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaHost);

        try (AdminClient adminClient = AdminClient.create(props)) {
            ListTopicsResult result = adminClient.listTopics();
            Set<String> nameSet = result.names().get();
            LOGGER.info("Topic names reported by Kafka: " + nameSet);
            LOGGER.info("Topic names in this dataflow: " + topicNames);
            DeleteTopicsResult result2 = adminClient.deleteTopics(topicNames);
            result2.all().get();
            LOGGER.info("Deleted topics" + topicNames);
            topicNames.clear();
        }
        catch (ExecutionException | InterruptedException e) {
            LOGGER.warn("Unable to list topics", e);
        }
    }
}
