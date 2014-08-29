package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;
import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;

public class CreatePolicy extends ApiHandler<Map<String, Object>> {

	public static int policyCount = 0;
	private Logger log = LogManager.getLogger(CreatePolicy.class.getName());
	
	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;
		
		// parse policy
		final String policy = (String) params.get("policy");
		
		PolicyOperator policyOperator = PolicyOperator.Parallel;
		if (policy.charAt(0) == '0') {
			policyOperator = PolicyOperator.Parallel;
		} else if (policy.charAt(0) == '1') {
			policyOperator = PolicyOperator.Sequential;
		} else if (policy.charAt(0) == '2') {
			policyOperator = PolicyOperator.Override;
		}
		
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
    	List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
    	if (policy.charAt(1) == '0') {
    		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    		storeKeys.add(PolicyFlowModStoreKey.ALL);
		} else if (policy.charAt(1) == '1') {
			storeTypes.add(PolicyFlowModStoreType.EXACT);
    		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    		
    		storeKeys.add(PolicyFlowModStoreKey.DATA_DST);
    		storeKeys.add(PolicyFlowModStoreKey.ALL);
		} else if (policy.charAt(1) == '2') {
			storeTypes.add(PolicyFlowModStoreType.PREFIX);
    		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
    		
    		storeKeys.add(PolicyFlowModStoreKey.NETWORK_DST);
    		storeKeys.add(PolicyFlowModStoreKey.ALL);
		}
    	
		// install policy
		final OVXMap map = OVXMap.getInstance();
		for (Entry<PhysicalSwitch, ConcurrentHashMap<Integer, OVXSwitch>> entry
				: map.getPhysicalSwitchMap().entrySet()) {
			
			PolicyTree policyTree = new PolicyTree();
			policyTree.operator = policyOperator;
			
			PhysicalSwitch sw = entry.getKey();
			for (Entry<Integer, OVXSwitch> tenantSw : entry.getValue().entrySet()) {
				PolicyTree subPolicyTree = new PolicyTree(storeTypes, storeKeys);
				subPolicyTree.tenantId = tenantSw.getKey();
				//if (policyTree.leftChild == null) {
				if (subPolicyTree.tenantId == 1) {
					policyTree.leftChild = subPolicyTree;
				}
				else {
					policyTree.rightChild = subPolicyTree;
				}
			}
			sw.ConfigurePolicy(policyTree);

		}
		
		this.log.info("create policy {}", policy);
        resp = new JSONRPC2Response(0);
		return resp;
	}
	

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
