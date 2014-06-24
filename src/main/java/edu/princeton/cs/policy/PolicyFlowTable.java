package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.elements.datapath.OVXFlowEntry;
import net.onrc.openvirtex.elements.datapath.OVXFlowTable;
import net.onrc.openvirtex.messages.OVXFlowMod;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;

public class PolicyFlowTable {
	
	private List<OFFlowMod> flowMods;
	
	public PolicyFlowTable() {
		this.flowMods = new ArrayList<OFFlowMod>();
	}
	
	public void addFlowMod(OFFlowMod fm) {
		this.flowMods.add(fm);
	}
	
	public void clearTable() {
		this.flowMods.clear();
	}
	
	public PolicyUpdateTable update (OFFlowMod fm) {
		switch (fm.getCommand()) {
        case OFFlowMod.OFPFC_ADD:
            return doFlowModAdd(fm);
        case OFFlowMod.OFPFC_MODIFY:
        case OFFlowMod.OFPFC_MODIFY_STRICT:
            return doFlowModModify(fm);
        case OFFlowMod.OFPFC_DELETE:
            return doFlowModDelete(fm, false);
        case OFFlowMod.OFPFC_DELETE_STRICT:
            return doFlowModDelete(fm, true);
        default:
            return null;
        }
	}

	private PolicyUpdateTable doFlowModAdd(OFFlowMod fm) {
		flowMods.add(fm);
		
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		updateTable.addFlowMods.add(fm);
		return updateTable;
	}
	
	private PolicyUpdateTable doFlowModModify(OFFlowMod fm) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private PolicyUpdateTable doFlowModDelete(OFFlowMod fm, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<OFFlowMod> getFlowMods() {
		return this.flowMods;
	}
}
