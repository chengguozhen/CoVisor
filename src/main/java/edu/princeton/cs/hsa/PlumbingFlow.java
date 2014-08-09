package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openflow.protocol.OFMatch;

public class PlumbingFlow {
	
	private OFMatch match;
	private PlumbingFlowMod prevPfm;
	private PlumbingFlowMod nextPfm;
	private PlumbingFlow prevPflow;
	private List<PlumbingFlow> nextPflow;
	
	
	public PlumbingFlow (OFMatch match, PlumbingFlowMod prevPfm,
			PlumbingFlowMod nextPfm, PlumbingFlow prevPflow) {
		this.match = match;
		this.prevPfm = prevPfm;
		this.nextPfm = nextPfm;
		this.prevPflow = prevPflow;
		this.nextPflow = new ArrayList<PlumbingFlow>();
	}
	
	public OFMatch getMatch() {
		return this.match;
	}
	
	public PlumbingFlowMod getPrevPFlowMod() {
		return this.prevPfm;
	}
	
	public PlumbingFlowMod getNextPFlowMod() {
		return this.nextPfm;
	}
	
	public PlumbingFlow getPrevPFlow() {
		return this.prevPflow;
	}
	
	public Collection<PlumbingFlow> getNextPFlows() {
		return this.nextPflow;
	}
}
