package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.Map;
import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import edu.princeton.cs.policy.PolicyTree;
import edu.princeton.cs.policy.PolicyTree.PolicyUpdateMechanism;

public class SetComposeAlgo extends ApiHandler<Map<String, Object>> {

	private Logger log = LogManager.getLogger(SetComposeAlgo.class.getName());
	
	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {

		final String algo = (String) params.get("algo");
		if (algo.equals("strawman")) {
			PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Strawman;
		} else {
			PolicyTree.UPDATEMECHANISM = PolicyUpdateMechanism.Incremental;
		}
		
		this.log.info("set compose algorithm to {}", PolicyTree.UPDATEMECHANISM);
		return new JSONRPC2Response(0);
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
