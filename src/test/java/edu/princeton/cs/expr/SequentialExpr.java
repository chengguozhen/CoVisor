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

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyUpdateMechanism;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class SequentialExpr extends TestCase {
	
	private static Logger log = LogManager.getLogger(SequentialExpr.class.getName());
	private static Random rand = new Random(1);
	
    public SequentialExpr(final String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(SequentialExpr.class);
    }
    
    public void atestTrie() {
		// init policy tree
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
		
		policyTree.update(OFFlowModHelper.genFlowMod("priority=8,ether-type=2048,dst-ip=0.0.0.0/8"), 1);
		policyTree.update(OFFlowModHelper.genFlowMod("priority=8,ether-type=2048,dst-ip=1.0.0.0/8,actions=output:1"), 1);
		policyTree.update(OFFlowModHelper.genFlowMod("priority=20,ether-type=2048,dst-ip=1.0.0.0/20"), 1);
		policyTree.update(OFFlowModHelper.genFlowMod("priority=24,ether-type=2048,dst-ip=1.0.0.0/24,actions=output:1"), 1);
		policyTree.update(OFFlowModHelper.genFlowMod("priority=32,ether-type=2048,dst-ip=1.0.0.0/32,actions=output:1"), 1);
		policyTree.update(OFFlowModHelper.genFlowMod("priority=16,ether-type=2048,dst-ip=1.0.0.0/16,actions=output:2"), 2);
		policyTree.update(OFFlowModHelper.genFlowMod("priority=16,ether-type=2048,dst-ip=1.1.0.0/16,actions=output:3"), 2);
		policyTree.update(OFFlowModHelper.genFlowMod("priority=16,ether-type=2048,dst-ip=0.0.0.0/0,actions=output:4"), 2);
		
		
		log.error(policyTree.leftChild.flowTable);
		log.error(policyTree.rightChild.flowTable);
		log.error(policyTree.flowTable);
    	
    }
    
    public void atestTriePerformance() {
    	
    	int initialRuleCount = 100000;
    	int updateRuleCount = 10;
    	List<OFFlowMod> fwRules = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < updateRuleCount; i++) {
    		int priority = OFFlowModHelper.getRandomNumber(1, 33);
    		int ip = (rand.nextInt() >> priority) << priority;
    		fwRules.add(
    				OFFlowModHelper.genFlowMod(
    						String.format("priority=%d,src-ip=1.0.0.0/16,dst-ip=%d/%d,actions=output:2",
    								priority, ip, priority)));
    	}
    	
    	List<OFFlowMod> routingRules = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < initialRuleCount; i++) {
    		int priority = OFFlowModHelper.getRandomNumber(1, 33);
    		int ip = (rand.nextInt() >> priority) << priority;
    		routingRules.add(
    				OFFlowModHelper.genFlowMod(
    						String.format("priority=%d,dst-ip=%d/%d,actions=output:2",
    								priority, ip, priority)));
    	}
    	
    	//exprHelper(fwRules, routingRules, 2000, 10);
    	/*exprHelper(fwRules, routingRules, 40000, 2);
    	exprHelper(fwRules, routingRules, 80000, 2);
    	exprHelper(fwRules, routingRules, 4000, 1);
    	exprHelper(fwRules, routingRules, 5000, 1);
    	exprHelper(fwRules, routingRules, 6000, 1);
    	exprHelper(fwRules, routingRules, 7000, 1);
    	exprHelper(fwRules, routingRules, 8000, 1);
    	exprHelper(fwRules, routingRules, 9000, 1);*/
    }

	public void testExpr() {
		SequentialComposition expr = new SequentialComposition();
		expr.testExpr();
		
		//Assert.assertEquals(true, true);
		System.out.println("done");
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
