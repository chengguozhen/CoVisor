package net.onrc.openvirtex.elements.datapath;

import java.util.ArrayList;
import java.util.List;

import net.onrc.openvirtex.exceptions.SwitchMappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import edu.cs.princeton.cs.exprfl.VirtualTopoExprFl;
import edu.princeton.cs.hsa.PlumbingGraph;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;

/**
 * The Class OVXMultiSwitch.  Inherits from OVXSwitch, but also implements
 * many Network methods.
 */
public class OVXMultiSwitch extends OVXSingleSwitch {

	private static Logger logger = LogManager.getLogger(OVXMultiSwitch.class.getName());
	
	private List<OVXBabySwitch> babySwitches;
	private PlumbingGraph plumbingGraph;
	
	private List<OFFlowMod> fmFlowMods1;
    private List<OFFlowMod> fmFlowMods2;
    private List<OFFlowMod> fmFlowMods3;


	public OVXMultiSwitch(final Long switchId, final Integer tenantId) {
		super(switchId, tenantId);
		this.babySwitches = new ArrayList<OVXBabySwitch>();
		this.plumbingGraph = new PlumbingGraph();
		
		this.fmFlowMods1 = new ArrayList<OFFlowMod>();
		this.fmFlowMods2 = new ArrayList<OFFlowMod>();
		this.fmFlowMods3 = new ArrayList<OFFlowMod>();
	}
	
	public PlumbingGraph getPlumbingGraph() {
		return this.plumbingGraph;
	}

	public void addSwitch(OVXBabySwitch babySwitch) {
		this.babySwitches.add(babySwitch);
		this.plumbingGraph.addNode(babySwitch.getPlumbingNode());
	}

	public List<OVXBabySwitch> getSwitches() {
		return this.babySwitches;
	}
	
	public PhysicalSwitch getPhysicalSwitch() {
		PhysicalSwitch psw = null;
		try {
			psw = this.map.getPhysicalSwitches(this).get(0);
		} catch (SwitchMappingException e) {
			logger.warn("Cannot recover physical switch : {}", e);
		}
		return psw;
    }
	
	public void runExpr() {
		VirtualTopoExprFl expr = new VirtualTopoExprFl();
		//expr.startExpr(fmFlowMods1, fmFlowMods2, fmFlowMods3);
		logger.info("switch 1");
		for (OFFlowMod fm : fmFlowMods1) {
			logger.info(fm);
		}
		
		logger.info("switch 2");
		for (OFFlowMod fm : fmFlowMods2) {
			logger.info(fm);
		}
		
		logger.info("switch 3");
		for (OFFlowMod fm : fmFlowMods3) {
			logger.info(fm);
		}
	}

	public void sendSouth(final OFMessage msg, final OVXBabySwitch babySwitch) {
        //log.info("Sending packet to sw {}: {}", this.getPhysicalSwitch().getName(), msg);
        
        if (msg.getType() == OFType.FLOW_MOD) {

        	if (babySwitch.getTenantId() == 1) {
        		this.fmFlowMods1.add((OFFlowMod) msg);
        	} else if (babySwitch.getTenantId() == 2) {
        		this.fmFlowMods2.add((OFFlowMod) msg);
        	} else {
        		this.fmFlowMods3.add((OFFlowMod) msg);
        	}
        	
        	/*PolicyUpdateTable updateTable = this.plumbingGraph.update((OFFlowMod) msg, babySwitch.getPlumbingNode());
        	logger.info(this.plumbingGraph);
        	for (OFFlowMod ofm : updateTable.addFlowMods) {
        		this.getPhysicalSwitch().sendMsg(ofm, this);
        	}*/
        } else {
        	this.getPhysicalSwitch().sendMsg(msg, this);
        }
    }
	
}
