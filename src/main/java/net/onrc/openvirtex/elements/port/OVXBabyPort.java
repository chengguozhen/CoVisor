package net.onrc.openvirtex.elements.port;

import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;

public class OVXBabyPort extends OVXPort {

	final private OVXBabySwitch parentSwitch;
	final private boolean isMapToPhysicalPort;
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch, PhysicalPort port)
			throws IndexOutOfBoundException {
		super(tenantId, port, true);
		this.parentSwitch = babySwitch;
		this.isMapToPhysicalPort = true;
		babySwitch.getParentSwitch().getPlumbingGraph().addPort(babySwitch.getSwitchId(), this.portNumber, port.getPortNumber());
	}
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch)
			throws IndexOutOfBoundException {
		super(tenantId, babySwitch.getPhysicalSwitch().getPort((short) 1) , true);
		this.parentSwitch = babySwitch;
		this.isMapToPhysicalPort = false;
		babySwitch.getParentSwitch().getPlumbingGraph().addPort(babySwitch.getSwitchId(), this.portNumber, null);
	}
	
	public boolean isMapToPhysicalPort() {
		return this.isMapToPhysicalPort;
	}
	
	
	@Override
	public OVXBabySwitch getParentSwitch() {
		return this.parentSwitch;
	}
}