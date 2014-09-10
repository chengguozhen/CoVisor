#!/usr/bin/python
import sys
import time
import subprocess
import random
from ExprTopo.mtopo import *
from apps import *

WorkDir = "/home/xinjin/xin-flowmaster"
#SWITCH_NUM = 2
#swDPIDs = []
#for i in range(SWITCH_NUM):
#    swDPIDs.append("00:a4:23:05:00:00:00:0%d" % (i+1))
#DefaultRuleNum = 20
#UpdateRuleNum = 5
CONTROLLER_IP = "localhost"
ovxctlPy = "%s/OpenVirteX/utils/ovxctl.py" % WorkDir


topoFile = "%s/OpenVirteX/experiments/ExprTopo/Rocketfuel/" % WorkDir + \
    "internet2/weights.intra"
    #"test/weights.intra"
prefixFile = "%s/OpenVirteX/experiments/classbench/" % WorkDir + \
    "fw1_prefix"
    #"test"
fwFile = "%s/OpenVirteX/experiments/classbench/" % WorkDir + \
    "fw1_10"
    #"test"
swNumber = 1
perSwRoutingRule = 2

#********************************************************************
# mininet: start, kill
#********************************************************************
def startMininet():
    topo = MNTopo(topoFile=topoFile)
    net = Mininet(topo, autoSetMacs=True, xterms=False,
        controller=RemoteController)
    net.addController('c', ip='127.0.0.1')
    print "\nHosts configured with IPs, " + \
        "switches pointing to OpenVirteX at 127.0.0.1 port 6633\n"
    net.start()
    CLI(net)
    net.stop()

def startMininetWithoutCLI():
    topo = MNTopo(sw_number = swNumber, topoFile=topoFile)
    net = Mininet(topo, autoSetMacs=True, xterms=False,
        controller=RemoteController)
    net.addController('c', ip='127.0.0.1')
    print "\nHosts configured with IPs, " + \
        "switches pointing to OpenVirteX at 127.0.0.1 port 6633\n"
    net.start()
    return (topo, net)

def killMininet():
    print "kill mininet"
    subprocess.call("mn -c > /dev/null 2>&1", shell=True)

#********************************************************************
# ovx: start, show, kill, add controller
#********************************************************************
def startOVX():
    with open("ovx.log", "w") as logfile:
        subprocess.call("sh %s/OpenVirteX/scripts/ovx.sh &" % WorkDir,
            shell=True, stdout=logfile, stderr=subprocess.STDOUT)

def showOVX():
    subprocess.call("ps ax | grep ovx.sh | grep -v grep", shell=True)
    subprocess.call("ps ax | grep java | grep -v grep", shell=True)

def killOVX():
    print "kill ovx"
    subprocess.call("ps ax | grep ovx.sh | grep -v grep | awk '{print $1}' " +
        #"| xargs kill > /dev/null 2>&1", shell=True)
        "| xargs kill -9 > /dev/null 2>&1", shell=True)
        #"| xargs pkill -TERM -P > /dev/null 2>&1", shell=True)
    subprocess.call("ps ax | grep OpenVirteX | grep -v grep | awk '{print $1}' " +
        "| xargs kill -9 > /dev/null 2>&1", shell=True)

def addController1(topo):
    print "*****************************"
    print "******** Controller 1 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:10000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)
    for sw in topo.graph.nodes():
        # create switch
        cmd = "%s -n createSwitch 1 %s" % (ovxctlPy,
            topo.graph.node[sw]['dpid'])
        subprocess.call(cmd, shell=True)
        # create port
        for i in range(topo.hostPerSw):
            cmd = "%s -n createPort 1 %s %d" % (ovxctlPy,
                topo.graph.node[sw]['dpid'], i+1)
            subprocess.call(cmd, shell=True)
        # add 1 host
        rawHostMac = topo.graph.node[sw]['dpid'][4:-1] + '1'
        hostMac = ':'.join(a+b for a,b in zip(rawHostMac[::2], rawHostMac[1::2]))
        cmd = "%s -n connectHost 1 %s %d %s" % (ovxctlPy,
            "00a42305" + topo.graph.node[sw]['dpid'][-10:-2], 1,
            hostMac)
        subprocess.call(cmd, shell=True)
    cmd = "%s -n startNetwork 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

def addController2(topo):
    print "*****************************"
    print "******** Controller 2 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:20000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)
    for sw in topo.graph.nodes():
        # create switch
        cmd = "%s -n createSwitch 2 %s" % (ovxctlPy,
            topo.graph.node[sw]['dpid'])
        subprocess.call(cmd, shell=True)
        # create port
        for i in range(topo.hostPerSw):
            cmd = "%s -n createPort 2 %s %d" % (ovxctlPy,
                topo.graph.node[sw]['dpid'], i+1)
            subprocess.call(cmd, shell=True)
        # add the remaining host
        for i in xrange(1, topo.hostPerSw):
            rawHostMac = topo.graph.node[sw]['dpid'][4:-1] + '%d' % (i+1)
            hostMac = ':'.join(a+b for a,b in zip(rawHostMac[::2], rawHostMac[1::2]))
            cmd = "%s -n connectHost 2 %s %d %s" % (ovxctlPy,
                "00a42305" + topo.graph.node[sw]['dpid'][-10:-2], i+1,
                hostMac)
        subprocess.call(cmd, shell=True)
    cmd = "%s -n startNetwork 2" % ovxctlPy
    subprocess.call(cmd, shell=True)

def addVirtController(topo):
    print "*****************************"
    print "******** Controller *********"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:10000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    print cmd
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createMultiSwitch 1 00:00:00:00:00:00:01:00 3" % ovxctlPy
    print cmd
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:02 1" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:02 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:02 0" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:03 2" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:03 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:03 0" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:04 3" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:04 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:04 0" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n connectBabyLink 1 00:a4:23:05:00:00:00:02 2 00:a4:23:05:00:00:00:03 5" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n connectBabyLink 1 00:a4:23:05:00:00:00:02 3 00:a4:23:05:00:00:00:04 9" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n connectBabyLink 1 00:a4:23:05:00:00:00:03 6 00:a4:23:05:00:00:00:04 8" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

def addVirtMultiController(topo):
    print "*****************************"
    print "******** Controller *********"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:10000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)
    
    cmd = "%s -n createNetwork tcp:%s:20000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createNetwork tcp:%s:30000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)


    cmd = "%s -n createMultiSwitch 1 00:00:00:00:00:00:01:00 0" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabySwitch 1 00:a4:23:05:00:00:00:01 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabySwitch 1 00:a4:23:05:00:00:00:01 2" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabySwitch 1 00:a4:23:05:00:00:00:01 3" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:02 1" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:02 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 1 00:a4:23:05:00:00:00:02 0" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabyPort 2 00:a4:23:05:00:00:00:03 2" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 2 00:a4:23:05:00:00:00:03 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 2 00:a4:23:05:00:00:00:03 0" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createBabyPort 3 00:a4:23:05:00:00:00:04 3" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 3 00:a4:23:05:00:00:00:04 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createBabyPort 3 00:a4:23:05:00:00:00:04 0" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n connectBabyLink 1 00:a4:23:05:00:00:00:02 2 00:a4:23:05:00:00:00:03 2" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n connectBabyLink 1 00:a4:23:05:00:00:00:02 3 00:a4:23:05:00:00:00:04 3" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n connectBabyLink 2 00:a4:23:05:00:00:00:03 3 00:a4:23:05:00:00:00:04 2" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 2" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 3" % ovxctlPy
    subprocess.call(cmd, shell=True)



def addRfTopoController1(topo):
    print "*****************************"
    print "******** Controller 1 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:10000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)
    for sw in topo.graph.nodes():
        cmd = "%s -n createSwitch 1 %s" % (ovxctlPy,
            topo.graph.node[sw]['dpid'])
        subprocess.call(cmd, shell=True)
        for i in range(len(topo.graph.edge[sw])+1):
            cmd = "%s -n createPort 1 %s %d" % (ovxctlPy,
                topo.graph.node[sw]['dpid'], i+1)
            subprocess.call(cmd, shell=True)
    for edge in topo.graph.edges():
        srcMac = "00a42305" + topo.graph.node[edge[0]]['dpid'][-10:-2]
        srcPort = topo.graph.edge[edge[0]][edge[1]][edge[0]]
        dstMac = "00a42305" + topo.graph.node[edge[1]]['dpid'][-10:-2]
        dstPort = topo.graph.edge[edge[0]][edge[1]][edge[1]]
        cmd = "%s -n connectLink 1 %s %d %s %d spf 1" % (ovxctlPy, srcMac,
            srcPort, dstMac, dstPort)
        subprocess.call(cmd, shell=True)
    cmd = "%s -n startNetwork 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

def addRfTopoController2(topo):
    print "*****************************"
    print "******** Controller 2 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:20000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)
    for sw in topo.graph.nodes():
        cmd = "%s -n createSwitch 2 %s" % (ovxctlPy,
            topo.graph.node[sw]['dpid'])
        subprocess.call(cmd, shell=True)
        for i in range(len(topo.graph.edge[sw])+1):
            cmd = "%s -n createPort 2 %s %d" % (ovxctlPy,
                topo.graph.node[sw]['dpid'], i+1)
            subprocess.call(cmd, shell=True)
        rawHostMac = topo.graph.node[sw]['dpid'][4:-1] + '1'
        hostMac = ':'.join(a+b for a,b in zip(rawHostMac[::2], rawHostMac[1::2]))
        cmd = "%s -n connectHost 2 %s %d %s" % (ovxctlPy,
            "00a42305" + topo.graph.node[sw]['dpid'][-10:-2], 1,
            hostMac)
        subprocess.call(cmd, shell=True)
    for edge in topo.graph.edges():
        srcMac = "00a42305" + topo.graph.node[edge[0]]['dpid'][-10:-2]
        srcPort = topo.graph.edge[edge[0]][edge[1]][edge[0]]
        dstMac = "00a42305" + topo.graph.node[edge[1]]['dpid'][-10:-2]
        dstPort = topo.graph.edge[edge[0]][edge[1]][edge[1]]
        cmd = "%s -n connectLink 2 %s %d %s %d spf 1" % (ovxctlPy, srcMac,
            srcPort, dstMac, dstPort)
        subprocess.call(cmd, shell=True)
    cmd = "%s -n startNetwork 2" % ovxctlPy
    subprocess.call(cmd, shell=True)

def startComposition():
    subprocess.call("%s -n startComposition" % ovxctlPy, shell=True)

def stopComposition():
    subprocess.call("%s -n stopComposition" % ovxctlPy, shell=True)

def setComposeAlgo(algo):
    subprocess.call("%s -n setComposeAlgo %s" % (ovxctlPy, algo), shell=True)

# policy: ab
# a: 0 -> parallel, 1 -> sequential
# b: 0 -> normal, 1 -> exact, 2 -> trie
def addPolicy(policy):
    subprocess.call([ovxctlPy, "-n", "createPolicy", policy])

#********************************************************************
# floodlight: start, show, kill
#********************************************************************
def startOneFloodlight(index):
    with open("ctrl%d.log" % index, "w") as logfile:
        subprocess.call("java -jar " +
            "%s/floodlight-0.90/target/floodlight.jar -cf " % WorkDir +
            "%s/OpenVirteX/experiments/" % WorkDir +
            "ctrl/ctrl%d.floodlight &" % index,
            shell=True, stdout=logfile, stderr=subprocess.STDOUT)

def startFloodlight(count):
    if count > 3:
        print "warning: bigger than 3 controllers, only start 3 controllers"
        count = 3
    for i in range(count):
        startOneFloodlight(i+1)
    #startOneFloodlight(2)

def showFloodlight():
    subprocess.call("ps ax | grep floodlight | grep -v grep", shell=True)

def killFloodlight():
    print "kill floodlight"
    subprocess.call("ps ax | grep floodlight | grep -v grep | awk '{print $1}' " +
        "| xargs kill -9 > /dev/null 2>&1", shell=True)

#********************************************************************
# rule: monitor, routing
#********************************************************************
#def generateDefaultRule(swDPID):
#    rule = '{"switch":"%s", ' % swDPID
#    rule = rule + '"name":"default0", "priority":"0", ' + \
#        '"active":"true", "actions":""}'
#    return rule
#
#def generateMonitorRule(swDPID, name):
#    rule = '{"switch":"%s", ' % swDPID
#    rule = rule + '"name":"%s", ' % name
#    rule = rule + '"priority":"%d", ' % random.randint(1, 60000)
#    rule = rule + '"ether-type":"2048", '
#    rule = rule + '"src-ip":"%d.%d.%d.%d/%d", ' % (
#        random.randint(0, 256), random.randint(0, 256),
#        random.randint(0, 256), random.randint(0, 256),
#        random.randint(0, 32))
#    rule = rule + '"active":"true", "actions":""}' 
#    return rule
#
#def generateMonitorRuleDelete(swDPID, name):
#    #rule = '{"switch":"%s", ' % swDPID
#    #rule = rule + '"name":"%s"}' % name
#    rule = '{"name":"%s"}' % name
#    return rule
# 
#def generateRouteRule(swDPID, name):
#    rule = '{"switch":"%s", ' % swDPID
#    rule = rule + '"name":"%s", ' % name
#    rule = rule + '"priority":"%d", ' % random.randint(1, 60000)
#    rule = rule + '"ether-type":"2048", '
#    rule = rule + '"dst-ip":"%d.%d.%d.%d/%d", ' % (
#        random.randint(0, 256), random.randint(0, 256),
#        random.randint(0, 256), random.randint(0, 256),
#        random.randint(0, 32))
#    rule = rule + '"active":"true", "actions":"output=1"}' 
#    return rule
#
#def deleteRules(rules, app):
#    for rule in rules:
#        print rule
#        if app == "m":
#            subprocess.call(["curl", "-X", "DELETE", "-d", rule,
#                "http://localhost:10001/wm/staticflowentrypusher/json"])
#        else:
#            subprocess.call(["curl", "-X", "DELETE", "-d", rule,
#                "http://localhost:20001/wm/staticflowentrypusher/json"])
#        print ""
#
#def installRules(rules, app):
#    for rule in rules:
#        print rule
#        if app == "m":
#            subprocess.call(["curl", "-d", rule,
#                "http://localhost:10001/wm/staticflowentrypusher/json"])
#        else:
#            subprocess.call(["curl", "-d", rule,
#                "http://localhost:20001/wm/staticflowentrypusher/json"])
#        print ""
#
#def initMonitor():
#    defaultRules = []
#    for swDPID in swDPIDs:
#        defaultRules.append(generateDefaultRule(swDPID))
#    installRules(defaultRules, "m")
#
#    monitorRules = []
#    for swDPID in swDPIDs:
#        for i in range(DefaultRuleNum):
#            monitorRules.append(generateMonitorRule(swDPID,
#                "%sMonitor%d" % (swDPID[-2:], i)))
#    installRules(monitorRules, "m")
#
#    print "init monitor rules"
#
#def initRoute():
#    defaultRules = []
#    for swDPID in swDPIDs:
#        defaultRules.append(generateDefaultRule(swDPID))
#    installRules(defaultRules, "r")
#
#    routeRules = []
#    for swDPID in swDPIDs:
#        for i in range(DefaultRuleNum):
#            routeRules.append(generateRouteRule(swDPID,
#                "%sRoute%d" % (swDPID[-2:], i)))
#    installRules(routeRules, "r")
#
#    print "init route rules"
#
#def updateMonitor():
#    monitorRules = []
#    for swDPID in swDPIDs:
#        for i in range(UpdateRuleNum):
#            monitorRules.append(generateMonitorRule(swDPID,
#                "%sMonitor%d" % (swDPID[-2:], DefaultRuleNum + i)))
#            #monitorRules.append(generateMonitorRuleDelete(swDPID,
#            #    "%sMonitor%d" % (swDPID[-2:], i)))
#    installRules(monitorRules, "m")
#    #deleteRules(monitorRules, "m")
#
#    print "update monitor rules"

#********************************************************************
# expr utils
#********************************************************************
def cleanAll():
    killMininet()
    killFloodlight()
    killOVX()
    showOVX()
    showFloodlight()

def processLog(fout):
    cmd = "python log_process.py ovx.log %s" % fout
    subprocess.call(cmd, shell=True)

#********************************************************************
# expr: parallel
#********************************************************************
def exprParallelHelper(algo, policy, outLog):
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininetWithoutCLI()
    time.sleep(5)
    addController1(topo)
    addController2(topo)
    addPolicy(policy)
    startComposition()
    app2 = MACLearnerApp(topo, perSwRule = perSwRoutingRule)
    app2.installRules()
    app1 = MonitorApp(topo, app2.macs, perSwRule = perSwRoutingRule, addRuleCount = 10)
    app1.installRules()
    time.sleep(1)
    setComposeAlgo(algo)
    app1.updateRules()
    cleanAll()
    processLog(outLog)

def exprParallelRule():
    global perSwRoutingRule
    global swNumber
    perSwRoutingRule = 100
    swNumber = 1
    for perSwRoutingRule in [1000, 2000, 3000, 4000, 5000]:
        #exprParallelHelper('strawman', '00', 'res_strawman_%d' %  perSwRoutingRule)
        exprParallelHelper('inc', '00', 'res_inc_%d' %  perSwRoutingRule)
        #exprParallelHelper('inc', '01', 'res_inc_acl_%d' %  perSwRoutingRule)

def exprParallelSw():
    global perSwRoutingRule
    global swNumber
    global perSwRoutingRule
    perSwRoutingRule = 100
    swNumber = 1
    for swNumber in [16, 32, 64, 128, 256]:
        #exprParallelHelper('strawman', '00', 'res_strawman_%d' %  swNumber)
        exprParallelHelper('inc', '00', 'res_inc_%d' %  swNumber)

def exprParallel():
    exprParallelRule()
    #exprParallelSw()

#********************************************************************
# expr: sequential
#********************************************************************
def exprSequentialHelper(algo, policy, outLog):
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininetWithoutCLI()
    time.sleep(5)
    addController1(topo)
    addController2(topo)
    addPolicy(policy)
    startComposition()
    app1 = FirewallApp(topo, fwFile, 10)
    app1.genRules()
    app1.installRules()
    app2 = RoutingApp(topo, prefixFile, perSwRule = perSwRoutingRule)
    app2.genRules()
    app2.installRules()
    time.sleep(1)
    setComposeAlgo(algo)
    app1.updateRules()
    cleanAll()
    #processLog('res.' + algo)
    processLog(outLog)

def exprSequentialRule():
    global fwFile
    global perSwRoutingRule

    perSwRoutingRule = 100
    for perSwRoutingRule in [1000]:#[100, 200, 300, 400, 500]:
        fwFile = 'classbench/fw1_%d' % perSwRoutingRule
        #exprSequentialHelper('strawman', '10', 'res_strawman_%d' % perSwRoutingRule)
        exprSequentialHelper('inc', '10', 'res_inc_%d' % perSwRoutingRule)
        exprSequentialHelper('inc', '12', 'res_inc_acl_%d' % perSwRoutingRule)

def exprSequential():
    exprSequentialRule()

#********************************************************************
# expr: SDX
#********************************************************************
def exprVirt():
    cleanAll()
    startOneFloodlight(1)
    startOVX()
    (topo, net) = startMininetWithoutCLI()
    time.sleep(5)
    addVirtController(topo)
    app = SDXApp()
    app.installRules()
    CLI(net)

def exprVirtMultiController():
    cleanAll()
    startFloodlight()
    startOVX()
    (topo, net) = startMininetWithoutCLI()
    time.sleep(5)
    addVirtMultiController(topo)
    CLI(net)
    app = SDXApp()
    app.installRules()
    CLI(net)

def exprAll1():
    global fwFile
    global perSwRoutingRule

    perSwRoutingRule = 100
    fwRule = 100
    for fwRule in [100, 200, 300, 400, 500]:
        fwFile = 'classbench/acl1_%d' % fwRule
        expr('strawman', 'res_strawman_%d' %  fwRule)
        expr('inc', 'res_inc_%d' %  fwRule)
        
def exprAll():
    global fwFile
    global perSwRoutingRule

    perSwRoutingRule = 100
    fwRule = 100
    for perSwRoutingRule in [100, 200, 300, 400, 500]:
        fwFile = 'classbench/acl1_%d' % fwRule
        expr('strawman', 'res_strawman_%d' %  perSwRoutingRule)
        expr('inc', 'res_inc_%d' %  perSwRoutingRule)


#********************************************************************
# main
#********************************************************************
def startAll():
    startFloodlight()
    startOVX()
    (topo, net) = startMininetWithoutCLI()
    time.sleep(5)
    addController1(topo)
    addController2(topo)
    addPolicy()
    startComposition()
    app1 = FirewallApp(topo, fwFile)
    app1.genRules()
    app1.installRules()
    CLI(net)
    app2 = RoutingApp(topo, prefixFile, perSwRule = 5)
    app2.genRules()
    app2.installRules()
    CLI(net)
    setComposeAlgo('incremental')
    app1.updateRules()
    #CLI(net)
    
    cleanAll()

def testApp():
    topo = MNTopo(sw_number = 2, topoFile=topoFile)
    #topo = MNTopo(topoFile=topoFile)
    app1 = MACLearnerApp(topo, perSwRule = 5)
    #app = FirewallApp(topo, fwFile)
    #app = RoutingApp(topo, prefixFile, perSwRule = 5)
    #app.genRules()
    app1.installRules()
    #app.updateRules()

    print "----------"
    app2 = MonitorApp(topo, app1.macs, perSwRule = 5, addRuleCount = 5)
    app2.installRules()
    print "----------"
    app2.updateRules()


def printHelp():
    print "\tUsage: ctrl.py"
    print "\t\tstart-mn kill-mn"
    print "\t\tstart-ovx show-ovx kill-ovx"
    print "\t\tstart-fl show-fl kill-fl"

if __name__ == '__main__':
    if len(sys.argv) < 2:
        printHelp()
        sys.exit()

    if sys.argv[1] == "start-mn":
        startMininet()
    elif sys.argv[1] == "kill-mn":
        killMininet()
    elif sys.argv[1] == "start-ovx":
        startOVX()
    elif sys.argv[1] == "show-ovx":
        showOVX()
    elif sys.argv[1] == "kill-ovx":
        killOVX()
    elif sys.argv[1] == "start-fl":
        startFloodlight()
    elif sys.argv[1] == "show-fl":
        showFloodlight()
    elif sys.argv[1] == "kill-fl":
        killFloodlight()
    elif sys.argv[1] == "start":
        startAll()
    elif sys.argv[1] == "clean":
        cleanAll()
    elif sys.argv[1] == "test-app":
        testApp()
    elif sys.argv[1] == "expr":
        #expr(sys.argv[2])
        #exprAll()
        #exprVirtMultiController()
        exprParallel()
        #exprSequential()
    else:
        printHelp()



