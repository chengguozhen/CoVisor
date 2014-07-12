package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;

public class PolicyUpdateTable {
	
	public List<OFFlowMod> addFlowMods;
	public List<OFFlowMod> deleteFlowMods;
	
	public PolicyUpdateTable() {
		this.addFlowMods = new ArrayList<OFFlowMod>();
		this.deleteFlowMods = new ArrayList<OFFlowMod>();
	}
}
