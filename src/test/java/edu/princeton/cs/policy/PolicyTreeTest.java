package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.onrc.openvirtex.elements.address.PhysicalIPAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.PolicyTree.PolicyUpdateMechanism;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PolicyTreeTest extends TestCase {

	private static Logger log = LogManager.getLogger(PolicyTreeTest.class.getName());
	private static Random rand = new Random();
	
    public PolicyTreeTest(final String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(PolicyTreeTest.class);
    }

    // parallel composition case: M + R
    public void atest1() {
    	PolicyTree leftTree = new PolicyTree();
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree();
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Parallel;
    	policyTree.leftChild = leftTree;
    	policyTree.rightChild = rightTree;
		
    	// monitoring policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 1);
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "1.0.0.0", 24, OFFlowMod.OFPFC_ADD), 1);
		
		// routing policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("********************************************************************************");
		log.error("policy tree test 1: parallel composition M + R");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "3.0.0.0", 10, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 1: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "1.0.0.0", 24, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 1: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "1.0.0.0", 24, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 1: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateMonotoringRule(1, "3.0.0.0", 10, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_DELETE), 2);

		log.error("policy tree test 1: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// update flow mods
		PolicyUpdateTable updateTable = null;
		updateTable = policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
		
		updateTable = policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_DELETE), 2);
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
    }
    
    // sequential composition case: LB >> R
    public void atest2() {
    	PolicyTree leftTree = new PolicyTree();
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree();
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Sequential;
    	policyTree.leftChild = leftTree;
    	policyTree.rightChild = rightTree;
		
		// load balancing policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 1);
		policyTree.update(RuleGenerationUtil.generateLBRule(3, "0.0.0.0", 2, "3.0.0.0", "2.0.0.1", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateLBRule(1, "0.0.0.0", 0, "3.0.0.0", "2.0.0.2", OFFlowMod.OFPFC_ADD), 1);
		
		// routing policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("********************************************************************************");
		log.error("policy tree test 2: sequential composition LB >> R");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateLBRule(4, "1.0.0.0", 10, "3.0.0.0", "2.0.0.4", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 2: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateLBRule(3, "0.0.0.0", 2, "3.0.0.0", "2.0.0.1", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateLBRule(1, "0.0.0.0", 0, "3.0.0.0", "2.0.0.2", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 2: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateLBRule(3, "0.0.0.0", 2, "3.0.0.0", "2.0.0.1", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateLBRule(1, "0.0.0.0", 0, "3.0.0.0", "2.0.0.2", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 2: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateLBRule(4, "1.0.0.0", 10, "3.0.0.0", "2.0.0.4", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 2: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// update flow mods
		PolicyUpdateTable updateTable = null;
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		log.error("policy tree test 2: pre add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		updateTable = policyTree.update(RuleGenerationUtil.generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD), 1);
		log.error("policy tree test 2: after add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
		
		updateTable = policyTree.update(RuleGenerationUtil.generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_DELETE), 1);
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());		
    }
    
    // sequential composition case: FW >> R
    public void test4() {
    	PolicyTree leftTree = new PolicyTree();
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree();
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Sequential;
    	policyTree.leftChild = leftTree;
    	policyTree.rightChild = rightTree;
		
    	PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
    	PolicyTree.ActionOutputAsPass = true;
    	
		// firewall policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 1);
		policyTree.update(RuleGenerationUtil.generateFWRule(1, "2.0.0.1", 32, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateFWRule(2, "2.0.0.2", 32, OFFlowMod.OFPFC_ADD), 1);
		
		// routing policy
		policyTree.update(RuleGenerationUtil.generateDefaultRule(), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("********************************************************************************");
		log.error("policy tree test 2: sequential composition FW >> R");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateFWRule(1, "2.1.0.0", 24, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateFWRule(2, "2.0.0.5", 32, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.1.0.1", 3, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 2: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateFWRule(1, "2.0.0.1", 32, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateFWRule(2, "2.0.0.2", 32, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 2: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(RuleGenerationUtil.generateFWRule(1, "2.0.0.1", 32, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateFWRule(2, "2.0.0.2", 32, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 2: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(RuleGenerationUtil.generateFWRule(1, "2.1.0.0", 24, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateFWRule(2, "2.0.0.5", 32, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.1.0.1", 3, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 2: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// update flow mods
		PolicyUpdateTable updateTable = null;
		policyTree.update(RuleGenerationUtil.generateRoutingRule(1, "2.1.0.1", 3, OFFlowMod.OFPFC_ADD), 2);
		log.error("policy tree test 2: pre add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		updateTable = policyTree.update(RuleGenerationUtil.generateFWRule(1, "2.1.0.0", 24, OFFlowMod.OFPFC_ADD), 1);
		log.error("policy tree test 2: after add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
		
		updateTable = policyTree.update(RuleGenerationUtil.generateFWRule(1, "2.1.0.0", 24, OFFlowMod.OFPFC_DELETE), 1);
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());		
    }
    
    // experiment: M + R
    public void atest3() {
    	
    	//int mInitial = 70;
    	//int rInitial = 70;
    	//int mUpdate = 100;
    	
    	// bootstrap expr
    	/*System.out.println("Incremental:");
    	for (int mInitial = 10, rInitial = 10; mInitial < 110; mInitial += 10, rInitial += 10) {
    	    	runParallelCompositionExprBootstrap(mInitial, rInitial, PolicyUpdateMechanism.Incremental);
    	}
    	
    	System.out.println("Strawman:");
    	for (int mInitial = 10, rInitial = 10; mInitial < 110; mInitial += 10, rInitial += 10) {
			runParallelCompositionExprBootstrap(mInitial, rInitial, PolicyUpdateMechanism.Strawman);
    	}*/
    	
    	// update expr
    	System.out.println("Incremental:");
    	for (int mInitial = 10, rInitial = 10, mUpdate = 1; mInitial < 110; mInitial += 10, rInitial += 10) {
    	    	runParallelCompositionExprUpdate(mInitial, rInitial, PolicyUpdateMechanism.Incremental, mUpdate);
    	}
    	
    	System.out.println("Strawman:");
    	for (int mInitial = 10, rInitial = 10, mUpdate = 1; mInitial < 110; mInitial += 10, rInitial += 10) {
    	    	runParallelCompositionExprUpdate(mInitial, rInitial, PolicyUpdateMechanism.Strawman, mUpdate);
    	}
    	
    }
    
    private void runParallelCompositionExprBootstrap(int mInitial, int rInitial, PolicyUpdateMechanism updateMechanism) {
    	PolicyTree leftTree = new PolicyTree();
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree();
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Parallel;
    	policyTree.leftChild = leftTree;
    	policyTree.rightChild = rightTree;
    	
    	// initialize M
		List<OFFlowMod> mRules = new ArrayList<OFFlowMod>();
		mRules.add(RuleGenerationUtil.generateDefaultRule());
    	for (int i = 0; i < mInitial; i++) {
    		mRules.add(RuleGenerationUtil.generateMonotoringRule());
    	}
    	
    	List<OFFlowMod> rRules = new ArrayList<OFFlowMod>();
    	rRules.add(RuleGenerationUtil.generateDefaultRule());
    	for (int i = 0; i < rInitial; i++) {
    		rRules.add(RuleGenerationUtil.generateRoutingRule());
    	}
    	
    	// add rules to policy
    	PolicyTree.UPDATEMECHANISM = updateMechanism;
    	//log.error("begin experiment m:{} r:{} update:{}", mInitial, rInitial, updateMechanism);
    	long startTime = System.nanoTime();
    	for (OFFlowMod fm : mRules) {
    		policyTree.update(fm, 1);
    	}
    	for (OFFlowMod fm : rRules) {
    		policyTree.update(fm, 2);
    	}
    	long elapseTime = System.nanoTime() - startTime;
    	//log.error("elapse time:{}", elapseTime / (1e9));
    	System.out.println(elapseTime / (1e9));
    }
    
    private void runParallelCompositionExprUpdate(int mInitial, int rInitial,
    		PolicyUpdateMechanism updateMechanism, int mUpdate) {
    	PolicyTree leftTree = new PolicyTree();
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree();
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Parallel;
    	policyTree.leftChild = leftTree;
    	policyTree.rightChild = rightTree;
    	
    	// initialize M
		List<OFFlowMod> mRules = new ArrayList<OFFlowMod>();
		mRules.add(RuleGenerationUtil.generateDefaultRule());
    	for (int i = 0; i < mInitial; i++) {
    		mRules.add(RuleGenerationUtil.generateMonotoringRule());
    	}
    	
    	List<OFFlowMod> rRules = new ArrayList<OFFlowMod>();
    	rRules.add(RuleGenerationUtil.generateDefaultRule());
    	for (int i = 0; i < rInitial; i++) {
    		rRules.add(RuleGenerationUtil.generateRoutingRule());
    	}
    	
    	PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
    	for (OFFlowMod fm : mRules) {
    		policyTree.update(fm, 1);
    	}
    	for (OFFlowMod fm : rRules) {
    		policyTree.update(fm, 2);
    	}
    	
    	// generate update
    	List<OFFlowMod> mUpdateRules = new ArrayList<OFFlowMod>();
    	if (mUpdate == 1) {
    		{
				OFFlowMod fmDelete = null;
				try {
					fmDelete = mRules.get(1).clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				fmDelete.setCommand(OFFlowMod.OFPFC_DELETE);
				mUpdateRules.add(fmDelete);
			}
    		//mUpdateRules.add(generateMonotoringRule());
		} else {
			for (int i = 0; i < mUpdate / 2; i++) {
				OFFlowMod fmDelete = null;
				try {
					fmDelete = mRules.get(i+1).clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				fmDelete.setCommand(OFFlowMod.OFPFC_DELETE);
				mUpdateRules.add(fmDelete);
			}
			for (int i = 0; i < mUpdate / 2; i++) {
				mUpdateRules.add(RuleGenerationUtil.generateMonotoringRule());
			}
			Collections.shuffle(mUpdateRules);
		}
    	
    	// add rules to policy
    	PolicyTree.UPDATEMECHANISM = updateMechanism;
    	int totalFlowMods = 0;
    	long startTime = System.nanoTime();
    	for (OFFlowMod fm : mUpdateRules) {
    		PolicyUpdateTable updateTable = policyTree.update(fm, 1);
    		totalFlowMods += updateTable.addFlowMods.size();
    		totalFlowMods += updateTable.deleteFlowMods.size();
    		//log.error("{} {} {}", fm.getCommand(), updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
    	}
    	long elapseTime = System.nanoTime() - startTime;
    	System.out.println(elapseTime / (1e9) + "\t" + totalFlowMods);
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
