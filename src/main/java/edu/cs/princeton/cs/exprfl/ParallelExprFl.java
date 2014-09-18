package edu.cs.princeton.cs.exprfl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.expr.OFFlowModHelper;
import edu.princeton.cs.expr.SwitchTime;
import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyUpdateMechanism;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class ParallelExprFl {
	
	public void startExpr(List<OFFlowMod> monitorRules, List<OFFlowMod> MACLearnerRules) {
		
		monitorRules.add(0, OFFlowModHelper.genFlowMod(String.format("priority=0")));
		SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
		int round = 10;
		
		{
			Random rand = new Random(1);
			String fileName = String.format("experiments/PlotGraph/rres_parallel_strawman_%d", MACLearnerRules.size());
			Writer writer = null;
			try {
				writer = new FileWriter(fileName);
				for (int i = 0; i < round; i++) {
					Collections.shuffle(monitorRules.subList(1, monitorRules.size()), rand);
					Collections.shuffle(MACLearnerRules, rand);
					exprHelper(monitorRules, monitorRules.size() - 10, 10, MACLearnerRules, MACLearnerRules.size(), writer, switchTime, 0);
				}
			} catch (IOException ex) {
			} finally {
				try {writer.close();} catch (Exception ex) {}
			}
		
		}
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

}
