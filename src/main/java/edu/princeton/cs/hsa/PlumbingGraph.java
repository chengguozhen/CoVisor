package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyUpdateTable;

public class PlumbingGraph {
	
	public static final int PRIORITY_HOPS = 3;
	
	private Logger logger = LogManager.getLogger(PlumbingGraph.class.getName());
	
	private List<PlumbingNode> nodes;
	private List<PlumbingEdge> edges;
	
	public PlumbingGraph() {
		this.nodes = new ArrayList<PlumbingNode>();
		this.edges = new ArrayList<PlumbingEdge>();
	}

}
