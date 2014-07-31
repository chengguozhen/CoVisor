#!/usr/bin/python
import sys
import time
import subprocess
import random

def getAverage(numbers):
    return sum(numbers)/float(len(numbers))

def processLog(logFile):
    time = []
    add = []
    delete = []

    fin = open(logFile, 'r')
    oneline = fin.readline()
    while oneline != "":
        temp = oneline.strip().split()
        time.append(float(temp[0]))
        add.append(float(temp[1]))
        delete.append(float(temp[2]))
        oneline = fin.readline()
    return (getAverage(time), getAverage(add), getAverage(delete))

def processLogs():
    fout = open('res_strawman', 'w')
    for i in range(100, 600, 100):
        (time, add, delete) = processLog('res_strawman_%d' % i)
        fout.write('%f\t%f\t%f\n' % (time, add, delete))
    fout.close()
    fout = open('res_inc', 'w')
    for i in range(100, 600, 100):
        (time, add, delete) = processLog('res_inc_%d' % i)
        fout.write('%f\t%f\t%f\n' % (time, add, delete))
    fout.close()

if __name__ == '__main__':
    processLogs()

