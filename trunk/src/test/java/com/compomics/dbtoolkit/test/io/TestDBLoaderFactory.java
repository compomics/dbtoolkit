/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 14:20:54
 */
package com.compomics.dbtoolkit.test.io;

import com.compomics.dbtoolkit.io.DBLoaderFactory;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import junit.TestCaseLM;
import junit.framework.Assert;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class provides the tests for the DBLoaderFactory class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.DBLoaderFactory
 */
public class TestDBLoaderFactory extends TestCaseLM {

    public TestDBLoaderFactory() {
        this("Test for DBLoaderFactory class.");
    }

    public TestDBLoaderFactory(String aName) {
        super(aName);
    }

    /**
     * This methods test the normal (ie. 'not exceptional') behaviour
     * of the factory;
     */
    public void testFactoryBehaviour() {
        try {
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            Assert.assertEquals(DBLoader.SWISSPROT, db.getDBName());

            db = DBLoaderFactory.getDBLoader(DBLoader.FASTA);
            Assert.assertEquals(DBLoader.FASTA, db.getDBName());

        } catch(UnknownDBFormatException udfe) {
            fail("The Factory responded with an unknown DB for name '" + udfe.getDBName() + "'.");
        }
    }

    /**
     * This method tests the behaviour of the DBLoaderFactory when confronted
     * with an incorrect DB ID.
     */
    public void testExceptionByFactory() {
        // First, test with 'null'.
        try {
            DBLoaderFactory.getDBLoader(null);
            fail("The Factory did not throw an Exception when confronted with name='null'.");
        } catch(UnknownDBFormatException udfe) {
            // OK, this is what should happen.
            // Test the correctness of the ID.
            Assert.assertTrue(udfe.getDBName() == null);
        }

        // Next, test with "" (empty String).
        try {
            DBLoaderFactory.getDBLoader("");
            fail("The Factory did not throw an Exception when confronted with name='\"\"'.");
        } catch(UnknownDBFormatException udfe) {
            // OK, this is what should happen.
            // Test the correctness of the ID.
            Assert.assertEquals("", udfe.getDBName());
        }

        // Then take a name, but a wrong one.
        final String wrongDB = "ZwansDB";
        try {
            DBLoaderFactory.getDBLoader(wrongDB);
            fail("The Factory did not throw an Exception when confronted with name='" + wrongDB + "'.");
        } catch(UnknownDBFormatException udfe) {
            // OK, this is what should happen.
            // Test the correctness of the ID.
            Assert.assertEquals(wrongDB, udfe.getDBName());
        }
    }
}
