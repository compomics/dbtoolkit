package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.DBLoaderFactory;
import com.compomics.dbtoolkit.io.implementations.SwissProtProteinSequenceLengthFilter;
import com.compomics.dbtoolkit.io.implementations.SwissProtTaxonomyFilter;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class implements the test scenario for the FASTAProteinSequenceLengthFilter class.
 *
 * Created with IntelliJ IDEA.
 * User: Lennart
 * Date: 03/09/13
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */
public class TestSwissProtProteinSequenceLengthFilter extends TestCase {

    public TestSwissProtProteinSequenceLengthFilter() {
        this("The test scenario for the FASTAProteinSequenceLengthFilter.");
    }

    public TestSwissProtProteinSequenceLengthFilter(String aName) {
        super(aName);
    }

    /**
     * This method tests the filter.
     */
    public void testFilter() {
        try {
            final String input = "test.spr";


            String inputFile = TestCaseLM.getFullFilePath(input);
            DBLoader db = DBLoaderFactory.getDBLoader(DBLoader.SWISSPROT);
            Filter pf1 = new SwissProtProteinSequenceLengthFilter("300");
            Filter pf2 = new SwissProtProteinSequenceLengthFilter("<300");
            Filter pf3 = new SwissProtProteinSequenceLengthFilter("@300");
            Filter pf4 = new SwissProtProteinSequenceLengthFilter("    300    ");


            db.load(inputFile);
            String entry = null;

            entry = db.nextRawEntry();
            Assert.assertFalse(pf1.passesFilter(entry));
            Assert.assertTrue(pf2.passesFilter(entry));
            Assert.assertFalse(pf3.passesFilter(entry));
            Assert.assertFalse(pf4.passesFilter(entry));

            entry = db.nextRawEntry();
            Assert.assertTrue(pf1.passesFilter(entry));
            Assert.assertFalse(pf2.passesFilter(entry));
            Assert.assertTrue(pf3.passesFilter(entry));
            Assert.assertTrue(pf4.passesFilter(entry));

            entry = db.nextRawEntry();
            Assert.assertTrue(pf1.passesFilter(entry));
            Assert.assertFalse(pf2.passesFilter(entry));
            Assert.assertTrue(pf3.passesFilter(entry));
            Assert.assertTrue(pf4.passesFilter(entry));

            entry = db.nextRawEntry();
            Assert.assertTrue(pf1.passesFilter(entry));
            Assert.assertFalse(pf2.passesFilter(entry));
            Assert.assertTrue(pf3.passesFilter(entry));
            Assert.assertTrue(pf4.passesFilter(entry));

            entry = db.nextRawEntry();
            Assert.assertTrue(pf1.passesFilter(entry));
            Assert.assertFalse(pf2.passesFilter(entry));
            Assert.assertTrue(pf3.passesFilter(entry));
            Assert.assertTrue(pf4.passesFilter(entry));

            entry = db.nextRawEntry();
            Assert.assertTrue(pf1.passesFilter(entry));
            Assert.assertFalse(pf2.passesFilter(entry));
            Assert.assertTrue(pf3.passesFilter(entry));
            Assert.assertTrue(pf4.passesFilter(entry));

            entry = db.nextRawEntry();
            Assert.assertTrue(pf1.passesFilter(entry));
            Assert.assertFalse(pf2.passesFilter(entry));
            Assert.assertTrue(pf3.passesFilter(entry));
            Assert.assertTrue(pf4.passesFilter(entry));
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
}