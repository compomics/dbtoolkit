/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 22-okt-02
 * Time: 10:08:26
 */
package com.compomics.dbtoolkit.test.gui.workerthreads;

import com.compomics.dbtoolkit.gui.workerthreads.ConcatenateThread;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the ConcatenateThread class.
 * The test does not test the threading behaviour, however.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.gui.workerthreads.ConcatenateThread
 */
public class TestConcatenateThread extends TestCase {

    public TestConcatenateThread() {
        this("Test scenario for the ConcatenateThread class (albeit without testing threading).");
    }

    public TestConcatenateThread(String aName) {
        super(aName);
    }

    /**
     * This method tests the copying behaviour.
     */
    public void testCopy() {
        File input = null;
        File output = null;
        try {
            // Necessary variables.
            input = new File(TestCaseLM.getFullFilePath("testFASTA.fas"));
            output = new File(input.getParent() + "/outputOfCopyTest.fas");

            ConcatenateThread ct = new ConcatenateThread(input, output);
            ct.run();

            BufferedInputStream biControle = new BufferedInputStream(new FileInputStream(input));
            BufferedInputStream biOutput = new BufferedInputStream(new FileInputStream(output));

            int current = -1;
            while((current = biControle.read()) != -1) {
                Assert.assertEquals(current, biOutput.read());
            }
            biControle.close();
            biOutput.close();
        } catch(IOException ioe) {
            fail("IOException thrown when testing the copying behaviour of a ConcatenateThread: " + ioe.getMessage());
        } finally {
            if(output != null && output.exists()) {
                output.delete();
            }
        }
    }

    /**
     * This method tests the concatenation behaviour.
     */
    public void testConcatenation() {
        File input1 = null;
        File input2 = null;
        File output = null;
        File control = null;
        try {
            // Necessary variables.
            input1 = new File(TestCaseLM.getFullFilePath("testFASTA.fas"));
            input2 = new File(TestCaseLM.getFullFilePath("testAfter.spr"));
            control = new File(TestCaseLM.getFullFilePath("controlOfConcatTest.fas"));
            output = new File(input1.getParent() + "/outputOfConcatTest.fas");

            ConcatenateThread ct = new ConcatenateThread(input1, input2, output);
            ct.run();

            BufferedInputStream biControle = new BufferedInputStream(new FileInputStream(control));
            BufferedInputStream biOutput = new BufferedInputStream(new FileInputStream(output));

            int current = -1;
            while((current = biControle.read()) != -1) {
                Assert.assertEquals(current, biOutput.read());
            }

            biControle.close();
            biOutput.close();

        } catch(IOException ioe) {
            fail("IOException thrown when testing the copying behaviour of a ConcatenateThread: " + ioe.getMessage());
        } finally {
            if(output != null && output.exists()) {
                output.delete();
            }
        }
    }
}
