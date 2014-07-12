package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.List;

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
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PolicyTreeTest extends TestCase {

	private static Logger log = LogManager.getLogger(PolicyTreeTest.class.getName());
	
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
    public void test2() {
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
		{
		OFFlowMod fm = generateLBRule(2, "0.0.0.0", 1, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD);
		fm = generateLBRule(2, "0.0.0.0", 2, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD);
		fm = generateLBRule(2, "0.0.0.0", 3, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD);
		fm = generateLBRule(2, "0.0.0.0", 0, "3.0.0.0", "2.0.0.3", OFFlowMod.OFPFC_ADD);
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
    
    /*public void test2() {
    	PolicyTree leftTree = new PolicyTree();
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree();
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Sequential;
    	policyTree.leftChild = leftTree;
    	policyTree.rightChild = rightTree;
    	
    	OFFlowMod fm0 = new OFFlowMod();
		{
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
			
			fm0 = fm;
		}
		
		OFFlowMod fm1 = new OFFlowMod();
    	{
			OFFlowMod fm = new OFFlowMod();
			fm.setCommand(OFFlowMod.OFPFC_ADD);
			fm.setIdleTimeout((short) 0);
			fm.setHardTimeout((short) 0);
			fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
			fm.setCookie(0);
			fm.setPriority((short) 3);

			OFMatch m = new OFMatch();
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE
					& (30 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK)
					& ~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
			m.setDataLayerType((short) 2048);
			m.setNetworkSource((new PhysicalIPAddress("0.0.0.0")).getIp());
			m.setNetworkDestination((new PhysicalIPAddress("3.0.0.0")).getIp());
			fm.setMatch(m);

			OFActionNetworkLayerDestination action = new OFActionNetworkLayerDestination();
			action.setNetworkAddress((new PhysicalIPAddress("2.0.0.1")).getIp());
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(action);
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU() + action.getLengthU());
			
			fm1 = fm;
		}
    	
    	OFFlowMod fm2 = new OFFlowMod();
    	{
			OFFlowMod fm = new OFFlowMod();
			fm.setCommand(OFFlowMod.OFPFC_ADD);
			fm.setIdleTimeout((short) 0);
			fm.setHardTimeout((short) 0);
			fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
			fm.setCookie(0);
			fm.setPriority((short) 1);

			OFMatch m = new OFMatch();
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE & ~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
			m.setDataLayerType((short) 2048);
			m.setNetworkDestination((new PhysicalIPAddress("3.0.0.0")).getIp());
			fm.setMatch(m);

			OFActionNetworkLayerDestination action = new OFActionNetworkLayerDestination();
			action.setNetworkAddress((new PhysicalIPAddress("2.0.0.2")).getIp());
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(action);
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU() + action.getLengthU());
			
			fm2 = fm;
		}
		
		OFFlowMod fm3 = new OFFlowMod();
		{
			OFFlowMod fm = new OFFlowMod();
			fm.setCommand(OFFlowMod.OFPFC_ADD);
			fm.setIdleTimeout((short) 0);
			fm.setHardTimeout((short) 0);
			fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
			fm.setCookie(0);
			fm.setPriority((short) 1);

			OFMatch m = new OFMatch();
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE & ~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
			m.setDataLayerType((short) 2048);
			m.setNetworkDestination((new PhysicalIPAddress("2.0.0.1")).getIp());
			fm.setMatch(m);

			OFActionOutput action = new OFActionOutput();
			action.setPort((short) 1);
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(action);
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU() + action.getLengthU());
			
			fm3 = fm;
		}
		
		OFFlowMod fm4 = new OFFlowMod();
		{
			OFFlowMod fm = new OFFlowMod();
			fm.setCommand(OFFlowMod.OFPFC_ADD);
			fm.setIdleTimeout((short) 0);
			fm.setHardTimeout((short) 0);
			fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
			fm.setCookie(0);
			fm.setPriority((short) 1);

			OFMatch m = new OFMatch();
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE &~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
			m.setDataLayerType((short) 2048);
			m.setNetworkDestination((new PhysicalIPAddress("2.0.0.2")).getIp());
			fm.setMatch(m);

			OFActionOutput action = new OFActionOutput();
			action.setPort((short) 2);
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(action);
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU() + action.getLengthU());
			
			fm4 = fm;
		}
		
		policyTree.update(fm0, 1);
		policyTree.update(fm1, 1);
		policyTree.update(fm2, 1);
		policyTree.update(fm0, 2);
		policyTree.update(fm3, 2);
		policyTree.update(fm4, 2);
		
		log.error("policy tree test 2: sequential composition LB >> R");
		for (OFFlowMod fm : policyTree.flowTable.getFlowMods()) {
			log.error(fm);
		}
		
		Assert.assertEquals(1, 1);
    }*/
    
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
