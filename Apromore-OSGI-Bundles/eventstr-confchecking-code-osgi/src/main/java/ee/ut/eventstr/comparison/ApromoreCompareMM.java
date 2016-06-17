package ee.ut.eventstr.comparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;

import ee.ut.bpmn.BPMNProcess;
import ee.ut.bpmn.utils.BPMN2Reader;
import ee.ut.bpmn.utils.Petrifier;
import ee.ut.eventstr.PESSemantics;
import ee.ut.nets.unfolding.Unfolder_PetriNet;
import ee.ut.nets.unfolding.Unfolding2PES;
import ee.ut.nets.unfolding.BPstructBP.MODE;
import hub.top.petrinet.PetriNet;
import hub.top.petrinet.Transition;

public class ApromoreCompareMM{

	public static void main(String[] args) throws Exception{
		BPMNProcess<Element> model1 = BPMN2Reader.parse(new File("bpm2014/Model1.bpmn"));
		Petrifier<Element> petrifier1 = new Petrifier<Element>(model1);
		PetriNet net1 = petrifier1.petrify(model1.getSources().iterator().next(), model1.getSinks().iterator().next());
		HashSet<String> labels1 = new HashSet<>();//model1.getLabels().values());
		
		for(Integer i : model1.getVisibleNodes())
			labels1.add(model1.getLabels().get(i));
		
		BPMNProcess<Element> model2 = BPMN2Reader.parse(new File("bpm2014/Model2.bpmn"));
		Petrifier<Element> petrifier2 = new Petrifier<Element>(model2);
		PetriNet net2 = petrifier2.petrify(model2.getSources().iterator().next(), model2.getSinks().iterator().next());
		
		HashSet<String> labels2 = new HashSet<>();//model1.getLabels().values());
		for(Integer i : model2.getVisibleNodes())
			labels2.add(model2.getLabels().get(i));
		
		ApromoreCompareMM comp = new ApromoreCompareMM();
		
//		System.out.println(comp.getDifferences(net1, net2, labels1, labels2));
	}
	
	public Set<String> getDifferences(PetriNet net1, PetriNet net2, HashSet<String> obs1, HashSet<String> obs2) throws Exception {
		DiffMMVerbalizer<Integer> verbalizer = analyzeDifferences(net1, net2, obs1, obs2);
		verbalizer.verbalize();
		
		Set<String> statements = verbalizer.getStatements();
		
		if(statements.isEmpty())
			statements.add("No difference was found between the models");
		
		return statements;
	}

	private DiffMMVerbalizer<Integer> analyzeDifferences(PetriNet net1, PetriNet net2, HashSet<String> obs1, HashSet<String> obs2) throws Exception {		
		Set<String> common = new HashSet<>(obs1);
		common.retainAll(obs2);
//		System.out.println("\t\t "+ common);
//		silent.add("_0_");
//		silent.add("_1_");
		
		PESSemantics<Integer> pnmlpes1 = getUnfoldingPES(net1, common);
		PESSemantics<Integer> pnmlpes2 = getUnfoldingPES(net2, common);
		
		PartialSynchronizedProduct<Integer> psp = new PartialSynchronizedProduct<>(pnmlpes1, pnmlpes2);		
		PartialSynchronizedProduct<Integer> pre = psp.perform();
		
		HashSet<String> commonLabels = new HashSet<>(pnmlpes1.getLabels());
		commonLabels.retainAll(pnmlpes2.getLabels());
		commonLabels.remove("_0_");
		commonLabels.remove("_1_");
		
//		HashSet<String> obsLabel1 = new HashSet<>(pnmlpes1.getLabels());
//		obsLabel1 = filterSilent(obsLabel1, silent1);
//		
//		HashSet<String> obsLabel2 = new HashSet<>(pnmlpes2.getLabels());
//		obsLabel2 = filterSilent(obsLabel2, silent2);
		
		DiffMMVerbalizer<Integer> verbalizer = new DiffMMVerbalizer<Integer>(pnmlpes1, pnmlpes2, commonLabels, obs1, obs2);
		psp.setVerbalizer(verbalizer);
		pre.prune();
		
		write(psp.toDot(), "psp.dot");
		
		return verbalizer;
	}
	
//	private HashSet<String> filterSilent(HashSet<String> obsLabel, HashSet<String> obs) {
//		HashSet<String> obsFiltered = new HashSet<>();
//		
//		for (String t: obsLabel){
//			boolean found = false;
//			
//			for(String st : silent)
//				if(t.startsWith(st))
//					found = true;
//			
//			if(!found)
//				obsFiltered.add(t);
//		}
//		
//		return obsFiltered;
//	}

	private void write(String toWrite, String fileName) {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
		    writer.write(toWrite);
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}

	private PESSemantics<Integer> getUnfoldingPES(PetriNet net, Set<String> obs) throws Exception {
//		System.out.println("\t" + obs + " -- silent labels -- ");
		Set<String> labels = new HashSet<>();
		for (Transition t : net.getTransitions()){
//			boolean found = false;
//			
//			for(String st : silent)
//				if(t.getName().startsWith(st))
//					found = true;
//			
//			if(!found)
			if(obs.contains(t.getName()))
				labels.add(t.getName());
		}
		
//		System.out.println("\t" + labels + " -- labels -- ");
		
		Unfolder_PetriNet unfolder = new Unfolder_PetriNet(net, MODE.EQUAL_PREDS);
		unfolder.computeUnfolding();
		
		Unfolding2PES pes = new Unfolding2PES(unfolder, labels);
//		System.out.println(pes.getCyclicTasks());
		return new PESSemantics<Integer>(pes.getPES());
		
//		pes.getPES();
//		NewUnfoldingPESSemantics<Integer> pessem = new NewUnfoldingPESSemantics<Integer>(pes.getPES(), pes);
//		return pessem;
	}
}
