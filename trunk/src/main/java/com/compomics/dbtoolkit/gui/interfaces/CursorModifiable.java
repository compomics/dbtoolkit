/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-okt-02
 * Time: 17:08:04
 */
package com.compomics.dbtoolkit.gui.interfaces;

import java.awt.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This interface describes the behaviour for a view class that can recursively
 * set a cursor type on all its subcomponents.
 *
 * @author Lennart Martens
 */
public interface CursorModifiable {

    /**
     * Call this method to set the specified type of constructor
     * on the view and all its subcomponents.
     *
     * @param   aCursor Cursor to set on the view and all subcomponents.
     */
    public abstract void setCursorOnComponents(Cursor aCursor);
}
