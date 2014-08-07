package net.onrc.openvirtex.elements.port;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFPhysicalPort;

import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;

public class OVXBabyPort extends OVXPort {

	private static Logger log = LogManager.getLogger(OVXBabyPort.class
			.getName());
	final private OVXBabySwitch parentSwitch;
	final private boolean isMapToPhysicalPort;
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch, PhysicalPort port)
			throws IndexOutOfBoundException {
		super(tenantId, port, true);
		this.parentSwitch = babySwitch;
		this.isMapToPhysicalPort = false;
	}
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch)
			throws IndexOutOfBoundException {
		super(tenantId, babySwitch.getPhysicalSwitch().getPort((short) 1) , true);
		this.parentSwitch = babySwitch;
		this.isMapToPhysicalPort = true;
	}
	
	public boolean isMapToPhysicalPort() {
		return this.isMapToPhysicalPort;
	}
	
	
	@Override
	public OVXBabySwitch getParentSwitch() {
		return this.parentSwitch;
	}
}