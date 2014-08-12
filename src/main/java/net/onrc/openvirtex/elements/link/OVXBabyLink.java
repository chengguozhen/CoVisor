package net.onrc.openvirtex.elements.link;

import net.onrc.openvirtex.elements.datapath.OVXMultiSwitch;

public class OVXBabyLink {
	
	public final int linkId;
	public final long srcSwitch;
	public final short srcPort;
	public final long dstSwitch;
	public final short dstPort;
	
	public OVXBabyLink (int linkId, long srcSwitch, short srcPort,
			long dstSwitch, short dstPort, OVXMultiSwitch multiSwitch) {
		this.linkId = linkId;
		this.srcSwitch = srcSwitch;
		this.srcPort = srcPort;
		this.dstSwitch = dstSwitch;
		this.dstPort = dstPort;

		multiSwitch.getPlumbingGraph().addEdge(srcSwitch, srcPort, dstSwitch, dstPort);
	}

}
