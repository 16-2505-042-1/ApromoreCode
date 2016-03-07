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

package org.apromore.portal.dialogController;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Set;

import org.apromore.model.UserType;
import org.apromore.portal.common.UserSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SizeEvent;
import org.zkoss.zul.Window;

import org.zkoss.zul.Tree;

import org.apromore.portal.exception.DialogException;
import org.zkoss.zk.ui.SuspendNotAllowedException;

import org.zkoss.zul.Applet;

public class PQLFilterController extends BaseController {
    private MainController mainController;

    private Window queryWindow;

    private Tree processFolderTree;

    private String selText;
    private Applet applet;

    private static final Logger LOGGER = LoggerFactory.getLogger(PQLFilterController.class.getName());

    public PQLFilterController(final MainController mainController) throws SuspendNotAllowedException, InterruptedException, DialogException{

        this.mainController=mainController;

        this.queryWindow = (Window) Executions.createComponents(
                "macros/filter/pqlFilter.zul", null, null);

        this.queryWindow.setTitle("PQL Query");

        applet=(Applet)queryWindow.getFirstChild().getFirstChild();

        UserType user=UserSessionManager.getCurrentUser();
        applet.setParam("manager_endpoint", "http://" + config.getSiteExternalHost() + ":" + config.getSiteExternalPort() + "/" + config.getSiteManager() + "/services/manager");
        applet.setParam("portal_endpoint", "http://" + config.getSiteExternalHost() + ":" + config.getSiteExternalPort() + "/" + config.getSitePortal() + "/services/portal");
        applet.setParam("filestore_url", "http://" + config.getSiteExternalHost() + ":" + config.getSiteExternalPort() + "/" + config.getSiteFilestore());
        applet.setParam("user",user.getUsername());
        applet.setParam("idSession",user.getId());

        queryWindow.addEventListener(Events.ON_CLOSE,new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                if(mainController!=null) {
                    mainController.loadWorkspace();
                    applet=null;
                }
            }
        });

        queryWindow.addEventListener(Events.ON_SIZE, new EventListener<SizeEvent>() {
            @Override
            public void onEvent(SizeEvent sizeEvent) throws Exception {
                applet.setWidth(sizeEvent.getWidth());
                applet.setHeight(sizeEvent.getHeight());
            }
        });

        queryWindow.doModal();
    }

    protected void cancel() {
        this.queryWindow.detach();
        applet.detach();
    }

}
