package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;

import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;

import edu.princeton.cs.trie.radix.ConcurrentIPRadixTree;
import edu.princeton.cs.trie.radix.IPRadixTree;

public class PolicyFlowModStoreTrie implements PolicyFlowModStore {
	
	private PolicyFlowModStoreType storeType;
	private PolicyFlowModStoreKey storeKey;
	private List<PolicyFlowModStoreType> childStoreTypes;
	private List<PolicyFlowModStoreKey> childStoreKeys;
	private IPRadixTree<PolicyFlowModStore> flowModsTrie; 
	
	public PolicyFlowModStoreTrie(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		this.storeType = storeTypes.get(0);
		for (int i = 1; i < storeTypes.size(); i++) {
			this.childStoreTypes.add(storeTypes.get(i));
		}
		this.storeKey = storeKeys.get(0);
		for (int i = 1; i < storeKeys.size(); i++) {
			this.childStoreKeys.add(storeKeys.get(i));
		}
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
			value = this.createChildFlowModStore();
			this.flowModsTrie.put(key, value);
		}
		value.add(fm);
	}
	
	private String getKey (OFFlowMod fm) {
		int ip = 0;
		switch (this.storeKey) {
		case NETWORK_SRC:
			ip = fm.getMatch().getNetworkSource();
			break;
		case NETWORK_DST:
			ip = fm.getMatch().getNetworkDestination();
			break;
		default:
			break;
		}
		return String.format("%32s", Integer.toBinaryString(ip)).replace(' ', '0');
	}
	
	private PolicyFlowModStore createChildFlowModStore() {
		PolicyFlowModStore flowModStore = null;
		switch (this.childStoreTypes.get(0)) {
		case EXACT: {
			switch (this.childStoreKeys.get(0)) {
			case DATA_SRC:
			case DATA_DST:
				flowModStore = new PolicyFlowModStoreMap<ByteArrayWrapper>(childStoreTypes, childStoreKeys);
				break;
			case NETWORK_SRC:
			case NETWORK_DST:
				flowModStore = new PolicyFlowModStoreMap<Integer>(childStoreTypes, childStoreKeys);
				break;
			case NETWORK_PROTO:
				flowModStore = new PolicyFlowModStoreMap<Byte>(childStoreTypes, childStoreKeys);
				break;
			case TRANSPORT_SRC:
			case TRANSPORT_DST:
				flowModStore = new PolicyFlowModStoreMap<Short>(childStoreTypes, childStoreKeys);
				break;
			default:
				break;
			}
			break;
		}
		case PREFIX: {
			flowModStore = new PolicyFlowModStoreTrie(childStoreTypes, childStoreKeys);
			break;
		}
		case WILDCARD: {
			flowModStore = new PolicyFlowModStoreList(childStoreTypes, childStoreKeys);
			break;
		}
		default: {
			break;
		}
		}
		return flowModStore;
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
	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm,
			boolean isSequentialLeft) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		String key = this.getKey(fm);
		List<KeyValuePair<PolicyFlowModStore>> keyValuePairs =
				this.flowModsTrie.getIPKeyValuePairsForKeysStartingWith(key);
		for (KeyValuePair<PolicyFlowModStore> keyValuePair : keyValuePairs) {
			flowMods.addAll(keyValuePair.getValue().getPotentialFlowMods(fm, isSequentialLeft));
		}
		return flowMods;
	}
	
	@Override
	public String toString() {
		return "Type: " + this.storeType + "\tKey: " + this.storeKey;
	}

}
