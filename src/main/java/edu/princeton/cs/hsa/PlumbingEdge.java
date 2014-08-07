package edu.princeton.cs.hsa;

import org.openflow.protocol.OFFlowMod;

public class PlumbingEdge {
	
	public PlumbingNode srcNode;
	public short srcPort;
	public PlumbingNode dstNode;
	public short dstPort;
	public OFFlowMod srcFlowMod;
	public OFFlowMod dstFlowMod;
	
	public PlumbingEdge() {
		;
	}

}
