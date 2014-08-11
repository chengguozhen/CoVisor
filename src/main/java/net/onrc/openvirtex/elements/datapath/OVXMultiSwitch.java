package net.onrc.openvirtex.elements.datapath;

import java.util.ArrayList;
import java.util.List;

import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.SwitchMappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import edu.princeton.cs.hsa.PlumbingGraph;
import edu.princeton.cs.hsa.PlumbingNode;

/**
 * The Class OVXMultiSwitch.  Inherits from OVXSwitch, but also implements
 * many Network methods.
 */
public class OVXMultiSwitch extends OVXSingleSwitch {

	private static Logger log = LogManager.getLogger(OVXMultiSwitch.class.getName());
	
	private List<OVXBabySwitch> babySwitches;
	private PlumbingGraph plumbingGraph;


	public OVXMultiSwitch(final Long switchId, final Integer tenantId) {
		super(switchId, tenantId);
		this.babySwitches = new ArrayList<OVXBabySwitch>();
		this.plumbingGraph = new PlumbingGraph();
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
			log.warn("Cannot recover physical switch : {}", e);
		}
		return psw;
    }

	public void sendSouth(final OFMessage msg, final OVXBabySwitch babySwitch) {
        log.info("Sending packet to sw {}: {}", this.getPhysicalSwitch().getName(), msg);
        
        if (msg.getType() == OFType.FLOW_MOD) {
        	this.plumbingGraph.update((OFFlowMod) msg, babySwitch.getPlumbingNode());
        } else {
        	this.getPhysicalSwitch().sendMsg(msg, this);
        }
    }
	
}
