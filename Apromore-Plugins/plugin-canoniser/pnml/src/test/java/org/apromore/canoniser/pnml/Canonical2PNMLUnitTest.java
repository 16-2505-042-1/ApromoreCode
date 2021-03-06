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

package org.apromore.canoniser.pnml;

import org.apromore.anf.ANFSchema;
import org.apromore.anf.AnnotationsType;
import org.apromore.canoniser.pnml.internal.Canonical2PNML;
import org.apromore.canoniser.pnml.internal.pnml2canonical.NamespaceFilter;
import org.apromore.cpf.CPFSchema;
import org.apromore.cpf.CanonicalProcessType;
import org.apromore.pnml.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import java.io.*;
import java.util.StringTokenizer;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;

public class Canonical2PNMLUnitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Canonical2PNMLUnitTest.class.getName());

    /**
     * Decanonize <code>Basic.cpf</code>.
     */
    @Test
    public void testBasic() throws Exception {

        // Convert the CPF Task to a single PNML Transition
        PnmlType pnml = decanonise("Basic.cpf", "Basic.anf", "Basic-small.pnml", true, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(2, net.getArc().size());
        assertEquals(2, net.getPlace().size());
        assertEquals(1, net.getTransition().size());

        // Convert the CPF Task to a PNML Place bounded by Transitions
        pnml = decanonise("Basic.cpf", "Basic.anf", "Basic-big.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        net = pnml.getNet().get(0);
        assertEquals(4, net.getArc().size());
        assertEquals(3, net.getPlace().size());
        assertEquals(2, net.getTransition().size());
    }

    /**
     * Decanonize <code>ANDJoin.cpf</code>.
     */
    @Test
    public void testANDJoin() throws Exception {

        PnmlType pnml = decanonise("ANDJoin.cpf", null, "ANDJoin.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(3, net.getArc().size());
    }

    /**
     * Decanonize <code>ANDSplit.cpf</code>.
     */
    @Test
    public void testANDSplit() throws Exception {

        PnmlType pnml = decanonise("ANDSplit.cpf", null, "ANDSplit.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(3, net.getArc().size());
    }

    /**
     * Decanonize <code>ORJoin.cpf</code>.
     *
     * OR is interpreted as XOR
     *
     * test succeeds
     */
    @Ignore
    @Test
    public void testORJoin() throws Exception {

        PnmlType pnml = decanonise("ORJoin.cpf", null, "ORJoin.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(4, net.getArc().size());
        assertEquals(3, net.getPlace().size());
        assertEquals(2, net.getTransition().size());
    }

    /**
     * Decanonize <code>ORSplit.cpf</code>.
     *
     * OR is interpreted as XOR
     *
     * test succeeds
     */
    @Ignore
    @Test
    public void testORSplit() throws Exception {

        PnmlType pnml = decanonise("ORSplit.cpf", null, "ORSplit.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(4, net.getArc().size());
        assertEquals(3, net.getPlace().size());
        assertEquals(2, net.getTransition().size());
    }

    /**
     * Decanonize <code>XORJoin.cpf</code>.
     */
    @Test
    public void testXORJoin() throws Exception {

        PnmlType pnml = decanonise("XORJoin.cpf", null, "XORJoin.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(6, net.getArc().size());
        assertEquals(4, net.getPlace().size());
        assertEquals(3, net.getTransition().size());
    }

    /**
     * Decanonize <code>XORSplit.cpf</code>.
     */
    @Test
    public void testXORSplit() throws Exception {

        PnmlType pnml = decanonise("XORSplit.cpf", null, "XORSplit.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(6, net.getArc().size());
        assertEquals(4, net.getPlace().size());
        assertEquals(3, net.getTransition().size());
    }

    /**
     * Decanonize <code>Case 1.cpf</code>.
     */
    @Test
    public void testCase1() throws Exception {

        PnmlType pnml = decanonise("Case 1.cpf", null, "Case 1-small.pnml", true, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(3, net.getArc().size());  // 3 edges
        assertEquals(3, net.getPlace().size());  // 1 start, 2 end
        assertEquals(1, net.getTransition().size());  // 1 task

        pnml = decanonise("Case 1.cpf", null, "Case 1-big.pnml", false, false);

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        net = pnml.getNet().get(0);
        assertEquals(5, net.getArc().size());  // 4 edges, 1 synthetic
        assertEquals(4, net.getPlace().size());  // 1 start, 1 task, 2 end
        assertEquals(2, net.getTransition().size());  // 2 task
    }

    /**
     * Decanonize <code>Case 12.cpf</code>.
     *
     * See that CPF cancellation sets get converted into PNML reset arcs.
     * 
     * not supported yet
     *
     * test succeeds
     */
    @Test
    @Ignore
    public void testCase12() throws Exception {
        PnmlType pnml = decanonise("Case 12.cpf",   // input file
                                   null,            // no ANF file
                                   "Case 12.pnml",  // output file
                                   false,           // tasks have a (resettable) place while they execute
                                   false);          // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(12, net.getArc().size());  // 6 edges, 5 synthetic, 3 resets
        assertEquals(5, net.getPlace().size());  // 1 start, 1 task, 1 end, 1 timer event, 1 synthetic
        assertEquals(4, net.getTransition().size());  // 2 task, 1 timer event,1 AND-split
    }

    /**
     * Decanonize <code>TripleCancellation.cpf</code>.
     *
     * CPF generated from a BPMN task with two boundary events.
     * Three CPF nodes (task, timer & message) cancel each of the others.
     * 
     * not supported yet
     *
     * test succeeds
     */
    @Test
    @Ignore
    public void testTripleCancellation() throws Exception {
        PnmlType pnml = decanonise("TripleCancellation.cpf",   // input file
                                   null,                       // no ANF file
                                   "TripleCancellation.pnml",  // output file
                                   false,                      // tasks have a (resettable) place while they execute
                                   false);                     // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(20, net.getArc().size());  // 12 edges, 8 resets
        assertEquals(6, net.getPlace().size());  // 1 start, 1 task, 1 end, 1 timer event, 1 message event, 1 synthetic
        assertEquals(5, net.getTransition().size());  // 2 task, 1 timer event, 1 message event, 1 AND-split
    }
    
    /**
     * Decanonize <code>Standard.cpf</code>.
     *
     * this verifys that the graphics of the elements get adjusted correct
     */
    @Test
    public void testGraphicalAdjustments() throws Exception {
        PnmlType pnml = decanonise("Standard.cpf",   // input file
                                   null,                       // no ANF file
                                   "Standard.pnml",  // output file
                                   true,                      // tasks have a (resettable) place while they execute
                                   false);                     // don't generate addition places for the CPF edges

        // Inspect the result
        NetType net = pnml.getNet().get(0);
        BigDecimal YPlace = net.getPlace().get(0).getGraphics().getPosition().getY();
        BigDecimal YPlace2 = net.getPlace().get(1).getGraphics().getPosition().getY();
        
        assertEquals(YPlace.intValue(), YPlace2.intValue());
       }
    
    /**
     * Decanonize <code>Resource.cpf</code>.
     *
     * this verifys that the resource for example the bpmn pools/lanes get allocated correct
     *
     * Code for Toolspecifics is not executed yet. Toolspecifics are null.
     *
     */
    @Ignore
    @Test
    public void testResourceAllocation() throws Exception {
        PnmlType pnml = decanonise("Resource.cpf",   // input file
                                   null,                       // no ANF file
                                   "Resource.pnml",  // output file
                                   true,                      // tasks have a (resettable) place while they execute
                                   false);                     // don't generate addition places for the CPF edges

        // Inspect the result
        NetType net = pnml.getNet().get(0);
        TransitionType transition = null;
        for(int i=0; i< net.getTransition().size();i++){
        	if(net.getTransition().get(i).getName().getText().equals("T1")){
        		transition = net.getTransition().get(i);
        	}
        }
        assertEquals("T1", transition.getName().getText());
        assertEquals(transition.getToolspecific().get(0).getTransitionResource().getRoleName(),"L1");  //Toolspecific is currently null

       }
    
    /**
     * Decanonize <code>Multiple Tasks.cpf</code>.
     *
     * this verifys that Multiple tasks get a place between them
     * 
     */
    @Test
    public void testMultipleTasks() throws Exception {
        PnmlType pnml = decanonise("Multiple Tasks.cpf",   // input file
                                   null,            // no ANF file
                                   "Multiple Tasks.pnml",  // output file
                                   true,           // tasks have a (resettable) place while they execute
                                   false);          // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(4, net.getArc().size());
        assertEquals(3, net.getPlace().size());
        assertEquals(2, net.getTransition().size());
    }
    
    /**
     * Decanonize <code>Nested operators.cpf</code>.
     *
     * this verifys that nested operators get translated correct
     * 
     */
    @Test
    @Ignore
    public void testNestedOperators() throws Exception {
        PnmlType pnml = decanonise("Nested operators.cpf",   // input file
                                   null,            // no ANF file
                                   "Nested operators.pnml",  // output file
                                   true,           // tasks have a (resettable) place while they execute
                                   false);          // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(14, net.getArc().size());
        assertEquals(7, net.getPlace().size());
        assertEquals(6, net.getTransition().size()); //4 Tasks, 2 AND & 2 XOR
    }
    
    /**
     * Decanonize <code>Missing Endevent.cpf</code>.
     *
     * this verifys that in case of an missing endevent one will be added
     * 
     * function not yet implemented 
     * 
     */
    @Test
    @Ignore
    public void testMissingEndevent() throws Exception {
        PnmlType pnml = decanonise("Missing Endevent.cpf",   // input file
                                   null,            // no ANF file
                                   "Missing Endevent.pnml",  // output file
                                   true,           // tasks have a (resettable) place while they execute
                                   false);          // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(2, net.getArc().size());
        assertEquals(2, net.getPlace().size());
        assertEquals(1, net.getTransition().size()); 
    }
    
    /**
     * Decanonize <code>Missing Startevent.cpf</code>.
     *
     * this verifys that in case of an missing StartEvent one will be added
     * 
     * function not yet implemented
     * 
     */
    @Test
    @Ignore
    public void testMissingStartEvent() throws Exception {
        PnmlType pnml = decanonise("Missing Startevent.cpf",   // input file
                                   null,            // no ANF file
                                   "Missing Startevent.pnml",  // output file
                                   true,           // tasks have a (resettable) place while they execute
                                   false);          // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(2, net.getArc().size());
        assertEquals(2, net.getPlace().size());
        assertEquals(1, net.getTransition().size()); 
    }
    
    /**
     * Decanonize <code>Subprocess.cpf</code>.
     *
     * this verifys that subprocesses get translated correct
     * 
     * function not yet implemented
     * 
     */
    @Ignore
    @Test
    public void testSubprocess() throws Exception {
        PnmlType pnml = decanonise("Subprocess.cpf",   // input file
                                   null,            // no ANF file
                                   "Subprocess.pnml",  // output file
                                   true,           // tasks have a (resettable) place while they execute
                                   false);          // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(1, net.getTransition().size());
        assertEquals(1, net.getTransition().get(0).getToolspecific().size());
        assertTrue(net.getTransition().get(0).getToolspecific().get(0).isSubprocess());
        
    }
    
    /**
     * Decanonize <code>Subprocess expanded.cpf</code>.
     *
     * this verifys that expanded subprocesses get translated correct
     * 
     * function not yet implemented
     * 
     */
    @Ignore
    @Test
    public void testSubprocessExpanded() throws Exception {
        PnmlType pnml = decanonise("Subprocess expanded.cpf",   // input file
                                   null,            // no ANF file
                                   "Subprocess expanded.pnml",  // output file
                                   true,           // tasks have a (resettable) place while they execute
                                   false);          // don't generate addition places for the CPF edges

        // Inspect the result
        assertEquals(1, pnml.getNet().size());
        NetType net = pnml.getNet().get(0);
        assertEquals(2, net.getTransition().size());
        TransitionType subProcess = null;
        for(int i = 0; i<net.getTransition().size();i++){
        	if(net.getTransition().get(i).getName().getText().equals("SP")){
        		subProcess = net.getTransition().get(i);
        	}
        }
        assertEquals(1, net.getTransition().get(0).getToolspecific().size());
        assertTrue(subProcess.getToolspecific().get(0).isSubprocess());
        
    }


    // Internal methods

    private PnmlType decanonise(final String  cpfFileName,
                                final String  anfFileName,
                                final String  pnmlFileName,
                                final boolean isCpfTaskPnmlTransition,
                                final boolean isCpfEdgePnmlPlace)
        throws FileNotFoundException, JAXBException, SAXException {

        CanonicalProcessType cpf = CPFSchema.unmarshalCanonicalFormat(
            new FileInputStream(new File("src/test/resources/CPF_testcases/" + cpfFileName)),
            true  // validate?
        ).getValue();

        AnnotationsType anf = null;
        if (anfFileName != null) {
            anf = ANFSchema.unmarshalAnnotationFormat(
                new FileInputStream(new File("src/test/resources/CPF_testcases/" + anfFileName)),
                true  // validate?
            ).getValue();
        }

        PnmlType pnml = (new Canonical2PNML(cpf, anf, isCpfTaskPnmlTransition, isCpfEdgePnmlPlace)).getPNML();

        // Serialize the decanonized PNML for inspection
        PNMLSchema.marshalPNMLFormat(
            new FileOutputStream(new File("target/" + pnmlFileName)),
            pnml,
            false  // validate?
        );

        // Check that this is a valid Petri Net
        validate(pnml);

        return pnml;
    }

    /**
     * Perform structural validation of an alleged Petri Net beyond what the PNML schema can enforce.
     *
     * This verifies that every arc connects a place and a transition.
     *
     * @param pnml a PNML model
     * @throws AssertionError if <var>pnml</var> isn't valid
     */
    private static void validate(final PnmlType pnml) {
        for (NetType net: pnml.getNet()) {
            for (ArcType arc: net.getArc()) {
                assert arc.getSource() instanceof PlaceType && arc.getTarget() instanceof TransitionType ||
                       arc.getSource() instanceof TransitionType && arc.getTarget() instanceof PlaceType:
                    "Arc " + arc.getId() + " does not connect a place to a transition";
            }
        }
    }


    // Older tests -- these have been salvaged to run, but don't actually test anything other than parsing

    @Test
    public void testWoped() {

        String cpf_file_without_path = null;
        String anf_file_without_path = null;

        File anf_file = null;
        File cpf_file = null;
        File foldersave = new File("target");
        File output = null;
        File folder = new File("src/test/resources/PNML_testcases/woped_cases_expected_cpf");
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        File[] folderContent = folder.listFiles(fileFilter);
        int n = 0;

        for (File file : folderContent) {
            String filename = file.getName();
            StringTokenizer tokenizer = new StringTokenizer(filename, ".");
            String filename_without_path = tokenizer.nextToken();

            String extension = filename.split("\\.")[filename.split("\\.").length - 1];

            output = new File(foldersave + "/" + filename_without_path + ".pnml");

            if (!filename.contains("subnet")) {
                if (extension.compareTo("cpf") == 0 && extension.compareTo("anf") == 0) {
                    //LOGGER.debug("Skipping " + filename);
                }

                if (extension.compareTo("anf") == 0) {
                    //LOGGER.debug("Analysing " + filename);
                    n++;
                    anf_file = new File(folder + "/" + filename);
                    anf_file_without_path = filename_without_path;
                }

                if (extension.compareTo("cpf") == 0) {
                    //LOGGER.debug("Analysing " + filename);
                    n++;
                    cpf_file = new File(folder + "/" + filename);
                    cpf_file_without_path = filename_without_path;

                }
            }

            if (anf_file != null && cpf_file != null && anf_file_without_path != null && cpf_file_without_path != null && anf_file_without_path.equals(cpf_file_without_path) && !filename.contains("subnet")) {

                try {
                    JAXBContext jc = JAXBContext.newInstance("org.apromore.cpf");
                    Unmarshaller u = jc.createUnmarshaller();
                    JAXBElement<CanonicalProcessType> rootElement = (JAXBElement<CanonicalProcessType>) u.unmarshal(cpf_file);
                    CanonicalProcessType cpf = rootElement.getValue();

                    jc = JAXBContext.newInstance("org.apromore.anf");
                    u = jc.createUnmarshaller();
                    JAXBElement<AnnotationsType> anfRootElement = (JAXBElement<AnnotationsType>) u.unmarshal(anf_file);
                    AnnotationsType anf = anfRootElement.getValue();

                    // Canonical2EPML canonical2epml_1 = new
                    // Canonical2EPML(cpf,true);

                    jc = JAXBContext.newInstance("org.apromore.pnml");

                    Canonical2PNML canonical2pnml = new Canonical2PNML(cpf, anf, filename_without_path);

                    Marshaller m1 = jc.createMarshaller();

                    NamespaceFilter outFilter = new NamespaceFilter(null, false);

                    OutputFormat format = new OutputFormat();
                    format.setIndent(true);
                    format.setNewlines(true);
                    format.setXHTML(true);
                    format.setExpandEmptyElements(true);
                    format.setNewLineAfterDeclaration(false);

                    XMLWriter writer = null;
                    try {
                        writer = new XMLWriter(new FileOutputStream(output), format);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    // Attach the writer to the filter
                    outFilter.setContentHandler(writer);

                    // Tell JAXB to marshall to the filter which in turn will
                    // call the writer
                    m1.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    JAXBElement<PnmlType> cprocRootElem1 = new ObjectFactory().createPnml(canonical2pnml.getPNML());
                    m1.marshal(cprocRootElem1, outFilter);

                } catch (JAXBException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        LOGGER.debug("Analysed " + n + " files.");
    }

}
