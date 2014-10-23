package edu.princeton.cs.policy.adv;

import java.util.HashMap;
import java.util.Map;

import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModAction;

public class PolicyACL {
	
	public Map<PolicyFlowModStoreKey, PolicyFlowModStoreType> aclMatch;
	public Map<PolicyFlowModAction, Boolean> aclAction;
	
	public PolicyACL() {
		this.aclMatch = new HashMap<PolicyFlowModStoreKey, PolicyFlowModStoreType>();
		this.aclAction = new HashMap<PolicyFlowModAction, Boolean>();
		
		/*for (PolicyFlowModStoreKey field : PolicyFlowModStoreKey.values()) {
			this.aclMatch.put(field, PolicyFlowModStoreType.DISALLOW);
		}
		for (PolicyFlowModAction action : PolicyFlowModAction.values()) {
			this.aclAction.put(action, false);
		}*/
		
	}

}
