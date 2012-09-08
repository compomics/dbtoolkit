/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-okt-02
 * Time: 11:04:47
 */
package com.compomics.dbtoolkit.test.gui.workerthreads;

import com.compomics.dbtoolkit.gui.workerthreads.ClearRedundancyThread;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class provides a test for the non-threaded workings of
 * the ClearRedundancyThread class.
 *
 * @author Lennart
 * @see com.compomics.dbtoolkit.gui.workerthreads.ClearRedundancyThread
 */
public class TestClearRedundancyThread extends TestCase {

    public TestClearRedundancyThread() {
        this("Test scenario for the clear redundancy thread - non threaded!");
    }

    public TestClearRedundancyThread(String aName) {
        super(aName);
    }

    /**
     * Test the clearing of redundancy and the deletion of the files.
     */
    public void testClearing() {
        File temp = null;
        File output = null;
        try {
            String input = TestCaseLM.getFullFilePath("redundantDB.fas");
            File inputFile = new File(input);
            temp = new File(inputFile.getParent() + "/temp/");
            temp.mkdir();
            output = new File(inputFile.getParent() + "/outputOfClearRedundancyTest.fas");
            File control = new File(TestCaseLM.getFullFilePath("controlOfClearRedundancyTest.fas"));

            AutoDBLoader auto = new AutoDBLoader(new String[]{"com.compomics.dbtoolkit.io.implementations.FASTADBLoader"});
            DBLoader loader = auto.getLoaderForFile(input);
            ClearRedundancyThread crt = new ClearRedundancyThread(temp, output, loader, auto);
            crt.run();

            loader.close();
            crt = null;

            // Okay, now we need to verify the files.
            BufferedReader brOutput = new BufferedReader(new FileReader(output));
            BufferedReader brControl = new BufferedReader(new FileReader(control));
            String line = null;
            while((line = brOutput.readLine()) != null) {
                Assert.assertEquals(brControl.readLine(), line);
            }
            brOutput.close();
            brOutput = null;
            brControl.close();
            brControl = null;

            boolean lTemp = false;
            boolean lOutput = false;
        } catch(IOException ioe) {
            fail("IOException occurred while trying to test the clearing of redundancy: '" + ioe.getMessage() + "'.");
        } catch(UnknownDBFormatException udfe) {
            fail("UnknownDBFormatException occurred while trying to test the clearing of redundancy (with FASTA DB): '" + udfe.getMessage() + "'.");
        } finally {
            if(temp != null && temp.exists()) {
                temp.delete();
            }
            if(output != null && output.exists()) {
                output.delete();
            }
        }
    }
}
