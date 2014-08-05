package net.onrc.openvirtex.elements.port;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.db.DBManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPortStatus.OFPortReason;

import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.datapath.OVXMultiSwitch;
import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.exceptions.LinkMappingException;
import net.onrc.openvirtex.routing.SwitchRoute;
import net.onrc.openvirtex.util.MACAddress;
import net.onrc.openvirtex.messages.OVXPortStatus;
import net.onrc.openvirtex.elements.Persistable;

public class OVXBabyPort extends OVXPort implements Persistable {

    private static Logger log = LogManager.getLogger(OVXBabyPort.class
						     .getName());
    private final boolean hasPhysPort;
    private OVXBabySwitch parentSwitch;

    // Constructors for OVXBabyPort that does map to a port on the multiSwitch.
    public OVXBabyPort(final int tenantId, final OVXBabySwitch parentSwitch,
		       final PhysicalPort physicalPort, final boolean isEdge,
		       final short portNumber)
	throws IndexOutOfBoundException {
	// Create baby port.
	super(tenantId, physicalPort, isEdge, portNumber);

	/*
	 * OVXPort constructor sets parentSwitch to the multiSwitch.  We want parentSwitch of port
	 * to be babySwitch, and parentSwitch of babySwitch to be multiSwitch.
	 */
	this.parentSwitch = parentSwitch;
	OVXMultiSwitch multiSwitch = ((OVXBabySwitch) this.parentSwitch).getParentSwitch();
       
	this.hasPhysPort = true;
    }
    
    public OVXBabyPort(final int tenantId, final OVXBabySwitch parentSwitch,
		       final PhysicalPort physicalPort, final boolean isEdge)
	throws IndexOutOfBoundException {
	this(tenantId, parentSwitch, physicalPort, isEdge, (short) 0);
	this.portNumber = this.parentSwitch.getNextPortNumber();
	this.name = "ovxbabyport-" + this.portNumber;
    }

    // Constructors for OVXBabyPort that does not map to a port on the multiSwitch.
    public OVXBabyPort(final int tenantId, final OVXBabySwitch parentSwitch,
		       final boolean isEdge, final short portNumber,
		       final MACAddress mac) throws IndexOutOfBoundException {
	/*
	 * June 29
	 * Pass null hardwareAddress and physicalPort to superclass constructor.
	 * Then set hardwareAddress and mac based on fresh address passed in by
	 * network.  Need to rewrite the logic of all methods that rely on a non null
	 * physicalPort.
	 */
	super(tenantId, null, isEdge, portNumber);
	this.name = "ovxbabyport-" + this.portNumber;
	this.hardwareAddress = mac.toBytes();
	this.mac = mac;

	/*
	 * OVXPort constructor sets parentSwitch to the multiSwitch.  We want parentSwitch of port
	 * to be babySwitch, and parentSwitch of babySwitch to be multiSwitch.
	 */
	this.parentSwitch = parentSwitch;
	OVXMultiSwitch multiSwitch = ((OVXBabySwitch) this.parentSwitch).getParentSwitch();

	this.hasPhysPort = false;
    }

    public OVXBabyPort(final int tenantId, final OVXBabySwitch parentSwitch,
		       final boolean isEdge, final MACAddress mac)
	throws IndexOutOfBoundException {
	this(tenantId, parentSwitch, isEdge, parentSwitch.getNextPortNumber(), mac);
    }
    
    // babyPort maps to a physical port.
    public boolean hasPhysPort() {
	return this.hasPhysPort;
    }

    /*
     * 30 July
     * Message sending probably will need custom logic.  Currently is copied from
     * OVXPort register.
     */
    public void register() {
	this.parentSwitch.addPort(this);
	if (hasPhysPort()) {
	    getPhysicalPort().setOVXPort(this);
	}
	if (this.parentSwitch.isActive()) {
	    sendStatusMsg(OFPortReason.OFPPR_ADD);
	    this.parentSwitch.generateFeaturesReply();
	}
	DBManager.getInstance().save(this);
    }

    // Override Port getParentSwitch to return static type OVXBabySwitch.
    public OVXBabySwitch getParentSwitch() {
	return this.parentSwitch;
    }

    

    /*
     * June 29
     * Need to implement logic to send correct counters.  For now, send nothing.
     */
    private void cleanUpFlowMods() {
	return;
    }
    
    public boolean equals(final OVXBabyPort port) {
	return this.portNumber == port.portNumber
	    && this.parentSwitch.getSwitchId() == port.getParentSwitch()
	    .getSwitchId();
    }
    
    /*
     * 29 July
     * Probably will need to add logic to send the correct messages.  For now,
     * use OVXPort tearDown.
     */
    public void tearDown() {
	super.tearDown();
    }

    @Override
    public String toString() {
	int linkId = 0;
	if (isLink()) {
	    linkId = this.getLink().getOutLink().getLinkId();
	}
	return "BabyPORT:\n- portNumber: " + this.portNumber
	    + "\n- parentSwitch: " + this.getParentSwitch().getSwitchName()
	    + "\n- virtualNetwork: " + this.getTenantId()
	    + "\n- hardwareAddress: " + MACAddress.valueOf(this.hardwareAddress).toString()
	    + "\n- config: " + this.config + "\n- state: " + this.state
	    + "\n- currentFeatures: " + this.currentFeatures
	    + "\n- advertisedFeatures: " + this.advertisedFeatures
	    + "\n- supportedFeatures: " + this.supportedFeatures
	    + "\n- peerFeatures: " + this.peerFeatures 
	    + "\n- isEdge: " + this.isEdge
	    + "\n- isActive: " + isActive()
	    + "\n- linkId: " + linkId
	    + "\n- physicalPortNumber: " + this.getPhysicalPortNumber()
	    + "\n- physicalSwitchName: " + this.getPhysicalPort().getParentSwitch().getSwitchName();
    }
}