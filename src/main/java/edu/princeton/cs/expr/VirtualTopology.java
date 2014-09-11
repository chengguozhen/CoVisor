package edu.princeton.cs.expr;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.hsa.PlumbingGraph;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class VirtualTopology {
	
	private static Logger log = LogManager.getLogger(VirtualTopology.class.getName());
	private static Random rand = new Random(1);
	
	public VirtualTopology() {
		
	}
	
	public void testExpr() {
    	
    	List<String> macs = genMACs(100000);
    	List<String> ips = genIPs(100000);
    	//exprHelperIP(3, 3, 3, 1, macs, ips);
    	//exprHelperMAC(4, 3, 3, 1, macs, ips);
    	
    	/*exprHelperIP(16, 3, 3, 2);
    	exprHelperIP(32, 3, 3, 2);
    	exprHelperIP(64, 3, 3, 2);
    	exprHelperIP(128, 3, 3, 2);
    	exprHelperIP(256, 3, 3, 2);
    	exprHelperIP(512, 3, 3, 2);
    	exprHelperIP(1024, 3, 3, 2);*/
    	
    	/*exprHelperIP(8, 100, 500, 1, macs, ips);
    	exprHelperIP(8, 100, 500, 1, macs, ips);
    	exprHelperIP(8, 100, 500, 1, macs, ips);
    	exprHelperIP(8, 100, 500, 1, macs, ips);
    	exprHelperIP(16, 100, 500, 2, macs, ips);
    	exprHelperIP(64, 100, 500, 6, macs, ips);
    	exprHelperIP(128, 100, 500, 13, macs, ips);
    	exprHelperIP(256, 100, 500, 26, macs, ips);
    	exprHelperIP(512, 100, 500, 51, macs, ips);
    	exprHelperIP(512, 100, 500, 51, macs, ips);
    	exprHelperIP(512, 100, 500, 51, macs, ips);
    	exprHelperIP(1024, 100, 500, 102, macs, ips);*/
    	
    	SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
    	int[] ipCount = {1000, 2000, 4000, 8000, 16000, 32000};//, 64000};
    	int round = 100;
    	for (int i : ipCount) {
    		System.out.println(i);
    		
			{
				String fileName = String.format("experiments/PlotGraph/res_gateway_strawman_%d", i);
				Writer writer = null;
				try {
					writer = new FileWriter(fileName);
					for (int j = 0; j < round; j++) {
						// exprHelperIP(i, 100, 500, (int) Math.ceil(i * 0.1), macs, ips, writer, switchTime);

						// exprHelperMACStrawman(i, 2, 1, 1, macs, ips, writer, switchTime);
						// exprHelperMAC(i, 2, 1, 1, macs, ips, writer, switchTime, false);
						// exprHelperMAC(i, 2, 1, 1, macs, ips, writer, switchTime, true);
						exprHelperMACStrawman(i, 100, 900, 1, macs, ips, writer, switchTime);
						// exprHelperMAC(i, 100, 900, 1, macs, ips, writer, switchTime, false);
						// exprHelperMAC(i, 100, 900, 1, macs, ips, writer, switchTime, true);
					}
				} catch (IOException ex) {
				} finally {
					try {writer.close();} catch (Exception ex) {}
				}
			}
			
			{
				String fileName = String.format("experiments/PlotGraph/res_gateway_inc_%d", i);
				Writer writer = null;
				try {
					writer = new FileWriter(fileName);
					for (int j = 0; j < round; j++) {
						exprHelperMAC(i, 100, 900, 1, macs, ips, writer, switchTime, false);
					}
				} catch (IOException ex) {
				} finally {
					try {writer.close();} catch (Exception ex) {}
				}
			}
			
			{
				String fileName = String.format("experiments/PlotGraph/res_gateway_incacl_%d", i);
				Writer writer = null;
				try {
					writer = new FileWriter(fileName);
					for (int j = 0; j < round; j++) {
						exprHelperMAC(i, 100, 900, 1, macs, ips, writer, switchTime, true);
					}
				} catch (IOException ex) {
				} finally {
					try {writer.close();} catch (Exception ex) {}
				}
			}
    	}
    	
    	
    	/*exprHelperIP(100, 10, 500, 1, macs, ips);
    	exprHelperIP(100, 100, 500, 1, macs, ips);
    	exprHelperIP(100, 500, 500, 1, macs, ips);*/
    	
    	/*exprHelperMAC(10, 100, 500, 1, macs, ips);
    	exprHelperMAC(100, 100, 500, 1, macs, ips);
    	exprHelperMAC(1000, 100, 500, 1, macs, ips);
    	exprHelperMAC(2000, 100, 500, 1, macs, ips);
    	exprHelperMAC(4000, 100, 500, 1, macs, ips);
    	exprHelperMAC(8000, 100, 500, 1, macs, ips);
    	exprHelperMAC(16000, 100, 500, 1, macs, ips);
    	exprHelperMAC(32000, 100, 500, 1, macs, ips);
    	exprHelperMAC(64000, 100, 500, 1, macs, ips);
    	
    	exprHelperMAC(100, 10, 500, 1, macs, ips);
    	exprHelperMAC(100, 100, 500, 1, macs, ips);
    	exprHelperMAC(100, 200, 500, 1, macs, ips);
    	exprHelperMAC(100, 300, 500, 1, macs, ips);
    	exprHelperMAC(100, 400, 500, 1, macs, ips);
    	exprHelperMAC(100, 500, 500, 1, macs, ips);*/

    }
    
    public void exprHelperIP(int ipCount, int macExternal, int macInternal, int ipUpdate, List<String> macs, List<String> ips,
    		Writer writer, SwitchTime switchTime) throws IOException {
    	
    	List<OFFlowMod> ipRouterRules = initIPRouterRules(ipCount);
    	List<OFFlowMod> gatewayRules = initGatewayRules(macExternal, ips, macs);
    	List<OFFlowMod> macLearnerRules = initMACLearnerRules(macExternal, macInternal, macs);
    	
    	// build topology
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
    	
    	// delete old, add new
    	int fmCount = 0;
    	long startTime = System.nanoTime();
    	for (int i = 0; i < ipUpdate; i++) {
    		OFFlowMod fm = ipRouterRules.get(i);
			fm.setCommand(OFFlowMod.OFPFC_DELETE);
			PolicyUpdateTable updateTable = graph.update(fm, graph.getNode((long) 1));
			fmCount += updateTable.addFlowMods.size();
			fmCount += updateTable.deleteFlowMods.size();
    	}
    	//log.error(graph);
    	for (int i = 0; i < ipUpdate; i++) {
			OFFlowMod fm = ipRouterRules.get(i);
			((OFActionOutput) fm.getActions().get(0)).setPort((short) 6);
			fm.setCommand(OFFlowMod.OFPFC_ADD);
			PolicyUpdateTable updateTable = graph.update(fm, graph.getNode((long) 1));
			fmCount += updateTable.addFlowMods.size();
			fmCount += updateTable.deleteFlowMods.size();
    	}
    	//log.error(graph);
    	long elapseTime = System.nanoTime() - startTime;
		//System.out.println(elapseTime / 1e6 + "\t" + fmCount + "\t" + graph.flowTable.getFlowMods().size());
    	double compileTime = elapseTime / 1e6;
    	double updateTime = 0;
    	for (int i = 0; i < fmCount; i++) {
    		updateTime += switchTime.getTime();
    	}
    	//writer.write(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	System.out.println(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    }
    
    public void exprHelperMAC(int ipCount, int macExternal, int macInternal, int macUpdate, List<String> macs, List<String> ips,
    		Writer writer, SwitchTime switchTime, boolean aclOpt) throws IOException {
    	
    	List<OFFlowMod> ipRouterRules = initIPRouterRules(ipCount);
    	List<OFFlowMod> gatewayRules = initGatewayRules(macExternal, ips, macs);
    	List<OFFlowMod> macLearnerRules = initMACLearnerRules(macExternal, macInternal, macs);
    	
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
    	
    	List<OFFlowMod> gatewayUpdateRules = initGatewayRules(macUpdate, ips.subList(macExternal, ips.size()), macs.subList(macExternal, macs.size()));
    	List<OFFlowMod> macLearnerUpdateRules = initMACLearnerRules(macUpdate, 0, macs.subList(macExternal, macs.size()));
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
    
    public void exprHelperMACStrawman(int ipCount, int macExternal, int macInternal, int macUpdate, List<String> macs, List<String> ips,
    		Writer writer, SwitchTime switchTime) throws IOException {
    	
    	List<OFFlowMod> ipRouterRules = initIPRouterRules(ipCount);
    	List<OFFlowMod> gatewayRules = initGatewayRules(macExternal, ips, macs);
    	List<OFFlowMod> macLearnerRules = initMACLearnerRules(macExternal, macInternal, macs);
    	List<OFFlowMod> gatewayUpdateRules = initGatewayRules(macUpdate, ips.subList(macExternal, ips.size()), macs.subList(macExternal, macs.size()));
    	List<OFFlowMod> macLearnerUpdateRules = initMACLearnerRules(macUpdate, 0, macs.subList(macExternal, macs.size()));
    	
    	// build topology
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
    	
    	List<OFFlowMod> newFlowMods = graph.flowTable.getFlowMods();
    	fmCount = this.calculateUpdateFlowModsStrawman(oldFlowMods, newFlowMods);
    	
    	double compileTime = elapseTime / 1e6;
    	double updateTime = 0;
    	for (int i = 0; i < fmCount; i++) {
    		updateTime += switchTime.getTime();
    	}
    	
    	writer.write(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	//System.out.println(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
    	
    }
    
    private int calculateUpdateFlowModsStrawman(List<OFFlowMod> oldFlowModsOriginal, List<OFFlowMod> newFlowMods) {
    	
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
    }
    
    private List<OFFlowMod> initIPRouterRules(int count) {
    	List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < 254 && i < count; i++) {
    		String rule = String.format("priority=16,ether-type=2048,dst-ip=%d.0.0.0/16,actions=output:5", i + 2);
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	for (int i = 254; i < 254*256 && i < count; i++) {
    		String rule = String.format("priority=16,ether-type=2048,dst-ip=%d.%d.0.0/16,actions=output:5", i % 254 + 2, i / 254);
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	
    	String rule = "priority=16,ether-type=2048,dst-ip=1.0.0.0/16,actions=output:7";
    	flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	
    	return flowMods;
    }
    
    private List<OFFlowMod> initGatewayRules(int count, List<String> ips, List<String> macs) {
    	List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < count; i++) {
    		String rule = String.format("priority=1,ether-type=2048,inport=8,dst-ip=%s,"
					+ "actions=output:9,actions=set-src-mac:11:11:11:11:11:11,actions=set-dst-mac:%s",
					ips.get(i), macs.get(i));
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	
    	String rule = "priority=1,ether-type=2048,inport=9,actions=output:8";
    	flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	
    	return flowMods;
    }
    
    private List<OFFlowMod> initMACLearnerRules(int external, int internal,  List<String> macs) {
    	List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < external; i++) {
    		String rule = String.format("priority=1,inport=10,"
    				+ "src-mac=11:11:11:11:11:11,dst-mac=%s,actions=output:12", macs.get(i));
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	for (int i = 0; i < external; i++) {
    		String rule = String.format("priority=1,inport=12,"
    				+ "src-mac=%s,dst-mac=11:11:11:11:11:11,actions=output:10", macs.get(i));
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	for (int i = 0; i < internal; i++) {
    		String rule = String.format("priority=1,inport=11,"
    				+ "src-mac=00:00:00:00:11:11,dst-mac=%s,actions=output:12", macs.get(i));
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	
    	return flowMods;
    }
    
    private List<String> genMACs(int count) {
    	List<String> macs = new ArrayList<String>();
    	for (int i = 0; i < 256 && i < count; i++) {
    		macs.add(String.format("00:00:00:00:00:%02x", i));
    	}
    	for (int i = 256; i < 256*256 && i < count; i++) {
    		macs.add(String.format("00:00:00:00:%02x:%02x", i / 256, i % 256));
    	}
    	return macs;
    }
    
    private List<String> genIPs(int count) {
    	List<String> ips = new ArrayList<String>();
    	for (int i = 0; i < 256 && i < count; i++) {
    		ips.add(String.format("1.0.0.%d", i));
    	}
    	for (int i = 256; i < 256*256 && i < count; i++) {
    		ips.add(String.format("1.0.%d.%d", i / 256, i % 256));
    	}
    	return ips;
    }

}
