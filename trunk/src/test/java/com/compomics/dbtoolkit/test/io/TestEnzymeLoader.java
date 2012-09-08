/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-aug-2003
 * Time: 15:21:25
 */
package com.compomics.dbtoolkit.test.io;

import com.compomics.dbtoolkit.io.EnzymeLoader;
import com.compomics.util.protein.Enzyme;
import junit.framework.*;

import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class test the EnzymeLoader class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.EnzymeLoader
 *
 */
public class TestEnzymeLoader extends TestCase {

    public TestEnzymeLoader() {
        this("test scenario for the EnzymeLoader class.");
    }

    public TestEnzymeLoader(String aName) {
        super(aName);
    }

    /**
     * Test the loading of an enzyme.
     */
    public void testLoader() {
        try{
            Enzyme e = EnzymeLoader.loadEnzyme(null, null);
            Assert.assertTrue(e == null);
        } catch(IOException ioe) {
            fail("IOException thrown when testing loadEnzyme with 'null' for enzyme and missed cleavages: " + ioe.getMessage());
        }

        try{
            Enzyme e = EnzymeLoader.loadEnzyme(null, "something");
            Assert.assertTrue(e == null);
        } catch(IOException ioe) {
            fail("IOException thrown when testing loadEnzyme with 'null' for enzyme and a value for missed cleavages: " + ioe.getMessage());
        }

        try{
            Enzyme e = EnzymeLoader.loadEnzyme("Trypsin", null);
            Assert.assertTrue(e != null);
            Assert.assertEquals(1, e.getMiscleavages());
            Assert.assertEquals("Trypsin", e.getTitle());
        } catch(IOException ioe) {
            fail("IOException thrown when testing loadEnzyme with 'null' for enzyme and a value for missed cleavages: " + ioe.getMessage());
        }

        try{
            Enzyme e = EnzymeLoader.loadEnzyme("Trypsin", "3");
            Assert.assertTrue(e != null);
            Assert.assertEquals(3, e.getMiscleavages());
            Assert.assertEquals("Trypsin", e.getTitle());
            e = EnzymeLoader.loadEnzyme("Trypsin", "0");
            Assert.assertTrue(e != null);
            Assert.assertEquals(0, e.getMiscleavages());
            Assert.assertEquals("Trypsin", e.getTitle());
        } catch(IOException ioe) {
            fail("IOException thrown when testing loadEnzyme with 'null' for enzyme and a value for missed cleavages: " + ioe.getMessage());
        }

        try{
            EnzymeLoader.loadEnzyme("Trypsin", "-1");
            fail("No IOException thrown when loadEnzyme was confronted with '-1' for missed cleavages.");
        } catch(IOException ioe) {
            // This is good.
        }

        try{
            EnzymeLoader.loadEnzyme("Trypsin", "0.4");
            fail("No IOException thrown when loadEnzyme was confronted with '0.4' for missed cleavages.");
        } catch(IOException ioe) {
            // This is good.
        }

        try{
            EnzymeLoader.loadEnzyme("Trypsin", "-1.6");
            fail("No IOException thrown when loadEnzyme was confronted with '-1.6' for missed cleavages.");
        } catch(IOException ioe) {
            // This is good.
        }

        try{
            EnzymeLoader.loadEnzyme("Trypsin", "test");
            fail("No IOException thrown when loadEnzyme was confronted with 'test' for missed cleavages.");
        } catch(IOException ioe) {
            // This is good.
        }

        try{
            EnzymeLoader.loadEnzyme("DoesNotExistAsFarAsIamConcerned", "test");
            fail("No IOException thrown when loadEnzyme was confronted with 'DoesNotExistAsFarAsIamConcerned' for enzyme.");
        } catch(IOException ioe) {
            // This is good.
        }
    }
}
