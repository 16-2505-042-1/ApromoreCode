package au.edu.qut.promplugins;

import au.edu.qut.processmining.log.LogParser;
import au.edu.qut.processmining.log.graph.LogGraph;
import au.edu.qut.processmining.miner.FakeMiner;
import au.edu.qut.processmining.ui.MinerUI;
import au.edu.qut.processmining.ui.MinerUIResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Adriano on 17/06/2016.
 */

@Plugin(
        name = "Get Fuzzy Net from Log",
        parameterLabels = { "Event Log" },
        returnLabels = { "Fuzzy Net" },
        returnTypes = { BPMNDiagram.class },
        userAccessible = true,
        help = "Returns the log as a fuzzy net"
)
public class LogFuzzyNetPlugin {

    @UITopiaVariant(
            affiliation = "Queensland University of Technology",
            author = "Adriano Augusto",
            email = "a.augusto@qut.edu.au"
    )
    @PluginVariant(variantLabel = "Get Fuzzy Net from Log", requiredParameterLabels = {0})
    public static BPMNDiagram getFuzzyNetFromLog(UIPluginContext context, XLog log) {
        LogGraph graph = LogParser.generateLogGraph(log);
        return graph.getLogAsDiagram();
    }
}