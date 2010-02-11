/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 14:48:19
 */
package com.compomics.dbtoolkit.test.io.implementations;

import com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.protein.Protein;
import junit.TestCaseLM;
import junit.framework.Assert;

import java.io.*;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test for the ZippedFASTADBLoader class.
 *
 * @author Lennart
 * @see com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader
 */
public class TestZippedFASTADBLoader extends TestCaseLM {

    public TestZippedFASTADBLoader() {
        this("Test for the ZippedFASTADBLoader class.");
    }

    public TestZippedFASTADBLoader(String aName) {
        super(aName);
    }

    /**
     * This method tests the behaviour of the class when the
     * specified file does not exist.
     */
    public void testFileNotFoundBehaviour() {
        final String wrongFile = "FileThatDoesNotExist";
        try {
            DBLoader sp = new ZippedFASTADBLoader();
            sp.load(wrongFile);
            fail("ZippedFASTADBLoader should have flagged an IOException when confronted with the erronous filename '"+wrongFile+"'.");
        } catch(IOException ioe) {
            // OK, this is EXACTLY what we want.
        }
    }

    /**
     * This method tests the correct reporting on the name for the SwissProtDBLoader.
     */
    public void testGetNameMethod() {
        DBLoader db = new ZippedFASTADBLoader();
        Assert.assertEquals(DBLoader.FASTA, db.getDBName());
    }

    /**
     * This method tests the reading of raw entries from the flatfile db.
     */
    public void testRawReadingBehaviour() {
        // Zipfile.
        try {
            final String inputFile = "testFASTA.zip";
            String input = super.getFullFilePath(inputFile);

            DBLoader db = new ZippedFASTADBLoader();
            db.load(input);
            int liCounter = 0;
            String entry = null;
            while((entry = db.nextRawEntry()) != null) {
                Assert.assertTrue(entry.startsWith(">"));
                liCounter++;
            }
            Assert.assertEquals(7, liCounter);
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP raw reading behaviour:\n" + ioe.getMessage());
        }

        try {
            final String inputFile = "testFASTA.fas.gz";
            String input = super.getFullFilePath(inputFile);

            DBLoader db = new ZippedFASTADBLoader();
            db.load(input);
            int liCounter = 0;
            String entry = null;
            while((entry = db.nextRawEntry()) != null) {
                Assert.assertTrue(entry.startsWith(">"));
                liCounter++;
            }
            Assert.assertEquals(7, liCounter);
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP raw reading behaviour:\n" + ioe.getMessage());
        }
    }

    /**
     * This method tests the correct reading of the FASTA format against the original FASTA format.
     */
    public void testFastaFormat() {
        // ZIP format.
        try {
            final String inputFile = "testFASTA.zip";
            final String controlFile = "testFASTA_No_line_breaks.fas";
            String input = super.getFullFilePath(inputFile);
            String control = super.getFullFilePath(controlFile);

            DBLoader db = new ZippedFASTADBLoader();
            db.load(input);
            StringBuffer received = new StringBuffer();
            StringBuffer check = new StringBuffer();
            String line = null;
            while((line = db.nextFASTAEntry()) != null) {
                received.append(line+"\n");
            }

            BufferedReader lBr = new BufferedReader(new InputStreamReader(new FileInputStream(control)));
            while((line = lBr.readLine()) != null) {
                check.append(line+"\n");
            }
            lBr.close();
            Assert.assertEquals(check.toString(), received.toString());

        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP FASTA conversion:\n" + ioe.getMessage());
        }

        // GZIP format.
        try {
            final String inputFile = "testFASTA.fas.gz";
            final String controlFile = "testFASTA_No_line_breaks.fas";
            String input = super.getFullFilePath(inputFile);
            String control = super.getFullFilePath(controlFile);

            DBLoader db = new ZippedFASTADBLoader();
            db.load(input);
            StringBuffer received = new StringBuffer();
            StringBuffer check = new StringBuffer();
            String line = null;
            while((line = db.nextFASTAEntry()) != null) {
                received.append(line+"\n");
            }

            BufferedReader lBr = new BufferedReader(new InputStreamReader(new FileInputStream(control)));
            while((line = lBr.readLine()) != null) {
                check.append(line+"\n");
            }
            lBr.close();
            Assert.assertEquals(check.toString(), received.toString());

        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP FASTA conversion:\n" + ioe.getMessage());
        }
    }

    /**
     * This method test the counter.
     */
    public void testCounter() {
        // ZIP format
        try {
            final String inputFile = "testFASTA.zip";
            final String control = ">sw|P21541|ZY11_CAEEL Early embryogenesis ZYG-11 protein.";
            String input = super.getFullFilePath(inputFile);

            // First find out whether the correct amount of entries is found.
            DBLoader db = new ZippedFASTADBLoader();
            db.load(input);
            long entries = db.countNumberOfEntries();
            Assert.assertEquals(7l, entries);
            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP FASTA DB entrycounter:\n" + ioe.getMessage());
        }
        // GZIP format
        try {
            final String inputFile = "testFASTA.fas.gz";
            final String control = ">sw|P21541|ZY11_CAEEL Early embryogenesis ZYG-11 protein.";
            String input = super.getFullFilePath(inputFile);

            // First find out whether the correct amount of entries is found.
            DBLoader db = new ZippedFASTADBLoader();
            db.load(input);
            long entries = db.countNumberOfEntries();
            Assert.assertEquals(7l, entries);
            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP FASTA DB entrycounter:\n" + ioe.getMessage());
        }
    }

    /**
     * This method tests the reading of the DB via Protein
     * instances.
     */
    public void testNextProtein() {
        // ZIP format
        final String inputFile = "testFASTA.zip";
        final String controlFile = "testFASTA_No_line_breaks.fas";

        final String control = super.getFullFilePath(controlFile);

        try {
            String input = super.getFullFilePath(inputFile);
            DBLoader db = new ZippedFASTADBLoader();
            BufferedReader br = new BufferedReader(new FileReader(control));
            db.load(input);
            Protein p = null;
            int counter = 0;
            while((p = db.nextProtein()) != null) {
                counter++;
                Assert.assertEquals(p.getHeader().getFullHeaderWithAddenda(), br.readLine());
                Assert.assertEquals(p.getSequence().getSequence(), br.readLine());
            }
            br.close();
            Assert.assertEquals(7, counter);
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP FASTA DB nextProtein() method: '" + ioe.getMessage() + "'.");
        }
        // GZIP format
        final String inputFile2 = "testFASTA.fas.gz";
        try {
            String input = super.getFullFilePath(inputFile2);
            DBLoader db = new ZippedFASTADBLoader();
            BufferedReader br = new BufferedReader(new FileReader(control));
            db.load(input);
            Protein p = null;
            int counter = 0;
            while((p = db.nextProtein()) != null) {
                counter++;
                Assert.assertEquals(p.getHeader().getFullHeaderWithAddenda(), br.readLine());
                Assert.assertEquals(p.getSequence().getSequence(), br.readLine());
            }
            br.close();
            Assert.assertEquals(7, counter);
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP FASTA DB nextProtein() method: '" + ioe.getMessage() + "'.");
        }
    }

    /**
     * This method tests the reset capability.
     */
    public void testReset() {
        final String inputFile = "testFASTA.zip";
        final String input = super.getFullFilePath(inputFile);

        final String inputFile2 = "testFASTA.fas.gz";
        final String input2 = super.getFullFilePath(inputFile2);

        // ZIP format
        try {
            DBLoader db = new ZippedFASTADBLoader();
            db.load(input);
            int counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);

            // Reset and re-cycle.
            db.reset();
            counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);

        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP FASTA DB nextProtein() method: '" + ioe.getMessage() + "'.");
        }
        // GZIP format
        try {
            DBLoader db = new ZippedFASTADBLoader();
            db.load(input2);
            int counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);

            // Reset and re-cycle.
            db.reset();
            counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);

        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP FASTA DB nextProtein() method: '" + ioe.getMessage() + "'.");
        }
    }
}
