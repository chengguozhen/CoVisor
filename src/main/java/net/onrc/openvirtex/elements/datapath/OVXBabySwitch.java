package net.onrc.openvirtex.elements.datapath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.db.DBManager;
import net.onrc.openvirtex.elements.Persistable;

import java.util.Set;
import java.util.TreeSet;

import net.onrc.openvirtex.elements.datapath.role.RoleManager;
import net.onrc.openvirtex.elements.datapath.role.RoleManager.Role;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXBabyPort;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ControllerStateException;
import net.onrc.openvirtex.exceptions.MappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.UnknownRoleException;
import net.onrc.openvirtex.messages.Devirtualizable;
import net.onrc.openvirtex.messages.OVXFlowMod;
import net.onrc.openvirtex.messages.OVXMessageUtil;
import net.onrc.openvirtex.messages.OVXPacketIn;
import net.onrc.openvirtex.util.BitSetIndex;
import net.onrc.openvirtex.util.BitSetIndex.IndexType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFVendor;
import org.openflow.protocol.OFError.OFBadRequestCode;
import org.openflow.util.LRULinkedHashMap;
import org.openflow.vendor.nicira.OFNiciraVendorData;
import org.openflow.vendor.nicira.OFRoleReplyVendorData;
import org.openflow.vendor.nicira.OFRoleRequestVendorData;

/**
 * The Class OVXBabySwitch.
 */
public class OVXBabySwitch extends OVXSwitch implements Persistable {

	public static final String DPDESCSTRING = "OpenVirteX Baby Virtual Switch";
	private static Logger log = LogManager.getLogger(OVXBabySwitch.class
			.getName());
	public final OVXMultiSwitch parentSwitch;

	public OVXBabySwitch(final Long switchId, final OVXMultiSwitch parentSwitch) {
		super(switchId, parentSwitch.getTenantId());
		this.parentSwitch = parentSwitch;
	}

	// Override getPort of Switch to return static type OVXBabyPort.
	public OVXBabyPort getPort(final short portNumber) {
		return (OVXBabyPort) super.getPort(portNumber);
	}

	public OVXMultiSwitch getParentSwitch() {
		return this.parentSwitch;
	}

	public long getParentId() {
		return getParentSwitch().getSwitchId();
	}

	@Override
	public void handleRoleIO(OFVendor msg, Channel channel) {
		log.info("OVXBabySwitch handleRoleIO.");
		super.handleRoleIO(msg, channel);
	}

	/*
	 * Tell OVXMultiSwitch about new channel so parent can find this switch when
	 * it needs to send a reply on its behalf.
	 * 
	 * @Override public void setChannel(Channel channel) {
	 * super.setChannel(channel); getParentSwitch().addChannelSwitch(channel,
	 * this); }
	 * 
	 * // Tell OVXMultiSwitch to remove channel from its channelMap.
	 * 
	 * @Override public void removeChannel(Channel channel) {
	 * super.removeChannel(channel);
	 * getParentSwitch().removeChannelSwitch(channel); }
	 */

	// Copied directly from OVXSingleSwitch.
	@Override
	public void sendSouth(OFMessage msg, OVXPort inPort) {
		PhysicalSwitch psw = getPhySwitch(inPort);
		log.info("Sending packet to sw {}: {}", psw.getName(), msg);
		psw.sendMsg(msg, this);
	}

	// Copied directly from OVXSingleSwitch.
	@Override
	public int translate(final OFMessage ofm, final OVXPort inPort) {
		// get new xid from only PhysicalSwitch tied to this switch
		PhysicalSwitch psw = getPhySwitch(inPort);
		return psw.translate(ofm, this);
	}

	private PhysicalSwitch getPhySwitch(OVXPort inPort) {
		PhysicalSwitch psw = null;
		if (inPort == null) {
			try {
				psw = this.map.getPhysicalSwitches(getParentSwitch()).get(0);
			} catch (SwitchMappingException e) {
				log.warn("Cannot recover physical switch : {}", e);
			}
		} else {
			return inPort.getPhysicalPort().getParentSwitch();
		}
		return psw;
	}
}
