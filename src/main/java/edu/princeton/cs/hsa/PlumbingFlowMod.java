package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;

import edu.princeton.cs.policy.adv.PolicyCompositionUtil;

public class PlumbingFlowMod extends OFFlowMod {
	
	private List<PlumbingFlowMod> prevFlowMods;
	private List<PlumbingFlowMod> nextFlowMods;
	
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
		
		this.prevFlowMods = new ArrayList<PlumbingFlowMod>();
		this.nextFlowMods = new ArrayList<PlumbingFlowMod>();
	}

	public boolean createFilter(PlumbingFlowMod nextFm) {
		OFMatch match = PolicyCompositionUtil.intersectMatch(this.getMatch(),
				PolicyCompositionUtil.actRevertMatch(nextFm.getMatch(), this.getActions()));
		if(match != null) {
			this.nextFlowMods.add(nextFm);
			nextFm.prevFlowMods.add(this);
			return true;
		}
		return false;
	}
	
	public List<PlumbingFlowMod> getNextFlowMods {
		return this.nextFlowMods;
	}

}
