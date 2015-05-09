package edu.princeton.cs.expr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;

public class ParallelExpr extends TestCase {
	
    private static Logger log = LogManager.getLogger(ParallelExpr.class.getName());
    private static Random rand = new Random(1);
	
    public ParallelExpr(final String name) {
        super(name);
    }
    
    public static TestSuite suite() {
        return new TestSuite(ParallelExpr.class);
    }
    
    private void myTime(int count) {
    	int x = 0;
    	long startTime = System.nanoTime();
	for (int i = 0; i < count; i++) {
	    x = 5 + rand.nextInt();
	}
	long elapseTime = System.nanoTime() - startTime;
	System.out.println(count + "\t" + elapseTime / 1e6 + "\t" + x);
    }
    
    public void testExpr() {
    	ParallelComposition test = new ParallelComposition();
    	test.testExpr();
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
