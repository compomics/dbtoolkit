/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-sep-02
 * Time: 16:01:23
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.Filter;

import java.util.HashMap;
import java.util.StringTokenizer;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a header filter for a FASTA formatted database.
 *
 * @author Lennart Martens
 */
public class FASTAHeaderFilter implements Filter {

    /**
     * This instance will convert the raw String passed in into a
     * HashMap when appropriate.
     */
    private FASTADBLoader iDB = new FASTADBLoader();

    /**
     * This variable holds the String to match the header fields against.
     */
    protected final String iMatch;

    /**
     * This boolean flags the Boolean NOT operator for this Filter.
     */
    protected boolean iInvert = false;

    /**
     * Key for the FASTA header.
     */
    public static final String HEADER = "HEADER";

    /**
     * Key for the FASTA sequence.
     */
    public static final String SEQUENCE = "SEQUENCE";


    /**
     * This constructor takes the match String against which a successfull
     * match must be detected for the header fields before an entry can pass
     * the filter.
     *
     * @param   aMatch  String to match.
     */
    public FASTAHeaderFilter(String aMatch) {
        this.iMatch = aMatch.toUpperCase();
    }

    /**
     * This constructor takes the match String against which a successfull
     * match must be detected for the header fields before an entry can pass
     * the filter.
     *
     * @param   aMatch  String to match.
     * @param   aInvert boolean to indicate whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     */
    public FASTAHeaderFilter(String aMatch, boolean aInvert) {
        this.iMatch = aMatch.toUpperCase();
        this.iInvert = aInvert;
    }

    /**
     * This method tests whether the entry passes the filter.
     *
     * @param   aEntry  String with the fasta entry to filter
     */
    public boolean passesFilter(String aEntry) {
        boolean passed = false;

        // Check the header.
        HashMap hm = new HashMap(2);
        StringTokenizer lSt = new StringTokenizer(aEntry, "\n");
        hm.put(HEADER, lSt.nextToken());
        hm.put(SEQUENCE, lSt.nextToken());

        passed = this.passesFilter(hm);

        return passed;
    }

    /**
     * This method tests whether the entry passes the filter.
     *
     * @param   aEntry  HashMap with the raw entry to filter.
     */
    public boolean passesFilter(HashMap aEntry) {
        boolean passed = false;

        Object os = aEntry.get(HEADER);
        if(os != null) {
            if(((String)os).toUpperCase().indexOf(iMatch) >= 0) {
                passed = true;
            }
        }

        if(iInvert) {
            passed = ! passed;
        }

        return passed;
    }
}
