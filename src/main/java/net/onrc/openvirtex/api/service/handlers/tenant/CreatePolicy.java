package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import edu.princeton.cs.policy.PolicyTree;
import edu.princeton.cs.policy.PolicyTree.PolicyOperator;
import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;

public class CreatePolicy extends ApiHandler<Map<String, Object>> {

	public static int policyCount = 0;
	private Logger log = LogManager.getLogger(CreateOVXSwitch.class.getName());
	
	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;
		
		final String policy = (String) params.get("policy");
		final OVXMap map = OVXMap.getInstance();
		
		// hard code policy for now
		for (Entry<PhysicalSwitch, ConcurrentHashMap<Integer, OVXSwitch>> entry
				: map.getPhysicalSwitchMap().entrySet()) {
			
			PolicyTree policyTree = new PolicyTree();
			policyTree.operator = PolicyOperator.Sequential;
			
			PhysicalSwitch sw = entry.getKey();
			for (Entry<Integer, OVXSwitch> tenantSw : entry.getValue().entrySet()) {
				PolicyTree subPolicyTree = new PolicyTree();
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
