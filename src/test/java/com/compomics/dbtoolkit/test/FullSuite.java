/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 14:17:11
 */
package com.compomics.dbtoolkit.test;

import com.compomics.dbtoolkit.test.general.TestNoEnzymeSimulator;
import com.compomics.dbtoolkit.test.general.TestPeptideSequenceRegionRetriever;
import com.compomics.dbtoolkit.test.gui.workerthreads.TestClearRedundancyThread;
import com.compomics.dbtoolkit.test.gui.workerthreads.TestConcatenateThread;
import com.compomics.dbtoolkit.test.gui.workerthreads.TestProcessThread;
import com.compomics.dbtoolkit.test.gui.workerthreads.TestFASTAOutputThread;
import com.compomics.dbtoolkit.test.io.*;
import com.compomics.dbtoolkit.test.io.implementations.*;
import com.compomics.dbtoolkit.test.toolkit.TestContainsPeptide;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * CVS information:
 *
 * $Revision: 1.6 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class represents the full suite of tests for the DB_Tools project.
 *
 * @author Lennart Martens
 */
public class FullSuite extends TestCase {

    public FullSuite() {
        this("Full Suite for the DB_Tools project;");
    }

    public FullSuite(String aName) {
        super(aName);
    }

    public static Test suite() {
        TestSuite ts = new TestSuite("Full Suite for DB_Tools project.");

        ts.addTest(new TestSuite(TestSwissProtDBLoader.class));
        ts.addTest(new TestSuite(TestZippedSwissProtDBLoader.class));
        ts.addTest(new TestSuite(TestFASTADBLoader.class));
        ts.addTest(new TestSuite(TestZippedFASTADBLoader.class));
        ts.addTest(new TestSuite(TestDBLoaderFactory.class));
        ts.addTest(new TestSuite(TestSwissProtTaxonomyFilter.class));
        ts.addTest(new TestSuite(TestSwissProtNCBITaxonomyFilter.class));
        ts.addTest(new TestSuite(TestSwissProtKeywordFilter.class));
        ts.addTest(new TestSuite(TestFASTAHeaderFilter.class));
        ts.addTest(new TestSuite(TestFASTATaxonomyFilter.class));
        ts.addTest(new TestSuite(TestFilterCollection.class));
        ts.addTest(new TestSuite(TestProteinSequenceFilter.class));
        ts.addTest(new TestSuite(TestProteinResiduCountFilter.class));
        ts.addTest(new TestSuite(TestProteinMassFilter.class));
        ts.addTest(new TestSuite(TestProteinFilterCollection.class));
        ts.addTest(new TestSuite(TestAutoDBLoader.class));
        ts.addTest(new TestSuite(TestProcessThread.class));
        ts.addTest(new TestSuite(TestConcatenateThread.class));
        ts.addTest(new TestSuite(TestClearRedundancyThread.class));
        ts.addTest(new TestSuite(TestFASTAOutputThread.class));
        ts.addTest(new TestSuite(TestQueryParser.class));
        ts.addTest(new TestSuite(TestSequenceRegion.class));
        ts.addTest(new TestSuite(TestPeptideSequenceRegionRetriever.class));
        ts.addTest(new TestSuite(TestNoEnzymeSimulator.class));
        ts.addTest(new TestSuite(TestEnzymeLoader.class));
        ts.addTest(new TestSuite(TestDBLoaderLoader.class));
        ts.addTest(new TestSuite(TestFilterLoader.class));
        ts.addTest(new TestSuite(TestContainsPeptide.class));
        ts.addTest(new TestSuite(TestSwissProtAccessionFilter.class));

        return ts;
    }
}
