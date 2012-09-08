/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-sep-02
 * Time: 18:14:59
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.DBLoaderFactory;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.FilterCollection;
import com.compomics.dbtoolkit.io.implementations.SwissProtKeywordFilter;
import com.compomics.dbtoolkit.io.implementations.SwissProtTaxonomyFilter;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.*;

import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the FilterCollection class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.FilterCollection
 */
public class TestFilterCollection extends TestCase {

    public TestFilterCollection() {
        this("Test for the FilterCollection class.");
    }

    public TestFilterCollection(String aName) {
        super(aName);
    }

    /**
     * This method tests the filtering behaviour of the class.
     */
    public void testFilter() {
        try {
            final String input = "test.spr";

            String inputFile = TestCaseLM.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            // Test case insensitivity at the same time.
            FilterCollection filter = new FilterCollection(FilterCollection.AND);
            filter.add(new SwissProtKeywordFilter("MiToSiS"));
            filter.add(new SwissProtTaxonomyFilter("HumAn"));

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Second one is human.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Third one is human.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fourth one is C. elegans.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fifth one is chicken.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;

            // Second pass.
            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new FilterCollection(FilterCollection.OR);
            filter.add(new SwissProtKeywordFilter("MiToSiS"));
            filter.add(new SwissProtTaxonomyFilter("HumAn"));

            db.load(inputFile);
            entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Second one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Third one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fourth one is C. elegans.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fifth one is chicken.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;
        } catch(UnknownDBFormatException udfe) {
            fail("Database 'SwissProt' reported as being unknown: " + udfe.getMessage());
        } catch(IOException ioe) {
            fail("IOException was thrown while testing for filtering with SwissProtKeywordFilter: " + ioe.getMessage());
        }
    }

    /**
     * This method specifically tests the 'NOT' operator for both AND and OR Collections.
     */
    public void testInversionOfCollection() {
        try {
            final String input = "test.spr";

            String inputFile = TestCaseLM.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            // Test case insensitivity at the same time.
            FilterCollection filter = new FilterCollection(FilterCollection.AND, true);
            filter.add(new SwissProtKeywordFilter("MiToSiS"));
            filter.add(new SwissProtTaxonomyFilter("HumAn"));

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Second one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Third one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fourth one is C. elegans.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fifth one is chicken.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));
            db = null;

            // Second pass.
            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new FilterCollection(FilterCollection.OR, true);
            filter.add(new SwissProtKeywordFilter("MiToSiS"));
            filter.add(new SwissProtTaxonomyFilter("HumAn"));

            db.load(inputFile);
            entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Second one is human.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Third one is human.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fourth one is C. elegans.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fifth one is chicken.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));
            db = null;

            // Third pass.
            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new FilterCollection(FilterCollection.OR, false);
            filter.add(new SwissProtKeywordFilter("MiToSiS"));
            filter.add(new SwissProtTaxonomyFilter("HumAn"));

            db.load(inputFile);
            entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Second one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Third one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fourth one is C. elegans.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fifth one is chicken.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;
        } catch(UnknownDBFormatException udfe) {
            fail("Database 'SwissProt' reported as being unknown: " + udfe.getMessage());
        } catch(IOException ioe) {
            fail("IOException was thrown while testing for filtering with SwissProtKeywordFilter: " + ioe.getMessage());
        }
    }
}
