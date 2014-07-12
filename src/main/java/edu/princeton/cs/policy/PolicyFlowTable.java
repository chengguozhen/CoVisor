package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;


public class PolicyFlowTable {
	
	private List<OFFlowMod> flowMods;
	private ConcurrentHashMap<OFFlowMod, List<OFFlowMod>> generatedParentFlowModsDictionary;
	
	public PolicyFlowTable() {
		this.flowMods = new ArrayList<OFFlowMod>();
		this.generatedParentFlowModsDictionary = new ConcurrentHashMap<OFFlowMod, List<OFFlowMod>>();
	}
	
	public void addFlowMod(OFFlowMod fm) {
		this.flowMods.add(fm);
		this.generatedParentFlowModsDictionary.put(fm, new ArrayList<OFFlowMod>());
	}
	
	public void setTable(List<OFFlowMod> flowMods) {
		this.flowMods = flowMods;
	}
	
	public void clearTable() {
		this.flowMods.clear();
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
		OFFlowMod toDelete = null;
		for (OFFlowMod curFlowMod : this.flowMods) {
			if (curFlowMod.getMatch().equals(fm.getMatch()) && curFlowMod.getPriority() == fm.getPriority()) {
				toDelete = curFlowMod;
				break;
			}
		}
		
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		if (toDelete != null) {
			this.flowMods.remove(toDelete);
			updateTable.deleteFlowMods.add(toDelete);
		}
		
		return updateTable;
	}
	
	public List<OFFlowMod> getFlowMods() {
		return this.flowMods;
	}
	
	public List<OFFlowMod> getFlowModsSorted() {
		Collections.sort(this.flowMods, new Comparator<OFFlowMod>() {
			public int compare(OFFlowMod fm1, OFFlowMod fm2) {
				return fm2.getPriority() - fm1.getPriority();
			}
		});
		return this.flowMods;
	}
	
	@Override
    public String toString() {
		String str = "Flow Table:\n";
		for (OFFlowMod fm : this.flowMods) {
			str = str + fm.toString() + "\n";
		}
		return str;
	}

	public List<OFFlowMod> getGenerateParentFlowMods(OFFlowMod fm) {
		return this.generatedParentFlowModsDictionary.get(fm);
	}

	public List<OFFlowMod> deleteFlowMods(List<OFFlowMod> flowMods) {
		List<OFFlowMod> deletedFlowMods = new ArrayList<OFFlowMod>();
		for (OFFlowMod fm : flowMods) {
			if (this.flowMods.contains(fm)) {
				this.flowMods.remove(fm);
				deletedFlowMods.add(fm);
			}
		}
		return deletedFlowMods;
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
	
}
