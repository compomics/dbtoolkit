/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-jan-03
 * Time: 17:11:05
 */
package com.compomics.dbtoolkit.test.general;

import com.compomics.dbtoolkit.general.NoEnzymeSimulator;
import com.compomics.util.junit.TestCaseLM;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;
import junit.framework.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the NoEnzymeSimulator class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.general.NoEnzymeSimulator
 */
public class TestNoEnzymeSimulator extends TestCase {

    public TestNoEnzymeSimulator() {
        this("Test scenario for the NoEnzymeSimulator class.");
    }

    public TestNoEnzymeSimulator(String aName) {
        super(aName);
    }

    /**
     * This method tests the constructor.
     * It will throw a NullPointerException when confronted with 'null'.
     */
    public void testConstructor() {
        try {
            NoEnzymeSimulator nes = new NoEnzymeSimulator(null);
            fail("No NullPointerException thrown when confronting NoEnzymeSimulator with a 'null' center String.");
        } catch(NullPointerException npe) {
            // Okay, all's well.
        }

        NoEnzymeSimulator nes = new NoEnzymeSimulator("M");
        Assert.assertEquals("M", nes.getCenter());

        nes = new NoEnzymeSimulator("m");
        Assert.assertEquals("M", nes.getCenter());
    }

    /**
     * Tests the NoEnzyme simulation.
     */
    public void testNoEnzymeSimulation() {
        try {
            final BufferedReader br = new BufferedReader(new FileReader(TestCaseLM.getFullFilePath("testNoEnzymeSimulator.fas")));
            Protein p = new Protein(">Test Protein.\nKLMIR");
            NoEnzymeSimulator nes = new NoEnzymeSimulator("M");
            Protein[] result = nes.performRagging(p);
            Arrays.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int compared = 0;
                    compared = ((Protein)o1).getHeader().getStartLocation() - ((Protein)o2).getHeader().getStartLocation();
                    if(compared == 0) {
                        compared = ((Protein)o1).getHeader().getEndLocation() - ((Protein)o2).getHeader().getEndLocation();
                    }
                    return compared;
                }
            });

            // Checking...
            for(int i = 0; i < result.length; i++) {
                String header = br.readLine();
                String sequence = br.readLine();
                if(header == null || sequence == null) {
                    fail("End of file reached prematurely while checking No Enzyme ragging results!");
                }
                Assert.assertEquals(header, result[i].getHeader().getFullHeaderWithAddenda());
                Assert.assertEquals(sequence, result[i].getSequence().getSequence());
            }

            p = new Protein(">Test Protein 2.\nGHKLMIRTVFH");
            result = nes.performRagging(p);

            Arrays.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int compared = 0;
                    compared = ((Protein)o1).getHeader().getStartLocation() - ((Protein)o2).getHeader().getStartLocation();
                    if(compared == 0) {
                        compared = ((Protein)o1).getHeader().getEndLocation() - ((Protein)o2).getHeader().getEndLocation();
                    }
                    return compared;
                }
            });
            // Checking...
            for(int i = 0; i < result.length; i++) {
                String header = br.readLine();
                String sequence = br.readLine();
                if(header == null || sequence == null) {
                    fail("End of file reached prematurely while checking No Enzyme ragging results!");
                }
                Assert.assertEquals(header, result[i].getHeader().getFullHeaderWithAddenda());
                Assert.assertEquals(sequence, result[i].getSequence().getSequence());
            }

            // Try with mass limits.
            result = nes.performRagging(new Protein(">Test Protein 3.\nGHKLMIRTVFH"), 200, 300);
            Arrays.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int compared = 0;
                    compared = ((Protein)o1).getHeader().getStartLocation() - ((Protein)o2).getHeader().getStartLocation();
                    if(compared == 0) {
                        compared = ((Protein)o1).getHeader().getEndLocation() - ((Protein)o2).getHeader().getEndLocation();
                    }
                    return compared;
                }
            });

            for(int i = 0; i < result.length; i++) {
                String header = br.readLine();
                String sequence = br.readLine();
                if(header == null || sequence == null) {
                    fail("End of file reached prematurely while checking No Enzyme ragging results!");
                }
                Assert.assertEquals(header, result[i].getHeader().getFullHeaderWithAddenda());
                Assert.assertEquals(sequence, result[i].getSequence().getSequence());
            }

            // Try with enzyme tagging.
            result = nes.performRagging(new Protein("Test protein 4\nLENNARTMARTENS"), new Enzyme("TestEnzyme", "KR", "P", "Cterm", 1));
            Arrays.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int compared = 0;
                    compared = ((Protein)o1).getHeader().getStartLocation() - ((Protein)o2).getHeader().getStartLocation();
                    if(compared == 0) {
                        compared = ((Protein)o1).getHeader().getEndLocation() - ((Protein)o2).getHeader().getEndLocation();
                    }
                    return compared;
                }
            });

            for(int i = 0; i < result.length; i++) {
                String header = br.readLine();
                String sequence = br.readLine();
                if(header == null || sequence == null) {
                    fail("End of file reached prematurely while checking No Enzyme ragging results!");
                }
                Assert.assertEquals(header, result[i].getHeader().getFullHeaderWithAddenda());
                Assert.assertEquals(sequence, result[i].getSequence().getSequence());
            }

            // Try with enzyme and mass limits.
            result = nes.performRagging(new Protein("Test protein 5\nLENNARTMARTENS"), new Enzyme("TestEnzyme", "KR", "P", "Cterm", 1), 200, 400);
            Arrays.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int compared = 0;
                    compared = ((Protein)o1).getHeader().getStartLocation() - ((Protein)o2).getHeader().getStartLocation();
                    if(compared == 0) {
                        compared = ((Protein)o1).getHeader().getEndLocation() - ((Protein)o2).getHeader().getEndLocation();
                    }
                    return compared;
                }
            });

            for(int i = 0; i < result.length; i++) {
                String header = br.readLine();
                String sequence = br.readLine();
                if(header == null || sequence == null) {
                    fail("End of file reached prematurely while checking No Enzyme ragging results!");
                }
                Assert.assertEquals(header, result[i].getHeader().getFullHeaderWithAddenda());
                Assert.assertEquals(sequence, result[i].getSequence().getSequence());
            }

            // See if the last of the lines has in fact been read in the file.
            Assert.assertTrue(br.readLine() == null);
            br.close();
        } catch(IOException ioe) {
            fail("IOException occurred while testing the NoEnzymeSimulator: " + ioe.getMessage() + ".");
        }
    }

    /**
     * This method tests what happens when a center is not found.
     */
    public void testNoCenterFound() {
        NoEnzymeSimulator nes = new NoEnzymeSimulator("M");
        Protein[] result = nes.performRagging(new Protein(">Test 1.\nLENNART"));
        Assert.assertTrue(result != null);
        Assert.assertEquals(0, result.length);
        result = nes.performRagging(new Protein(">Test 2.\nLENNART"), new Enzyme("Test Enzyme", "KR", "P", "Cterm", 1));
        Assert.assertTrue(result != null);
        Assert.assertEquals(0, result.length);
        result = nes.performRagging(new Protein(">Test 1.\nLENNART"), new Enzyme("Test Enzyme", "KR", "P", "Cterm", 1), 1.0, 4500.0);
        Assert.assertTrue(result != null);
        Assert.assertEquals(0, result.length);
        result = nes.performRagging(new Protein(">Test 1.\nLENNART"), 1.0, 4500.0);
        Assert.assertTrue(result != null);
        Assert.assertEquals(0, result.length);
    }
}
