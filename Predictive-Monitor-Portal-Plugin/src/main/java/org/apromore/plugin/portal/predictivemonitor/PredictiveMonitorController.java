/*
 * Copyright © 2009-2018 The Apromore Initiative.
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
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Third party packages
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.ListModelMap;
import org.zkoss.zul.Window;

// Local packages
import org.apromore.plugin.portal.PortalContext;

/**
 * In MVC terms, this is a controller whose corresponding model is {@link Dataflow} and corresponding view is <code>predictive_monitor.zul</code>.
 */
public class PredictiveMonitorController implements Closeable, EventListener<DataflowEvent> {

    private static Logger LOGGER = LoggerFactory.getLogger(PredictiveMonitorController.class.getCanonicalName());

    private final Dataflow dataflow;
    private final DataflowListener dataflowListener;

    private final Label runningCasesLabel;
    private final Label completedCasesLabel;
    private final Label completedEventsLabel;
    private final Label averageCaseLengthLabel;
    private final Label averageCaseDurationLabel;

    private final NumberFormat numberFormat = new DecimalFormat("0.##");

    private final ListModelList<DataflowEvent> eventsModel = new ListModelList<>();
    //private final ListModelList<DataflowEvent> latestEventsModel = new ListModelList<>();

    /**
     * @param dataflow  never <code>null</code>
     */
    public PredictiveMonitorController(PortalContext portalContext, Dataflow dataflow) throws IOException {

        this.dataflow = dataflow;

        Window window = (Window) portalContext.getUI().createComponent(getClass().getClassLoader(), "zul/predictive_monitor.zul", null, null);

        Listbox eventsListbox    = (Listbox) window.getFellow("events");
        //Listbox latestEventsListbox = (Listbox) window.getFellow("latestEvents");

        runningCasesLabel        = (Label) window.getFellow("runningCases");
        completedCasesLabel      = (Label) window.getFellow("completedCases");
        completedEventsLabel     = (Label) window.getFellow("completedEvents");
        averageCaseLengthLabel   = (Label) window.getFellow("averageCaseLength");
        averageCaseDurationLabel = (Label) window.getFellow("averageCaseDuration");
        
        updateUI();

        for (Predictor predictor: dataflow.getPredictors()) {
            predictor.addHeaders(eventsListbox.getListhead());
            //predictor.addHeaders(latestEventsListbox.getListhead());
        }
        eventsListbox.setRows(15);  // TODO: figure out how to make vflex work with this
        ListitemRenderer<DataflowEvent> renderer = new ListitemRenderer<DataflowEvent> () {
            public void render(Listitem item, DataflowEvent event, int index) {
                item.setStyle(event.isLast() ? "background-color: #EEFFEE" : "");
                item.appendChild(new Listcell(event.getCaseId()));
                item.appendChild(new Listcell(event.isLast() ? "Yes" : "No"));
                item.appendChild(new Listcell(Integer.toString(event.getIndex())));
                item.appendChild(new Listcell(formatTime(event.getStartTime())));
                item.appendChild(new Listcell(formatTime(event.getTime())));
                item.appendChild(new Listcell(formatTime(event.getEndTime())));
                item.appendChild(new Listcell(event.getFormattedDuration()));

                // Populate the columns added by predictors
                for (Predictor predictor: dataflow.getPredictors()) {
                    predictor.addCells(item, event);
                }
            }

            final DateFormat dataFormat = new SimpleDateFormat("yyyy-MMM-dd h:mm:ss a");

            private String formatTime(Date date) {
                return date == null ? "" : dataFormat.format(date);
            }
        };
        eventsListbox.setItemRenderer(renderer);
        Listheader header = (Listheader) eventsListbox.getListhead().getFirstChild();
        header.setSortAscending(new DataflowEventComparator(true));
        header.setSortDescending(new DataflowEventComparator(true));
        eventsListbox.setModel(eventsModel);

        /*
        latestEventsListbox.setRows(15);
        latestEventsListbox.setItemRenderer(renderer);
        latestEventsListbox.setModel(latestEventsModel);
        */

        this.dataflowListener = new DataflowListener() {
            final Desktop desktop = window.getDesktop();
            final EventListener eventListener = PredictiveMonitorController.this; 

            public void notify(DataflowEvent event) {
                Executions.schedule(desktop, eventListener, event);
            }
        };

        dataflow.listeners.add(dataflowListener);

        window.doModal();
    }

    public void close() {
        dataflow.listeners.remove(dataflowListener);
    }

    private void updateUI() {
        runningCasesLabel.setValue(Integer.toString(dataflow.caseCount - dataflow.completedCaseCount));
        completedEventsLabel.setValue(Integer.toString(dataflow.completedEventCount));
        completedCasesLabel.setValue(Integer.toString(dataflow.completedCaseCount));
        if (dataflow.completedCaseCount > 0) {
            averageCaseLengthLabel.setValue(numberFormat.format((dataflow.completedCaseEventCount / (double) dataflow.completedCaseCount)));
            averageCaseDurationLabel.setValue(DataflowEvent.format(dataflow.totalCompletedCaseDuration.dividedBy(dataflow.completedCaseCount)));
        }
    }

    // Implementation of EventListener<DataflowEvent>

    //final private Map<String, DataflowEvent> latestEventsMap = new HashMap<>();

    public void onEvent(DataflowEvent event) {
        eventsModel.add(0, event);

        /*
        String caseId = event.getCaseId();
        DataflowEvent previousEvent = latestEventsMap.get(caseId);
        if (previousEvent != null) {
            latestEventsModel.remove(previousEvent);
            latestEventsMap.remove(caseId);
        }
        if (!event.isLast()) {
            latestEventsModel.add(0, event);
            latestEventsMap.put(caseId, event);
        }
        */

        updateUI();
    }
}
