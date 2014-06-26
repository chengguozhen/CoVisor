package edu.princeton.cs.policy;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Parent class for Policy tests.
 */
public final class BasePolicyTests {

    /**
     * Overrides default constructor to no-op private constructor.
     * Required by checkstyle.
     */
    private BasePolicyTests() {
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite(BasePolicyTests.class.getName());
        // $JUnit-BEGIN$
        suite.addTest(ParallelCompositionTest.suite());
        suite.addTest(SequentialCompositionTest.suite());
        suite.addTest(PolicyTreeTest.suite());
        // $JUnit-END$
        return suite;
    }

}
