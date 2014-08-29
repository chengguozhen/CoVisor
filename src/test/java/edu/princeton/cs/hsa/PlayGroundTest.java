package edu.princeton.cs.hsa;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyFlowTable;
import edu.princeton.cs.policy.adv.RuleGenerationUtil;

public class PlayGroundTest extends TestCase {
	
	private static Logger log = LogManager.getLogger(PlayGroundTest.class.getName());

    public PlayGroundTest(final String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(PlayGroundTest.class);
    }

    public void test1() {
    	
    	PolicyFlowTable flowTable = new PolicyFlowTable();
    	OFFlowMod fm = RuleGenerationUtil.generateMonitoringRule(1, "1.0.0.0", 24, OFFlowMod.OFPFC_ADD);
    	PlumbingFlowMod pfm = new PlumbingFlowMod(fm, null);
    	flowTable.update(pfm);
    	for (OFFlowMod fmi: flowTable.getFlowMods()) {
    		PlumbingFlowMod fmp = (PlumbingFlowMod) fmi;
    		//log.error("{} {}", fmp.myNum, fmp);
    	}
    	
    	
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
