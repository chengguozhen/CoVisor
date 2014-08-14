package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.apache.commons.lang.StringUtils;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class CreateOVXMultiSwitch extends ApiHandler<Map<String, Object>> {

	Logger log = LogManager.getLogger(CreateOVXMultiSwitch.class.getName());

	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;

		try {
			final Number tenantId = HandlerUtils.<Number> fetchField(
					TenantHandler.TENANT, params, true, null);
			final Number physicalDpid = HandlerUtils.<Number> fetchField(
					TenantHandler.PHYSICAL_DPID, params, true, null);
			final Number numberOfBabySwitches = HandlerUtils
					.<Number> fetchField(TenantHandler.NUMBER_OF_BABY_SWITCHES,
							params, true, null);

			HandlerUtils.isValidTenantId(tenantId.intValue());

			final OVXMap map = OVXMap.getInstance();
			final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId.intValue());
			
			final List<Long> longDpids = new ArrayList<Long>();
			long longDpid = physicalDpid.longValue();
			longDpids.add(longDpid);
			HandlerUtils.isValidDPID(tenantId.intValue(), longDpids);
			
			final OVXMultiSwitch ovxMultiSwitch;
			ovxMultiSwitch = virtualNetwork.createMultiSwitch(longDpid,
					numberOfBabySwitches.intValue());

			if (ovxMultiSwitch == null) {
				resp = new JSONRPC2Response(
						new JSONRPC2Error(
								JSONRPC2Error.INTERNAL_ERROR.getCode(),
								this.cmdName()), 0);
			} else {
				List<String> babyDpids = new ArrayList<String>();
				for (OVXBabySwitch ovxBabySwitch : ovxMultiSwitch.getSwitches()) {
					babyDpids.add(ovxBabySwitch.getSwitchName());
				}
				this.log.info(
						"Created virtual switch {} in virtual network {} with internal baby switches {}",
						ovxMultiSwitch.getSwitchName(),
						virtualNetwork.getTenantId(),
						StringUtils.join(babyDpids, ", "));
				Map<String, Object> reply = new HashMap<String, Object>(
						ovxMultiSwitch.getDBObject());
				reply.put(TenantHandler.TENANT, ovxMultiSwitch.getTenantId());
				if (babyDpids.isEmpty()) {
					reply.put(TenantHandler.BABY_DPIDS, "none");
				} else {
					reply.put(TenantHandler.BABY_DPIDS,
							StringUtils.join(babyDpids, ", "));
				}
				resp = new JSONRPC2Response(reply, 0);
			}

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