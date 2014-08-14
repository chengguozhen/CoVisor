package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXBabyPort;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.datapath.OVXMultiSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.InvalidDPIDException;
import net.onrc.openvirtex.exceptions.InvalidPortException;
import net.onrc.openvirtex.exceptions.InvalidTenantIdException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.exceptions.SwitchMappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class CreateOVXBabyPort extends ApiHandler<Map<String, Object>> {

	Logger log = LogManager.getLogger(CreateOVXBabyPort.class.getName());

	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		JSONRPC2Response resp = null;

		try {
			final Number tenantId = HandlerUtils.<Number> fetchField(
					TenantHandler.TENANT, params, true, null);
			final Number babyDpid = HandlerUtils.<Number> fetchField(
					TenantHandler.BABY_DPID, params, true, null);
			// OVXBabyPort does not need to map to PhysicalPort.
			final Number pport = HandlerUtils.<Number> fetchField(
					TenantHandler.PPORT, params, true, null);

			HandlerUtils.isValidTenantId(tenantId.intValue());

			final OVXMap map = OVXMap.getInstance();
			final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId
					.intValue());
			boolean pportExists = pport.intValue() != 0;
			/*if (pportExists) {
				OVXMultiSwitch multiSwitch = ((OVXBabySwitch) virtualNetwork
						.getSwitch(babyDpid.longValue())).getParentSwitch();
				List<PhysicalSwitch> physicalSwitches = map
						.getPhysicalSwitches(multiSwitch);
				if (physicalSwitches.size() != 1) {
					throw new SwitchMappingException(
							"OVXMultiSwitch should be associated "
									+ "with exactly one PhysicalSwitch.");
				}
				PhysicalSwitch physicalSwitch = physicalSwitches.get(0);
				HandlerUtils.isValidPhysicalPort(tenantId.intValue(),
						physicalSwitch.getSwitchId(), pport.shortValue());
			}*/

			final OVXBabyPort ovxBabyPort;
			if (pportExists) {
				ovxBabyPort = virtualNetwork.createBabyPort(babyDpid.longValue(), pport.shortValue());
			} else {
				ovxBabyPort = virtualNetwork.createBabyPort(babyDpid.longValue());
			}

			if (ovxBabyPort == null) {
				resp = new JSONRPC2Response(
						new JSONRPC2Error(
								JSONRPC2Error.INTERNAL_ERROR.getCode(),
								this.cmdName()), 0);
			} else {
				this.log.info(
						"Created virtual port {} on virtual switch {} in virtual network {}",
						ovxBabyPort.getPortNumber(), ovxBabyPort
								.getParentSwitch().getSwitchName(),
						virtualNetwork.getTenantId());
				Map<String, Object> reply = new HashMap<String, Object>(
						ovxBabyPort.getDBObject());
				reply.put(TenantHandler.VDPID, ovxBabyPort.getParentSwitch()
						.getSwitchId());
				reply.put(TenantHandler.TENANT, ovxBabyPort.getTenantId());
				resp = new JSONRPC2Response(reply, 0);
			}
		} catch (final MissingRequiredField e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Unable to create virtual port : "
							+ e.getMessage()), 0);
		} catch (final InvalidPortException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Invalid port : " + e.getMessage()), 0);
		} catch (final InvalidTenantIdException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Invalid tenant id : " + e.getMessage()), 0);
		} catch (final IndexOutOfBoundException e) {
			resp = new JSONRPC2Response(
					new JSONRPC2Error(
							JSONRPC2Error.INVALID_PARAMS.getCode(),
							this.cmdName()
									+ ": Impossible to create the virtual port, too many ports on this virtual switch : "
									+ e.getMessage()), 0);
		} catch (final InvalidDPIDException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Invalid physical dpid : " + e.getMessage()), 0);
		} catch (final NetworkMappingException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": " + e.getMessage()), 0);
		} catch (final SwitchMappingException e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": " + e.getMessage()), 0);
		}
		return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
