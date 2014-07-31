#!/usr/bin/python
import sys
import subprocess
import time
import random


def countSubnets(classbenchFile):
    subnets = set()
    f = open(classbenchFile, 'r')
    oneline = f.readline()
    while oneline != "":
        temp = oneline.strip().split()
        subnets.add(temp[0][1:])
        subnets.add(temp[1])
        oneline = f.readline()
    f.close()
    print len(subnets)
    #subnets = list(subnets)

if __name__ == '__main__':
    countSubnets(sys.argv[1])

