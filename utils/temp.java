public class temp {

    public void devirtualize(final OVXSwitch sw) {

        if (this.match.getDataLayerType() == Ethernet.TYPE_LLDP) {
            return;
        }

        this.sw = sw;
        FlowTable ft = this.sw.getFlowTable();

        int bufferId = OVXPacketOut.BUFFER_ID_NONE;
        if (sw.getFromBufferMap(this.bufferId) != null) {
            bufferId = sw.getFromBufferMap(this.bufferId).getBufferId();
        }
        final short inport = this.getMatch().getInputPort();

        boolean pflag = ft.handleFlowMods(this.clone());

        OVXMatch ovxMatch = new OVXMatch(this.match);
        ovxCookie = ((OVXFlowTable) ft).getCookie(this, false);
        ovxMatch.setCookie(ovxCookie);
        this.setCookie(ovxMatch.getCookie());

        for (final OFAction act: this.getActions()) {
            try {
                ((VirtualizableAction) act).virtualize(sw, this.approvedActions,
                    ovxMatch);
            } catch () {
                return;
            }
        }

        final OVXPort ovxInPort = sw.getPort(inport);
        this.setBufferId(bufferId);

        if (ovxInPort == null) {
            if (this.match.getWildcardObj().isWildcarded(Flag.IN_PORT)) {
                for (OVXPort iport : sw.getPorts().values()) {
                    int wcard = this.match.getWildcards() &
                    (~OFMatch.OFPFW_IN_PORT);
                    this.match.setWildcards(wcard);
                    prepAndSendSouth(iport, pflag);
                }
            }
            else {
                unknown port;
                return;
            }
        } else {
            prepAndSendSouth(ovxInPort, pflag);
        }
    }


    private void prepAndSendSouth(OVXPort inPort, boolean pflag) {
    
        if (!inPort.isActive()) {
            return;
        }
        this.getMatch().setInputPort(inPort.getPhysicalPortNumber());
        OVXMessage.Util.translateXid(this, inPort);
        try {
            if (inPort.isEdge()) {
                this.prependRewriteActions();
            } else {
                IPMapper.rewriteMatch(sw.getTenantId(), this.match);
                if (inPort != null && inPort.isLink() &&
                    (!this.match.getWildcardObj().isWildcarded(Flag.DL_DST) ||
                    !this.match.getWildcardObj().isWildcarded(Flag.DL_SRC))) {
                    
                    OVXPort dstPort =
                    sw.getMap().getVirtualNetwork(sw.getTenantId()).getNeighborPort(inPort);
                    OVXLink link =
                    sw.getMap().getVirtualNetwork(sw.getTenantId()).getLink(dstPort,
                    inPort);

                    if (inPort != null && link != null) {
                        Integer flowId =
                        sw.getMap().getVirtualNetwork(sw.getTenantId()).getFlowManager().getFlowId(this.match.getDataLayerSource,
                        this.match.getDataLayerDestination());
                        OVXLinkUtils lUtils = new OVXLinkUtils(sw.getTenantId(),
                        link.getLinkId(), flowId);
                        lUtils.rewriteMatch(this.getMatch());
                    }
                }
            }

        }

        this.computeLength();
        if(pflag) {
            this.flags |= OFFlowMod.OFPFF_SEND_FLOW_REM;
            sw.sendSouth(this, inPort);
            
            }

    }

    



}
