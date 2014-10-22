#!/usr/bin/python
import sys
import time
import subprocess
import random
from ExprTopo.mtopo import *
from apps import *

WorkDir = "/home/xinjin/xin-flowmaster"
CONTROLLER_IP = "localhost"
ovxctlPy = "%s/OpenVirteX/utils/ovxctl.py" % WorkDir


topoFile = "%s/OpenVirteX/experiments/ExprTopo/Rocketfuel/" % WorkDir + \
    "internet2/weights.intra"
prefixFile = "%s/OpenVirteX/experiments/classbench/" % WorkDir + \
    "fw1_prefix"
fwFile = "%s/OpenVirteX/experiments/classbench/" % WorkDir + \
    "fw1_10"
swNumber = 1
perSwRoutingRule = 2

#********************************************************************
# mininet: start, kill
#********************************************************************
def startMininet():
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

def createPlumbingGraph():
    cmd = "%s -n createPlumbingSwitch 00:00:00:00:00:00:01:00 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

def addController1(topo):
    print "*****************************"
    print "******** Controller 1 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:10000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createSwitch 1 00:00:00:00:00:00:01:00 0" % ovxctlPy
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

    cmd = "%s -n createSwitch 2 00:00:00:00:00:00:01:00 0" % ovxctlPy
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

def showFloodlight():
    subprocess.call("ps ax | grep floodlight | grep -v grep", shell=True)

def killFloodlight():
    print "kill floodlight"
    subprocess.call("ps ax | grep floodlight | grep -v grep | awk '{print $1}' " +
        "| xargs kill -9 > /dev/null 2>&1", shell=True)

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
def exprParallel():
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(5)
    createPlumbingGraph()
    addController1(topo)
    addController2(topo)
    app1 = DemoMonitorApp(topo)
    app1.installRules()
    app2 = DemoRouterApp(topo)
    app2.installRules()
    CLI(net)

#    addController2(topo)
#    addPolicy(policy)
#    startComposition()
#    app2 = MACLearnerApp(topo, perSwRule = perSwRoutingRule)
#    app2.installRules()
#    app1 = MonitorApp(topo, app2.macs, perSwRule = 1000, addRuleCount = 10)
#    app1.installRules()
#    app1.updateRules()
#    time.sleep(1)
#    setComposeAlgo(algo)

def exprParallelHelper(algo, policy, outLog):
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(5)
    addController1(topo)
    addController2(topo)
    addPolicy(policy)
    startComposition()
    app2 = MACLearnerApp(topo, perSwRule = perSwRoutingRule)
    app2.installRules()
    #app1 = MonitorApp(topo, app2.macs, perSwRule = perSwRoutingRule, addRuleCount = 10)
    app1 = MonitorApp(topo, app2.macs, perSwRule = 1000, addRuleCount = 10)
    app1.installRules()
    time.sleep(1)
    setComposeAlgo(algo)
    app1.updateRules()
    cleanAll()
    processLog(outLog)

#********************************************************************
# expr: sequential
#********************************************************************
def exprSequentialOne(algo, policy, outLog):
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(5)
    addController1(topo)
    addController2(topo)
    addPolicy(policy)
    startComposition()
    app1 = FirewallApp(topo, fwFile, 1000, 10)
    app1.genRules()
    app1.installRules()
    app1.updateRules()
    app2 = RoutingApp(topo, prefixFile, perSwRule = perSwRoutingRule)
    app2.genRules()
    app2.installRules()
    time.sleep(1)
    setComposeAlgo(algo)

def exprSequentialHelper(algo, policy, outLog):
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
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
    for perSwRoutingRule in [8000]:#[100, 200, 300, 400, 500]:
        #fwFile = 'classbench/fw1_%d' % perSwRoutingRule
        #exprSequentialHelper('strawman', '10', 'res_strawman_%d' % perSwRoutingRule)
        #exprSequentialHelper('inc', '10', 'res_inc_%d' % perSwRoutingRule)
        #exprSequentialHelper('inc', '12', 'res_inc_acl_%d' % perSwRoutingRule)
        fwFile = 'experiments/classbench/fw1_5000'
        exprSequentialOne('inc', '10', 'res_inc_%d' % perSwRoutingRule)

def exprSequential():
    exprSequentialRule()

#********************************************************************
# expr: gateway
#********************************************************************
def exprGatewayHelper(ipCount):
    cleanAll()
    startFloodlight(3)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(5)
    addVirtMultiController(topo)
    app1 = GWIPRouterApp(ipCount)
    app3 = GWMACLearnerApp(800, 100)
    app2 = GWGatewayApp(app3.macs, app3.ips, 100)
    app1.installRules()
    app2.installRules()
    app3.installRules()
    setComposeAlgo('inc')

def exprGateway():
    exprGatewayHelper(8000)

#********************************************************************
# main
#********************************************************************
def expr():
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
    CLI(net)

def startAll():
    startFloodlight()
    startOVX()
    (topo, net) = startMininet()
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
    topo = MNTopo(sw_number = 1, topoFile=topoFile)
    app1 = DemoMonitorApp(topo)
    app1.installRules()
    app2 = DemoRouterApp(topo)
    app2.installRules()
    app3 = DemoLoadBalancerApp(topo)
    app3.installRules()
    
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
        #exprParallel()
        #exprSequential()
        #exprGateway()
        exprParallel()
    else:
        printHelp()



