package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.Filter;

import java.io.IOException;
import java.util.*;

/**
 * This class implements the UniProt accession number filter for the SwissProtDatabase.
 *
 * @author Florian Reisinger
 * @since 2.8.7
 */
public class SwissProtAccessionFilter implements Filter {

//    // UniProt pattern just in case we need them
//    // ToDo: apply UniProt pattern to input accessions??
//    public final static Pattern UNIPROT_ACCESSION = Pattern.compile("[a-z][0-9][a-z0-9]{3}[0-9](?:-\\d+)?|[a-z0-9]{1,6}_[a-z0-9]{3,5}", Pattern.CASE_INSENSITIVE);
//    public final static Pattern UNIPARC_ACCESSION = Pattern.compile("upi[0-9a-f]{10}", Pattern.CASE_INSENSITIVE);
//    public final static Pattern UNIREF_ACCESSION = Pattern.compile("uniref(?:50|90|100)_[a-z0-9\\-]+", Pattern.CASE_INSENSITIVE);
//    public final static Pattern UNISAVE_ACCESSION = Pattern.compile("([A-Z][0-9][A-Z0-9]{3}[0-9])(?:[:.](\\d+))", Pattern.CASE_INSENSITIVE);
//


    /**
     * This instance will convert the raw String passed in into a
     * HashMap when appropriate.
     */
    private SwissProtDBLoader iSpdb = new SwissProtDBLoader();

    /**
     * This variable holds the List of accessions to filter the entries for.
     */
    private Set iAccList = new HashSet();

    /**
     * This boolean flags the Boolean NOT operator for this Filter.
     */
    private boolean iInvert = false;


    /**
     * This constructor takes a single String which can contain a comma separated list
     * of UniProt accession numbers.
     *
     * @param accessions a comma separated list of UniProt accessions in one single String.
     */
    public SwissProtAccessionFilter(String accessions) {
        this(accessions, false);
    }

    /**
     * This constructor takes a single String which can contain a comma separated list
     * of UniProt accession numbers.
     *
     * @param accessions a comma separated list of UniProt accessions in one single String.
     * @param aInvert boolean to indicat whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     */
    public SwissProtAccessionFilter(String accessions, boolean aInvert) {
        String[] tmp = accessions.split(",");
        for (int i = 0; i < tmp.length; i++) {
            String s = tmp[i].trim().intern();
            iAccList.add(s);
        }
        this.iInvert = aInvert;
    }

    /**
     * This constructor takes a list of UniProt accessions against which to filter
     * the entry. At least one accession of the AC field has to match for a entry
     * to pass this filter.
     *
     * @param aAccList a List of Strings, each of which is representing one UniProt accession number.
     */
    public  SwissProtAccessionFilter(List aAccList) {
      this(aAccList, false);
    }

    /**
     * This constructor takes a list of UniProt accessions against which to filter
     * the entry. At least one accession of the AC field has to match for a entry
     * to pass this filter.
     *
     * @param aAccList a List of Strings, each of which is representing one UniProt accession number.
     * @param aInvert boolean to indicat whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     */
    public  SwissProtAccessionFilter(List aAccList, boolean aInvert) {
        // check the list of accessions, take only Strings into account and we convert
        //  the list entries to upper-case just in case.
        Iterator iter = aAccList.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            String acc = null;
            if (o instanceof String) {
                acc = (String) o;
            }
            if (acc != null) {
                iAccList.add(acc.toUpperCase());
            }
        }
        this.iInvert = aInvert;
    }

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

    public boolean passesFilter(HashMap aEntry) {
        boolean passed = false;

        Object ac = aEntry.get("AC");
        if(ac != null) {
            String[] accs = ((String)ac).split(";");
            for (int i = 0; i < accs.length; i++) {
                // truncate matcher result (don't use the original String with start and stop positions)
                // get rid of leading and trailing white space
                String acc = accs[i].trim().intern();
                if (iAccList.contains(acc)) {
                    passed = true;
                }
            }
        }

        if(iInvert) {
            passed = !passed;
        }

        return passed;
    }


}
