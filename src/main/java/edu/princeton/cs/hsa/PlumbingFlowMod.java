package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.adv.PolicyCompositionUtil;

public class PlumbingFlowMod extends OFFlowMod {
	
	private OFFlowMod originalOfm;
	private List<PlumbingFlowMod> prevPfms;
	private List<PlumbingFlowMod> nextPfms;
	private List<PlumbingFlow> prevPflows;
	private List<PlumbingFlow> nextPflows;
	private PlumbingNode pNode;
	
	public PlumbingFlowMod(final OFFlowMod fm, final PlumbingNode pNode) {
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
		
		this.originalOfm = fm;
		this.prevPfms = new ArrayList<PlumbingFlowMod>();
		this.nextPfms = new ArrayList<PlumbingFlowMod>();
		this.prevPflows = new ArrayList<PlumbingFlow>();
		this.nextPflows = new ArrayList<PlumbingFlow>();
		this.pNode = pNode;
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
		return this.prevPflows;
	}
	
	public Collection<PlumbingFlow> getNextPFlows() {
		return this.nextPflows;
	}
	
	public void addPrevPFlow(PlumbingFlow pflow) {
		this.prevPflows.add(pflow);
	}
	
	public void addNextPFlow(PlumbingFlow pflow) {
		this.nextPflows.add(pflow);
	}
	
	public OFFlowMod getOriginalOfm() {
		return this.originalOfm;
	}
	
	public PlumbingNode getPlumbingNode () {
		return this.pNode;
	}

}
