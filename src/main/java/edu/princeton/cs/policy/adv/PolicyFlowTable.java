package edu.princeton.cs.policy.adv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.store.PolicyFlowModStore;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;


public class PolicyFlowTable {
	
	//private List<OFFlowMod> flowMods;
	private ConcurrentHashMap<OFFlowMod, List<OFFlowMod>> generatedParentFlowModsDictionary;
	private PolicyFlowModStore flowModStore;
	
	public PolicyFlowTable() {
		this.generatedParentFlowModsDictionary = new ConcurrentHashMap<OFFlowMod, List<OFFlowMod>>();
		
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
    	storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    	List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
    	storeKeys.add(PolicyFlowModStoreKey.ALL);
    	this.flowModStore = PolicyFlowModStore.createFlowModStore(storeTypes, storeKeys);
	}

	public PolicyFlowTable(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		this.generatedParentFlowModsDictionary = new ConcurrentHashMap<OFFlowMod, List<OFFlowMod>>();
		this.flowModStore = PolicyFlowModStore.createFlowModStore(storeTypes, storeKeys);
	}
	
	public void addFlowMod(OFFlowMod fm) {
		this.flowModStore.add(fm);
		this.generatedParentFlowModsDictionary.put(fm, new ArrayList<OFFlowMod>());
	}
	
	public void setTable(List<OFFlowMod> flowMods) {
		this.flowModStore.setStore(flowMods);
	}
	
	public void clearTable() {
		this.flowModStore.clear();
	}
	
	public PolicyUpdateTable update (OFFlowMod fm) {
		switch (fm.getCommand()) {
        case OFFlowMod.OFPFC_ADD:
            return doFlowModAdd(fm);
        case OFFlowMod.OFPFC_MODIFY:
        case OFFlowMod.OFPFC_MODIFY_STRICT:
            throw new NotImplementedException("don't allow OFPFC_MODIFY and OFPFC_MODIFY_STRICT");
        case OFFlowMod.OFPFC_DELETE:
        case OFFlowMod.OFPFC_DELETE_STRICT:
            return doFlowModDelete(fm);
        default:
            return null;
        }
	}

	private PolicyUpdateTable doFlowModAdd(OFFlowMod fm) {
		this.addFlowMod(fm);
		
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		updateTable.addFlowMods.add(fm);
		return updateTable;
	}
	
	/*private PolicyUpdateTable doFlowModModify(OFFlowMod fm) {
		OFFlowMod toDelete = null;
		for (OFFlowMod curFlowMod : this.flowMods) {
			if (curFlowMod.getMatch().equals(fm.getMatch())) {
				toDelete = curFlowMod;
				break;
			}
		}
		
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		if (toDelete != null) {
			this.flowMods.remove(toDelete);
			this.flowMods.add(fm);
			updateTable.modifyFlowMods.add(fm);
		}
		else {
			this.flowMods.add(fm);
			updateTable.addFlowMods.add(fm);
		}
		
		return updateTable;
	}*/
	
	private PolicyUpdateTable doFlowModDelete(OFFlowMod fm) {
		OFFlowMod deletedFm = this.flowModStore.remove(fm);
		
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		if (deletedFm != null) {
			updateTable.deleteFlowMods.add(deletedFm);
		}
		
		return updateTable;
	}
	
	public List<OFFlowMod> getFlowMods() {
		return this.flowModStore.getFlowMods();
	}
	
	public List<OFFlowMod> getFlowModsSorted() {
		List<OFFlowMod> flowMods = this.flowModStore.getFlowMods();
		Collections.sort(flowMods, new Comparator<OFFlowMod>() {
			public int compare(OFFlowMod fm1, OFFlowMod fm2) {
				return fm2.getPriority() - fm1.getPriority();
			}
		});
		return flowMods;
	}
	
	public List<OFFlowMod> getFlowModsSortByInport() {
		List<OFFlowMod> flowMods = this.flowModStore.getFlowMods();
		Collections.sort(flowMods, new Comparator<OFFlowMod>() {
			public int compare(OFFlowMod fm1, OFFlowMod fm2) {
				if (fm1.getMatch().getInputPort() != fm2.getMatch().getInputPort()) {
					return fm1.getMatch().getInputPort() - fm2.getMatch().getInputPort();
				} else {
					return fm2.getPriority() - fm1.getPriority();
				}
			}
		});
		return flowMods;
	}
	
	@Override
    public String toString() {
		String str = "Flow Table:\n";
		for (OFFlowMod fm : this.flowModStore.getFlowMods()) {
			str = str + fm.toString() + "\n";
		}
		return str;
	}

	public List<OFFlowMod> getGenerateParentFlowMods(OFFlowMod fm) {
		return this.generatedParentFlowModsDictionary.get(fm);
	}

	public List<OFFlowMod> deleteFlowMods(List<OFFlowMod> flowMods) {
		return this.flowModStore.removaAll(flowMods);
	}
	
	public void addGeneratedParentFlowMod (OFFlowMod fm, OFFlowMod generateParentFlowMod) {
		this.generatedParentFlowModsDictionary.get(fm).add(generateParentFlowMod);
	}
	
	public void deleteGenerateParentFlowModKey (OFFlowMod fm) {
		this.generatedParentFlowModsDictionary.remove(fm);
	}
	
	public void deleteGenerateParentFlowModKeys (List<OFFlowMod> fms) {
		for (OFFlowMod fm : fms) {
			this.deleteGenerateParentFlowModKey(fm);
		}
	}
	
	public List<OFFlowMod> getPotentialFlowMods (OFFlowMod fm) {
		return this.flowModStore.getPotentialFlowMods(fm);
	}
	
}
