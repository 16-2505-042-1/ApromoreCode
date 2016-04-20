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

package org.apromore.manager.service;

import static org.powermock.api.easymock.PowerMock.createMock;

import org.apromore.manager.ManagerPortalEndpoint;
import org.apromore.service.*;
import org.apromore.service.helper.UIHelper;
import org.apromore.service.helper.UserInterfaceHelper;
import org.apromore.service.impl.*;
import org.junit.Before;

public abstract class AbstractEndpointUnitTest {

    protected ManagerPortalEndpoint endpoint;

    protected DeploymentService deploymentService;
    protected CanoniserService canoniserService;
    protected PluginService pluginService;
    protected FragmentService fragmentSrv;
    protected ProcessService procSrv;
    protected ClusterService clusterService;
    protected FormatService frmSrv;
    protected DomainService domSrv;
    protected UserService userSrv;
    protected SecurityService secSrv;
    protected WorkspaceService wrkSrv;
    protected UserInterfaceHelper uiHelper;
    protected PQLService pqlSrv;
    protected DatabaseService dbSrv;
    protected BPMNMinerService bpmnMinerSrv;
    protected ProDriftDetectionService proDriftSrv;
    protected StructuringService structuringServ;
    protected MeasurementsService measurementsServ;

    @Before
    public void setUp() throws Exception {
        deploymentService = createMock(DeploymentServiceImpl.class);
        pluginService = createMock(PluginServiceImpl.class);
        fragmentSrv = createMock(FragmentServiceImpl.class);
        canoniserService = createMock(CanoniserServiceImpl.class);
        procSrv = createMock(ProcessServiceImpl.class);
        clusterService = createMock(ClusterServiceImpl.class);
        frmSrv = createMock(FormatServiceImpl.class);
        domSrv = createMock(DomainServiceImpl.class);
        userSrv = createMock(UserServiceImpl.class);
        secSrv = createMock(SecurityServiceImpl.class);
        wrkSrv = createMock(WorkspaceServiceImpl.class);
        uiHelper = createMock(UIHelper.class);
        pqlSrv = createMock(PQLServiceImpl.class);
        dbSrv = createMock(DatabaseServiceImpl.class);
        bpmnMinerSrv = createMock(BPMNMinerServiceImpl.class);
        proDriftSrv = createMock(ProDriftDetectionServiceImpl.class);
        structuringServ = createMock(StructuringServiceImpl.class);
        measurementsServ = createMock(MeasurementsServiceImpl.class);

        endpoint = new ManagerPortalEndpoint(deploymentService, pluginService, fragmentSrv, canoniserService, procSrv,
                clusterService, frmSrv, domSrv, userSrv, secSrv, wrkSrv, uiHelper,
                pqlSrv, dbSrv, bpmnMinerSrv, proDriftSrv, structuringServ, measurementsServ);
    }
}
