package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.iptrie.IPTrie;


public class PolicyFlowModStoreTrie extends PolicyFlowModStore {
	
	private IPTrie<PolicyFlowModStore> flowModsTrie;
	private PolicyFlowModStore wildcardFlowStore;
	
	public PolicyFlowModStoreTrie(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		super(storeTypes, storeKeys);
		this.flowModsTrie = new IPTrie<PolicyFlowModStore>();
		
		List<PolicyFlowModStoreType> wildcardStoreTypes = new ArrayList<PolicyFlowModStoreType>();
		wildcardStoreTypes.add(PolicyFlowModStoreType.WILDCARD);
    	List<PolicyFlowModStoreKey> wildcardStoreKeys = new ArrayList<PolicyFlowModStoreKey>();
    	wildcardStoreKeys.add(PolicyFlowModStoreKey.ALL);
		this.wildcardFlowStore = new PolicyFlowModStoreList(wildcardStoreTypes, wildcardStoreKeys);
	}

	@Override
	public void setStore(List<OFFlowMod> flowMods) {
		this.clear();
		for (OFFlowMod fm : flowMods) {
			this.add(fm);
		}
	}

	@Override
	public void clear() {
		this.flowModsTrie = new IPTrie<PolicyFlowModStore>();
		this.wildcardFlowStore.clear();
	}

	@Override
	public void add(OFFlowMod fm) {
		String key = this.getKey(fm);
		if (key.equals("")) {
			this.wildcardFlowStore.add(fm);
		} else {
			PolicyFlowModStore value = this.flowModsTrie.getExact(key);
			if (value == null) {
				value = PolicyFlowModStore.createFlowModStore(
						this.childStoreTypes, this.childStoreKeys);
				this.flowModsTrie.put(key, value);
			}
			value.add(fm);
		}
	}
	
	private String getKey (OFFlowMod fm) {
		int ip = 0;
		int prefixLen = 0;
		switch (this.storeKey) {
		case NETWORK_SRC:
			ip = fm.getMatch().getNetworkSource();
			prefixLen = fm.getMatch().getNetworkSourceMaskLen(); 
			break;
		case NETWORK_DST:
			ip = fm.getMatch().getNetworkDestination();
			prefixLen = fm.getMatch().getNetworkDestinationMaskLen();
			break;
		default:
			break;
		}
		return String.format("%32s", Integer.toBinaryString(ip)).replace(' ', '0').substring(0, prefixLen);
	}

	@Override
	public OFFlowMod remove(OFFlowMod fm) {
		String key = this.getKey(fm);
		if (key.equals("")) {
			return wildcardFlowStore.remove(fm);
		} else {
			PolicyFlowModStore value = this.flowModsTrie.getExact(key);
			if (value != null) {
				return value.remove(fm);
			}
		}
		return null;
	}

	@Override
	public List<OFFlowMod> removaAll(List<OFFlowMod> flowMods) {
		List<OFFlowMod> deletedFms = new ArrayList<OFFlowMod>();
		for (OFFlowMod fm : flowMods) {
			String key = this.getKey(fm);
			if (key.equals("")) {
				OFFlowMod deleted =wildcardFlowStore.remove(fm);
				if (deleted != null) {
					deletedFms.add(deleted);
				}
			} else {
				PolicyFlowModStore value = this.flowModsTrie.getExact(key);
				if (value != null) {
					OFFlowMod deleted = value.remove(fm);
					if (deleted != null) {
						deletedFms.add(deleted);
					}
				}
			}
		}
		return deletedFms;
	}

	@Override
	public List<OFFlowMod> getFlowMods() {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();

		// get flowmods that match this field
		List<PolicyFlowModStore> values = this.flowModsTrie.get("");
		for (PolicyFlowModStore value : values) {
			flowMods.addAll(value.getFlowMods());
		}

		// get flowmods that wildcard this field
		flowMods.addAll(this.wildcardFlowStore.getFlowMods());
		return flowMods;
	}

	@Override
	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm) {
		String key = this.getKey(fm);

		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();

		// get flowmods that match this field
		List<PolicyFlowModStore> values = this.flowModsTrie.get(key);
		for (PolicyFlowModStore value : values) {
			flowMods.addAll(value.getPotentialFlowMods(fm));
		}

		// get flowmods that wildcard this field
		flowMods.addAll(this.wildcardFlowStore.getFlowMods());
		return flowMods;
	}
	
	@Override
	public String toString() {
		return "Type: " + this.storeType + "\tKey: " + this.storeKey;
	}

}
