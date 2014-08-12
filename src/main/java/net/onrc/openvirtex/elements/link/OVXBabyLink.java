package net.onrc.openvirtex.elements.link;

import net.onrc.openvirtex.elements.datapath.OVXMultiSwitch;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.port.OVXBabyPort;

public class OVXBabyLink {
	
	private int linkId;
	private OVXBabySwitch srcSwitch;
	private OVXBabyPort srcPort;
	private OVXBabySwitch dstSwitch;
	private OVXBabyPort dstPort;
	
	public OVXBabyLink (int linkId, OVXBabySwitch srcSwitch, OVXBabyPort srcPort,
			OVXBabySwitch dstSwitch, OVXBabyPort dstPort) {
		this.linkId = linkId;
		this.srcSwitch = srcSwitch;
		this.srcPort = srcPort;
		this.dstSwitch = dstSwitch;
		this.dstPort = dstPort;
		
        OVXMultiSwitch sw = this.srcSwitch.getParentSwitch();
        PlumbingGraph graph = sw.getPlumbingGraph();

		this.srcSwitch.getParentSwitch().getPlumbingGraph().addEdge(
				srcSwitch.getSwitchId(), srcPort.getPortNumber(),
				dstSwitch.getSwitchId(), dstPort.getPortNumber());
	}

	public int getLinkId() {
		return this.linkId;
	}

	public OVXBabySwitch getSrcSwitch() {
		return this.srcSwitch;
	}
	
	public OVXBabyPort getSrcPort() {
		return this.srcPort;
	}
	
	public OVXBabySwitch getDstSwitch() {
		return this.dstSwitch;
	}
	
	public OVXBabyPort getDstPort() {
		return this.dstPort;
	}

}
