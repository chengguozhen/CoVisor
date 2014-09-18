package edu.cs.princeton.cs.exprfl;

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

import edu.princeton.cs.expr.OFFlowModHelper;
import edu.princeton.cs.expr.SwitchTime;
import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyUpdateMechanism;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class SequentialExprFl {
	
	public void startExpr(List<OFFlowMod> fwRules, List<OFFlowMod> routingRules) {
		
		fwRules.add(0, OFFlowModHelper.genFlowMod(String.format("priority=0")));
		routingRules.add(0, OFFlowModHelper.genFlowMod(String.format("priority=0")));
		SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
		int round = 10;
		
		{
			Random rand = new Random(1);
			String fileName = String.format("experiments/PlotGraph/rres_sequential_strawman_%d", routingRules.size());
			Writer writer = null;
			try {
				writer = new FileWriter(fileName);
				for (int i = 0; i < round; i++) {
					Collections.shuffle(fwRules.subList(1, fwRules.size()), rand);
					Collections.shuffle(routingRules.subList(1, fwRules.size()), rand);
					exprHelper(fwRules, fwRules.size() - 10, 10, routingRules, routingRules.size(), writer, switchTime, 0);
				}
			} catch (IOException ex) {
			} finally {
				try {writer.close();} catch (Exception ex) {}
			}
		
		}
	}
	
	private void exprHelper (List<OFFlowMod> fwRules, int fwSize, int fwUpdateSize,
			List<OFFlowMod> routingRules, int routingSize, Writer writer, SwitchTime switchTime,
			int mechanism)
					throws IOException {
		
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
	    	//System.out.println(String.format("%f\t%d\t%f\t%f\n", compileTime, fmCount, updateTime, compileTime / 1e3 + updateTime));
		}
		
	}

}
