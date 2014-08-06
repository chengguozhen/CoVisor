package net.onrc.openvirtex.elements.datapath;

import net.onrc.openvirtex.elements.port.OVXPort;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMessage;

/**
 * The Class OVXBabySwitch.
 */
public class OVXBabySwitch extends OVXSingleSwitch {

	private static Logger log = LogManager.getLogger(OVXBabySwitch.class.getName());
	private final OVXMultiSwitch parentSwitch;

	public OVXBabySwitch(final Long switchId, final OVXMultiSwitch parentSwitch) {
		super(switchId, parentSwitch.getTenantId());
		this.parentSwitch = parentSwitch;
	}
	
	public OVXMultiSwitch getParentSwitch() {
		return this.parentSwitch;
	}
	
	public PhysicalSwitch getPhysicalSwitch() {
		return this.parentSwitch.getPhysicalSwitch();
	}
	
	@Override
    public void sendSouth(final OFMessage msg, final OVXPort inPort) {
        log.info("babyswitch {} get msg {}", this.getName(), msg);
        this.parentSwitch.sendSouth(msg, this);
    }

}
