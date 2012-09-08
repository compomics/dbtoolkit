/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-okt-02
 * Time: 17:01:50
 */
package com.compomics.dbtoolkit.test.io;

import com.compomics.dbtoolkit.io.QueryParser;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;
import junit.framework.*;

import java.text.ParseException;

/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the test scenario for the QueryParser class.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.QueryParser
 */
public class TestQueryParser extends TestCase {

    public TestQueryParser() {
        this("Test scenario for the QueryParser class.");
    }

    public TestQueryParser(String aName) {
        super(aName);
    }

    /**
     * This method tests a simple query String, resulting in a
     * single sequence filter.
     */
    public void testSimpleFilter() {
        try {
            Protein pass = new Protein(">Passes filter.", "LENNARTMARTENS");
            Protein noPass = new Protein(">Does not pass filter.", "KRISGEVAERT");

            QueryParser qp = new QueryParser();
            ProteinFilter pf = qp.parseQuery("M");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));

            pf = qp.parseQuery("^M");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
        } catch(ParseException pe) {
            fail("ParseException thrown when testing simple query String: " + pe.getMessage());
        }
    }

    /**
     * This method tests a complex query String, resulting in a
     * sequence filter collection.
     * AND version.
     */
    public void testComplexAndFilter() {
        try {
            Protein pass = new Protein(">Passes filter.", "LENNARTMARTENS");
            Protein noPass = new Protein(">Does not pass filter.", "KRISGEVAERT");

            QueryParser qp = new QueryParser();
            ProteinFilter pf = qp.parseQuery("M and R");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));

            pf= qp.parseQuery("^M and G");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

            pf= qp.parseQuery("^M and G and ^L");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

            pf= qp.parseQuery("^M and G and ^L and ^K");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));
        } catch(ParseException pe) {
            fail("ParseException thrown when testing simple query String: " + pe.getMessage());
        }
    }

    /**
     * This method tests a complex query String, resulting in a
     * sequence filter collection.
     * OR version.
     */
    public void testComplexOrFilter() {
        try {
            Protein pass = new Protein(">Passes filter.", "LENNARTMARTENS");
            Protein noPass = new Protein(">Does not pass filter.", "KRISGEVAERT");
            Protein noPass2 = new Protein(">Does not pass filter.", "GRTMAR");

            QueryParser qp = new QueryParser();
            ProteinFilter pf = qp.parseQuery("M OR L");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));
            Assert.assertTrue(pf.passesFilter(noPass2));

            pf= qp.parseQuery("^M OR K");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertFalse(pf.passesFilter(noPass2));

            pf= qp.parseQuery("^M OR K or RIS");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertFalse(pf.passesFilter(noPass2));

            pf= qp.parseQuery("^M OR K or ^ARTMAR");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertTrue(pf.passesFilter(noPass2));
        } catch(ParseException pe) {
            fail("ParseException thrown when testing simple query String: " + pe.getMessage());
        }
    }

    /**
     * This method tests a complex query String with lowercase, resulting in a
     * sequence filter collection.
     * OR version.
     */
    public void testComplexOrFilterLowerCase() {
        try {
            Protein pass = new Protein(">Passes filter.", "LENNARTMARTENS");
            Protein noPass = new Protein(">Does not pass filter.", "KRISGEVAERT");
            Protein noPass2 = new Protein(">Does not pass filter.", "GRTMAR");

            QueryParser qp = new QueryParser();
            ProteinFilter pf = qp.parseQuery("m OR L");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));
            Assert.assertTrue(pf.passesFilter(noPass2));

            pf= qp.parseQuery("^M OR k");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertFalse(pf.passesFilter(noPass2));

            pf= qp.parseQuery("^m OR k or RiS");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertFalse(pf.passesFilter(noPass2));

            pf= qp.parseQuery("^m OR K or ^ArtmAR");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertTrue(pf.passesFilter(noPass2));
        } catch(ParseException pe) {
            fail("ParseException thrown when testing simple query String: " + pe.getMessage());
        }
    }

    /**
     * This method tests a variety of highly complex query Strings
     * with subsections.
     */
    public void testHighlyComplex() {
        try {
            Protein pass = new Protein(">Passes filter.", "LENNARTMARTENS");
            Protein noPass = new Protein(">Does not pass filter.", "KRISGEVAERT");
            Protein noPass2 = new Protein(">Does not pass filter.", "GRTMAR");

            QueryParser qp = new QueryParser();
            ProteinFilter pf = qp.parseQuery("(L and M) or (K and G)");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertFalse(pf.passesFilter(noPass2));

            pf= qp.parseQuery("^(L and M) OR (k or G)");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertTrue(pf.passesFilter(noPass2));

            pf= qp.parseQuery("(L and ^K) or ((G and S) or (V and H))");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertFalse(pf.passesFilter(noPass2));

            pf= qp.parseQuery("(K or R) and (V or M)");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));
            Assert.assertTrue(pf.passesFilter(noPass2));

            pf= qp.parseQuery("(^K and ^R) or (^V and ^M)");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));
            Assert.assertFalse(pf.passesFilter(noPass2));
        } catch(ParseException pe) {
            fail("ParseException thrown when testing simple query String: " + pe.getMessage());
        }
    }

    /**
     * This method tests the throwing of a parseException when nonsense is sent to the
     * parser.
     */
    public void testException() {
        QueryParser qp = new QueryParser();
        try {
            String wrongQuery = "^M AND G AND     ";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "^M OR G OR     ";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "AND G";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = " AND G AND     ";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "OR G";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "   OR  G";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "()";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "(A)) OR (G and F)";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "(A)) OR G";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "((A or G)";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }

        try {
            String wrongQuery = "(A or G) (K and L)";
            qp.parseQuery(wrongQuery);
            fail("No ParseException thrown when the parser was confronted with '" + wrongQuery + "'!");
        } catch(ParseException pe) {
            // This is what we want.
        }
    }

    /**
     * This method tests the ability of the parser to parse residucount filters
     */
    public void testSimpleResCountFilters() {
         try {
            Protein pass = new Protein(">Passes filter.", "LENNARTMARTENS");
            Protein noPass = new Protein(">Does not pass filter.", "KRISGEVAERT");

            QueryParser qp = new QueryParser();
            ProteinFilter pf = qp.parseQuery("2A");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));

            pf= qp.parseQuery("^2A");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

            pf= qp.parseQuery("=2A");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));

            pf= qp.parseQuery("^=2A");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

             pf = qp.parseQuery("25A");
             Assert.assertFalse(pf.passesFilter(pass));
             Assert.assertFalse(pf.passesFilter(noPass));

             pf= qp.parseQuery("^25A");
             Assert.assertTrue(pf.passesFilter(pass));
             Assert.assertTrue(pf.passesFilter(noPass));

             pf= qp.parseQuery("=25A");
             Assert.assertFalse(pf.passesFilter(pass));
             Assert.assertFalse(pf.passesFilter(noPass));

             pf= qp.parseQuery("^=25A");
             Assert.assertTrue(pf.passesFilter(pass));
             Assert.assertTrue(pf.passesFilter(noPass));

             pf = qp.parseQuery("<2A");
             Assert.assertFalse(pf.passesFilter(pass));
             Assert.assertTrue(pf.passesFilter(noPass));

             pf= qp.parseQuery("^<2A");
             Assert.assertTrue(pf.passesFilter(pass));
             Assert.assertFalse(pf.passesFilter(noPass));

             pf= qp.parseQuery("<25A");
             Assert.assertTrue(pf.passesFilter(pass));
             Assert.assertTrue(pf.passesFilter(noPass));

             pf= qp.parseQuery("^<25A");
             Assert.assertFalse(pf.passesFilter(pass));
             Assert.assertFalse(pf.passesFilter(noPass));

             pf = qp.parseQuery(">1A");
             Assert.assertTrue(pf.passesFilter(pass));
             Assert.assertFalse(pf.passesFilter(noPass));

             pf= qp.parseQuery("^>1A");
             Assert.assertFalse(pf.passesFilter(pass));
             Assert.assertTrue(pf.passesFilter(noPass));

             pf= qp.parseQuery(">25A");
             Assert.assertFalse(pf.passesFilter(pass));
             Assert.assertFalse(pf.passesFilter(noPass));

             pf= qp.parseQuery("^>25A");
             Assert.assertTrue(pf.passesFilter(pass));
             Assert.assertTrue(pf.passesFilter(noPass));

        } catch(ParseException pe) {
            fail("ParseException thrown when testing simple residu count String: " + pe.getMessage());
        }
    }

    /**
     * Complex queries using rescounter.
     */
    public void testComplexResCounter() {
        try {
            Protein pass = new Protein(">Passes filter.", "LENNARTMARTENS");
            Protein noPass = new Protein(">Does not pass filter.", "KRISGEVAERT");

            QueryParser qp = new QueryParser();

            ProteinFilter pf = qp.parseQuery("^>2A and 2R");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

            pf = qp.parseQuery("^>2A and 1K");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

            pf = qp.parseQuery("^>2A or 2K");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

            pf = qp.parseQuery("^(^>2A or 2K)");
            Assert.assertFalse(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));

            pf = qp.parseQuery("(^>2A or 2K) and (M and L)");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));

            pf = qp.parseQuery(">1R or >1K");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertTrue(pf.passesFilter(noPass));

            pf = qp.parseQuery(">0LENN or 2K");
            Assert.assertTrue(pf.passesFilter(pass));
            Assert.assertFalse(pf.passesFilter(noPass));

        } catch(ParseException pe) {
            fail("ParseException thrown when testing simple residu count String: " + pe.getMessage());
        }
    }

    /**
     * Test exception throwing.
     */
    public void testExceptionResCount() {
        QueryParser qp = new QueryParser();
        try {
            String query = "2,H";
            qp.parseQuery(query);
            // We should never get here.
            fail("ParseException was not raised when confronting the parser with invalid query: '" + query + "'!");
        } catch(ParseException pe) {
            // We expect this.
        }

        try {
            String query = "M and 2";
            qp.parseQuery(query);
            // We should never get here.
            fail("ParseException was not raised when confronting the parser with invalid query: '" + query + "'!");
        } catch(ParseException pe) {
            // We expect this.
        }

        try {
            String query = "2 and M";
            qp.parseQuery(query);
            // We should never get here.
            fail("ParseException was not raised when confronting the parser with invalid query: '" + query + "'!");
        } catch(ParseException pe) {
            // We expect this.
        }

        try {
            String query = "(3H and R) OR (M and 2)";
            qp.parseQuery(query);
            // We should never get here.
            fail("ParseException was not raised when confronting the parser with invalid query: '" + query + "'!");
        } catch(ParseException pe) {
            // We expect this.
        }
    }
}
