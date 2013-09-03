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
import org.apache.batik.script.rhino.WindowWrapper;

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
public class FASTAProteinSequenceLengthFilter extends ProteinSequenceLengthFilter {

    /**
     * This constructor takes the upper and lower mass limits for this filter.
     *
     * @param   aParameter  String with the less than/larger than character, and the length threshold
     *                              If no less than/larger than is specified, it is taken to be minimal length
     */
    public FASTAProteinSequenceLengthFilter(String aParameter) {
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

        // Check the header.
        HashMap hm = new HashMap(2);
        StringTokenizer lSt = new StringTokenizer(aEntry, "\n");
        hm.put("HEADER", lSt.nextToken());
        hm.put("SEQUENCE", lSt.nextToken());

        passed = this.passesFilter(hm);

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

        Object os = aEntry.get("SEQUENCE");
        if(os != null) {
            int sequenceLength = ((String)os).length();
            if(iLargerThan && sequenceLength >= iLength) {
                passed = true;
            } else if(!iLargerThan && sequenceLength <= iLength) {
                passed = true;
            }
        }

        return passed;
    }
}