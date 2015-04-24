#!/usr/bin/python
import sys
import time
import random

def process(fileName):
    fout = open(fileName + "_addSoft", 'w')
    fin = open(fileName, 'r')
    oneline = fin.readline()
    while oneline != "":
        temp = oneline.strip().split()
        hardwareTime = float(temp[3]) + 0.0003
        softwareTime = (float(temp[0])+float(temp[1])*0.2) * 0.001 + 0.0003
        fout.write(oneline.strip() + "\t" + str(hardwareTime) + "\t" + str(softwareTime) +"\n")
        oneline = fin.readline()
    fin.close()
    fout.close()

if __name__ == '__main__':
	process("res_sequential_strawman_8000")
	process("res_sequential_inc_8000")
	process("res_sequential_incacl_8000")


