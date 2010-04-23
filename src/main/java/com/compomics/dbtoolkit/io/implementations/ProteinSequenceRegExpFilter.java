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

import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class ProteinSequenceRegExpFilter implements ProteinFilter {

    /**
     * This variable is the compiled regular expression to filter by for the filter.
     */
    private Pattern iRegExp = null;

    /**
     * This boolean indicates whether the results from this
     * filter should be inverted.
     */
    private boolean iInvert = false;

    /**
     * This constructor takes the String to be used as Regular Expression for this filter.
     *
     * @param   aRegExp compiled regular expression to match the sequence against.
     */
    public ProteinSequenceRegExpFilter(Pattern aRegExp) {
        this(aRegExp, false);
    }

    /**
     * This constructor takes the upper and lower mass limits for this filter
     * as well as a boolean indicative of inversion.
     *
     * @param   aRegExp compiled regular expression to match the sequence against.
     * @param   aInvert boolean to indicate whether the filter should be inverted.
     */
    public ProteinSequenceRegExpFilter(Pattern aRegExp, boolean aInvert) {
        this.iRegExp = aRegExp;
        this.setInversion(aInvert);
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

        String sequence = aProtein.getSequence().getSequence();
        Matcher sequenceMatcher = iRegExp.matcher(sequence);
        if(sequenceMatcher.find()) {
            result = true;
        }

        if(iInvert) {
            result = !result;
        }
        return result;
    }

    /**
     * This method sets the inversion flag on a ProteinFilter.
     *
     * @param   aInvert boolean to indicate whether the results from this filter should be inverted.
     */
    public void setInversion(boolean aInvert) {
        this.iInvert = aInvert;
    }
}