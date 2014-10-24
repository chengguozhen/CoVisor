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

def createPolicy(policy):
    cmd = "%s -n createPolicy 00:00:00:00:00:00:01:00 0 %s" % (ovxctlPy, policy)
    subprocess.call(cmd, shell=True)

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
    createPolicy('"1+2"')
    app1 = DemoMonitorApp(topo)
    app1.installRules()
    app2 = DemoRouterApp(topo)
    app2.installRules()
    CLI(net)

#********************************************************************
# expr: sequential
#********************************************************************
def exprSequential():
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(5)
    createPlumbingGraph()
    addController1(topo)
    addController2(topo)
    createPolicy('"1>2"')
    app1 = DemoLoadBalancerApp(topo)
    app1.installRules()
    app2 = DemoRouterApp(topo)
    app2.installRules()
    CLI(net)

#********************************************************************
# expr: gateway
#********************************************************************
def createPlumbingGraphOneToManyDemo():
    cmd = "%s -n createPlumbingSwitch 00:00:00:00:00:00:01:00 3" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 0 1" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 0 2" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 0 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 1 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 1 3" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 1 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 2 0" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 2 4" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingPort 00:00:00:00:00:00:01:00 2 5" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingLink 00:00:00:00:00:00:01:00 0 3 1 1" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPlumbingLink 00:00:00:00:00:00:01:00 1 3 2 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

def virtAddController1(topo):
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

def virtAddController2(topo):
    print "*****************************"
    print "******** Controller 2 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:20000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createSwitch 2 00:00:00:00:00:00:01:00 1" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 2" % ovxctlPy
    subprocess.call(cmd, shell=True)

def virtAddController3(topo):
    print "*****************************"
    print "******** Controller 3 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:30000 10.0.0.0 16" % (ovxctlPy,
        CONTROLLER_IP)
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createSwitch 3 00:00:00:00:00:00:01:00 2" % ovxctlPy
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 3" % ovxctlPy
    subprocess.call(cmd, shell=True)

def virtCreatePolicy():
    cmd = "%s -n createPolicy 00:00:00:00:00:00:01:00 0 1" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPolicy 00:00:00:00:00:00:01:00 1 2" % ovxctlPy
    subprocess.call(cmd, shell=True)
    cmd = "%s -n createPolicy 00:00:00:00:00:00:01:00 2 3" % ovxctlPy
    subprocess.call(cmd, shell=True)

def exprVirt():
    cleanAll()
    startFloodlight(3)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(5)
    virtCreatePlumbingGraph()
    virtAddController1(topo)
    virtAddController2(topo)
    virtAddController3(topo)
    virtCreatePolicy()
    app = DemoVirtApp(topo)
    app.installRules()
    CLI(net)

#********************************************************************
# main
#********************************************************************
def expr():
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
    CLI(net)

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
        exprSequential()
        #exprGateway()
    else:
        printHelp()



