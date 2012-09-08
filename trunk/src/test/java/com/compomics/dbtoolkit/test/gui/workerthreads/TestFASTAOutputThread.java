/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 7-apr-2006
 * Time: 17:47:00
 */
package com.compomics.dbtoolkit.test.gui.workerthreads;

import com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread;
import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.junit.TestCaseLM;
import junit.framework.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.HashMap;
/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2006/04/08 20:10:01 $
 */

/**
 * This class implements the test scenario for the FASTAOutputThread class.
 *
 * @author Lennart Martens
 * @version $Id: TestFASTAOutputThread.java,v 1.1 2006/04/08 20:10:01 lennart Exp $
 * @see com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread
 */
public class TestFASTAOutputThread extends TestCase {

    public TestFASTAOutputThread() {
        this("Test scenario for the FASTAOutputThread class.");
    }

    public TestFASTAOutputThread(String aName) {
        super(aName);
    }

    /**
     * This method tests the parsing of a substition String.
     */
    public void testSubstitutionParsing() {
        String test = "I,L=1";
        try {
            HashMap result = FASTAOutputThread.parseSubstitutions(test);
            Assert.assertEquals(2, result.size());
            Assert.assertEquals("1", result.get("I"));
            Assert.assertEquals("1", result.get("L"));
        } catch(ParseException pe) {
            fail("ParseException thrown when attempting to parse '" + test + "': " + pe.getMessage() + ", at " + pe.getErrorOffset() + "!");
        }

        test = "test,L=1";
        try {
            HashMap result = FASTAOutputThread.parseSubstitutions(test);
            Assert.assertEquals(2, result.size());
            Assert.assertEquals("1", result.get("test"));
            Assert.assertEquals("1", result.get("L"));
        } catch(ParseException pe) {
            fail("ParseException thrown when attempting to parse '" + test + "': " + pe.getMessage() + ", at " + pe.getErrorOffset() + "!");
        }

        test = "I,L=1;K,Q=2;M,F=3";
        try {
            HashMap result = FASTAOutputThread.parseSubstitutions(test);
            Assert.assertEquals(6, result.size());
            Assert.assertEquals("1", result.get("I"));
            Assert.assertEquals("1", result.get("L"));
            Assert.assertEquals("2", result.get("K"));
            Assert.assertEquals("2", result.get("Q"));
            Assert.assertEquals("3", result.get("M"));
            Assert.assertEquals("3", result.get("F"));
        } catch(ParseException pe) {
            fail("ParseException thrown when attempting to parse '" + test + "': " + pe.getMessage() + ", at " + pe.getErrorOffset() + "!");
        }

        test = " I , L  = 1 ;  K , Q =  2 ; M ,  F = 3 ";
        try {
            HashMap result = FASTAOutputThread.parseSubstitutions(test);
            Assert.assertEquals(6, result.size());
            Assert.assertEquals("1", result.get("I"));
            Assert.assertEquals("1", result.get("L"));
            Assert.assertEquals("2", result.get("K"));
            Assert.assertEquals("2", result.get("Q"));
            Assert.assertEquals("3", result.get("M"));
            Assert.assertEquals("3", result.get("F"));
        } catch(ParseException pe) {
            fail("ParseException thrown when attempting to parse '" + test + "': " + pe.getMessage() + ", at " + pe.getErrorOffset() + "!");
        }
        test = "NNA=NGA;E=2;R=1";
        try {
            HashMap result = FASTAOutputThread.parseSubstitutions(test);
            Assert.assertEquals(3, result.size());
            Assert.assertEquals("NGA", result.get("NNA"));
            Assert.assertEquals("2", result.get("E"));
            Assert.assertEquals("1", result.get("R"));
        } catch(ParseException pe) {
            fail("ParseException thrown when attempting to parse '" + test + "': " + pe.getMessage() + ", at " + pe.getErrorOffset() + "!");
        }

        test = "this will never work.";
        try {
            HashMap result = FASTAOutputThread.parseSubstitutions(test);
            fail("No ParseException thrown when attempting to parse '" + test + "'!");
        } catch(ParseException pe) {
            // This is what should happen!
        }
    }

    /**
     * This method tests whether the substition function works.
     */
    public void testSubstitution1() {
        File input = new File(TestCaseLM.getFullFilePath("redundantDB.fas"));
        File control = new File(TestCaseLM.getFullFilePath("control_substitution.fas"));
        File output = new File(input.getParentFile(), "output_substitution.fas");
        try {
            DBLoader loader = DBLoaderLoader.loadDB(input);
            HashMap substitions = FASTAOutputThread.parseSubstitutions("E=2;R=1");
            FASTAOutputThread fot = new FASTAOutputThread(null, loader, output, substitions);
            fot.run();

            BufferedReader contr = new BufferedReader(new FileReader(control));
            BufferedReader subst = new BufferedReader(new FileReader(output));
            String line = null;
            while((line = contr.readLine()) != null) {
                Assert.assertEquals(line, subst.readLine());
            }
            Assert.assertTrue(subst.readLine() == null);

            contr.close();
            subst.close();
        }catch(Exception e) {
            fail("Exception occurred while trying to substitute in file '" + input.getAbsolutePath() + "': " + e.getMessage() + "!");
        } finally {
            if(output != null && output.exists()) {
                output.delete();
            }
        }
    }

    /**
     * This method tests whether the substition function works.
     */
    public void testSubstitution2() {
        File input = new File(TestCaseLM.getFullFilePath("redundantDB.fas"));
        File control = new File(TestCaseLM.getFullFilePath("control_substitution_2.fas"));
        File output = new File(input.getParentFile(), "output_substitution_2.fas");
        try {
            DBLoader loader = DBLoaderLoader.loadDB(input);
            HashMap substitions = FASTAOutputThread.parseSubstitutions("NNA=NGA;E=2;R=1");
            FASTAOutputThread fot = new FASTAOutputThread(null, loader, output, substitions);
            fot.run();

            BufferedReader contr = new BufferedReader(new FileReader(control));
            BufferedReader subst = new BufferedReader(new FileReader(output));
            String line = null;
            while((line = contr.readLine()) != null) {
                Assert.assertEquals(line, subst.readLine());
            }
            Assert.assertTrue(subst.readLine() == null);

            contr.close();
            subst.close();
        }catch(Exception e) {
            fail("Exception occurred while trying to substitute in file '" + input.getAbsolutePath() + "': " + e.getMessage() + "!");
        } finally {
            if(output != null && output.exists()) {
                output.delete();
            }
        }
    }

    /**
     * This method tests whether the substition function works.
     */
    public void testSubstitution3() {
        File input = new File(TestCaseLM.getFullFilePath("redundantDB.fas"));
        File control = new File(TestCaseLM.getFullFilePath("control_substitution_3.fas"));
        File output = new File(input.getParentFile(), "output_substitution_3.fas");
        try {
            DBLoader loader = DBLoaderLoader.loadDB(input);
            HashMap substitions = FASTAOutputThread.parseSubstitutions("R,M=Z;E,A=1");
            FASTAOutputThread fot = new FASTAOutputThread(null, loader, output, substitions);
            fot.run();

            BufferedReader contr = new BufferedReader(new FileReader(control));
            BufferedReader subst = new BufferedReader(new FileReader(output));
            String line = null;
            while((line = contr.readLine()) != null) {
                Assert.assertEquals(line, subst.readLine());
            }
            Assert.assertTrue(subst.readLine() == null);

            contr.close();
            subst.close();
        }catch(Exception e) {
            fail("Exception occurred while trying to substitute in file '" + input.getAbsolutePath() + "': " + e.getMessage() + "!");
        } finally {
            if(output != null && output.exists()) {
                output.delete();
            }
        }
    }
}
