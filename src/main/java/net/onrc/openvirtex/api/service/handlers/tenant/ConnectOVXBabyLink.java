package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.HashMap;
import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXBabySwitch;
import net.onrc.openvirtex.elements.datapath.OVXMultiSwitch;
import net.onrc.openvirtex.elements.link.OVXBabyLink;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.InvalidDPIDException;
import net.onrc.openvirtex.exceptions.InvalidPortException;
import net.onrc.openvirtex.exceptions.InvalidTenantIdException;
import net.onrc.openvirtex.exceptions.MappingException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.exceptions.VirtualLinkException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class ConnectOVXBabyLink extends ApiHandler<Map<String, Object>> {

    Logger log = LogManager.getLogger(ConnectOVXLink.class.getName());

    @Override
    public JSONRPC2Response process(final Map<String, Object> params) {
        JSONRPC2Response resp = null;

        try {
            final Number tenantId = HandlerUtils.<Number>fetchField(
                    TenantHandler.TENANT, params, true, null);
            final Number srcDpid = HandlerUtils.<Number>fetchField(
                    TenantHandler.SRC_DPID, params, true, null);
            final Number srcPort = HandlerUtils.<Number>fetchField(
                    TenantHandler.SRC_PORT, params, true, null);
            final Number dstDpid = HandlerUtils.<Number>fetchField(
                    TenantHandler.DST_DPID, params, true, null);
            final Number dstPort = HandlerUtils.<Number>fetchField(
                    TenantHandler.DST_PORT, params, true, null);

            final OVXMap map = OVXMap.getInstance();
            final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId
                    .intValue());

            final OVXBabyLink virtualLink = virtualNetwork.connectBabyLink(
                    srcDpid.longValue(), srcPort.shortValue(),
                    dstDpid.longValue(), dstPort.shortValue());

            OVXMultiSwitch multiSwitch = ((OVXBabySwitch) (virtualNetwork.getSwitch((Long) srcDpid))).getParentSwitch();
            this.log.info(multiSwitch.getPlumbingGraph().getGraphString());

            this.log.info(
			        "Created bi-directional virtual link {} between ports {}/{} - {}/{} in virtual network {}",
			        virtualLink.linkId, virtualLink.srcSwitch, virtualLink.srcPort,
			        virtualLink.dstSwitch, virtualLink.dstPort, virtualNetwork.getTenantId());
			resp = new JSONRPC2Response(0);
        } catch (final MissingRequiredField e) {
            resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Unable to create virtual link : "
                            + e.getMessage()), 0);
        } catch (final VirtualLinkException e) {
            resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Invalid virtual link : " + e.getMessage()), 0);
        } catch (final InvalidTenantIdException e) {
            resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Invalid tenant id : " + e.getMessage()), 0);
        } catch (final IndexOutOfBoundException e) {
            resp = new JSONRPC2Response(
                    new JSONRPC2Error(
                            JSONRPC2Error.INVALID_PARAMS.getCode(),
                            this.cmdName()
                                    + ": Impossible to create the virtual link, too many links in this virtual network : "
                                    + e.getMessage()), 0);
        } catch (final InvalidPortException e) {
            resp = new JSONRPC2Response(new JSONRPC2Error(
                    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
                            + ": Invalid port : " + e.getMessage()), 0);
        } catch (final InvalidDPIDException e) {
            resp = new JSONRPC2Response(
                    new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(),
                            this.cmdName() + ": Invalid virtual switch id : "
                                    + e.getMessage()), 0);
        } catch (final MappingException e) {
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
