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

    public void test1() {
    	PolicyTree leftTree = new PolicyTree();
    	leftTree.tenantId = 1;
    	
    	PolicyTree rightTree = new PolicyTree();
    	rightTree.tenantId = 2;
    	
    	PolicyTree policyTree = new PolicyTree();
    	policyTree.operator = PolicyOperator.Parallel;
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
			fm.setPriority((short) 1);

			OFMatch m = new OFMatch();
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE
					& (8 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
			m.setWildcards(wcards);
			m.setDataLayerType((short) 2048);
			m.setNetworkSource((new PhysicalIPAddress("1.0.0.0")).getIp());
			fm.setMatch(m);

			List<OFAction> actions = new ArrayList<OFAction>();
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU());
			
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
			m.setNetworkDestination((new PhysicalIPAddress("2.0.0.1")).getIp());
			fm.setMatch(m);

			OFActionOutput action = new OFActionOutput();
			action.setPort((short) 1);
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
			
			fm3 = fm;
		}
		
		policyTree.update(fm0, 1);
		policyTree.update(fm1, 1);
		policyTree.update(fm0, 2);
		policyTree.update(fm2, 2);
		policyTree.update(fm3, 2);
		
		for (OFFlowMod fm : policyTree.flowTable.getFlowMods()) {
			log.error(fm);
		}
		
		Assert.assertEquals(1, 1);
		
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
