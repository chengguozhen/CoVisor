package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;

import net.onrc.openvirtex.elements.datapath.FlowTable;
import net.onrc.openvirtex.messages.OVXFlowMod;

public class PolicyTree {
	
	public enum PolicyOperator {
		Parallel,
		Sequential,
		Override,
		Predicate,
		Invalid
	}
	
	
	private PolicyOperator operator;
	private PolicyTree leftChild;
	private PolicyTree rightChild;
	private PolicyFlowTable flowTable;
	private Integer tenantId;
	
	public PolicyTree() {
		this.operator = PolicyOperator.Invalid;
		this.leftChild = null;
		this.rightChild = null;
		this.flowTable = null;
		this.tenantId = -1;
	}
	
	public PolicyUpdateTable update(OFFlowMod fm, Integer tenantId) {
		
		PolicyUpdateTable updateTable = null;
		
		switch(this.operator) {
		case Parallel:
			updateTable = updateParallelStrawman(fm, tenantId);
			break;
		case Sequential:
			break;
		case Override:
			break;
		case Predicate:
			break;
		case Invalid: // this is leaf, directly add to flow table
			if (tenantId == this.tenantId) {
				updateTable = flowTable.update(fm);
			}
			break;
		default:
			break;
		}
		
		return updateTable;
	}
	
	// strawman solution: calculate a new cross product
	private PolicyUpdateTable updateParallelStrawman(OFFlowMod fm, Integer tenantId) {
		
		this.leftChild.update(fm, tenantId);
		this.rightChild.update(fm, tenantId);
		
		this.flowTable.clearTable();
		for (OFFlowMod fm1 : this.leftChild.flowTable.getFlowMods()) {
			for (OFFlowMod fm2 : this.rightChild.flowTable.getFlowMods()) {
				OFFlowMod composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
				this.flowTable.addFlowMod(composedFm);
			}
		}
		
		return null;
	}
	
}
