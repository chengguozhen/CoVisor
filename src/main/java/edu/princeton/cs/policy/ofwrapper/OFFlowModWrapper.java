package edu.princeton.cs.policy.ofwrapper;

import org.openflow.protocol.OFFlowMod;

public class OFFlowModWrapper {
	
	public final OFFlowMod flowMod;
	public final OFMatchWrapper matchWrapper;
	public final short command;
	
	public OFFlowModWrapper (OFFlowMod flowMod, short command) {
		this.flowMod = flowMod;
		this.matchWrapper = new OFMatchWrapper(flowMod.getMatch());
		this.command = command;
	}

}
