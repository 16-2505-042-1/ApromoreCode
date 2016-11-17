package au.edu.qut.promplugins;

import au.edu.qut.processmining.miners.heuristic.net.HeuristicNet;
import au.edu.qut.processmining.miners.heuristic.HeuristicMinerPlus;
import au.edu.qut.processmining.miners.heuristic.ui.HMPlusUI;
import au.edu.qut.processmining.miners.heuristic.ui.HMPlusUIResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Adriano on 25/10/2016.
 */

@Plugin(
        name = "Mine BPMN model with HM+",
        parameterLabels = { "Event Log" },
        returnLabels = { "HM+ output BPMN model" },
        returnTypes = { BPMNDiagram.class },
        userAccessible = true,
        help = "Returns a BPMN model mined with Heuristic Miner Plus"
)
public class HeuristicMinerPlusPlugin {

    @UITopiaVariant(
            affiliation = "University of Tartu",
            author = "Adriano Augusto",
            email = "adriano.augusto@ut.ee"
    )
    @PluginVariant(variantLabel = "Mine BPMN model with HM+", requiredParameterLabels = {0})
    public static BPMNDiagram mineBPMNModelWithHMP(UIPluginContext context, XLog log) {
        boolean debug = true;
        boolean onlyHeuristicNet = false;
        BPMNDiagram output;

        HMPlusUI gui = new HMPlusUI();
        HMPlusUIResult result = gui.showGUI(context);

        HeuristicMinerPlus hmp = new HeuristicMinerPlus();

        if( onlyHeuristicNet ) hmp.mineHeuristicNet(log, result.getDependencyThreshold(), result.getPositiveObservations(), result.getRelative2BestThreshold());
        else hmp.mineBPMNModel(log, result.getDependencyThreshold(), result.getPositiveObservations(), result.getRelative2BestThreshold());

        HeuristicNet heuristicNet = hmp.getHeuristicNet();

        if( debug ) {
            heuristicNet.printFrequencies();
            heuristicNet.printConflicts();
            heuristicNet.printParallelisms();
        }

        if( onlyHeuristicNet ) output = heuristicNet.getHeuristicDiagram(false);
        else output = hmp.getBPMNDiagram();

        return output;
    }
}
