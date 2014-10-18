package edu.cs.princeton.cs.exprfl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.expr.SwitchTime;
import edu.princeton.cs.hsa.PlumbingGraph;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class VirtualTopoExprFl {
	
	public void myPrint(List<OFFlowMod> fms) {
		for (OFFlowMod fm : fms) {
			System.out.println(fm);
		}
	}
	
	public void startExpr(List<OFFlowMod> ipRouterRulesOriginal, List<OFFlowMod> gatewayRulesOriginal,
			List<OFFlowMod> macLearnerRulesOriginal) {

		/*SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
		int round = 10;
		
		{
			int macSize = (macLearnerRulesOriginal.size() - 800) / 2;
			Random rand = new Random(1);
			String fileName = String.format("experiments/PlotGraph/rres_gateway_strawman_%d", ipRouterRulesOriginal.size() - 1);
			Writer writer = null;
			try {
				writer = new FileWriter(fileName);
				for (int i = 0; i < round; i++) {
					
					List<OFFlowMod> ipRouterRules = new ArrayList<OFFlowMod>(ipRouterRulesOriginal);
					List<OFFlowMod> gatewayRules = new ArrayList<OFFlowMod>(gatewayRulesOriginal);
					List<OFFlowMod> macLearnerRules = new ArrayList<OFFlowMod>(macLearnerRulesOriginal);
					List<OFFlowMod> gatewayUpdateRules = new ArrayList<OFFlowMod>();
					List<OFFlowMod> macLearnerUpdateRules = new ArrayList<OFFlowMod>();
					
					int updateIndex = rand.nextInt(macSize);
					gatewayUpdateRules.add(gatewayRules.get(updateIndex));
					macLearnerUpdateRules.add(macLearnerRules.get(updateIndex * 2));
					macLearnerUpdateRules.add(macLearnerRules.get(updateIndex * 2 + 1));
					gatewayRules.remove(updateIndex);
					macLearnerRules.remove(updateIndex * 2);
					macLearnerRules.remove(updateIndex * 2);
					
					exprHelperMACStrawman(ipRouterRules, gatewayRules, macLearnerRules, gatewayUpdateRules, macLearnerUpdateRules,
							writer, switchTime);
				}
			} catch (IOException ex) {
			} finally {
				try {
					writer.close();
				} catch (Exception ex) {
				}
			}
		}

		{
			int macSize = (macLearnerRulesOriginal.size() - 800) / 2;
			Random rand = new Random(1);
			String fileName = String.format("experiments/PlotGraph/rres_gateway_inc_%d", ipRouterRulesOriginal.size() - 1);
			Writer writer = null;
			try {
				writer = new FileWriter(fileName);
				for (int i = 0; i < round; i++) {
					
					List<OFFlowMod> ipRouterRules = new ArrayList<OFFlowMod>(ipRouterRulesOriginal);
					List<OFFlowMod> gatewayRules = new ArrayList<OFFlowMod>(gatewayRulesOriginal);
					List<OFFlowMod> macLearnerRules = new ArrayList<OFFlowMod>(macLearnerRulesOriginal);
					List<OFFlowMod> gatewayUpdateRules = new ArrayList<OFFlowMod>();
					List<OFFlowMod> macLearnerUpdateRules = new ArrayList<OFFlowMod>();
					
					//System.out.println(macSize);
					//System.out.println(ipRouterRulesOriginal.size());
					//System.out.println(gatewayRulesOriginal.size());
					//System.out.println(macLearnerRulesOriginal.size());
					
					int updateIndex = rand.nextInt(macSize);
					gatewayUpdateRules.add(gatewayRules.get(updateIndex));
					macLearnerUpdateRules.add(macLearnerRules.get(updateIndex * 2));
					macLearnerUpdateRules.add(macLearnerRules.get(updateIndex * 2 + 1));
					gatewayRules.remove(updateIndex);
					macLearnerRules.remove(updateIndex * 2);
					macLearnerRules.remove(updateIndex * 2);
					
					//System.out.println("ip router rules");
					//myPrint(ipRouterRules);
					//System.out.println("gateway rules");
					//myPrint(gatewayRules);
					//System.out.println("mac learner rules");
					//myPrint(macLearnerRules);
					//System.out.println("gateway update rules");
					//myPrint(gatewayUpdateRules);
					//System.out.println("mac learner update rules");
					//myPrint(macLearnerUpdateRules);
					
					//Collections.shuffle(ipRouterRules, rand);
					//Collections.shuffle(gatewayRules, rand);
					//Collections.shuffle(macLearnerRules, rand);
					exprHelperMAC(ipRouterRules, gatewayRules, macLearnerRules, gatewayUpdateRules, macLearnerUpdateRules,
							writer, switchTime, false);
				}
			} catch (IOException ex) {
			} finally {
				try {
					writer.close();
				} catch (Exception ex) {
				}
			}
		}
		
		{
			int macSize = (macLearnerRulesOriginal.size() - 800) / 2;
			Random rand = new Random(1);
			String fileName = String.format("experiments/PlotGraph/rres_gateway_incacl_%d", ipRouterRulesOriginal.size() - 1);
			Writer writer = null;
			try {
				writer = new FileWriter(fileName);
				for (int i = 0; i < round; i++) {
					
					List<OFFlowMod> ipRouterRules = new ArrayList<OFFlowMod>(ipRouterRulesOriginal);
					List<OFFlowMod> gatewayRules = new ArrayList<OFFlowMod>(gatewayRulesOriginal);
					List<OFFlowMod> macLearnerRules = new ArrayList<OFFlowMod>(macLearnerRulesOriginal);
					List<OFFlowMod> gatewayUpdateRules = new ArrayList<OFFlowMod>();
					List<OFFlowMod> macLearnerUpdateRules = new ArrayList<OFFlowMod>();
					
					int updateIndex = rand.nextInt(macSize);
					gatewayUpdateRules.add(gatewayRules.get(updateIndex));
					macLearnerUpdateRules.add(macLearnerRules.get(updateIndex * 2));
					macLearnerUpdateRules.add(macLearnerRules.get(updateIndex * 2 + 1));
					gatewayRules.remove(updateIndex);
					macLearnerRules.remove(updateIndex * 2);
					macLearnerRules.remove(updateIndex * 2);

					exprHelperMAC(ipRouterRules, gatewayRules, macLearnerRules, gatewayUpdateRules, macLearnerUpdateRules,
							writer, switchTime, true);
				}
			} catch (IOException ex) {
			} finally {
				try {
					writer.close();
				} catch (Exception ex) {
				}
			}

		}*/
	}
	
	public void exprHelperMACStrawman(List<OFFlowMod> ipRouterRules, List<OFFlowMod> gatewayRules, List<OFFlowMod> macLearnerRules,
			List<OFFlowMod> gatewayUpdateRules, List<OFFlowMod> macLearnerUpdateRules,
    		Writer writer, SwitchTime switchTime) throws IOException {
    	
    	/*// build topology
    	PlumbingGraph graph = new PlumbingGraph();
    	graph.addNode((long) 1);
    	graph.addNode((long) 2);
    	graph.addNode((long) 3);
    	graph.addPort((long) 1, (short) 5, (short) 1);
    	graph.addPort((long) 1, (short) 6, (short) 2);
    	graph.addPort((long) 1, (short) 7, null);
    	graph.addPort((long) 2, (short) 8, null);
    	graph.addPort((long) 2, (short) 9, null);
    	graph.addPort((long) 3, (short) 10, null);
    	graph.addPort((long) 3, (short) 11, (short) 3);
    	graph.addPort((long) 3, (short) 12, (short) 4);
    	graph.addEdge((long) 1, (short) 7, (long) 2, (short) 8);
    	graph.addEdge((long) 2, (short) 9, (long) 3, (short) 10);
    	
    	long startTime = System.nanoTime();
    	// ip router
    	for (OFFlowMod fm : ipRouterRules) {
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 1));
    	}
    	
    	// gateway switch
    	for (OFFlowMod fm : gatewayRules){
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 2));
    	}
    	
    	// mac learner
    	for (OFFlowMod fm : macLearnerRules){
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 3));
    	}
    	
    	List<OFFlowMod> oldFlowMods = new ArrayList<OFFlowMod>(graph.flowTable.getFlowMods());
    	
    	int fmCount = 0;
    	for (OFFlowMod fm : gatewayUpdateRules) {
    		PolicyUpdateTable updateTable = graph.update(fm, graph.getNode((long) 2));
    		fmCount += updateTable.addFlowMods.size();
			fmCount += updateTable.deleteFlowMods.size();
    	}
    	for (OFFlowMod fm : macLearnerUpdateRules){
			PolicyUpdateTable updateTable = graph.update(fm, graph.getNode((long) 3));
			fmCount += updateTable.addFlowMods.size();
			fmCount += updateTable.deleteFlowMods.size();
    	}
    	long elapseTime = System.nanoTime() - startTime;
    	
    	List<OFFlowMod> newFlowMods = graph.flowTable.getFlowMods();
    	fmCount = this.calculateUpdateFlowModsStrawman(oldFlowMods, newFlowMods);
    	
    	double compileTime = elapseTime / 1e6;
    	double updateTime = 0;
    	for (int i = 0; i < fmCount; i++) {
    		updateTime += switchTime.getTime();
    	}
    	
    	writer.write(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	//System.out.println(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	*/
    }
	
	public void exprHelperMAC(List<OFFlowMod> ipRouterRules, List<OFFlowMod> gatewayRules, List<OFFlowMod> macLearnerRules,
			List<OFFlowMod> gatewayUpdateRules, List<OFFlowMod> macLearnerUpdateRules,
    		Writer writer, SwitchTime switchTime, boolean aclOpt) throws IOException {
    	/*
    	// build topology
    	PlumbingGraph graph = new PlumbingGraph();
    	if (aclOpt) {
    		{
    			List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
    			List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
    			storeTypes.add(PolicyFlowModStoreType.PREFIX);
    			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    			storeKeys.add(PolicyFlowModStoreKey.NETWORK_DST);
    			storeKeys.add(PolicyFlowModStoreKey.ALL);
    			graph.addNode((long) 1, storeTypes, storeKeys);
    		}
    		{
    			List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
    			List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
    			storeTypes.add(PolicyFlowModStoreType.PREFIX);
    			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    			storeKeys.add(PolicyFlowModStoreKey.NETWORK_DST);
    			storeKeys.add(PolicyFlowModStoreKey.ALL);
    			graph.addNode((long) 2, storeTypes, storeKeys);
    		}
    		{
    			List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
    			List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
    			storeTypes.add(PolicyFlowModStoreType.EXACT);
    			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    			storeKeys.add(PolicyFlowModStoreKey.DATA_DST);
    			storeKeys.add(PolicyFlowModStoreKey.ALL);
    			graph.addNode((long) 3, storeTypes, storeKeys);
    		}
    	} else {
    		graph.addNode((long) 1);
    		graph.addNode((long) 2);
    		graph.addNode((long) 3);
    	}
    	graph.addPort((long) 1, (short) 5, (short) 1);
    	graph.addPort((long) 1, (short) 6, (short) 2);
    	graph.addPort((long) 1, (short) 7, null);
    	graph.addPort((long) 2, (short) 8, null);
    	graph.addPort((long) 2, (short) 9, null);
    	graph.addPort((long) 3, (short) 10, null);
    	graph.addPort((long) 3, (short) 11, (short) 3);
    	graph.addPort((long) 3, (short) 12, (short) 4);
    	graph.addEdge((long) 1, (short) 7, (long) 2, (short) 8);
    	graph.addEdge((long) 2, (short) 9, (long) 3, (short) 10);
    	
    	// ip router
    	for (OFFlowMod fm : ipRouterRules) {
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 1));
    	}
    	
    	// gateway switch
    	for (OFFlowMod fm : gatewayRules){
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 2));
    	}
    	
    	// mac learner
    	for (OFFlowMod fm : macLearnerRules){
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 3));
    	}
    	
    	//log.error(graph);
    	
    	int fmCount = 0;
    	long startTime = System.nanoTime();
    	for (OFFlowMod fm : gatewayUpdateRules) {
    		PolicyUpdateTable updateTable = graph.update(fm, graph.getNode((long) 2));
    		fmCount += updateTable.addFlowMods.size();
			fmCount += updateTable.deleteFlowMods.size();
			System.out.println(fmCount);
    	}
    	for (OFFlowMod fm : macLearnerUpdateRules){
			PolicyUpdateTable updateTable = graph.update(fm, graph.getNode((long) 3));
			fmCount += updateTable.addFlowMods.size();
			fmCount += updateTable.deleteFlowMods.size();
			System.out.println(fmCount);
    	}
    	long elapseTime = System.nanoTime() - startTime;
    	double compileTime = elapseTime / 1e6;
    	double updateTime = 0;
    	for (int i = 0; i < fmCount; i++) {
    		updateTime += switchTime.getTime();
    	}
    	writer.write(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	System.out.println(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	*/
    }
	
	private int calculateUpdateFlowModsStrawman(List<OFFlowMod> oldFlowModsOriginal, List<OFFlowMod> newFlowMods) {
    	/*
    	int fmCount = 0;
    	
    	// assign id to flowmod
    	int count = 0;
    	for (OFFlowMod fm : newFlowMods) {
    		fm.helperId = count;
    		count++;
    	}
    	
    	// deep copy of old flowmods
    	List<OFFlowMod> oldFlowMods = new ArrayList<OFFlowMod>();
    	for (OFFlowMod fm : oldFlowModsOriginal) {
    		try {
				oldFlowMods.add(fm.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
    	}
    	
    	// store old flowmod to a hash table
    	Map<Integer, OFFlowMod> oldFlowModsMap = new HashMap<Integer, OFFlowMod>();
    	for (OFFlowMod fm : oldFlowMods) {
    		oldFlowModsMap.put(fm.helperId, fm);
    	}
    	
    	// assign priority to old flowmod
    	Map<Short, List<OFFlowMod>> flowModStoreMap = new HashMap<Short, List<OFFlowMod>>();
    	for (OFFlowMod fm : oldFlowMods) {
    		List<OFFlowMod> fms = flowModStoreMap.get(fm.getMatch().getInputPort());
    		if (fms == null) {
    			fms = new ArrayList<OFFlowMod>();
    			flowModStoreMap.put(fm.getMatch().getInputPort(), fms);
    		}
    		fms.add(fm);
    	}
    	for (Map.Entry<Short, List<OFFlowMod>> entry : flowModStoreMap.entrySet()) {
    		List<OFFlowMod> fms = entry.getValue();
    		Collections.sort(fms, new Comparator<OFFlowMod>() {
    			public int compare(OFFlowMod fm1, OFFlowMod fm2) {
    				return fm2.getPriority() - fm1.getPriority();
    			}
    		});
    		count = fms.size() - 1;
    		for (OFFlowMod fm : fms) {
    			fm.setPriority((short) count);
    			count -= 1;
    		}
    	}
    	
    	// assign priority to new flowmod
    	flowModStoreMap.clear();
    	for (OFFlowMod fm : newFlowMods) {
    		List<OFFlowMod> fms = flowModStoreMap.get(fm.getMatch().getInputPort());
    		if (fms == null) {
    			fms = new ArrayList<OFFlowMod>();
    			flowModStoreMap.put(fm.getMatch().getInputPort(), fms);
    		}
    		fms.add(fm);
    	}
    	for (Map.Entry<Short, List<OFFlowMod>> entry : flowModStoreMap.entrySet()) {
    		List<OFFlowMod> fms = entry.getValue();
    		Collections.sort(fms, new Comparator<OFFlowMod>() {
    			public int compare(OFFlowMod fm1, OFFlowMod fm2) {
    				return fm2.getPriority() - fm1.getPriority();
    			}
    		});
    		count = fms.size() - 1;
    		for (OFFlowMod fm : fms) {
    			//System.out.println(fm);
    			OFFlowMod ofm = oldFlowModsMap.get(fm.helperId);
    			if(ofm == null) {
    				fmCount += 1;
    			} else if(count != ofm.getPriority()) {
    				fmCount += 2;
    			}
    			fm.setPriority((short) count);
    			count -= 1;
    		}
    	}
    	
    	return fmCount;
    	*/
		return 0;
    }

}
