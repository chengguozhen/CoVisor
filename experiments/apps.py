#!/usr/bin/python
import sys
import time
import random
from ExprTopo.rftopo import *

#********************************************************************
# Routing App
#********************************************************************
class RoutingApp():

    def __init__(self, topo, classbenchFile):
        self.graph = topo.graph
        self.rules = {}
        self.ruleCount = 0

        # assign subnets to switches
        subnets = set()
        f = open(classbenchFile, 'r')
        oneline = f.readline()
        while oneline != "":
            temp = oneline.strip().split()
            subnets.add(temp[0][1:])
            subnets.add(temp[1])
            oneline = f.readline()
        f.close()
        subnets = list(subnets)

        perSwSubnetCount = len(subnets) / len(topo.graph.nodes())
        for index, switch in enumerate(topo.graph.nodes()):
            topo.graph.node[switch]['subnets'] = []
            for snIndex in range(perSwSubnetCount):
                subnet = subnets[index * perSwSubnetCount + snIndex]
                topo.graph.node[switch]['subnets'].append(subnet)

    def genRules(self):
        paths = nx.all_pairs_dijkstra_path(self.graph)
        for dst in self.graph.nodes():
            visited = set()
            for src in self.graph.nodes():
                if src == dst:
                    continue
                path = paths[src][dst]
                if len(path) == 0:
                    continue
                prev = path[0]
                for sw in path[1:]:
                    if prev in visited:
                        prev = sw
                        continue
                    visited.add(prev)
                    dpid = self.graph.node[prev]['dpid']
                    output = self.graph.edge[prev][sw][prev]
                    for subnet in self.graph.node[dst]['subnets']:
                        name = "RouteApp%d" % self.ruleCount
                        rule = '{"switch":"%s", ' % dpid
                        rule = rule + '"name":"%s", ' % name
                        rule = rule + '"priority":"%s", ' % subnet.split('/')[1]
                        rule = rule + '"ether-type":"2048", '
                        rule = rule + '"dst-ip":"%s", ' % subnet
                        rule = rule + '"active":"true", "actions":"output=%d"}' % output
                        self.rules[name] = rule
                        self.ruleCount += 1
                    prev = sw
        print "generate total rules:", len(self.rules)


#********************************************************************
# Firewall App
#********************************************************************

