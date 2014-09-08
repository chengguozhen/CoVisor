package edu.princeton.cs.expr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.action.OFActionOutput;

import edu.princeton.cs.hsa.PlumbingGraph;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.RuleGenerationUtil;

public class GatewayExpr extends TestCase {
	
	private static Logger log = LogManager.getLogger(GatewayExpr.class.getName());
	private static Random rand = new Random(1);
	
    public GatewayExpr(final String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(GatewayExpr.class);
    }
    
    public void atestCorrectness() {
    	PlumbingGraph graph = new PlumbingGraph();
    	graph.addNode((long) 1);
    	graph.addNode((long) 2);
    	graph.addNode((long) 3);
    	graph.addPort((long) 1, (short) 5, (short) 1);
    	graph.addPort((long) 1, (short) 6, (short) 2);
    	graph.addPort((long) 1, (short) 7, null);
    	graph.addPort((long) 2, (short) 8, null);
    	graph.addPort((long) 2, (short) 9, null);
    	graph.addPort((long) 3, (short) 10, null);
    	graph.addPort((long) 3, (short) 11, (short) 3);
    	graph.addPort((long) 3, (short) 12, (short) 4);
    	graph.addEdge((long) 1, (short) 7, (long) 2, (short) 8);
    	graph.addEdge((long) 2, (short) 9, (long) 3, (short) 10);
    	
    	
    	// ip router
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=8,ether-type=2048,dst-ip=1.0.0.0/8,actions=output:5"),
    			graph.getNode((long) 1));
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=8,ether-type=2048,dst-ip=3.0.0.0/8,actions=output:7"),
    			graph.getNode((long) 1));
    	
    	// gateway switch
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=1,ether-type=2048,inport=9,actions=output:8"),
    			graph.getNode((long) 2));
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=8,ether-type=2048,inport=8,dst-ip=3.0.0.1,"
    					+ "actions=output:9,actions=set-src-mac:10:00:00:00:00:00,actions=set-dst-mac:00:00:00:00:00:01"),
    			graph.getNode((long) 2));
    	
    	// mac leaner
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=1,inport=10,src-mac=10:00:00:00:00:00,dst-mac=00:00:00:00:00:01,actions=output:11"),
    			graph.getNode((long) 3));
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=1,inport=11,src-mac=10:00:00:00:00:00,dst-mac=00:00:00:00:00:02,actions=output:12"),
    			graph.getNode((long) 3));
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=1,inport=12,src-mac=00:00:00:00:00:05,dst-mac=10:00:00:00:00:00,actions=output:10"),
    			graph.getNode((long) 3));
    	
    	log.error(graph);
    	
    	/*List<String> macs = genMACs(500);
    	List<String> ips = genIPs(500);
    	System.out.println(macs.get(0));
    	System.out.println(macs.get(100));
    	System.out.println(macs.get(255));
    	System.out.println(macs.get(256));
    	System.out.println(macs.get(300));
    	
    	System.out.println(ips.get(0));
    	System.out.println(ips.get(100));
    	System.out.println(ips.get(255));
    	System.out.println(ips.get(256));
    	System.out.println(ips.get(300));*/
    }
    
    public void testExpr() {
    	VirtualTopology expr = new VirtualTopology();
    	expr.testExpr();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
