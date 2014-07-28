package edu.princeton.cs.policy.ofwrapper;

import org.openflow.protocol.OFFlowMod;

public class OFFlowModWrapper {
	
	public final OFFlowMod flowMod;
	public final OFMatchWrapper matchWrapper;
	
	public OFFlowModWrapper (OFFlowMod flowMod) {
		this.flowMod = flowMod;
		this.matchWrapper = new OFMatchWrapper(flowMod.getMatch());
	}

}
