/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-sep-02
 * Time: 15:57:27
 */
package com.compomics.dbtoolkit.io.interfaces;

import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This interface describes the behaviour for a filter.
 *
 * @author Lennart Martens
 */
public interface Filter {

    /**
     * This method tests whether the entry passes the filter implementation.
     *
     * @param   aEntry  String with the raw entry to filter
     */
    public boolean passesFilter(String aEntry);

    /**
     * This method tests whether the entry passes the filter implementation.
     *
     * @param   aEntry  HashMap with the raw entry to filter.
     */
    public boolean passesFilter(HashMap aEntry);
}
