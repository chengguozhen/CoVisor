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
		policyTree.update(generateDefaultRule(), 1);
		policyTree.update(generateMonotoringRule(1, "1.0.0.0", 24, OFFlowMod.OFPFC_ADD), 1);
		
		// routing policy
		policyTree.update(generateDefaultRule(), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("********************************************************************************");
		log.error("policy tree test 1: parallel composition M + R");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateMonotoringRule(1, "3.0.0.0", 10, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 1: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(generateMonotoringRule(1, "1.0.0.0", 24, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 1: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(generateMonotoringRule(1, "1.0.0.0", 24, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 1: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(generateMonotoringRule(1, "2.0.0.0", 20, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateMonotoringRule(1, "3.0.0.0", 10, OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_DELETE), 2);

		log.error("policy tree test 1: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// update flow mods
		PolicyUpdateTable updateTable = null;
		updateTable = policyTree.update(generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
		
		updateTable = policyTree.update(generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_DELETE), 2);
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
		policyTree.update(generateDefaultRule(), 1);
		policyTree.update(generateLBRule(3, "0.0.0.0", 2, "3.0.0.0", "2.0.0.1", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateLBRule(1, "0.0.0.0", 0, "3.0.0.0", "2.0.0.2", OFFlowMod.OFPFC_ADD), 1);
		
		// routing policy
		policyTree.update(generateDefaultRule(), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("********************************************************************************");
		log.error("policy tree test 2: sequential composition LB >> R");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(generateLBRule(4, "1.0.0.0", 10, "3.0.0.0", "2.0.0.4", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 2: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(generateLBRule(3, "0.0.0.0", 2, "3.0.0.0", "2.0.0.1", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateLBRule(1, "0.0.0.0", 0, "3.0.0.0", "2.0.0.2", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 2: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// add
		policyTree.update(generateLBRule(3, "0.0.0.0", 2, "3.0.0.0", "2.0.0.1", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateLBRule(1, "0.0.0.0", 0, "3.0.0.0", "2.0.0.2", OFFlowMod.OFPFC_ADD), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.1", 1, OFFlowMod.OFPFC_ADD), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.2", 2, OFFlowMod.OFPFC_ADD), 2);
		
		log.error("policy tree test 2: add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// delete
		policyTree.update(generateLBRule(4, "1.0.0.0", 10, "3.0.0.0", "2.0.0.4", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_DELETE), 1);
		policyTree.update(generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_DELETE), 2);
		policyTree.update(generateRoutingRule(1, "2.0.0.4", 4, OFFlowMod.OFPFC_DELETE), 2);
		
		log.error("policy tree test 2: delete");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		
		// update flow mods
		PolicyUpdateTable updateTable = null;
		policyTree.update(generateRoutingRule(1, "2.0.0.3", 3, OFFlowMod.OFPFC_ADD), 2);
		log.error("policy tree test 2: pre add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		updateTable = policyTree.update(generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD), 1);
		log.error("policy tree test 2: after add");
		for (OFFlowMod fm : policyTree.flowTable.getFlowModsSorted()) {
			log.error(fm);
		}
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());
		
		updateTable = policyTree.update(generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_DELETE), 1);
		log.error("add {}, delete {}", updateTable.addFlowMods.size(), updateTable.deleteFlowMods.size());		
    }
    
    // experiment: M + R
    public void test3() {
    	
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
		mRules.add(generateDefaultRule());
    	for (int i = 0; i < mInitial; i++) {
    		mRules.add(generateMonotoringRule());
    	}
    	
    	List<OFFlowMod> rRules = new ArrayList<OFFlowMod>();
    	rRules.add(generateDefaultRule());
    	for (int i = 0; i < rInitial; i++) {
    		rRules.add(generateRoutingRule());
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
		mRules.add(generateDefaultRule());
    	for (int i = 0; i < mInitial; i++) {
    		mRules.add(generateMonotoringRule());
    	}
    	
    	List<OFFlowMod> rRules = new ArrayList<OFFlowMod>();
    	rRules.add(generateDefaultRule());
    	for (int i = 0; i < rInitial; i++) {
    		rRules.add(generateRoutingRule());
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
				mUpdateRules.add(generateMonotoringRule());
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
    
    private OFFlowMod generateDefaultRule() {
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(OFFlowMod.OFPFC_ADD);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) 0);

		OFMatch m = new OFMatch();
		fm.setMatch(m);

		List<OFAction> actions = new ArrayList<OFAction>();
		fm.setActions(actions);
		fm.setLengthU(fm.getLengthU());
		
		return fm;
    }
    
    private OFFlowMod generateMonotoringRule() {
    	String srcIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	return generateMonotoringRule(getRandomNumber(1, 60000), srcIp, getRandomNumber(0, 32), OFFlowMod.OFPFC_ADD);
    }
    
    private OFFlowMod generateMonotoringRule(int priority, String srcIp, int srcPrefix, short command) {
    	
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(command);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) priority);

		OFMatch m = new OFMatch();
		int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE
				& ((32 - srcPrefix) << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
		m.setWildcards(wcards);
		m.setDataLayerType((short) 2048);
		m.setNetworkSource((new PhysicalIPAddress(srcIp)).getIp());
		fm.setMatch(m);

		List<OFAction> actions = new ArrayList<OFAction>();
		fm.setActions(actions);
		fm.setLengthU(fm.getLengthU());
		
		return fm;
    }
    
    private OFFlowMod generateRoutingRule() {
    	String dstIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	return generateRoutingRule(1, dstIp, getRandomNumber(0, 48), OFFlowMod.OFPFC_ADD);
    }
    
    private OFFlowMod generateRoutingRule(int priority, String dstIp, int outPort, short command) {
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(command);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) priority);

		OFMatch m = new OFMatch();
		int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE & ~OFMatch.OFPFW_NW_DST_MASK;
		m.setWildcards(wcards);
		m.setDataLayerType((short) 2048);
		m.setNetworkDestination((new PhysicalIPAddress(dstIp)).getIp());
		fm.setMatch(m);

		OFActionOutput action = new OFActionOutput();
		action.setPort((short) outPort);
		List<OFAction> actions = new ArrayList<OFAction>();
		actions.add(action);
		fm.setActions(actions);
		fm.setLengthU(fm.getLengthU() + action.getLengthU());
		
		return fm;
    }
    
    private OFFlowMod generateLBRule() {
    	String srcIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	String dstIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	String setDstIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	return generateLBRule(getRandomNumber(1, 60000), srcIp, getRandomNumber(0, 32),
    			dstIp, setDstIp, OFFlowMod.OFPFC_ADD);
    }
    
    private OFFlowMod generateLBRule(int priority, String srcIp, int srcPrefix, String dstIp, String setDstIp, short command) {
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(command);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) priority);

		OFMatch m = new OFMatch();
		if (srcPrefix == 0) {
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE & ~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
			m.setNetworkSource((new PhysicalIPAddress(srcIp)).getIp());
		} else {
			int wcards = OFMatch.OFPFW_ALL
					& ~OFMatch.OFPFW_DL_TYPE
					& ((32 - srcPrefix) << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK)
					& ~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
		}
		m.setDataLayerType((short) 2048);
		m.setNetworkDestination((new PhysicalIPAddress(dstIp)).getIp());
		fm.setMatch(m);

		OFActionNetworkLayerDestination action = new OFActionNetworkLayerDestination();
		action.setNetworkAddress((new PhysicalIPAddress(setDstIp)).getIp());
		List<OFAction> actions = new ArrayList<OFAction>();
		actions.add(action);
		fm.setActions(actions);
		fm.setLengthU(fm.getLengthU() + action.getLengthU());
		
		return fm;
    	
    }
    
    // get random number in [min, max)
    private int getRandomNumber(int min, int max) {
    	return rand.nextInt(max - min) + min;
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
