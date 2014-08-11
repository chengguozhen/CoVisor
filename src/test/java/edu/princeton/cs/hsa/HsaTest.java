package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;

import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.RuleGenerationUtil;

public class HsaTest extends TestCase {
	
	private static Logger log = LogManager.getLogger(HsaTest.class.getName());

    public HsaTest(final String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(HsaTest.class);
    }

    public void test1() {
    	
    	// build graph
    	PlumbingGraph graph = new PlumbingGraph();
    	graph.addNode((long) 1);
    	graph.addNode((long) 2);
    	graph.addNode((long) 3);
    	graph.addPort((long) 1, (short) 1, (short) 4);
    	graph.addPort((long) 1, (short) 2, null);
    	graph.addPort((long) 1, (short) 3, null);
    	graph.addPort((long) 2, (short) 1, null);
    	graph.addPort((long) 2, (short) 2, (short) 5);
    	graph.addPort((long) 2, (short) 3, null);
    	graph.addPort((long) 3, (short) 1, null);
    	graph.addPort((long) 3, (short) 2, null);
    	graph.addPort((long) 3, (short) 3, (short) 6);
    	graph.addEdge((long) 1, (short) 2, (long) 2, (short) 1);
    	graph.addEdge((long) 1, (short) 3, (long) 3, (short) 1);
    	graph.addEdge((long) 2, (short) 3, (long) 3, (short) 2);
    	
    	graph.update(RuleGenerationUtil.generateDefaultRule(), graph.getNode((long) 1));
    	graph.update(RuleGenerationUtil.generateDefaultRule(), graph.getNode((long) 2));
    	graph.update(RuleGenerationUtil.generateDefaultRule(), graph.getNode((long) 3));
    	
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "1.0.0.0", 1, OFFlowMod.OFPFC_ADD), graph.getNode((long) 1));
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.0", 2, OFFlowMod.OFPFC_ADD), graph.getNode((long) 1));
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "3.0.0.0", 3, OFFlowMod.OFPFC_ADD), graph.getNode((long) 1));
    	
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "1.0.0.0", 1, OFFlowMod.OFPFC_ADD), graph.getNode((long) 2));
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.0", 2, OFFlowMod.OFPFC_ADD), graph.getNode((long) 2));
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "3.0.0.0", 3, OFFlowMod.OFPFC_ADD), graph.getNode((long) 2));
    	
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "1.0.0.0", 1, OFFlowMod.OFPFC_ADD), graph.getNode((long) 3));
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.0", 2, OFFlowMod.OFPFC_ADD), graph.getNode((long) 3));
    	graph.update(RuleGenerationUtil.generateRoutingRule(1, "3.0.0.0", 3, OFFlowMod.OFPFC_ADD), graph.getNode((long) 3));
    	
    	log.error(graph);
    	
    	
    	
    	
    	// insert flowmods
    	/*PolicyUpdateTable updateTable = null;
    	updateTable = graph.getNode((long) 1).update(RuleGenerationUtil.generateDefaultRule());
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 2).update(RuleGenerationUtil.generateDefaultRule());
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 3).update(RuleGenerationUtil.generateDefaultRule());
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 1).update(RuleGenerationUtil.generateRoutingRule(1, "1.0.0.0", 1, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 1).update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.0", 2, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 1).update(RuleGenerationUtil.generateRoutingRule(1, "3.0.0.0", 3, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 2).update(RuleGenerationUtil.generateRoutingRule(1, "1.0.0.0", 1, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 2).update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.0", 2, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 2).update(RuleGenerationUtil.generateRoutingRule(1, "3.0.0.0", 3, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 3).update(RuleGenerationUtil.generateRoutingRule(1, "1.0.0.0", 1, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 3).update(RuleGenerationUtil.generateRoutingRule(1, "2.0.0.0", 2, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}
    	
    	updateTable = graph.getNode((long) 3).update(RuleGenerationUtil.generateRoutingRule(1, "3.0.0.0", 3, OFFlowMod.OFPFC_ADD));
    	for (OFFlowMod fm : updateTable.addFlowMods) {
    		log.error(fm);
    	}
    	for (OFFlowMod fm : updateTable.deleteFlowMods) {
    		log.error(fm);
    	}*/
    	
    }
}
