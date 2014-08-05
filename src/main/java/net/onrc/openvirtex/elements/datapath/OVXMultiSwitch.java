package net.onrc.openvirtex.elements.datapath;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.exceptions.ControllerStateException;
import net.onrc.openvirtex.exceptions.RoutingAlgorithmException;
import net.onrc.openvirtex.exceptions.PortMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.UnknownRoleException;
import net.onrc.openvirtex.exceptions.InvalidDPIDException;
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
import org.openflow.util.HexString;

import net.onrc.openvirtex.elements.Persistable;
import net.onrc.openvirtex.elements.datapath.role.RoleManager.Role;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXBabyPort;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.link.OVXBabyLink;
import net.onrc.openvirtex.elements.link.OVXLink;

/**
 * The Class OVXMultiSwitch.  Inherits from OVXSwitch, but also implements
 * many Network methods.
 */
public class OVXMultiSwitch extends OVXSwitch implements Persistable {

	public static final String DPDESCSTRING = "OpenVirteX Multi Virtual Switch";
	private static Logger log = LogManager.getLogger(OVXMultiSwitch.class
			.getName());

	// private final BitSetIndex linkCounter;
	protected final Set<OVXBabySwitch> babySwitchSet;
	protected final Set<OVXBabyLink> babyLinkSet;

	/*
	 * Keep track of which channel is associated with each OVXBabySwitch to pass
	 * sendMsg call to correct OVXBabySwitch. An OVXBabySwitch will appear in
	 * channelMap multiple times if it is connected to multiple controllers.
	 */
	protected final Map<Channel, OVXBabySwitch> channelMap;
	protected final Map<Long, OVXBabySwitch> dpidMap;
	protected final Map<OVXBabyPort, OVXBabyPort> neighborPortMap;
	protected final Map<OVXBabySwitch, HashSet<OVXBabySwitch>> neighborMap;

	public OVXMultiSwitch(final Long switchId, final Integer tenantId) {
		// Switch stuff.
		super(switchId, tenantId);

		// Network stuff.
		this.babySwitchSet = new HashSet<OVXBabySwitch>();
		this.babyLinkSet = new HashSet<OVXBabyLink>();
		this.channelMap = new HashMap<Channel, OVXBabySwitch>();
		this.dpidMap = new HashMap<Long, OVXBabySwitch>();
		this.neighborPortMap = new HashMap<OVXBabyPort, OVXBabyPort>();
		this.neighborMap = new HashMap<OVXBabySwitch, HashSet<OVXBabySwitch>>();
	}

	// Find the OVXBabyPort corresponding to physPortNumber.
	public OVXBabyPort getPort(final short physPortNumber) {
		for (OVXBabySwitch sw : this.babySwitchSet) {
			for (OVXPort port : sw.getPorts().values()) {
				OVXBabyPort babyPort = (OVXBabyPort) port;
				if (babyPort.hasPhysPort()) {
					if (babyPort.getPhysicalPortNumber() == physPortNumber) {
						return babyPort;
					}
				}
			}
		}
		return null;
	}

	public synchronized OVXBabyLink connectLink(final long babySrcDpid,
			final short babySrcPort, final long babyDstDpid,
			final short babyDstPort, final int linkId)
			throws IndexOutOfBoundException, PortMappingException,
			RoutingAlgorithmException {
		// Get the end OVXBabyPorts.
		OVXBabyPort srcPort = getSwitch(babySrcDpid).getPort(babySrcPort);
		OVXBabyPort dstPort = getSwitch(babyDstDpid).getPort(babyDstPort);

		// Create link and add it to the topology.
		OVXBabyLink link = new OVXBabyLink(linkId, this.tenantId, srcPort,
				dstPort);
		OVXBabyLink reverseLink = new OVXBabyLink(linkId, this.tenantId,
				dstPort, srcPort);
		addLink(link);
		addLink(reverseLink);
		log.info(
				"Created bi-directional virtual baby link {} between ports {}/{} - {}/{} "
						+ "in virtual network {}", link.getLinkId(), link
						.getSrcSwitch().getSwitchName(), srcPort
						.getPortNumber(), link.getDstSwitch().getSwitchName(),
				dstPort.getPortNumber(), this.getTenantId());
		srcPort.boot();
		dstPort.boot();
		return link;
	}

	/*
	 * If controller adds link to network between two OVXBabySwitches,
	 * OVXNetwork passes the call to their parent OVXMultiSwitch.
	 */
	public void addLink(final OVXBabyLink link) {
		this.babyLinkSet.add(link);
		final OVXBabySwitch srcSwitch = link.getSrcSwitch();
		final OVXBabySwitch dstSwitch = link.getDstSwitch();
		final OVXBabyPort srcPort = link.getSrcPort();
		final OVXBabyPort dstPort = link.getSrcPort();
		srcPort.setEdge(false);
		dstPort.setEdge(false);
		final HashSet<OVXBabySwitch> neighbors = this.neighborMap
				.get(srcSwitch);
		neighbors.add(dstSwitch);
		this.neighborPortMap.put(link.getSrcPort(), link.getDstPort());
	}

	public boolean removeLink(final OVXBabyLink link) {
		this.babyLinkSet.remove(link);
		final OVXBabySwitch srcSwitch = link.getSrcSwitch();
		final OVXBabySwitch dstSwitch = link.getDstSwitch();
		final OVXBabyPort srcPort = link.getSrcPort();
		final OVXBabyPort dstPort = link.getSrcPort();
		srcPort.setEdge(true);
		dstPort.setEdge(true);
		final HashSet<OVXBabySwitch> neighbors = this.neighborMap
				.get(srcSwitch);
		neighbors.remove(dstSwitch);
		this.neighborPortMap.remove(link.getSrcPort());
		return true;
	}

	public void addSwitch(final OVXBabySwitch sw) {
		if (this.babySwitchSet.add(sw)) {
			this.dpidMap.put(sw.getSwitchId(), sw);
			this.neighborMap.put(sw, new HashSet<OVXBabySwitch>());
		}
	}

	/*
	 * OVXMultiSwitch itself doesn't connect to controller so shouldn't register
	 * itself with OpenVirteXController. Still needs to set itself active and
	 * boot its ports.
	 */
	public boolean boot() {
		setActive(true);
		return true;
	}

	/*
	 * public void setConnected(final boolean isConnected) {
	 * log.info("OVXMultiSwitch setConnected."); for (final OVXBabySwitch sw :
	 * this.babySwitchSet) { sw.setConnected(isConnected); } }
	 */

	/*
	 * June 14 Should removeSwitch also remove sw from dpidMap and neighborMap?
	 * What about removing sw from places it appears in neighbor listings of
	 * other switches? Why doesn't OVXNetwork's removeSwitch method do these
	 * things?
	 */
	public boolean removeSwitch(final OVXBabySwitch sw) {
		return this.babySwitchSet.remove(sw);
	}

	public Set<OVXBabySwitch> getNeighbors(final OVXBabySwitch sw) {
		return Collections.unmodifiableSet(this.neighborMap.get(sw));
	}

	public OVXBabyPort getNeighborPort(final OVXBabyPort port) {
		return this.neighborPortMap.get(port);
	}

	/**
	 * Returns switch instance based on its dpid.
	 * 
	 * @param dpid
	 *            the datapath ID
	 * @return the switch instance
	 */
	public OVXBabySwitch getSwitch(final Long dpid) throws InvalidDPIDException {
		try {
			return this.dpidMap.get(dpid);
		} catch (ClassCastException | NullPointerException ex) {
			throw new InvalidDPIDException("DPID "
					+ HexString.toHexString(dpid) + " is unknown ");
		}
	}

	/**
	 * Returns the unmodifiable set of switches belonging to the network.
	 * 
	 * @return set of switches
	 */
	public Set<OVXBabySwitch> getSwitches() {
		return Collections.unmodifiableSet(this.babySwitchSet);
	}

	/**
	 * Returns the unmodifiable set of links belonging to the network.
	 * 
	 * @return set of links
	 */
	public Set<OVXBabyLink> getLinks() {
		return Collections.unmodifiableSet(this.babyLinkSet);
	}

	/**
	 * Gets the link instance between the given ports.
	 * 
	 * @param srcPort
	 *            the source port
	 * @param dstPort
	 *            the destination port
	 * @return the link instance, null if it doesn't exist
	 */
	public OVXBabyLink getLink(final OVXBabyPort srcPort,
			final OVXBabyPort dstPort) {
		for (final OVXBabyLink link : this.babyLinkSet) {
			if (link.getSrcPort().equals(srcPort)
					&& link.getDstPort().equals(dstPort)) {
				return link;
			}
		}
		return null;
	}

	/**
	 * Generates a new XID for messages destined for the physical network.
	 * 
	 * @param msg
	 *            The OFMessage being translated
	 * @param inPort
	 *            The ingress port
	 * @return the new message XID
	 * @throws SwitchMappingException
	 */
	/*
	 * Invariant: OVXBabySwitches handle northbound and southbound
	 * communication. OVXMultiSwitch translate method never should be called.
	 */
	public int translate(OFMessage msg, OVXPort inPort) {
		return -1;
	}

	/**
	 * Sends a message towards the physical network, via the PhysicalSwitch
	 * mapped to this OVXSwitch.
	 * 
	 * @param msg
	 *            The OFMessage being translated
	 * @param inPort
	 *            The ingress port, used to identify the PhysicalSwitch
	 *            underlying an OVXBigSwitch. May be null. Sends a message
	 *            towards the physical network
	 * 
	 * @param msg
	 *            The OFMessage being translated
	 * @param inPort
	 *            The ingress port
	 */
	/*
	 * Invariant: OVXBabySwitches handle northbound and southbound
	 * communication. OVXMultiSwitch sendSouth method never should be called.
	 */
	public void sendSouth(OFMessage msg, OVXPort inPort) {
		return;
	}

	// OVXMultiSwitch doesn't send any messages to the controller.
	private void cleanUpFlowMods(boolean isOk) {
		return;
	}

	private void denyAccess(Channel channel, OFMessage m, Role role) {
		return;
	}

	private void sendRoleReply(Role role, int xid, Channel channel) {
		return;
	}

	/*
	 * Send message on behalf of babySwitch. Necessary so OVXMultiSwitch
	 * channelMux has a map of all (message XID, channel) pairs, which it needs
	 * to look up the OVXBabySwitch for a reply sendMsg.
	 * 
	 * public void handleIO(final OFMessage msg, Channel channel, OVXBabySwitch
	 * babySwitch) { msg.setXid(channelMux.translate(msg.getXid(), channel));
	 * try { if (babySwitch.roleMan.canSend(channel, msg)) { ((Devirtualizable)
	 * msg).devirtualize(babySwitch); } else { denyAccess(channel, msg,
	 * babySwitch.roleMan.getRole(channel)); } } catch (final ClassCastException
	 * e) { log.error("Received illegal message : " + msg); } }
	 */

	public void handleIO(final OFMessage msg, Channel channel) {
		log.info("OVXMultiSwitch handleIO.");
		super.handleIO(msg, channel);
	}

	public void handleRoleIO(OFVendor msg, Channel channel) {
		log.info("OVXMultiSwitch handleRoleIO.");
		super.handleRoleIO(msg, channel);
	}

	public void sendMsg(final OFMessage msg, final OVXSendMsg from) {
		log.info("OVXMultiSwitch sendMsg.");
		return;
	}

	/*
	 * // Called by OVXBabySwitch in addChannel. public void
	 * addChannelSwitch(Channel channel, OVXBabySwitch babySwitch) {
	 * this.channelMap.put(channel, babySwitch); }
	 * 
	 * // Called by OVXBabySwitch in removeChannel. public void
	 * removeChannelSwitch(Channel channel) { this.channelMap.remove(channel); }
	 * 
	 * // Send message on behalf of OVXBabySwitch. public void sendMsg(final
	 * OFMessage msg, final OVXSendMsg from) {
	 * log.info("OVXMultiSwitch sendMsg."); XidPair<Channel> pair =
	 * channelMux.untranslate(msg.getXid()); Channel c = null; OVXBabySwitch
	 * babySwitch = null; if (pair != null) { msg.setXid(pair.getXid()); c =
	 * pair.getSwitch(); babySwitch = this.channelMap.get(c); } else {
	 * log.warn("pair == null."); }
	 * 
	 * // Simulate being babySwitch and send message. if (babySwitch != null) {
	 * if (babySwitch.isConnected() && babySwitch.isActive()) {
	 * babySwitch.roleMan.sendMsg(msg, c); } else { log.warn(
	 * "Virtual switch {} is not active or is not connected to a controller",
	 * babySwitch.getSwitchName()); } } else {
	 * log.warn("Channel not found in channelMap."); } }
	 */
}