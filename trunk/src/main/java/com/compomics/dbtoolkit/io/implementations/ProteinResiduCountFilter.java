/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 7-nov-02
 * Time: 9:14:23
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a ProteinFilter that filters on the number of occurrences
 * of a specified residu (or stretch) in the sequence.
 *
 * @author Lennart Martens
 */
public class ProteinResiduCountFilter implements ProteinFilter {

    /**
     * The count to filter on.
     */
    private int iCount = 0;

    /**
     * The mode in which to compare the count.
     */
    private int iMode = 0;

    /**
     * The residu (or stretch) to count occurrances for.
     */
    private String iResidu = null;

    /**
     * Boolean to indicate inversion.
     */
    private boolean iInvert = false;

    /**
     * Boolean to indicate whether initiator methionines should be used.
     */
    private boolean iInitMetCounts = true;

    /**
     * Variable to denote the 'GREATER THAN' mode.
     */
    public static final int GREATER_THAN = 1;

    /**
     * Variable to denote the 'LESS THAN' mode.
     */
    public static final int LESS_THAN = 2;

    /**
     * Variable to denote the 'EQUALS_TO' mode.
     */
    public static final int EQUALS_TO = 3;


    /**
     * This constructor takes two parameters:
     * the count, and the mode in which to compare the
     * counts.
     *
     * @param   aResidu String with the residu (or stretch) to count
     *                  occurrences for.
     * @param   aCount  int with the count to compare the number of occurrances to.
     * @param   aMode   int with the mode in which comparison is to take place.
     *                  <b>Please use only ints defined as constants on this class!</b>
     */
    public ProteinResiduCountFilter(String aResidu, int aCount, int aMode) {
        this(aResidu, aCount, aMode, false);
    }

    /**
     * This constructor takes two parameters:
     * the count, the mode in which to compare the
     * counts and a boolean to indicate inversion.
     *
     * @param   aResidu String with the residu (or stretch) to count
     *                  occurrences for.
     * @param   aCount  int with the count to compare the number of occurrances to.
     * @param   aMode   int with the mode in which comparison is to take place.
     *                  <b>Please use only ints defined as constants on this class!</b>
     * @param   aInvert boolean to indicate inversion.
     */
    public ProteinResiduCountFilter(String aResidu, int aCount, int aMode, boolean aInvert) {
        // Check for initmets.
        if(aResidu.indexOf("U") >= 0) {
            this.iInitMetCounts = false;
            aResidu = aResidu.replace('U', 'M');
        } else {
            this.iInitMetCounts = true;
        }
        this.iResidu = aResidu;
        this.iCount = aCount;
        this.iMode = aMode;
        this.iInvert = aInvert;
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
        // The counter itself.
        int counter = 0;
        // Helper to avoid endless loop of finding the same residu.
        int index = -1;
        int tempIndex = -1;

        // Count all occurrances.
        String sequence = aProtein.getSequence().getSequence();
        // See if we must cut away the init Met.
        // If so, do so.
        if(!iInitMetCounts && sequence.startsWith("M")) {
            sequence = sequence.substring(1);
        }
        while((index = sequence.indexOf(iResidu, tempIndex)) >= 0) {
            counter++;
            tempIndex = index + 1;
        }

        // Okay, we now have the count. See how we must compare...
        switch(iMode) {
            case EQUALS_TO:
                if(counter == iCount) {
                    result = true;
                }
                break;
            case GREATER_THAN:
                if(counter > iCount) {
                    result = true;
                }
                break;
            case LESS_THAN:
                if(counter < iCount) {
                    result = true;
                }
                break;
        }

        // See if we must invert.
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

       /**
     * Returns a string representation of the object.
     *
     * @return  a string representation of the object.
     */
    public String toString() {
        StringBuffer lsb = new StringBuffer("ProteinResiduCountFilter for  ");
        if(!this.iInvert) {
            if(this.iMode == EQUALS_TO) {
                lsb.append("an exact number of ");
            } else if(this.iMode == GREATER_THAN) {
                lsb.append("strictly more than ");
            } else if(this.iMode == LESS_THAN) {
                lsb.append("strictly less than ");
            }
        } else {
            if(this.iMode == EQUALS_TO) {
                lsb.append("a number different from ");
            } else if(this.iMode == GREATER_THAN) {
                lsb.append("equal to or less than ");
            } else if(this.iMode == LESS_THAN) {
                lsb.append("equals to or greater than ");
            }
        }
        lsb.append(this.iCount + " residu" + (((this.iCount == 0) || (this.iCount > 1))?"es":""));
        if(!this.iInitMetCounts) {
            lsb.append(" (in which initiator methionines are considered to be cleaved off)");
        }
        lsb.append(".");

        return lsb.toString();
    }
}
