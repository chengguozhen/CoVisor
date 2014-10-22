package edu.princeton.cs.policy.adv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

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
	
	public static PolicyUpdateMechanism UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
	public static boolean ActionOutputAsPass = true; // set true for firewall app
	
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
	
	public PolicyTree(List<PolicyFlowModStoreType> storeTypes,
			List<PolicyFlowModStoreKey> storeKeys) {
		this.operator = PolicyOperator.Invalid;
		this.leftChild = null;
		this.rightChild = null;
		this.flowTable = new PolicyFlowTable(storeTypes, storeKeys);
		this.tenantId = -1;
	}
	
	public PolicyUpdateTable update(OFFlowMod fm, Integer tenantId) {
		
		PolicyUpdateTable updateTable = null;
		
		switch(this.operator) {
		case Parallel:
		case Sequential:
		case Override:
			updateTable = updateIncremental(fm, tenantId);
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
	
	private PolicyUpdateTable updateIncremental(OFFlowMod newFm, Integer tenantId) {
		
		// update children
		PolicyUpdateTable leftUpdateTable = this.leftChild.update(newFm, tenantId);
		PolicyUpdateTable rightUpdateTable = this.rightChild.update(newFm, tenantId);
		
		PolicyUpdateTable updateTable = new PolicyUpdateTable();
		if (this.operator == PolicyOperator.Parallel || this.operator == PolicyOperator.Sequential) {
			
			// add
			for (OFFlowMod fm1 : leftUpdateTable.addFlowMods) {
				
				if (this.operator == PolicyOperator.Sequential) {
					boolean flag = false;
					if (fm1.getActions().isEmpty()) {
						flag = true;
					}
					if (!ActionOutputAsPass) {
						for (OFAction action : fm1.getActions()) {
							if (action instanceof OFActionOutput) {
								flag = true;
								break;
							}
						}
					}
					if (flag) {
						OFFlowMod composedFm = null;
						try {
							composedFm = fm1.clone();
							composedFm.setPriority(
									(short) (fm1.getPriority() * PolicyCompositionUtil.SEQUENTIAL_SHIFT));
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
						this.flowTable.addFlowMod(composedFm);
						leftChild.flowTable.addGeneratedParentFlowMod(fm1, composedFm);
						updateTable.addFlowMods.add(composedFm);
						continue;
					}
				}
				
				// check point 1
				//long startTime = System.nanoTime();
				List<OFFlowMod> potentialFlowMods = rightChild.flowTable.getPotentialFlowMods(fm1);
				//long elapseTime = System.nanoTime() - startTime;
				//System.out.println("check point 1: " + elapseTime / 1e6 + " size: " + potentialFlowMods.size());
				
				// check point 2
				//startTime = System.nanoTime();
				//int count = 0;
				for (OFFlowMod fm2: potentialFlowMods) {
					
					OFFlowMod composedFm = null;
					if (this.operator == PolicyOperator.Parallel) {
						composedFm = PolicyCompositionUtil.parallelComposition(fm1, fm2);
					} else {
						composedFm = PolicyCompositionUtil.sequentialComposition(fm1, fm2);
					}
					//count++;
					if (composedFm != null) {
						this.flowTable.addFlowMod(composedFm);
						leftChild.flowTable.addGeneratedParentFlowMod(fm1, composedFm);
						rightChild.flowTable.addGeneratedParentFlowMod(fm2, composedFm);
						updateTable.addFlowMods.add(composedFm);
					}
				}
				//elapseTime = System.nanoTime() - startTime;
				//System.out.println("check point 2: " + elapseTime / 1e6 + " count: " + count + " " + updateTable.addFlowMods.size());
			}
			
			for (OFFlowMod fm2 : rightUpdateTable.addFlowMods) {
				
				List<OFFlowMod> leftTableWithoutAdd = PolicyCompositionUtil
						.diffFlowMods(leftChild.flowTable.getPotentialFlowMods(fm2),
								leftUpdateTable.addFlowMods);
				for (OFFlowMod fm1 : leftTableWithoutAdd) {
					
					if (this.operator == PolicyOperator.Sequential) {
						boolean flag = false;
						if (fm1.getActions().isEmpty()) {
							flag = true;
						}
						if (!ActionOutputAsPass) {
							for (OFAction action : fm1.getActions()) {
								if (action instanceof OFActionOutput) {
									flag = true;
									break;
								}
							}
						}
						if (flag) {
							continue;
						}
					}
					
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
	/*private PolicyUpdateTable updateSequentialStrawman(OFFlowMod fm,
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
	}*/
	
}
