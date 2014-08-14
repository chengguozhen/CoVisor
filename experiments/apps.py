#!/usr/bin/python
import sys
import subprocess
import time
import random
from ExprTopo.mtopo import *

#********************************************************************
# SDX App
#********************************************************************
class SDXApp():

    def __init__(self):
        self.rules = {}

        dpid = "00:a4:23:05:00:00:00:02"
        self.rules[dpid] = {}
        self.genDefaultRuleHelper("s1d0", dpid)
        self.genRoutingRuleHelper("s1r1", dpid, "1", "1.0.0.0", 1)
        self.genRoutingRuleHelper("s1r2", dpid, "1", "2.0.0.0", 2)
        self.genRoutingRuleHelper("s1r3", dpid, "1", "3.0.0.0", 3)

        dpid = "00:a4:23:05:00:00:00:03"
        self.rules[dpid] = {}
        self.genDefaultRuleHelper("s2d0", dpid)
        self.genRoutingRuleHelper("s2r1", dpid, "1", "1.0.0.0", 5)
        self.genRoutingRuleHelper("s2r2", dpid, "1", "2.0.0.0", 4)
        self.genRoutingRuleHelper("s2r3", dpid, "1", "3.0.0.0", 6)

        dpid = "00:a4:23:05:00:00:00:04"
        self.rules[dpid] = {}
        self.genDefaultRuleHelper("s3d0", dpid)
        self.genRoutingRuleHelper("s3r1", dpid, "1", "1.0.0.0", 9)
        self.genRoutingRuleHelper("s3r2", dpid, "1", "2.0.0.0", 8)
        self.genRoutingRuleHelper("s3r3", dpid, "1", "3.0.0.0", 7)

    def genDefaultRuleHelper(self, name, dpid):
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"%s", ' % name + \
            '"priority":"0", ' + \
            '"active":"true", "actions":""}'
        self.rules[dpid][name] = rule

    def genRoutingRuleHelper(self, name, dpid, priority, dstip, outport):
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"%s", ' % name + \
            '"priority":"%s", ' % priority + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"%s", ' % dstip + \
            '"active":"true", "actions":"output=%s"}' % outport
        self.rules[name] = rule
        self.rules[dpid][name] = rule

    def installRules(self):
        dpid = "00:a4:23:05:00:00:00:02"
        self.installRulesHelper(dpid, "10001")
        dpid = "00:a4:23:05:00:00:00:03"
        self.installRulesHelper(dpid, "10001")
        dpid = "00:a4:23:05:00:00:00:04"
        self.installRulesHelper(dpid, "20001")

    def installRulesHelper(self, dpid, port):
        for rule in self.rules[dpid].values():
            print rule
            #cmd = "curl -d '%s' http://localhost:%s/wm/staticflowentrypusher/json" % (rule, port)
            #subprocess.call(cmd, shell=True)
            print ""


#********************************************************************
# Routing App
#********************************************************************
class RoutingApp():

    def __init__(self, topo, prefixFile, perSwRule = 100):
        self.graph = topo.graph
        self.perSwRule = perSwRule
        self.rules = {}
        self.subnets = []

        f = open(prefixFile, 'r')
        oneline = f.readline()
        while oneline != "":
            self.subnets.append(oneline.strip())
            oneline = f.readline()
        f.close()
        random.shuffle(self.subnets)

    def genRules(self):
        for switch in self.graph.nodes():
            ridx = self.graph.node[switch]['ridx']
            vdpid = self.graph.node[switch]['vdpid']
            
            # default rule
            name = "RouteAppS%dD0" % ridx
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"0", ' + \
                '"active":"true", "actions":""}'
            self.rules[name] = rule
            # routing rules
            for index, subnet in enumerate(self.subnets[0:self.perSwRule]):
                name = "RouteAppS%dR%d" % (ridx, index)
                rule = '{"switch":"%s", ' % vdpid + \
                    '"name":"%s", ' % name + \
                    '"priority":"%s", ' % subnet.split('/')[1] + \
                    '"ether-type":"2048", ' + \
                    '"dst-ip":"%s", ' % subnet + \
                    '"active":"true", "actions":"output=1"}'
                self.rules[name] = rule

    def installRules(self):
        for rule in self.rules.values():
            cmd = "curl -d '%s' http://localhost:20001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

#********************************************************************
# Firewall App
#********************************************************************
class FirewallApp():

    def __init__(self, topo, classbenchFile):
        self.graph = topo.graph
        self.rules = {}
        self.ruleCount = 0

        self.addRules = {}
        self.addRuleCount = 5

        random.seed(1)
        self.metaRules = []
        f = open(classbenchFile, 'r')
        oneline = f.readline()
        while oneline != "":
            temp = oneline.strip().split('\t')
            rule = '"priority":"%d", ' % random.randint(1, 100) + \
                '"ether-type":"2048", ' + \
                '"src-ip":"%s", ' % temp[0][1:] + \
                '"dst-ip":"%s", ' % temp[1]

            srcports = [int(x) for x in temp[2].split(':')]
            if srcports[0] == srcports[1]:
                rule = rule + '"src-port":"%d", ' % srcports[0]

            dstports = [int(x) for x in temp[3].split(':')]
            if dstports[0] == dstports[1]:
                rule = rule + '"dst-port":"%d", ' % dstports[0]

            protocol = temp[4].split('/')
            if protocol[1] == '0xFF':
                rule = rule + '"protocol":"%s", ' % int(protocol[0], 0)
            
            if random.randint(0, 1) == 0:
                rule = rule + '"active":"true", "actions":""}' 
            else:
                rule = rule + '"active":"true", "actions":"output=1"}' 

            self.metaRules.append(rule)
            self.ruleCount += 1
            oneline = f.readline()
        f.close()

    def genRules(self):
        for switch in self.graph.nodes():
            ridx = self.graph.node[switch]['ridx'] 
            vdpid = self.graph.node[switch]['vdpid']

            # default rule
            name = "RouteAppS%dD0" % ridx
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"0", ' + \
                '"active":"true", "actions":""}'
            self.rules[name] = rule

            # firewall rule
            for index, metaRule in enumerate(self.metaRules[:-self.addRuleCount]):
                name = "FWAppS%dR%d" % (ridx, index)
                rule = '{"switch":"%s", ' % vdpid + \
                            '"name":"%s", ' % name + \
                            metaRule
                self.rules[name] = rule
            for index, metaRule in enumerate(self.metaRules[-self.addRuleCount:]):
                name = "FWAppS%dR%d" % (ridx, index + len(self.metaRules) - self.addRuleCount)
                rule = '{"switch":"%s", ' % vdpid + \
                            '"name":"%s", ' % name + \
                            metaRule
                self.addRules[name] = rule

    def installRules(self):
        for rule in self.rules.values():
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

    def updateRules(self):
        for rule in self.addRules.values():
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""


