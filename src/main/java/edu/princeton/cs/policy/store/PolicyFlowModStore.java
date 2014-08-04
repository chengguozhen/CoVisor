package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;

public abstract class PolicyFlowModStore {
	
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
		ALL
	}
	
	protected PolicyFlowModStoreType storeType;
	protected PolicyFlowModStoreKey storeKey;
	protected List<PolicyFlowModStoreType> childStoreTypes;
	protected List<PolicyFlowModStoreKey> childStoreKeys;
	
	public PolicyFlowModStore(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		this.storeType = storeTypes.get(0);
		this.childStoreTypes = new ArrayList<PolicyFlowModStoreType>();
		for (int i = 1; i < storeTypes.size(); i++) {
			this.childStoreTypes.add(storeTypes.get(i));
		}
		this.storeKey = storeKeys.get(0);
		this.childStoreKeys = new ArrayList<PolicyFlowModStoreKey>();
		for (int i = 1; i < storeKeys.size(); i++) {
			this.childStoreKeys.add(storeKeys.get(i));
		}
	}

	public abstract void setStore(List<OFFlowMod> flowMods);
	
	public abstract void clear();

	public abstract void add(OFFlowMod fm);

	public abstract OFFlowMod remove(OFFlowMod fm);

	public abstract List<OFFlowMod> removaAll(List<OFFlowMod> flowMods);
	
	public abstract List<OFFlowMod> getFlowMods();

	public abstract List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm);
	
	public static PolicyFlowModStore createFlowModStore(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		PolicyFlowModStore flowModStore = null;
		switch (storeTypes.get(0)) {
		case EXACT: {
			switch (storeKeys.get(0)) {
			case DATA_SRC:
			case DATA_DST:
				flowModStore = new PolicyFlowModStoreMap<ByteArrayWrapper>(storeTypes, storeKeys);
				break;
			case NETWORK_SRC:
			case NETWORK_DST:
				flowModStore = new PolicyFlowModStoreMap<Integer>(storeTypes, storeKeys);
				break;
			case NETWORK_PROTO:
				flowModStore = new PolicyFlowModStoreMap<Byte>(storeTypes, storeKeys);
				break;
			case TRANSPORT_SRC:
			case TRANSPORT_DST:
				flowModStore = new PolicyFlowModStoreMap<Short>(storeTypes, storeKeys);
				break;
			default:
				break;
			}
			break;
		}
		case PREFIX: {
			flowModStore = new PolicyFlowModStoreTrie(storeTypes, storeKeys);
			break;
		}
		case WILDCARD: {
			flowModStore = new PolicyFlowModStoreList(storeTypes, storeKeys);
			break;
		}
		default: {
			break;
		}
		}
		return flowModStore;
	}

}
