package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;

import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;

import edu.princeton.cs.trie.radix.ConcurrentIPRadixTree;
import edu.princeton.cs.trie.radix.IPRadixTree;

public class PolicyFlowModStoreTrie extends PolicyFlowModStore {
	
	private IPRadixTree<PolicyFlowModStore> flowModsTrie;
	
	public PolicyFlowModStoreTrie(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		super(storeTypes, storeKeys);
		this.flowModsTrie = new ConcurrentIPRadixTree<PolicyFlowModStore>(new DefaultCharArrayNodeFactory());
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
		this.flowModsTrie = new ConcurrentIPRadixTree<PolicyFlowModStore>(new DefaultCharArrayNodeFactory());
	}

	@Override
	public void add(OFFlowMod fm) {
		String key = this.getKey(fm);
		PolicyFlowModStore value = this.flowModsTrie.getValueForExactKey(key);
		if (value == null) {
			value = PolicyFlowModStore.createFlowModStore(this.childStoreTypes, this.childStoreKeys);
			this.flowModsTrie.put(key, value);
		}
		value.add(fm);
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
		PolicyFlowModStore value = this.flowModsTrie.getValueForExactKey(key);
		if (value != null) {
			return value.remove(fm);
		}
		return null;
	}

	@Override
	public List<OFFlowMod> removaAll(List<OFFlowMod> flowMods) {
		List<OFFlowMod> deletedFms = new ArrayList<OFFlowMod>();
		for (OFFlowMod fm : flowMods) {
			String key = this.getKey(fm);
			PolicyFlowModStore value = this.flowModsTrie.getValueForExactKey(key);
			if (value != null) {
				OFFlowMod deleted = value.remove(fm);
				if (deleted != null) {
					deletedFms.add(deleted);
				}
			}
		}
		return deletedFms;
	}

	@Override
	public List<OFFlowMod> getFlowMods() {
		throw new NotImplementedException("don't allow getFlowMods from PolicyFlowModStoreTrie");
	}

	@Override
	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		String key = this.getKey(fm);
		List<KeyValuePair<PolicyFlowModStore>> keyValuePairs =
				this.flowModsTrie.getIPKeyValuePairsForKeysStartingWith(key);
		for (KeyValuePair<PolicyFlowModStore> keyValuePair : keyValuePairs) {
			flowMods.addAll(keyValuePair.getValue().getPotentialFlowMods(fm));
		}
		return flowMods;
	}
	
	@Override
	public String toString() {
		return "Type: " + this.storeType + "\tKey: " + this.storeKey;
	}

}
