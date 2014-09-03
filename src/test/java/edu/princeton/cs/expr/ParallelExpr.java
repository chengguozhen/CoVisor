package edu.princeton.cs.expr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

	public void testExpr() {
		
		// init rules
		List<String> MACs = getMACs(5000);
		List<OFFlowMod> monitorRules = initMonitorRules(MACs, 5000);
		List<OFFlowMod> MACLearnerRules = initMACLearnerRules(MACs);
		
		/*exprHelper(monitorRules, MACLearnerRules, 1000, 10);
		exprHelper(monitorRules, MACLearnerRules, 2000, 10);
		exprHelper(monitorRules, MACLearnerRules, 3000, 10);
		exprHelper(monitorRules, MACLearnerRules, 4000, 10);
		exprHelper(monitorRules, MACLearnerRules, 5000, 10);*/
		
		exprHelper(monitorRules, MACLearnerRules, 2048, 10);
		exprHelper(monitorRules, MACLearnerRules, 2048, 10);
		exprHelper(monitorRules, MACLearnerRules, 128, 10);
		exprHelper(monitorRules, MACLearnerRules, 256, 10);
		exprHelper(monitorRules, MACLearnerRules, 512, 10);
		exprHelper(monitorRules, MACLearnerRules, 1024, 10);
		exprHelper(monitorRules, MACLearnerRules, 2048, 10);
		
	}
	
	private void exprHelper (List<OFFlowMod> monitorRules, List<OFFlowMod> MACLearnerRules,
			int initialRuleCount, int updateRuleCount) {
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
		initialRuleCount = Math.min(initialRuleCount, monitorRules.size() - updateRuleCount);
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(monitorRules.get(i), 1);
		}
		//log.error("finish monitor");
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(MACLearnerRules.get(i), 2);
		}
		log.error("finish mac learner");

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
		long startTime = System.nanoTime();
		for (int i = 0; i < updateRuleCount; i++) {
			policyTree.update(monitorRules.get(initialRuleCount + i), 1);
		}
		long elapseTime = System.nanoTime() - startTime;
		//log.error("Time: {} ms", elapseTime / 1e6);
		System.out.println(elapseTime / 1e6);
	}
	
	private List<OFFlowMod> initMonitorRules(List<String> MACs, int ruleCount) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		
		List<Integer> MACPairs = new ArrayList<Integer>();
		for (int i = 0; i < MACs.size() * MACs.size(); i++) {
			MACPairs.add(i);
		}
		Collections.shuffle(MACPairs, rand);
		
		for (int i = 0; i < ruleCount; i++) {
			int index = MACPairs.get(i);
			OFFlowMod fm = OFFlowModHelper.genFlowMod(
					String.format("priority=1,src-mac=%s,dst-mac=%s",
							MACs.get(index / MACs.size()),
							MACs.get(index % MACs.size())));
			flowMods.add(fm);
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
