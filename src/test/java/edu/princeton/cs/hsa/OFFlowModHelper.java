package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.onrc.openvirtex.elements.address.PhysicalIPAddress;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionDataLayerDestination;
import org.openflow.protocol.action.OFActionDataLayerSource;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.HexString;

public class OFFlowModHelper {
	
	public static Random rand = new Random(1);
	
	/* Example
	 * 
	 * OFFlowMod fm1 = OFFlowModHelper.genFlowMod("priority=1,ether-type=2048,src-ip=1.0.0.0,dst-ip=2.0.0.0,"
	 *	+ "src-mac=00:00:00:00:00:01,dst-mac=00:00:00:00:00:02,src-port=80,dst-port=1,protocol=10");
	 *
	 * OFFlowMod fm2 = OFFlowModHelper.genFlowMod("priority=1,ether-type=2048,src-ip=1.0.0.0/24,dst-ip=2.0.0.0/16,"
	 *	+ "src-mac=00:00:00:00:00:01,dst-mac=00:00:00:00:00:02,src-port=80,dst-port=1,protocol=10,"
	 *	+ "actions=output:1,actions=set-dst-ip:0.0.0.1");
	 */
	public static OFFlowMod genFlowMod (String str) {
		
		OFFlowMod fm = new OFFlowMod();
		fm.setCommand(OFFlowMod.OFPFC_ADD);
		fm.setIdleTimeout((short) 0);
		fm.setHardTimeout((short) 0);
		fm.setBufferId(OFPacketOut.BUFFER_ID_NONE);
		fm.setCookie(0);
		
		OFMatch m = new OFMatch();
		fm.setMatch(m);
		
		List<OFAction> actions = new ArrayList<OFAction>();
		fm.setActions(actions);
		
		int wcards = OFMatch.OFPFW_ALL;
		String[] parts = str.split(",");
		for (String part : parts) {
			
			String[] temp = part.split("=");
			String field = temp[0];
			String value = temp[1];
			
			if (field.equals("priority")) {
				fm.setPriority(Short.parseShort(value));
			} else if (field.equals("inport")) {
				wcards = wcards & ~OFMatch.OFPFW_IN_PORT;
				m.setInputPort(Short.parseShort(value));
			} else if (field.equals("ether-type")) {
				wcards = wcards & ~OFMatch.OFPFW_DL_TYPE;
				m.setDataLayerType(Short.parseShort(value));
			} else if (field.equals("src-mac")) {
				wcards = wcards & ~OFMatch.OFPFW_DL_SRC;
				m.setDataLayerSource(value);
			} else if (field.equals("dst-mac")) {
				wcards = wcards & ~OFMatch.OFPFW_DL_DST;
				m.setDataLayerDestination(value);
			} else if (field.equals("src-ip")) {
				String[] ipPrefix = value.split("/");
				if (ipPrefix.length == 2) {
					String ip = ipPrefix[0];
					int prefix = Integer.parseInt(ipPrefix[1]);
					wcards = wcards & ((32-prefix) << OFMatch.OFPFW_NW_SRC_SHIFT | ~OFMatch.OFPFW_NW_SRC_MASK);
					if (ip.contains(".")) {
						m.setNetworkSource((new PhysicalIPAddress(ip)).getIp());
					} else {
						m.setNetworkSource(Integer.parseInt(ip));
					}
				} else {
					wcards = wcards & ~OFMatch.OFPFW_NW_SRC_MASK;
					if (value.contains(".")) {
						m.setNetworkSource((new PhysicalIPAddress(value)).getIp());
					} else {
						m.setNetworkSource(Integer.parseInt(value));
					}
				}
			} else if (field.equals("dst-ip")) {
				String[] ipPrefix = value.split("/");
				if (ipPrefix.length == 2) {
					String ip = ipPrefix[0];
					int prefix = Integer.parseInt(ipPrefix[1]);
					wcards = wcards & ((32-prefix) << OFMatch.OFPFW_NW_DST_SHIFT | ~OFMatch.OFPFW_NW_DST_MASK);
					if (ip.contains(".")) {
						m.setNetworkDestination((new PhysicalIPAddress(ip)).getIp());
					} else {
						m.setNetworkDestination(Integer.parseInt(ip));
					}
				} else {
					wcards = wcards & ~OFMatch.OFPFW_NW_DST_MASK;
					if (value.contains(".")) {
						m.setNetworkDestination((new PhysicalIPAddress(value)).getIp());
					} else {
						m.setNetworkDestination(Integer.parseInt(value));
					}
				}
			} else if (field.equals("src-port")) {
				wcards = wcards & ~OFMatch.OFPFW_TP_SRC;
				m.setTransportSource(Short.parseShort(value));
			} else if (field.equals("dst-port")) {
				wcards = wcards & ~OFMatch.OFPFW_TP_DST;
				m.setTransportDestination(Short.parseShort(value));
			} else if (field.equals("protocol")) {
				wcards = wcards & ~OFMatch.OFPFW_NW_PROTO;
				m.setNetworkProtocol(Byte.parseByte(value));
			} else if (field.equals("actions")) {
				String actionType = value.split(":")[0];
				String actionValue = value.split(":")[1];
				if (actionType.equals("output")) {
					OFActionOutput action = new OFActionOutput();
					action.setPort(Short.parseShort(actionValue));
					actions.add(action);
				} else if (actionType.equals("set-dst-ip")) {
					OFActionNetworkLayerDestination action = new OFActionNetworkLayerDestination();
					action.setNetworkAddress((new PhysicalIPAddress(actionValue)).getIp());
					actions.add(action);
				} else if (actionType.equals("set-src-mac")) {
					OFActionDataLayerSource action = new OFActionDataLayerSource();
					action.setDataLayerAddress(HexString.fromHexString(value.substring(12)));
					actions.add(action);
				} else if (actionType.equals("set-dst-mac")) {
					OFActionDataLayerDestination action = new OFActionDataLayerDestination();
					action.setDataLayerAddress(HexString.fromHexString(value.substring(12)));
					actions.add(action);
				}
			}
		}
		m.setWildcards(wcards);
		
		int length = OFFlowMod.MINIMUM_LENGTH;
		for (OFAction action : actions) {
			length += action.getLengthU();
		}
		fm.setLengthU(length);
		
		return fm;
	}
	
	// get random number in [min, max)
	public static int getRandomNumber(int min, int max) {
		return rand.nextInt(max - min) + min;
	}
}