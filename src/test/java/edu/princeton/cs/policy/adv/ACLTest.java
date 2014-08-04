package edu.princeton.cs.policy.adv;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyUpdateMechanism;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ACLTest extends TestCase {
	
	private static Logger log = LogManager.getLogger(ACLTest.class.getName());
	private static Random rand = new Random();
	
    public ACLTest(final String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(ACLTest.class);
    }

    // Monitoring: srcMac, dstMac
    // Routing: dstMac
    // index: dstMac
	public void testParallelExactDstMac() {
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

    	// monitoring policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 1);
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:01", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:10", OFFlowMod.OFPFC_ADD), 1);
		
		// routing policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 2);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:01", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:02", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("********************************************************************************");
		log.error("policy tree test 1: parallel composition M + R");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:20", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:03", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:03", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:04", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 1: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:01", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:10", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:01", 1, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:02", 2, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 1: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:01", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:10", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:01", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:02", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 1: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:20", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(1, "00:00:00:00:10:00", "00:00:00:00:00:03", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:03", 1, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRouterDstMacRule(1, "00:00:00:00:00:04", 2, OFFlowMod.OFPFC_DELETE), 2);

		log.error("policy tree test 1: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// update flow mods
		PolicyUpdateTable updateTable = null;
		log.error("policy tree test 1: pre add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		updateTable = policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(0, "00:00:00:00:10:00", "00:00:00:00:00:02", OFFlowMod.OFPFC_ADD), 1);
		log.error("policy tree test 1: after add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
		
		updateTable = policyTree.update(RuleGenerationUtil.generateMonitorSrcDstMacRule(0, "00:00:00:00:10:00", "00:00:00:00:00:02", OFFlowMod.OFPFC_DELETE), 1);
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
	}
	
	// Firewall: srcPrefix, dstPrefix, srcPort, dstPort, protocol
    // Routing: dstPrefix
    // index: dstPrefix
	public void testSequentialPrefixDstIp() {
		PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		
    	List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
    	storeTypes.add(PolicyFlowModStoreType.PREFIX);
    	storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    	List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
    	storeKeys.add(PolicyFlowModStoreKey.NETWORK_DST);
    	storeKeys.add(PolicyFlowModStoreKey.ALL);
    	
    	PolicyTree leftTree = new PolicyTree(storeTypes, storeKeys);
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree(storeTypes, storeKeys);
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Sequential;
    	policyTree.leftChild = leftTree;
    	policyTree.rightChild = rightTree;
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
