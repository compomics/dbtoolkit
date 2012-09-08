/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-okt-02
 * Time: 12:04:28
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.ProteinFilterCollection;
import com.compomics.dbtoolkit.io.implementations.ProteinMassFilter;
import com.compomics.dbtoolkit.io.implementations.ProteinResiduCountFilter;
import com.compomics.dbtoolkit.io.implementations.ProteinSequenceFilter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;
import junit.framework.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the full test scenario for the
 * ProteinFilterCollection class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.ProteinFilterCollection
 */
public class TestProteinFilterCollection extends TestCase {

    public TestProteinFilterCollection() {
        this("Full test scenario for the ProteinFilterCollection class.");
    }

    public TestProteinFilterCollection(String aName) {
        super(aName);
    }

    /**
     * This method tests a simple collection with a single filter.
     */
    public void testSimpleCollection() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Positive inclusieve test sequence for ProteinSequenceFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Negative inclusieve test sequence for ProteinSequenceFilter", "KRISGEVAERT");

        // A filter for a single residu.
        ProteinFilter filter = new ProteinSequenceFilter("M");

        // FilterCollection.
        ProteinFilterCollection pfc = new ProteinFilterCollection(ProteinFilterCollection.AND, false);
        pfc.add(filter);

        // Assert function.
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));

        // Okay, now an OR.
        pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, false);
        pfc.add(filter);

        // Assert function.
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));

        // Now the NOT with both.
        // (AND version).
        pfc = new ProteinFilterCollection(ProteinFilterCollection.AND, true);
        pfc.add(filter);

        // Assert function.
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));

        // (OR version).
        pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, true);
        pfc.add(filter);

        // Assert function.
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));

        filter = new ProteinResiduCountFilter("M", 1, ProteinResiduCountFilter.EQUALS_TO);
        pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, false);
        pfc.add(filter);

        // Assert function.
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));

        pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, true);
        pfc.add(filter);

        // Assert function.
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));
    }

    /**
     * This method tests a collection of AND filters.
     */
    public void testANDCollection() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Positive inclusieve test sequence for ProteinSequenceFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Negative inclusieve test sequence for ProteinSequenceFilter", "KRISGEVAERT");

        // A filter for a single residu.
        ProteinFilter filter1 = new ProteinSequenceFilter("M");
        ProteinFilter filter2 = new ProteinSequenceFilter("^K");
        ProteinFilter filter3 = new ProteinSequenceFilter("NN.RT");


        // FilterCollection.
        ProteinFilterCollection pfc = new ProteinFilterCollection(ProteinFilterCollection.AND, false);
        pfc.add(filter1);
        pfc.add(filter2);
        pfc.add(filter3);

        // Check...
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));

        // Now make sure everything fails.
        pfc.add(new ProteinSequenceFilter("ABC...XYZ...LK"));
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));

        // NOT mode.
        pfc = new ProteinFilterCollection(ProteinFilterCollection.AND, true);
        pfc.add(filter1);
        pfc.add(filter2);
        pfc.add(filter3);

        // Check...
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));

        // Now make sure everything fails, but this time
        // the inversion flag will make sure the result passes.
        pfc.add(new ProteinSequenceFilter("ABC...XYZ...LK"));
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));

        // The same for the ProtResCountFilter
        pfc = new ProteinFilterCollection(ProteinFilterCollection.AND, false);
        pfc.add(new ProteinResiduCountFilter("M", 0, ProteinResiduCountFilter.GREATER_THAN));
        pfc.add(new ProteinResiduCountFilter("M", 5, ProteinResiduCountFilter.LESS_THAN));
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));

        pfc = new ProteinFilterCollection(ProteinFilterCollection.AND, true);
        pfc.add(new ProteinResiduCountFilter("M", 0, ProteinResiduCountFilter.GREATER_THAN));
        pfc.add(new ProteinResiduCountFilter("M", 5, ProteinResiduCountFilter.LESS_THAN));
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));
    }

    /**
     * This method tests a collection of OR filters.
     */
    public void testORCollection() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Positive inclusieve test sequence for ProteinSequenceFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Negative inclusieve test sequence for ProteinSequenceFilter", "KRISGEVAERT");

        // A filter for a single residu.
        ProteinFilter filter1 = new ProteinSequenceFilter("M");
        ProteinFilter filter2 = new ProteinSequenceFilter("^N");


        // FilterCollection.
        ProteinFilterCollection pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, false);
        pfc.add(filter1);
        pfc.add(filter2);

        // Check...
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));

        // NOT mode.
        pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, true);
        pfc.add(filter1);
        pfc.add(filter2);

        // Check...
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));

        // The same for the ProtResCountFilter
        pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, false);
        pfc.add(new ProteinResiduCountFilter("M", 0, ProteinResiduCountFilter.GREATER_THAN));
        pfc.add(new ProteinResiduCountFilter("M", 5, ProteinResiduCountFilter.LESS_THAN));
        Assert.assertTrue(pfc.passesFilter(in));
        Assert.assertTrue(pfc.passesFilter(notIn));

        pfc = new ProteinFilterCollection(ProteinFilterCollection.OR, true);
        pfc.add(new ProteinResiduCountFilter("M", 0, ProteinResiduCountFilter.GREATER_THAN));
        pfc.add(new ProteinResiduCountFilter("M", 5, ProteinResiduCountFilter.LESS_THAN));
        Assert.assertFalse(pfc.passesFilter(in));
        Assert.assertFalse(pfc.passesFilter(notIn));
    }

    /**
     * This method tests a collection of FilterCollections.
     */
    public void testComplexCollection() {
        // The proteins we'll be testing.
        Protein in = new Protein(">Positive inclusieve test sequence for ProteinSequenceFilter", "LENNARTMARTENS");
        Protein notIn = new Protein(">Negative inclusieve test sequence for ProteinSequenceFilter", "KRISGEVAERT");

        // The Filters.
        ProteinFilter filter1 = new ProteinSequenceFilter("^M");
        ProteinFilter filter2 = new ProteinSequenceFilter("ARTMAR");

        // First collection.
        ProteinFilterCollection pfc1 = new ProteinFilterCollection(ProteinFilterCollection.OR, false);
        pfc1.add(filter1);
        pfc1.add(filter2);

        // More filters.
        ProteinFilter filter3 = new ProteinSequenceFilter("^S");
        ProteinFilterCollection pfc2 = new ProteinFilterCollection(ProteinFilterCollection.OR, true);
        pfc2.add(filter3);

        ProteinFilter filter4 = new ProteinSequenceFilter("L");
        ProteinFilter filter5 = new ProteinSequenceFilter("T");
        ProteinFilterCollection pfc3 = new ProteinFilterCollection(ProteinFilterCollection.AND, false);
        pfc3.add(filter4);
        pfc3.add(filter5);

        ProteinFilterCollection pfc4 = new ProteinFilterCollection(ProteinFilterCollection.AND, false);
        pfc4.add(pfc1);
        pfc4.add(pfc2);
        pfc4.add(pfc3);
        pfc4.add(new ProteinResiduCountFilter("E", 2, ProteinResiduCountFilter.EQUALS_TO));

        // Check results.
        Assert.assertTrue(pfc4.passesFilter(in));
        Assert.assertFalse(pfc4.passesFilter(notIn));

        ProteinFilterCollection pfc5 = new ProteinFilterCollection(ProteinFilterCollection.OR);
        pfc5.add(new ProteinResiduCountFilter("A", 2, ProteinResiduCountFilter.EQUALS_TO));
        pfc5.add(new ProteinResiduCountFilter("L", 0, ProteinResiduCountFilter.GREATER_THAN));

        pfc4.add(pfc5);

        // Check results.
        Assert.assertTrue(pfc4.passesFilter(in));
        Assert.assertFalse(pfc4.passesFilter(notIn));

        pfc4.add(new ProteinResiduCountFilter("K", 0, ProteinResiduCountFilter.GREATER_THAN));
        // Check results.
        Assert.assertFalse(pfc4.passesFilter(in));
        Assert.assertFalse(pfc4.passesFilter(notIn));
    }

    /**
     * This filter tests the combination of three ProteinFilters (mass, residucount and
     * sequence).
     */
    public void testThreeSomeFilter() {
        final Protein pass = new Protein(">Passes.\nLENNAR");
        final Protein noPass = new Protein(">Does not pass.\nLENNARTMARTENS");

        ProteinFilterCollection pfc = new ProteinFilterCollection(ProteinFilterCollection.AND);
        pfc.add(new ProteinResiduCountFilter("L", 1, ProteinResiduCountFilter.EQUALS_TO));
        pfc.add(new ProteinSequenceFilter("A"));
        pfc.add(new ProteinMassFilter(600.0, 1000.0));

        Assert.assertTrue(pfc.passesFilter(pass));
        Assert.assertFalse(pfc.passesFilter(noPass));
    }
}
