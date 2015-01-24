package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.messages.statistics.OVXFlowStatisticsReply;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionDataLayerDestination;
import org.openflow.protocol.action.OFActionDataLayerSource;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionOutput;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.OFStatisticsReply;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.statistics.OFAggregateStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFStatisticsType;

import edu.princeton.cs.policy.adv.PolicyCompositionUtil;
import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class PlumbingSwitch implements OVXSendMsg {
    
    private Logger logger = LogManager.getLogger(PlumbingSwitch.class.getName());
    
    public final int id;
    public PlumbingGraph graph;
    private PolicyTree policyTree;
    private PolicyFlowTable flowTable;
    //private Map<Short, Boolean> isEdgePortMap;
    private Map<Short, Short> portMap; // virtual port -> physical port, null if it is an internal port
    private Map<Short, PlumbingSwitch> prevHopMap;
    private Map<Short, PlumbingSwitch> nextHopMap;
    private Map<Short, Short> nextHopPortMap;
    private int portNumber;

    /* Physical flow mods corresponding to flow mod from controller; combine
       statistics from these flow mods to respond to stats query. */
    private Map<OFFlowMod, List<OFFlowMod>> virtualToPhysicalFMMap = new HashMap<OFFlowMod,
	List<OFFlowMod>>();


    public PlumbingSwitch(int id, PlumbingGraph graph) {
	this.id = id;
	this.graph = graph;
	this.policyTree = null;
	this.flowTable = new PolicyFlowTable();
	//this.isEdgePortMap = new HashMap<Short, Boolean>();
	this.portMap = new HashMap<Short, Short>();
	this.prevHopMap = new HashMap<Short, PlumbingSwitch>();
	this.nextHopMap = new HashMap<Short, PlumbingSwitch>();
	this.nextHopPortMap = new HashMap<Short, Short>();
	this.portNumber = 0;
    }
    
    public PlumbingSwitch(int id, PlumbingGraph graph,
			  List<PolicyFlowModStoreType> storeTypes,
			  List<PolicyFlowModStoreKey> storeKeys) {
	this.id = id;
	this.graph = graph;
	this.policyTree = null;
	this.flowTable = new PolicyFlowTable(storeTypes, storeKeys, false);
	//this.isEdgePortMap = new HashMap<Short, Boolean>();
	this.portMap = new HashMap<Short, Short>();
	this.prevHopMap = new HashMap<Short, PlumbingSwitch>();
	this.nextHopMap = new HashMap<Short, PlumbingSwitch>();
	this.nextHopPortMap = new HashMap<Short, Short>();
    }
    
    public void setPolicyTree (PolicyTree policyTree) {
	this.policyTree = policyTree;
    }
    
    public short getNextPortNumber() {
	this.portNumber++;
	return (short) this.portNumber;
    }
    
    public void addPort(Short physicalPort) {
	this.portMap.put(this.getNextPortNumber(), physicalPort);
    }
    
    public void addNextHop(short port, PlumbingSwitch nextHop, short nextHopPort) {
	nextHop.prevHopMap.put(nextHopPort, this);
	this.nextHopMap.put(port, nextHop);
	this.nextHopPortMap.put(port, nextHopPort);
    }
    
    public Short getPhysicalPort(short port) {
	return this.portMap.get(port);
    }
    
    @Override
    public void sendMsg(final OFMessage msg, final OVXSendMsg from) {

	int tid = ((OVXSwitch) from).getTenantId();
	
	if (msg.getType() == OFType.FLOW_MOD) {
	    
	    //this.logger.info("{} get msg {}", this, msg);
	    OFFlowMod fmMsg = (OFFlowMod) msg;
	    PolicyUpdateTable updateTable1 = this.policyTree.
		update(fmMsg, tid);
	    PolicyUpdateTable updateTable2 = new PolicyUpdateTable();
			
	    for (OFFlowMod fm : updateTable1.addFlowMods) {
		PolicyUpdateTable partialUpdateTable = this.graph.update(fm, this);
		updateTable2.addUpdateTable(partialUpdateTable);
	    }
	    for (OFFlowMod fm : updateTable1.deleteFlowMods) {
		fm.setCommand(OFFlowMod.OFPFC_DELETE);
		PolicyUpdateTable partialUpdateTable = this.graph.update(fm, this);
		updateTable2.addUpdateTable(partialUpdateTable);
	    }
	    
	    for (OFFlowMod fm : updateTable2.addFlowMods) {
		this.graph.getPhysicalSwitch().sendMsg(fm, this);
	    }
	    for (OFFlowMod fm : updateTable2.deleteFlowMods) {
		fm.setCommand(OFFlowMod.OFPFC_DELETE);
		this.graph.getPhysicalSwitch().sendMsg(fm, this);
	    }

	    /* 23 January
	     * I think (but I'm not sure) that all controllers sending messages
	     * to this PlumbingSwitch share an identical view of the virtual
	     * network.  If this is the case, don't need to keep track of
	     * which controller sent which flow mod.  If not, need to do something
	     * more complicated than just flowModToControllerMap.
	     */
	    // Add fmMsg and its derived rules to map for answering queries.
	    logger.info(virtualToPhysicalFMMapString());
	    logger.info("adding key: " + fmMsg + "\nvalue: " + updateTable2.addFlowMods
			+ "\n");
	    this.virtualToPhysicalFMMap.put(fmMsg, updateTable2.addFlowMods);
	    logger.info(virtualToPhysicalFMMapString());
	    
            //this.logger.info("left child {}", this.policyTree.leftChild.flowTable);
            //this.logger.info("right child {}", this.policyTree.rightChild.flowTable);
	    //this.logger.info("plumbing {} flow table {}", this.id, this.policyTree.flowTable);
	    //this.logger.info("graph flow table {}", this.graph.flowTable);
	    
	} 

	else if (msg.getType() == OFType.STATS_REQUEST) {
	    // Devirtualize stats request using virtualToPhysicalFMMap.
	    this.logger.info("msg.getType() == OFType.STATS_REQUEST");
	    OFStatisticsRequest req = (OFStatisticsRequest) msg;
	    if (req.getStatisticType() == OFStatisticsType.FLOW) {
		this.logger.info("FLOW statistic.");
		try {
		    OFStatistics stat = req.getFirstStatistics();
		    OFFlowStatisticsRequest flowStatReq =
			(OFFlowStatisticsRequest) stat;
		    logger.info("Calling handleFlowStatsRequest for " +
				"flowStatReq = " + flowStatReq.toString() +
				" and tid = " + Integer.toString(tid));
		    this.handleFlowStatsRequest(flowStatReq, tid);
		}
		catch (IllegalArgumentException e) {
		    this.logger.warn("FLOW stats request should have " +
				     "only one message in its body.");
		}
	    }
	}
	//else if (msg.getType() == OFType.STATS_REPLY) {
	    /*
	     * Virtualize stats request using virtualToPhysicalFMMap.
	     * Need to aggregate counts from multiple physical rules.
	     */
	/*OFStatisticsReply rep = (OFStatisticsReply) msg;
	    if (rep.getStatisticType() == OFStatisticsType.AGGREGATE) {
		// Do aggregate stuff.
		return;
	    }
	    else if (rep.getStatisticType() == OFStatisticsType.FLOW) {
		// Do flow stuff.
		return;
	    }
	    }*/
	else {
	    this.logger.info("msg isn't flow mod or stats request.");
	    this.logger.info(msg.toString());
	    this.graph.getPhysicalSwitch().sendMsg(msg, this);
	}
	
    }
    
    private void handleFlowStatsRequest(OFFlowStatisticsRequest flowStatsReq,
					int tid) {
	logger.info("inside handleFlowStatsRequest");
	OFMatch match = flowStatsReq.getMatch();
	logger.info("match: " + match.toString());
	/* 21 January
	 * Get stuck here.  See covisor.log.  allReps is null.
	 */
	List<OVXFlowStatisticsReply> allReps =
	    this.graph.getPhysicalSwitch().getFlowStats(tid);
	if (allReps != null) {
	    logger.info("allReps: " + allReps.toString());
	}
	else {
	    logger.info("allReps is null.");
	}
	for (OFFlowMod fm : this.virtualToPhysicalFMMap.keySet()) {
	    logger.info("fm: " + fm);
	    boolean coveredMatch = match.covers(fm.getMatch());
	    //logger.info("fm match: " + fm.getMatch().toString());
	    //logger.info("coveredMatch: " + String.valueOf(coveredMatch));
	    if (coveredMatch) {
		// Know which flow mods we want stats for.
		logger.info("coveredMatch");
		List<OFFlowMod> physFlowMods = this.virtualToPhysicalFMMap.get(fm);
		logger.info("physFlowMods: " + physFlowMods);
		// Get those stats replies out of the big list.
		List<OVXFlowStatisticsReply> relevantReps =
		    getRepliesForPhysFlowMods(physFlowMods, allReps);
		// Aggregate the counters in these replies into a single reply
		// to send to the controller.
	    }
	}
	
	return;    
    }

    /*
     * Get stats replies for just physFlowMods out of list of replies for all
     * flow mods on the physical switch.
     */
    private List<OVXFlowStatisticsReply> getRepliesForPhysFlowMods
	(List<OFFlowMod> physFlowMods, List<OVXFlowStatisticsReply> allReps) {
	List<OVXFlowStatisticsReply> relevantReps =
	    new ArrayList<OVXFlowStatisticsReply>();
	if (allReps != null) {
	    // Match flow mods to replies by cookie.
	    // 21 JANUARY:  I'm not sure this is a valid strategy.
	    for (OFFlowMod physFlowMod : physFlowMods) {
		logger.info("physFlowMod:  %s", physFlowMod);
		long fmCookie = physFlowMod.getCookie();
		logger.info("fmCookie: %s", fmCookie);
		for (OVXFlowStatisticsReply flowStatRep : allReps) {
		    logger.info("flowStatRep: %s", flowStatRep);
		    logger.info("flowStatRep.getCookie(): %s", flowStatRep.getCookie());
		    if (flowStatRep.getCookie() == fmCookie) {
			logger.info("Cookies match.");
			relevantReps.add(flowStatRep);
		    }
		}
	    }
	}
	return relevantReps;
    }

    // Utility for debugging.
    /*
    private String flowModToControllerMapString() {
	String s = "flowModToControllerMap{";
	for (OFFlowMod entry : this.flowModToControllerMap.keySet()) {
	    s += "key: " + entry.toString();
	    s += "value: " + String.valueOf(flowModToControllerMap.get(entry));
	    s += "\n";
	}
	int size = flowModToControllerMap.size();
	if (size > 0){
	    s = s.substring(0, s.length() - 2);
	}
	s += "}\n\n";
	s += "size = " + String.valueOf(size);
	return s;
    }
    */
    // Utility for debugging.
    private String virtualToPhysicalFMMapString() {
	String s = "virtualToPhysicalFMMap{";
	for (OFFlowMod entry : virtualToPhysicalFMMap.keySet()) {
	    s += "key: " + entry.toString();
	    s += "value: " + String.valueOf(virtualToPhysicalFMMap.get(entry));
	    s += "\n";
	}
	int size = virtualToPhysicalFMMap.size();
	if (size > 0){
	    s = s.substring(0, s.length() - 2);
	}
	s += "}\n\n";
	s += "size = " + String.valueOf(size);
	return s;
    }
    
    public PolicyUpdateTable update(OFFlowMod ofm) {
	PlumbingFlowMod pmod = new PlumbingFlowMod(ofm, this);
	
	// update flow table for this node
	PolicyUpdateTable updateTable = this.flowTable.update(pmod);
	
	// calculate the update table for the real switch
	switch (pmod.getCommand()) {
        case OFFlowMod.OFPFC_ADD:
            return doFlowModAdd(pmod);
        case OFFlowMod.OFPFC_MODIFY:
        case OFFlowMod.OFPFC_MODIFY_STRICT:
            throw new NotImplementedException("don't allow OFPFC_MODIFY and OFPFC_MODIFY_STRICT");
        case OFFlowMod.OFPFC_DELETE:
        case OFFlowMod.OFPFC_DELETE_STRICT:
            return doFlowModDelete(pmod, updateTable);
        default:
            return null;
	}
    }
	
    public PolicyUpdateTable doFlowModAdd(PlumbingFlowMod pmod) {
	PolicyUpdateTable updateTable = new PolicyUpdateTable();
	
	// update filter to descendant
	PlumbingSwitch nextHop = this.getNextHop(pmod);
	if (nextHop != null) {
	    for (OFFlowMod nextOfm : nextHop.flowTable
		     .getPotentialFlowMods(pmod)) {
		pmod.createFilter((PlumbingFlowMod) nextOfm);
	    }
	}
	
	// update filter to ascendant
	for (PlumbingSwitch prevHop : this.getPrevHops(pmod)) {
	    for (OFFlowMod prevOfm : prevHop.flowTable.getPotentialFlowMods(pmod)) {
		PlumbingFlowMod prevPmod = (PlumbingFlowMod) prevOfm;
		if (prevHop.getNextHop(prevPmod) == this) {
		    prevPmod.createFilter(pmod);
		}
	    }
	}
	
	// generate update flowmods for edge port
	if ((pmod.getMatch().getWildcards() & OFMatch.OFPFW_IN_PORT) == 0) {
	    Short inport = pmod.getMatch().getInputPort();
	    Short physicalInPort = this.portMap.get(inport);
	    if (physicalInPort != null) {
		OFMatch match = new OFMatch();
		int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_IN_PORT;
		match.setWildcards(wcards);
		match.setInputPort(physicalInPort);
		PlumbingFlow pflow = new PlumbingFlow(match, null, pmod, null);
		List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> fmTuples = fwdPropagateFlow(pflow);
		fmTuples = backPropagateFlow(fmTuples, pflow);
		for (Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> fmTuple : fmTuples) {
		    OFFlowMod flowMod = fmTuple.first.first;
		    updateTable.addFlowMods.add(flowMod);
		    //System.out.println("checkpoint 1:" + flowMod);
		    for (PlumbingFlowMod pFlowMod : fmTuple.first.second) {
			pFlowMod.getPlumbingNode().flowTable.addGeneratedParentFlowMod(pFlowMod, flowMod);
		    }
		}
	    }
	} else {
	    for (Map.Entry<Short, Short> portPair : this.portMap.entrySet()) {
		if (portPair.getValue() != null) {
		    OFMatch match = new OFMatch();
		    int wcards = OFMatch.OFPFW_ALL & ~OFMatch.OFPFW_IN_PORT;
		    match.setWildcards(wcards);
		    match.setInputPort(portPair.getValue());
		    PlumbingFlow pflow = new PlumbingFlow(match, null, pmod, null);
		    List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> fmTuples = fwdPropagateFlow(pflow);
		    fmTuples = backPropagateFlow(fmTuples, pflow);
		    for (Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> fmTuple : fmTuples) {
			OFFlowMod flowMod = fmTuple.first.first;
			updateTable.addFlowMods.add(flowMod);
			//System.out.println("checkpoint 2:" + flowMod);
			for (PlumbingFlowMod pFlowMod : fmTuple.first.second) {
			    pFlowMod.getPlumbingNode().flowTable.addGeneratedParentFlowMod(pFlowMod, flowMod);
			}
		    }
		}
	    }
	}
	
	// generate update flowmods for non-edge port
	for (PlumbingFlowMod prevPmod : pmod.getPrevPMods()) {
	    for (PlumbingFlow prevPflow : prevPmod.getPrevPFlows()) {
		try {
		    if (prevPflow.getPrevPFlow().getPrevPMod().getPlumbingNode() == prevPflow.getNextPMod().getPlumbingNode()) {
			continue;
		    }
		} catch (NullPointerException e) {
		    ;
		}
		OFMatch match = prevPmod.getPlumbingNode().actApplyMatchWithInportChange(
		    PolicyCompositionUtil.intersectMatch(prevPmod.getMatch(), prevPflow.getMatch()),
		    prevPmod.getActions());
		if (PolicyCompositionUtil.intersectMatch(match, pmod.getMatch()) != null) {
		    /*System.out.println("\t" + prevPmod);
		      System.out.println("\t" + prevPflow.getMatch());
		      try {
		      System.out.println("\t" + prevPflow.getPrevPFlow().getPrevPMod().getPlumbingNode().dpid);
		      System.out.println("\t" + prevPflow.getNextPMod().getPlumbingNode().dpid);
		      } catch (NullPointerException e) {
		      ;
		      }
		      printX(prevPflow);*/
		    PlumbingFlow pflow = new PlumbingFlow(match, prevPmod, pmod, prevPflow);
		    List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> fmTuples = fwdPropagateFlow(pflow);
		    fmTuples = backPropagateFlow(fmTuples, pflow);
		    for (Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> fmTuple : fmTuples) {
			OFFlowMod flowMod = fmTuple.first.first;
			updateTable.addFlowMods.add(flowMod);
			//System.out.println("checkpoint 3:" + flowMod);
			for (PlumbingFlowMod pFlowMod : fmTuple.first.second) {
			    pFlowMod.getPlumbingNode().flowTable.addGeneratedParentFlowMod(pFlowMod, flowMod);
			}
		    }
		}
	    }
	}
	
	return updateTable;
    }
    
    private void printX(PlumbingFlow pflow) {
	while (pflow.getPrevPFlow() != null) {
	    System.out.println("\t\t" + pflow.getPrevPMod());
	    //System.out.println("\t\t" + pflow.getMatch());
	    pflow = pflow.getPrevPFlow();
	}
	System.out.println("\t\t" + pflow.getMatch());
    }
	
    private Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> revertApplyFm(
	Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> fmTuple,
	PlumbingFlowMod pmod) {
	OFFlowMod ofm = fmTuple.first.first;
	Integer hop = fmTuple.second;
	
	// match
	OFMatch newMatch = PolicyCompositionUtil.intersectMatchIgnoreInport(pmod.getMatch(),
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
	
	return new Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>(fmTuple.first, hop + 1);
    }
    
    private List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> fwdPropagateFlow (PlumbingFlow pflow) {
	List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>>  fmTuples = new ArrayList<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>>();
	
	OFMatch match = pflow.getMatch();
		PlumbingFlowMod pmod = pflow.getNextPMod();
		
		if (this.isEdgePFlowMod(pmod)) {
		    Tuple<OFFlowMod, List<PlumbingFlowMod>> fm = null;
		    try { 
			fm = new Tuple<OFFlowMod, List<PlumbingFlowMod>>(pmod.getOriginalOfm().clone(), new ArrayList<PlumbingFlowMod>());
			this.updateActionOutputPort(fm.first);
			fm.second.add(pmod);
		    } catch (CloneNotSupportedException e) {
			e.printStackTrace();
		    }
		    fmTuples.add(new Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>(fm, 1));
		    return fmTuples;
		}
		
		OFMatch nextMatch = this.actApplyMatchWithInportChange(
		    PolicyCompositionUtil.intersectMatchIgnoreInport(match, pmod.getMatch()),
		    pmod.getActions());
		for (PlumbingFlowMod nextPmod : pmod.getNextPMods()) {
		    
		    if (PolicyCompositionUtil.intersectMatch(nextMatch, nextPmod.getMatch()) != null) {
			
			PlumbingFlow nextPflow = new PlumbingFlow(nextMatch, pmod, nextPmod, pflow);
			pflow.addNextPFlow(nextPflow);
			
			List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> nextFmTuples = nextPmod.getPlumbingNode().fwdPropagateFlow(nextPflow);
			for (Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> nextFmTuple : nextFmTuples) {
			    if (nextFmTuple.second == PlumbingGraph.PRIORITY_HOPS) {
				continue;
			    }
			    fmTuples.add(this.revertApplyFm(nextFmTuple, pmod));
			    nextFmTuple.first.second.add(pmod);
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
	    } else if (action instanceof OFActionDataLayerSource) {
		OFActionDataLayerSource modDataSrc = (OFActionDataLayerSource) action;
		m.setWildcards(m.getWildcards() & ~OFMatch.OFPFW_DL_SRC);
		m.setDataLayerSource(modDataSrc.getDataLayerAddress());
	    } else if (action instanceof OFActionDataLayerDestination) {
		OFActionDataLayerDestination modDataDst = (OFActionDataLayerDestination) action;
		m.setWildcards(m.getWildcards() & ~OFMatch.OFPFW_DL_DST);
		m.setDataLayerDestination(modDataDst.getDataLayerAddress());
	    } else if (action instanceof OFActionOutput) {
		short outport = ((OFActionOutput) action).getPort();
		m.setWildcards(m.getWildcards() & ~OFMatch.OFPFW_IN_PORT);
		m.setInputPort(this.nextHopPortMap.get(outport));
	    }
	}
	return m;
    }
    
    private List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> backPropagateFlow (
	List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> fmTuples,
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
	
	List<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>> curFmTuples = null;
	PlumbingFlow curFlow = pflow;
	PlumbingFlowMod prevPmod = curFlow.getPrevPMod();
	while (prevPmod != null) {
	    
	    curFmTuples = new ArrayList<Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer>>();
	    for (Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> fmTuple : fmTuples) {
		if (fmTuple.second == PlumbingGraph.PRIORITY_HOPS) {
		    continue;
		}
		curFmTuples.add(prevPmod.getPlumbingNode().revertApplyFm(fmTuple, prevPmod));
		fmTuple.first.second.add(prevPmod);
	    }
	    fmTuples = curFmTuples;
	    
	    curFlow = curFlow.getPrevPFlow();
	    prevPmod = curFlow.getPrevPMod();
	}
	
	for (Tuple<Tuple<OFFlowMod, List<PlumbingFlowMod>>, Integer> fmTuple : fmTuples) {
	    OFFlowMod ofm = fmTuple.first.first;
	    OFMatch match = PolicyCompositionUtil.intersectMatchIgnoreInport(ofm.getMatch(), curFlow.getMatch());
	    match.setWildcards(match.getWildcards() & ~OFMatch.OFPFW_IN_PORT);
	    match.setInputPort(curFlow.getMatch().getInputPort());
	    
	    /*logger.error(fmTuple.first);
	      logger.error(curFlow.getMatch());
	      logger.error(match);*/
	    
	    ofm.setMatch(match);
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
	} else {
	    return (int) Math.pow(num, power);
	}
    }
    
    private PlumbingSwitch getNextHop(PlumbingFlowMod pmod) {
	for (OFAction action : pmod.getActions()) {
	    if (action instanceof OFActionOutput) {
		short outport = ((OFActionOutput) action).getPort();
		return this.nextHopMap.get(outport);
	    }
	}
	return null;
    }
    
    public Short getNextHopPort(PlumbingFlowMod pmod) {
	for (OFAction action : pmod.getActions()) {
	    if (action instanceof OFActionOutput) {
		short outport = ((OFActionOutput) action).getPort();
		return this.nextHopPortMap.get(outport);
	    }
	}
	return null;
    }
    
    private Collection<PlumbingSwitch> getPrevHops(PlumbingFlowMod pmod) {
	OFMatch match = pmod.getMatch();
	if ((match.getWildcards() & OFMatch.OFPFW_IN_PORT) == 0) {
	    List<PlumbingSwitch> prevHops = new ArrayList<PlumbingSwitch>();
	    PlumbingSwitch prevHop = this.prevHopMap.get(match.getInputPort());
	    if (prevHop != null) {
		prevHops.add(prevHop);
	    }
	    return prevHops;
	} else {
	    return this.prevHopMap.values();
	}
    }
    
    public PolicyUpdateTable doFlowModDelete(PlumbingFlowMod pmod, PolicyUpdateTable nodeUpdateTable) {
	
	PolicyUpdateTable updateTable = new PolicyUpdateTable();
	
	if (nodeUpdateTable.deleteFlowMods.size() == 0) {
	    return updateTable;
	}
	
	// TODO: clean delete, better index
	List<OFFlowMod> generatedParentFlowMods = this.flowTable.getGenerateParentFlowMods(nodeUpdateTable.deleteFlowMods.get(0));
	//List<OFFlowMod> deletedFlowMods = this.graph.flowTable.deleteFlowMods(generatedParentFlowMods);
	updateTable.deleteFlowMods.addAll(generatedParentFlowMods);
	
	return updateTable;
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
	String str = "" + this.id + "\n";
	for (short port : this.portMap.keySet()) {
	    if (this.portMap.get(port) != null) {
		str = str + "\t" + port + " -> physical:" + this.portMap.get(port) + "\n";
	    } else if (this.nextHopMap.get(port) != null) {
		str = str + "\t" + port + " -> " + this.nextHopMap.get(port).id + ":" + this.nextHopPortMap.get(port) + "\n";
	    } else {
		str = str + "\t" + port + " -> internal\n";
	    }
	}
	return str;
    }
    
    public String getFlowModString() {
	String str = "";
	for (OFFlowMod flowMod : this.flowTable.getFlowMods()) {
	    str = str + flowMod + "\n";
	    for (OFFlowMod fm : this.flowTable.getGenerateParentFlowMods(flowMod)) {
		str = str + "\t" + fm + "\n";
	    }
	}
	return str;
    }
    
    @Override
    public String getName() {
	return this.graph.getPhysicalSwitch().getName() + ":" + this.id;
    }
}
