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
    	
    	List<Integer> x = new ArrayList<Integer>();
    	x.add(null);
    	x.add(1);
    	
    	if(x.get(0) == null) {
    		log.error("null");
    	}
    	
    	if(x.get(1) != null) {
    		log.error("not null");
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
