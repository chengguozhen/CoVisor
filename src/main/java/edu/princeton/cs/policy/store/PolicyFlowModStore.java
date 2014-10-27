package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;

public abstract class PolicyFlowModStore {
	
	public enum PolicyFlowModStoreType {
		EXACT,
		PREFIX,
		WILDCARD,
		DISALLOW
	}
	
	public enum PolicyFlowModStoreKey {
		DATA_SRC,
		DATA_DST,
		NETWORK_SRC,
		NETWORK_DST,
		NETWORK_PROTO,
		TRANSPORT_SRC,
		TRANSPORT_DST
	}
	
	public enum PolicyFlowModAction {
		Output,
		DataLayerDestination,
		DataLayerSource,
		//Enqueue,
		NetworkLayerDestination,
		NetworkLayerSource,
		//NetworkTypeOfService,
		//StripVirtualLan,
		TransportLayerDestination,
		TransportLayerSource,
		//Vendor,
		//VirtuaLanIdentifier,
		//VirtalLanPriorityCodePoint
	}
	
	protected PolicyFlowModStoreType storeType;
	protected PolicyFlowModStoreKey storeKey;
	protected List<PolicyFlowModStoreType> childStoreTypes;
	protected List<PolicyFlowModStoreKey> childStoreKeys;
	protected boolean isLeftInSequentialComposition;
	
	public PolicyFlowModStore(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys,
			Boolean isLeftInSequentialComposition) {
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
		
		this.isLeftInSequentialComposition = isLeftInSequentialComposition;
	}
	
	@Override
	public String toString() {
		String str = this.storeKey + ":" + this.storeType;
		for (int i = 0; i < childStoreKeys.size(); i++) {
			str = "," + this.childStoreKeys.get(i) + ":" + this.childStoreTypes.get(i);
		}
		return str;
	}

	public abstract void setStore(List<OFFlowMod> flowMods);
	
	public abstract void clear();

	public abstract void add(OFFlowMod fm);

	public abstract OFFlowMod remove(OFFlowMod fm);

	public abstract List<OFFlowMod> removaAll(List<OFFlowMod> flowMods);
	
	public abstract List<OFFlowMod> getFlowMods();

	public abstract List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm);
	
	public static PolicyFlowModStore createFlowModStore(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys,
			Boolean isLeftInSequentialComposition) {
		
		if (storeTypes.size() == 0) {
			return new PolicyFlowModStoreList(storeTypes, storeKeys, isLeftInSequentialComposition);
		}
		
		
		PolicyFlowModStore flowModStore = null;
		switch (storeTypes.get(0)) {
		case EXACT: {
			switch (storeKeys.get(0)) {
			case DATA_SRC:
			case DATA_DST:
				flowModStore = new PolicyFlowModStoreMap<ByteArrayWrapper>(storeTypes, storeKeys, isLeftInSequentialComposition);
				break;
			case NETWORK_SRC:
			case NETWORK_DST:
				flowModStore = new PolicyFlowModStoreMap<Integer>(storeTypes, storeKeys, isLeftInSequentialComposition);
				break;
			case NETWORK_PROTO:
				flowModStore = new PolicyFlowModStoreMap<Byte>(storeTypes, storeKeys, isLeftInSequentialComposition);
				break;
			case TRANSPORT_SRC:
			case TRANSPORT_DST:
				flowModStore = new PolicyFlowModStoreMap<Short>(storeTypes, storeKeys, isLeftInSequentialComposition);
				break;
			default:
				break;
			}
			break;
		}
		case PREFIX: {
			flowModStore = new PolicyFlowModStoreGoogleTrie(storeTypes, storeKeys, isLeftInSequentialComposition);
			break;
		}
		case WILDCARD: {
			flowModStore = new PolicyFlowModStoreList(storeTypes, storeKeys, isLeftInSequentialComposition);
			break;
		}
		default: {
			break;
		}
		}
		return flowModStore;
	}

}
