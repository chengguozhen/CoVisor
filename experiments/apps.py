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
        dpid = self.graph.node[switch]['vdpid']
        self.rules = []

        rule = '{"switch":"%s", ' % dpid + \
                '"name":"DemoMinitor0", ' + \
                '"priority":"0", ' + \
                '"active":"true", "actions":""}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoMonitor1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"src-ip":"1.0.0.0/24", ' + \
            '"active":"true", "actions":""}'
        self.rules.append(rule)

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

        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoRouter1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"2.0.0.1", ' + \
            '"active":"true", "actions":"output=1"}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoRouter2", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"2.0.0.2", ' + \
            '"active":"true", "actions":"output=2"}'
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
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"src-ip":"0.0.0.0/2", ' + \
            '"dst-ip":"3.0.0.0", ' + \
            '"active":"true", "actions":"set-dst-ip=2.0.0.1"}'
        self.rules.append(rule)

        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoLB2", ' + \
            '"priority":"3", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"3.0.0.0", ' + \
            '"active":"true", "actions":"set-dst-ip=2.0.0.2"}'
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

        dpid = "00:a4:23:05:00:00:00:01"
        self.rules1 = []
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoVirt1R1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/8", ' + \
            '"active":"true", "actions":"output=3"}'
        self.rules1.append(rule)
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoVirt1R2", ' + \
            '"priority":"4", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"2.0.0.0/16", ' + \
            '"active":"true", "actions":"output=2"}'
        self.rules1.append(rule)

        dpid = "00:a4:23:05:00:00:00:02"
        self.rules2 = []
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoVirt2R1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/4", ' + \
            '"active":"true", "actions":"output=3,dstip=2.0.0.0"}'
        self.rules2.append(rule)
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoVirt2R2", ' + \
            '"priority":"6", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/24", ' + \
            '"active":"true", "actions":"output=2"}'
        self.rules2.append(rule)

        dpid = "00:a4:23:05:00:00:00:03"
        self.rules3 = []
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoVirt3R1", ' + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/8", ' + \
            '"active":"true", "actions":"output=3"}'
        self.rules3.append(rule)
        rule = '{"switch":"%s", ' % dpid + \
            '"name":"DemoVirt3R2", ' + \
            '"priority":"4", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"2.0.0.0/16", ' + \
            '"active":"true", "actions":"output=2"}'
        self.rules1.append(rule)

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

#********************************************************************
# Monitor App
#********************************************************************
class MonitorApp():

    def __init__(self, topo, macs, perSwRule = 100, addRuleCount = 5):
        self.graph = topo.graph
        self.perSwRule = perSwRule
        self.addRuleCount = addRuleCount
        self.rules = {}
        self.addRules = {}
        self.macpairs = []

        random.seed(1)
        macPairIndex = [i for i in range(len(macs) * len(macs))]
        random.shuffle(macPairIndex)
        for i in range(perSwRule + addRuleCount):
            index = macPairIndex[i]
            srcMac = macs[index/len(macs)]
            dstMac = macs[index%len(macs)]
            self.macpairs.append((srcMac, dstMac))

        for switch in self.graph.nodes():
            ridx = self.graph.node[switch]['ridx']
            vdpid = self.graph.node[switch]['vdpid']
            
            # default rule
#            name = "MonitorAppS%dD0" % ridx
#            rule = '{"switch":"%s", ' % vdpid + \
#                '"name":"%s", ' % name + \
#                '"priority":"0", ' + \
#                '"active":"true", "actions":""}'
#            self.rules[name] = rule
            # mac routing rules
            for index in range(perSwRule):
                macpair = self.macpairs[index]
                name = "MonitorAppS%dR%d" % (ridx, index)
                rule = '{"switch":"%s", ' % vdpid + \
                    '"name":"%s", ' % name + \
                    '"priority":"1", ' + \
                    '"src-mac":"%s", ' % macpair[0] + \
                    '"dst-mac":"%s", ' % macpair[1] + \
                    '"active":"true", "actions":""}'
                self.rules[name] = rule
            for i in range(addRuleCount):
                index = perSwRule + i
                macpair = self.macpairs[index]
                name = "MonitorAppS%dR%d" % (ridx, index)
                rule = '{"switch":"%s", ' % vdpid + \
                    '"name":"%s", ' % name + \
                    '"priority":"1", ' + \
                    '"src-mac":"%s", ' % macpair[0] + \
                    '"dst-mac":"%s", ' % macpair[1] + \
                    '"active":"true", "actions":""}'
                self.addRules[name] = rule

    def installRules(self):
        for rule in self.rules.values():
            #print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

    def updateRules(self):
        for rule in self.addRules.values():
            #print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""


#********************************************************************
# MAC Learner App
#********************************************************************
class MACLearnerApp():

    def __init__(self, topo, perSwRule = 100):
        self.graph = topo.graph
        self.perSwRule = perSwRule
        self.rules = {}
        self.macs = []

        random.seed(1)
        for i in range(perSwRule):
            self.macs.append(self.genRandomMAC())

        for switch in self.graph.nodes():
            ridx = self.graph.node[switch]['ridx']
            vdpid = self.graph.node[switch]['vdpid']
            
            # default rule
#            name = "MACAppS%dD0" % ridx
#            rule = '{"switch":"%s", ' % vdpid + \
#                '"name":"%s", ' % name + \
#                '"priority":"0", ' + \
#                '"active":"true", "actions":""}'
#            self.rules[name] = rule
            # mac routing rules
            for index, mac in enumerate(self.macs):
                name = "MACAppS%dR%d" % (ridx, index)
                rule = '{"switch":"%s", ' % vdpid + \
                    '"name":"%s", ' % name + \
                    '"priority":"1", ' + \
                    '"dst-mac":"%s", ' % mac + \
                    '"active":"true", "actions":"output=1"}'
                self.rules[name] = rule

    def genRandomMAC(self):
        return '{:02x}'.format(random.randint(0,255)) + ":" + \
            '{:02x}'.format(random.randint(0,255)) + ":" + \
            '{:02x}'.format(random.randint(0,255)) + ":" + \
            '{:02x}'.format(random.randint(0,255)) + ":" + \
            '{:02x}'.format(random.randint(0,255)) + ":" + \
            '{:02x}'.format(random.randint(0,255)) + ":"

    def installRules(self):
        for rule in self.rules.values():
            #print rule
            cmd = "curl -d '%s' http://localhost:20001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""


#********************************************************************
# SDX App
#********************************************************************
class SDXApp():

    def __init__(self):
        self.rules = {}

        dpid = "00:a4:23:05:00:00:00:02"
        self.rules[dpid] = {}
        #self.genDefaultRuleHelper("s1d0", dpid)
        self.genRoutingRuleHelper("s1r1", dpid, "1", "1.0.0.0", 1)
        self.genRoutingRuleHelper("s1r2", dpid, "1", "2.0.0.0", 2)
        self.genRoutingRuleHelper("s1r3", dpid, "1", "3.0.0.0", 3)

        dpid = "00:a4:23:05:00:00:00:03"
        self.rules[dpid] = {}
        #self.genDefaultRuleHelper("s2d0", dpid)
        self.genRoutingRuleHelper("s2r1", dpid, "1", "1.0.0.0", 2)
        self.genRoutingRuleHelper("s2r2", dpid, "1", "2.0.0.0", 1)
        self.genRoutingRuleHelper("s2r3", dpid, "1", "3.0.0.0", 3)

        dpid = "00:a4:23:05:00:00:00:04"
        self.rules[dpid] = {}
        #self.genDefaultRuleHelper("s3d0", dpid)
        self.genRoutingRuleHelper("s3r1", dpid, "1", "1.0.0.0", 3)
        self.genRoutingRuleHelper("s3r2", dpid, "1", "2.0.0.0", 2)
        self.genRoutingRuleHelper("s3r3", dpid, "1", "3.0.0.0", 1)

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
        self.installRulesHelper(dpid, "20001")
        dpid = "00:a4:23:05:00:00:00:04"
        self.installRulesHelper(dpid, "30001")

    def installRulesHelper(self, dpid, port):
        for rule in self.rules[dpid].values():
            #print rule
            cmd = "curl -d '%s' http://localhost:%s/wm/staticflowentrypusher/json" % (rule, port)
            subprocess.call(cmd, shell=True)
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

        self.genRules()

    def genRules(self):
        for switch in self.graph.nodes():
            ridx = self.graph.node[switch]['ridx']
            vdpid = self.graph.node[switch]['vdpid']
            
            # default rule
#            name = "RouteAppS%dD0" % ridx
#            rule = '{"switch":"%s", ' % vdpid + \
#                '"name":"%s", ' % name + \
#                '"priority":"0", ' + \
#                '"active":"true", "actions":""}'
#            self.rules[name] = rule
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
            #print rule
            cmd = "curl -d '%s' http://localhost:20001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

#********************************************************************
# Firewall App
#********************************************************************
class FirewallApp():

    def __init__(self, topo, classbenchFile, perSwRule = 100, addRuleCount = 5):
        self.graph = topo.graph
        self.rules = {}
        self.ruleCount = 0
        self.perSwRule = perSwRule

        self.addRules = {}
        self.addRuleCount = addRuleCount

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
            
            if random.randint(0, 9) == 0:
                rule = rule + '"active":"true", "actions":"output=1"}' 
            else:
                rule = rule + '"active":"true", "actions":""}' 

            self.metaRules.append(rule)
            self.ruleCount += 1
            oneline = f.readline()
        f.close()
        random.shuffle(self.metaRules)

        self.genRules()

    def genRules(self):
        for switch in self.graph.nodes():
            ridx = self.graph.node[switch]['ridx'] 
            vdpid = self.graph.node[switch]['vdpid']

            # default rule
#            name = "RouteAppS%dD0" % ridx
#            rule = '{"switch":"%s", ' % vdpid + \
#                '"name":"%s", ' % name + \
#                '"priority":"0", ' + \
#                '"active":"true", "actions":""}'
#            self.rules[name] = rule

            # firewall rule
            for index, metaRule in enumerate(self.metaRules[0:self.perSwRule]):
                name = "FWAppS%dR%d" % (ridx, index)
                rule = '{"switch":"%s", ' % vdpid + \
                            '"name":"%s", ' % name + \
                            metaRule
                self.rules[name] = rule
            for index, metaRule in enumerate(self.metaRules[self.perSwRule:self.perSwRule+self.addRuleCount:]):
                name = "FWAppS%dR%d" % (ridx, index + self.perSwRule)
                rule = '{"switch":"%s", ' % vdpid + \
                            '"name":"%s", ' % name + \
                            metaRule
                self.addRules[name] = rule

    def installRules(self):
        for rule in self.rules.values():
            #print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

    def updateRules(self):
        for rule in self.addRules.values():
            #print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

#********************************************************************
# Gateway App
#********************************************************************
class GWIPRouterApp():
    def __init__(self, perSwRule = 1000):
        self.perSwRule = perSwRule
        self.rules = []

        ridx = 2
        vdpid = "00:a4:23:05:00:00:00:02"
 
        i = 0
        while i < 254 and i < perSwRule:
            name = "RouterAppS%dR%d" % (ridx, i)
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"16", ' + \
                '"ether-type":"2048", ' + \
                '"dst-ip":"%d.0.0.0/16", ' % (i+2) + \
                '"active":"true", "actions":"output=5"}'
            self.rules.append(rule)
            i += 1
 
        while i < 254*256 and i < perSwRule:
            name = "RouterAppS%dR%d" % (ridx, i)
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"16", ' + \
                '"ether-type":"2048", ' + \
                '"dst-ip":"%d.%d.0.0/16", ' % (i%254+2, i/254) + \
                '"active":"true", "actions":"output=5"}'
            self.rules.append(rule)
            i += 1

        name = "RouterAppS%dE" % ridx
        rule = '{"switch":"%s", ' % vdpid + \
            '"name":"%s", ' % name + \
            '"priority":"16", ' + \
            '"ether-type":"2048", ' + \
            '"dst-ip":"1.0.0.0/16", ' + \
            '"active":"true", "actions":"output=7"}'
        self.rules.append(rule)
        i += 1

    def installRules(self):
        for rule in self.rules:
            #print rule
            cmd = "curl -d '%s' http://localhost:10001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

class GWGatewayApp():

    def __init__(self, macs, ips, external):
        self.rules = []

        ridx = 3
        vdpid = "00:a4:23:05:00:00:00:03"
        for i in range(external):
            name = "GatewayAppS%dR%d" % (ridx, i)
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"1", ' + \
                '"ether-type":"2048", ' + \
                '"ingress-port":"8", ' + \
                '"dst-ip":"%s", ' % ips[i] + \
                '"active":"true", ' + \
                '"actions":"set-src-mac=11:11:11:11:11:11,set-dst-mac=%s,output=9"}' % macs[i]
            self.rules.append(rule)

        name = "GatewayAppSK"
        rule = '{"switch":"%s", ' % vdpid + \
            '"name":"%s", ' % name + \
            '"priority":"1", ' + \
            '"ether-type":"2048", ' + \
            '"ingress-port":"9", ' + \
            '"active":"true", "actions":"output=8"}'
        self.rules.append(rule)

    def installRules(self):
        for rule in self.rules:
            #print rule
            cmd = "curl -d '%s' http://localhost:20001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""

class GWMACLearnerApp():

    def __init__(self, macSize = 100, externalSize = 100):
        self.macSize = macSize
        self.externalSize = externalSize
        self.rules = []
        self.macs = []
        self.ips = []

        for i in range(macSize):
            if i < 256:
                self.macs.append('00:00:00:00:00:{:02x}'.format(i))
                self.ips.append("1.0.0." + str(i))
            else:
                self.macs.append('00:00:00:00:{:02x}:{:02x}'.format(i/256, i%256))
                self.ips.append("1.0." + str(i/256) + "." + str(i%256))

        ridx = 4
        vdpid = "00:a4:23:05:00:00:00:04"
        
        for i in range(externalSize):
            name = "MACAppS%dE1%d" % (ridx, i*2)
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"1", ' + \
                '"ingress-port":"10", ' + \
                '"src-mac":"11:11:11:11:11:11", ' + \
                '"dst-mac":"%s", ' % self.macs[i] + \
                '"active":"true", "actions":"output=12"}'
            self.rules.append(rule)

            name = "MACAppS%dE2%d" % (ridx, i*2+1)
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"1", ' + \
                '"ingress-port":"12", ' + \
                '"src-mac":"%s", ' % self.macs[i] + \
                '"dst-mac":"11:11:11:11:11:11", ' + \
                '"active":"true", "actions":"output=10"}'
            self.rules.append(rule)

        for i in range(macSize):
            name = "MACAppS%dI%d" % (ridx, i)
            rule = '{"switch":"%s", ' % vdpid + \
                '"name":"%s", ' % name + \
                '"priority":"1", ' + \
                '"ingress-port":"11", ' + \
                '"dst-mac":"00:00:00:00:11:11", ' + \
                '"src-mac":"%s", ' % self.macs[i] + \
                '"active":"true", "actions":"output=12"}'
            self.rules.append(rule)

    def installRules(self):
        for rule in self.rules:
            #print rule
            cmd = "curl -d '%s' http://localhost:30001/wm/staticflowentrypusher/json" % rule
            subprocess.call(cmd, shell=True)
            print ""


