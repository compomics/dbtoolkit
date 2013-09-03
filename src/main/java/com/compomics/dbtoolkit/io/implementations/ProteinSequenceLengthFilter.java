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
import org.apache.batik.script.rhino.WindowWrapper;

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
public class ProteinSequenceLengthFilter implements ProteinFilter {

    /**
     * This variable is the length limit for the filter. Whether it is an upper of lower bound,
     * is defined by the boolean 'iLargerThan'.
     */
    private int iLength = Integer.MIN_VALUE;


    /**
     * This boolean indicates whether the length limit acts as an upper bound ('false')
     * or a lower bound ('true').
     */
    private boolean iLargerThan = false;

    /**
     * This constructor takes the upper and lower mass limits for this filter.
     *
     * @param   aParameter  String with the less than/larger than character, and the length threshold
     *                              If no less than/larger than is specified, it is taken to be minimal length
     */
    public ProteinSequenceLengthFilter(String aParameter) {
        String temp = aParameter.trim();
        String firstChar = temp.substring(0, 1);
        boolean largerThan = true;
        if(firstChar.equals("<")) {
            largerThan = false;
            temp = temp.substring(1);
        } else if(!firstChar.matches("[0-9]")) {
            temp = temp.substring(1);
        }
        int length = Integer.parseInt(temp);
        this.iLength = length;
        this.iLargerThan = largerThan;
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

        int sequenceLength = aProtein.getSequence().getLength();

        if(iLargerThan && sequenceLength >= iLength) {
            result = true;
        } else if(!iLargerThan && sequenceLength <= iLength) {
            result = true;
        }

        return result;
    }

    public void setInversion(boolean aInvert) {
        iLargerThan = !aInvert;
    }
}
