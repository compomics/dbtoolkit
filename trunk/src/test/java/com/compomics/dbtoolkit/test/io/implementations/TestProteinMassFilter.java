/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 23-jan-03
 * Time: 9:56:03
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.ProteinMassFilter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;
import junit.TestCaseLM;
import junit.framework.Assert;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the ProteinMassFilter class.
 *
 * @author Lennart Martens
 */
public class TestProteinMassFilter extends TestCaseLM {

    public TestProteinMassFilter() {
        this("The test scenario for the ProteinMassFilter.");
    }

    public TestProteinMassFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests the filter.
     */
    public void testFilter() {
        final Protein pass = new Protein(">Protein that passes filter.\nLENNAR");
        final Protein noPass1 = new Protein(">Protein that does not pass filter.\nLENNARTMARTENS");
        final Protein noPass2 = new Protein(">Protein that does not pass filter.\nL");

        // Simplest constructor.
        ProteinFilter pf = new ProteinMassFilter(600.0, 1000.0);
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertFalse(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Constructor with flag for inversion (not set).
        pf = new ProteinMassFilter(600.0, 1000.0, false);
        Assert.assertTrue(pf.passesFilter(pass));
        Assert.assertFalse(pf.passesFilter(noPass1));
        Assert.assertFalse(pf.passesFilter(noPass2));

        // Constructor with flag for inversion (set).
        pf = new ProteinMassFilter(600.0, 1000.0, true);
        Assert.assertFalse(pf.passesFilter(pass));
        Assert.assertTrue(pf.passesFilter(noPass1));
        Assert.assertTrue(pf.passesFilter(noPass2));

    }
}
