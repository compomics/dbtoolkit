/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 3-dec-2004
 * Time: 11:16:01
 */
package com.compomics.dbtoolkit.test.toolkit;

import junit.TestCaseLM;
import junit.framework.Assert;

import java.io.IOException;
import java.io.File;
import java.util.*;

import com.compomics.dbtoolkit.toolkit.ContainsPeptide;
import com.compomics.util.protein.Protein;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class tests the behaviour of the ContainsPeptide class.
 *
 * @author Lennart Martens
 * @version $Id: TestContainsPeptide.java,v 1.3 2007/07/06 09:52:03 lennart Exp $
 * @see com.compomics.dbtoolkit.toolkit.ContainsPeptide
 */
public class TestContainsPeptide extends TestCaseLM {

    public TestContainsPeptide() {
        this("This class tests the behaviour of the ContainsPeptide class.");
    }

    public TestContainsPeptide(String aName) {
        super(aName);
    }

    /**
     * This method tests the resolving of all isoforms for a particular
     * set of sequences.
     */
    public void testProcessSequences() {
        // First try a nonexistant file.
        try {
            ContainsPeptide.processSequences(new File("IAmSoSureThisFileWillNotExist.dud"), new ArrayList());
            fail("No IOException thrown when attempting to process sequences using a non-existent file!");
        } catch(IOException ioe) {
            // This is good!
        }
        // Now try an empty sequence collection.
        try {
            HashMap results = ContainsPeptide.processSequences(new File(super.getFullFilePath("testFASTA.fas")), new ArrayList());
            Assert.assertEquals(0, results.size());
        } catch(IOException ioe) {
            fail("IOException thrown when processing empty sequence list: " + ioe.getMessage());
        }
        // Now try three sequences: one is present multiple times, one only once, and one not at all.
        try {
            List seqs = new ArrayList();
            seqs.add("GPG");
            seqs.add("PPHAWEPGAAPAQQPRCLIAPQAGFPQAAHPG");
            seqs.add("LENNARTMARTENS");
            HashMap results = ContainsPeptide.processSequences(new File(super.getFullFilePath("testFASTA.fas")), seqs);
            Assert.assertEquals(3, results.size());
            // Now check each.
            // First sequence: GPG
            Collection found = (Collection)results.get("GPG");
            Assert.assertEquals(3, found.size());
            String accession1 = "P98168";
            boolean foundOne = false;
            String accession2 = "P98169";
            boolean foundTwo = false;
            String accession3 = "Q62523";
            boolean foundThree = false;
            for (Iterator lIterator = found.iterator(); lIterator.hasNext();) {
                Protein protein = (Protein)lIterator.next();
                if(protein.getHeader().getAccession().equals(accession1)) {
                    foundOne = true;
                } else if(protein.getHeader().getAccession().equals(accession2)) {
                    foundTwo = true;
                } else if(protein.getHeader().getAccession().equals(accession3)) {
                    foundThree = true;
                } else {
                    fail("Unexpected accession number '" + protein.getHeader().getAccession() + "' reported for sequence 'GPG'!");
                }
            }
            Assert.assertTrue(foundOne);
            Assert.assertTrue(foundTwo);
            Assert.assertTrue(foundThree);
            // Second sequence: PPHAWEPGAAPAQQPRCLIAPQAGFPQAAHPG
            found = (Collection)results.get("PPHAWEPGAAPAQQPRCLIAPQAGFPQAAHPG");
            Assert.assertEquals(1, found.size());
            accession1 = "P98168";
            foundOne = false;
            for (Iterator lIterator = found.iterator(); lIterator.hasNext();) {
                Protein protein = (Protein)lIterator.next();
                if(protein.getHeader().getAccession().equals(accession1)) {
                    foundOne = true;
                } else {
                    fail("Unexpected accession number '" + protein.getHeader().getAccession() + "' reported for sequence 'PPHAWEPGAAPAQQPRCLIAPQAGFPQAAHPG'!");
                }
            }
            Assert.assertTrue(foundOne);
            // Third sequence: LENNARTMARTENS
            found = (Collection)results.get("LENNARTMARTENS");
            Assert.assertEquals(0, found.size());
        } catch(IOException ioe) {
            fail("IOException thrown when processing empty sequence list: " + ioe.getMessage());
        }
    }
}
