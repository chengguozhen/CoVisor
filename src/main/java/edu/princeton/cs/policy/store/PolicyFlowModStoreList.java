package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class PolicyFlowModStoreList extends PolicyFlowModStore {
	
	private List<OFFlowMod> flowMods;
	
	public PolicyFlowModStoreList (List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		super(storeTypes, storeKeys);
		this.flowMods = new ArrayList<OFFlowMod>();
	}

	@Override
	public void setStore(List<OFFlowMod> flowMods) {
		this.flowMods = flowMods;
	}

	@Override
	public void clear() {
		this.flowMods.clear();
	}

	@Override
	public void add(OFFlowMod fm) {
		this.flowMods.add(fm);
	}

	@Override
	public OFFlowMod remove(OFFlowMod fm) {
		OFFlowMod toDelete = null;
		for (OFFlowMod curFlowMod : this.flowMods) {
			if (curFlowMod.getMatch().equals(fm.getMatch()) && curFlowMod.getPriority() == fm.getPriority()) {
				toDelete = curFlowMod;
				break;
			}
		}
		if (toDelete != null) {
			this.flowMods.remove(toDelete);
		}
		return toDelete;
	}

	@Override
	public List<OFFlowMod> removaAll(List<OFFlowMod> flowMods) {
		List<OFFlowMod> toDelete = new ArrayList<OFFlowMod>();
		for (OFFlowMod fm : flowMods) {
			for (OFFlowMod curFlowMod : this.flowMods) {
				if (curFlowMod.getMatch().equals(fm.getMatch()) && curFlowMod.getPriority() == fm.getPriority()) {
					toDelete.add(curFlowMod);
					break;
				}
			}
		}
		this.flowMods.removeAll(toDelete);
		return toDelete;
	}

	@Override
	public List<OFFlowMod> getFlowMods() {
		return this.flowMods;
	}

	@Override
	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm) {
		return this.flowMods;
	}
	
	@Override
	public String toString() {
		return "Type: " + this.storeType + "\tKey: " + this.storeKey;
	}

}
