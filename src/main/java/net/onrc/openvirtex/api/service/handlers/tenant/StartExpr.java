package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import edu.princeton.cs.expr.ParallelComposition;
import edu.princeton.cs.expr.SequentialComposition;
import edu.princeton.cs.expr.VirtualTopology;

public class StartExpr extends ApiHandler<Map<String, Object>> {

	private Logger log = LogManager.getLogger(StartExpr.class.getName());
	
	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;
		
		final String exprString = (String) params.get("expr");
		this.log.info("start expr {}", exprString);
		
		if (exprString.equals("parallel")) {
			ParallelComposition expr = new ParallelComposition();
			expr.testExpr();
		} else if (exprString.equals("sequential")) {
			SequentialComposition expr = new SequentialComposition();
			expr.testExpr();
		} else if (exprString.equals("gateway")) {
			VirtualTopology expr = new VirtualTopology();
			expr.testExpr();
		}
		
        resp = new JSONRPC2Response(0);
		return resp;
	}
	

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
