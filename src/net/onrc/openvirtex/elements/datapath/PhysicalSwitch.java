/**
 * Copyright (c) 2013 Open Networking Laboratory
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package net.onrc.openvirtex.elements.datapath;

import java.util.Collections;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.messages.Virtualizable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPhysicalPort;

/**
 * The Class PhysicalSwitch.
 */
public class PhysicalSwitch extends Switch<PhysicalPort> {

    /** The log. */
    Logger log = LogManager.getLogger(PhysicalSwitch.class.getName());

    /**
     * Instantiates a new physical switch.
     * 
     * @param switchId
     *            the switch id
     */
    public PhysicalSwitch(final long switchId) {
	super(switchId);
	PhysicalNetwork.getInstance().addSwitch(this);
    }

    /**
     * Gets the OVX port number.
     * 
     * @param physicalPortNumber
     *            the physical port number
     * @param tenantId
     *            the tenant id
     * @return the oVX port number
     */
    public Short getOVXPortNumber(final Short physicalPortNumber,
	    final Integer tenantId) {
	return this.portMap.get(physicalPortNumber).getOVXPortNumber(tenantId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.onrc.openvirtex.elements.datapath.Switch#handleIO(org.openflow.protocol
     * .OFMessage)
     */
    @Override
    public void handleIO(final OFMessage msgs) {
	try {
	    ((Virtualizable) msgs).virtualize(this);
	} catch (final ClassCastException e) {
	    this.log.error("Received illegal message : " + msgs);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.onrc.openvirtex.elements.datapath.Switch#tearDown()
     */
    @Override
    public void tearDown() {
	this.log.info("Switch disconnected {} ",
	        this.featuresReply.getDatapathId());
	this.channel.disconnect();

    }

    /**
     * Fill port map.
     */
    protected void fillPortMap() {
	for (final OFPhysicalPort port : this.featuresReply.getPorts()) {
	    final PhysicalPort physicalPort = new PhysicalPort(port, false,
		    this);
	    this.addPort(physicalPort);
	}
    }
    
    public boolean addPort(PhysicalPort port) {
	boolean result = super.addPort(port);
	if (result) {
	    PhysicalNetwork.getInstance().addPort(port);
	}
	return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see net.onrc.openvirtex.elements.datapath.Switch#init()
     */
    @Override
    public void init() {
	this.log.info("Switch connected {} : {}",
	        this.featuresReply.getDatapathId(),
	        this.desc.getHardwareDescription());
	this.fillPortMap();
    }

    /*
     * Temporary implementation(non-Javadoc)
     * 
     * @see
     * net.onrc.openvirtex.core.io.OVXSendMsg#sendMsg(org.openflow.protocol.
     * OFMessage, net.onrc.openvirtex.core.io.OVXSendMsg)
     */
    @Override
    public void sendMsg(final OFMessage msg, final OVXSendMsg from) {
	this.channel.write(Collections.singletonList(msg));
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.onrc.openvirtex.elements.datapath.Switch#toString()
     */
    @Override
    public String toString() {
	return "DPID : " + this.featuresReply.getDatapathId()
	        + ", remoteAddr : "
	        + this.channel.getRemoteAddress().toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.onrc.openvirtex.elements.datapath.Switch#setSwitchId(java.lang.Long)
     */
    @Override
    public boolean setSwitchId(final Long switchId) {
	return false;
    }

    /**
     * Gets the port.
     * 
     * @param portNumber
     *            the port number
     * @return a COPY of the port instance
     */
    @Override
    public PhysicalPort getPort(final Short portNumber) {
	return this.portMap.get(portNumber).clone();
    }
}
