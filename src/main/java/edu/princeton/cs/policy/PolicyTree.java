package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;


public class PolicyTree {
	
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
	
	private static final PolicyUpdateMechanism UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
	
	public PolicyOperator operator;
	public PolicyTree leftChild;
	public PolicyTree rightChild;
	public PolicyFlowTable flowTable;
	public Integer tenantId; // only meaningful when operator is Invalid
	
	public PolicyTree() {
		this.operator = PolicyOperator.Invalid;
		this.leftChild = null;
		this.rightChild = null;
		this.flowTable = new PolicyFlowTable();
		this.tenantId = -1;
	}
	
	public PolicyUpdateTable update(OFFlowMod fm, Integer tenantId) {
		
		PolicyUpdateTable updateTable = null;
		
		switch(this.operator) {
		case Parallel:
		case Sequential:
		case Override:
			if (UPDATEMECHANISM == PolicyUpdateMechanism.Strawman) {
				updateTable = updateStrawman(fm, tenantId);
			} else {
				updateTable = updateIncremental(fm, tenantId);
			}
			break;
		case Predicate:
			throw new NotImplementedException();
		case Invalid: // this is leaf, directly add to flow table
			if (tenantId == this.tenantId) {
				updateTable = this.flowTable.update(fm);
			} else {
				updateTable = new PolicyUpdateTable();
			}
			break;
		default:
			break;
		}
		
		return updateTable;
	}

	// strawman solution: calculate a new cross product
	private PolicyUpdateTable updateStrawman(OFFlowMod newFm, Integer tenantId) {
		
		// update children
		this.leftChild.update(newFm, tenantId);
		this.rightChild.update(newFm, tenantId);
		
		// generate new table
		List<OFFlowMod> newFlowMods = new ArrayList<OFFlowMod>();
		if (this.operator == PolicyOperator.Parallel || this.operator == PolicyOperator.Sequential) {
			
			for (OFFlowMod fm1 : this.leftChild.flowTable.getFlowModsSorted()) {
				for (OFFlowMod fm2 : this.rightChild.flowTable.getFlowModsSorted()) {
					
					OFFlowMod composedFm = null;
					if (this.operator == PolicyOperator.Parallel) {
						composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
					} else {
						composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
					}
					if (composedFm != null) {
						newFlowMods.add(composedFm);
					}
				}
			}
		} else if (this.operator == PolicyOperator.Override) {
			for (OFFlowMod fm1 : this.leftChild.flowTable.getFlowModsSorted()) {
				try {
					newFlowMods.add(fm1.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			for (OFFlowMod fm2 : this.rightChild.flowTable.getFlowModsSorted()) {
				try {
					newFlowMods.add(fm2.clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
		
		short currentPriority = (short) (newFlowMods.size() - 1);
		for (OFFlowMod fm : newFlowMods) {
			fm.setPriority(currentPriority);
			currentPriority -= 1;
		}
		
		// calculate difference between old and new table
		List<OFFlowMod> oldFlowMods = this.flowTable.getFlowMods();
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		updateTable.addFlowMods = PolicyCompositionUtil.diffFlowMods(newFlowMods, oldFlowMods);
		updateTable.deleteFlowMods = PolicyCompositionUtil.diffFlowMods(oldFlowMods, newFlowMods);
		
		// update flow table
		this.flowTable.setTable(newFlowMods);
		
		return updateTable;
	}
	
	private PolicyUpdateTable updateIncremental(OFFlowMod newFm, Integer tenantId) {
		
		// update children
		PolicyUpdateTable leftUpdateTable = this.leftChild.update(newFm, tenantId);
		PolicyUpdateTable rightUpdateTable = this.rightChild.update(newFm, tenantId);
		
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		if (this.operator == PolicyOperator.Parallel || this.operator == PolicyOperator.Sequential) {
			
			// add
			for (OFFlowMod fm1 : leftUpdateTable.addFlowMods) {
				for (OFFlowMod fm2: rightChild.flowTable.getFlowMods()) {
					
					OFFlowMod composedFm = null;
					if (this.operator == PolicyOperator.Parallel) {
						composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
					} else {
						composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
					}
					if (composedFm != null) {
						this.flowTable.addFlowMod(composedFm);
						leftChild.flowTable.addGeneratedParentFlowMod(fm1, composedFm);
						rightChild.flowTable.addGeneratedParentFlowMod(fm2, composedFm);
						updateTable.addFlowMods.add(composedFm);
					}
				}
			}
			
			List<OFFlowMod> leftTableWithoutAdd = PolicyCompositionUtil
					.diffFlowMods(leftChild.flowTable.getFlowMods(), leftUpdateTable.addFlowMods);
			for (OFFlowMod fm1 : leftTableWithoutAdd) {
				for (OFFlowMod fm2 : rightUpdateTable.addFlowMods) {
					OFFlowMod composedFm = null;
					if (this.operator == PolicyOperator.Parallel) {
						composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
					} else {
						composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
					}
					if (composedFm != null) {
						this.flowTable.addFlowMod(composedFm);
						leftChild.flowTable.addGeneratedParentFlowMod(fm1, composedFm);
						rightChild.flowTable.addGeneratedParentFlowMod(fm2, composedFm);
						updateTable.addFlowMods.add(composedFm);
					}
				}
			}
			
			// delete
			for (OFFlowMod fm : leftUpdateTable.deleteFlowMods) {
				List<OFFlowMod> generatedParentFlowMods = leftChild.flowTable.getGenerateParentFlowMods(fm);
				List<OFFlowMod> deletedFlowMods = this.flowTable.deleteFlowMods(generatedParentFlowMods);
				updateTable.deleteFlowMods.addAll(deletedFlowMods);
			}
			for (OFFlowMod fm : rightUpdateTable.deleteFlowMods) {
				List<OFFlowMod> generatedParentFlowMods = rightChild.flowTable.getGenerateParentFlowMods(fm);
				List<OFFlowMod> deletedFlowMods = this.flowTable.deleteFlowMods(generatedParentFlowMods);
				updateTable.deleteFlowMods.addAll(deletedFlowMods);
			}
			leftChild.flowTable.deleteGenerateParentFlowModKeys(leftUpdateTable.deleteFlowMods);
			rightChild.flowTable.deleteGenerateParentFlowModKeys(rightUpdateTable.deleteFlowMods);
		} else if (this.operator == PolicyOperator.Override) {
			
			// add
			for (OFFlowMod fm : leftUpdateTable.addFlowMods) {
				OFFlowMod addFm = null;
				try {
					addFm = fm.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				addFm.setPriority((short) (addFm.getPriority() * PolicyCompositionUtil.OVERRIDE_SHIFT));
				this.flowTable.addFlowMod(addFm);
				leftChild.flowTable.addGeneratedParentFlowMod(fm, addFm);
				updateTable.addFlowMods.add(addFm);
			}
			
			for (OFFlowMod fm : rightUpdateTable.addFlowMods) {
				OFFlowMod addFm = null;
				try {
					addFm = fm.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				this.flowTable.addFlowMod(addFm);
				rightChild.flowTable.addGeneratedParentFlowMod(fm, addFm);
				updateTable.addFlowMods.add(addFm);
			}
			
			// delete
			for (OFFlowMod fm : leftUpdateTable.deleteFlowMods) {
				List<OFFlowMod> generatedParentFlowMods = leftChild.flowTable.getGenerateParentFlowMods(fm);
				List<OFFlowMod> deletedFlowMods = this.flowTable.deleteFlowMods(generatedParentFlowMods);
				updateTable.deleteFlowMods.addAll(deletedFlowMods);
			}
			for (OFFlowMod fm : rightUpdateTable.deleteFlowMods) {
				List<OFFlowMod> generatedParentFlowMods = rightChild.flowTable.getGenerateParentFlowMods(fm);
				List<OFFlowMod> deletedFlowMods = this.flowTable.deleteFlowMods(generatedParentFlowMods);
				updateTable.deleteFlowMods.addAll(deletedFlowMods);
			}
			leftChild.flowTable.deleteGenerateParentFlowModKeys(leftUpdateTable.deleteFlowMods);
			rightChild.flowTable.deleteGenerateParentFlowModKeys(rightUpdateTable.deleteFlowMods);
		}
		
		return updateTable;
	}

	// strawman solution: calculate a new cross product
	private PolicyUpdateTable updateSequentialStrawman(OFFlowMod fm,
			Integer tenantId) {

		this.leftChild.update(fm, tenantId);
		this.rightChild.update(fm, tenantId);

		this.flowTable.clearTable();
		for (OFFlowMod fm1 : this.leftChild.flowTable.getFlowMods()) {
			for (OFFlowMod fm2 : this.rightChild.flowTable.getFlowMods()) {
				OFFlowMod composedFm = PolicyCompositionUtil
						.sequentialComposition(fm1, fm2);
				if (composedFm != null) {
					this.flowTable.addFlowMod(composedFm);
				}
			}
		}

		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		for (OFFlowMod ofm : this.flowTable.getFlowMods()) {
			updateTable.addFlowMods.add(ofm);
		}

		return updateTable;
	}
	
}
