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
 * This class implements the test scenario for the FASTATaxonomyFilter class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.FASTATaxonomyFilter
 */
public class TestFASTATaxonomyFilter extends TestCaseLM {

    public TestFASTATaxonomyFilter() {
        this("Test scenario for the FASTATaxonomyFilter class.");
    }

    public TestFASTATaxonomyFilter(String aName) {
        super(aName);
    }

    /**
     * Test scenario for the FASTA db taxonomy.
     */
    public void testFilter() {
        // Test filter simple and with inversion.
        String emptyEntry = ">FASTA header number 1: no taxonomy data\nLENNARTMARTENS";
        String falseEntry = ">FASTA header nummer 2: incorrect taxonomy data [Mus musculus]\nLENNARTMARTENS";
        String trueEntry = ">FASTA header nummer 3: correct taxonomy data [Homo sapiens]\nLENNARTMARTENS";
        String alsoTrueEntry = ">FASTA header nummer 4: correct taxonomy data [Homo sapiens] [Rattus norvegicus]\nLENNARTMARTENS";
        Filter filter = new FASTAHeaderFilter("Homo sApIeNs");
        Assert.assertFalse(filter.passesFilter(emptyEntry));
        Assert.assertFalse(filter.passesFilter(falseEntry));
        Assert.assertTrue(filter.passesFilter(trueEntry));
        Assert.assertTrue(filter.passesFilter(alsoTrueEntry));
        // Inverted filter.
        filter = new FASTAHeaderFilter("Homo sapiens", true);
        Assert.assertTrue(filter.passesFilter(emptyEntry));
        Assert.assertTrue(filter.passesFilter(falseEntry));
        Assert.assertFalse(filter.passesFilter(trueEntry));
        Assert.assertFalse(filter.passesFilter(alsoTrueEntry));
    }
}
