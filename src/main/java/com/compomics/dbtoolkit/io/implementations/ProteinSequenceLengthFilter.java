package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.Filter;

/**
 * Created with IntelliJ IDEA.
 * User: Lennart
 * Date: 03/09/13
 * Time: 22:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class ProteinSequenceLengthFilter implements Filter {

    /**
     * This variable is the length limit for the filter. Whether it is an upper of lower bound,
     * is defined by the boolean 'iLargerThan'.
     */
    protected int iLength = Integer.MIN_VALUE;


    /**
     * This boolean indicates whether the length limit acts as an upper bound ('false')
     * or a lower bound ('true').
     */
    protected boolean iLargerThan = false;

    /**
     * This constructor takes the upper and lower mass limits for this filter.
     *
     * @param   aLengthParameter  String with the less than/larger than character, and the length threshold
     *                              If no less than/larger than is specified, it is taken to be minimal length
     */
    public ProteinSequenceLengthFilter(String aLengthParameter) {
        String temp = aLengthParameter.trim();
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
}
