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
		if (prevPfm != null) {
			prevPfm.addNextPFlow(this);
		}
		this.nextPfm = nextPfm;
		if (nextPfm != null) {
			nextPfm.addPrevPFlow(this);
		}
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
	
	public void addNextPFlow(PlumbingFlow pflow) {
		this.nextPflow.add(pflow);
	}
}
