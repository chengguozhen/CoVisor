package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.elements.address.PhysicalIPAddress;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;

import com.google.common.collect.Multiset.Entry;
import com.googlecode.concurrenttrees.common.KeyValuePair;

import edu.princeton.cs.policy.adv.PolicyCompositionUtil;
import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;

public class PlumbingNode {
	
	//private OVXBabySwitch babySwitch;
	public final long dpid;
	private PolicyFlowTable flowTable;
	private Map<Short, Boolean> isEdgePortMap;
	private Map<Short, PlumbingNode> nextHopMap;
	private Map<Short, Short> nextHopPortMap;
	
	public PlumbingNode(long dpid) {
		this.dpid = dpid;
		this.flowTable = new PolicyFlowTable();
		this.isEdgePortMap = new HashMap<Short, Boolean>();
		this.nextHopMap = new HashMap<Short, PlumbingNode>();
		this.nextHopPortMap = new HashMap<Short, Short>();
	}
	
	public void addPort(short port, boolean isEdge) {
		this.isEdgePortMap.put(port, isEdge);
	}
	
	public void addNextHop(short port, PlumbingNode nextHop, short nextHopPort) {
		this.nextHopMap.put(port, nextHop);
		this.nextHopPortMap.put(port, nextHopPort);
	}
	
	public PolicyUpdateTable update(OFFlowMod ofm) {
		PlumbingFlowMod pfm = new PlumbingFlowMod(ofm, this);
		
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
		if (nextHop != null) {
			for (OFFlowMod nextOfm : nextHop.flowTable
					.getPotentialFlowMods(pfm)) {
				pfm.createFilter((PlumbingFlowMod) nextOfm);
			}
		}
		
		// update filter to ascendant
		for (PlumbingNode prevHop : this.getPrevHops()) {
			for (OFFlowMod prevOfm : prevHop.flowTable.getPotentialFlowMods(pfm)) {
				PlumbingFlowMod prevPfm = (PlumbingFlowMod) prevOfm;
				if (prevHop.getNextHop(prevPfm) == this) {
					prevPfm.createFilter(pfm);
				}
			}
		}
		
		// generate update flowmodes for edge port
		for (Map.Entry<Short, Boolean> isEdgePortPair : this.isEdgePortMap.entrySet()) {
			if (isEdgePortPair.getValue()) {
				OFMatch match = new OFMatch();
				int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_IN_PORT;
				match.setWildcards(wcards);
				match.setInputPort(isEdgePortPair.getKey());
				PlumbingFlow pflow = new PlumbingFlow(match, null, pfm, null);
				List<Tuple<OFFlowMod, Integer>> fmTuples = fwdPropagateFlow(pflow);
				//fmTuples = backPropagateFlow(fmTuples, null);
				for (Tuple<OFFlowMod, Integer> fmTuple : fmTuples) {
					updateTable.addFlowMods.add(fmTuple.first);
				}
			}
		}
		
		// generate update flowmods for non-edge port
		for (PlumbingFlowMod prevPfm : pfm.getPrevPFlowMods()) {
			for (PlumbingFlow prevPflow : prevPfm.getPrevPFlows()) {
				OFMatch match = PolicyCompositionUtil.actApplyMatch(prevPflow.getMatch(), prevPfm.getActions());
				if (PolicyCompositionUtil.intersectMatch(match, pfm.getMatch()) != null) {
					PlumbingFlow pflow = new PlumbingFlow(match, prevPfm, pfm, prevPflow);
					List<Tuple<OFFlowMod, Integer>> fmTuples = fwdPropagateFlow(pflow);
					//fmTuples = backPropagateFlow(fmTuples, prevPfm);
					for (Tuple<OFFlowMod, Integer> fmTuple : fmTuples) {
						updateTable.addFlowMods.add(fmTuple.first);
					}
				}
			}
		}

		return updateTable;
	}
	
	private Tuple<OFFlowMod, Integer> revertApplyFm(Tuple<OFFlowMod, Integer> fmTuple, PlumbingFlowMod pfm) {
		OFFlowMod ofm = fmTuple.first;
		Integer hop = fmTuple.second;
		
		// match
		OFMatch newMatch = PolicyCompositionUtil.intersectMatch(pfm.getMatch(),
				PolicyCompositionUtil.actRevertMatch(ofm.getMatch(), pfm.getActions()));
		ofm.setMatch(newMatch);
		
		// action
		for (OFAction action : pfm.getActions()) {
			if (action instanceof OFActionOutput) {
				continue;
			}
			ofm.getActions().add(action);
			ofm.setLengthU(ofm.getLengthU() + action.getLengthU());
		}
		
		// priority
		ofm.setPriority(
				(short) (pfm.getPriority()* PolicyCompositionUtil.SEQUENTIAL_SHIFT + ofm.getPriority()));
		
		return new Tuple<OFFlowMod, Integer>(ofm, hop + 1);
	}
	
	public List<Tuple<OFFlowMod, Integer>> fwdPropagateFlow (PlumbingFlow pflow) {
		List<Tuple<OFFlowMod, Integer>> fmTuples = new ArrayList<Tuple<OFFlowMod, Integer>>();
		
		OFMatch match = pflow.getMatch();
		PlumbingFlowMod pfm = pflow.getNextPFlowMod();
		
		if (this.isEdgePFlowMod(pfm)) {
			Tuple<OFFlowMod, Integer> fmTuple = null;
			try {
				fmTuple = new Tuple<OFFlowMod, Integer>(pfm.getOriginalOfm().clone(), 0);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			fmTuples.add(fmTuple);
			return fmTuples;
		}
		
		OFMatch nextMatch = PolicyCompositionUtil.intersectMatch(pfm.getMatch(),
				this.actApplyMatchWithInportChange(match, pfm.getActions()));
		for (PlumbingFlowMod nextPfm : pfm.getNextPFlowMods()) {
			
			if (PolicyCompositionUtil.intersectMatch(match, pfm.getMatch()) != null) {
				
				PlumbingFlow nextPflow = new PlumbingFlow(nextMatch, pfm, nextPfm, pflow);
				pflow.addNextPFlow(nextPflow);
				
				List<Tuple<OFFlowMod, Integer>> nextFmTuples = nextPfm.getPlumbingNode().fwdPropagateFlow(nextPflow);
				for (Tuple<OFFlowMod, Integer> nextFmTuple : nextFmTuples) {
					fmTuples.add(revertApplyFm(nextFmTuple, pfm));
				}
			}
			
		}
		
		return fmTuples;
	}
	
	private OFMatch actApplyMatchWithInportChange(OFMatch match,
			List<OFAction> actions) {
		OFMatch m = match.clone();
		for (OFAction action : actions) {
			if (action instanceof OFActionNetworkLayerDestination) {
				OFActionNetworkLayerDestination modNwDst = (OFActionNetworkLayerDestination) action;
				m.setWildcards(m.getWildcards() & ~OFMatch.OFPFW_NW_DST_MASK);
				m.setNetworkDestination(modNwDst.getNetworkAddress());
			}
			if (action instanceof OFActionOutput) {
				short outport = ((OFActionOutput) action).getPort();
				m.setWildcards(m.getWildcards() & ~OFMatch.OFPFW_IN_PORT);
				m.setInputPort(this.nextHopPortMap.get(outport));
			}
		}
		return m;
	}

	public List<Tuple<OFFlowMod, Integer>> backPropagateFlow (List<Tuple<OFFlowMod, Integer>> fmTuples,
			PlumbingFlowMod pfm) {
		/*List<Tuple<OFFlowMod, Integer>> fmTuples = new ArrayList<Tuple<OFFlowMod, Integer>>();
		
		OFMatch m = PolicyCompositionUtil.actApplyMatch(match, pfm.getActions());
		for (PlumbingFlowMod nextPfm : pfm.getNextFlowMods()) {
			List<Tuple<OFFlowMod, Integer>> curFmTuples = this.fwdPropagateFlow(m, nextPfm);
			for (Tuple<OFFlowMod, Integer> curFmTuple : curFmTuples) {
				OFFlowMod composedFm = PolicyCompositionUtil.sequentialComposition(pfm, curFmTuple.first);
				if (composedFm != null) {
					fmTuples.add(new Tuple<OFFlowMod, Integer>(composedFm, curFmTuple.second + 1));
				}
			}
		}*/
		
		return fmTuples;
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

	private boolean isEdgePFlowMod(PlumbingFlowMod pfm) {
		if (pfm.getActions().isEmpty()) {
			return true;
		}
		
		for (OFAction action : pfm.getActions()) {
			if (action instanceof OFActionOutput) {
				short outport = ((OFActionOutput) action).getPort();
				return this.isEdgePortMap.get(outport);
			}
		}
		return false;
	}
	
}
