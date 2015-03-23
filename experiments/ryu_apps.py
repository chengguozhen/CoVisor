#!/usr/bin/python
import sys
import subprocess
import time
import random
from ExprTopo.mtopo import *

#********************************************************************
# Demo App
#********************************************************************
class RyuMonitorApp():

    def __init__(self, topo):
        self.graph = topo.graph
        switch = self.graph.nodes()[0]
        self.dpid = self.graph.node[switch]['vdpid']
        self.rules = []

        rule = '{"dpid":"%s", ' % self.dpid + \
                '"priority":"0", ' + \
                '"actions":[]}'
        self.rules.append(rule)

        rule = '{"dpid":"%s", ' % self.dpid + \
            '"priority":"1", ' + \
            '"match":{"dl_type":"2048", "nw_src":"1.0.0.0/24"}, ' + \
            '"actions":[]}'
        self.rules.append(rule)

        rule = '{"dpid":"%s", ' % self.dpid + \
            '"priority":1, ' + \
            '"match":{"dl_type":"2054"}, ' + \
            '"actions":[{"type":"OUTPUT",' + \
            '"port":OFPP_FLOOD}]}'
        self.rules.append(rule)

    def send_query(self, stat_type="flow"):
        if "agg" in stat_type:
            stat_type = "aggregateflow"
        cmd = "curl -X GET http://localhost:10001/stats/%s/%s" \
              % (self.dpid, stat_type)
        subprocess.call(cmd, shell=True)

    def installRules(self):
        url = "http://localhost:10001/stats/flowentry/add"
        for rule in self.rules:
            print rule
            cmd = "curl -X POST -d '%s' %s" % (rule, url)
            subprocess.call(cmd, shell=True)
            print ""

class RyuRouterApp():

    def __init__(self, topo):
        self.graph = topo.graph
        switch = self.graph.nodes()[0]
        dpid = self.graph.node[switch]['vdpid']
        self.rules = []

        rule = '{"dpid":"%s", ' % dpid + \
                '"priority":0, ' + \
                '"actions":[]}'
        self.rules.append(rule)

        for i in range(topo.hostPerSw):
            rule = '{"dpid":"%s", ' % dpid + \
                   '"priority":1, ' + \
                   '"match":{"dl_type":2048, "nw_src":"1.0.0.%s"}, ' % (i + 1) + \
                   '"actions":[{"type":"OUTPUT", "port":%d}]}' % (i + 1)
            self.rules.append(rule)

        rule = '{"dpid":"%s", ' % dpid + \
            '"priority":1, ' + \
            '"match":{"dl_type":2054}, ' + \
            '"actions":[{"type":"OUTPUT",' + \
            '"port":OFPP_FLOOD}]}'
        self.rules.append(rule)

    def installRules(self):
        url = "http://localhost:20001/stats/flowentry/add"
        for rule in self.rules:
            print rule
            cmd = "curl -X POST -d '%s' %s" % (rule, url)
            subprocess.call(cmd, shell=True)
            print ""

################################################################################
# 22 March
# These apps not modified for Ryu.
################################################################################

class DemoLoadBalancerApp():

    def __init__(self, topo):
        self.graph = topo.graph
        switch = self.graph.nodes()[0]
        dpid = self.graph.node[switch]['vdpid']
        self.rules = []

        rule = '{"switch":"%s", ' % dpid + \
                '"name":"DemoLB0", ' + \
                '"priority":"0", ' + \
                '"active":"true", "actions":""}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoLB1", ' + \
            '"priority":"3", ' + \
            '"ether-type":"2048", ' + \
            '"src-ip":"0.0.0.0/2", ' + \
            '"dst-ip":"3.0.0.0", ' + \
            '"active":"true", "actions":"set-dst-ip=2.0.0.1"}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoLB2", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"3.0.0.0", ' + \
            '"active":"true", "actions":"set-dst-ip=2.0.0.2"}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % dpid + \
            '"name":"ARP", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2054", ' + \
            '"active":"true", "actions":"output=flood"}'
        self.rules.append(rule)

    def installRules(self):
        for rule in self.rules:
            print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

class DemoVirtApp():

    def __init__(self, topo):
        self.graph = topo.graph

        self.dpid = "00:a4:23:05:00:00:00:01"
        self.rules1 = []
        rule = '{"switch":"%s", ' % self.dpid + \
                '"name":"DemoVirt1R0", ' + \
                '"priority":"0", ' + \
                '"active":"true", "actions":""}'
        self.rules1.append(rule)
        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"DemoVirt1R1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/8", ' + \
            '"active":"true", "actions":"output=3"}'
        self.rules1.append(rule)
        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"DemoVirt1R2", ' + \
            '"priority":"4", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"2.0.0.0/16", ' + \
            '"active":"true", "actions":"output=2"}'
        self.rules1.append(rule)

        #dpid = "00:a4:23:05:00:00:00:01"
        self.rules2 = []
        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"DemoVirt2R1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.1.1.1/4", ' + \
            '"active":"true", "actions":"set-dst-ip=2.0.0.0,output=3"}'
        self.rules2.append(rule)
        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"DemoVirt2R2", ' + \
            '"priority":"6", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/24", ' + \
            '"active":"true", "actions":"output=2"}'
        self.rules2.append(rule)

        #dpid = "00:a4:23:05:00:00:00:01"
        self.rules3 = []
        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"DemoVirt3R1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/8", ' + \
            '"active":"true", "actions":"output=3"}'
        self.rules3.append(rule)
        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"DemoVirt3R2", ' + \
            '"priority":"4", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"2.0.0.0/16", ' + \
            '"active":"true", "actions":"output=2"}'
        self.rules3.append(rule)

        arp = '{"switch":"%s", ' % self.dpid + \
            '"name":"ARP", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2054", ' + \
            '"active":"true", "actions":"output=controller"}'
        self.rules1.append(arp)
        self.rules2.append(arp)
        self.rules3.append(arp)

    def installRules(self):
        for rule in self.rules1:
            print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""
        for rule in self.rules2:
            print rule
            cmd = "curl -d '%s' http://localhost:20001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""
        for rule in self.rules3:
            print rule
            cmd = "curl -d '%s' http://localhost:30001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

    def send_query(self, stat_type):
        cmd = "curl http://localhost:10001/wm/core/switch/%s/%s/json" \
              % (self.dpid, stat_type)
        #cmd = "curl http://localhost:10001/wm/topology/links/json"
        subprocess.call(cmd, shell=True)

