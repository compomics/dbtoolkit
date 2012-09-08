/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 20-jan-03
 * Time: 11:27:57
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.SequenceRegion;
import junit.framework.*;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the tests for the SequenceRegion class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.SequenceRegion
 */
public class TestSequenceRegion extends TestCase {

    public TestSequenceRegion() {
        this("Test scenario for the SequenceRegion class.");
    }

    public TestSequenceRegion(String aName) {
        super(aName);
    }

    /**
     * This method tests the construction and the relevant getters and setters.
     */
    public void testConstruction() {
        final String accesion = "P04775";
        final String sequence = "LENNART";
        final int nterm = 5;
        final int cterm = 3;

        // 'Symmetrical' constructor.
        SequenceRegion sr = new SequenceRegion(accesion, sequence, nterm);
        Assert.assertEquals(accesion, sr.getAccession());
        Assert.assertEquals(sequence, sr.getQuerySequence());
        Assert.assertEquals(nterm, sr.getNterminalResidueCount());
        Assert.assertEquals(nterm, sr.getCterminalResidueCount());
        Assert.assertTrue(sr.getNterminalAddition() == null);
        Assert.assertTrue(sr.getCterminalAddition() == null);
        Assert.assertTrue(sr.getRetrievedSequence() == null);
        Assert.assertFalse(sr.isQueried());
        Assert.assertFalse(sr.isFound());

        // 'Asymmetrical' constructor.
        sr = new SequenceRegion(accesion, sequence, nterm, cterm);
        Assert.assertEquals(accesion, sr.getAccession());
        Assert.assertEquals(sequence, sr.getQuerySequence());
        Assert.assertEquals(nterm, sr.getNterminalResidueCount());
        Assert.assertEquals(cterm, sr.getCterminalResidueCount());
        Assert.assertTrue(sr.getNterminalAddition() == null);
        Assert.assertTrue(sr.getCterminalAddition() == null);
        Assert.assertTrue(sr.getRetrievedSequence() == null);
        Assert.assertFalse(sr.isQueried());
        Assert.assertFalse(sr.isFound());

        // Getters have been tested, now setters.
        sr.setCterminalAddition("TGT");
        sr.setNterminalAddition("HGYT");
        sr.setFound(true);
        sr.setQueried(true);
        Assert.assertEquals(accesion, sr.getAccession());
        Assert.assertEquals(sequence, sr.getQuerySequence());
        Assert.assertEquals(nterm, sr.getNterminalResidueCount());
        Assert.assertEquals(cterm, sr.getCterminalResidueCount());
        Assert.assertEquals("HGYT", sr.getNterminalAddition());
        Assert.assertEquals("TGT", sr.getCterminalAddition());
        Assert.assertEquals("HGYT" + sequence + "TGT", sr.getRetrievedSequence());
        Assert.assertTrue(sr.isQueried());
        Assert.assertTrue(sr.isFound());
    }
}
