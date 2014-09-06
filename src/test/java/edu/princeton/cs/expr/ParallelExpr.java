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

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyUpdateMechanism;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class ParallelExpr extends TestCase {
	
	private static Logger log = LogManager.getLogger(ParallelExpr.class.getName());
	private static Random rand = new Random(1);
	
    public ParallelExpr(final String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(ParallelExpr.class);
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
    
    private void myTime(int count) {
    	int x = 0;
    	long startTime = System.nanoTime();
		for (int i = 0; i < count; i++) {
			x = 5 + rand.nextInt();
		}
		long elapseTime = System.nanoTime() - startTime;
		System.out.println(count + "\t" + elapseTime / 1e6 + "\t" + x);
    }

	public void testExpr() {
		
		SwitchTime switchTime = new SwitchTime("experiments/switch_time.txt");
    	//int[] ruleSizes = {1280, 2560, 5120, 10240, 20480, 40960};//{128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768};
    	int[] ruleSizes = {128, 256, 512, 1024, 2048};
    	int round = 10;
    	for (int ruleSize : ruleSizes) {
    		List<String> MACs = getMACs(ruleSize);
    		List<OFFlowMod> monitorRules = initMonitorRules(MACs, ruleSize + 10);
    		List<OFFlowMod> MACLearnerRules = initMACLearnerRules(MACs);
    		
    		{
    			String fileName = String.format("experiments/PlotGraph/res_parallel_strawman_%d", ruleSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					exprHelperStrawman(monitorRules, MACLearnerRules, ruleSize, 10, writer, switchTime);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}
    		
    		{
    			String fileName = String.format("experiments/PlotGraph/res_parallel_inc_%d", ruleSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					exprHelperIncremental(monitorRules, MACLearnerRules, ruleSize, 10, writer, switchTime);
    				}
    			} catch (IOException ex) {
    			} finally {
    				try {writer.close();} catch (Exception ex) {}
    			}
    		}
    		
    		{
    			String fileName = String.format("experiments/PlotGraph/res_parallel_incacl_%d", ruleSize);
    			Writer writer = null;
    			try {
    				writer = new FileWriter(fileName);
    				for (int i = 0; i < round; i++) {
    					exprHelperIncrementalACL(monitorRules, MACLearnerRules, ruleSize, 10, writer, switchTime);
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
	
	private void exprHelperStrawman (List<OFFlowMod> monitorRules, List<OFFlowMod> MACLearnerRules,
			int initialRuleCount, int updateRuleCount, Writer writer, SwitchTime switchTime)
					throws IOException {
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
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
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(monitorRules.get(i), 1);
		}
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(MACLearnerRules.get(i), 2);
		}
		
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		List<Long> elapseTimes = new ArrayList<Long>();
		List<Integer> fmCounts = new ArrayList<Integer>();
		for (int i = 0; i < updateRuleCount; i++) {
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(monitorRules.get(initialRuleCount + i), 1);
			long elapseTime = System.nanoTime() - startTime; // in ns
			elapseTimes.add(elapseTime);
			fmCounts.add(updateTable.addFlowMods.size() + updateTable.deleteFlowMods.size());
		}
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
	
	private void exprHelperIncremental (List<OFFlowMod> monitorRules, List<OFFlowMod> MACLearnerRules,
			int initialRuleCount, int updateRuleCount, Writer writer, SwitchTime switchTime)
					throws IOException {
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
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
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(monitorRules.get(i), 1);
		}
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(MACLearnerRules.get(i), 2);
		}
		
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		List<Long> elapseTimes = new ArrayList<Long>();
		List<Integer> fmCounts = new ArrayList<Integer>();
		for (int i = 0; i < updateRuleCount; i++) {
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(monitorRules.get(initialRuleCount + i), 1);
			long elapseTime = System.nanoTime() - startTime; // in ns
			elapseTimes.add(elapseTime);
			fmCounts.add(updateTable.addFlowMods.size() + updateTable.deleteFlowMods.size());
		}
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
	
	private void exprHelperIncrementalACL (List<OFFlowMod> monitorRules, List<OFFlowMod> MACLearnerRules,
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
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(monitorRules.get(i), 1);
		}
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(MACLearnerRules.get(i), 2);
		}
		
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		List<Long> elapseTimes = new ArrayList<Long>();
		List<Integer> fmCounts = new ArrayList<Integer>();
		for (int i = 0; i < updateRuleCount; i++) {
			long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(monitorRules.get(initialRuleCount + i), 1);
			long elapseTime = System.nanoTime() - startTime; // in ns
			elapseTimes.add(elapseTime);
			fmCounts.add(updateTable.addFlowMods.size() + updateTable.deleteFlowMods.size());
		}
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
	
	private void exprHelper (List<OFFlowMod> monitorRules, List<OFFlowMod> MACLearnerRules,
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
	
	// get random number in [min, max)
	public static int getRandomNumber(int min, int max) {
		return rand.nextInt(max - min) + min;
	}
	
	// get random mac, e.g., "00:17:42:EF:CD:8D"
	public static String getRandomMac() {
		String mac = String.format("%02x:%02x:%02x:%02x:%02x:%02x",
				getRandomNumber(0, 255), getRandomNumber(0, 255),
				getRandomNumber(0, 255), getRandomNumber(0, 255),
				getRandomNumber(0, 255), getRandomNumber(0, 255));
		return mac;
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
