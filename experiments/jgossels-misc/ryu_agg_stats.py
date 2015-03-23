# Copyright (C) 2011 Nippon Telegraph and Telephone Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
# implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import logging
import struct

import thread
import time

from ryu.base import app_manager
from ryu.controller import mac_to_port
from ryu.controller import ofp_event
from ryu.controller.handler import MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_0
from ryu.lib.mac import haddr_to_str, haddr_to_bin

# Constants used to track contents of stats reply
FROM_H1 = -1
TO_H1 = 1


# TODO: we should split the handler into two parts, protocol
# independent and dependant parts.

# TODO: can we use dpkt python library?

# TODO: we need to move the followings to something like db


class SimpleSwitch(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_0.OFP_VERSION]

    def __init__(self, *args, **kwargs):
        super(SimpleSwitch, self).__init__(*args, **kwargs)
        self.mac_to_port = {}
        self.trigger_stats = False
        
        # Use pos or neg value to specify what type of reply message we're waiting for
        # negative value = info about traffic from h1 (see const def above)
        # positive value = info about traffic to h1 (see const def above)
        # Sync messages sent/received through boolean reply_received
        type_message = 0
        reply_received = False

    def add_flow(self, datapath, in_port, dst, actions):
        ofproto = datapath.ofproto

        wildcards = ofproto_v1_0.OFPFW_ALL
        wildcards &= ~ofproto_v1_0.OFPFW_IN_PORT
        wildcards &= ~ofproto_v1_0.OFPFW_DL_DST

        match = datapath.ofproto_parser.OFPMatch(
            wildcards, in_port, 0, dst,
            0, 0, 0, 0, 0, 0, 0, 0, 0)

        mod = datapath.ofproto_parser.OFPFlowMod(
            datapath=datapath, match=match, cookie=0,
            command=ofproto.OFPFC_ADD, idle_timeout=0, hard_timeout=0,
            priority=ofproto.OFP_DEFAULT_PRIORITY,
            flags=ofproto.OFPFF_SEND_FLOW_REM, actions=actions)

        datapath.send_msg(mod)

        # Trigger this flow only once when first flow is installed
        if not self.trigger_stats:
            self.trigger_stats = True
            thread.start_new_thread(self.send_flow_stats_request, (datapath,))


    @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    def _packet_in_handler(self, ev):
        msg = ev.msg
        datapath = msg.datapath
        ofproto = datapath.ofproto

        dst, src, _eth_type = struct.unpack_from('!6s6sH', buffer(msg.data), 0)

        dpid = datapath.id
        self.mac_to_port.setdefault(dpid, {})

        self.logger.info("packet in %s %s %s %s",
                         dpid, haddr_to_str(src), haddr_to_str(dst),
                         msg.in_port)

        # learn a mac address to avoid FLOOD next time.
        self.mac_to_port[dpid][src] = msg.in_port

        if dst in self.mac_to_port[dpid]:
            out_port = self.mac_to_port[dpid][dst]
        
        else:
            out_port = ofproto.OFPP_FLOOD

        actions = [datapath.ofproto_parser.OFPActionOutput(out_port)]

        # install a flow to avoid packet_in next time
        if out_port != ofproto.OFPP_FLOOD:
            # Install empty action set to switch to drop traffic btwn hosts
            if msg.in_port == 2 and (haddr_to_str(dst) == "00:00:00:00:00:03"):
                actions = []
            if msg.in_port == 3 and (haddr_to_str(dst) == "00:00:00:00:00:02"):
                actions = []            
            
            self.add_flow(datapath, msg.in_port, dst, actions)

        out = datapath.ofproto_parser.OFPPacketOut(
            datapath=datapath, buffer_id=msg.buffer_id, in_port=msg.in_port,
            actions=actions)

        datapath.send_msg(out)

    @set_ev_cls(ofp_event.EventOFPPortStatus, MAIN_DISPATCHER)
    def _port_status_handler(self, ev):
        msg = ev.msg
        reason = msg.reason
        port_no = msg.desc.port_no

        ofproto = msg.datapath.ofproto
        if reason == ofproto.OFPPR_ADD:
            self.logger.info("port added %s", port_no)
        elif reason == ofproto.OFPPR_DELETE:
            self.logger.info("port deleted %s", port_no)
        elif reason == ofproto.OFPPR_MODIFY:
            self.logger.info("port modified %s", port_no)
        else:
            self.logger.info("Illeagal port state %s %s", port_no, reason)


    def send_flow_stats_request(self, datapath):
        ofp = datapath.ofproto
        ofp_parser = datapath.ofproto_parser
        
        wildcards = ofproto_v1_0.OFPFW_ALL
        wildcards &= ~ofproto_v1_0.OFPFW_IN_PORT
        wildcards &= ~ofproto_v1_0.OFPFW_DL_DST
        
        # background thread will drop into this infinite loop
        while True:
            # match on all traffic coming from H1
            match = ofp_parser.OFPMatch(in_port=1)

            req = ofp_parser.OFPAggregateStatsRequest(datapath,
                                             0,
                                             match, 0xff,
                                             ofp.OFPP_NONE)
            # keep track of state so we know what message we're replying to
            self.type_message = FROM_H1
            self.reply_received = False
            
            # send message requesting stats about flows from H1
            datapath.send_msg(req)
            time.sleep(5)

            # wait until stats reply handler is done processing the info before thread moves on
            while self.reply_received is False:
                time.sleep(1)
            
            # Rinse and repeat, only ask for info about flows destined to H1  
            match = ofp_parser.OFPMatch(dl_dst=haddr_to_bin("00:00:00:00:00:01"))
            req = ofp_parser.OFPAggregateStatsRequest(datapath,
                                             0,
                                             match, 0xff,
                                             ofp.OFPP_NONE)
            
            # keep track of state so we know what message we're replying to
            self.type_message = TO_H1
            self.reply_received = False
            
            # send message requesting stats about flows destined to H1
            datapath.send_msg(req)
            time.sleep(5)

            # wait until stats reply handler is done processing the info before thread moves on
            while self.reply_received is False:
                time.sleep(1)

    @set_ev_cls(ofp_event.EventOFPAggregateStatsReply, MAIN_DISPATCHER)
    def aggregate_stats_reply_handler(self, ev):
        flows_from_h1 = []
        flows_to_h1 = []
        
        # if message type about traffic coming from H1, print appropriate counter
        if self.type_message == FROM_H1:
            # print the counter that's incrementing on the switch
            for stat in ev.msg.body:
                flows_from_h1.append('packet_count=%d byte_count=%d '
                         %  (stat.packet_count, stat.byte_count))
            self.logger.info('FlowStats from H1: %s', flows_from_h1)
            
            # acknowledge reciept so thread can continue
            self.reply_received = True

        # if message type is about traffic destined to  H1, print appropriate counter
        if self.type_message == TO_H1:
            # print the counter that's incrementing on the switch
            for stat in ev.msg.body:
                flows_to_h1.append('packet_count=%d byte_count=%d '
                         %  (stat.packet_count, stat.byte_count))
            self.logger.info('FlowStats to H1: %s', flows_to_h1)
            
            # acknowledge reciept so thread can continue
            self.reply_received = True
