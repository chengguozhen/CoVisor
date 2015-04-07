#!/usr/bin/python
import sys
import subprocess
import time
import random
from ExprTopo.mtopo import *

#********************************************************************
# Demo App
#********************************************************************
class DemoMonitorApp():

    def __init__(self, topo):
        self.graph = topo.graph
        switch = self.graph.nodes()[0]
        self.dpid = self.graph.node[switch]['vdpid']
        self.rules = []

        rule = '{"switch":"%s", ' % self.dpid + \
                '"name":"DemoMinitor0", ' + \
                '"priority":"0", ' + \
                '"active":"true", "actions":""}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"DemoMonitor1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"src-ip":"1.0.0.0/24", ' + \
            '"active":"true", "actions":""}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % self.dpid + \
            '"name":"ARP", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2054", ' + \
            '"active":"true", "actions":"output=flood"}'
        self.rules.append(rule)

    # Trying to record time to execute the command.
    def send_query(self, stat_type, time_log="time.txt"):
        ending = "1> /dev/null 2>> %s" # print just time output
        # ending = ">> %s 2>&1" # also print stats response
        cmd = "(time curl http://localhost:10001/wm/core/switch/%s/%s/json) " \
              % (self.dpid, stat_type)
        cmd = cmd + ending % time_log
        subprocess.call(cmd, shell=True)

    def send_queries(self, stat_type, time_log="time.txt", n=300000):
        for i in range(n):
            cmd = "curl http://localhost:10001"
            cmd = cmd + "/wm/core/switch/%s/%s/json > /dev/null" \
                  % (self.dpid, stat_type)
            # Print one of each 1000.
            if (i % 1000) == 0:
                ending = "1> /dev/null 2>> %s"  # print just time output
                # ending = ">> %s 2>&1"  # also print stats response
                cmd = "(time curl http://localhost:10001"
                cmd = cmd + "/wm/core/switch/%s/%s/json) " \
                      % (self.dpid, stat_type)
                cmd = cmd + ending % time_log
            subprocess.call(cmd, shell=True)

    def installRules(self):
        for rule in self.rules:
            print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

class DemoRouterApp():

    def __init__(self, topo):
        self.graph = topo.graph
        switch = self.graph.nodes()[0]
        dpid = self.graph.node[switch]['vdpid']
        self.rules = []

        rule = '{"switch":"%s", ' % dpid + \
                '"name":"DemoRouter0", ' + \
                '"priority":"0", ' + \
                '"active":"true", "actions":""}'
        self.rules.append(rule)

        #rule = '{"switch":"%s", ' % dpid + \
        #    '"name":"DemoRouter1", ' + \
        #    '"priority":"1", ' + \
        #    '"ether-type":"2048", ' + \
        #    '"dst-ip":"2.0.0.1", ' + \
        #    '"active":"true", "actions":"output=1"}'
        #self.rules.append(rule)

        #rule = '{"switch":"%s", ' % dpid + \
        #    '"name":"DemoRouter2", ' + \
        #    '"priority":"1", ' + \
        #    '"ether-type":"2048", ' + \
        #    '"dst-ip":"2.0.0.2", ' + \
        #    '"active":"true", "actions":"output=2"}'
        #self.rules.append(rule)
        for i in range(topo.hostPerSw):
            rule = '{"switch":"%s", ' % dpid + \
                   '"name":"DemoRouter%s", ' % (i + 1) + \
                   '"priority":"1", ' + \
                   '"ether-type":"2048", ' + \
                   '"dst-ip":"1.0.0.%s", ' % (i + 1) + \
                   '"active":"true", "actions":"output=%d"}' % (i + 1)
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
            cmd = "curl -d '%s' http://localhost:20001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

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

