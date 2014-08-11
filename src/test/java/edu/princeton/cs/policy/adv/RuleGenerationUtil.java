package edu.princeton.cs.policy.adv;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.onrc.openvirtex.elements.address.PhysicalIPAddress;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;

public class RuleGenerationUtil {
	
	private static Random rand = new Random(1);
	
	public static OFFlowMod generateDefaultRule() {
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
	
	public static OFFlowMod generateMonitorSrcDstMacRule() {
		return generateMonitorSrcDstMacRule(getRandomNumber(1, 60000), getRandomMac(), getRandomMac(), OFFlowMod.OFPFC_ADD);
	}
	
	public static OFFlowMod generateMonitorSrcDstMacRule(int priority, String srcMac, String dstMac, short command) {
    	
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(command);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) priority);

		OFMatch m = new OFMatch();
		int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_SRC & ~OFMatch.OFPFW_DL_DST;
		m.setWildcards(wcards);
		m.setDataLayerSource(srcMac);
		m.setDataLayerDestination(dstMac);
		fm.setMatch(m);

		List<OFAction> actions = new ArrayList<OFAction>();
		fm.setActions(actions);
		fm.setLengthU(fm.getLengthU());
		
		return fm;
    }
	
	public static OFFlowMod generateRouterDstMacRule() {
    	return generateRouterDstMacRule(1, getRandomMac(), getRandomNumber(0, 48), OFFlowMod.OFPFC_ADD);
    }
    
	public static OFFlowMod generateRouterDstMacRule(int priority, String dstMac, int outPort, short command) {
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(command);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) priority);

		OFMatch m = new OFMatch();
		int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_DST;
		m.setWildcards(wcards);
		m.setDataLayerDestination(dstMac);
		fm.setMatch(m);

		OFActionOutput action = new OFActionOutput();
		action.setPort((short) outPort);
		List<OFAction> actions = new ArrayList<OFAction>();
		actions.add(action);
		fm.setActions(actions);
		fm.setLengthU(fm.getLengthU() + action.getLengthU());
		
		return fm;
    }
    
	public static OFFlowMod generateMonitoringRule() {
    	String srcIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	return generateMonitoringRule(getRandomNumber(1, 60000), srcIp, getRandomNumber(0, 32), OFFlowMod.OFPFC_ADD);
    }
    
	public static OFFlowMod generateMonitoringRule(int priority, String srcIp, int srcPrefix, short command) {
    	
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
	
	public static OFFlowMod generateFWRule() {
    	String dstIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	return generateFWRule(getRandomNumber(1, 60000), dstIp, getRandomNumber(0, 32), OFFlowMod.OFPFC_ADD);
    }
    
	public static OFFlowMod generateFWRule(int priority, String dstIp, int dstPrefix, short command) {
    	
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(command);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) priority);

		OFMatch m = new OFMatch();
		int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_DL_TYPE
				& ((32 - dstPrefix) << OFMatch.OFPFW_NW_DST_SHIFT | ~OFMatch.OFPFW_NW_DST_MASK)
				& ((24 << OFMatch.OFPFW_NW_SRC_SHIFT) | ~OFMatch.OFPFW_NW_SRC_MASK)
				& ~OFMatch.OFPFW_TP_SRC
				& ~OFMatch.OFPFW_TP_DST
				& ~OFMatch.OFPFW_NW_PROTO;
		m.setWildcards(wcards);
		m.setDataLayerType((short) 2048);
		m.setNetworkDestination((new PhysicalIPAddress(dstIp)).getIp());
		m.setNetworkSource((new PhysicalIPAddress("196.1.0.0")).getIp());
		m.setTransportSource((short) 1521);
		m.setTransportDestination((short) 1237);
		m.setNetworkProtocol((byte) 6);
		fm.setMatch(m);

		List<OFAction> actions = new ArrayList<OFAction>();
		if (priority % 2 == 0) {
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU());
		} else {
			OFActionOutput action = new OFActionOutput();
			action.setPort((short) 1);
			actions.add(action);
			fm.setActions(actions);
			fm.setLengthU(fm.getLengthU() + action.getLengthU());
		}
		
		return fm;
    }
    
	public static OFFlowMod generateRoutingRule() {
    	String dstIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	return generateRoutingRule(1, dstIp, getRandomNumber(0, 48), OFFlowMod.OFPFC_ADD);
    }
    
	public static OFFlowMod generateRoutingRule(int priority, String dstIp, int outPort, short command) {
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
	
	public static OFFlowMod generatePrefixRoutingRule(int priority, String dstIp, int prefix, int outPort, short command) {
    	OFFlowMod fm = new OFFlowMod();
		fm.setCommand(command);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		fm.setPriority((short) priority);

		OFMatch m = new OFMatch();
		int wcards = OFMatch.OFPFW_ALL
				& ~OFMatch.OFPFW_DL_TYPE
				& ((32 - prefix) << OFMatch.OFPFW_NW_DST_SHIFT | ~OFMatch.OFPFW_NW_DST_MASK);
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
    
	public static OFFlowMod generateLBRule() {
    	String srcIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	String dstIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	String setDstIp = String.format("%d.%d.%d.%d", getRandomNumber(0, 256), getRandomNumber(0, 256),
    			getRandomNumber(0, 256), getRandomNumber(0, 256));
    	return generateLBRule(getRandomNumber(1, 60000), srcIp, getRandomNumber(0, 32),
    			dstIp, setDstIp, OFFlowMod.OFPFC_ADD);
    }
    
	public static OFFlowMod generateLBRule(int priority, String srcIp, int srcPrefix, String dstIp, String setDstIp, short command) {
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
	public static int getRandomNumber(int min, int max) {
    	return rand.nextInt(max - min) + min;
    }
	
	// get random mac, e.g., "00:17:42:EF:CD:8D"
	public static String getRandomMac() {
		String mac = String.format("%s:%s:%s:%s:%s:%s", getRandomNumber(0, 255), getRandomNumber(0, 255),
				getRandomNumber(0, 255), getRandomNumber(0, 255), getRandomNumber(0, 255), getRandomNumber(0, 255));
		return mac;
	}

}
