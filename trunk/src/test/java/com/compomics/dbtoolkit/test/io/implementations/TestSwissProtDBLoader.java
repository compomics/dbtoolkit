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

import com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader;
import com.compomics.dbtoolkit.io.implementations.SwissProtTaxonomyFilter;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.junit.TestCaseLM;
import com.compomics.util.protein.Protein;
import junit.framework.*;

import java.io.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2008/09/08 16:14:04 $
 */

/**
 * This class implements the test for the SwissProtDBLoader class.
 *
 * @author Lennart
 * @see com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader
 */
public class TestSwissProtDBLoader extends TestCase {

    public TestSwissProtDBLoader() {
        this("Test for the SwissProtDBLoader class.");
    }

    public TestSwissProtDBLoader(String aName) {
        super(aName);
    }

    /**
     * This method tests the behaviour of the class when the
     * specified file does not exist.
     */
    public void testFileNotFoundBehaviour() {
        final String wrongFile = "FileThatDoesNotExist";
        try {
            DBLoader sp = new SwissProtDBLoader();
            sp.load(wrongFile);
            fail("SwissProtDBLoader should have flagged an IOException when confronted with the erronous filename '"+wrongFile+"'.");
        } catch(IOException ioe) {
            // OK, this is EXACTLY what we want.
        }
    }

    /**
     * This method tests the correct reporting on the name for the SwissProtDBLoader.
     */
    public void testGetNameMethod() {
        DBLoader sp = new SwissProtDBLoader();
        Assert.assertEquals(DBLoader.SWISSPROT, sp.getDBName());
    }

    /**
     * This method tests the reading of raw entries from the flatfile db.
     */
    public void testRawReadingBehaviour() {
        try {
            final String inputFile = "test.spr";
            final String controlFile = "testAfter.spr";
            String input = TestCaseLM.getFullFilePath(inputFile);
            String control = TestCaseLM.getFullFilePath(controlFile);

            DBLoader db = new SwissProtDBLoader();
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
            fail("An IOException was encountered while testing the raw reading behaviour:\n" + ioe.getMessage());
        }
    }

    /**
     * This method test the conversion of SwissProt raw format into a standard FASTA format.
     */
    public void testFastaFormat() {
        try {
            final String inputFile = "test.spr";
            final String controlFile = "testFASTA_NoLineBreaks.fas";
            String input = TestCaseLM.getFullFilePath(inputFile);
            String control = TestCaseLM.getFullFilePath(controlFile);

            DBLoader db = new SwissProtDBLoader();
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

            // Now test the post-September 2008 format conversion to FASTA.
            input = TestCaseLM.getFullFilePath("test3.spr");

            db = new SwissProtDBLoader();
            db.load(input);
            int count = 0;
            while((line = db.nextFASTAEntry()) != null) {
                count++;
                Assert.assertEquals(">sw|Q1IKC9|PYRG_ACIBL CTP synthase\nMSAKYIFVTGGVVSSLGKGLAAASIGCLLEMRGLKVNMQKFDPYLNVDPGTMSPFQHGEVFVTDDGAETDLDLGHYERYTHSKLTRENNWTTGRIYEQIITKERRGDYLGKTVQVIPHVTNEIKAAMKRAAVDVDVAIVEIGGTVGDIESLPFIEAIRQMRQELGRDNTLFVHLTLVPYIAAAGELKTKPTQHSVKELLSIGIQPDILLCRTDRFLSKDIKGKIALFCNVEDEAVITAKDVASIYEVPLGFHHEGVDRLVMKYLRLDAKEPDLTRWQDIVHRVYNPKDEVIIGIIGKYVEYEDSYKSLKEALVHGSLAHNLKLNVTWIEAEGLETKDESYYEQLRHVDGILVPGGFGKRGIAGMLNGIRFAREHKVPYFGICLGMQTASIEFARNVCGLEDANSSEFDPATPHRVIYKLRELRGVEELGGTMRLGAWACKLEPGSHAAKAYGTTEISERHRHRYEFNQEYREQMAAAGLKFTGTTPDGTYIEIVELDQNEHPYFLGCQFHPEFKSKPLEPHPLFKAFIGASYEHRMKRTHTKEREEESVFLRPERVGK\n", line);

            }
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the FASTA conversion:\n" + ioe.getMessage());
        }
    }

    /**
     * This method tests the filtering methods of the DBLoader class.
     */
    public void testFilter() {
        try {
            final String inputFile = TestCaseLM.getFullFilePath("test.spr");
            Filter f = new SwissProtTaxonomyFilter("Homo saPiens");
            DBLoader db = new SwissProtDBLoader();
            db.load(inputFile);
            String entry = null;
            int liCounter = 0;
            while((entry = db.nextFilteredRawEntry(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

            db = null;

            db = new SwissProtDBLoader();
            db.load(inputFile);
            liCounter = 0;
            while((entry = db.nextFilteredFASTAEntry(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the FASTA conversion:\n" + ioe.getMessage());
        }
    }

    /**
     * This method test the counter.
     */
    public void testCounter() {
        try {
            final String inputFile = "test.spr";
            String input = TestCaseLM.getFullFilePath(inputFile);

            // First find out whether the correct amount of entries is found.
            DBLoader db = new SwissProtDBLoader();
            db.load(input);
            long entries = db.countNumberOfEntries();
            Assert.assertEquals(7l, entries);
            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the SwissProt DB entrycounter:\n" + ioe.getMessage());
        }
    }

    /**
     * This method tests the sequenctial retrieval of Protein instances
     * from the DB.
     */
    public void testNextProtein() {
        try {
            final String inputFile = TestCaseLM.getFullFilePath("test.spr");
            final String controlFile = TestCaseLM.getFullFilePath("testFASTA_NoLineBreaks.fas");

            DBLoader db = new SwissProtDBLoader();
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
            fail("An IOException was encountered while testing the FASTA conversion:\n" + ioe.getMessage());
        }
    }


    /**
     * This method tests the nextFilteredProtein method.
     */
    /**
     * This method tests the filtering methods of the DBLoader class.
     */
    public void testNextFilteredProtein() {
        try {
            final String inputFile = TestCaseLM.getFullFilePath("test.spr");
            Filter f = new SwissProtTaxonomyFilter("Homo saPiens");
            DBLoader db = new SwissProtDBLoader();
            db.load(inputFile);
            Protein entry = null;
            int liCounter = 0;
            while((entry = db.nextFilteredProtein(f)) != null) {
                liCounter++;
            }
            Assert.assertEquals(3, liCounter);

            db = null;
        } catch(IOException ioe) {
            fail("An IOException was encountered while testing the FASTA conversion:\n" + ioe.getMessage());
        }
    }

    /**
     * This method tests the reset capability.
     */
    public void testReset() {
        try {
            final String inputFile = "test.spr";
            String input = TestCaseLM.getFullFilePath(inputFile);

            DBLoader db = new SwissProtDBLoader();
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
            fail("An IOException was encountered while testing the raw reading behaviour:\n" + ioe.getMessage());
        }
    }
}
