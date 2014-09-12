package edu.princeton.cs.expr;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyUpdateMechanism;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class ParallelComposition {
	
	private static Logger log = LogManager.getLogger(ParallelComposition.class.getName());
	private static Random rand = new Random(1);
	
	public ParallelComposition() {
		
	}
	
	public void atestCorrectness() {
    	List<String> MACs = getMACs(50);
		List<OFFlowMod> MACLearnerRules = initMACLearnerRules(MACs);
		
		int initialRuleCount = 5;
		
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		storeTypes.add(PolicyFlowModStoreType.EXACT);
		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
		storeKeys.add(PolicyFlowModStoreKey.DATA_DST);
		storeKeys.add(PolicyFlowModStoreKey.ALL);

		PolicyTree leftTree = new PolicyTree(storeTypes, storeKeys);
		leftTree.tenantId = 1;

		PolicyTree rightTree = new PolicyTree(storeTypes, storeKeys);
		rightTree.tenantId = 2;

		PolicyTree policyTree = new PolicyTree();
		policyTree.operator = PolicyOperator.Parallel;
		policyTree.leftChild = leftTree;
		policyTree.rightChild = rightTree;

		// install initial rules
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(MACLearnerRules.get(i), 2);
		}

		//PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		policyTree.update(OFFlowModHelper.genFlowMod(
				String.format("priority=1,src-mac=0f:a3:70:21:95:7c,dst-mac=61:4e:a2:7c:3e:93")), 1);
		log.error("----------------------------------------");
		log.error(policyTree.leftChild.flowTable);
		log.error(policyTree.rightChild.flowTable);
		log.error(policyTree.flowTable);
		
		policyTree.update(OFFlowModHelper.genFlowMod(
				String.format("priority=1,src-mac=2c:5b:b2:0d:f4:85,dst-mac=49:c5:d7:c9:67:f0")), 1);
		log.error("----------------------------------------");
		log.error(policyTree.leftChild.flowTable);
		log.error(policyTree.rightChild.flowTable);
		log.error(policyTree.flowTable);

    }
	
	public void testExpr() {
		
		SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
		
		int monitorSize = 1000;
    	int[] macSizes = {100, 2000};//, 4000, 8000, 16000, 32000};//, 64000, 128000};
    	int round = 10;
    	for (int macSize : macSizes) {
			System.out.println(macSize);
			
			/*{
    			rand = new Random(1);
    			OFFlowModHelper.rand = new Random(1);
    			String fileName = String.format("experiments/PlotGraph/res_parallel_strawman_%d", macSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					List<String> MACs = getMACs(macSize);
    		    		List<OFFlowMod> monitorRules = initMonitorRules(MACs, monitorSize + 10);
    		    		List<OFFlowMod> MACLearnerRules = initMACLearnerRules(MACs);
    					exprHelper(monitorRules, monitorSize, 10, MACLearnerRules, macSize, writer, switchTime, 0);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}
			
			{
    			rand = new Random(1);
    			OFFlowModHelper.rand = new Random(1);
    			String fileName = String.format("experiments/PlotGraph/res_parallel_inc_%d", macSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					List<String> MACs = getMACs(macSize);
    		    		List<OFFlowMod> monitorRules = initMonitorRules(MACs, monitorSize + 10);
    		    		List<OFFlowMod> MACLearnerRules = initMACLearnerRules(MACs);
    					exprHelper(monitorRules, monitorSize, 10, MACLearnerRules, macSize, writer, switchTime, 1);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}*/
    		
    		{
    			rand = new Random(1);
    			OFFlowModHelper.rand = new Random(1);
    			String fileName = String.format("experiments/PlotGraph/res_parallel_incacl_%d", macSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					List<String> MACs = getMACs(macSize);
    		    		List<OFFlowMod> monitorRules = initMonitorRules(MACs, monitorSize + 10);
    		    		List<OFFlowMod> MACLearnerRules = initMACLearnerRules(MACs);
    					exprHelper(monitorRules, monitorSize, 10, MACLearnerRules, macSize, writer, switchTime, 2);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}
    	}
		
		
		/*System.out.println("begin\t" + monitorRules.size() + "\t" + MACLearnerRules.size());
		System.out.flush();
		for (int i = 0; i < 10; i++) {
			exprHelper(monitorRules, MACLearnerRules, 5000, 10, switchTime);
			System.out.println("----------");
			System.out.flush();
		}*/
		
		/*
		SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
		List<String> MACs = getMACs(5000);
		List<OFFlowMod> monitorRules = initMonitorRules(MACs, 5000);
		List<OFFlowMod> MACLearnerRules = initMACLearnerRules(MACs);
		exprHelper(monitorRules, MACLearnerRules, 1000, 10);
		exprHelper(monitorRules, MACLearnerRules, 2000, 10);
		exprHelper(monitorRules, MACLearnerRules, 3000, 10);
		exprHelper(monitorRules, MACLearnerRules, 4000, 10);
		exprHelper(monitorRules, MACLearnerRules, 5000, 10);
		exprHelper(monitorRules, MACLearnerRules, 128, 10);
		exprHelper(monitorRules, MACLearnerRules, 256, 10);
		exprHelper(monitorRules, MACLearnerRules, 512, 10);
		exprHelper(monitorRules, MACLearnerRules, 1024, 10);
		exprHelper(monitorRules, MACLearnerRules, 2048, 10);*/
		
	}
	
	private void exprHelper (List<OFFlowMod> monitorRules, int monitorSize, int monitorUpdateSize,
			List<OFFlowMod> MACLearnerRules, int macSize, Writer writer, SwitchTime switchTime,
			int mechanism)
					throws IOException {
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
		if (mechanism == 0 || mechanism == 1) {
			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
			storeKeys.add(PolicyFlowModStoreKey.ALL);
		} else {
			storeTypes.add(PolicyFlowModStoreType.EXACT);
			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
			storeKeys.add(PolicyFlowModStoreKey.DATA_DST);
			storeKeys.add(PolicyFlowModStoreKey.ALL);
		}

		PolicyTree leftTree = new PolicyTree(storeTypes, storeKeys);
		leftTree.tenantId = 1;

		PolicyTree rightTree = new PolicyTree(storeTypes, storeKeys);
		rightTree.tenantId = 2;

		PolicyTree policyTree = new PolicyTree();
		policyTree.operator = PolicyOperator.Parallel;
		policyTree.leftChild = leftTree;
		policyTree.rightChild = rightTree;

		// install initial rules
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		for (int i = 0; i < monitorSize; i++) {
			policyTree.update(monitorRules.get(i), 1);
		}
		for (int i = 0; i < macSize; i++) {
			policyTree.update(MACLearnerRules.get(i), 2);
		}
		
		if (mechanism == 0) {
			PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		} else {
			PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		}
		List<Long> elapseTimes = new ArrayList<Long>();
		List<Integer> fmCounts = new ArrayList<Integer>();
		for (int i = 0; i < monitorUpdateSize; i++) {
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(monitorRules.get(monitorSize + i), 1);
			long elapseTime = System.nanoTime() - startTime; // in ns
			elapseTimes.add(elapseTime);
			fmCounts.add(updateTable.addFlowMods.size() + updateTable.deleteFlowMods.size());
		}
		System.out.println(policyTree.flowTable.getFlowMods().size()
				+ " " + policyTree.leftChild.flowTable.getFlowMods().size()
				+ " " + policyTree.rightChild.flowTable.getFlowMods().size());
		for (int i = 0; i < elapseTimes.size(); i++) {
			double compileTime = elapseTimes.get(i) / 1e6;
			int fmCount = fmCounts.get(i);
			double updateTime = 0;
	    	for (int j = 0; j < fmCount; j++) {
	    		updateTime += switchTime.getTime();
	    	}
			writer.write(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
			//System.out.print(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
		}
	}
	
	@SuppressWarnings("unused")
	private void exprHelperOld (List<OFFlowMod> monitorRules, List<OFFlowMod> MACLearnerRules,
			int initialRuleCount, int updateRuleCount, Writer writer, SwitchTime switchTime)
					throws IOException {
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		storeTypes.add(PolicyFlowModStoreType.EXACT);
		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
		storeKeys.add(PolicyFlowModStoreKey.DATA_DST);
		storeKeys.add(PolicyFlowModStoreKey.ALL);

		PolicyTree leftTree = new PolicyTree(storeTypes, storeKeys);
		leftTree.tenantId = 1;

		PolicyTree rightTree = new PolicyTree(storeTypes, storeKeys);
		rightTree.tenantId = 2;

		PolicyTree policyTree = new PolicyTree();
		policyTree.operator = PolicyOperator.Parallel;
		policyTree.leftChild = leftTree;
		policyTree.rightChild = rightTree;

		// install initial rules
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		/*for (int i = 0; i < initialRuleCount - updateRuleCount; i++) {
			policyTree.update(monitorRules.get(i), 1);
		}*/
		//log.error("finish monitor");
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(MACLearnerRules.get(i), 2);
		}
		//log.error("finish mac learner");

		// install update rules
		/*PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		for (int i = 0; i < updateRuleCount; i++) {
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(monitorRules.get(initialRuleCount + i), 1);
			long elapseTime = System.nanoTime() - startTime;
			log.error("Time: {} ms\t{}\t{}\t{}\t{}",
					elapseTime / 1e6,
					updateTable.addFlowMods.size(),
					updateTable.deleteFlowMods.size(),
					policyTree.leftChild.flowTable.getFlowMods().size(),
					policyTree.rightChild.flowTable.getFlowMods().size());
		}*/
		
		//PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		List<Long> elapseTimes = new ArrayList<Long>();
		List<Integer> fmCounts = new ArrayList<Integer>();
		for (int i = 0; i < updateRuleCount; i++) {
			//System.out.println(initialRuleCount+ "\t" + i);
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(monitorRules.get(initialRuleCount -updateRuleCount + i), 1);
			long elapseTime = System.nanoTime() - startTime; // in ns
			elapseTimes.add(elapseTime);
			fmCounts.add(updateTable.addFlowMods.size() + updateTable.deleteFlowMods.size());
		}
		//log.error("Time: {} ms", elapseTime / 1e6); // in ms
		//System.out.println(elapseTime / 1e7);
		for (int i = 0; i < elapseTimes.size(); i++) {
			double compileTime = elapseTimes.get(i) / 1e7;
			int fmCount = fmCounts.get(i);
			double updateTime = 0;
	    	for (int j = 0; j < fmCount; j++) {
	    		updateTime += switchTime.getTime();
	    	}
			//System.out.println(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
			writer.write(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
		}
	}
	
	private List<OFFlowMod> initMonitorRules(List<String> MACs, int ruleCount) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		flowMods.add(OFFlowModHelper.genFlowMod(String.format("priority=0")));
		
		List<Integer> macIndex = new ArrayList<Integer>();
		for (int i = 0; i < MACs.size(); i++) {
			macIndex.add(i);
		}
		
		int curCount = 0;
		for (int i = 0; i < MACs.size(); i++) {
			String srcMAC = MACs.get(i);
			int dstMACCount = OFFlowModHelper.getRandomNumber(0, MACs.size());
			Collections.shuffle(macIndex, rand);
			for (int j = 0; j < dstMACCount; j++) {
				String dstMAC = MACs.get(macIndex.get(j));
				OFFlowMod fm = OFFlowModHelper.genFlowMod(
						String.format("priority=1,src-mac=%s,dst-mac=%s", srcMAC, dstMAC));
				flowMods.add(fm);
				curCount++;
				if (curCount >= ruleCount) {
					return flowMods;
				}
			}
		}
		return flowMods;
	}
	
	private List<OFFlowMod> initMACLearnerRules(List<String> MACs) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		for (String MAC : MACs) {
			OFFlowMod fm = OFFlowModHelper.genFlowMod(
					String.format("priority=1,dst-mac=%s,actions=output:1", MAC));
			flowMods.add(fm);
		}
		return flowMods;
	}
	
	private List<String> getMACs(int count) {
		List<String> MACs = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			MACs.add(getRandomMac());
		}
		return MACs;
	}
	
	// get random mac, e.g., "00:17:42:EF:CD:8D"
	public static String getRandomMac() {
		String mac = String.format("%02x:%02x:%02x:%02x:%02x:%02x",
				OFFlowModHelper.getRandomNumber(0, 255), OFFlowModHelper.getRandomNumber(0, 255),
				OFFlowModHelper.getRandomNumber(0, 255), OFFlowModHelper.getRandomNumber(0, 255),
				OFFlowModHelper.getRandomNumber(0, 255), OFFlowModHelper.getRandomNumber(0, 255));
		return mac;
	}

}
