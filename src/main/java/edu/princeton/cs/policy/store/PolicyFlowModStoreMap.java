package edu.princeton.cs.policy.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;

public class PolicyFlowModStoreMap<O> extends PolicyFlowModStore {

	private Map<O, PolicyFlowModStore> flowModsMap;
	private O wildcardKey;
	
	public PolicyFlowModStoreMap(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		super(storeTypes, storeKeys);
		this.flowModsMap = new HashMap<O, PolicyFlowModStore>();
		this.wildcardKey = generateWildcardKey();
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

	@Override
	public void add(OFFlowMod fm) {
		O key = this.getKey(fm);
		PolicyFlowModStore value = this.flowModsMap.get(key);
		if (value == null) {
			value = PolicyFlowModStore.createFlowModStore(this.childStoreTypes, this.childStoreKeys);
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
	
	@SuppressWarnings("unchecked")
	private O generateWildcardKey () {
		O key = null;
		switch (this.storeKey) {
		case DATA_SRC:
			key = (O) new ByteArrayWrapper(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
			break;
		case DATA_DST:
			key = (O) new ByteArrayWrapper(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
			break;
		case NETWORK_SRC:
			key = (O) Integer.valueOf(0);
			break;
		case NETWORK_DST:
			key = (O) Integer.valueOf(0);
			break;
		case NETWORK_PROTO:
			key = (O) Byte.valueOf((byte) 0);
			break;
		case TRANSPORT_SRC:
			key = (O) Short.valueOf((short) 0);
			break;
		case TRANSPORT_DST:
			key = (O) Short.valueOf((short) 0);
			break;
		default:
			break;
		}
		return key;
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
	public List<OFFlowMod> getPotentialFlowMods(OFFlowMod fm) {
		O key = this.getKey(fm);
		if (key.equals(wildcardKey)) {
			return this.getFlowMods();
		} else {
			List<OFFlowMod> potentialFlowMods = new ArrayList<OFFlowMod>();
			
			// get flowmods that match this field
			PolicyFlowModStore value = this.flowModsMap.get(key);
			if (value != null) {
				potentialFlowMods.addAll(value.getPotentialFlowMods(fm));
			}

			// get flowmods that wildcard this field
			value = this.flowModsMap.get(this.wildcardKey);
			if (value != null) {
				potentialFlowMods.addAll(value.getPotentialFlowMods(fm));
			}
			
			return potentialFlowMods;
		}
	}

	@Override
	public String toString() {
		return "Type: " + this.storeType + "\tKey: " + this.storeKey;
	}
}
