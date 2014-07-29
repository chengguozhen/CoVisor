#!/usr/bin/python
import sys
import time
import subprocess
import random
from ExprTopo.rftopo import *
from apps import *


WorkDir = "/home/xinjin/xin-flowmaster"
SWITCH_NUM = 2
swDPIDs = []
for i in range(SWITCH_NUM):
    swDPIDs.append("00:a4:23:05:00:00:00:0%d" % (i+1))
DefaultRuleNum = 20
UpdateRuleNum = 5
CONTROLLER_IP = "localhost"
ovxctlPy = "%s/OpenVirteX/utils/ovxctl.py" % WorkDir

#********************************************************************
# mininet: start, kill
#********************************************************************
def startMininet():
    topo = MNTopo()
    net = Mininet(topo, autoSetMacs=True, xterms=False,
        controller=RemoteController)
    net.addController('c', ip='127.0.0.1')
    print "\nHosts configured with IPs, " + \
        "switches pointing to OpenVirteX at 127.0.0.1 port 6633\n"
    net.start()
    CLI(net)
    net.stop()

def startMininetWithoutCLI():
    topo = MNTopo()
    net = Mininet(topo, autoSetMacs=True, xterms=False,
        controller=RemoteController)
    net.addController('c', ip='127.0.0.1')
    print "\nHosts configured with IPs, " + \
        "switches pointing to OpenVirteX at 127.0.0.1 port 6633\n"
    net.start()
    return topo

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
        #"| xargs kill -9 > /dev/null 2>&1", shell=True)
        "| xargs pkill -TERM -P > /dev/null 2>&1", shell=True)


def addController1(topo):
    print "*****************************"
    print "******** Controller 1 *******"
    print "*****************************"
    subprocess.call([ovxctlPy, "-n", "createNetwork",
        "tcp:%s:10000" % CONTROLLER_IP, "10.0.0.0", "16"])
    for sw in topo.graph.nodes():
        subprocess.call([ovxctlPy, "-n", "createSwitch",
            "1", topo.graph.node[sw]['dpid'])
        for i in range(len(topo.graph.edge[sw])):
            subprocess.call([ovxctlPy, "-n", "createPort",
                "1", topo.graph.node[sw]['dpid'], "%d" % (i+2])
        subprocess.call([ovxctlPy, "-n", "connectHost",
            "1", topo.graph.node[sw]['dpid'], "1",
            self.graph.node[switch]['dpid'][4:-1] + '1'])
    subprocess.call([ovxctlPy, "-n", "startNetwork", "1"])


def addMonitorController():
    print "*****************************"
    print "***** Monitor Controller ****"
    print "*****************************"
    subprocess.call([ovxctlPy, "-n", "createNetwork",
        "tcp:%s:10000" % CONTROLLER_IP, "10.0.0.0", "16"])
    for i in xrange(1, SWITCH_NUM + 1):
        subprocess.call([ovxctlPy, "-n", "createSwitch",
            "1", "00:00:00:00:00:00:0%d:00" % i])
        subprocess.call([ovxctlPy, "-n", "createPort",
            "1", "00:00:00:00:00:00:0%d:00" % i, "1"])
        subprocess.call([ovxctlPy, "-n", "connectHost",
            "1", "00:a4:23:05:00:00:00:0%d" % i, "1",
            "00:00:00:00:0%d:01" % i])
    subprocess.call([ovxctlPy, "-n", "startNetwork", "1"])

def addRouteController():
    print "*****************************"
    print "***** Route Controller ******"
    print "*****************************"
    subprocess.call([ovxctlPy, "-n", "createNetwork",
        "tcp:%s:20000" % CONTROLLER_IP, "10.0.0.0", "16"])
    for i in xrange(1, SWITCH_NUM + 1):
        subprocess.call([ovxctlPy, "-n", "createSwitch",
            "2", "00:00:00:00:00:00:0%d:00" % i])
        subprocess.call([ovxctlPy, "-n", "createPort",
            "2", "00:00:00:00:00:00:0%d:00" % i, "2"])
        subprocess.call([ovxctlPy, "-n", "connectHost",
            "2", "00:a4:23:05:00:00:00:0%d" % i, "1",
            "00:00:00:00:0%d:02" % i])
    subprocess.call([ovxctlPy, "-n", "startNetwork", "2"])

def addPolicy():
    subprocess.call([ovxctlPy, "-n", "createPolicy", "1+2"])

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

def startFloodlight():
    startOneFloodlight(1)
    startOneFloodlight(2)

def showFloodlight():
    subprocess.call("ps ax | grep floodlight | grep -v grep", shell=True)

def killFloodlight():
    print "kill floodlight"
    subprocess.call("ps ax | grep floodlight | grep -v grep | awk '{print $1}' " +
        "| xargs kill > /dev/null 2>&1", shell=True)

#********************************************************************
# rule: monitor, routing
#********************************************************************
def generateDefaultRule(swDPID):
    rule = '{"switch":"%s", ' % swDPID
    rule = rule + '"name":"default0", "priority":"0", ' + \
        '"active":"true", "actions":""}'
    return rule

def generateMonitorRule(swDPID, name):
    rule = '{"switch":"%s", ' % swDPID
    rule = rule + '"name":"%s", ' % name
    rule = rule + '"priority":"%d", ' % random.randint(1, 60000)
    rule = rule + '"ether-type":"2048", '
    rule = rule + '"src-ip":"%d.%d.%d.%d/%d", ' % (
        random.randint(0, 256), random.randint(0, 256),
        random.randint(0, 256), random.randint(0, 256),
        random.randint(0, 32))
    rule = rule + '"active":"true", "actions":""}' 
    return rule

def generateMonitorRuleDelete(swDPID, name):
    #rule = '{"switch":"%s", ' % swDPID
    #rule = rule + '"name":"%s"}' % name
    rule = '{"name":"%s"}' % name
    return rule
 
def generateRouteRule(swDPID, name):
    rule = '{"switch":"%s", ' % swDPID
    rule = rule + '"name":"%s", ' % name
    rule = rule + '"priority":"%d", ' % random.randint(1, 60000)
    rule = rule + '"ether-type":"2048", '
    rule = rule + '"dst-ip":"%d.%d.%d.%d/%d", ' % (
        random.randint(0, 256), random.randint(0, 256),
        random.randint(0, 256), random.randint(0, 256),
        random.randint(0, 32))
    rule = rule + '"active":"true", "actions":"output=1"}' 
    return rule

def deleteRules(rules, app):
    for rule in rules:
        print rule
        if app == "m":
            subprocess.call(["curl", "-X", "DELETE", "-d", rule,
                "http://localhost:10001/wm/staticflowentrypusher/json"])
        else:
            subprocess.call(["curl", "-X", "DELETE", "-d", rule,
                "http://localhost:20001/wm/staticflowentrypusher/json"])
        print ""

def installRules(rules, app):
    for rule in rules:
        print rule
        if app == "m":
            subprocess.call(["curl", "-d", rule,
                "http://localhost:10001/wm/staticflowentrypusher/json"])
        else:
            subprocess.call(["curl", "-d", rule,
                "http://localhost:20001/wm/staticflowentrypusher/json"])
        print ""

def initMonitor():
    defaultRules = []
    for swDPID in swDPIDs:
        defaultRules.append(generateDefaultRule(swDPID))
    installRules(defaultRules, "m")

    monitorRules = []
    for swDPID in swDPIDs:
        for i in range(DefaultRuleNum):
            monitorRules.append(generateMonitorRule(swDPID,
                "%sMonitor%d" % (swDPID[-2:], i)))
    installRules(monitorRules, "m")

    print "init monitor rules"

def initRoute():
    defaultRules = []
    for swDPID in swDPIDs:
        defaultRules.append(generateDefaultRule(swDPID))
    installRules(defaultRules, "r")

    routeRules = []
    for swDPID in swDPIDs:
        for i in range(DefaultRuleNum):
            routeRules.append(generateRouteRule(swDPID,
                "%sRoute%d" % (swDPID[-2:], i)))
    installRules(routeRules, "r")

    print "init route rules"

def updateMonitor():
    monitorRules = []
    for swDPID in swDPIDs:
        for i in range(UpdateRuleNum):
            monitorRules.append(generateMonitorRule(swDPID,
                "%sMonitor%d" % (swDPID[-2:], DefaultRuleNum + i)))
            #monitorRules.append(generateMonitorRuleDelete(swDPID,
            #    "%sMonitor%d" % (swDPID[-2:], i)))
    installRules(monitorRules, "m")
    #deleteRules(monitorRules, "m")

    print "update monitor rules"

#********************************************************************
# main
#********************************************************************
def cleanAll():
    killMininet()
    killOVX()
    killFloodlight()

def startAll():
    startFloodlight()
    startOVX()
    topo = startMininetWithoutCLI()
    time.sleep(5)
    addController1(topo)
    raw_input("continue")
    addController2(topo)
    addPolicy()

    app = FirewallApp(topo, 'classbench/test')
    app = RoutingApp(topo, 'classbench/acl1k')
    
    raw_input("continue")

    cleanAll()


def printHelp():
    print "\tUsage: ctrl_rule.py"
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
    else:
        printHelp()



