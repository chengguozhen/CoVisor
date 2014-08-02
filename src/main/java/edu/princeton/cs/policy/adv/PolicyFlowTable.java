package edu.princeton.cs.policy.adv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.ofwrapper.OFFlowModWrapper;

public class PolicyFlowTable {
	
	private List<OFFlowModWrapper> flowModWrappers;
	

	public PolicyFlowTable() {
		this.flowModWrappers = new ArrayList<OFFlowModWrapper>();
	}
	
	public void update (PolicyUpdateTable updateTable) {
		for (OFFlowModWrapper fm : updateTable.getFlowModWrappers()) {
			switch (fm.command) {
			case OFFlowMod.OFPFC_ADD:
	            this.doFlowModAdd(fm);
	            break;
	        case OFFlowMod.OFPFC_MODIFY:
	        case OFFlowMod.OFPFC_MODIFY_STRICT:
	            throw new NotImplementedException("don't allow OFPFC_MODIFY and OFPFC_MODIFY_STRICT");
	        case OFFlowMod.OFPFC_DELETE:
	        case OFFlowMod.OFPFC_DELETE_STRICT:
	            this.doFlowModDelete(fm);
	            break;
	        default:
	            break;
			}
		}
	}
	
	public void doFlowModAdd(OFFlowModWrapper fm) {
		this.flowModWrappers.add(fm);
	}
	
	public void doFlowModDelete(OFFlowModWrapper fm) {
		OFFlowModWrapper toDelete = null;
		for (OFFlowModWrapper curFm : this.flowModWrappers) {
			if (curFm.flowMod.getMatch().equals(fm.flowMod.getMatch()) && curFm.flowMod.getPriority() == fm.flowMod.getPriority()) {
				toDelete = fm;
				break;
			}
		}
		
		if (toDelete != null) {
			this.flowModWrappers.remove(toDelete);
		}
	}

}
