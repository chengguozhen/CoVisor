package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;

import com.google.common.base.CaseFormat;

public class PolicyFlowModStoreMap<O> implements PolicyFlowModStore {

	@SuppressWarnings("unused")
	private PolicyFlowModStoreType storeType;
	private PolicyFlowModStoreKey storeKey;
	private List<PolicyFlowModStoreType> childStoreTypes;
	private List<PolicyFlowModStoreKey> childStoreKeys;
	private Map<O, PolicyFlowModStore> flowModsMap;
	
	public PolicyFlowModStoreMap(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		this.storeType = storeTypes.get(0);
		for (int i = 1; i < storeTypes.size(); i++) {
			this.childStoreTypes.add(storeTypes.get(i));
		}
		this.storeKey = storeKeys.get(0);
		for (int i = 1; i < storeKeys.size(); i++) {
			this.childStoreKeys.add(storeKeys.get(i));
		}
		this.flowModsMap = new HashMap<O, PolicyFlowModStore>();
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
		this.flowModsMap.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(OFFlowMod fm) {
		O key = this.getKey(fm);
		PolicyFlowModStore value = this.flowModsMap.get(key);
		if (value == null) {
			value = createChildFlowModStore();
			this.flowModsMap.put(key, value);
		}
		value.add(fm);
	}
	
	@SuppressWarnings("unchecked")
	private O getKey (OFFlowMod fm) {
		O key = null;
		switch (this.storeKey) {
		case DATA_SRC:
			key = (O) new ByteArrayWrapper(fm.getMatch().getDataLayerSource());
			break;
		case DATA_DST:
			key = (O) new ByteArrayWrapper(fm.getMatch().getDataLayerDestination());
			break;
		case NETWORK_SRC:
			key = (O) Integer.valueOf(fm.getMatch().getNetworkSource());
			break;
		case NETWORK_DST:
			key = (O) Integer.valueOf(fm.getMatch().getNetworkDestination());
			break;
		case NETWORK_PROTO:
			key = (O) Byte.valueOf(fm.getMatch().getNetworkProtocol());
			break;
		case TRANSPORT_SRC:
			key = (O) Short.valueOf(fm.getMatch().getTransportSource());
			break;
		case TRANSPORT_DST:
			key = (O) Short.valueOf(fm.getMatch().getTransportDestination());
			break;
		default:
			break;
		}
		return key;
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
		O key = this.getKey(fm);
		PolicyFlowModStore value = this.flowModsMap.get(key);
		if (value != null) {
			return value.remove(fm);
		} else {
			return null;
		}
	}

	@Override
	public List<OFFlowMod> removaAll(List<OFFlowMod> flowMods) {
		List<OFFlowMod> deletedFms = new ArrayList<OFFlowMod>();
		for (OFFlowMod fm : flowMods) {
			O key = this.getKey(fm);
			PolicyFlowModStore value = this.flowModsMap.get(key);
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
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
		for (PolicyFlowModStore flowModStore : this.flowModsMap.values()) {
			flowMods.addAll(flowModStore.getFlowMods());
		}
		return flowMods;
	}

	@Override
	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm,
			boolean isSequentialLeft) {
		O key = this.getKey(fm);
		PolicyFlowModStore value = this.flowModsMap.get(key);
		if (value != null) {
			return value.getPotentialFlowMods(fm, isSequentialLeft);
		} else {
			return new ArrayList<OFFlowMod>();
		}
	}

	@Override
	public String toString() {
		return "Type: " + this.storeType + "\tKey: " + this.storeKey;
	}
}
