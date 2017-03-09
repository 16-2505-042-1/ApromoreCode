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

package au.edu.qut.promplugins;

import au.edu.qut.processmining.miners.yam.dfgp.DirectlyFollowGraphPlus;
import au.edu.qut.processmining.miners.yam.YAM;
import au.edu.qut.processmining.miners.yam.ui.miner.YAMUI;
import au.edu.qut.processmining.miners.yam.ui.miner.YAMUIResult;
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
        name = "Discover BPMN model with YAM",
        parameterLabels = { "Event Log" },
        returnLabels = { "YAM output" },
        returnTypes = { BPMNDiagram.class },
        userAccessible = true,
        help = "Returns a BPMN model mined with YAM"
)
public class YAMPlugin {

    @UITopiaVariant(
            affiliation = "University of Tartu",
            author = "Adriano Augusto",
            email = "adriano.augusto@ut.ee"
    )
    @PluginVariant(variantLabel = "Discover BPMN model with YAM", requiredParameterLabels = {0})
    public static BPMNDiagram discoverBPMNModelWithYAM(UIPluginContext context, XLog log) {
        boolean debug = false;
        BPMNDiagram output;

        YAMUI gui = new YAMUI();
        YAMUIResult result = gui.showGUI(context, "Setup HM+");

        YAM hmp = new YAM();
        hmp.mineBPMNModel( log, result.getFrequencyThreshold(), result.getParallelismsThreshold(),
                                result.isReplaceIORs(), result.getStructuringTime());

        DirectlyFollowGraphPlus dfgp = hmp.getDfgp();

        if( debug ) {
            dfgp.printFrequencies();
            dfgp.printParallelisms();
        }

        output = hmp.getBPMNDiagram();

        return output;
    }
}
