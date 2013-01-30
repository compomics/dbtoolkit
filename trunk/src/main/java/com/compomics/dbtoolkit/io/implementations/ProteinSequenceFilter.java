/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-okt-02
 * Time: 11:36:12
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;

import java.util.regex.Pattern;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the ProteinFilter interface for a sequence-based
 * filtering.
 *
 * @author Lennart Martens
 */
public class ProteinSequenceFilter implements ProteinFilter {

    /**
     * This method holds the sequence element to match (can be
     * a single residu or a stretch).
     */
    private String iSequenceMatch = null;

    /**
     * Patterns are only used for String queries that contain '.'.
     */
    private Pattern iPattern = null;

    /**
     * This boolean indicates whether the sequence element should be present
     * for a pass ('true'), or should be absent for a pass('false').
     */
    private boolean iInclusive = true;

    /**
     * This boolean indicates (for a Met filter) whether the initiator Met counts.
     */
    private boolean iInitMetCounts = true;

    /**
     * This constructor takes the residu(es) to match. Some parsing is done -
     * if the sequence is prefixed with '!', a passing sequence will NOT contain the
     * sequence, otherwise the sequence is required to be present in the Protein sequence
     * for a pass.
     *
     * @param   aCodedSequence  String with the sequence to match for a pass, or, when
     *                          prefixed with a '!', to be absent for a pass
     */
    public ProteinSequenceFilter(String aCodedSequence) {
        // Check for inversion flag.
        while(aCodedSequence.startsWith("!")) {
            aCodedSequence = aCodedSequence.substring(1);
            iInclusive = false;
        }

        // Check for 'U' flag (Met without initiator).
        if(aCodedSequence.indexOf("U") >= 0) {
            iInitMetCounts = false;
            aCodedSequence = aCodedSequence.replace('U', 'M');
        }
        this.iSequenceMatch = aCodedSequence;
        // See if regexp is required.
        if(iSequenceMatch.indexOf(".") >= 0) {
            iPattern = Pattern.compile(iSequenceMatch);
        }
    }

    /**
     * This method sets the inclusion flag on a ProteinFilter.
     *
     * @param   aInvert boolean to indicate whether the results from this filter
     *                  should be included ('true') or excluded ('false').
     */
    public void setInversion(boolean aInvert) {
        this.iInclusive = aInvert;
    }

    /**
     * This method returns a flag that indicates whether the specified instance
     * passes the filter.
     *
     * @param   aProtein    Protein instance to check against the filter.
     * @return  boolean 'true' if the specified Protein passes the filter, 'false' otherwise.
     */
    public boolean passesFilter(Protein aProtein) {
        boolean result = false;

        String toVerify = aProtein.getSequence().getSequence();
        // See if initMet counts. If it does not and the sequence
        // starts with methionine, we need to kick it out first.
        if((!iInitMetCounts) && toVerify.startsWith("M")) {
            toVerify = toVerify.substring(1);
        }
        // Check whether we should do regular expression matching or normal matching.
        if(iPattern != null) {
            if(iPattern.matcher(toVerify).find()) {
                result = true;
            }
        } else {
            // See if the sequence to match is present.
            if(toVerify.indexOf(this.iSequenceMatch) >= 0) {
                result = true;
            }
        }

        // See if we are inclusive (nothing changes) or
        // exclusive (we invert the result).
        if(!iInclusive) {
            result = !result;
        }


        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return  a string representation of the object.
     */
    public String toString() {
        StringBuffer lsb = new StringBuffer("ProteinSequenceFilter for the ");

        if(this.iInclusive) {
            lsb.append("presence");
        } else {
            lsb.append("absence");
        }

        lsb.append(" of " + this.iSequenceMatch);
        if(!iInitMetCounts) {
            lsb.append(" (in which initiator methionines are considered cleaved off)");
        }

        lsb.append(".");

        return lsb.toString();
    }
}
