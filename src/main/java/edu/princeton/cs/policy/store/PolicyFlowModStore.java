package edu.princeton.cs.policy.store;

import java.util.List;

import org.openflow.protocol.OFFlowMod;

public interface PolicyFlowModStore {
	
	public enum PolicyFlowModStoreType {
		EXACT,
		PREFIX,
		WILDCARD
	}
	
	public enum PolicyFlowModStoreKey {
		DATA_SRC,
		DATA_DST,
		NETWORK_SRC,
		NETWORK_DST,
		NETWORK_PROTO,
		TRANSPORT_SRC,
		TRANSPORT_DST,
		all
	}

	public void setStore(List<OFFlowMod> flowMods);
	
	public void clear();

	public void add(OFFlowMod fm);

	public OFFlowMod remove(OFFlowMod fm);

	public List<OFFlowMod> removaAll(List<OFFlowMod> flowMods);
	
	public List<OFFlowMod> getFlowMods();

	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm,
			boolean isSequentialLeft);
	

}
