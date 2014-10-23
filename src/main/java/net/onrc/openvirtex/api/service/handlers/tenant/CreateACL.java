package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class CreateACL extends ApiHandler<Map<String, Object>> {
	
	private Logger log = LogManager.getLogger(CreateACL.class.getName());
	
	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;
		this.log.info("enter create acl");
		try {
			final Integer tenantId = HandlerUtils.<Number>fetchField(
                    TenantHandler.TENANT, params, true, null).intValue();
			final String aclMatch = HandlerUtils.<String> fetchField(
					TenantHandler.ACL_MATCH, params, true, null);
			final String aclAction = HandlerUtils.<String> fetchField(
					TenantHandler.ACL_ACTION, params, true, null);
			this.log.info("create acl {} {} {}", tenantId, aclMatch, aclAction);
			
			/*final PhysicalSwitch physicalSwitch = PhysicalNetwork.getInstance().getSwitch(dpid);
			final PlumbingSwitch plumbingSwitch = physicalSwitch.getPlumbingGraph().getNode(plumbingSwitchId);
			plumbingSwitch.createPolicy(policy);
			this.log.info("create policy {} {} {}", dpid, plumbingSwitchId, policy);*/
			
		} catch (Exception e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + e.getMessage()), 0);
			this.log.info(e.getMessage());
		}
		
        resp = new JSONRPC2Response(0);
		return resp;
	}
	

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
