package net.onrc.openvirtex.elements.port;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;

public class OVXBabyPort extends OVXPort {

	private static Logger log = LogManager.getLogger(OVXBabyPort.class
			.getName());
	final private OVXBabySwitch parentSwitch;
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch, PhysicalPort port)
			throws IndexOutOfBoundException {
		super(tenantId, port, true);
		this.parentSwitch = babySwitch;
	}
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch)
			throws IndexOutOfBoundException {
		super(tenantId, null, true);
		this.parentSwitch = babySwitch;
	}
	
	@Override
	public OVXBabySwitch getParentSwitch() {
		return this.parentSwitch;
	}
}