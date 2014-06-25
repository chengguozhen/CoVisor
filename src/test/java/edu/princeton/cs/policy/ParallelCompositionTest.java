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

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for policy.
 */
public class ParallelCompositionTest extends TestCase {

	private static Logger log = LogManager.getLogger(ParallelCompositionTest.class.getName());

    public ParallelCompositionTest(final String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(ParallelCompositionTest.class);
    }

    public void test1() {
    	
    	// flow mod 1
    	OFFlowMod fm1 = new OFFlowMod();
    	fm1.setCommand(OFFlowMod.OFPFC_ADD);
    	fm1.setIdleTimeout((short)0); // 0 means permanent
    	fm1.setHardTimeout((short)0); // 0 means permanent
    	fm1.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm1.setCookie(0);
    	fm1.setPriority((short) 1);
    	
    	OFMatch m1 = new OFMatch();
    	int wcards1 = OFMatch.OFPFW_ALL & (8 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
    	m1.setWildcards(wcards1);
    	m1.setNetworkSource((new PhysicalIPAddress("1.0.0.0")).getIp());
    	fm1.setMatch(m1);
    	
    	OFActionOutput action1 = new OFActionOutput();
    	action1.setPort((short) 1);
		List<OFAction> actions1 = new ArrayList<OFAction>();
		actions1.add(action1);
		fm1.setActions(actions1);
		fm1.setLengthU(fm1.getLengthU() + action1.getLengthU());
		
		// flow mod 2
    	OFFlowMod fm2 = new OFFlowMod();
    	fm2.setCommand(OFFlowMod.OFPFC_ADD);
    	fm2.setIdleTimeout((short)0); // 0 means permanent
    	fm2.setHardTimeout((short)0); // 0 means permanent
    	fm2.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm2.setCookie(0);
    	fm2.setPriority((short) 10);
    	
    	OFMatch m2 = new OFMatch();
    	int wcards2 = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_NW_DST_MASK;
    	m2.setWildcards(wcards2);
    	m2.setNetworkDestination((new PhysicalIPAddress("2.0.0.1")).getIp());
    	fm2.setMatch(m2);
    	
    	OFActionOutput action2 = new OFActionOutput();
    	action2.setPort((short) 2);
		List<OFAction> actions2 = new ArrayList<OFAction>();
		actions2.add(action2);
		fm2.setActions(actions2);
		fm2.setLengthU(fm2.getLengthU() + action2.getLengthU());
    	
    	// fm1 + fm2
    	OFFlowMod fm3 = new OFFlowMod();
    	fm3.setCommand(OFFlowMod.OFPFC_ADD);
    	fm3.setIdleTimeout((short)0); // 0 means permanent
    	fm3.setHardTimeout((short)0); // 0 means permanent
    	fm3.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm3.setCookie(0);
    	fm3.setPriority((short) 11);
    	
    	OFMatch m3 = new OFMatch();
    	int wcards3 = OFMatch.OFPFW_ALL & (8 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK)
    			& ~OFMatch.OFPFW_NW_DST_MASK;
    	m3.setWildcards(wcards3);
    	m3.setNetworkSource((new PhysicalIPAddress("1.0.0.0")).getIp());
    	m3.setNetworkDestination((new PhysicalIPAddress("2.0.0.1")).getIp());
    	fm3.setMatch(m3);
    	
    	OFActionOutput action3_0 = new OFActionOutput();
    	action3_0.setPort((short) 1);
    	OFActionOutput action3_1 = new OFActionOutput();
    	action3_1.setPort((short) 2);
		List<OFAction> actions3 = new ArrayList<OFAction>();
		actions3.add(action3_0);
		actions3.add(action3_1);
		fm3.setActions(actions3);
		fm3.setLengthU(fm3.getLengthU() + action3_0.getLengthU() + action3_1.getLengthU());
    	
    	// use the function to calculate
    	OFFlowMod composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
    	
    	log.error("test1");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1+2: {}", composedFm);
    	Assert.assertEquals(fm3.equals(composedFm), true);
    }
    
    public void test2() {
    	
    	// flow mod 1
    	OFFlowMod fm1 = new OFFlowMod();
    	fm1.setCommand(OFFlowMod.OFPFC_ADD);
    	fm1.setIdleTimeout((short)0); // 0 means permanent
    	fm1.setHardTimeout((short)0); // 0 means permanent
    	fm1.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm1.setCookie(0);
    	fm1.setPriority((short) 10);
    	
    	OFMatch m1 = new OFMatch();
    	int wcards1 = OFMatch.OFPFW_ALL & (8 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
    	m1.setWildcards(wcards1);
    	m1.setNetworkSource((new PhysicalIPAddress("1.0.0.0")).getIp());
    	fm1.setMatch(m1);
    	
    	OFActionOutput action1 = new OFActionOutput();
    	action1.setPort((short) 1);
		List<OFAction> actions1 = new ArrayList<OFAction>();
		actions1.add(action1);
		fm1.setActions(actions1);
		fm1.setLengthU(fm1.getLengthU() + action1.getLengthU());
		
		// flow mod 2
    	OFFlowMod fm2 = new OFFlowMod();
    	fm2.setCommand(OFFlowMod.OFPFC_ADD);
    	fm2.setIdleTimeout((short)0); // 0 means permanent
    	fm2.setHardTimeout((short)0); // 0 means permanent
    	fm2.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm2.setCookie(0);
    	fm2.setPriority((short) 1);
    	
    	OFMatch m2 = new OFMatch();
    	fm2.setMatch(m2);

		List<OFAction> actions2 = new ArrayList<OFAction>();
		fm2.setActions(actions2);
		fm2.setLengthU(fm2.getLengthU());
    	
    	// fm1 + fm2
		OFFlowMod fm3 = new OFFlowMod();
    	fm3.setCommand(OFFlowMod.OFPFC_ADD);
    	fm3.setIdleTimeout((short)0); // 0 means permanent
    	fm3.setHardTimeout((short)0); // 0 means permanent
    	fm3.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm3.setCookie(0);
    	fm3.setPriority((short) 11);
    	
    	OFMatch m3 = new OFMatch();
    	int wcards3 = OFMatch.OFPFW_ALL & (8 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
    	m3.setWildcards(wcards3);
    	m3.setNetworkSource((new PhysicalIPAddress("1.0.0.0")).getIp());
    	fm3.setMatch(m3);
    	
    	OFActionOutput action3 = new OFActionOutput();
    	action3.setPort((short) 1);
		List<OFAction> actions3 = new ArrayList<OFAction>();
		actions3.add(action3);
		fm3.setActions(actions3);
		fm3.setLengthU(fm3.getLengthU() + action3.getLengthU());
    	
    	// use the function to calculate
    	OFFlowMod composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
    	
    	log.error("test2");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1+2: {}", composedFm);
    	Assert.assertEquals(fm3.equals(composedFm), true);
    }
    
    public void test3() {
    	
    	// flow mod 1
    	OFFlowMod fm1 = new OFFlowMod();
    	fm1.setCommand(OFFlowMod.OFPFC_ADD);
    	fm1.setIdleTimeout((short)0); // 0 means permanent
    	fm1.setHardTimeout((short)0); // 0 means permanent
    	fm1.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm1.setCookie(0);
    	fm1.setPriority((short) 10);
    	
    	OFMatch m1 = new OFMatch();
    	fm1.setMatch(m1);
    	
		List<OFAction> actions1 = new ArrayList<OFAction>();
		fm1.setActions(actions1);
		fm1.setLengthU(fm1.getLengthU());
		
		// flow mod 2
    	OFFlowMod fm2 = new OFFlowMod();
    	fm2.setCommand(OFFlowMod.OFPFC_ADD);
    	fm2.setIdleTimeout((short)0); // 0 means permanent
    	fm2.setHardTimeout((short)0); // 0 means permanent
    	fm2.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm2.setCookie(0);
    	fm2.setPriority((short) 1);
    	
    	OFMatch m2 = new OFMatch();
    	fm2.setMatch(m2);

		List<OFAction> actions2 = new ArrayList<OFAction>();
		fm2.setActions(actions2);
		fm2.setLengthU(fm2.getLengthU());
    	
    	// fm1 + fm2
		OFFlowMod fm3 = new OFFlowMod();
    	fm3.setCommand(OFFlowMod.OFPFC_ADD);
    	fm3.setIdleTimeout((short)0); // 0 means permanent
    	fm3.setHardTimeout((short)0); // 0 means permanent
    	fm3.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm3.setCookie(0);
    	fm3.setPriority((short) 11);
    	
    	OFMatch m3 = new OFMatch();
    	fm3.setMatch(m3);
    	
		List<OFAction> actions3 = new ArrayList<OFAction>();
		fm3.setActions(actions3);
		fm3.setLengthU(fm3.getLengthU());
    	
    	// use the function to calculate
    	OFFlowMod composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
    	
    	log.error("test3");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1+2: {}", composedFm);
    	Assert.assertEquals(fm3.equals(composedFm), true);
    }
    
    public void test4() {
    	
    	// flow mod 1
    	OFFlowMod fm1 = new OFFlowMod();
    	fm1.setCommand(OFFlowMod.OFPFC_ADD);
    	fm1.setIdleTimeout((short)0); // 0 means permanent
    	fm1.setHardTimeout((short)0); // 0 means permanent
    	fm1.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm1.setCookie(0);
    	fm1.setPriority((short) 1);
    	
    	OFMatch m1 = new OFMatch();
    	int wcards1 = OFMatch.OFPFW_ALL & (8 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
    	m1.setWildcards(wcards1);
    	m1.setNetworkSource((new PhysicalIPAddress("1.0.0.0")).getIp());
    	fm1.setMatch(m1);
    	
    	OFActionOutput action1 = new OFActionOutput();
    	action1.setPort((short) 1);
		List<OFAction> actions1 = new ArrayList<OFAction>();
		actions1.add(action1);
		fm1.setActions(actions1);
		fm1.setLengthU(fm1.getLengthU() + action1.getLengthU());
		
		// flow mod 2
    	OFFlowMod fm2 = new OFFlowMod();
    	fm2.setCommand(OFFlowMod.OFPFC_ADD);
    	fm2.setIdleTimeout((short)0); // 0 means permanent
    	fm2.setHardTimeout((short)0); // 0 means permanent
    	fm2.setBufferId(OFPacketOut.BUFFER_ID_NONE);
    	fm2.setCookie(0);
    	fm2.setPriority((short) 10);
    	
    	OFMatch m2 = new OFMatch();
    	int wcards2 = OFMatch.OFPFW_ALL & (10 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
    	m2.setWildcards(wcards2);
    	m2.setNetworkSource((new PhysicalIPAddress("2.0.0.0")).getIp());
    	fm2.setMatch(m2);
    	
    	OFActionOutput action2 = new OFActionOutput();
    	action2.setPort((short) 2);
		List<OFAction> actions2 = new ArrayList<OFAction>();
		actions2.add(action2);
		fm2.setActions(actions2);
		fm2.setLengthU(fm2.getLengthU() + action2.getLengthU());
    	
    	// fm1 + fm2
    	OFFlowMod fm3 = null;
    	
    	// use the function to calculate
    	OFFlowMod composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
    	
    	log.error("test4");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1+2: {}", composedFm);
    	Assert.assertEquals(composedFm, fm3);

    }
    
    public void test5() {
    	
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
			fm.setPriority((short) 2);

			OFMatch m = new OFMatch();
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE
					& (8 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK)
					&~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
			m.setDataLayerType((short) 2048);
			m.setNetworkSource((new PhysicalIPAddress("1.0.0.0")).getIp());
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
		
		OFFlowMod composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
    	
    	log.error("test5");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1+2: {}", composedFm);
    	Assert.assertEquals(composedFm, fm3);
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
