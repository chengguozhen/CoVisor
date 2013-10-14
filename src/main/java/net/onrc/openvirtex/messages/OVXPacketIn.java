/*******************************************************************************
 * Copyright (c) 2013 Open Networking Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/


package net.onrc.openvirtex.messages;

import java.util.LinkedList;

import net.onrc.openvirtex.core.OpenVirteXController;
import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.address.OVXIPAddress;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.link.OVXLinkUtils;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.OVXLinkField;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.packet.ARP;
import net.onrc.openvirtex.packet.Ethernet;
import net.onrc.openvirtex.packet.IPv4;
import net.onrc.openvirtex.util.MACAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.Wildcards.Flag;

public class OVXPacketIn extends OFPacketIn implements Virtualizable {

    private final Logger log      = LogManager.getLogger(OVXPacketIn.class
	                                  .getName());
    private PhysicalPort port     = null;
    private OVXPort		 ovxPort  = null;
    private Integer      tenantId = null;

    @Override
    public void virtualize(final PhysicalSwitch sw) {

    	OVXSwitch vSwitch = OVXMessageUtil.untranslateXid(this, sw);
    	/*
    	 * Fetching port from the physical switch
    	 */

    	short inport = this.getInPort();
    	port = sw.getPort(inport);

    	Mappable map = sw.getMap();

    	final OFMatch match = new OFMatch();
    	match.loadFromPacket(this.getPacketData(), inport);

    	/*
    	 * Check whether this packet arrived on
    	 * an edge port.
    	 * 
    	 * if it did we do not need to rewrite anything,
    	 * but just find which controller this should be
    	 * send to.
    	 */
    	if (this.port.isEdge()) {
    		this.tenantId = this.fetchTenantId(match, map, true);
    		if (this.tenantId == null) {
    			this.log.warn(
    					"PacketIn {} does not belong to any virtual network; "
    							+ "dropping and intalling a temporary drop rule",
    							this);
    			this.installDropRule(sw, match);
    			return;
    		}
    		if (vSwitch == null) {
    			vSwitch = map.getVirtualSwitch(sw, this.tenantId);
    		}
    		this.ovxPort = this.port.getOVXPort(this.tenantId, 0);
    		this.sendPkt(vSwitch, match, sw);
    		this.learnAddresses(match, map);
    		this.log.debug("Edge PacketIn {} sent to virtual network {}", this,
    				this.tenantId);
    		return;
    	}

    	/*
    	 * Below handles packets traveling in the core.
    	 */

    	if (match.getDataLayerType() == Ethernet.TYPE_IPv4 || match.getDataLayerType() == Ethernet.TYPE_ARP) {
    		PhysicalIPAddress srcIP = new PhysicalIPAddress(match.getNetworkSource());
    		PhysicalIPAddress dstIP = new PhysicalIPAddress(match.getNetworkDestination());

    		Ethernet eth = new Ethernet();
    		eth.deserialize(this.getPacketData(), 0, this.getPacketData().length);

    		OVXLinkUtils lUtils = new OVXLinkUtils(eth.getSourceMAC(), eth.getDestinationMAC());
    		//rewrite the OFMatch with the values of the link
    		if (lUtils.isValid()) {
    			OVXPort srcPort = port.getOVXPort(lUtils.getTenantId(), lUtils.getLinkId());
    			if (srcPort == null) {
    				this.log.info("ERROR: PacketIn from switch {}, port {} with this match {}", sw.getName(), match.getInputPort(), match);
    				return;
    			}
    			this.setInPort(srcPort.getPortNumber());
    			OVXPort dstPort = map.getVirtualNetwork(lUtils.getTenantId()).getNeighborPort(srcPort);
    			OVXLink link = map.getVirtualSwitch(sw, lUtils.getTenantId()).getMap().
    					getVirtualNetwork(lUtils.getTenantId()).getLink(dstPort, srcPort);
    			if (link == null)
    				return;
    			
    			this.ovxPort = this.port.getOVXPort(lUtils.getTenantId(), link.getLinkId());
    			OVXLinkField linkField = OpenVirteXController.getInstance().getOvxLinkField();
    			//TODO: Need to check that the values in linkId and flowId don't exceed their space
    			if (linkField == OVXLinkField.MAC_ADDRESS) {
    				LinkedList<MACAddress> macList = sw.getMap().getVirtualNetwork(this.ovxPort.getTenantId()).
    						getFlowValues(lUtils.getFlowId());
    				eth.setSourceMACAddress(macList.get(0).toBytes());
    				eth.setDestinationMACAddress(macList.get(1).toBytes());
    				match.setDataLayerSource(eth.getSourceMACAddress());
    				match.setDataLayerDestination(eth.getDestinationMACAddress());
    			}
    			else if (linkField == OVXLinkField.VLAN) {
    				//TODO: retrieve flowId from VLAN value
    			}
    			
    		}   

    		if (match.getDataLayerType() == Ethernet.TYPE_ARP) {
    			// ARP packet
    			final ARP arp = (ARP) eth.getPayload();
    			System.out.println("ETH " + eth);
    			System.out.println("supposed ARP " + arp);
    			this.tenantId = this.fetchTenantId(match, map, true);
    			if (map.getVirtualIP(srcIP) != null) {
    				arp.setSenderProtocolAddress(map.getVirtualIP(srcIP)
    						.getIp());
    			}
    			if (map.getVirtualIP(dstIP) != null) {
    				arp.setTargetProtocolAddress(map.getVirtualIP(dstIP)
    						.getIp());
    			}
    		} else
    			if (match.getDataLayerType() == Ethernet.TYPE_IPv4) {
    				final IPv4 ip = (IPv4) eth.getPayload();
    				this.tenantId = map.getVirtualIP(srcIP).getTenantId();
    				ip.setDestinationAddress(map.getVirtualIP(dstIP).getIp());
    				ip.setSourceAddress(map.getVirtualIP(srcIP).getIp());
    			} else {
    				this.log.info("{} handling not yet implemented; dropping",
    						match.getDataLayerType());
    				this.installDropRule(sw, match);
    				return;
    			}
    		this.setPacketData(eth.serialize());
    		if (vSwitch == null)  {
    			vSwitch = map.getVirtualSwitch(sw, this.tenantId);
    		}
    		this.sendPkt(vSwitch, match, sw);
    		this.log.debug("IPv4 PacketIn {} sent to virtual network {}", this,
    				this.tenantId);
    		return;
    	}

    	this.tenantId = this.fetchTenantId(match, map, true);
    	if (this.tenantId == null) {
    		this.log.warn(
    				"PacketIn {} does not belong to any virtual network; "
    						+ "dropping and intalling a temporary drop rule",
    						this);
    		this.installDropRule(sw, match);
    		return;
    	}
    	if (vSwitch == null) {
    		vSwitch = map.getVirtualSwitch(sw, this.tenantId);
    	}
    	this.sendPkt(vSwitch, match, sw);
    	this.log.debug("Layer2 PacketIn {} sent to virtual network {}", this,
    			this.tenantId);
    }

    private void sendPkt(final OVXSwitch vSwitch, final OFMatch match,
    		final PhysicalSwitch sw) {
    	if (vSwitch == null || !vSwitch.isActive()) {
    		this.log.warn(
    				"Controller for virtual network {} has not yet connected "
    						+ "or is down", this.tenantId);
    		this.installDropRule(sw, match);
    		return;
    	}
    	this.setBufferId(vSwitch.addToBufferMap(this));
    	if (this.port != null && this.ovxPort != null) {
    		this.setInPort(this.ovxPort.getPortNumber());
    		vSwitch.sendMsg(this, sw);
    	}
    }

    private void learnAddresses(final OFMatch match, final Mappable map) {
	if (match.getDataLayerType() == 0x800
	        || match.getDataLayerType() == 0x806) {
	    final OVXNetwork vnet = map.getVirtualNetwork(this.tenantId);

	    OVXIPAddress vip = new OVXIPAddress(this.tenantId,
		    match.getNetworkSource());
	    if (!match.getWildcardObj().isWildcarded(Flag.NW_SRC)
		    && map.getPhysicalIP(vip, this.tenantId) == null) {
		final PhysicalIPAddress pip = new PhysicalIPAddress(
		        vnet.nextIP());
		map.addIP(pip, vip);
	    }
	    vip = new OVXIPAddress(this.tenantId, match.getNetworkDestination());
	    if (!match.getWildcardObj().isWildcarded(Flag.NW_DST)
		    && map.getPhysicalIP(vip, this.tenantId) == null) {
		final PhysicalIPAddress pip = new PhysicalIPAddress(
		        vnet.nextIP());
		map.addIP(pip, vip);
	    }
	}
    }

    private void installDropRule(final PhysicalSwitch sw, final OFMatch match) {
	final OVXFlowMod fm = new OVXFlowMod();
	fm.setMatch(match);
	fm.setBufferId(this.getBufferId());
	fm.setHardTimeout((short) 1);
	sw.sendMsg(fm, sw);
    }

    private Integer fetchTenantId(final OFMatch match, final Mappable map,
	    final boolean useMAC) {
	if (useMAC) {
	    return map.getMAC(MACAddress.valueOf(match.getDataLayerSource()));
	}
	return null;
    }

    public OVXPacketIn(final OVXPacketIn pktIn) {
	this.bufferId = pktIn.bufferId;
	this.inPort = pktIn.inPort;
	this.length = pktIn.length;
	this.packetData = pktIn.packetData;
	this.reason = pktIn.reason;
	this.totalLength = pktIn.totalLength;
	this.type = pktIn.type;
	this.version = pktIn.version;
	this.xid = pktIn.xid;
    }

    public OVXPacketIn() {
    }

}
