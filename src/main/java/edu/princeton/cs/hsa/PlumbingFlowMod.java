package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;

import edu.princeton.cs.policy.adv.PolicyCompositionUtil;

public class PlumbingFlowMod extends OFFlowMod {
	
	private List<PlumbingFlowMod> prevPfms;
	private List<PlumbingFlowMod> nextPfms;
	private Map<PlumbingFlowMod, PlumbingFlow> prevPflows;
	private Map<PlumbingFlowMod, PlumbingFlow> nextPflows;
	
	public PlumbingFlowMod(final OFFlowMod fm) {
		super();
		this.match = fm.getMatch();
		this.cookie = fm.getCookie();
		this.command = fm.getCommand();
		this.idleTimeout = fm.getIdleTimeout();
		this.hardTimeout = fm.getHardTimeout();
		this.priority = fm.getPriority();
		this.bufferId = fm.getBufferId();
		this.outPort = fm.getOutPort();
		this.flags = fm.getFlags();
		this.actions = fm.getActions();
		
		this.prevPfms = new ArrayList<PlumbingFlowMod>();
		this.nextPfms = new ArrayList<PlumbingFlowMod>();
	}

	public void createFilter(PlumbingFlowMod nextFm) {
		OFMatch match = PolicyCompositionUtil.intersectMatch(this.getMatch(),
				PolicyCompositionUtil.actRevertMatch(nextFm.getMatch(), this.getActions()));
		if(match != null) {
			this.nextPfms.add(nextFm);
			nextFm.prevPfms.add(this);
		}
	}
	
	public Collection<PlumbingFlowMod> getPrevPFlowMods() {
		return this.prevPfms;
	}

	public Collection<PlumbingFlowMod> getNextPFlowMods() {
		return this.nextPfms;
	}
	
	public Collection<PlumbingFlow> getPrevPFlows() {
		return this.prevPflows.values();
	}
	
	public Collection<PlumbingFlow> getNextPFlows() {
		return this.nextPflows.values();
	}
	
	

}
