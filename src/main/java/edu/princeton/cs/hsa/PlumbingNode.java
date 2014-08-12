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
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.adv.PolicyCompositionUtil;
import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;

public class PlumbingNode {
	
	//private OVXBabySwitch babySwitch;
	public final long dpid;
	private PolicyFlowTable flowTable;
	//private Map<Short, Boolean> isEdgePortMap;
	private Map<Short, Short> portMap; // virtual port -> physical port, null if it is an internal port
	private Map<Short, PlumbingNode> nextHopMap;
	private Map<Short, Short> nextHopPortMap;
	
	public PlumbingNode(long dpid) {
		this.dpid = dpid;
		this.flowTable = new PolicyFlowTable();
		//this.isEdgePortMap = new HashMap<Short, Boolean>();
		this.portMap = new HashMap<Short, Short>();
		this.nextHopMap = new HashMap<Short, PlumbingNode>();
		this.nextHopPortMap = new HashMap<Short, Short>();
	}
	
	public void addPort(Short port, Short physicalPort) {
		this.portMap.put(port, physicalPort);
	}
	
	public void addNextHop(short port, PlumbingNode nextHop, short nextHopPort) {
		this.nextHopMap.put(port, nextHop);
		this.nextHopPortMap.put(port, nextHopPort);
	}
	
	public Short getPhysicalPort(short port) {
		return this.portMap.get(port);
	}
	
	public PolicyUpdateTable update(OFFlowMod ofm) {
		PlumbingFlowMod pmod = new PlumbingFlowMod(ofm, this);
		
		// update flow table for this node
		this.flowTable.update(pmod);
		
		// calculate the update table for the real switch
		switch (pmod.getCommand()) {
        case OFFlowMod.OFPFC_ADD:
            return doFlowModAdd(pmod);
        case OFFlowMod.OFPFC_MODIFY:
        case OFFlowMod.OFPFC_MODIFY_STRICT:
            throw new NotImplementedException("don't allow OFPFC_MODIFY and OFPFC_MODIFY_STRICT");
        case OFFlowMod.OFPFC_DELETE:
        case OFFlowMod.OFPFC_DELETE_STRICT:
            return doFlowModDelete(pmod);
        default:
            return null;
		}
	}
	
	public PolicyUpdateTable doFlowModAdd(PlumbingFlowMod pmod) {
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		
		// update filter to descendant
		PlumbingNode nextHop = this.getNextHop(pmod);
		if (nextHop != null) {
			for (OFFlowMod nextOfm : nextHop.flowTable
					.getPotentialFlowMods(pmod)) {
				pmod.createFilter((PlumbingFlowMod) nextOfm);
			}
		}
		
		// update filter to ascendant
		for (PlumbingNode prevHop : this.getPrevHops()) {
			for (OFFlowMod prevOfm : prevHop.flowTable.getPotentialFlowMods(pmod)) {
				PlumbingFlowMod prevPmod = (PlumbingFlowMod) prevOfm;
				if (prevHop.getNextHop(prevPmod) == this) {
					prevPmod.createFilter(pmod);
				}
			}
		}
		
		// generate update flowmodes for edge port
		for (Map.Entry<Short, Short> portPair : this.portMap.entrySet()) {
			if (portPair.getValue() != null) {
				OFMatch match = new OFMatch();
				int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_IN_PORT;
				match.setWildcards(wcards);
				match.setInputPort(portPair.getValue());
				PlumbingFlow pflow = new PlumbingFlow(match, null, pmod, null);
				List<Tuple<OFFlowMod, Integer>> fmTuples = fwdPropagateFlow(pflow);
				fmTuples = backPropagateFlow(fmTuples, pflow);
				for (Tuple<OFFlowMod, Integer> fmTuple : fmTuples) {
					updateTable.addFlowMods.add(fmTuple.first);
				}
			}
		}
		
		// generate update flowmods for non-edge port
		for (PlumbingFlowMod prevPmod : pmod.getPrevPMods()) {
			for (PlumbingFlow prevPflow : prevPmod.getPrevPFlows()) {
				OFMatch match = PolicyCompositionUtil.actApplyMatch(
						PolicyCompositionUtil.intersectMatch(prevPmod.getMatch(), prevPflow.getMatch()),
						prevPmod.getActions());
				if (PolicyCompositionUtil.intersectMatch(match, pmod.getMatch()) != null) {
					PlumbingFlow pflow = new PlumbingFlow(match, prevPmod, pmod, prevPflow);
					List<Tuple<OFFlowMod, Integer>> fmTuples = fwdPropagateFlow(pflow);
					fmTuples = backPropagateFlow(fmTuples, pflow);
					for (Tuple<OFFlowMod, Integer> fmTuple : fmTuples) {
						updateTable.addFlowMods.add(fmTuple.first);
					}
				}
			}
		}

		return updateTable;
	}
	
	private Tuple<OFFlowMod, Integer> revertApplyFm(Tuple<OFFlowMod, Integer> fmTuple, PlumbingFlowMod pmod) {
		OFFlowMod ofm = fmTuple.first;
		Integer hop = fmTuple.second;
		
		// match
		OFMatch newMatch = PolicyCompositionUtil.intersectMatch(pmod.getMatch(),
				PolicyCompositionUtil.actRevertMatch(ofm.getMatch(), pmod.getActions()));
		ofm.setMatch(newMatch);
		
		// action
		for (OFAction action : pmod.getActions()) {
			if (action instanceof OFActionOutput) {
				continue;
			}
			ofm.getActions().add(action);
			ofm.setLengthU(ofm.getLengthU() + action.getLengthU());
		}
		
		// priority
		ofm.setPriority(
				(short) (pmod.getPriority() * vanillaPow(PolicyCompositionUtil.SEQUENTIAL_SHIFT, hop)
						+ ofm.getPriority()));
		
		return new Tuple<OFFlowMod, Integer>(ofm, hop + 1);
	}
	
	private List<Tuple<OFFlowMod, Integer>> fwdPropagateFlow (PlumbingFlow pflow) {
		List<Tuple<OFFlowMod, Integer>> fmTuples = new ArrayList<Tuple<OFFlowMod, Integer>>();
		
		OFMatch match = pflow.getMatch();
		PlumbingFlowMod pmod = pflow.getNextPMod();
		
		if (this.isEdgePFlowMod(pmod)) {
			Tuple<OFFlowMod, Integer> fmTuple = null;
			try {
				// TODO: add port transformation here
				fmTuple = new Tuple<OFFlowMod, Integer>(pmod.getOriginalOfm().clone(), 1);
				this.updateActionOutputPort(fmTuple.first);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			fmTuples.add(fmTuple);
			return fmTuples;
		}
		
		OFMatch nextMatch = this.actApplyMatchWithInportChange(
				PolicyCompositionUtil.intersectMatch(match, pmod.getMatch()),
				pmod.getActions());
		for (PlumbingFlowMod nextPmod : pmod.getNextPMods()) {
			
			if (PolicyCompositionUtil.intersectMatch(nextMatch, nextPmod.getMatch()) != null) {
				
				PlumbingFlow nextPflow = new PlumbingFlow(nextMatch, pmod, nextPmod, pflow);
				pflow.addNextPFlow(nextPflow);
				
				List<Tuple<OFFlowMod, Integer>> nextFmTuples = nextPmod.getPlumbingNode().fwdPropagateFlow(nextPflow);
				for (Tuple<OFFlowMod, Integer> nextFmTuple : nextFmTuples) {
					fmTuples.add(this.revertApplyFm(nextFmTuple, pmod));
				}
			}
			
		}
		
		return fmTuples;
	}
	
	private void updateActionOutputPort(OFFlowMod fm) {
		for (OFAction action : fm.getActions()) {
			if (action instanceof OFActionOutput) {
				OFActionOutput actionOutput = (OFActionOutput) action;
				short outport = actionOutput.getPort();
				Short physicalOutPort = this.getPhysicalPort(outport);
				if (physicalOutPort != null) {
					actionOutput.setPort(physicalOutPort);
				}
			}
		}
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

	private List<Tuple<OFFlowMod, Integer>> backPropagateFlow (List<Tuple<OFFlowMod, Integer>> fmTuples,
			PlumbingFlow pflow) {
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
		
		List<Tuple<OFFlowMod, Integer>> curFmTuples = null;
		PlumbingFlow curFlow = pflow;
		PlumbingFlowMod prevPmod = curFlow.getPrevPMod();
		while (prevPmod != null) {
			
			curFmTuples = new ArrayList<Tuple<OFFlowMod, Integer>>();
			for (Tuple<OFFlowMod, Integer> fmTuple : fmTuples) {
				curFmTuples.add(prevPmod.getPlumbingNode().revertApplyFm(fmTuple, prevPmod));
			}
			fmTuples = curFmTuples;
			
			curFlow = curFlow.getPrevPFlow();
			prevPmod = curFlow.getPrevPMod();
		}
		

		curFmTuples = new ArrayList<Tuple<OFFlowMod, Integer>>();
		for (Tuple<OFFlowMod, Integer> fmTuple : fmTuples) {
			OFFlowMod ofm = fmTuple.first;
			ofm.setMatch(
					PolicyCompositionUtil.intersectMatch(
							ofm.getMatch(), pflow.getMatch()));
			Integer hop = fmTuple.second;
			if (hop < PlumbingGraph.PRIORITY_HOPS) {
				ofm.setPriority(
						(short) (ofm.getPriority() * vanillaPow(
								PolicyCompositionUtil.SEQUENTIAL_SHIFT,
								PlumbingGraph.PRIORITY_HOPS - hop)));
			}
		}
		
		return fmTuples;
	}
	
	private int vanillaPow(int num, int power) {
		if (power == 0) {
			return 1;
		} else if (power == 1) {
			return num;
		} else if (power == 2) {
			return num * num;
		} else if (power == 3) {
			return num * num * num;
		} else if (power == 4) {
			return num * num * num * num;
		} else if (power == 5) {
			return num * num * num * num * num;
		} else{
			return (int) Math.pow(num, power);
		}
	}
	
	private PlumbingNode getNextHop(PlumbingFlowMod pmod) {
		for (OFAction action : pmod.getActions()) {
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

	public PolicyUpdateTable doFlowModDelete(PlumbingFlowMod pmod) {
		throw new NotImplementedException("don't allow OFPFC_MODIFY and OFPFC_MODIFY_STRICT");
	}

	private boolean isEdgePFlowMod(PlumbingFlowMod pmod) {
		if (pmod.getActions().isEmpty()) {
			return true;
		}
		
		for (OFAction action : pmod.getActions()) {
			if (action instanceof OFActionOutput) {
				short outport = ((OFActionOutput) action).getPort();
				return this.portMap.get(outport) != null;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		String str = "" + this.dpid + "\n";
		for (short port : this.portMap.keySet()) {
			if (this.portMap.get(port) != null) {
				str = str + "\t" + port + " -> physical:" + this.portMap.get(port) + "\n";
			} else if (this.nextHopMap.get(port) != null) {
				str = str + "\t" + port + " -> " + this.nextHopMap.get(port).dpid + ":" + this.nextHopPortMap.get(port) + "\n";
			} else {
				str = str + "\t" + port + " -> internal\n";
			}
		}
		return str;
	}
}
