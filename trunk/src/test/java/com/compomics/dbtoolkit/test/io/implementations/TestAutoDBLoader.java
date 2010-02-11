/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 10-okt-02
 * Time: 18:25:28
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.*;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import junit.TestCaseLM;
import junit.framework.Assert;

import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class implements the testscenario for the
 * AutoDBLoader class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.AutoDBLoader
 */
public class TestAutoDBLoader extends TestCaseLM {

    public TestAutoDBLoader() {
        this("Test scenario for the AutoDBLoader class.");
    }

    public TestAutoDBLoader(String aName) {
        super(aName);
    }

    /**
     * This method tests the automatic detection
     * functionality.
     */
    public void testAutoDetectionBehaviour() {
        final String cleanFASTA = super.getFullFilePath("testFASTA.fas");
        final String zippedCleanFASTA = super.getFullFilePath("testFASTA.zip");
        final String cleanSW = super.getFullFilePath("test.spr");
        final String zippedCleanSW = super.getFullFilePath("test_SPFormat.zip");
        final String autoFASTA = super.getFullFilePath("testAutoFASTA.fas");
        final String zippedAutoFASTA = super.getFullFilePath("testAutoFASTA.zip");
        final String autoSW = super.getFullFilePath("testAutoSW.spr");
        final String zippedAutoSW = super.getFullFilePath("testAutoSW.zip");
        final String autoEmpty = super.getFullFilePath("testAutoEmpty.txt");
        final String autoUnknown = super.getFullFilePath("testAutoUnknown.txt");

        AutoDBLoader auto = new AutoDBLoader(new String[] {"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader",
                                                           "com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader"});

        try {
            DBLoader loader = auto.getLoaderForFile(cleanSW);
            Assert.assertEquals(DBLoader.SWISSPROT, loader.getDBName());
            Assert.assertTrue(loader instanceof SwissProtDBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format clean SwissProt was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(zippedCleanSW);
            Assert.assertEquals(DBLoader.SWISSPROT, loader.getDBName());
            Assert.assertTrue(loader instanceof ZippedSwissProtDBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format clean SwissProt was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(autoSW);
            Assert.assertEquals(DBLoader.SWISSPROT, loader.getDBName());
            Assert.assertTrue(loader instanceof SwissProtDBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format dirty SwissProt was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(zippedAutoSW);
            Assert.assertEquals(DBLoader.SWISSPROT, loader.getDBName());
            Assert.assertTrue(loader instanceof ZippedSwissProtDBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format dirty SwissProt was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(cleanFASTA);
            Assert.assertEquals(DBLoader.FASTA, loader.getDBName());
            Assert.assertTrue(loader instanceof FASTADBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format clean FASTA was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(zippedCleanFASTA);
            Assert.assertEquals(DBLoader.FASTA, loader.getDBName());
            Assert.assertTrue(loader instanceof ZippedFASTADBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format clean FASTA was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(autoFASTA);
            Assert.assertEquals(DBLoader.FASTA, loader.getDBName());
            Assert.assertTrue(loader instanceof FASTADBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format dirty FASTA was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(zippedAutoFASTA);
            Assert.assertEquals(DBLoader.FASTA, loader.getDBName());
            Assert.assertTrue(loader instanceof ZippedFASTADBLoader);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("DB format dirty FASTA was not detected automatically by the AutoDBloader class: '" + udfe.getMessage() + "'.");
        }

        try {
            DBLoader loader = auto.getLoaderForFile(autoEmpty);
            fail("AutoDBLoader did NOT throw an UnknownDBFormatException when confronted with an empty file (only blanks)!");
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            // We expect this.
        }

        try {
            DBLoader loader = auto.getLoaderForFile(autoUnknown);
            fail("AutoDBLoader did NOT throw an UnknownDBFormatException when confronted with an unknown file format!");
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader detection: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            // We expect this.
        }
    }

    /**
     * This method tests whether the AutoDBLoader correctly inits the DB when
     * detecting a suitable DBLoader.
     */
    public void testInitBehaviour() {
        final String cleanFASTA = super.getFullFilePath("testFASTA.fas");
        try {
            AutoDBLoader auto = new AutoDBLoader(new String[] {"com.compomics.dbtoolkit.io.implementations.FASTADBLoader"});
            DBLoader loader = auto.getLoaderForFile(cleanFASTA);
            Assert.assertTrue(loader.nextFASTAEntry() != null);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader init: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("AutoDBLoader threw an UnknownDBFormatException when confronted with a FASTA file!");
        }

        try {
            AutoDBLoader auto = new AutoDBLoader(new String[] {"com.compomics.dbtoolkit.io.implementations.FASTADBLoader"});
            DBLoader loader = auto.getLoaderForFile(cleanFASTA, true);
            Assert.assertTrue(loader.nextFASTAEntry() != null);
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader init: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("AutoDBLoader threw an UnknownDBFormatException when confronted with a FASTA file!");
        }

        try {
            AutoDBLoader auto = new AutoDBLoader(new String[] {"com.compomics.dbtoolkit.io.implementations.FASTADBLoader"});
            DBLoader loader = auto.getLoaderForFile(cleanFASTA, false);
            loader.nextFASTAEntry();
            fail("Apparently, the AutoDBLoader initialized the DBLoader whereas we specified that it shouldn't!");
        } catch(IOException ioe) {
            fail("IOException occurred while testing the automatic DBLoader init: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("AutoDBLoader threw an UnknownDBFormatException when confronted with a FASTA file!");
        } catch(NullPointerException npe) {
            // We want this to happen.
        }
    }
}
