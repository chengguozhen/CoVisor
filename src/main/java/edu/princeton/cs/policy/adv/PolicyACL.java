package edu.princeton.cs.policy.adv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.onrc.openvirtex.exceptions.NetworkMappingException;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;

import com.googlecode.concurrenttrees.common.KeyValuePair;

import edu.princeton.cs.hsa.Tuple;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModAction;

public class PolicyACL implements Cloneable {
	
	public Map<PolicyFlowModStoreKey, PolicyFlowModStoreType> aclMatch;
	public Map<PolicyFlowModAction, Boolean> aclAction;
	
	public PolicyACL() {
		this.aclMatch = new HashMap<PolicyFlowModStoreKey, PolicyFlowModStoreType>();
		this.aclAction = new HashMap<PolicyFlowModAction, Boolean>();
		
		for (PolicyFlowModStoreKey field : PolicyFlowModStoreKey.values()) {
			this.aclMatch.put(field, PolicyFlowModStoreType.DISALLOW);
		}
		this.aclMatch.remove(PolicyFlowModStoreKey.ALL);
		for (PolicyFlowModAction action : PolicyFlowModAction.values()) {
			this.aclAction.put(action, false);
		}
		
	}
	
	@Override
	public PolicyACL clone() {
        try {
            final PolicyACL ret = (PolicyACL) super.clone();
            ret.aclMatch = new HashMap<PolicyFlowModStoreKey, PolicyFlowModStoreType>();
            ret.aclAction = new HashMap<PolicyFlowModAction, Boolean>();
            for (Map.Entry<PolicyFlowModStoreKey, PolicyFlowModStoreType> entry : this.aclMatch.entrySet()) {
            	ret.aclMatch.put(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<PolicyFlowModAction, Boolean> entry : this.aclAction.entrySet()) {
            	ret.aclAction.put(entry.getKey(), entry.getValue());
            }
            return ret;
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
	
	@Override
	public String toString() {
		String str = "";
		for (PolicyFlowModStoreKey field : PolicyFlowModStoreKey.values()) {
            if (this.aclMatch.containsKey(field) && this.aclMatch.get(field) != PolicyFlowModStoreType.DISALLOW) {
			    str = str + field + ":" + this.aclMatch.get(field) + ",";
            }
		}
		str += "\n";
		for (PolicyFlowModAction action : PolicyFlowModAction.values()) {
            if (this.aclAction.containsKey(action) && this.aclAction.get(action)) {
			    str = str + action + ",";
            }
		}
		return str;
	}
	
	public static PolicyACL composeACL (PolicyTree leftChild, PolicyTree rightChild, PolicyOperator operator)
			throws NetworkMappingException {
		
		if (leftChild.policyACL == null) {
			leftChild.initializeFlowTable();
		}
		if (rightChild.policyACL == null) {
			rightChild.initializeFlowTable();
		}
		PolicyACL leftACL = leftChild.policyACL;
		PolicyACL rightACL = rightChild.policyACL;
		PolicyACL policyACL = new PolicyACL();
	
		// generate policy acl
		// deal with match, union
		for (PolicyFlowModStoreKey field : PolicyFlowModStoreKey.values()) {
			if (leftACL.aclMatch.get(field) == PolicyFlowModStoreType.DISALLOW) {
				policyACL.aclMatch.put(field, rightACL.aclMatch.get(field));
			} else if (rightACL.aclMatch.get(field) == PolicyFlowModStoreType.DISALLOW) {
				policyACL.aclMatch.put(field, leftACL.aclMatch.get(field));
			} else if (leftACL.aclMatch.get(field) == PolicyFlowModStoreType.WILDCARD) {
				policyACL.aclMatch.put(field, rightACL.aclMatch.get(field));
			} else if (rightACL.aclMatch.get(field) == PolicyFlowModStoreType.WILDCARD) {
				policyACL.aclMatch.put(field, leftACL.aclMatch.get(field));
			} else if (leftACL.aclMatch.get(field) == PolicyFlowModStoreType.PREFIX) {
				policyACL.aclMatch.put(field, rightACL.aclMatch.get(field));
			} else if (rightACL.aclMatch.get(field) == PolicyFlowModStoreType.PREFIX) {
				policyACL.aclMatch.put(field, leftACL.aclMatch.get(field));
			} else if (leftACL.aclMatch.get(field) == PolicyFlowModStoreType.EXACT) {
				policyACL.aclMatch.put(field, rightACL.aclMatch.get(field));
			} else if (rightACL.aclMatch.get(field) == PolicyFlowModStoreType.EXACT) {
				policyACL.aclMatch.put(field, leftACL.aclMatch.get(field));
			}
		}

		// deal with action, union
		for (PolicyFlowModAction action : PolicyFlowModAction.values()) {
			policyACL.aclAction.put(action, leftACL.aclAction.get(action)
					|| rightACL.aclAction.get(action));
		}
		
        System.out.println(leftACL);
        System.out.println(rightACL);
        System.out.println(policyACL);

		// generate flow table for children
		if (operator == PolicyOperator.Parallel || operator == PolicyOperator.Sequential) {
			
			if (operator == PolicyOperator.Sequential) {
				leftACL = ACLApplyActToMatch(leftACL);
			}
			
			List<Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType>> keyTypes =
					new ArrayList<Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType>>();
			for (PolicyFlowModStoreKey field : PolicyFlowModStoreKey.values()) {
				
				System.out.println(field + "\tleft:" + leftACL.aclMatch.get(field) + "\tright:" + rightACL.aclMatch.get(field));
				
				if (leftACL.aclMatch.get(field) == PolicyFlowModStoreType.DISALLOW
						|| rightACL.aclMatch.get(field) == PolicyFlowModStoreType.DISALLOW) {
					continue;
				} else if (leftACL.aclMatch.get(field) == PolicyFlowModStoreType.WILDCARD
						|| rightACL.aclMatch.get(field) == PolicyFlowModStoreType.WILDCARD) {
					keyTypes.add(new Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType>(field, PolicyFlowModStoreType.WILDCARD));
				} else if (leftACL.aclMatch.get(field) == PolicyFlowModStoreType.PREFIX
						|| rightACL.aclMatch.get(field) == PolicyFlowModStoreType.PREFIX) {
					keyTypes.add(new Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType>(field, PolicyFlowModStoreType.PREFIX));
				} else {
					keyTypes.add(new Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType>(field, PolicyFlowModStoreType.EXACT));
				}
			}
			Collections.sort(keyTypes, new Comparator<Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType>>() {
				public int compare(Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType> keyType1,
						Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType> keyType2) {
					if (keyType1.second == PolicyFlowModStoreType.EXACT) {
						return 1;
					} else if (keyType2.second == PolicyFlowModStoreType.EXACT) {
						return -1;
					} else if (keyType1.second == PolicyFlowModStoreType.PREFIX) {
						return 1;
					} else if (keyType2.second == PolicyFlowModStoreType.PREFIX) {
						return -1;
					} else {
						return 0;
					}
				}
			});
			
			List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
			List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
			for (Tuple<PolicyFlowModStoreKey, PolicyFlowModStoreType> keyType : keyTypes) {
				storeKeys.add(keyType.first);
				storeTypes.add(keyType.second);
			}
			storeKeys.add(PolicyFlowModStoreKey.ALL);
			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
			if (operator == PolicyOperator.Sequential) {
				leftChild.flowTable = new PolicyFlowTable(storeTypes, storeKeys, true);
			} else {
				leftChild.flowTable = new PolicyFlowTable(storeTypes, storeKeys, false);
			}
			rightChild.flowTable = new PolicyFlowTable(storeTypes, storeKeys, false);
		} else { // override composition
			List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
			List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
			storeKeys.add(PolicyFlowModStoreKey.ALL);
			storeTypes.add(PolicyFlowModStoreType.WILDCARD);
			leftChild.flowTable = new PolicyFlowTable(storeTypes, storeKeys, false);
			rightChild.flowTable = new PolicyFlowTable(storeTypes, storeKeys, false);
		}
		
		return policyACL;
	}
	
	private static PolicyACL ACLApplyActToMatch (PolicyACL acl) {
		PolicyACL ret = acl.clone();
		for (PolicyFlowModAction action : PolicyFlowModAction.values()) {
			if (acl.aclAction.get(action)) {
				switch (action) {
				case DataLayerDestination:
					ret.aclMatch.put(PolicyFlowModStoreKey.DATA_DST, PolicyFlowModStoreType.EXACT);
					break;
				case DataLayerSource:
					ret.aclMatch.put(PolicyFlowModStoreKey.DATA_SRC, PolicyFlowModStoreType.EXACT);
					break;
				case NetworkLayerDestination:
					ret.aclMatch.put(PolicyFlowModStoreKey.NETWORK_DST, PolicyFlowModStoreType.EXACT);
					break;
				case NetworkLayerSource:
					ret.aclMatch.put(PolicyFlowModStoreKey.NETWORK_SRC, PolicyFlowModStoreType.EXACT);
					break;
				case TransportLayerDestination:
					ret.aclMatch.put(PolicyFlowModStoreKey.TRANSPORT_DST, PolicyFlowModStoreType.EXACT);
					break;
				case TransportLayerSource:
					ret.aclMatch.put(PolicyFlowModStoreKey.TRANSPORT_SRC, PolicyFlowModStoreType.EXACT);
					break;
				default:
					break;
				}
			}
		}
		return ret;
	}

}
