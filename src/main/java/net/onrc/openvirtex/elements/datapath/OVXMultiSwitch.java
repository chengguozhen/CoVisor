package net.onrc.openvirtex.elements.datapath;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class OVXMultiSwitch.  Inherits from OVXSwitch, but also implements
 * many Network methods.
 */
public class OVXMultiSwitch extends OVXSingleSwitch {

	private static Logger log = LogManager.getLogger(OVXMultiSwitch.class.getName());
	
	private List<OVXBabySwitch> babySwitches;


	public OVXMultiSwitch(final Long switchId, final Integer tenantId) {
		super(switchId, tenantId);
		this.babySwitches = new ArrayList<OVXBabySwitch>();
	}


	public void addSwitch(OVXBabySwitch babySwitch) {
		this.babySwitches.add(babySwitch);
	}

	public List<OVXBabySwitch> getSwitches() {
		return this.babySwitches;
	}

	
}