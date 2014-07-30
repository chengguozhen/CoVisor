#!/usr/bin/python
import sys
import time
import subprocess
import random
from ExprTopo.rftopo import *
from apps import *


def processLog(logFile):
    fin = open(logFile, 'r')
    fout = open('res.' + logFile, 'w')

    startTime = 0
    t0 = 0
    t1 = 0
    t2 = 0
    oneline = fin.readline()
    while oneline != "":
        if oneline.find("MagicTimestamp\t4") != -1:
            startTime = int(oneline.strip().split('\t')[-1])
            #fout.write(str(startTime) + '\n')
        if oneline.find("MagicTimestamp\t0") != -1:
            t0 = int(oneline.strip().split('\t')[-1])

            oneline = fin.readline()
            if oneline.find("MagicTimestamp\t1") != -1:
                t1 = int(oneline.strip().split('\t')[-1])
            else:
                oneline = fin.readline()
                continue

            oneline = fin.readline()
            if oneline.find("MagicTimestamp\t2") != -1:
                t2 = int(oneline.strip().split('\t')[-1])
            else:
                oneline = fin.readline()
                continue

            delta1 = (t2 - t1) / 1e6
            delta2 = (t2 - t0) / 1e6
            fout.write(str(delta1) + '\t' + str(delta2) + '\n')
        oneline = fin.readline()
    fin.close()
    fout.close()

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print "\tUsage: log_process.py logFile"
        sys.exit()

    logFile = sys.argv[1]
    processLog(logFile)


