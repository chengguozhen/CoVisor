package edu.princeton.cs.policy.adv;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.ofwrapper.OFFlowModWrapper;

public class PolicyUpdateTable {
	private List<OFFlowModWrapper> updateFlowMods;
	
	public PolicyUpdateTable() {
		this.updateFlowMods = new ArrayList<OFFlowModWrapper>();
	}
	
	public void addFlowMod(OFFlowMod flowMod, short command) {
		OFFlowModWrapper flowModWrapper = new OFFlowModWrapper(flowMod, command);
		this.updateFlowMods.add(flowModWrapper);
	}
	
	public void addFlowModWrapper(OFFlowModWrapper flowModWrapper) {
		this.updateFlowMods.add(flowModWrapper);
	}
	
	public List<OFFlowModWrapper> getFlowModWrappers() {
		return this.updateFlowMods;
	}
	
	public void clear() {
		this.updateFlowMods.clear();
	}
	
	public boolean isEmpty() {
		return updateFlowMods.isEmpty();
	}
	
	public PolicyUpdateTable getCopy() {
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		for (OFFlowModWrapper flowModWrapper : this.updateFlowMods) {
			updateTable.addFlowModWrapper(flowModWrapper);
		}
		return updateTable;
	}

}
