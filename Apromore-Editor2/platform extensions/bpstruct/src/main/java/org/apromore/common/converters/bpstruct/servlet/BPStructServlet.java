/*
 * Copyright © 2009-2014 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package org.apromore.common.converters.bpstruct.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
import java.io.StringReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;

import de.hpi.bpmn2_0.factory.AbstractBpmnFactory;
import de.hpi.bpmn2_0.model.Definitions;
import de.hpi.bpmn2_0.transformation.BPMN2DiagramConverter;
import de.hpi.bpmn2_0.transformation.Diagram2BpmnConverter;
import de.hpi.bpmn2_0.model.extension.synergia.Configurable;
import de.hpi.bpmn2_0.model.extension.synergia.ConfigurationAnnotationAssociation;
import de.hpi.bpmn2_0.model.extension.synergia.ConfigurationAnnotationShape;
import de.hpi.bpmn2_0.model.extension.synergia.Variants;
//import de.hpi.bpt.process.Process;
//import de.hpi.bpt.process.serialize.SerializationException;
import org.apache.log4j.Logger;
//import org.apromore.common.converters.bpstruct.BasicDiagramToProcessModel;
//import org.apromore.common.converters.bpstruct.JSON2Process;
//import org.apromore.common.converters.bpstruct.Process2JSON;
//import org.apromore.common.converters.bpstruct.ProcessModelToBasicDiagram;
import org.apromore.manager.client.ManagerService;
import org.json.JSONException;
import org.json.JSONObject;
import org.oryxeditor.server.diagram.basic.BasicDiagram;
import org.oryxeditor.server.diagram.basic.BasicDiagramBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet to access the BPStruct service.
 */
public class BPStructServlet extends HttpServlet {

    private static final long serialVersionUID = 4651531154294830523L;

    private static final Logger LOGGER = Logger.getLogger(BPStructServlet.class);
    //private static final String BPSTRUCT_SERVER = "http://apromore.qut.edu.au:8080/bpstruct/rest/v1/structure/max";

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        doPost(req, res);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        String jsonData = req.getParameter("data");
        String diagramType = req.getParameter("type");

        /* Transform and return */
        try {
            if (jsonData == null || jsonData.isEmpty()) {
                res.setStatus(500);
                res.setContentType("text/plain; charset=UTF-8");
                res.getWriter().write("Empty or missing parameter 'data'!");
            } else {
                String diagramJson = convert(jsonData);
                res.setContentType("application/xml; charset=UTF-8");
                res.setStatus(200);
                res.getWriter().write(diagramJson);
            }
        } catch (Exception e) {
            try {
                LOGGER.fatal(e.toString());
                res.setStatus(500);
                res.setContentType("text/plain; charset=UTF-8");
                if (e.getCause() != null) {
                    res.getWriter().write(e.getCause().getMessage());
                } else {
                    res.getWriter().write(e.getMessage());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Process the model in the editor using the BPStruct service.
     *
     * @param jsonData  a JSON-formatted string representing a BPMN diagram from the Signavio editor
     * @return the same diagram after having been processed by BPStruct, or <code>null</code> if <var>jsonData</var> can't be parsed as a Signavio {@link BasicDiagram}
     */
    private String convert(String jsonData) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Definitions.class,
                                                          Configurable.class,
                                                          ConfigurationAnnotationAssociation.class,
                                                          ConfigurationAnnotationShape.class,
                                                          Variants.class);

        // JSON-formatted String -> Signavio Diagram
        BasicDiagram diagram = BasicDiagramBuilder.parseJson(jsonData);
        if (diagram == null) {
            return null;
        }
        assert diagram != null;

        // Signavio Diagram -> BPMN DOM
        Diagram2BpmnConverter bpmnConverter = new Diagram2BpmnConverter(diagram, AbstractBpmnFactory.getFactoryClasses());
        Definitions bpmn = bpmnConverter.getDefinitionsFromDiagram();

        // BPMN DOM -> BPMN-formatted String
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxbContext.createMarshaller().marshal(bpmn, baos);
        String bpmnString = baos.toString("utf-8");

        // Ask the manager to restructure the BPMN-formatted String
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletConfig().getServletContext());
        ManagerService manager = (ManagerService) applicationContext.getAutowireCapableBeanFactory().getBean("managerClient");
        log("Users " + manager.readAllUsers());  // Confirm that we really can talk to the manager
        String bpstructedBpmnString = bpmnString;  //"manager.f(bpmnString)";  // Call to Adriano's code will go here

        // BPMN-formatted String -> BPMN DOM
        StreamSource source = new StreamSource(new StringReader(bpstructedBpmnString));
        Definitions definitions = jaxbContext.createUnmarshaller().unmarshal(source, Definitions.class).getValue();

        // BPMN DOM -> Signavio Diagram
        BPMN2DiagramConverter jsonConverter = new BPMN2DiagramConverter("/signaviocore/editor/");
        List<BasicDiagram> diagrams = jsonConverter.getDiagramFromBpmn20(bpmn);
        assert diagrams != null;
        if (diagrams.size() < 1) {
            throw new Exception("Signavio JSON diagram could not be obtained from restructured BPMN model: " + bpstructedBpmnString);
        }
        else if (diagrams.size() > 1) {
            log("Multiple diagrams obtained from restructured BPMN model; arbitrarily choosing the first to reload into the editor");
        }
        BasicDiagram newDiagram = diagrams.get(0);
        newDiagram.setBounds(diagram.getBounds());

        // Signavio Diagram -> JSON-formatted String
        JSONObject json = new JSONObject();
        json.put("data_json", newDiagram.getJSON());
        json.put("hasChanged", true);
        //json.put("errors", ...);
        return json.toString();
    }

    /*
    private String convert(String jsonData) throws Exception {
        BasicDiagram newDiagram ;
        BasicDiagram diagram = BasicDiagramBuilder.parseJson(jsonData);

        if (diagram != null) {
            Process process = convertToProcessModel(diagram);
            String json = getJsonFromProcess(process);

            String structuredJson = processWithBPStruct(json);
            Process newProcess = getProcessFromJson(structuredJson);

            newDiagram = convertToBasicDiagram(newProcess);
            newDiagram.setBounds(diagram.getBounds());

            return updateDiagramWithBPStructExtras(newDiagram, structuredJson);
        }

        return null;
    }
    */

    /** Update the json to send back with the BPStruct Extras. */
    /*
    private String updateDiagramWithBPStructExtras(BasicDiagram newDiagram, String structuredJson) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject bpJson = new JSONObject(structuredJson);

        json.put("data_json", newDiagram.getJSON());
        if (bpJson.has("hasChanged")) {
            json.put("hasChanged", bpJson.get("hasChanged"));
        }
        if (bpJson.has("errors")) {
            json.put("errors", bpJson.get("errors"));
        }

        return json.toString();
    }
    */

    /* Convert a basicDiagram into the Format required for BPStruct. */
    /*
    private de.hpi.bpt.process.Process convertToProcessModel(BasicDiagram diagram) throws SerializationException {
        BasicDiagramToProcessModel bd2pm = new BasicDiagramToProcessModel();
        return bd2pm.convert(diagram);
    }
    */

    /* Convert a basicDiagram into the Format required for BPStruct. */
    /*
    private BasicDiagram convertToBasicDiagram(Process process) throws SerializationException {
        ProcessModelToBasicDiagram pm2bd = new ProcessModelToBasicDiagram();
        return pm2bd.convert(process);
    }
    */

    /* Converts the Process to JSon. */
    /*
    private String getJsonFromProcess(Process process) {
        String json = null;
        try {
            json = Process2JSON.convert(process);
        } catch (SerializationException e) {
            LOGGER.error(e.getMessage());
        }
        return json;
    }
    */

    /* Converts the JSon to Process. */
    /*
    private Process getProcessFromJson(String json) {
        Process process = null;
        try {
            process = JSON2Process.convert(json);
        } catch (SerializationException e) {
            LOGGER.error(e.getMessage());
        }
        return process;
    }
    */

    /* Send the converted document to BPStruct and see if we have any changes. */
    /*
    private String processWithBPStruct(String json) throws Exception {
        if (json != null && !json.equals("")) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BPSTRUCT_SERVER);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");

                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(json);
                wr.flush();
                wr.close();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return null;
    }
    */
}
