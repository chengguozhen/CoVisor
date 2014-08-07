package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.adv.PolicyCompositionUtil;
import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;

public class PlumbingNode {
	
	private PolicyFlowTable flowTable;
	private Map<Short, PlumbingNode> nextHopMap;
	
	public PlumbingNode() {
		this.flowTable = new PolicyFlowTable();
		this.nextHopMap = new HashMap<Short, PlumbingNode>();
	}
	
	public PolicyUpdateTable update(OFFlowMod ofm) {
		PlumbingFlowMod pfm = new PlumbingFlowMod(ofm);
		
		// update flow table for this node
		this.flowTable.update(pfm);
		
		// calculate the update table for the real switch
		switch (pfm.getCommand()) {
        case OFFlowMod.OFPFC_ADD:
            return doFlowModAdd(pfm);
        case OFFlowMod.OFPFC_MODIFY:
        case OFFlowMod.OFPFC_MODIFY_STRICT:
            throw new NotImplementedException("don't allow OFPFC_MODIFY and OFPFC_MODIFY_STRICT");
        case OFFlowMod.OFPFC_DELETE:
        case OFFlowMod.OFPFC_DELETE_STRICT:
            return doFlowModDelete(pfm);
        default:
            return null;
		}
	}
	
	public PolicyUpdateTable doFlowModAdd(PlumbingFlowMod pfm) {
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		
		// update filter to descendant
		PlumbingNode nextHop = this.getNextHop(pfm);
		for (OFFlowMod nextOfm : nextHop.flowTable.getPotentialFlowMods(pfm)) {
			pfm.createFilter((PlumbingFlowMod) nextOfm);
		}
		
		// update filter to ascendant
		for (PlumbingNode prevHop : this.getPrevHops()) {
			for (OFFlowMod prevOfm : prevHop.flowTable.getPotentialFlowMods(pfm)) {
				boolean isIntersected = ((PlumbingFlowMod) prevOfm).createFilter(pfm);
				if (isIntersected) {
					/*
					 * for flow : prevFm.getFlows
					 * 		apply prevFm to flow
					 * 		try flow with fm
					 * 		if true
					 * 			propagate_helper
					 * 
					 * propagate_helper (flow, fm)
					 * 	apply fm to flow
					 * 	rules = progate_helper(flow, after)
					 */
				}
			}
		}
		
		return updateTable;
	}
	
	public List<OFFlowMod> propagateFlow (OFMatch match, PlumbingFlowMod pfm) {
		List<OFFlowMod> ofms = new ArrayList<OFFlowMod>();
		
		OFMatch m = PolicyCompositionUtil.actApplyMatch(match, pfm.getActions());
		for (PlumbingFlowMod nextPfm : pfm.getNextFlowMods()) {
			List<OFFlowMod> curOfms = this.propagateFlow(m, nextPfm);
		}
		
		return null;
	}
	
	private PlumbingNode getNextHop(PlumbingFlowMod fm) {
		for (OFAction action : fm.getActions()) {
			if (action instanceof OFActionOutput) {
				short outport = ((OFActionOutput) action).getPort();
				return this.nextHopMap.get(outport);
			}
		}
		return null;
	}
	
	private Collection<PlumbingNode> getPrevHops() {
		return this.nextHopMap.values();
	}

	public PolicyUpdateTable doFlowModDelete(PlumbingFlowMod fm) {
		throw new NotImplementedException("don't allow OFPFC_MODIFY and OFPFC_MODIFY_STRICT");
	}

}
