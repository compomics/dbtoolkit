/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-okt-02
 * Time: 11:33:33
 */
package com.compomics.dbtoolkit.io.interfaces;

import com.compomics.util.protein.Protein;


/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This interface describes the behaviour for a Filter that is based on a Protein
 * instance.
 *
 * @author Lennart Martens
 */
public interface ProteinFilter {

    /**
     * This method returns a flag that indicates whether the specified instance
     * passes the filter.
     *
     * @param   aProtein    Protein instance to check against the filter.
     * @return  boolean 'true' if the specified Protein passes the filter, 'false' otherwise.
     */
    public boolean passesFilter(Protein aProtein);

    /**
     * This method sets the inversion flag on a ProteinFilter.
     *
     * @param   aInvert boolean to indicate whether the results from this filter should be inverted.
     */
    public void setInversion(boolean aInvert);
}
