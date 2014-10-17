package edu.princeton.cs.hsa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class PlumbingGraph {
	
	public static final int PRIORITY_HOPS = 3;
	
	private Logger logger = LogManager.getLogger(PlumbingGraph.class.getName());
	
	private Map<Integer, PlumbingNode> nodes;
	public PolicyFlowTable flowTable;
	
	public PlumbingGraph() {
		this.nodes = new HashMap<Integer, PlumbingNode>();
		this.flowTable = new PolicyFlowTable();
	}
	
	public void createNodes (int count) {
		for (int i = 0; i < count; i++) {
			PlumbingNode node = new PlumbingNode(i, this);
			this.nodes.put(i, node);
		}
	}
	
	public void addNode (int id) {
		PlumbingNode node = new PlumbingNode(id, this);
		this.nodes.put(id, node);
	}
	
	public void addPort(int id, Short physicalPort) {
		this.nodes.get(id).addPort(physicalPort);
	}
	
	public void addEdge(int id1, short port1, int id2, short port2) {
		PlumbingNode node1 = this.nodes.get(id1);
		PlumbingNode node2 = this.nodes.get(id2);
		
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
