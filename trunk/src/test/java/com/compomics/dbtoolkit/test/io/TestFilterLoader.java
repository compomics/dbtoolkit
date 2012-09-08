/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-aug-2003
 * Time: 16:22:32
 */
package com.compomics.dbtoolkit.test.io;

import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.FilterLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.*;

import java.io.File;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the tests cenario for the FilterLoader class.
 *
 * @author Lennart Marte,s
 * @see com.compomics.dbtoolkit.io.FilterLoader
 */
public class TestFilterLoader extends TestCase {

    public TestFilterLoader() {
        this("Test scenario for the FilterLoader class.");
    }

    public TestFilterLoader(String aName) {
        super(aName);
    }

    /**
     * This method tests the filter loading.
     */
    public void testLoading() {
        DBLoader fastaLoader = null;
        try {
            fastaLoader = DBLoaderLoader.loadDB(new File(TestCaseLM.getFullFilePath("testFASTA.fas")));
        } catch(IOException ioe) {
            fail("IOException thrown when creating the FASTA DBLoader: " + ioe.getMessage());
        }

        DBLoader spLoader = null;
        try {
            spLoader = DBLoaderLoader.loadDB(new File(TestCaseLM.getFullFilePath("test.spr")));
        } catch(IOException ioe) {
            fail("IOException thrown when creating the SwissProt DBLoader: " + ioe.getMessage());
        }

        try {
            Filter f = FilterLoader.loadFilter("FASTAtaxonomy", "Homo sapiens", fastaLoader);
            Assert.assertTrue(f != null);
        } catch(IOException ioe) {
            fail("IOException thrown when testing the loadFilter method: " + ioe.getMessage());
        }

        try {
            FilterLoader.loadFilter("FASTAtaxonomy", null, fastaLoader);
            fail("No IOException thrown when testing the loadFilter method with a filter that takes a parameter, yet 'null' for this parameter!");
        } catch(IOException ioe) {
            // OK.
        }


        try {
            Filter f = FilterLoader.loadFilter("SPtaxonomy", "Homo sapiens", spLoader);
            Assert.assertTrue(f != null);
        } catch(IOException ioe) {
            fail("IOException thrown when testing the loadFilter method: " + ioe.getMessage());
        }

        try {
            FilterLoader.loadFilter("SPtaxonomy", null, spLoader);
            fail("No IOException thrown when testing the loadFilter method with a filter that takes a parameter, yet 'null' for this parameter!");
        } catch(IOException ioe) {
            // OK.
        }

        try {
            FilterLoader.loadFilter("FASTAtaxonomy", "Homo sapiens", spLoader);
            fail("No IOException thrown when testing the loadFilter method with the wrong filter for the database!");
        } catch(IOException ioe) {
            // OK.
        }

        try {
            FilterLoader.loadFilter("NotAVeryCommonNameForAFilterFilter", null, spLoader);
            fail("No IOException thrown when testing the loadFilter method with an inexistant filter for the database!");
        } catch(IOException ioe) {
            // OK.
        }
    }
}
