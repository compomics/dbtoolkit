/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 20-jan-03
 * Time: 15:30:32
 */
package com.compomics.dbtoolkit.test.general;

import com.compomics.dbtoolkit.general.PeptideSequenceRegionRetriever;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.SequenceRegion;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.*;

import java.io.IOException;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the PeptideSequenceRegionRetriever class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.general.PeptideSequenceRegionRetriever
 */
public class TestPeptideSequenceRegionRetriever extends TestCase {

    public TestPeptideSequenceRegionRetriever() {
        this("This class implements the tests for the PeptideSequenceRegionRetriever class.");
    }

    public TestPeptideSequenceRegionRetriever(String aName) {
        super(aName);
    }

    /**
     * This method tests batch retrieval of regions.
     */
    public void testBatchRetrieval() {
        // First get a database to load.
        String db = TestCaseLM.getFullFilePath("testFASTA.fas");

        // Create a new Retriever.
        try {
            PeptideSequenceRegionRetriever psr = new PeptideSequenceRegionRetriever(db);

            // Create a Vector of query sequences.
            // First one is symmetric and should be found.
            // Second is asymmetric and should also be found.
            // Third is symmetric and found but not the total number of requested residues.
            // Fourth is like third but asymmetric.
            // Fifth returns nothing on N-terminal side (symmetric)
            // Sixth returns nothing on C-terminal side (symmetric).
            Vector queries = new Vector(6, 2);
            queries.add(new SequenceRegion("O54692", "TLLNTAIAEMM", 8));
            queries.add(new SequenceRegion("P98168", "GPRGLLG", 3, 5));
            queries.add(new SequenceRegion("P21541", "DHNAALTT", 10));
            queries.add(new SequenceRegion("Q15942", "VLCRKCHTARA", 8, 10));
            queries.add(new SequenceRegion("Q04584", "MASPGTPGTRMTTT", 5));
            queries.add(new SequenceRegion("P98168", "VTGSSFLV", 5));

            queries = psr.retrieveSequenceRegions(queries);

            // Check fist one.
            SequenceRegion sr = (SequenceRegion)queries.get(0);
            Assert.assertEquals("O54692", sr.getAccession());
            Assert.assertEquals("TLLNTAIAEMM", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(8, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("NIYCKAMG", sr.getNterminalAddition());
            Assert.assertEquals("SRITALED", sr.getCterminalAddition());
            Assert.assertEquals("NIYCKAMGTLLNTAIAEMMSRITALED", sr.getRetrievedSequence());

            // Check second one.
            sr = (SequenceRegion)queries.get(1);
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("GPRGLLG", sr.getQuerySequence());
            Assert.assertEquals(3, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("AAL", sr.getNterminalAddition());
            Assert.assertEquals("SGPGV", sr.getCterminalAddition());
            Assert.assertEquals("AALGPRGLLGSGPGV", sr.getRetrievedSequence());

            // Check third one.
            sr = (SequenceRegion)queries.get(2);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("DHNAALTT", sr.getQuerySequence());
            Assert.assertEquals(10, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("M", sr.getNterminalAddition());
            Assert.assertEquals("CSTSAIPRLA", sr.getCterminalAddition());
            Assert.assertEquals("MDHNAALTTCSTSAIPRLA", sr.getRetrievedSequence());

            // Check fourth one.
            sr = (SequenceRegion)queries.get(3);
            Assert.assertEquals("Q15942", sr.getAccession());
            Assert.assertEquals("VLCRKCHTARA", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("GCFPLDGH", sr.getNterminalAddition());
            Assert.assertEquals("QT", sr.getCterminalAddition());
            Assert.assertEquals("GCFPLDGHVLCRKCHTARAQT", sr.getRetrievedSequence());

            // Check fifth one.
            sr = (SequenceRegion)queries.get(4);
            Assert.assertEquals("Q04584", sr.getAccession());
            Assert.assertEquals("MASPGTPGTRMTTT", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("", sr.getNterminalAddition());
            Assert.assertEquals("VSINI", sr.getCterminalAddition());
            Assert.assertEquals("MASPGTPGTRMTTTVSINI", sr.getRetrievedSequence());

            // Check sixth one.
            sr = (SequenceRegion)queries.get(5);
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("VTGSSFLV", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("RNLIT", sr.getNterminalAddition());
            Assert.assertEquals("", sr.getCterminalAddition());
            Assert.assertEquals("RNLITVTGSSFLV", sr.getRetrievedSequence());

        } catch(IOException ioe) {
            fail("IOException thrown when attempting to load a DB for PeptideSequenceRegionRetriever testing: " + ioe.getMessage() + "!");
        } catch(UnknownDBFormatException udfe) {
            fail("UnknownDBFormatException thrown when attempting to read FASTA formatted DB for PeptideSequenceRegionRetriever testing: " + udfe.getMessage() + "!");
        }
    }

    /**
     * Test the occurrence of many queries based on the same accession.
     */
    public void testMultipleOccurrence() {
        // First get a database to load.
        final String db = TestCaseLM.getFullFilePath("testFASTA.fas");

        // Create a new Retriever.
        try {
            PeptideSequenceRegionRetriever psr = new PeptideSequenceRegionRetriever(db);

            // Create a Vector of query sequences.
            // First one is symmetric and should be found.
            // Second is asymmetric and should also be found.
            // Third is symmetric and found but not the total number of requested residues.
            // Fourth is like third but asymmetric.
            // Fifth returns nothing on N-terminal side (symmetric)
            // Sixth returns nothing on C-terminal side (symmetric).
            Vector queries = new Vector(4, 2);
            queries.add(new SequenceRegion("P21541", "TTCST", 3, 5));
            queries.add(new SequenceRegion("P21541", "TCST", 10));
            queries.add(new SequenceRegion("P21541", "PKLEQISLLAT", 0, 1));
            queries.add(new SequenceRegion("P21541", "NENALYTLKFT", 0));
            queries.add(new SequenceRegion("P21541", "GAIMWCLWGVH", 1));

            queries = psr.retrieveSequenceRegions(queries);

            // Check fist one.
            SequenceRegion sr = (SequenceRegion)queries.get(0);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("TTCST", sr.getQuerySequence());
            Assert.assertEquals(3, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("AAL", sr.getNterminalAddition());
            Assert.assertEquals("SAIPR", sr.getCterminalAddition());
            Assert.assertEquals("AALTTCSTSAIPR", sr.getRetrievedSequence());

            // Check second one.
            sr = (SequenceRegion)queries.get(1);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("TCST", sr.getQuerySequence());
            Assert.assertEquals(10, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("MDHNAALT", sr.getNterminalAddition());
            Assert.assertEquals("SAIPRLARLA", sr.getCterminalAddition());
            Assert.assertEquals("MDHNAALTTCSTSAIPRLARLA", sr.getRetrievedSequence());

            // Check third one.
            sr = (SequenceRegion)queries.get(2);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("PKLEQISLLAT", sr.getQuerySequence());
            Assert.assertEquals(0, sr.getNterminalResidueCount());
            Assert.assertEquals(1, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("", sr.getNterminalAddition());
            Assert.assertEquals("P", sr.getCterminalAddition());
            Assert.assertEquals("PKLEQISLLATP", sr.getRetrievedSequence());

            // Check fourth one.
            sr = (SequenceRegion)queries.get(3);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("NENALYTLKFT", sr.getQuerySequence());
            Assert.assertEquals(0, sr.getNterminalResidueCount());
            Assert.assertEquals(0, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("", sr.getNterminalAddition());
            Assert.assertEquals("", sr.getCterminalAddition());
            Assert.assertEquals("NENALYTLKFT", sr.getRetrievedSequence());

            // Check fifth one.
            sr = (SequenceRegion)queries.get(4);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("GAIMWCLWGVH", sr.getQuerySequence());
            Assert.assertEquals(1, sr.getNterminalResidueCount());
            Assert.assertEquals(1, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("N", sr.getNterminalAddition());
            Assert.assertEquals("H", sr.getCterminalAddition());
            Assert.assertEquals("NGAIMWCLWGVHH", sr.getRetrievedSequence());
        } catch(IOException ioe) {
            fail("IOException thrown when attempting to load a DB for PeptideSequenceRegionRetriever testing: " + ioe.getMessage() + "!");
        } catch(UnknownDBFormatException udfe) {
            fail("UnknownDBFormatException thrown when attempting to read FASTA formatted DB for PeptideSequenceRegionRetriever testing: " + udfe.getMessage() + "!");
        }
    }

    /**
     * This method tests the presence of a mixed Vector.
     */
    public void testMixedVector() {
        // First get a database to load.
        String db = TestCaseLM.getFullFilePath("testFASTA.fas");

        // Create a new Retriever.
        try {
            PeptideSequenceRegionRetriever psr = new PeptideSequenceRegionRetriever(db);

            // Create a Vector of query sequences.
            // First one is symmetric and should be found.
            // Second is asymmetric and should also be found.
            // Third is symmetric and found but not the total number of requested residues.
            // Fourth is like third but asymmetric.
            // Fifth returns nothing on N-terminal side (symmetric)
            // Sixth returns nothing on C-terminal side (symmetric).
            Vector queries = new Vector(9, 2);
            queries.add(new SequenceRegion("O54692", "TLLNTAIAEMM", 8));
            queries.add(new Integer(3));
            queries.add(new SequenceRegion("P98168", "GPRGLLG", 3, 5));
            queries.add(new SequenceRegion("P21541", "DHNAALTT", 10));
            queries.add(this);
            queries.add(new SequenceRegion("Q15942", "VLCRKCHTARA", 8, 10));
            queries.add(new SequenceRegion("Q04584", "MASPGTPGTRMTTT", 5));
            queries.add(new IOException("Something."));
            queries.add(new SequenceRegion("P98168", "VTGSSFLV", 5));

            queries = psr.retrieveSequenceRegions(queries);

            // Check fist one.
            SequenceRegion sr = (SequenceRegion)queries.get(0);
            Assert.assertEquals("O54692", sr.getAccession());
            Assert.assertEquals("TLLNTAIAEMM", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(8, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("NIYCKAMG", sr.getNterminalAddition());
            Assert.assertEquals("SRITALED", sr.getCterminalAddition());
            Assert.assertEquals("NIYCKAMGTLLNTAIAEMMSRITALED", sr.getRetrievedSequence());

            // Check second one.
            sr = (SequenceRegion)queries.get(2);
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("GPRGLLG", sr.getQuerySequence());
            Assert.assertEquals(3, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("AAL", sr.getNterminalAddition());
            Assert.assertEquals("SGPGV", sr.getCterminalAddition());
            Assert.assertEquals("AALGPRGLLGSGPGV", sr.getRetrievedSequence());

            // Check third one.
            sr = (SequenceRegion)queries.get(3);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("DHNAALTT", sr.getQuerySequence());
            Assert.assertEquals(10, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("M", sr.getNterminalAddition());
            Assert.assertEquals("CSTSAIPRLA", sr.getCterminalAddition());
            Assert.assertEquals("MDHNAALTTCSTSAIPRLA", sr.getRetrievedSequence());

            // Check fourth one.
            sr = (SequenceRegion)queries.get(5);
            Assert.assertEquals("Q15942", sr.getAccession());
            Assert.assertEquals("VLCRKCHTARA", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("GCFPLDGH", sr.getNterminalAddition());
            Assert.assertEquals("QT", sr.getCterminalAddition());
            Assert.assertEquals("GCFPLDGHVLCRKCHTARAQT", sr.getRetrievedSequence());

            // Check fifth one.
            sr = (SequenceRegion)queries.get(6);
            Assert.assertEquals("Q04584", sr.getAccession());
            Assert.assertEquals("MASPGTPGTRMTTT", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("", sr.getNterminalAddition());
            Assert.assertEquals("VSINI", sr.getCterminalAddition());
            Assert.assertEquals("MASPGTPGTRMTTTVSINI", sr.getRetrievedSequence());

            // Check sixth one.
            sr = (SequenceRegion)queries.get(8);
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("VTGSSFLV", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("RNLIT", sr.getNterminalAddition());
            Assert.assertEquals("", sr.getCterminalAddition());
            Assert.assertEquals("RNLITVTGSSFLV", sr.getRetrievedSequence());

        } catch(IOException ioe) {
            fail("IOException thrown when attempting to load a DB for PeptideSequenceRegionRetriever testing: " + ioe.getMessage() + "!");
        } catch(UnknownDBFormatException udfe) {
            fail("UnknownDBFormatException thrown when attempting to read FASTA formatted DB for PeptideSequenceRegionRetriever testing: " + udfe.getMessage() + "!");
        }
    }

    /**
     * This method tests those entries that are not found.
     */
    public void testNotFound() {
        // First get a database to load.
        String db = TestCaseLM.getFullFilePath("testFASTA.fas");

        // Create a new Retriever.
        try {
            PeptideSequenceRegionRetriever psr = new PeptideSequenceRegionRetriever(db);

            Vector queries = new Vector(6, 2);
            queries.add(new SequenceRegion("XxX_Not_Found", "TLLNTAIAEMM", 8));
            queries.add(new SequenceRegion("P98168", "LENNARTMARTENS", 3, 5));
            queries.add(new SequenceRegion("P21541", "DHNAALTT", 10));
            queries.add(new SequenceRegion("Q15942", "VLCRKCHTARA", 8, 10));
            queries.add(new SequenceRegion("Q04584", "MASPGTPGTRMTTT", 5));
            queries.add(new SequenceRegion("P98168", "VTGSSFLV", 5));

            queries = psr.retrieveSequenceRegions(queries);

            // Check fist one.
            SequenceRegion sr = (SequenceRegion)queries.get(0);
            Assert.assertEquals("XxX_Not_Found", sr.getAccession());
            Assert.assertEquals("TLLNTAIAEMM", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(8, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertFalse(sr.isFound());
            Assert.assertTrue(null == sr.getNterminalAddition());
            Assert.assertTrue(null == sr.getCterminalAddition());
            Assert.assertTrue(null == sr.getRetrievedSequence());

            // Check second one.
            sr = (SequenceRegion)queries.get(1);
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("LENNARTMARTENS", sr.getQuerySequence());
            Assert.assertEquals(3, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertFalse(sr.isFound());
            Assert.assertTrue(null == sr.getNterminalAddition());
            Assert.assertTrue(null == sr.getCterminalAddition());
            Assert.assertTrue(null == sr.getRetrievedSequence());

            // Check third one.
            sr = (SequenceRegion)queries.get(2);
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("DHNAALTT", sr.getQuerySequence());
            Assert.assertEquals(10, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("M", sr.getNterminalAddition());
            Assert.assertEquals("CSTSAIPRLA", sr.getCterminalAddition());
            Assert.assertEquals("MDHNAALTTCSTSAIPRLA", sr.getRetrievedSequence());

            // Check fourth one.
            sr = (SequenceRegion)queries.get(3);
            Assert.assertEquals("Q15942", sr.getAccession());
            Assert.assertEquals("VLCRKCHTARA", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("GCFPLDGH", sr.getNterminalAddition());
            Assert.assertEquals("QT", sr.getCterminalAddition());
            Assert.assertEquals("GCFPLDGHVLCRKCHTARAQT", sr.getRetrievedSequence());

            // Check fifth one.
            sr = (SequenceRegion)queries.get(4);
            Assert.assertEquals("Q04584", sr.getAccession());
            Assert.assertEquals("MASPGTPGTRMTTT", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("", sr.getNterminalAddition());
            Assert.assertEquals("VSINI", sr.getCterminalAddition());
            Assert.assertEquals("MASPGTPGTRMTTTVSINI", sr.getRetrievedSequence());

            // Check sixth one.
            sr = (SequenceRegion)queries.get(5);
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("VTGSSFLV", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("RNLIT", sr.getNterminalAddition());
            Assert.assertEquals("", sr.getCterminalAddition());
            Assert.assertEquals("RNLITVTGSSFLV", sr.getRetrievedSequence());

        } catch(IOException ioe) {
            fail("IOException thrown when attempting to load a DB for PeptideSequenceRegionRetriever testing: " + ioe.getMessage() + "!");
        } catch(UnknownDBFormatException udfe) {
            fail("UnknownDBFormatException thrown when attempting to read FASTA formatted DB for PeptideSequenceRegionRetriever testing: " + udfe.getMessage() + "!");
        }
    }

    /**
     * This method tests the throwing of the correct exceptions
     * at construction (IO and DB format unknown).
     */
    public void testExceptions() {
        // IOException.
        try {
            PeptideSequenceRegionRetriever psr = new PeptideSequenceRegionRetriever("FileDoesNot_Exist_Anywehere.dummy");
            fail("No IOException thrown when confronting the PeptideSequenceRegionRetriever constructor with non-existant file!");
        } catch(IOException ioe) {
            // Okelidokeli.
        } catch(UnknownDBFormatException udfe) {
            fail("UnknownDBFormatException thrown when attempting to read FASTA formatted DB for PeptideSequenceRegionRetriever testing: " + udfe.getMessage() + "!");
        }

        // UnknownDBFormatException.
        try {
            PeptideSequenceRegionRetriever psr = new PeptideSequenceRegionRetriever(TestCaseLM.getFullFilePath("filters.properties"));
            fail("No UnknownDBFormatException thrown when confronting the PeptideSequenceRegionRetriever constructor with a properties file!");
        } catch(IOException ioe) {
            fail("IOException thrown when attempting to load a DB for PeptideSequenceRegionRetriever testing: " + ioe.getMessage() + "!");
        } catch(UnknownDBFormatException udfe) {
            // Okelidokeli.
        }
    }

    /**
     * This method test a single retrieve.
     */
    public void testSingleRetrieve() {

        // First get a database to load.
        String db = TestCaseLM.getFullFilePath("testFASTA.fas");

        // Create a new Retriever.
        try {
            PeptideSequenceRegionRetriever psr = new PeptideSequenceRegionRetriever(db);

            // Check fist one.
            SequenceRegion sr = psr.retrieveSequenceRegion(new SequenceRegion("XxX_Not_Found", "TLLNTAIAEMM", 8));
            Assert.assertEquals("XxX_Not_Found", sr.getAccession());
            Assert.assertEquals("TLLNTAIAEMM", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(8, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertFalse(sr.isFound());
            Assert.assertTrue(null == sr.getNterminalAddition());
            Assert.assertTrue(null == sr.getCterminalAddition());
            Assert.assertTrue(null == sr.getRetrievedSequence());

            // Check second one.
            sr = psr.retrieveSequenceRegion(new SequenceRegion("P98168", "LENNARTMARTENS", 3, 5));
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("LENNARTMARTENS", sr.getQuerySequence());
            Assert.assertEquals(3, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertFalse(sr.isFound());
            Assert.assertTrue(null == sr.getNterminalAddition());
            Assert.assertTrue(null == sr.getCterminalAddition());
            Assert.assertTrue(null == sr.getRetrievedSequence());

            // Check third one.
            sr = psr.retrieveSequenceRegion(new SequenceRegion("P21541", "DHNAALTT", 10));
            Assert.assertEquals("P21541", sr.getAccession());
            Assert.assertEquals("DHNAALTT", sr.getQuerySequence());
            Assert.assertEquals(10, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("M", sr.getNterminalAddition());
            Assert.assertEquals("CSTSAIPRLA", sr.getCterminalAddition());
            Assert.assertEquals("MDHNAALTTCSTSAIPRLA", sr.getRetrievedSequence());

            // Check fourth one.
            sr = psr.retrieveSequenceRegion(new SequenceRegion("Q15942", "VLCRKCHTARA", 8, 10));
            Assert.assertEquals("Q15942", sr.getAccession());
            Assert.assertEquals("VLCRKCHTARA", sr.getQuerySequence());
            Assert.assertEquals(8, sr.getNterminalResidueCount());
            Assert.assertEquals(10, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("GCFPLDGH", sr.getNterminalAddition());
            Assert.assertEquals("QT", sr.getCterminalAddition());
            Assert.assertEquals("GCFPLDGHVLCRKCHTARAQT", sr.getRetrievedSequence());

            // Check fifth one.
            sr = psr.retrieveSequenceRegion(new SequenceRegion("Q04584", "MASPGTPGTRMTTT", 5)) ;
            Assert.assertEquals("Q04584", sr.getAccession());
            Assert.assertEquals("MASPGTPGTRMTTT", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("", sr.getNterminalAddition());
            Assert.assertEquals("VSINI", sr.getCterminalAddition());
            Assert.assertEquals("MASPGTPGTRMTTTVSINI", sr.getRetrievedSequence());

            // Check sixth one.
            sr = psr.retrieveSequenceRegion(new SequenceRegion("P98168", "VTGSSFLV", 5));
            Assert.assertEquals("P98168", sr.getAccession());
            Assert.assertEquals("VTGSSFLV", sr.getQuerySequence());
            Assert.assertEquals(5, sr.getNterminalResidueCount());
            Assert.assertEquals(5, sr.getCterminalResidueCount());
            Assert.assertTrue(sr.isQueried());
            Assert.assertTrue(sr.isFound());
            Assert.assertEquals("RNLIT", sr.getNterminalAddition());
            Assert.assertEquals("", sr.getCterminalAddition());
            Assert.assertEquals("RNLITVTGSSFLV", sr.getRetrievedSequence());

        } catch(IOException ioe) {
            fail("IOException thrown when attempting to load a DB for PeptideSequenceRegionRetriever testing: " + ioe.getMessage() + "!");
        } catch(UnknownDBFormatException udfe) {
            fail("UnknownDBFormatException thrown when attempting to read FASTA formatted DB for PeptideSequenceRegionRetriever testing: " + udfe.getMessage() + "!");
        }
    }
}
