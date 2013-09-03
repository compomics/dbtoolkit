/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 23-jan-03
 * Time: 9:49:30
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.protein.Protein;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.StringTokenizer;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the ProteinFilter interface for Protein mass.
 *
 * @author Lennart Martens
 */
public class SwissProtProteinSequenceLengthFilter extends ProteinSequenceLengthFilter {

    /**
     * This instance will convert the raw String passed in into a
     * HashMap when appropriate.
     */
    private SwissProtDBLoader iSpdb = new SwissProtDBLoader();

    /**
     * This constructor takes the upper and lower mass limits for this filter.
     *
     * @param   aParameter  String with the less than/larger than character, and the length threshold
     *                              If no less than/larger than is specified, it is taken to be minimal length
     */
    public SwissProtProteinSequenceLengthFilter(String aParameter) {
        super(aParameter);
    }

    /**
     * This method returns a flag that indicates whether the specified protein sequence string
     * (in FASTA format) passes the filter.
     *
     * @param   aEntry    String with the protein sequence string in FASTA format.
     * @return  boolean 'true' if the specified Protein passes the filter, 'false' otherwise.
     */
    public boolean passesFilter(String aEntry) {
        boolean passed = false;

        // First transform the raw String into a HashMap.
        try {
            String fastaString = iSpdb.toFASTAString(aEntry, false);

            StringTokenizer lSt = new StringTokenizer(fastaString, "\n");
            lSt.nextToken();
            String sequence = lSt.nextToken().trim();


            int sequenceLength = sequence.length();
            if(iLargerThan && sequenceLength >= iLength) {
                passed = true;
            } else if(!iLargerThan && sequenceLength <= iLength) {
                passed = true;            }

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return passed;
    }

    /**
     * This method returns a flag that indicates whether the specified protein sequence string
     * (in key-value Map format) passes the filter.
     *
     * @param   aEntry    String with the protein sequence string in key-value Map format.
     * @return  boolean 'true' if the specified Protein passes the filter, 'false' otherwise.
     */
    public boolean passesFilter(HashMap aEntry) {
        boolean passed = false;

        try {
            BufferedReader lBr = new BufferedReader(new StringReader((String)aEntry.get("  ")));

            StringBuffer tempSequence = new StringBuffer();
            String line = null;
            while((line = lBr.readLine()) != null) {
                StringTokenizer lst = new StringTokenizer(line.trim(), " ");
                while(lst.hasMoreTokens()) {
                    tempSequence.append(lst.nextToken().trim());
                }
            }

            int sequenceLength = tempSequence.length();
            if(iLargerThan && sequenceLength >= iLength) {
                passed = true;
            } else if(!iLargerThan && sequenceLength <= iLength) {
                passed = true;
            }

        } catch(IOException ioe) {
            // Not much else we can do here. sequence won't pass filter.
        }

        return passed;
    }
}