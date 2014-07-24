from mininet.net import Mininet
from mininet.topo import Topo
from mininet.log import lg, setLogLevel
from mininet.cli import CLI
from mininet.node import RemoteController
from Rocketfuel.rocketfueltopo import RTopo 


class MNTopo(Topo):

    def __init__(self, enable_all = True,
        topoFile = "/home/xinjin/xin-flowmaster/" + \
        "OpenVirteX/experiments/ExprTopo/Rocketfuel/internet2/weights.intra"):

        "Create Rocketfuel topology for mininet."

        # Add default members to class.
        super(MNTopo, self).__init__()

        # init rocketfuel topo
        self.rtopo = RTopo()
        self.rtopo.parse(topoFile)

        # Add core switches
        self.dpids = {}
        for index, switch in enumerate(self.rtopo.switches):
            if index < 10:
                self.dpids[switch] = '0000000000000%d00' % index
            elif index < 100:
                self.dpids[switch] = '000000000000%d00' % index
            elif index < 1000:
                self.dpids[switch] = '00000000000%d00' % index
            self.addSwitch(switch, dpid=self.dpids[switch])

        # Add hosts and connect them to their core switch
        for switch in self.rtopo.switches:
            # Add host
            host = 'h_%s_1' % switch
            ip = '10.0.0.1'
            mac = self.dpids[switch][4:-1] + '1'
            h = self.addHost(host, ip=ip, mac=mac)
            # Connect hosts to core switches
            self.addLink(h, switch)

        # Connect core switches
        rlinks = []
        for u, vs in self.rtopo.edges.items():
            for v in vs:
                if not (v, u) in rlinks:
                    self.addLink(u, v)
                    rlinks.append((u,v))
        for link in rlinks:
            print link

if __name__ == '__main__':
    topo = MNTopo()
    raw_input("pause")
    net = Mininet(topo, autoSetMacs=True, xterms=False, controller=RemoteController)
    net.addController('c', ip='127.0.0.1') # localhost:127.0.0.1 vm-to-mac:10.0.2.2 server-to-mac:128.112.93.28
    print "\nHosts configured with IPs, switches pointing to OpenVirteX at 127.0.0.1 port 6633\n"
    net.start()
    CLI(net)
    net.stop()
    
