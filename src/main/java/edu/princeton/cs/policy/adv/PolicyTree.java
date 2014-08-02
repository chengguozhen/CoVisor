package edu.princeton.cs.policy.adv;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;


public abstract class PolicyTree {
	
	public enum PolicyOperator {
		Parallel,
		Sequential,
		Override,
		Predicate,
		Invalid
	}
	
	public enum PolicyUpdateMechanism {
		Incremental,
		Strawman
	}
	
	public PolicyOperator operator;
	public PolicyTree leftChild;
	public PolicyTree rightChild;
	public PolicyFlowTable flowTable;
	public PolicyUpdateTable updateTable;
	public Integer tenantId;
	
	public PolicyTree() {
		this.operator = PolicyOperator.Invalid;
		this.leftChild = null;
		this.rightChild = null;
		this.flowTable = new PolicyFlowTable();
		this.updateTable = new PolicyUpdateTable();
		this.tenantId = -1;
	}
	
	public PolicyUpdateTable update(OFFlowMod fm, Integer tenantId) {
		
		this.calculateUpdateTable(fm, tenantId);
		PolicyUpdateTable returnUpdateTable = this.updateTable.getCopy();
		this.applyUpdateTable();
		return returnUpdateTable;
		
	}
		
	public void calculateUpdateTable(OFFlowMod fm, Integer tenantId) {
		if (!this.updateTable.isEmpty()) {
			return;
		}
		
		switch(this.operator) {
		case Parallel:
		case Sequential:
		case Override:
			this.doUpdate(fm, tenantId);;
			break;
		case Predicate:
			throw new NotImplementedException();
		case Invalid: // this is leaf, directly add to flow table
			if (tenantId == this.tenantId) {
				this.updateTable.addFlowMod(fm, fm.getCommand());
			}
			break;
		default:
			break;
		}

	}
	
	public void applyUpdateTable() {
		
		this.flowTable.update(this.updateTable);
		this.updateTable.clear();
		
	}
	
	protected abstract void doUpdate(OFFlowMod newFm, Integer tenantId);

}
