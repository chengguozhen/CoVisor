package edu.princeton.cs.policy.adv;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.concurrenttrees.common.PrettyPrinter;
import com.googlecode.concurrenttrees.radix.node.util.PrettyPrintable;

import edu.princeton.cs.iptrie.IPTrie;

public class TrieTest extends TestCase {
	
	private static Logger log = LogManager.getLogger(TrieTest.class.getName());

    public TrieTest(final String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(TrieTest.class);
    }
    
    public void test1() {
    	
    	IPTrie<Integer> tree = new IPTrie<Integer>();
    	
    	tree.put("000", 1);
        tree.put("100", 2);
        tree.put("100000", 3);
        tree.put("10000000", 4);

        System.out.println();
        System.out.println("000: " + tree.getExact("000"));
        System.out.println("100: " + tree.getExact("100"));
        System.out.println("100000: " + tree.getExact("100000"));
        System.out.println("10000000: " + tree.getExact("10000000"));

        System.out.println();
        System.out.println("Keys starting with '': " + tree.get(""));
        System.out.println("Keys starting with '10': " + tree.get("10"));
        System.out.println("Keys starting with '11': " + tree.get("11"));
        System.out.println("Keys starting with '100': " + tree.get("100"));
        System.out.println("Keys starting with '101': " + tree.get("101"));
        System.out.println("Keys starting with '1001': " + tree.get("1001"));
        System.out.println("Keys starting with '10000': " + tree.get("10000"));
        System.out.println("Keys starting with '10001': " + tree.get("10001"));
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
