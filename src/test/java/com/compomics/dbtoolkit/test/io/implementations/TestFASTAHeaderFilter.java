/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-mrt-03
 * Time: 11:07:27
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.FASTAHeaderFilter;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import junit.TestCaseLM;
import junit.framework.Assert;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the FASTAHeaderFilter class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.FASTAHeaderFilter
 */
public class TestFASTAHeaderFilter extends TestCaseLM {

    public TestFASTAHeaderFilter() {
        this("Test scenario for the FASTAHeaderFilter class.");
    }

    public TestFASTAHeaderFilter(String aName) {
        super(aName);
    }

    /**
     * Test scenario for the FASTA DB header.
     */
    public void testFilter() {
        // Test filter simple and with inversion.
        String entry = ">FASTA header number 1: no specific data\nLENNARTMARTENS";
        String falseEntry = ">FASTA header nummer 1: no specific data\nLENNARTMARTENS";
        Filter filter = new FASTAHeaderFilter("NuMber");
        Assert.assertTrue(filter.passesFilter(entry));
        Assert.assertFalse(filter.passesFilter(falseEntry));
        filter = new FASTAHeaderFilter("number", true);
        Assert.assertFalse(filter.passesFilter(entry));
        Assert.assertTrue(filter.passesFilter(falseEntry));
    }
}
