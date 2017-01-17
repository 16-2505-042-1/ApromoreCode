package au.edu.qut.processmining.log;

import au.edu.qut.processmining.log.graph.fuzzy.FuzzyNet;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.util.HashMap;

/**
 * Created by Adriano on 14/06/2016.
 */
public class LogParser {

    private static final int STARTCODE = 0;
    private static final int ENDCODE = -1;

    public static FuzzyNet initFuzzyNet(XLog log) { return (new FuzzyNet(log)); }

    public static SimpleLog getSimpleLog(XLog log) {
        System.out.println("Log Parser - starting ... ");
        System.out.println("Log Parser - input log size: " + log.size());

        SimpleLog sLog;

        HashMap<String, Integer> parsed = new HashMap<>();  //this maps the original name of an event to its code
        HashMap<Integer, String> events = new HashMap<>();  //this maps the code of the event to its original name
        HashMap<String, Integer> traces = new HashMap<>();  //this is the simple log, each trace is a string associated to its frequency

        int tIndex; //index to iterate on the log traces
        int eIndex; //index to iterate on the events of the trace

        XTrace trace;
        String sTrace;

        XEvent event;
        String label;
        long totalEvents;
        int eventCounter;

        int totalTraces = log.size();
        long traceSize;

        events.put(STARTCODE, "autogen-start");
        events.put(ENDCODE, "autogen-end");

        totalEvents = 0;
        eventCounter = 1;

        for( tIndex = 0; tIndex < totalTraces; tIndex++ ) {
            /* we convert each trace in the log into a string
            *  each string will be a sequence of "::x" terminated with "::", where:
            *  '::' is a separator
            *  'x' is an integer encoding the name of the original event
            */
            trace = log.get(tIndex);
            traceSize = trace.size();

            sTrace = "::" + Integer.toString(STARTCODE) + ":";
            for( eIndex = 0; eIndex < traceSize; eIndex++ ) {
                totalEvents++;
                event = trace.get(eIndex);
                label = event.getAttributes().get("concept:name").toString();

                if( !parsed.containsKey(label) ) {
                    parsed.put(label, eventCounter);
                    events.put(eventCounter, label);
                    eventCounter++;
                }

                sTrace += ":" + parsed.get(label).toString() + ":";
            }
            sTrace += ":" + Integer.toString(ENDCODE) + "::";

            if( !traces.containsKey(sTrace) ) traces.put(sTrace, 0);
            traces.put(sTrace, traces.get(sTrace)+1);
        }

        System.out.println("Log Parser - total events parsed: " + totalEvents);
        System.out.println("Log Parser - total different events: " + (eventCounter-1));
        System.out.println("Log Parser - total different traces: " + traces.size() );
//        for( String t : traces.keySet() ) System.out.println("DEBUG - ["+ traces.get(t) +"] trace: " + t);

        System.out.println("DEBUG - final mapping:");
        for( int code : events.keySet() ) System.out.println("DEBUG - " + code + " = " + events.get(code));

        sLog = new SimpleLog(traces, events);
        sLog.setStartcode(STARTCODE);
        sLog.setEndcode(ENDCODE);

        return sLog;
    }
}
