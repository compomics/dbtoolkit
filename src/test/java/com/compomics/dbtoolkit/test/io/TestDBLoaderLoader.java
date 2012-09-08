/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-aug-2003
 * Time: 16:37:02
 */
package com.compomics.dbtoolkit.test.io;

import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
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
 * This class implements the test scenario for the DBLoaderLoader class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.DBLoaderLoader
 */
public class TestDBLoaderLoader extends TestCase {

    public TestDBLoaderLoader() {
        this("Test scenario for the DBLoaderLoader class.");
    }

    public TestDBLoaderLoader(String aName) {
        super(aName);
    }
    /**
     * This method tests the DB loading.
     */
    public void testLoader() {
        try {
            DBLoader loader = DBLoaderLoader.loadDB(new File(TestCaseLM.getFullFilePath("testFASTA.fas")));
            Assert.assertTrue(loader != null);
        } catch(IOException ioe) {
            fail("IOException thrown when testing the DBLoaderLoader class: " + ioe.getMessage());
        }

        try {
            DBLoader loader = DBLoaderLoader.loadDB(new File(TestCaseLM.getFullFilePath("test.spr")));
            Assert.assertTrue(loader != null);
        } catch(IOException ioe) {
            fail("IOException thrown when testing the DBLoaderLoader class: " + ioe.getMessage());
        }

        try {
            DBLoader loader = DBLoaderLoader.loadDB(new File(TestCaseLM.getFullFilePath("testFASTA.fas")));
            Assert.assertTrue(loader != null);
        } catch(IOException ioe) {
            fail("IOException thrown when testing the DBLoaderLoader class: " + ioe.getMessage());
        }

        try {
            DBLoader loader = DBLoaderLoader.loadDB(new File(TestCaseLM.getFullFilePath("testAutoUnknown.txt")));
            fail("IOException thrown when testing the DBLoaderLoader class with an unknown DB format!");
        } catch(IOException ioe) {
            // OK.
        }
    }
}
