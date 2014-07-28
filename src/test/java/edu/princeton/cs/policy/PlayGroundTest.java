package edu.princeton.cs.policy;

import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.concurrenttrees.common.Iterables;
import com.googlecode.concurrenttrees.common.PrettyPrinter;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radix.node.util.PrettyPrintable;

import edu.princeton.cs.trie.radix.ConcurrentIPRadixTree;
import edu.princeton.cs.trie.radix.IPRadixTree;

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

    /*public void test1() {
    	RadixTree<Integer> tree = new ConcurrentRadixTree<Integer>(new DefaultCharArrayNodeFactory());

        tree.put("TEST", 1);
        tree.put("TOAST", 2);
        tree.put("TEAM", 3);

        System.out.println("Tree structure:");
        // PrettyPrintable is a non-public API for testing, prints semi-graphical representations of trees...
        PrettyPrinter.prettyPrint((PrettyPrintable) tree, System.out);

        System.out.println();
        System.out.println("Value for 'TEST' (exact match): " + tree.getValueForExactKey("TEST"));
        System.out.println("Value for 'TOAST' (exact match): " + tree.getValueForExactKey("TOAST"));
        System.out.println();
        System.out.println("Keys starting with 'T': " + Iterables.toString(tree.getKeysStartingWith("T")));
        System.out.println("Keys starting with 'TE': " + Iterables.toString(tree.getKeysStartingWith("TE")));
        System.out.println();
        System.out.println("Values for keys starting with 'TE': " + Iterables.toString(tree.getValuesForKeysStartingWith("TE")));
        System.out.println("Key-Value pairs for keys starting with 'TE': "
        	+ Iterables.toString(tree.getKeyValuePairsForKeysStartingWith("TE")));
        System.out.println();
        System.out.println("Keys closest to 'TEMPLE': " + Iterables.toString(tree.getClosestKeys("TEMPLE")));
    }*/
    
    public void testIP() {
    	
    	//RadixTree<Integer> tree = new ConcurrentRadixTree<Integer>(new DefaultCharArrayNodeFactory());
    	//tree.put("10.0.0.0", arg1)
    	
    	IPRadixTree<Integer> tree = new ConcurrentIPRadixTree<Integer>(new DefaultCharArrayNodeFactory());

        tree.put("12111", 1);
        tree.put("12", 2);
        tree.put("11121112", 3);
        tree.put("1", 4);
        tree.put("111", 5);
        tree.put("11221112", 6);
        tree.put("111211121", 7);
        tree.put("111211122", 8);

        System.out.println("Tree structure:");
        // PrettyPrintable is a non-public API for testing, prints semi-graphical representations of trees...
        PrettyPrinter.prettyPrint((PrettyPrintable) tree, System.out);

        System.out.println();
        System.out.println("Value for '12' (exact match): " + tree.getValueForExactKey("12"));
        System.out.println("Value for '12111' (exact match): " + tree.getValueForExactKey("12111"));
        System.out.println();
        System.out.println("Keys starting with '11': " + Iterables.toString(tree.getKeyValuePairsForKeysStartingWith("11")));
        System.out.println("Keys starting with '11121112': " + tree.getIPKeyValuePairsForKeysStartingWith("11121112"));
        System.out.println("Keys starting with '11121': " + tree.getIPKeyValuePairsForKeysStartingWith("11121"));
        System.out.println("Keys starting with '11': " + tree.getIPKeyValuePairsForKeysStartingWith("11"));
        System.out.println("Keys starting with '1': " + tree.getIPKeyValuePairsForKeysStartingWith("1"));
        System.out.println("Keys starting with '': " + tree.getIPKeyValuePairsForKeysStartingWith(""));
        System.out.println();
    	
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
