package edu.princeton.cs.policy;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.princeton.cs.policy.ofwrapper.OFMatchWrapper.ByteArrayWrapper;

public class ExactMatchTest extends TestCase {
	
	private static Logger log = LogManager.getLogger(ExactMatchTest.class.getName());

    public ExactMatchTest(final String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite() {
        return new TestSuite(ExactMatchTest.class);
    }

    public void test1() {
    	Random rand = new Random();
    	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    	List<Integer> list = new ArrayList<Integer>();
    	
    	for (int i = 0; i < 1e6; i++) {
    		int number = rand.nextInt();
    		map.put(number, number);
    		list.add(number);
    	}
    	
    	for (int i = 0; i < 10; i++) {
    		int number = rand.nextInt();
    		Boolean result = false;
    		long startTime = 0;
    		long elapseTime = 0;
    		
    		startTime = System.nanoTime();
    		result = map.containsKey(number);
    		elapseTime = System.nanoTime() - startTime;
    		System.out.println("dict: " + result + " " + elapseTime / (1e9));
    		
    		startTime = System.nanoTime();
    		result = list.contains(number);
    		elapseTime = System.nanoTime() - startTime;
    		System.out.println("list: " + result + " " + elapseTime / (1e9));
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
