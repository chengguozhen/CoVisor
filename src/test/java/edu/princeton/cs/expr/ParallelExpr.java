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
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
