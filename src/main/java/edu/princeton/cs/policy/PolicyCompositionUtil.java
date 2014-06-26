package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.List;

import net.onrc.openvirtex.util.MACAddress;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;

public class PolicyCompositionUtil {
	
	public static short SEQUENTIAL_MAX = 8;

	public static OFFlowMod parallelComposition(OFFlowMod fm1, OFFlowMod fm2) {
		
		OFFlowMod composedFm = new OFFlowMod();
		composedFm.setCommand(OFFlowMod.OFPFC_ADD);
		composedFm.setIdleTimeout((short)0); // 0 means permanent
		composedFm.setHardTimeout((short)0); // 0 means permanent
		composedFm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		composedFm.setCookie(0);
		
		// priority
		composedFm.setPriority((short) (fm1.getPriority() + fm2.getPriority()));
		
		// match
		OFMatch match = intersectMatch(fm1.getMatch(), fm2.getMatch());
		if (match != null) {
			composedFm.setMatch(match);
		}
		else {
			return null;
		}
		
		// action
		List<OFAction> actions = new ArrayList<OFAction>();
		int length = OFFlowMod.MINIMUM_LENGTH;
		for (OFAction action : fm1.getActions()) {
			actions.add(action);
			length += action.getLengthU();
		}
		for (OFAction action : fm2.getActions()) {
			actions.add(action);
			length += action.getLengthU();
		}
		composedFm.setActions(actions);
		composedFm.setLengthU(length);
		
		return composedFm;
	}
	
	public static OFFlowMod sequentialComposition(OFFlowMod fm1, OFFlowMod fm2) {
		
		// check fm1 actions, if fwd or drop, stop
		boolean flag = false;
		if (fm1.getActions().isEmpty()) {
			flag = true;
		}
		for (OFAction action : fm1.getActions()) {
			if (action instanceof OFActionOutput) {
				flag = true;
				break;
			}
		}
		if (flag) {
			OFFlowMod composedFm = null;
			try {
				composedFm = fm1.clone();
				composedFm.setPriority(
						(short) (fm1.getPriority() * PolicyCompositionUtil.SEQUENTIAL_MAX));
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return composedFm;
		}
		
		// compose logic
		OFFlowMod composedFm = new OFFlowMod();
		composedFm.setCommand(OFFlowMod.OFPFC_ADD);
		composedFm.setIdleTimeout((short)0);
		composedFm.setHardTimeout((short)0);
		composedFm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		composedFm.setCookie(0);
		
		// priority
		composedFm.setPriority(
				(short) (fm1.getPriority() * PolicyCompositionUtil.SEQUENTIAL_MAX + fm2.getPriority()));
		
		// match
		OFMatch match = intersectMatch(fm1.getMatch(), actRevertMatch(fm2.getMatch(), fm1.getActions()));
		if (match != null) {
			composedFm.setMatch(match);
		}
		else {
			return null;
		}
		
		// action
		List<OFAction> actions = new ArrayList<OFAction>();
		int length = OFFlowMod.MINIMUM_LENGTH;
		for (OFAction action : fm1.getActions()) {
			actions.add(action);
			length += action.getLengthU();
		}
		for (OFAction action : fm2.getActions()) {
			actions.add(action);
			length += action.getLengthU();
		}
		composedFm.setActions(actions);
		composedFm.setLengthU(length);
		
		return composedFm;
	}
	
	private static OFMatch actRevertMatch(OFMatch match, List<OFAction> actions) {
		OFMatch m = match.clone();
		for (OFAction action : actions) {
			if (action instanceof OFActionNetworkLayerDestination) {
				OFActionNetworkLayerDestination modNwDst = (OFActionNetworkLayerDestination) action;
				int mask = m.getWildcards() & OFMatch.OFPFW_NW_DST_MASK;
				int shift = Math.min(mask >> OFMatch.OFPFW_NW_DST_SHIFT, 32);
				int ip1 = (m.getNetworkDestination() >> shift) << shift;
				int ip2 = (modNwDst.getNetworkAddress() >> shift) << shift;
				if (shift == 32 || ip1 == ip2) {
					m.setWildcards(m.getWildcards() | OFMatch.OFPFW_NW_DST_ALL);
					m.setNetworkDestination(0);
				}
				else {
					return null;
				}
			}
		}
		return m;
	}

	private static OFMatch intersectMatch (OFMatch m1, OFMatch m2) {
		if (m1 == null || m2 == null) {
			return null;
		}
		
		OFMatch match = new OFMatch();
		int wcard1 = m1.getWildcards();
		int wcard2 = m2.getWildcards();
		
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_IN_PORT,
				m1.getInputPort(), m2.getInputPort())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_DL_VLAN,
				m1.getDataLayerVirtualLan(), m2.getDataLayerVirtualLan())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_DL_SRC,
				m1.getDataLayerSource(), m2.getDataLayerSource())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_DL_DST,
				m1.getDataLayerDestination(), m2.getDataLayerDestination())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_DL_TYPE,
				m1.getDataLayerType(), m2.getDataLayerType())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_NW_PROTO,
				m1.getNetworkProtocol(), m2.getNetworkProtocol())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_TP_SRC,
				m1.getTransportSource(), m2.getTransportSource())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_TP_DST,
				m1.getTransportDestination(), m2.getTransportDestination())) {
			return null;
		}
		
		{
			int mask1 = wcard1 & OFMatch.OFPFW_NW_SRC_MASK;
			int mask2 = wcard2 & OFMatch.OFPFW_NW_SRC_MASK;
			int shift = Math.min(Math.max(mask1, mask2) >> OFMatch.OFPFW_NW_SRC_SHIFT, 32);
			int ip1 = (m1.getNetworkSource() >> shift) << shift;
			int ip2 = (m2.getNetworkSource() >> shift) << shift;
			if (shift == 32 || ip1 == ip2) {
				int wcard = match.getWildcards() & (Math.min(mask1, mask2) | ~OFMatch.OFPFW_NW_SRC_MASK);
				match.setWildcards(wcard);
				
				int ip = mask1 <= mask2 ? m1.getNetworkSource() : m2.getNetworkSource();
				setMatchField(match, OFMatch.OFPFW_NW_SRC_ALL, ip);
			}
			else {
				return null;
			}
		}
		
		{
			int mask1 = wcard1 & OFMatch.OFPFW_NW_DST_MASK;
			int mask2 = wcard2 & OFMatch.OFPFW_NW_DST_MASK;
			int shift = Math.min(Math.max(mask1, mask2) >> OFMatch.OFPFW_NW_DST_SHIFT, 32);
			int ip1 = (m1.getNetworkSource() >> shift) << shift;
			int ip2 = (m2.getNetworkSource() >> shift) << shift;
			if (shift == 32 || ip1 == ip2) {
				int wcard = match.getWildcards() & (Math.min(mask1, mask2) | ~OFMatch.OFPFW_NW_DST_MASK);
				match.setWildcards(wcard);
				
				int ip = mask1 <= mask2 ? m1.getNetworkDestination() : m2.getNetworkDestination();
				setMatchField(match, OFMatch.OFPFW_NW_DST_ALL, ip);
			}
			else {
				return null;
			}
		}
		
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_DL_VLAN_PCP,
				m1.getDataLayerVirtualLanPriorityCodePoint(), m2.getDataLayerVirtualLanPriorityCodePoint())) {
			return null;
		}
		if (!intersectMatchField(match, wcard1, wcard2, OFMatch.OFPFW_NW_TOS,
				m1.getNetworkTypeOfService(), m2.getNetworkTypeOfService())) {
			return null;
		}
		
		return match;
	}
	
	// deal with Number
	private static boolean intersectMatchField (OFMatch match, int wcard1, int wcard2, int field,
			Number val1, Number val2) {
		int wcard = match.getWildcards();
		if ((wcard1 & field) == 0 && (wcard2 & field) == 0) {
			wcard = wcard & (~field);
			match.setWildcards(wcard);
			if (val1.equals(val2)) {
				setMatchField(match, field, val1);
				return true;
			}
			else {
				return false;
			}
		}
		else if ((wcard1 & field) == 0 && (wcard2 & field) != 0) {
			wcard = wcard & (~field);
			match.setWildcards(wcard);
			setMatchField(match, field, val1);
			return true;
		}
		else if ((wcard1 & field) != 0 && (wcard2 & field) == 0) {
			wcard = wcard & (~field);
			match.setWildcards(wcard);
			setMatchField(match, field, val2);
			return true;
		}
		else if ((wcard1 & field) != 0 && (wcard2 & field) != 0) {
			return true;
		}
		
		return false;
	}
	
	// deal with Number
	private static boolean intersectMatchField(OFMatch match, int wcard1,
			int wcard2, int field, byte[] val1, byte[] val2) {
		int wcard = match.getWildcards();
		if ((wcard1 & field) == 0 && (wcard2 & field) == 0) {
			wcard = wcard & (~field);
			for (int i = 0; i < MACAddress.MAC_ADDRESS_LENGTH; i++) {
				if (val1[i] != val2[i]) {
					return false;
				}
			}
			setMatchField(match, field, val1);
			return true;
		} else if ((wcard1 & field) == 0 && (wcard2 & field) != 0) {
			wcard = wcard & (~field);
			setMatchField(match, field, val1);
			return true;
		} else if ((wcard1 & field) != 0 && (wcard2 & field) == 0) {
			wcard = wcard & (~field);
			setMatchField(match, field, val2);
			return true;
		} else if ((wcard1 & field) != 0 && (wcard2 & field) != 0) {
			return true;
		}

		return false;
	}
	
	private static void setMatchField (OFMatch match, int field, Number val) {
		switch (field) {
		case OFMatch.OFPFW_IN_PORT:
			match.setInputPort((Short) val);
			break;
		case OFMatch.OFPFW_DL_VLAN:
			match.setDataLayerVirtualLan((Short) val);
			break;
		case OFMatch.OFPFW_DL_TYPE:
			match.setDataLayerType((Short) val);
			break;
		case OFMatch.OFPFW_NW_PROTO:
			match.setNetworkProtocol((Byte) val);
			break;
		case OFMatch.OFPFW_TP_SRC:
			match.setTransportSource((Byte) val);
			break;
		case OFMatch.OFPFW_TP_DST:
			match.setTransportDestination((Byte) val);
			break;
		case OFMatch.OFPFW_NW_SRC_ALL:
			match.setNetworkSource((Integer) val);
			break;
		case OFMatch.OFPFW_NW_DST_ALL:
			match.setNetworkDestination((Integer) val);
			break;
		case OFMatch.OFPFW_DL_VLAN_PCP:
			match.setDataLayerVirtualLanPriorityCodePoint((Byte) val);
			break;
		case OFMatch.OFPFW_NW_TOS:
			match.setNetworkTypeOfService((Byte) val);
			break;
		default:
			break;
		}
	}
	
	private static void setMatchField (OFMatch match, int field, byte[] val) {
		switch (field) {
		case OFMatch.OFPFW_DL_SRC:
			match.setDataLayerSource(val);
			break;
		case OFMatch.OFPFW_DL_DST:
			match.setDataLayerDestination(val);
			break;
		default:
			break;
		}
	}
	
}
