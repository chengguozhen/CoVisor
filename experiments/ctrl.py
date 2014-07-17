#!/usr/bin/python
import sys
import subprocess
import random
from ExprTopo.multiple import *

WorkDir = "/home/xinjin/xin-flowmaster"
MininetTopoScript = "%s/OpenVirteX/experiments/topo-mininet/" % WorkDir + \
    "multiple.py"
SWITCH_NUM = 2
swDPIDs = []
for i in range(SWITCH_NUM):
    swDPIDs.append("00:a4:23:05:00:00:00:0%d" % (i+1))
DefaultRuleNum = 20
UpdateRuleNum = 5

#********************************************************************
# mininet: start, kill
#********************************************************************
def startMininet():
    topo = MultipleTopo()
    net = Mininet(topo, autoSetMacs=True, xterms=False,
        controller=RemoteController)
    net.addController('c', ip='128.112.93.28')
    print "\nHosts configured with IPs, " + \
        "switches pointing to OpenVirteX at 128.112.93.28 port 6633\n"
    net.start()
    CLI(net)
    net.stop()

def killMininet():
    subprocess.call("mn -c", shell=True)

#********************************************************************
# ovx: start, show, kill
#********************************************************************
def startOVX():
    with open("ovx.log", "w") as logfile:
        subprocess.call("sh %s/OpenVirteX/scripts/ovx.sh &" % WorkDir,
            shell=True, stdout=logfile, stderr=subprocess.STDOUT)

def showOVX():
    subprocess.call("ps ax | grep ovx.sh | grep -v grep", shell=True)
    subprocess.call("ps ax | grep java | grep -v grep", shell=True)

def killOVX():
    subprocess.call("ps ax | grep ovx.sh | grep -v grep | awk '{print $1}' " +
        "| xargs pkill -TERM -P", shell=True)

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
    subprocess.call("ps ax | grep floodlight | grep -v grep | awk '{print $1}' " +
        "| xargs kill", shell=True)

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

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print "\tUsage: ctrl_rule.py init_m/init_r/update_m/update_r"
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
    else:
        print "not supported"



