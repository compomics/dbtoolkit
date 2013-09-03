package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.ProteinSequenceLengthFilter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class implements the test scenario for the ProteinSequenceLengthFilter class.
 *
 * Created with IntelliJ IDEA.
 * User: Lennart
 * Date: 03/09/13
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */
public class TestProteinSequenceLengthFilter  extends TestCase {

    public TestProteinSequenceLengthFilter() {
        this("The test scenario for the ProteinSequenceLengthFilter.");
    }

    public TestProteinSequenceLengthFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests the filter.
     */
    public void testFilter() {
        final Protein pass = new Protein(">Protein that passes lower and upper filter.\nLENNAR");
        final Protein noPass1 = new Protein(">Protein that does not pass upper filter.\nLENNARTMARTENS");
        final Protein noPass2 = new Protein(">Protein that does not pass lower filter.\nL");

        // Lower bound filter.
        ProteinFilter pf = new ProteinSequenceLengthFilter("6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Upper bound filter.
        pf = new ProteinSequenceLengthFilter("<6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertFalse(pf.passesFilter(noPass1));
        Assert.assertTrue(pf.passesFilter(noPass2));

        // Explicit lower bound filter.
        pf = new ProteinSequenceLengthFilter(">6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Filter with spaces.
        pf = new ProteinSequenceLengthFilter("   6   ");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Tolerated first nonsense character.
        pf = new ProteinSequenceLengthFilter("@6");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Two-digit implicit filter.
        pf = new ProteinSequenceLengthFilter("10");
        Assert.assertFalse(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Two-digit implicit filter.
        pf = new ProteinSequenceLengthFilter(">10");
        Assert.assertFalse(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Two-digit upper bound filter.
        pf = new ProteinSequenceLengthFilter("<10");
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertFalse(pf.passesFilter(noPass1));
        Assert.assertTrue(pf.passesFilter(noPass2));

        // Two-digit filter with spaces.
        pf = new ProteinSequenceLengthFilter("  >10   ");
        Assert.assertFalse(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));
    }
}