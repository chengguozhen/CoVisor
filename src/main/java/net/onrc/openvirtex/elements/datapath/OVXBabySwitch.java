package net.onrc.openvirtex.elements.datapath;

import net.onrc.openvirtex.elements.port.OVXPort;

import org.openflow.protocol.OFMessage;

import edu.princeton.cs.hsa.PlumbingNode;

/**
 * The Class OVXBabySwitch.
 */
public class OVXBabySwitch extends OVXSingleSwitch {

	private final OVXMultiSwitch parentSwitch;
	private PlumbingNode plumbingNode;

	public OVXBabySwitch(final Long switchId, final OVXMultiSwitch parentSwitch) {
		super(switchId, parentSwitch.getTenantId());
		this.parentSwitch = parentSwitch;
		this.plumbingNode = new PlumbingNode(switchId);
	}
	
	public PlumbingNode getPlumbingNode() {
		return this.plumbingNode;
	}
	
	public OVXMultiSwitch getParentSwitch() {
		return this.parentSwitch;
	}
	
	public PhysicalSwitch getPhysicalSwitch() {
		return this.parentSwitch.getPhysicalSwitch();
	}
	
	@Override
    public void sendSouth(final OFMessage msg, final OVXPort inPort) {
        this.parentSwitch.sendSouth(msg, this);
    }

}
