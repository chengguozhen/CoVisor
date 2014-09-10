package edu.princeton.cs.expr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyUpdateMechanism;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class SequentialComposition {
	
	private static Random rand = new Random(1);
	
	public SequentialComposition() {
		
	}
	
	public void testExpr() {
		List<OFFlowMod> fwRulesOriginal = readFwRules("experiments/classbench/fw1_5000");
		List<OFFlowMod> routingRulesOriginal = readRoutingRules("experiments/classbench/fw1_prefix");
		SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
		
		int fwSize = 1000;
    	//int[] routingSizes = {128, 256, 512, 1024, 2048, 4096};//, 8192};
    	int[] routingSizes = {1000, 2000, 4000, 8000, 16000, 32000};
    	int round = 10;
    	for (int routingSize : routingSizes) {
            
			System.out.println(routingSize);

			{
    			rand = new Random(1);
    			List<OFFlowMod> fwRules = new ArrayList<OFFlowMod>(fwRulesOriginal);
    			List<OFFlowMod> routingRules = new ArrayList<OFFlowMod>(routingRulesOriginal);
    			
    			String fileName = String.format("experiments/PlotGraph/res_sequential_strawman_%d", routingSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					exprHelper(fwRules, fwSize, 10, routingRules, routingSize, writer, switchTime, 0);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}
			
			/*{
    			rand = new Random(1);
    			List<OFFlowMod> fwRules = new ArrayList<OFFlowMod>(fwRulesOriginal);
    			List<OFFlowMod> routingRules = new ArrayList<OFFlowMod>(routingRulesOriginal);
    			
    			String fileName = String.format("experiments/PlotGraph/res_sequential_inc_%d", routingSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					exprHelper(fwRules, fwSize, 10, routingRules, routingSize, writer, switchTime, 1);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}

    		{
    			rand = new Random(1);
    			List<OFFlowMod> fwRules = new ArrayList<OFFlowMod>(fwRulesOriginal);
    			List<OFFlowMod> routingRules = new ArrayList<OFFlowMod>(routingRulesOriginal);
    			
    			String fileName = String.format("experiments/PlotGraph/res_sequential_incacl_%d", routingSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					exprHelper(fwRules, fwSize, 10, routingRules, routingSize, writer, switchTime, 2);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}*/
    	}
		
		// init rules
		/*List<OFFlowMod> fwRules = readFwRules("experiments/classbench/fw1_5000");
		Collections.shuffle(fwRules, rand);
		List<OFFlowMod> routingRules = readRoutingRules("experiments/classbench/fw1_prefix");
		
		//exprHelper(fwRules, routingRules, 5, 10);
		//exprHelper(fwRules, routingRules, 3000, 10);
		//exprHelper(fwRules, routingRules, 1000, 10);
		//exprHelper(fwRules, routingRules, 1000, 10);
		//exprHelper(fwRules, routingRules, 512, 10);
		//exprHelper(fwRules, routingRules, 512, 10);
		exprHelper(fwRules, routingRules, 128, 10);
		exprHelper(fwRules, routingRules, 256, 10);
		exprHelper(fwRules, routingRules, 512, 10);
		exprHelper(fwRules, routingRules, 1024, 10);
		exprHelper(fwRules, routingRules, 2048, 10);
		//exprHelper(fwRules, routingRules, 4096, 10);
		
		exprHelper(fwRules, routingRules, 2000, 10);
		exprHelper(fwRules, routingRules, 3000, 10);
		exprHelper(fwRules, routingRules, 4000, 10);
		exprHelper(fwRules, routingRules, 5000, 10);*/
	}
	
	private void exprHelper (List<OFFlowMod> fwRules, int fwSize, int fwUpdateSize,
			List<OFFlowMod> routingRules, int routingSize, Writer writer, SwitchTime switchTime,
			int mechanism)
					throws IOException {
		Collections.shuffle(fwRules.subList(1, fwRules.size()), rand);
		Collections.shuffle(routingRules.subList(1, routingRules.size()), rand);
		
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
		if (mechanism == 0 || mechanism == 1) {
			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
			storeKeys.add(PolicyFlowModStoreKey.ALL);
		} else {
			storeTypes.add(PolicyFlowModStoreType.PREFIX);
			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
			storeKeys.add(PolicyFlowModStoreKey.NETWORK_DST);
			storeKeys.add(PolicyFlowModStoreKey.ALL);
		}

		PolicyTree leftTree = new PolicyTree(storeTypes, storeKeys);
		leftTree.tenantId = 1;

		PolicyTree rightTree = new PolicyTree(storeTypes, storeKeys);
		rightTree.tenantId = 2;

		PolicyTree policyTree = new PolicyTree();
		policyTree.operator = PolicyOperator.Sequential;
		policyTree.leftChild = leftTree;
		policyTree.rightChild = rightTree;

		// install initial rules
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		for (int i = 0; i < fwSize; i++) {
			policyTree.update(fwRules.get(i), 1);
		}
		for (int i = 0; i < routingSize; i++) {
			policyTree.update(routingRules.get(i), 2);
		}

		// install update rules
		for (int i = 0; i < fwUpdateSize; i++) {
			OFFlowMod fm = fwRules.get(fwSize + i);
			//if (i % 2 == 0) {
				List<OFAction> actions = new ArrayList<OFAction>();
				fm.setActions(actions);
				
				OFActionOutput action = new OFActionOutput();
				action.setPort((short) 1);
				actions.add(action);
				
				fm.setLengthU(OFFlowMod.MINIMUM_LENGTH + action.getLengthU());
			/*} else {
				List<OFAction> actions = new ArrayList<OFAction>();
				fm.setActions(actions);
				fm.setLengthU(OFFlowMod.MINIMUM_LENGTH);
			}*/
		}
		
		if (mechanism == 0) {
			PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		} else {
			PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		}
		List<Long> elapseTimes = new ArrayList<Long>();
		List<Integer> fmCounts = new ArrayList<Integer>();
		for (int i = 0; i < fwUpdateSize; i++) {
			//System.out.println(fwRules.get(initialRuleCount + i));
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(fwRules.get(fwSize + i), 1);
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
		}
		
	}
	
	@SuppressWarnings("unused")
	private void exprHelperOld (List<OFFlowMod> fwRules, List<OFFlowMod> routingRules,
			int initialRuleCount, int updateRuleCount) {
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		//storeTypes.add(PolicyFlowModStoreType.PREFIX);
		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
		//storeKeys.add(PolicyFlowModStoreKey.NETWORK_DST);
		storeKeys.add(PolicyFlowModStoreKey.ALL);

		PolicyTree leftTree = new PolicyTree(storeTypes, storeKeys);
		leftTree.tenantId = 1;

		PolicyTree rightTree = new PolicyTree(storeTypes, storeKeys);
		rightTree.tenantId = 2;

		PolicyTree policyTree = new PolicyTree();
		policyTree.operator = PolicyOperator.Sequential;
		policyTree.leftChild = leftTree;
		policyTree.rightChild = rightTree;

		// install initial rules
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		initialRuleCount = Math.min(initialRuleCount, fwRules.size() - updateRuleCount);
		/*for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(fwRules.get(i), 1);
		}*/
		//log.error("finish firewall");
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(routingRules.get(i), 2);
			/*if (i % 500 == 0) {
				log.error("{}: {} {} {}",
						i,
						policyTree.leftChild.flowTable.getFlowMods().size(),
						policyTree.rightChild.flowTable.getFlowMods().size(),
						policyTree.flowTable.getFlowMods().size());
			}*/
		}
		//log.error("finish routing: {}", policyTree.rightChild.flowTable.getFlowMods().size());

		// install update rules
		for (int i = 0; i < updateRuleCount; i++) {
			//log.error(fwRules.get(initialRuleCount + i));
			OFFlowMod fm = fwRules.get(initialRuleCount + i);
			//if (i % 2 == 0) {
				List<OFAction> actions = new ArrayList<OFAction>();
				fm.setActions(actions);
				
				OFActionOutput action = new OFActionOutput();
				action.setPort((short) 1);
				actions.add(action);
				
				fm.setLengthU(OFFlowMod.MINIMUM_LENGTH + action.getLengthU());
			/*} else {
				List<OFAction> actions = new ArrayList<OFAction>();
				fm.setActions(actions);
				fm.setLengthU(OFFlowMod.MINIMUM_LENGTH);
			}*/
		}
		
		/*PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		for (int i = 0; i < updateRuleCount; i++) {
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(fwRules.get(initialRuleCount + i), 1);
			long elapseTime = System.nanoTime() - startTime;
			log.error("Time: {} ms\t{}\t{}\t{}\t{}\t{}",
					elapseTime / 1e6,
					updateTable.addFlowMods.size(),
					updateTable.deleteFlowMods.size(),
					policyTree.leftChild.flowTable.getFlowMods().size(),
					policyTree.rightChild.flowTable.getFlowMods().size(),
					policyTree.flowTable.getFlowMods().size());
		}*/
		
		
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		List<Long> elapseTimes = new ArrayList<Long>();
		List<Integer> fmCounts = new ArrayList<Integer>();
		for (int i = 0; i < updateRuleCount; i++) {
			//policyTree.update(fwRules.get(initialRuleCount + i), 1);
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(fwRules.get(i), 1);
			long elapseTime = System.nanoTime() - startTime; // in ns
			elapseTimes.add(elapseTime);
			fmCounts.add(updateTable.addFlowMods.size() + updateTable.deleteFlowMods.size());
		}
		//log.error("Count: {}\tTime: {} ms", initialRuleCount, elapseTime / 1e6);
		System.out.println("----------------------------------------");
		for (int i = 0; i < elapseTimes.size(); i++) {
			double compileTime = elapseTimes.get(i) / 1e6;
			int fmCount = fmCounts.get(i);
			double updateTime = 0;
	    	/*for (int j = 0; j < fmCount; j++) {
	    		updateTime += switchTime.getTime();
	    	}*/
			System.out.println(fwRules.get(i));
			System.out.println(String.format("%f\t%d\t%f\t%f", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
		}
		
	}
	
	private List<OFFlowMod> readFwRules(String fileName) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		flowMods.add(OFFlowModHelper.genFlowMod(String.format("priority=0")));

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				//log.error(line);
				
				String[] parts = line.split("\t");
				
				// source ip prefix
				// destination ip prefix
				String str = String.format("priority=%d,src-ip=%s,dst-ip=%s",
						OFFlowModHelper.getRandomNumber(1, 101),
						parts[0].substring(1),
						parts[1]
						);
				
				// source port
				String[] srcports = parts[2].split(":");
				if (srcports[0].trim().equals(srcports[1].trim())) {
					str = str + ",src-port=" + srcports[0].trim();
				}
				
				// destination port
				String[] dstports = parts[3].split(":");
				if (dstports[0].trim().equals(dstports[1].trim())) {
					str = str + ",dst-port=" + dstports[0].trim();
				}
				
				// protocol number
				String[] protocols = parts[4].split("/");
				if (protocols[1].equals("0xFF")) {
					str = str + ",protocol=" + Integer.parseInt(protocols[0].substring(2), 16);
				}
				
				// action
				if (rand.nextInt() % 10 == 1) {
					str = str + ",actions=output:1";
				}
				
				OFFlowMod fm = OFFlowModHelper.genFlowMod(str);
				flowMods.add(fm);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return flowMods;
	}
	
	private List<OFFlowMod> readRoutingRules(String fileName) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		flowMods.add(OFFlowModHelper.genFlowMod(String.format("priority=0")));

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("/");
				OFFlowMod fm = OFFlowModHelper.genFlowMod(
						String.format("priority=%s,dst-ip=%s,actions=output:1",
								parts[1],
								line.trim()));
				flowMods.add(fm);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return flowMods;
	}

}
