package edu.princeton.cs.expr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.naming.InitialContext;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFFlowMod;

import com.googlecode.concurrenttrees.common.Iterables;
import com.googlecode.concurrenttrees.common.PrettyPrinter;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radix.node.util.PrettyPrintable;

import edu.princeton.cs.policy.adv.PolicyTree;
import edu.princeton.cs.policy.adv.PolicyUpdateTable;
import edu.princeton.cs.policy.adv.RuleGenerationUtil;
import edu.princeton.cs.policy.adv.PolicyTree.PolicyOperator;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreKey;
import edu.princeton.cs.policy.store.PolicyFlowModStore.PolicyFlowModStoreType;
import edu.princeton.cs.trie.radix.ConcurrentIPRadixTree;
import edu.princeton.cs.trie.radix.IPRadixTree;

public class SequentialExpr extends TestCase {
	
	private static Logger log = LogManager.getLogger(SequentialExpr.class.getName());
	private static Random rand = new Random(1);
	
    public SequentialExpr(final String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(SequentialExpr.class);
    }

	public void testEmpty() {
		
		// init rules
		List<OFFlowMod> fwRules = readFwRules("experiments/classbench/fw1_5000");
		Collections.shuffle(fwRules, rand);
		List<OFFlowMod> routingRules = readRoutingRules("experiments/classbench/fw1_prefix");
		
		emptyHelper(fwRules, routingRules, 1000, 10);
		emptyHelper(fwRules, routingRules, 3000, 10);
		emptyHelper(fwRules, routingRules, 5000, 10);
		
	}
	
	private void emptyHelper (List<OFFlowMod> fwRules, List<OFFlowMod> routingRules,
			int initialRuleCount, int updateRuleCount) {
		// init policy tree
		List<PolicyFlowModStoreType> storeTypes = new ArrayList<PolicyFlowModStoreType>();
		//storeTypes.add(PolicyFlowModStoreType.PREFIX);
		storeTypes.add(PolicyFlowModStoreType.WILDCARD);
		List<PolicyFlowModStoreKey> storeKeys = new ArrayList<PolicyFlowModStoreKey>();
		//storeKeys.add(PolicyFlowModStoreKey.NETWORK_DST);
		storeKeys.add(PolicyFlowModStoreKey.ALL);

		PolicyTree leftTree = new PolicyTree(storeTypes, storeKeys);
		leftTree.tenantId = 1;

		PolicyTree rightTree = new PolicyTree(storeTypes, storeKeys);
		rightTree.tenantId = 2;

		PolicyTree policyTree = new PolicyTree();
		policyTree.operator = PolicyOperator.Sequential;
		policyTree.leftChild = leftTree;
		policyTree.rightChild = rightTree;

		// install initial rules
		initialRuleCount = Math.min(initialRuleCount, fwRules.size() - updateRuleCount);
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(fwRules.get(i), 1);
		}
		for (int i = 0; i < initialRuleCount; i++) {
			policyTree.update(routingRules.get(i), 2);
		}

		// install update rules
		long startTime = System.nanoTime();
		for (int i = 0; i < updateRuleCount; i++) {
			//long startTime = System.nanoTime();
			PolicyUpdateTable updateTable = policyTree.update(fwRules.get(initialRuleCount + i), 1);
			//long elapseTime = System.nanoTime() - startTime;
			//log.error("Time: {} ms", elapseTime / 1e6);
			/*log.error("Time: {} ms\t{}\t{}\t{}\t{}",
					elapseTime / 1e6,
					updateTable.addFlowMods.size(),
					updateTable.deleteFlowMods.size(),
					policyTree.leftChild.flowTable.getFlowMods().size(),
					policyTree.rightChild.flowTable.getFlowMods().size());*/
		}
		long elapseTime = System.nanoTime() - startTime;
		log.error("Time: {} ms", elapseTime / 1e6);
		
	}
	
	private List<OFFlowMod> readFwRules(String fileName) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				//log.error(line);
				
				String[] parts = line.split("\t");
				
				// source ip prefix
				// destination ip prefix
				String str = String.format("priority=%d,src-ip=%s,dst-ip=%s",
						getRandomNumber(1, 101),
						parts[0].substring(1),
						parts[1]
						);
				
				// source port
				String[] srcports = parts[2].split(":");
				if (srcports[0].trim().equals(srcports[1].trim())) {
					str = str + ",src-port=" + srcports[0].trim();
				}
				
				// destination port
				String[] dstports = parts[3].split(":");
				if (dstports[0].trim().equals(dstports[1].trim())) {
					str = str + ",dst-port=" + dstports[0].trim();
				}
				
				// protocol number
				String[] protocols = parts[4].split("/");
				if (protocols[1].equals("0xFF")) {
					str = str + ",protocol=" + Integer.parseInt(protocols[0].substring(2), 16);
				}
				
				// action
				if (rand.nextInt() % 2 == 1) {
					str = str + ",actions=output:1";
				}
				
				OFFlowMod fm = OFFlowModHelper.genFlowMod(str);
				flowMods.add(fm);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return flowMods;
	}
	
	private List<OFFlowMod> readRoutingRules(String fileName) {
		List<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("/");
				OFFlowMod fm = OFFlowModHelper.genFlowMod(
						String.format("priority=%s,src-ip=%s,actions=output:1",
								parts[1],
								line.trim()));
				flowMods.add(fm);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return flowMods;
	}
	
	// get random number in [min, max)
	public static int getRandomNumber(int min, int max) {
		return rand.nextInt(max - min) + min;
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
