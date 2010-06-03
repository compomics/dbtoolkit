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

import java.io.IOException;
import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class implements the Taxonomy filter for the SwissProtDatabase.
 *
 * @author Lennart
 */
public class SwissProtTaxonomyFilter implements Filter {

    /**
     * This instance will convert the raw String passed in into a
     * HashMap when appropriate.
     */
    private SwissProtDBLoader iSpdb = new SwissProtDBLoader();

    /**
     * This variable holds the String to match the TAXONOMY fields against.
     */
    private final String iMatch;

    /**
     * This boolean flags the Boolean NOT operator for this Filter.
     */
    private boolean iInvert = false;

    /**
     * This constructor takes the match String against which a successfull
     * match must be detected for the TAXONOMY fields before an entry can pass
     * the filter.
     *
     * @param   aMatch  String to match.
     */
    public SwissProtTaxonomyFilter(String aMatch) {
        this.iMatch = aMatch.toUpperCase();
    }

    /**
     * This constructor takes the match String against which a successfull
     * match must be detected for the TAXONOMY fields before an entry can pass
     * the filter.
     *
     * @param   aMatch  String to match.
     * @param   aInvert boolean to indicat whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     */
    public SwissProtTaxonomyFilter(String aMatch, boolean aInvert) {
        this.iMatch = aMatch.toUpperCase();
        this.iInvert = aInvert;
    }

    /**
     * This method tests whether the entry passes the filter.
     *
     * @param   aEntry  String with the raw entry to filter
     */
    public boolean passesFilter(String aEntry) {
        boolean passed = false;

        // First transform the raw String into a HashMap.
        try {
            HashMap lRaw = iSpdb.processRawData(aEntry);
            passed = this.passesFilter(lRaw);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return passed;
    }

    /**
     * This method tests whether the entry passes the filter.
     *
     * @param   aEntry  HashMap with the raw entry to filter.
     */
    public boolean passesFilter(HashMap aEntry) {
        boolean passed = false;

        Object os = aEntry.get("OS");
        Object oc = aEntry.get("OC");
        if(os != null) {
            if(((String)os).toUpperCase().indexOf(iMatch) >= 0) {
                passed = true;
            } else if(oc != null){
                if(((String)oc).toUpperCase().indexOf(iMatch) >= 0) {
                    passed = true;
                }
            }
        }

        if(iInvert) {
                    passed = ! passed;
                }

        return passed;
    }
}
