#!/usr/bin/python
import sys
import time
import subprocess
import random
from ExprTopo.rftopo import *
from apps import *


def processLog(logFile, resLogFile):
    fin = open(logFile, 'r')
    fout = open(resLogFile, 'w')

    startTime = 0
    t0 = 0
    t1 = 0
    t2 = 0
    oneline = fin.readline()
    while oneline != "":
        if oneline.find("MagicTimestamp\t4") != -1:
            #startTime = int(oneline.strip().split('\t')[-1])
            break
        oneline = fin.readline()
    oneline = fin.readline()
    while oneline != "":
        if oneline.find("MagicTimestamp\t2") != -1:
            info = oneline.strip().split('\t')
            time = int(info[2]) / 1e6
            fout.write(str(time) + '\t' + '\t'.join(info[3:]) + '\n')
        oneline = fin.readline()
    fin.close()
    fout.close()

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print "\tUsage: log_process.py logFile resLogFile"
        sys.exit()

    logFile = sys.argv[1]
    resLogFile = sys.argv[2]
    processLog(logFile, resLogFile)


