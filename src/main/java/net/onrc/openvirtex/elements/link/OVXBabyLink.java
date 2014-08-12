package net.onrc.openvirtex.elements.link;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.princeton.cs.hsa.PlumbingGraph;
import net.onrc.openvirtex.elements.datapath.OVXMultiSwitch;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.port.OVXBabyPort;

public class OVXBabyLink {
	
	private static Logger log = LogManager.getLogger(OVXBabyLink.class.getName());
	
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
        log.error(graph.getGraph());
        
        long src = srcSwitch.getSwitchId();
        short sport = srcPort.getPortNumber();
        long dst = dstSwitch.getSwitchId();
        short dport = dstPort.getPortNumber();

		this.srcSwitch.getParentSwitch().getPlumbingGraph().addEdge(src, sport, dst, dport);
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
