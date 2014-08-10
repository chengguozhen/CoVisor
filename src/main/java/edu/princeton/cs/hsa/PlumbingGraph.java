package edu.princeton.cs.hsa;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlumbingGraph {
	
	public static final int PRIORITY_HOPS = 3;
	
	private Logger logger = LogManager.getLogger(PlumbingGraph.class.getName());
	
	private Map<Long, PlumbingNode> nodes;
	
	public PlumbingGraph() {
		this.nodes = new HashMap<Long, PlumbingNode>();
	}
	
	public void addNode(long dpid) {
		PlumbingNode node = new PlumbingNode(dpid);
		this.nodes.put(dpid, node);
	}
	
	public void addPort(long dpid, short port, boolean isEdge) {
		this.nodes.get(dpid).addPort(port, isEdge);
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

}
