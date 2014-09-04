package edu.princeton.cs.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

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
    	
    	List<String> macs = genMACs(500);
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
    	System.out.println(ips.get(300));
    }
    
    public void atestExpr() {
    	List<String> macs = genMACs(500);
    	List<String> ips = genIPs(500);
    	List<OFFlowMod> ipRouterRules = initIPRouterRules(2);
    	List<OFFlowMod> gatewayRules = initGatewayRules(2, ips, macs);
    	List<OFFlowMod> macLearnerRules = initMACLearnerRules(3, macs);
    	
    	// build topology
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
    	for (OFFlowMod fm : ipRouterRules) {
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 1));
    	}
    	
    	// gateway switch
    	for (OFFlowMod fm : gatewayRules){
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 2));
    	}
    	
    	// mac learner
    	for (OFFlowMod fm : macLearnerRules){
    		//System.out.println(fm);
    		graph.update(fm, graph.getNode((long) 3));
    	}
    	
    	log.error(graph);
    	
    	// 
    }
    
    private List<OFFlowMod> initIPRouterRules(int count) {
    	List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < count; i++) {
    		int priority = OFFlowModHelper.getRandomNumber(1, 33);
    		int ip = (rand.nextInt() >> priority) << priority;
    		String rule = String.format("priority=%d,ether-type=2048,dst-ip=%d/%d,actions=output:5", priority, ip, priority);
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	
    	String rule = "priority=24,ether-type=2048,dst-ip=1.0.0.0/24,actions=output:7";
    	flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	
    	return flowMods;
    }
    
    private List<OFFlowMod> initGatewayRules(int count, List<String> ips, List<String> macs) {
    	List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < count; i++) {
    		String rule = String.format("priority=1,ether-type=2048,inport=8,dst-ip=%s,"
					+ "actions=output:9,actions=set-src-mac:10:00:00:00:00:00,actions=set-dst-mac:%s",
					ips.get(i), macs.get(i));
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	
    	String rule = "priority=1,ether-type=2048,inport=9,actions=output:8";
    	flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	
    	return flowMods;
    }
    
    private List<OFFlowMod> initMACLearnerRules(int count, List<String> macs) {
    	List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
    	for (int i = 0; i < count/3; i++) {
    		String rule = String.format("priority=1,inport=10,"
    				+ "src-mac=10:00:00:00:00:00,dst-mac=%s,actions=output:12", macs.get(i));
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	for (int i = 0; i < count/3; i++) {
    		String rule = String.format("priority=1,inport=12,"
    				+ "src-mac=%s,dst-mac=10:00:00:00:00:00,actions=output:10", macs.get(i));
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	for (int i = 0; i < count/3; i++) {
    		String rule = String.format("priority=1,inport=11,"
    				+ "src-mac=00:00:00:00:11:11,dst-mac=00:00:00:00:22:22,actions=output:12");
    		flowMods.add(OFFlowModHelper.genFlowMod(rule));
    	}
    	
    	return flowMods;
    }
    
    private List<String> genMACs(int count) {
    	List<String> macs = new ArrayList<String>();
    	for (int i = 0; i < 256 && i < count; i++) {
    		macs.add(String.format("00:00:00:00:00:%02x", i));
    	}
    	for (int i = 256; i < 256*256 && i < count; i++) {
    		macs.add(String.format("00:00:00:00:%02x:%02x", i / 256, i % 256));
    	}
    	return macs;
    }
    
    private List<String> genIPs(int count) {
    	List<String> ips = new ArrayList<String>();
    	for (int i = 0; i < 256 && i < count; i++) {
    		ips.add(String.format("1.0.0.%d", i));
    	}
    	for (int i = 256; i < 256*256 && i < count; i++) {
    		ips.add(String.format("1.0.%d.%d", i / 256, i % 256));
    	}
    	return ips;
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
