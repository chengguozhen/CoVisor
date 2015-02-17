package edu.princeton.cs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.lang.CloneNotSupportedException;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
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
import org.openflow.util.U16;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.OFStatisticsReply;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.statistics.OFAggregateStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFStatisticsType;

import edu.princeton.cs.hsa.PlumbingGraph;
import edu.princeton.cs.hsa.PlumbingSwitch;
import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyTree;

public class QueryManager {
    private Logger logger = LogManager.getLogger(QueryManager.class.getName());
    private PlumbingSwitch plumbingSwitch;
    // Most recent stats reply for each OFFlowMod, keyed by cookie.
    private Map<Long, OVXFlowStatisticsReply> cookieToStatsMap;
    private PlumbingGraph graph;
    private PhysicalSwitch physicalSwitch;

    private String logHeader = "";


    public QueryManager(PlumbingSwitch plumbingSwitch) {
	this.plumbingSwitch = plumbingSwitch;
	this.graph = this.plumbingSwitch.getPlumbingGraph();
	this.physicalSwitch = this.graph.getPhysicalSwitch();
	this.cookieToStatsMap = new HashMap<Long, OVXFlowStatisticsReply>();
    }

    public void handleStatsRequest(OFStatisticsRequest req, OVXSendMsg from) {
	int tid = ((OVXSwitch) from).getTenantId();

	if (req.getStatisticType() == OFStatisticsType.FLOW) {
	    this.logger.info("FLOW statistic.");
	    try {
		OFStatistics stat = req.getFirstStatistics();
		OFFlowStatisticsRequest flowStatReq =
		    (OFFlowStatisticsRequest) stat;
		OFStatisticsReply reply =
		    handleFlowStatsRequest(flowStatReq, req.getXid(), tid);
		logger.info("About to call from.sendMsg(reply, from) for " +
			    "from = " + from + "\n" + this.logHeader +
			    "reply = " + reply);
		logger.info("reply's statistics: " + reply.getStatistics());
		from.sendMsg(reply, from);
	    }
	  
	    catch (IllegalArgumentException e) {
		this.logger.warn("FLOW stats request should have " +
				 "only one message in its body.");
	    }
	}
    }

    private OFStatisticsReply handleFlowStatsRequest(OFFlowStatisticsRequest
						     flowStatsReq, int xid,
						     int tid) {
	logger.info("\n" + this.logHeader + "BEGIN handleFlowStatsRequest\n  " +
		    this.logHeader + "flowStatsReq =" +
		    flowStatsReq + "\n  " + this.logHeader + "xid = " + xid +
		    "\n  " + this.logHeader + "tid = " + tid);
	OFMatch match = flowStatsReq.getMatch();
	List<OVXFlowStatisticsReply> allReps =
	    this.physicalSwitch.getFlowStats(this.plumbingSwitch.id);
	updateCookieToStatsMap(allReps);
	/*
	 * Will be set as the statistics field of the OFStatisticsReply
	 * sent to the controller.
	 */
	List<OFFlowStatisticsReply> statistics =
	    new ArrayList<OFFlowStatisticsReply>();
	// Length of OFStatisticsReply we'll send to the controller.
	int length = 0;
	Map<OFFlowMod, List<OFFlowMod>> virtualToPhysicalFMMap =
	    this.plumbingSwitch.getVirtualToPhysicalFMMap();
	for (OFFlowMod fm : virtualToPhysicalFMMap.keySet()) {
	    boolean coveredMatch = match.covers(fm.getMatch());
	    Map<OFFlowMod, Integer> fmToControllerMap =
		this.plumbingSwitch.getFmToControllerMap();
	    boolean thisController = (fmToControllerMap.get(fm) == tid);
	    logger.info("coveredMatch: " + coveredMatch);
	    logger.info("thisController: " + thisController);
	    if (coveredMatch && thisController) {
		// Know which flow mods we want stats for.
		List<OFFlowMod> physFlowMods = virtualToPhysicalFMMap.get(fm);
		// Get those stats replies out of the big list.
		List<OVXFlowStatisticsReply> relevantReps =
		    getRepliesForPhysFlowMods(physFlowMods);
		/*
		 * Aggregate the counters in these replies into a single
		 * OFFlowStatisticsReply to be added to the statistics list
		 * of the single OFStatisticsReply to send to the controller.
		 */
		OFFlowStatisticsReply stat =
		    combineCounters(fm, relevantReps);
		statistics.add(stat);
		length += stat.getLength();
	    }
	}
	OFStatisticsReply reply = new OFStatisticsReply();
	reply.setXid(xid);
	reply.setStatisticType(OFStatisticsType.FLOW);
	reply.setStatistics(statistics);
	reply.setLengthU(OFStatisticsReply.MINIMUM_LENGTH + length);
	logger.info("\n" + this.logHeader + "END handleFlowStatsRequest("
		    + flowStatsReq + ")\n");
	return reply;
    }

    /*
     * Combine counters corresponding to a single virtual flow mod.
     * Should be represented as one OFFlowStatisticsReply in the statistics
     * field of the OFStatisticsReply returned to the controller.
     */
    private OFFlowStatisticsReply combineCounters(OFFlowMod fmFromController,
						  List<OVXFlowStatisticsReply>
						  statsFromPhysSwitch) {
	logger.info("\n" + this.logHeader + "BEGIN combineCounters\n  " +
		    this.logHeader + "fmFromController = " +
		    fmFromController + "\n  " + this.logHeader +
		    "statsFromPhysSwitch = " + statsFromPhysSwitch);
	OFFlowStatisticsReply stat = new OFFlowStatisticsReply();
	int packets = 0;
	int bytes = 0;
	for (OVXFlowStatisticsReply statReceived : statsFromPhysSwitch) {
	    logger.info("statReceived: " + statReceived);
	    packets += statReceived.getPacketCount();
	    bytes += statReceived.getByteCount();
	}
        stat.setPacketCount(packets);
	stat.setByteCount(bytes);
	stat.setActions(fmFromController.getActions());
	stat.setCookie(fmFromController.getCookie());
	stat.setMatch(fmFromController.getMatch());
	// Copied from OVXFlowStatisticsRequest.  Not sure if this is correct.
	stat.setLength(U16.t(OFFlowStatisticsReply.MINIMUM_LENGTH));
	for (OFAction act : stat.getActions()) {
	    stat.setLength(U16.t(stat.getLength() + act.getLength()));
	}
	logger.info("combined stat: " + stat);
	return stat;
    }

   
    private void updateCookieToStatsMap(List<OVXFlowStatisticsReply>
					allReps) {
	//logger.info("\n" + this.logHeader + "BEGIN updateCookieToStatsMap\n  "
	//	    + this.logHeader + "allReps = " + allReps);
	// logger.info("cookieToStatsMap before update:  " + this.cookieToStatsMap);
	for (OVXFlowStatisticsReply rep : allReps) {
	    this.cookieToStatsMap.put(rep.getCookie(), rep);
	}
	// logger.info("cookieToStatsMap after update:  " + this.cookieToStatsMap);
    }

    /*
     * Get stats replies for just physFlowMods out of list of replies for all
     * flow mods on the physical switch.
     */
    private List<OVXFlowStatisticsReply> getRepliesForPhysFlowMods
	(List<OFFlowMod> physFlowMods) {
	//logger.info("\n" + this.logHeader + "BEGIN getRepliesForPhysFlowMods\n"
	//	    + this.logHeader + "for physFlowMods = " + physFlowMods);
	List<OVXFlowStatisticsReply> relevantReps =
	    new ArrayList<OVXFlowStatisticsReply>();
	for (OFFlowMod physFm : physFlowMods) {
	    OVXFlowStatisticsReply rep = this.cookieToStatsMap.
		get(physFm.getCookie());
	    relevantReps.add(rep);
	}
	//logger.info("relevantReps: " + relevantReps);
	//logger.info("\n" + this.logHeader + "END getRepliesForPhysFlowMods\n");
	return relevantReps;
    }

    // fms contains fm1, using fmEquals equality method.
    public boolean fmListContains(OFFlowMod fm1, Collection<OFFlowMod> fms) {
	for (OFFlowMod fm2 : fms) {
	    if (fmEquals(fm1, fm2)) {
		return true;
	    }
	}
	return false;
    }

    // Flow mod equality, ignoring length, xid, version.
    public boolean fmEquals(OFFlowMod fm1, OFFlowMod fm2) {
	String reasons = "";
	boolean result = true;

        if (fm1 == fm2) {
            return true;
        }
	if (fm1.getLength() != fm2.getLength()) {
	    reasons += "fm1.getLength() = " + fm1.getLength() + ", ";
	    reasons += "fm2.getLength() = " + fm2.getLength() + "\n";
	    //result = false;
	}
	if (fm1.getVersion() != fm2.getVersion()) {
	    reasons += "fm1.getVersion() != fm2.getVersion()\n";
	    //result = false;
	}
	if (fm1.getXid() != fm2.getXid()) {
	    reasons += "fm1.getXid() = " + fm1.getXid() + ", ";
	    reasons += "fm2.getXid() = " + fm2.getXid() + "\n";
	    //result = false;
	}
        if (fm1.getActions() == null) {
            if (fm2.getActions() != null) {
		reasons += "fm1.getActions() == null and fm2.getActions() != null\n";
		result = false;
            }
        } else if (!fm1.getActions().equals(fm2.getActions())) {
	    reasons += "!fm1.getActions().equals(fm2.getActions())\n";
            result = false;
        }
        if (fm1.getBufferId() != fm2.getBufferId()) {
	    reasons += "fm1.getBufferId() != fm2.getBufferId()\n";
            result = false;
        }
        if (fm1.getCommand() != fm2.getCommand()) {
	    reasons += "fm1.getCommand() != fm2.getCommand()\n";
            result = false;
        }
        if (fm1.getCookie() != fm2.getCookie()) {
	    reasons += "fm1.getCookie() != fm2.getCookie()\n";
            result = false;
        }
        if (fm1.getFlags() != fm2.getFlags()) {
	    reasons += "fm1.getFlags() != fm2.getFlags()\n";
            result = false;
        }
        if (fm1.getHardTimeout() != fm2.getHardTimeout()) {
	    reasons += "fm1.getHardTimeout() != fm2.getHardTimeout()\n";
            result = false;
        }
        if (fm1.getIdleTimeout() != fm2.getIdleTimeout()) {
	    reasons += "fm1.getIdleTimeout() != fm2.getIdleTimeout()\n";
            result = false;
        }
        if (fm1.getMatch() == null) {
            if (fm2.getMatch() != null) {
		reasons += "fm1.getMatch() == null and fm2.getMatch() != null\n";
                result = false;
            }
        } else if (!fm1.getMatch().equals(fm2.getMatch())) {
	    reasons += "!fm1.getMatch().equals(fm2.getMatch())\n";
            result = false;
        }
        if (fm1.getOutPort() != fm2.getOutPort()) {
	    reasons += "fm1.getOutPort() != fm2.getOutPort()\n";
            result = false;
        }
        if (fm1.getPriority() != fm2.getPriority()) {
	    reasons += "fm1.getPriority() != fm2.getPriority()\n";
            result = false;
        }

	/*if (result != fm1.equals(fm2)) {
	    logger.info("fmEquals(" + fm1 + ", " + fm2 + ") = " + result);
	    logger.info("fm1.equals(fm2) = " + fm1.equals(fm2));
	    logger.info(reasons);
	    }*/
        return result;
    }

}
