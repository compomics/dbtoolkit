/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-okt-02
 * Time: 11:55:35
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.Protein;

import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a Collection of ProteinFilters.
 *
 * @author Lennart Martens
 */
public class ProteinFilterCollection extends Vector implements ProteinFilter {

    /**
     * This variable can be passed to the constructor to indicate a Boolean AND
     * realtionship between elements in this collection.
     */
    public static final int AND = 0;

    /**
     * This variable can be passed to the constructor to indicate a Boolean OR
     * realtionship between elements in this collection.
     */
    public static final int OR = 1;

    /**
     * This variable keeps track of the mode this collection is in.
     */
    private int iMode = -1;

    /**
     * This boolean flags the Boolean NOT operator for this ProteinFilter.
     */
    private boolean iInvert = false;

    /**
     * The constructor requires that the mode in which the added ProteinFilters are evaluated
     * is specified. Please use only the modes defined as static final ints on this class.
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     */
    public ProteinFilterCollection(int aMode) {
        super();
        this.iMode = aMode;
    }

    /**
     * This constructor allows the setting of the initial capacity, along with the capacity
     * increment for this Collection, and requires the mode in which the added ProteinFilters are evaluated.
     * Please use only the modes defined as static final ints on this class.
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     * @param   aInitialCapacity    int with the initial capacity for the Collection.
     * @param   aCapacityIncrement  int with the capacity increment for the Collection.
     */
    public ProteinFilterCollection(int aMode, int aInitialCapacity, int aCapacityIncrement) {
        super(aInitialCapacity, aCapacityIncrement);
        this.iMode = aMode;
    }

    /**
     * The constructor requires that the mode in which the added ProteinFilters are evaluated
     * is specified. Please use only the modes defined as static final ints on this class.
     * Additionally, it can also be specified whether this ProteinFilters results have to be inverted
     * (NOT operator).
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     * @param   aInvert boolean to indicate whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     */
    public ProteinFilterCollection(int aMode, boolean aInvert) {
        super();
        this.iMode = aMode;
        this.iInvert = aInvert;
    }

    /**
     * This constructor allows the setting of the initial capacity, along with the capacity
     * increment for this Collection, and requires the mode in which the added ProteinFilters are evaluated.
     * Please use only the modes defined as static final ints on this class.
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     * @param   aInvert boolean to indicate whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     * @param   aInitialCapacity    int with the initial capacity for the Collection.
     * @param   aCapacityIncrement  int with the capacity increment for the Collection.
     */
    public ProteinFilterCollection(int aMode, boolean aInvert, int aInitialCapacity, int aCapacityIncrement) {
        super(aInitialCapacity, aCapacityIncrement);
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

        switch(iMode) {
            case FilterCollection.AND:
                result = this.evaluateANDFilters(aProtein);
                break;
            case FilterCollection.OR:
                result = this.evaluateORFilters(aProtein);
                break;
            default:
                result = false;
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

    /**
     * This method will evaluate the given Protein against all listed
     * ProteinFilters, joining their results by boolean AND operator.
     *
     * @param   aProtein  Protein with the protein to filter.
     * @return  boolean with the result of the operation (Filter1(entry) AND Filter2(entry) AND ...).
     */
    private boolean evaluateANDFilters(Protein aProtein) {
        boolean result = true;
        int liSize = this.size();
        for(int i=0;i<liSize;i++) {
            Object temp = this.get(i);
            if(temp instanceof ProteinFilter) {
                ProteinFilter f = (ProteinFilter)temp;
                if(!f.passesFilter(aProtein)) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * This method will evaluate the given entry against all listed
     * ProteinFilters, joining their results by boolean OR operator.
     *
     * @param   aProtein  Protein with the protein to filter.
     * @return  boolean with the result of the operation (Filter1(entry) OR Filter2(entry) OR ...).
     */
    private boolean evaluateORFilters(Protein aProtein) {
        boolean result = false;

        int liSize = this.size();
        for(int i=0;i<liSize;i++) {
            Object temp = this.get(i);
            if(temp instanceof ProteinFilter) {
                ProteinFilter f = (ProteinFilter)temp;
                if(f.passesFilter(aProtein)) {
                    result = true;
                }
            }
        }
        return result;
    }
}
