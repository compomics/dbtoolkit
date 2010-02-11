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
public class ProteinMassFilter implements ProteinFilter {

    /**
     * This variable is the lower mass limit for the filter.
     */
    private double iLower = Double.MIN_VALUE;

    /**
     * This variable is the upper mass limit for the filter.
     */
    private double iUpper = Double.MAX_VALUE;

    /**
     * This boolean indicates whether the results from this
     * filter should be inverted.
     */
    private boolean iInvert = false;

    /**
     * This constructor takes the upper and lower mass limits for this filter.
     *
     * @param   aLower  double with the lower mass treshold
     * @param   aUpper  double with the upper mass treshold
     */
    public ProteinMassFilter(double aLower, double aUpper) {
        this(aLower, aUpper, false);
    }

    /**
     * This constructor takes the upper and lower mass limits for this filter
     * as well as a boolean indicative of inversion.
     *
     * @param   aLower  double with the lower mass treshold
     * @param   aUpper  double with the upper mass treshold
     * @param   aInvert boolean to indicate whether the filter should be inverted.
     */
    public ProteinMassFilter(double aLower, double aUpper, boolean aInvert) {
        this.iLower = aLower;
        this.iUpper = aUpper;
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

        double tempMass = aProtein.getMass();
        if((tempMass <= iUpper) && (tempMass >= iLower)) {
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
