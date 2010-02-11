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

import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a NCBI NRDB-style taxonomy filter for a FASTA formatted database.
 *
 * @author Lennart Martens
 */
public class FASTATaxonomyFilter extends FASTAHeaderFilter {

    /**
     * This constructor takes the match String against which a successfull
     * taxonomy match must be detected for the header fields before an entry can pass
     * the filter.
     *
     * @param   aMatch  String with the taxonomy to match.
     */
    public FASTATaxonomyFilter(String aMatch) {
        super(aMatch);
    }

    /**
     * This constructor takes the match String against which a successfull
     * taxonomy match must be detected for the header fields before an entry can pass
     * the filter.
     *
     * @param   aMatch  String with the taxonomy to match.
     * @param   aInvert boolean to indicate whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     */
    public FASTATaxonomyFilter(String aMatch, boolean aInvert) {
        super(aMatch, aInvert);
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
            if(((String)os).toUpperCase().indexOf("[" + iMatch + "]") >= 0) {
                passed = true;
            }
        }

        if(iInvert) {
            passed = ! passed;
        }

        return passed;
    }
}
