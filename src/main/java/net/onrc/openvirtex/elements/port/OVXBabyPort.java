package net.onrc.openvirtex.elements.port;

import java.util.HashMap;
import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;

public class OVXBabyPort {//extends OVXPort {

	final private int tenantId;
	final private OVXBabySwitch parentSwitch;
	final short portNumber;
	final private boolean isMapToPhysicalPort;
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch, PhysicalPort port)
			throws IndexOutOfBoundException {
		//super(tenantId, port, true);
		this.tenantId = tenantId;
		this.parentSwitch = babySwitch;
		this.portNumber = this.parentSwitch.getNextPortNumber();
		this.isMapToPhysicalPort = true;
		babySwitch.getParentSwitch().getPlumbingGraph().addPort(babySwitch.getSwitchId(), this.portNumber, port.getPortNumber());
	}
	
	public OVXBabyPort(int tenantId, OVXBabySwitch babySwitch)
			throws IndexOutOfBoundException {
		//super(tenantId, babySwitch.getPhysicalSwitch().getPort((short) 1) , true);
		this.tenantId = tenantId;
		this.parentSwitch = babySwitch;
		this.portNumber = this.parentSwitch.getNextPortNumber();
		this.isMapToPhysicalPort = false;
		babySwitch.getParentSwitch().getPlumbingGraph().addPort(babySwitch.getSwitchId(), this.portNumber, null);
	}
	
	public boolean isMapToPhysicalPort() {
		return this.isMapToPhysicalPort;
	}
	
	public short getPortNumber() {
		return this.portNumber;
	}
	
	public int getTenantId() {
		return this.tenantId;
	}
	
	public Map<String, Object> getDBObject() {
        Map<String, Object> dbObject = new HashMap<String, Object>();
        dbObject.put(TenantHandler.VPORT, this.portNumber);
        return dbObject;
    }
	
	//@Override
	public OVXBabySwitch getParentSwitch() {
		return this.parentSwitch;
	}
}