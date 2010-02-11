/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 24-Aug-2006
 * Time: 13:38:14
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.Filter;

import java.util.*;
import java.io.IOException;
/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class implements the Taxonomy filter based on NCBI taxonomy ID's
 * for the SwissProt database.
 *
 * @author Lennart Martens
 */
public class SwissProtNCBITaxonomyFilter implements Filter {

    /**
     * This instance will convert the raw String passed in into a
     * HashMap when appropriate.
     */
    private SwissProtDBLoader iSpdb = new SwissProtDBLoader();

    /**
     * This variable holds a Collection of Strings to match the
     * 'OX' (taxonomy identifier) fields against.
     */
    private Collection iMatch;

    /**
     * This boolean flags the Boolean NOT operator for this Filter.
     */
    private boolean iInvert = false;

    /**
     * This constructor takes the match String which will be decomposed into
     * a Collection of match Strings against which a successfull
     * match must be detected for the OX fields before an entry can pass
     * the filter.
     *
     * @param   aMatchLine  String to match.
     */
    public SwissProtNCBITaxonomyFilter(String aMatchLine) {
        this.processMatchLine(aMatchLine);
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
    public SwissProtNCBITaxonomyFilter(String aMatch, boolean aInvert) {
        this.processMatchLine(aMatch);
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

        Object ox = aEntry.get("OX");
        if(ox != null) {
            for (Iterator lIterator = iMatch.iterator(); lIterator.hasNext();) {
                String match = "NCBI_TAXID=" + lIterator.next() + ";";
                if(((String)ox).toUpperCase().indexOf(match) >= 0) {
                    passed = true;
                    break;
                }
            }
        }
        // See if we need to invert.
        if(iInvert) {
            passed = ! passed;
        }

        return passed;
    }


    /**
     * This method splits the input String on the basis of
     * ',', ' ' or ';' separators into distinct Strings
     * which are added to the 'iMatch' Collection.
     *
     * @param aMatchLine    String with the input matchline.
     */
    private void processMatchLine(String aMatchLine) {
        StringTokenizer st = new StringTokenizer(aMatchLine.toUpperCase().trim(), ", ;");
        int count = st.countTokens();
        this.iMatch = new ArrayList(count);
        while(st.hasMoreTokens()) {
            iMatch.add(st.nextToken().trim());
        }
    }
}
