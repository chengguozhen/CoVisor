package edu.princeton.cs.expr;

import java.util.Random;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.princeton.cs.hsa.PlumbingGraph;
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
    
    public void testCorrectness() {
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
    			.genFlowMod("priority=8,ether-type=2048,inport=6,dst-ip=1.0.0.0/8,actions=output:5"),
    			graph.getNode((long) 1));
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=8,ether-type=2048,dst-ip=3.0.0.0/8,actions=output:7"),
    			graph.getNode((long) 1));
    	
    	log.error(graph);
    	
    	// gateway switch
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=1,ether-type=2048,inport=9,actions=output:8"),
    			graph.getNode((long) 2));
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=8,ether-type=2048,inport=8,dst-ip=3.0.0.1,"
    					+ "actions=output:9,actions=set-src-mac:10:00:00:00:00:00,actions=set-dst-mac:00:00:00:00:00:01"),
    			graph.getNode((long) 2));
    	
    	log.error(graph);
    	
    	// mac leaner
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=1,inport=10,src-mac=10:00:00:00:00:00,dst-mac=00:00:00:00:00:01,actions=output:11"),
    			graph.getNode((long) 3));
    	
    	log.error(graph);
    	
    	graph.update(OFFlowModHelper
    			.genFlowMod("priority=1,inport=11,src-mac=10:00:00:00:00:00,dst-mac=00:00:00:00:00:02,actions=output:12"),
    			graph.getNode((long) 3));
    	
    	log.error(graph);
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
