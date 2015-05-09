package edu.princeton.cs.expr;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BaseExprs {
	
	private BaseExprs() {
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite(BaseExprs.class.getName());
        // $JUnit-BEGIN$
        suite.addTest(ParallelExpr.suite());
        // $JUnit-END$
        return suite;
    }

}
