package edu.princeton.cs.hsa;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;

public class PlumbingGraph {
	
	public static final int PRIORITY_HOPS = 3;
	
	private Logger logger = LogManager.getLogger(PlumbingGraph.class.getName());
	
	private Map<Long, PlumbingNode> nodes;
	public PolicyFlowTable flowTable;
	
	public PlumbingGraph() {
		this.nodes = new HashMap<Long, PlumbingNode>();
		this.flowTable = new PolicyFlowTable();
	}
	
	public void addNode(long dpid) {
		PlumbingNode node = new PlumbingNode(dpid, this);
		this.nodes.put(dpid, node);
	}
	
	public void addNode(PlumbingNode node) {
		this.nodes.put(node.dpid, node);
		node.graph = this;
	}
	
	public void addPort(long dpid, Short port, Short physicalPort) {
		this.nodes.get(dpid).addPort(port, physicalPort);
	}
	
	public void addEdge(long dpid1, short port1, long dpid2, short port2) {
		PlumbingNode node1 = this.nodes.get(dpid1);
		PlumbingNode node2 = this.nodes.get(dpid2);
		
		node1.addNextHop(port1, node2, port2);
		node2.addNextHop(port2, node1, port1);
	}
	
	public PlumbingNode getNode(long dpid) {
		return this.nodes.get(dpid);
	}
	
	public PolicyUpdateTable update(OFFlowMod ofm, PlumbingNode node) {
		PolicyUpdateTable updateTable = node.update(ofm);
		for (OFFlowMod fm : updateTable.addFlowMods) {
			this.flowTable.addFlowMod(fm);
		}
		return updateTable;
	}
	
	public String getGraphString() {
		String str = "";
		for (PlumbingNode node : this.nodes.values()) {
			str = str + node.toString();
		}
		return str;
	}
	
	@Override
    public String toString() {
		String str = "Flow Table:\n";
		for (OFFlowMod fm : this.flowTable.getFlowModsSortByInport()) {
			str = str + fm.toString() + "\n";
		}
		
		/*for (PlumbingNode node : this.nodes.values()) {
			str = str + node.dpid + "\n" + node.getFlowModString();
		}*/
		return str;
	}

}
