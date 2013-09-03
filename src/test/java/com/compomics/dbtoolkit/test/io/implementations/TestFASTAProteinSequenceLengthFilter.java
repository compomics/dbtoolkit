package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.FASTAProteinSequenceLengthFilter;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.protein.Protein;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class implements the test scenario for the FASTAProteinSequenceLengthFilter class.
 *
 * Created with IntelliJ IDEA.
 * User: Lennart
 * Date: 03/09/13
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */
public class TestFASTAProteinSequenceLengthFilter extends TestCase {

    public TestFASTAProteinSequenceLengthFilter() {
        this("The test scenario for the FASTAProteinSequenceLengthFilter.");
    }

    public TestFASTAProteinSequenceLengthFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests the filter.
     */
    public void testFilter() {
        final String pass = ">FASTA header number 1: no taxonomy data\nLENNAR";
        final String noPass1 = ">FASTA header nummer 2: incorrect taxonomy data [Mus musculus]\nLENNARTMARTENS";
        final String noPass2 = ">FASTA header nummer 3: correct taxonomy data [Homo sapiens]\nLE";

        // Lower bound filter.
        Filter pf = new FASTAProteinSequenceLengthFilter("6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Upper bound filter.
        pf = new FASTAProteinSequenceLengthFilter("<6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertFalse(pf.passesFilter(noPass1));
        Assert.assertTrue(pf.passesFilter(noPass2));

        // Explicit lower bound filter.
        pf = new FASTAProteinSequenceLengthFilter(">6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Filter with spaces.
        pf = new FASTAProteinSequenceLengthFilter("   6   ");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Tolerated first nonsense character.
        pf = new FASTAProteinSequenceLengthFilter("@6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Two-digit implicit filter.
        pf = new FASTAProteinSequenceLengthFilter("10");
        Assert.assertFalse(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Two-digit implicit filter.
        pf = new FASTAProteinSequenceLengthFilter(">10");
        Assert.assertFalse(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Two-digit upper bound filter.
        pf = new FASTAProteinSequenceLengthFilter("<10");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertFalse(pf.passesFilter(noPass1));
        Assert.assertTrue(pf.passesFilter(noPass2));

        // Two-digit filter with spaces.
        pf = new FASTAProteinSequenceLengthFilter("  >10   ");
        Assert.assertFalse(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));
    }
}