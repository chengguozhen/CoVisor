package net.onrc.openvirtex.elements.datapath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

}
