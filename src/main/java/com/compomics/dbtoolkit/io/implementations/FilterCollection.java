/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 30-sep-02
 * Time: 13:24:12
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.Filter;

import java.util.HashMap;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class 
 *
 * @author Lennart
 */
public class FilterCollection extends Vector implements Filter {

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
     * This boolean flags the Boolean NOT operator for this Filter.
     */
    private boolean iInvert = false;

    /**
     * The constructor requires that the mode in which the added Filters are evaluated
     * is specified. Please use only the modes defined as static final ints on this class.
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     */
    public FilterCollection(int aMode) {
        super();
        this.iMode = aMode;
    }

    /**
     * This constructor allows the setting of the initial capacity, along with the capacity
     * increment for this Collection, and requires the mode in which the added Filters are evaluated.
     * Please use only the modes defined as static final ints on this class.
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     * @param   aInitialCapacity    int with the initial capacity for the Collection.
     * @param   aCapacityIncrement  int with the capacity increment for the Collection.
     */
    public FilterCollection(int aMode, int aInitialCapacity, int aCapacityIncrement) {
        super(aInitialCapacity, aCapacityIncrement);
        this.iMode = aMode;
    }

    /**
     * The constructor requires that the mode in which the added Filters are evaluated
     * is specified. Please use only the modes defined as static final ints on this class.
     * Additionally, it can also be specified whether this Filters results have to be inverted
     * (NOT operator).
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     * @param   aInvert boolean to indicate whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     */
    public FilterCollection(int aMode, boolean aInvert) {
        super();
        this.iMode = aMode;
        this.iInvert = aInvert;
    }

    /**
     * This constructor allows the setting of the initial capacity, along with the capacity
     * increment for this Collection, and requires the mode in which the added Filters are evaluated.
     * Please use only the modes defined as static final ints on this class.
     *
     * @param   aMode   int with the code for the mode as defined by the final variables
     *                  on this class.
     * @param   aInvert boolean to indicate whether to apply the Boolean 'NOT' operator to the
     *                  results of the Filter.
     * @param   aInitialCapacity    int with the initial capacity for the Collection.
     * @param   aCapacityIncrement  int with the capacity increment for the Collection.
     */
    public FilterCollection(int aMode, boolean aInvert, int aInitialCapacity, int aCapacityIncrement) {
        super(aInitialCapacity, aCapacityIncrement);
        this.iMode = aMode;
        this.iInvert = aInvert;
    }

    /**
     * This method tests whether the entry passes the filter implementation.
     *
     * @param   aEntry  String with the raw entry to filter
     */
    public boolean passesFilter(String aEntry) {
        boolean result = false;

        switch(iMode) {
            case FilterCollection.AND:
                result = this.evaluateANDFilters(aEntry);
                break;
            case FilterCollection.OR:
                result = this.evaluateORFilters(aEntry);
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
     * This method tests whether the entry passes the filter implementation.
     *
     * @param   aEntry  HashMap with the raw entry to filter.
     */
    public boolean passesFilter(HashMap aEntry) {
        boolean result = false;

        switch(iMode) {
            case FilterCollection.AND:
                result = this.evaluateANDFilters(aEntry);
                break;
            case FilterCollection.OR:
                result = this.evaluateORFilters(aEntry);
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
     * This method will evaluate the given entry against all listed
     * Filters, joining their results by boolean AND operator.
     *
     * @param   aEntry  String with the raw entry to filter.
     * @return  boolean with the result of the operation (Filter1(entry) AND Filter2(entry) AND ...).
     */
    private boolean evaluateANDFilters(String aEntry) {
        boolean result = true;
        int liSize = this.size();
        for(int i=0;i<liSize;i++) {
            Object temp = this.get(i);
            if(temp instanceof Filter) {
                Filter f = (Filter)temp;
                if(!f.passesFilter(aEntry)) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * This method will evaluate the given entry against all listed
     * Filters, joining their results by boolean AND operator.
     *
     * @param   aEntry  HashMap with the raw entry to filter.
     * @return  boolean with the result of the operation (Filter1(entry) AND Filter2(entry) AND ...).
     */
    private boolean evaluateANDFilters(HashMap aEntry) {
        boolean result = true;
        int liSize = this.size();
        for(int i=0;i<liSize;i++) {
            Object temp = this.get(i);
            if(temp instanceof Filter) {
                Filter f = (Filter)temp;
                if(!f.passesFilter(aEntry)) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * This method will evaluate the given entry against all listed
     * Filters, joining their results by boolean OR operator.
     *
     * @param   aEntry  String with the raw entry to filter.
     * @return  boolean with the result of the operation (Filter1(entry) OR Filter2(entry) OR ...).
     */
    private boolean evaluateORFilters(String aEntry) {
        boolean result = false;

        int liSize = this.size();
        for(int i=0;i<liSize;i++) {
            Object temp = this.get(i);
            if(temp instanceof Filter) {
                Filter f = (Filter)temp;
                if(f.passesFilter(aEntry)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * This method will evaluate the given entry against all listed
     * Filters, joining their results by boolean OR operator.
     *
     * @param   aEntry  HashMap with the raw entry to filter.
     * @return  boolean with the result of the operation (Filter1(entry) OR Filter2(entry) OR ...).
     */
    private boolean evaluateORFilters(HashMap aEntry) {
        boolean result = false;

        int liSize = this.size();
        for(int i=0;i<liSize;i++) {
            Object temp = this.get(i);
            if(temp instanceof Filter) {
                Filter f = (Filter)temp;
                if(f.passesFilter(aEntry)) {
                    result = true;
                }
            }
        }
        return result;
    }
}
