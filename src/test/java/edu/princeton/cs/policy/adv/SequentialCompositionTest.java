package edu.princeton.cs.policy.adv;

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

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SequentialCompositionTest extends TestCase {

	private static Logger log = LogManager.getLogger(SequentialCompositionTest.class.getName());

    public SequentialCompositionTest(final String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(SequentialCompositionTest.class);
    }
    
    public void test1() {
    	
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
			fm.setPriority((short) 25);

			OFMatch m = new OFMatch();
			int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE
					& (30 << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK)
					& ~OFMatch.OFPFW_NW_DST_MASK;
			m.setWildcards(wcards);
			m.setDataLayerType((short) 2048);
			m.setNetworkSource((new PhysicalIPAddress("0.0.0.0")).getIp());
			m.setNetworkDestination((new PhysicalIPAddress("3.0.0.0")).getIp());
			fm.setMatch(m);

			OFActionNetworkLayerDestination action1 = new OFActionNetworkLayerDestination();
			action1.setNetworkAddress((new PhysicalIPAddress("2.0.0.1")).getIp());
			OFActionOutput action2 = new OFActionOutput();
			action2.setPort((short) 1);
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(action1);
			actions.add(action2);
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU() + action1.getLengthU() + action2.getLengthU());
			
			fm3 = fm;
		}
		
		OFFlowMod composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
    	
    	log.error("test1");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1>>2: {}", composedFm);
    	Assert.assertEquals(composedFm, fm3);
    }
    
    public void test2() {
    	
    	OFFlowMod fm1 = new OFFlowMod();
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
			fm.setPriority((short) 0);

			OFMatch m = new OFMatch();
			fm.setMatch(m);

			List<OFAction> actions = new ArrayList<OFAction>();
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU());
			
			fm3 = fm;
		}
		
		OFFlowMod composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
    	
    	log.error("test2");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1>>2: {}", composedFm);
    	Assert.assertEquals(composedFm, fm3);
    }
    
    public void test3() {
    	
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
			fm.setPriority((short) 0);

			OFMatch m = new OFMatch();
			fm.setMatch(m);

			List<OFAction> actions = new ArrayList<OFAction>();
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU());
			
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
			fm.setPriority((short) 24);

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
			
			fm3 = fm;
		}
		
		OFFlowMod composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
    	
    	log.error("test3");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1>>2: {}", composedFm);
    	Assert.assertEquals(composedFm, fm3);
    }
    
    public void test4() {
    	
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
			m.setNetworkDestination((new PhysicalIPAddress("2.0.0.2")).getIp());
			fm.setMatch(m);

			OFActionOutput action = new OFActionOutput();
			action.setPort((short) 1);
			List<OFAction> actions = new ArrayList<OFAction>();
			actions.add(action);
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU() + action.getLengthU());
			
			fm2 = fm;
		}
		
		OFFlowMod fm3 = null;
		
		OFFlowMod composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
    	
    	log.error("test4");
    	log.error("fm1:   {}", fm1);
    	log.error("fm2:   {}", fm2);
    	log.error("fm3:   {}", fm3);
    	log.error("fm1>>2: {}", composedFm);
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
