/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 24-Aug-2006
 * Time: 13:53:14
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.DBLoaderFactory;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.SwissProtNCBITaxonomyFilter;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.*;

import java.io.IOException;
/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/05/04 13:20:36 $
 */

/**
 * This class implements the test scenario for the SwissProtNCBITaxonomyFilter class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.implementations.SwissProtNCBITaxonomyFilter
 */
public class TestSwissProtNCBITaxonomyFilter extends TestCase {

    public TestSwissProtNCBITaxonomyFilter() {
        this("Test for the SwissProtNCBITaxonomyFilter class.");
    }

    public TestSwissProtNCBITaxonomyFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests the filtering behaviour of the class.
     */
    public void testFilter() {
        try {
            final String input = "test2.spr";

            String inputFile = TestCaseLM.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            Filter filter = new SwissProtNCBITaxonomyFilter("9606");

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

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

            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new SwissProtNCBITaxonomyFilter(" 9606 ");

            db.load(inputFile);
            entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

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
            fail("Database SwissProt reported as being unknown: " + udfe.getMessage());
        } catch(IOException ioe) {
            fail("IOException was thrown while testing for filtering with SwissProtNCBITaxonomyFilter: " + ioe.getMessage());
        }
    }

    /**
     * This method tests the filtering behaviour of the class when Boolean 'NOT' is applied..
     */
    public void testInvertedFilter() {
        try {
            final String input = "test2.spr";

            String inputFile = TestCaseLM.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            Filter filter = new SwissProtNCBITaxonomyFilter("9606", false);

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

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

            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new SwissProtNCBITaxonomyFilter(" 9606 ", true);

            db.load(inputFile);
            entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

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
        } catch(UnknownDBFormatException udfe) {
            fail("Database SwissProt reported as being unknown: " + udfe.getMessage());
        } catch(IOException ioe) {
            fail("IOException was thrown while testing for filtering with SwissProtNCBITaxonomyFilter: " + ioe.getMessage());
        }
    }

    /**
     * This method tests the filtering behaviour of the class when more than one
     * TaxID is presented.
     */
    public void testMultiFilter() {
        try {
            final String input = "test2.spr";

            String inputFile = TestCaseLM.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            Filter filter = new SwissProtNCBITaxonomyFilter("9606, 6239 ;");

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

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
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;

            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new SwissProtNCBITaxonomyFilter(";, ; , ; 9606 ;;;, , , ; ,;, ,; ,; 6239");

            db.load(inputFile);
            entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

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
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;
        } catch(UnknownDBFormatException udfe) {
            fail("Database SwissProt reported as being unknown: " + udfe.getMessage());
        } catch(IOException ioe) {
            fail("IOException was thrown while testing for filtering with SwissProtNCBITaxonomyFilter: " + ioe.getMessage());
        }
    }

    /**
     * This method tests the filtering behaviour of the class when Boolean
     * 'NOT' is applied to a multi-filter.
     */
    public void testInvertedMultiFilter() {
        try {
            final String input = "test2.spr";

            String inputFile = TestCaseLM.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            Filter filter = new SwissProtNCBITaxonomyFilter("9606 6239", false);

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

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
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;

            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new SwissProtNCBITaxonomyFilter(" ;, ; , ; 9606 ;;;, , , ; ,;, ,; ,; 6239 ", true);

            db.load(inputFile);
            entry = null;

            // Test them.
            // First one is bird.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

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
            Assert.assertTrue(filter.passesFilter(entry));

            // Sixth one is human.
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Seventh one is mouse.
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));
            db = null;
        } catch(UnknownDBFormatException udfe) {
            fail("Database SwissProt reported as being unknown: " + udfe.getMessage());
        } catch(IOException ioe) {
            fail("IOException was thrown while testing for filtering with SwissProtNCBITaxonomyFilter: " + ioe.getMessage());
        }
    }
}
