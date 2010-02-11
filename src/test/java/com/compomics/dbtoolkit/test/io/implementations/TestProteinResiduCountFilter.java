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

import com.compomics.dbtoolkit.io.implementations.ProteinResiduCountFilter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;
import junit.TestCaseLM;
import junit.framework.Assert;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the ProteinResiduCountFilter class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.ProteinResiduCountFilter
 */
public class TestProteinResiduCountFilter extends TestCaseLM {

    public TestProteinResiduCountFilter() {
        this("Test scenario for the ProteinResiduCountFilter class.");
    }

    public TestProteinResiduCountFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests construction and performance of the
     * 'inclusive' filter mode.
     */
    public void testInclusiveFilter() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Positive inclusieve test sequence for ProteinResiduCountFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Negative inclusieve test sequence for ProteinResiduCountFilter", "KRISGEVAERT");

        // A filter for a single residu.
        ProteinFilter filterEq = new ProteinResiduCountFilter("M", 1, ProteinResiduCountFilter.EQUALS_TO);
        ProteinFilter filterGt = new ProteinResiduCountFilter("M", 3, ProteinResiduCountFilter.GREATER_THAN);
        ProteinFilter filterLt = new ProteinResiduCountFilter("M", 2, ProteinResiduCountFilter.LESS_THAN);
        ProteinFilter filterS = new ProteinResiduCountFilter("S", 2, ProteinResiduCountFilter.LESS_THAN);

        // Assert function.
        Assert.assertTrue(filterEq.passesFilter(in));
        Assert.assertFalse(filterEq.passesFilter(notIn));

        Assert.assertFalse(filterGt.passesFilter(in));
        Assert.assertFalse(filterGt.passesFilter(notIn));

        Assert.assertTrue(filterLt.passesFilter(in));
        Assert.assertTrue(filterLt.passesFilter(notIn));

        Assert.assertTrue(filterS.passesFilter(in));
        Assert.assertTrue(filterS.passesFilter(notIn));


        // A filter for multiple occurrences of residues.
        filterEq = new ProteinResiduCountFilter("A", 1, ProteinResiduCountFilter.GREATER_THAN);
        Assert.assertTrue(filterEq.passesFilter(in));
        Assert.assertFalse(filterEq.passesFilter(notIn));

        filterEq = new ProteinResiduCountFilter("A", 2, ProteinResiduCountFilter.EQUALS_TO);
        Assert.assertTrue(filterEq.passesFilter(in));
        Assert.assertFalse(filterEq.passesFilter(notIn));

        filterEq = new ProteinResiduCountFilter("A", 8, ProteinResiduCountFilter.LESS_THAN);
        Assert.assertTrue(filterEq.passesFilter(in));
        Assert.assertTrue(filterEq.passesFilter(notIn));
    }

    /**
     * This method tests construction and performance of the
     * 'exclusive' filter mode.
     */
    public void testExclusiveFilter() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Negative exclusieve test sequence for ProteinResiduCountFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Positive exclusieve test sequence for ProteinResiduCountFilter", "KRISGEVAERT");

        // A filter for a single residu.
        ProteinFilter filter = new ProteinResiduCountFilter("M", 1, ProteinResiduCountFilter.EQUALS_TO, true);

        // Assert function.
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));

        // A filter for a sequence of residues.
        filter = new ProteinResiduCountFilter("ARTMAR", 0, ProteinResiduCountFilter.GREATER_THAN, true);
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));

        filter = new ProteinResiduCountFilter("ARTMAR", 1, ProteinResiduCountFilter.EQUALS_TO, true);
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));

        filter = new ProteinResiduCountFilter("ARTMAR", 3, ProteinResiduCountFilter.LESS_THAN, true);
        Assert.assertFalse(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));
    }

    /**
     * This method tests the usage of the 'U' 'initMet skip' replacement for
     * Methionine ('M').
     */
    public void testInitMetSkipping() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Negative exclusieve test sequence for ProteinResiduCountFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Positive exclusieve test sequence for ProteinResiduCountFilter", "KRISGEVAERT");
        Protein mIn = new Protein(">Initiator methionine present + another.", "MLENNARTMARTENS");
        Protein mNotIn = new Protein(">Initiator methionine present, but no other.", "MKRISGEVAERT");

        ProteinFilter filter = new ProteinResiduCountFilter("U", 1, ProteinResiduCountFilter.EQUALS_TO);
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(mIn));
        Assert.assertFalse(filter.passesFilter(mNotIn));

        filter = new ProteinResiduCountFilter("U", 1, ProteinResiduCountFilter.LESS_THAN, true);
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(mIn));
        Assert.assertFalse(filter.passesFilter(mNotIn));

        filter = new ProteinResiduCountFilter("UART", 1, ProteinResiduCountFilter.LESS_THAN, true);
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertFalse(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(mIn));
        Assert.assertFalse(filter.passesFilter(mNotIn));

        filter = new ProteinResiduCountFilter("U", 1, ProteinResiduCountFilter.GREATER_THAN, true);
        Assert.assertTrue(filter.passesFilter(in));
        Assert.assertTrue(filter.passesFilter(notIn));
        Assert.assertTrue(filter.passesFilter(mIn));
        Assert.assertTrue(filter.passesFilter(mNotIn));
    }
}
