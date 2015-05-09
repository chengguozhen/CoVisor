#!/usr/bin/python
import sys
import time
import os
import subprocess
from subprocess import Popen
import random
from ExprTopo.querytopo import *
from ExprTopo.parallel_querytopo import *
from apps import *
from ryu_apps import *
import os.path
from ctrl import *

WORKDIR = "/home/covisor"
CONTROLLER_IP = "localhost"
OVXCTLPY = "%s/OpenVirteX/utils/ovxctl.py" % WORKDIR
SWNUMBER = 1
SLEEP_TIME = 20
STATS_TIME = 32
TIME_LOG = "query_time.txt"
# Number of queries to send.
NUMBER = 100
# dpid of switch to send queries to
dpid = "00a4230500000001"


#********************************************************************
# 30 March
# This script is for evaluating stats queries.  See queries-v1.py for
# attempts to connect Ryu to CoVisor and send iperf traffic.
#********************************************************************

#********************************************************************
# mininet: start, kill
#********************************************************************
def startMininet():
    topo = QueryTopo(sw_number=SWNUMBER)
    net = Mininet(topo, autoSetMacs=True, xterms=False,
        controller=RemoteController)
    net.addController('c', ip="127.0.0.1")
    print "\nHosts configured with IPs, " + \
        "switches pointing to OpenVirteX at 127.0.0.1 port 6633\n"
    net.start()
    return (topo, net)

def startMininet(topo_string=""):
    topo = QueryTopo(sw_number=SWNUMBER)
    if topo_string == "par":
        topo = ParallelQueryTopo(sw_number=SWNUMBER)
    net = Mininet(topo, autoSetMacs=True, xterms=False,
        controller=RemoteController)
    net.addController('c', ip="127.0.0.1")
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
    with open("covisor.log", "w") as logfile:
        subprocess.call("sh %s/OpenVirteX/scripts/ovx.sh &" % WORKDIR,
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
    subprocess.call("ps ax | grep CoVisor | grep -v grep | awk '{print $1}' " +
        "| xargs kill -9 > /dev/null 2>&1", shell=True)

def killJava():
    print "kill java"
    subprocess.call("killall java", shell=True)

def createPlumbingGraph():
    cmd = "%s -n createPlumbingSwitch 00:00:00:00:00:00:01:00 1" % OVXCTLPY
    print "trying to create plumbing graph"
    subprocess.call(cmd, shell=True)

def addController1(topo):
    print "*****************************"
    print "******** Controller 1 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:10000 10.0.0.0 16" % (OVXCTLPY,
        CONTROLLER_IP)
    print cmd
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createSwitch 1 00:00:00:00:00:00:01:00 0" % OVXCTLPY
    print cmd
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 1" % OVXCTLPY
    print cmd
    subprocess.call(cmd, shell=True)

def addController2(topo):
    print "*****************************"
    print "******** Controller 2 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:20000 10.0.0.0 16" % (OVXCTLPY,
        CONTROLLER_IP)
    print cmd
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createSwitch 2 00:00:00:00:00:00:01:00 0" % OVXCTLPY
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 2" % OVXCTLPY
    subprocess.call(cmd, shell=True)

def addController3(topo):
    print "*****************************"
    print "******** Controller 3 *******"
    print "*****************************"
    cmd = "%s -n createNetwork tcp:%s:30000 10.0.0.0 16" % (OVXCTLPY,
        CONTROLLER_IP)
    print cmd
    subprocess.call(cmd, shell=True)

    cmd = "%s -n createSwitch 3 00:00:00:00:00:00:01:00 0" % OVXCTLPY
    subprocess.call(cmd, shell=True)

    cmd = "%s -n startNetwork 3" % OVXCTLPY
    subprocess.call(cmd, shell=True)

def createPolicy(policy):
    cmd = "%s -n createPolicy 00:00:00:00:00:00:01:00 0 %s" % (OVXCTLPY, policy)
    subprocess.call(cmd, shell=True)

def createACL(acl):
    cmd = "%s -n createACL %s" % (OVXCTLPY, acl)
    subprocess.call(cmd, shell=True)

#********************************************************************
# floodlight: start, show, kill
#********************************************************************
def startOneFloodlight(index):
    with open("ctrl%d.log" % index, "w") as logfile:
        floodlight_name = "%s/floodlight-0.90/target/floodlight.jar" \
                          % WORKDIR
        if os.path.exists(floodlight_name):
            print floodlight_name + " exists."
            cmd = "java -jar " + floodlight_name + " -cf %s/OpenVirteX" \
                  % WORKDIR + "/experiments/ctrl/ctrl%d.floodlight" \
                  % index + " &"
            print cmd
            subprocess.call(cmd,            
                            shell=True, stdout=logfile, stderr=
                            subprocess.STDOUT)
        else:
            print floodlight_name + " does not exist."

def startFloodlight(count):
    if count > 3:
        print "warning: bigger than 3 controllers, only start 3 controllers"
        count = 3
    for i in range(count):
        print "starting Floodlight " + str(i + 1)
        startOneFloodlight(i+1)

def showFloodlight():
    subprocess.call("ps ax | grep floodlight | grep -v grep", shell=True)

def killFloodlight():
    print "kill floodlight"
    subprocess.call("ps ax | grep floodlight | grep -v grep | awk '{print $1}' " +
        "| xargs kill -9 > /dev/null 2>&1", shell=True)


#********************************************************************
# utils
#********************************************************************
def cleanAll(time_log=TIME_LOG):
    killMininet()
    killFloodlight()
    killOVX()
    showOVX()
    showFloodlight()
    if os.path.exists(time_log):
        os.remove(time_log)

#********************************************************************
# expr: parallel
#********************************************************************
def exprParallel(number=NUMBER):
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet("par")
    time.sleep(SLEEP_TIME)
    createPlumbingGraph()
    addController1(topo)
    addController2(topo)
    createACL('1 dltype:exact,srcip:prefix,dstip:prefix output')
    createACL('2 dltype:exact,dstip:prefix output')
    createPolicy('"1+2"')
    app1 = DemoMonitorApp(topo)
    app1.installRules()
    app2 = DemoRouterApp(topo)
    app2.installRules()

    # Query timing evaluation.
    time_log = open(TIME_LOG, "w")
    for i in range(1000):
        elapsed = send_query("flow")
        time_log.write(str(elapsed) + "\n")
    time_log.close()
    net.stop()
    #CLI(net)


#********************************************************************
# expr: query evaluation
#********************************************************************

# Trying to record time to execute the command.
def send_query(stat_type):
    start = time.clock()
    cmd = "curl --silent http://localhost:10001/wm/core/switch/%s/%s/json > /dev/null" \
          % (dpid, stat_type)
    subprocess.call(cmd, shell=True)
    finish = time.clock()
    elapsed = finish - start
    return elapsed

def send_queries(number=NUMBER):
    for i in range(number):
        send_query("flow")

# Send queries and create time log file.
def send_queries1(output_number, number=NUMBER):
    log_name = str(output_number) + TIME_LOG
    time_log = open(log_name, "w")
    print log_name + " opened."
    for i in range(NUMBER):
        elapsed = send_query("flow")
        time_log.write(str(elapsed) + "\n")
    time_log.close()
    print log_name + " closed."

#********************************************************************
# expr: sequential
#********************************************************************
def exprSequential():
    cleanAll()
    startFloodlight(2)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(SLEEP_TIME)
    createPlumbingGraph()
    addController1(topo)
    addController2(topo)
    createACL('1 dltype:exact,srcip:prefix,dstip:exact mod:dstip')
    createACL('2 dltype:exact,dstip:prefix output')
    createPolicy('"1>2"')
    app1 = DemoLoadBalancerApp(topo)
    app1.installRules()
    app2 = DemoRouterApp(topo)
    app2.installRules()
    CLI(net)

#********************************************************************
# expr: virt
#********************************************************************
def exprVirt():
    cleanAll()
    startFloodlight(3)
    startOVX()
    (topo, net) = startMininet()
    time.sleep(SLEEP_TIME)
    virtCreatePlumbingGraph()
    virtAddController1(topo)
    virtAddController2(topo)
    virtAddController3(topo)
    createACL('1 dltype:exact,dstip:prefix output')
    createACL('2 dltype:exact,dstip:prefix output,mod:dstip')
    createACL('3 dltype:exact,dstip:prefix output')
    virtCreatePolicy()
    app = DemoVirtApp(topo)
    app.installRules()
    start_iperf(net)
    time.sleep(STATS_TIME)
    app.send_query("flow")
    CLI(net)

#********************************************************************
# main
#********************************************************************
   
def printHelp():
    print "\tUsage: queries.py"
    print "\t\tstart-mn kill-mn"
    print "\t\tstart-ovx show-ovx kill-ovx"
    print "\t\tstart-fl show-fl kill-fl"
    print "\t\texpr-sequential expr-virt clean"
    print "\t\tclean-ryu"

if __name__ == '__main__':
    if len(sys.argv) < 2:
        exprParallel()
    else:
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
            startFloodlight(1)
        elif sys.argv[1] == "show-fl":
            showFloodlight()
        elif sys.argv[1] == "kill-fl":
            killFloodlight()
        elif sys.argv[1] == "clean":
            cleanAll()
        elif sys.argv[1] == "expr-sequential":
            exprSequential()
        elif sys.argv[1] == "expr-virt":
            exprVirt()
        elif sys.argv[1] == "expr-par":
            if len(sys.argv) == 3:
                exprParallel(int(sys.argv[2]))
            else:
                exprParallel()
        elif sys.argv[1] == "query":
            if len(sys.argv) == 3:
                send_queries(int(sys.argv[2]))
            else:
                send_queries()
        elif sys.argv[1] == "query1":
            send_queries1(sys.argv[2])
        else:
            printHelp()


