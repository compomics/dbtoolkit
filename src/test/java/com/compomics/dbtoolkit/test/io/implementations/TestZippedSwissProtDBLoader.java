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

import com.compomics.dbtoolkit.io.implementations.SwissProtTaxonomyFilter;
import com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
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
 * This class implements the test for the ZippedSwissProtDBLoader class.
 *
 * @author Lennart
 * @see com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader
 */
public class TestZippedSwissProtDBLoader extends TestCaseLM {

    public TestZippedSwissProtDBLoader() {
        this("Test for the ZippedSwissProtDBLoader class.");
    }

    public TestZippedSwissProtDBLoader(String aName) {
        super(aName);
    }

    /**
     * This method tests the behaviour of the class when the
     * specified file does not exist.
     */
    public void testFileNotFoundBehaviour() {
        final String wrongFile = "FileThatDoesNotExist";
        try {
            DBLoader sp = new ZippedSwissProtDBLoader();
            sp.load(wrongFile);
            fail("ZippedSwissProtDBLoader should have flagged an IOException when confronted with the erronous filename '"+wrongFile+"'.");
        } catch(IOException ioe) {
            // OK, this is EXACTLY what we want.
        }
    }

    /**
     * This method tests the correct reporting on the name for the SwissProtDBLoader.
     */
    public void testGetNameMethod() {
        DBLoader sp = new ZippedSwissProtDBLoader();
        Assert.assertEquals(DBLoader.SWISSPROT, sp.getDBName());
    }

    /**
     * This method tests the reading of raw entries from the flatfile db.
     */
    public void testRawReadingBehaviour() {
        // ZIP format
        try {
            final String inputFile = "test_SPFormat.zip";
            final String controlFile = "testAfter.spr";
            String input = super.getFullFilePath(inputFile);
            String control = super.getFullFilePath(controlFile);

            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);
            StringBuffer received = new StringBuffer();
            StringBuffer check = new StringBuffer();
            String line = null;
            while((line = db.nextRawEntry()) != null) {
                received.append(line + "]]\n");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(control)));
            while((line = br.readLine()) != null) {
                check.append(line+"\n");
            }
            br.close();
            Assert.assertEquals(check.toString(), received.toString());
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP raw reading behaviour:\n" + ioe.getMessage());
        }
        // GZIP format
        try {
            final String inputFile = "test_SPFormat.spr.gz";
            final String controlFile = "testAfter.spr";
            String input = super.getFullFilePath(inputFile);
            String control = super.getFullFilePath(controlFile);

            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);
            StringBuffer received = new StringBuffer();
            StringBuffer check = new StringBuffer();
            String line = null;
            while((line = db.nextRawEntry()) != null) {
                received.append(line + "]]\n");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(control)));
            while((line = br.readLine()) != null) {
                check.append(line+"\n");
            }
            br.close();
            Assert.assertEquals(check.toString(), received.toString());
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing GZIP the raw reading behaviour:\n" + ioe.getMessage());
        }
    }

    /**
     * This method test the conversion of SwissProt raw format into a standard FASTA format.
     */
    public void testFastaFormat() {
        // ZIP format
        try {
            final String inputFile = "test_SPFormat.zip";
            final String controlFile = "testFASTA_NoLineBreaks.fas";
            String input = super.getFullFilePath(inputFile);
            String control = super.getFullFilePath(controlFile);

            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);
            StringBuffer received = new StringBuffer();
            StringBuffer check = new StringBuffer();
            String line = null;
            while((line = db.nextFASTAEntry()) != null) {
                received.append(line);
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
        // GZIP format
        try {
            final String inputFile = "test_SPFormat.spr.gz";
            final String controlFile = "testFASTA_NoLineBreaks.fas";
            String input = super.getFullFilePath(inputFile);
            String control = super.getFullFilePath(controlFile);

            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);
            StringBuffer received = new StringBuffer();
            StringBuffer check = new StringBuffer();
            String line = null;
            while((line = db.nextFASTAEntry()) != null) {
                received.append(line);
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
     * This method tests the filtering methods of the DBLoader class.
     */
    public void testFilter() {
        // ZIP format
        try {
            final String inputFile = super.getFullFilePath("test_SPFormat.zip");
            Filter f = new SwissProtTaxonomyFilter("Homo saPiens");
            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(inputFile);
            String entry = null;
            int liCounter = 0;
            while((entry = db.nextFilteredRawEntry(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

            db = null;

            db = new ZippedSwissProtDBLoader();
            db.load(inputFile);
            liCounter = 0;
            while((entry = db.nextFilteredFASTAEntry(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP filtering:\n" + ioe.getMessage());
        }
        // GZIP format
        try {
            final String inputFile = super.getFullFilePath("test_SPFormat.spr.gz");
            Filter f = new SwissProtTaxonomyFilter("Homo saPiens");
            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(inputFile);
            String entry = null;
            int liCounter = 0;
            while((entry = db.nextFilteredRawEntry(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

            db = null;

            db = new ZippedSwissProtDBLoader();
            db.load(inputFile);
            liCounter = 0;
            while((entry = db.nextFilteredFASTAEntry(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP filtering:\n" + ioe.getMessage());
        }
    }

    /**
     * This method test the counter.
     */
    public void testCounter() {
        // ZIP format
        try {
            final String inputFile = "test_SPFormat.zip";
            final String control = ">sw|P21541|ZY11_CAEEL Early embryogenesis ZYG-11 protein.";
            String input = super.getFullFilePath(inputFile);

            // First find out whether the correct amount of entries is found.
            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);
            long entries = db.countNumberOfEntries();
            Assert.assertEquals(7l, entries);
            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP SwissProt DB entrycounter:\n" + ioe.getMessage());
        }
        // GZIP format
        try {
            final String inputFile = "test_SPFormat.spr.gz";
            final String control = ">sw|P21541|ZY11_CAEEL Early embryogenesis ZYG-11 protein.";
            String input = super.getFullFilePath(inputFile);

            // First find out whether the correct amount of entries is found.
            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);
            long entries = db.countNumberOfEntries();
            Assert.assertEquals(7l, entries);
            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP SwissProt DB entrycounter:\n" + ioe.getMessage());
        }
    }

    /**
     * This method tests the sequenctial retrieval of Protein instances
     * from the DB.
     */
    public void testNextProtein() {
        // ZIP format
        try {
            final String inputFile = super.getFullFilePath("test_SPFormat.zip");
            final String controlFile = super.getFullFilePath("testFASTA_NoLineBreaks.fas");

            DBLoader db = new ZippedSwissProtDBLoader();
            BufferedReader br = new BufferedReader(new FileReader(controlFile));

            db.load(inputFile);
            Protein entry = null;
            int liCounter = 0;
            while((entry = db.nextProtein()) != null) {
                liCounter++;
                Assert.assertEquals(br.readLine(), entry.getHeader().getFullHeaderWithAddenda());
                Assert.assertEquals(br.readLine(), entry.getSequence().getSequence());
            }
            Assert.assertEquals(7, liCounter);

            br.close();
            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP FASTA conversion:\n" + ioe.getMessage());
        }
        // GZIP format
        try {
            final String inputFile = super.getFullFilePath("test_SPFormat.spr.gz");
            final String controlFile = super.getFullFilePath("testFASTA_NoLineBreaks.fas");

            DBLoader db = new ZippedSwissProtDBLoader();
            BufferedReader br = new BufferedReader(new FileReader(controlFile));

            db.load(inputFile);
            Protein entry = null;
            int liCounter = 0;
            while((entry = db.nextProtein()) != null) {
                liCounter++;
                Assert.assertEquals(br.readLine(), entry.getHeader().getFullHeaderWithAddenda());
                Assert.assertEquals(br.readLine(), entry.getSequence().getSequence());
            }
            Assert.assertEquals(7, liCounter);

            br.close();
            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP FASTA conversion:\n" + ioe.getMessage());
        }
    }


    /**
     * This method tests the nextFilteredProtein method.
     */
    /**
     * This method tests the filtering methods of the DBLoader class.
     */
    public void testNextFilteredProtein() {
        // ZIP format
        try {
            final String inputFile = super.getFullFilePath("test_SPFormat.zip");
            Filter f = new SwissProtTaxonomyFilter("Homo saPiens");
            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(inputFile);
            Protein entry = null;
            int liCounter = 0;
            while((entry = db.nextFilteredProtein(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP FASTA conversion:\n" + ioe.getMessage());
        }
        // GZIP format
        try {
            final String inputFile = super.getFullFilePath("test_SPFormat.spr.gz");
            Filter f = new SwissProtTaxonomyFilter("Homo saPiens");
            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(inputFile);
            Protein entry = null;
            int liCounter = 0;
            while((entry = db.nextFilteredProtein(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP FASTA conversion:\n" + ioe.getMessage());
        }
    }

    /**
     * This method tests the reset capability.
     */
    public void testReset() {
        // ZIP format
        try {
            final String inputFile = "test_SPFormat.zip";
            String input = super.getFullFilePath(inputFile);

            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);

            int counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);

            // Reset and recount.
            db.reset();
            counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the ZIP reset behaviour:\n" + ioe.getMessage());
        }

        try {
            final String inputFile = "test_SPFormat.spr.gz";
            String input = super.getFullFilePath(inputFile);

            DBLoader db = new ZippedSwissProtDBLoader();
            db.load(input);

            int counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);

            // Reset and recount.
            db.reset();
            counter = 0;
            while(db.nextRawEntry() != null) {
                counter++;
            }
            Assert.assertEquals(7, counter);
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the GZIP reset behaviour:\n" + ioe.getMessage());
        }
    }
}
