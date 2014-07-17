#!/usr/bin/python

import sys
import subprocess
import random


WorkDir = "/home/xinjin/xin-flowmaster"
MininetTopoScript = "this"
SWITCH_NUM = 2
swDPIDs = []
for i in range(SWITCH_NUM):
    swDPIDs.append("00:a4:23:05:00:00:00:0%d" % (i+1))
DefaultRuleNum = 20
UpdateRuleNum = 5

def startMininet(mnScript): 
    p1 = subprocess.Popen(["echo", "xinjin"], stdout=subprocess.PIPE)
    p2 = subprocess.Popen(["sudo", "python", mnScript], stdin=p1.stdout,
        stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

def showMininet(mnScript):
    subprocess.call("ps ax | grep %s | grep -v grep" % mnScript, shell=True)

def killMininet(mnScript):
    subprocess.call("echo xinjin | ps ax | grep %s " % mnScript +
        "| grep -v grep | awk '{print $1}' | xargs sudo -S kill -9",
        shell=True)

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


def main(argv):

    try:
        opts, args = getopt.getopt(argv, "h", ["start-mn", "show-mn", "kill-mn"])
    except getopt.GetoptError:
        print "ctrl.py start-mn/show-mn/kill-mn/" +\
            "start-ovx/show-ovx/kill-ovx/start-fl/show-fl/kill-fl"
        sys.exit(2)

    if len(sys.argv) < 2:
        print "\tUsage: ctrl_rule.py init_m/init_r/update_m/update_r"
        sys.exit()

    if sys.argv[1] == "init_m":
        initMonitor()
    elif sys.argv[1] == "init_r":
        initRoute()
    elif sys.argv[1] == "update_m":
        updateMonitor()
    elif sys.argv[1] == "showMn":
        showMininet("multiple")
#    elif sys.argv[1] == "update_r":
#        updateRoute()
    else:
        print "not supported"

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print "\tUsage: ctrl_rule.py init_m/init_r/update_m/update_r"
        sys.exit()

    if sys.argv[1] == "start-mn":
        startMininet(MininetTopoScript)
    elif sys.argv[1] == "show-mn":
        showMininet(MininetTopoScript)
    elif sys.argv[1] == "kill-mn":
        killMininet(MininetTopoScript)
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



