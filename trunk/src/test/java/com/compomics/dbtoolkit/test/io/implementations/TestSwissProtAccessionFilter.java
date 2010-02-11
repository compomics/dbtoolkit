package com.compomics.dbtoolkit.test.io.implementations;

import junit.TestCaseLM;
import junit.framework.Assert;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.DBLoaderFactory;
import com.compomics.dbtoolkit.io.implementations.SwissProtAccessionFilter;

/**
 * @author Florian Reisinger
 * @version $Id: TestSwissProtAccessionFilter.java,v 1.1 2008/11/18 16:00:55 lennart Exp $
 * @since x.y
 */
public class TestSwissProtAccessionFilter  extends TestCaseLM {

    public TestSwissProtAccessionFilter() {
        this("Test for the SwissProtAccessionFilter class.");
    }

    public TestSwissProtAccessionFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests the filtering behaviour of the class.
     */
    public void testFilter() {
        try {
            final String input = "test.spr";

            String inputFile = super.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);

            List accList = new ArrayList();
            // now add some accession numbers to the list we want to filter against
            accList.add("Q62523");  // primary acc from the last entry
            accList.add("P70461");  // secondary acc from the last entry

            // Test case insensitivity at the same time.
            Filter filter = new SwissProtAccessionFilter(accList);

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Seventh one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));
            db = null;

            // // // // // // // // // // // // // // // // // // // //
            // Second pass.

            accList = new ArrayList();
            // now add some accession numbers to the list we want to filter against
            accList.add("O54692"); // primary acc from the first entry
            accList.add("Q9UJP7"); // secondary acc from the second entry

            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new SwissProtAccessionFilter(accList);
            db.load(inputFile);
            entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Seventh one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;

            // // // // // // // // // // // // // // // // // // // //
            // Third pass (using the alternative single String constructor).
            // use the same accessions as above, but use a single String representation
            String s = "O54692, Q9UJP7";

            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new SwissProtAccessionFilter(s);
            db.load(inputFile);
            entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Seventh one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;

            // // // // // // // // // // // // // // // // // // // //
            // Fourth pass (using the alternative single String constructor).

            // use the same accessions as above, but slight differences in the String
            s = " O54692 ,Q9UJP7 ";

            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            filter = new SwissProtAccessionFilter(s);
            db.load(inputFile);
            entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Seventh one
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
     * This method tests the filtering behaviour of the class when the NOT operator needs to be applied.
     */
    public void testInvertedFilter() {
        try {
            final String input = "test.spr";

            String inputFile = super.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);

            List accList = new ArrayList();
            // now add some accession numbers to the list we want to filter against
            accList.add("Q62523");  // primary acc from the last entry
            accList.add("P70461");  // secondary acc from the last entry

            // Test case insensitivity at the same time.
            Filter filter = new SwissProtAccessionFilter(accList, true);

            db.load(inputFile);
            String entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));
            db = null;



            // // // // // // // // // // // // // // // // // // // //
            // Second pass.
            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);

            accList = new ArrayList();
            // now add some accession numbers to the list we want to filter against
            accList.add("O54692"); // primary acc from the first entry
            accList.add("Q9UJP7"); // secondary acc from the second entry

            filter = new SwissProtAccessionFilter(accList, true);
            db.load(inputFile);
            entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));
            db = null;

            // // // // // // // // // // // // // // // // // // // //
            // Third pass (using the alternative single String constructor).
            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);

            // use the same accessions as above, but use a single String representation
            String s = "O54692, Q9UJP7";

            filter = new SwissProtAccessionFilter(s, true);
            db.load(inputFile);
            entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));
            db = null;

            // // // // // // // // // // // // // // // // // // // //
            // Fourth pass (using the alternative single String constructor).
            db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);

            // use the same accessions as above, but slight differences in the String
            s = " O54692 ,Q9UJP7 ";

            filter = new SwissProtAccessionFilter(s, true);
            db.load(inputFile);
            entry = null;

            // Test them.
            // First one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Second one
            entry = db.nextRawEntry();
            Assert.assertFalse(filter.passesFilter(entry));

            // Third one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fourth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Fifth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Sixth one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));

            // Seventh one
            entry = db.nextRawEntry();
            Assert.assertTrue(filter.passesFilter(entry));
            db = null;
        } catch(UnknownDBFormatException udfe) {
            fail("Database 'SwissProt' reported as being unknown: " + udfe.getMessage());
        } catch(IOException ioe) {
            fail("IOException was thrown while testing for filtering with SwissProtKeywordFilter: " + ioe.getMessage());
        }
    }




}
