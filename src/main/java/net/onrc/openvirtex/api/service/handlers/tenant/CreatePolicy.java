package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;

public class CreatePolicy extends ApiHandler<Map<String, Object>> {

	public static int policyCount = 0;
	private Logger log = LogManager.getLogger(CreateOVXSwitch.class.getName());
	
	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;
		
		final String policy = (String) params.get("policy");
		
		this.log.info("create policy {}",
                policy);
        resp = new JSONRPC2Response(0);
		
		return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
