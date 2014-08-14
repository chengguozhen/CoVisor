package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.HashMap;
import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.datapath.OVXMultiSwitch;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.InvalidDPIDException;
import net.onrc.openvirtex.exceptions.InvalidTenantIdException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.exceptions.NetworkMappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class CreateOVXBabySwitch extends ApiHandler<Map<String, Object>> {

	Logger log = LogManager.getLogger(CreateOVXBabySwitch.class.getName());

	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;

		try {
			final Number tenantId = HandlerUtils.<Number> fetchField(
					TenantHandler.TENANT, params, true, null);
			final Number multiSwitchDpid = HandlerUtils.<Number> fetchField(
					TenantHandler.MULTI_DPID, params, true, null);
			final Number babyTenantId = HandlerUtils.<Number> fetchField(
					TenantHandler.BABY_TENANT, params, true, null);
			
			this.log.error("{} {} {}", tenantId, multiSwitchDpid, babyTenantId);

			// get multiSwitch
			final OVXMap map = OVXMap.getInstance();
			final OVXNetwork virtualNetworkMultiSwitch = map.getVirtualNetwork(tenantId.intValue());
			final OVXMultiSwitch ovxMultiSwitch = (OVXMultiSwitch) virtualNetworkMultiSwitch.getSwitch(multiSwitchDpid.longValue());
			
			// create babySwitch
			final OVXNetwork virtualNetworkBabySwitch = map.getVirtualNetwork(babyTenantId.intValue());
			OVXBabySwitch ovxBabySwitch = virtualNetworkBabySwitch
					.createBabySwitch(virtualNetworkMultiSwitch.getNewDpid(), ovxMultiSwitch);
			
			// generate response
			this.log.info(
					"Created baby switch {} in virtual network {} for multi switches {} in virtual network {}",
					ovxBabySwitch.getSwitchName(),
					ovxBabySwitch.getTenantId(),
					ovxMultiSwitch.getSwitchName(),
					ovxMultiSwitch.getTenantId());
			Map<String, Object> reply = new HashMap<String, Object>(ovxMultiSwitch.getDBObject());
			reply.put(TenantHandler.TENANT, ovxBabySwitch.getTenantId());
			reply.put(TenantHandler.BABY_DPID, ovxBabySwitch.getSwitchName());
			resp = new JSONRPC2Response(reply, 0);
		} catch (final MissingRequiredField e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Unable to create virtual switch : "
							+ e.getMessage()), 0);
		} catch (final InvalidDPIDException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Invalid DPID : " + e.getMessage()), 0);
		} catch (final InvalidTenantIdException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Invalid tenant id : " + e.getMessage()), 0);
		} catch (final NetworkMappingException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": " + e.getMessage()), 0);
		} catch (final IndexOutOfBoundException e) {
			resp = new JSONRPC2Response(
                    new JSONRPC2Error(
                            JSONRPC2Error.INVALID_PARAMS.getCode(),
                            this.cmdName()
                                    + ": Impossible to create the virtual switch, "
                                    + "too many switches in this virtual network : "
                                    + e.getMessage()), 0);
		}
		return resp;

	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}