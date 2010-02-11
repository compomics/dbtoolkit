/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-okt-02
 * Time: 11:46:23
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.ProteinSequenceFilter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;
import junit.TestCaseLM;
import junit.framework.Assert;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the ProteinSequenceFilter class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.ProteinSequenceFilter
 */
public class TestProteinSequenceFilter extends TestCaseLM {

    public TestProteinSequenceFilter() {
        this("Test scenario for the ProteinSequenceFilter class.");
    }

    public TestProteinSequenceFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests construction and performance of the
     * 'inclusive' filter mode.
     */
    public void testInclusiveFilter() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Positive inclusieve test sequence for ProteinSequenceFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Negative inclusieve test sequence for ProteinSequenceFilter", "KRISGEVAERT");

        // A filter for a single residu.
        ProteinFilter filter = new ProteinSequenceFilter("M");

        // Assert function.
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));

        // A filter for a sequence of residues.
        filter = new ProteinSequenceFilter("ARTMAR");
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));

        // A filter using a '.' (requiring regexps).
        filter = new ProteinSequenceFilter("NN.R");
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));

        // Another regexp one.
        filter = new ProteinSequenceFilter("..RT...");
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));
    }

    /**
     * This method tests construction and performance of the
     * 'exclusive' filter mode.
     */
    public void testExclusiveFilter() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Negative exclusieve test sequence for ProteinSequenceFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Positive exclusieve test sequence for ProteinSequenceFilter", "KRISGEVAERT");

        // A filter for a single residu.
        ProteinFilter filter = new ProteinSequenceFilter("^M");

        // Assert function.
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));

        // A filter for a sequence of residues.
        filter = new ProteinSequenceFilter("^ARTMAR");
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));

        // A filter using a '.' (requiring regexps).
        filter = new ProteinSequenceFilter("^NN.R");
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));

        // Another regexp one.
        filter = new ProteinSequenceFilter("^..RT...");
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));
    }

    /**
     * This method tests the ability of the ProteinSequenceFilter to distinguish between
     * initiator met and other methionines.
     */
    public void testInitMetOption() {
        // The proteins we'll test.
        Protein in = new Protein(">Negative exclusieve test sequence for ProteinSequenceFilter", "MLENNARTARTENS");
        Protein in2 = new Protein(">Negative exclusieve test sequence for ProteinSequenceFilter", "MLENNARTMARTENS");
        Protein notIn = new Protein(">Positive exclusieve test sequence for ProteinSequenceFilter", "KRISMGEVAERT");
        Protein notIn2 = new Protein(">Positive exclusieve test sequence for ProteinSequenceFilter", "KRISGEVAERT");

        ProteinFilter filter = new ProteinSequenceFilter("U");
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(in2));
        Assert.assertTrue(filter.passesFilter(notIn));
        Assert.assertFalse(filter.passesFilter(notIn2));

        filter = new ProteinSequenceFilter("U..");
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(in2));
        Assert.assertTrue(filter.passesFilter(notIn));
        Assert.assertFalse(filter.passesFilter(notIn2));

        filter = new ProteinSequenceFilter("^U");
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(in2));
        Assert.assertFalse(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(notIn2));

        filter = new ProteinSequenceFilter("^U.");
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(in2));
        Assert.assertFalse(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(notIn2));

        filter = new ProteinSequenceFilter("ULEN");
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(in2));
        Assert.assertFalse(filter.passesFilter(notIn));
        Assert.assertFalse(filter.passesFilter(notIn2));
        Assert.assertTrue(filter.passesFilter(new Protein(">Quick one", "MMLENNART")));
        Assert.assertTrue(filter.passesFilter(new Protein(">Quick two", "MKRISMLEN")));

        filter = new ProteinSequenceFilter("UL.N");
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(in2));
        Assert.assertFalse(filter.passesFilter(notIn));
        Assert.assertFalse(filter.passesFilter(notIn2));
        Assert.assertTrue(filter.passesFilter(new Protein(">Quick one", "MMLENNART")));
        Assert.assertTrue(filter.passesFilter(new Protein(">Quick two", "MKRISMLEN")));

        filter = new ProteinSequenceFilter("^ULEN");
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(in2));
        Assert.assertTrue(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(notIn2));
        Assert.assertFalse(filter.passesFilter(new Protein(">Quick one", "MMLENNART")));
        Assert.assertFalse(filter.passesFilter(new Protein(">Quick two", "MKRISMLEN")));

        filter = new ProteinSequenceFilter("^UL.N");
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(in2));
        Assert.assertTrue(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(notIn2));
        Assert.assertFalse(filter.passesFilter(new Protein(">Quick one", "MMLENNART")));
        Assert.assertFalse(filter.passesFilter(new Protein(">Quick two", "MKRISMLEN")));
    }
}
