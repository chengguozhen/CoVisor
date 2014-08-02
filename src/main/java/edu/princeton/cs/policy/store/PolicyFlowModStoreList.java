package edu.princeton.cs.policy.store;

import java.util.List;

import org.openflow.protocol.OFFlowMod;

public class PolicyFlowModStoreList implements PolicyFlowModStore {
	
	public PolicyFlowModStoreList () {
		
	}

	@Override
	public void setStore(List<OFFlowMod> flowMods) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(OFFlowMod fm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OFFlowMod remove(OFFlowMod fm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OFFlowMod> removaAll(List<OFFlowMod> flowMods) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OFFlowMod> getFlowMods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm,
			boolean isSequentialLeft) {
		// TODO Auto-generated method stub
		return null;
	}

}
