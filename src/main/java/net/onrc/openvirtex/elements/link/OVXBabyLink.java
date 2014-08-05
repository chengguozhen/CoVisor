package net.onrc.openvirtex.elements.link;

import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.port.OVXBabyPort;
import net.onrc.openvirtex.exceptions.PortMappingException;
import net.onrc.openvirtex.exceptions.RoutingAlgorithmException;
import net.onrc.openvirtex.routing.RoutingAlgorithms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class OVXBabyLink.
 * 
 */
public class OVXBabyLink extends OVXLink {
    Logger log = LogManager.getLogger(OVXBabyLink.class.getName());
    public OVXBabyLink(final Integer linkId, final Integer tenantId,
		       final OVXBabyPort srcPort, final OVXBabyPort dstPort) 
	throws PortMappingException, RoutingAlgorithmException {
	super(linkId, tenantId, srcPort, dstPort,
	      new RoutingAlgorithms("spf", (byte) 1));
    }

    public OVXBabyPort getSrcPort() {
	return (OVXBabyPort) this.srcPort;
    }

    public OVXBabyPort getDstPort() {
	return (OVXBabyPort) this.dstPort;
    }

    public OVXBabySwitch getSrcSwitch() {
	return (OVXBabySwitch) super.getSrcSwitch();
    }

    public OVXBabySwitch getDstSwitch() {
	return (OVXBabySwitch) super.getDstSwitch();
    }

}