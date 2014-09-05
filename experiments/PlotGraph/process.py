#!/usr/bin/python
import sys
import time
import random

def readSwitchTime(fileName = "switch_time.txt"):
    switchTime = list()
    fin = open(fileName, 'r')
    oneline = fin.readline()
    while oneline != "":
        switchTime.append(float(oneline) / 300)
        oneline = fin.readline()
    fin.close()
    return switchTime

def generateTime(inFile, outFile, switchTime, rounds = 100):
    fin = open(inFile, 'r')
    rules = list()
    oneline = fin.readline()
    while oneline != "":
        rules.append(int(oneline))
        oneline = fin.readline()
    fin.close()

    fout = open(outFile, 'w')
    fout.write("10\tCoVisor\tCoVisor\tCoVisor\n")
    for rule in rules:
        updateTimes = list()
        for i in range(rounds):
            updateTime = 0;
            for j in range(rule):
                updateTime += switchTime[random.randint(0, len(switchTime)-1)]
            updateTimes.append(updateTime)
        updateTimes.sort()
        fout.write(str(rule) + "\t" + \
            str(updateTimes[4]) + "\t" + \
            str(updateTimes[49]) + "\t" + \
            str(updateTimes[94]) + "\n")
    fout.close()


if __name__ == '__main__':
    switchTime = readSwitchTime()
    generateTime("log", "res", switchTime)


