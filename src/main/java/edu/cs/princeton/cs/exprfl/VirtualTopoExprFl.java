package edu.cs.princeton.cs.exprfl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.expr.SwitchTime;
import edu.princeton.cs.hsa.PlumbingGraph;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class VirtualTopoExprFl {
	
	public void exprHelperMAC(List<OFFlowMod> ipRouterRules, List<OFFlowMod> gatewayRules, List<OFFlowMod> macLearnerRules,
			List<OFFlowMod> gatewayUpdateRules, List<OFFlowMod> macLearnerUpdateRules,
			int ipCount, int macExternal, int macInternal, int macUpdate, List<String> macs, List<String> ips,
    		Writer writer, SwitchTime switchTime, boolean aclOpt) throws IOException {
    	
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
    	for (int i = 0; i < macUpdate; i++) {
    		OFFlowMod fm = gatewayUpdateRules.get(i);
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
    	double compileTime = elapseTime / 1e6;
    	double updateTime = 0;
    	for (int i = 0; i < fmCount; i++) {
    		updateTime += switchTime.getTime();
    	}
    	writer.write(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	//System.out.println(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    }

}
