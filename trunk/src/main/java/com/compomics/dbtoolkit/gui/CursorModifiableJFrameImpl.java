/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-okt-02
 * Time: 17:10:23
 */
package com.compomics.dbtoolkit.gui;

import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;

import javax.swing.*;
import java.awt.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the standard behaviour for a CursorModifiable JFrame
 *
 * @author Lennart
 */
public class CursorModifiableJFrameImpl extends JFrame implements CursorModifiable {

    public CursorModifiableJFrameImpl(String aTitle) {
        super(aTitle);
    }

    /**
     * Call this method to set the specified type of constructor
     * on the view and all its subcomponents.
     *
     * @param   aCursor Cursor to set on the view and all subcomponents.
     */
    public void setCursorOnComponents(Cursor aCursor) {
        this.setCursorOnSubComponents(this, aCursor);
    }

    /**
     * This method sets the specified cursor on all subcomponents of the
     * specified component recursively.
     *
     * @param   aContainer   the Container on which to set the specified
     *                      cursor. All subcomponents are also subjected
     *                      to this.
     * @param   aCursor the Cursor to set on the specified JComponent and all
     *                  its subcomponents (if any).
     */
    void setCursorOnSubComponents(Container aContainer, Cursor aCursor) {
        // Get all subcomponents for the Container.
        Component[] comps = aContainer.getComponents();
        // See if there are any.
        if((comps != null) && comps.length > 0) {
            // Cycle all subcomponents.
            for(int i = 0; i < comps.length; i++) {
                Component lComp = comps[i];
                // See if it's a Container.
                if(lComp instanceof Container) {
                    // Recursively treat it to the same treatment.
                    this.setCursorOnSubComponents((JComponent)lComp, aCursor);
                } else {
                    lComp.setCursor(aCursor);
                }
            }
        }
        // Finally, set the cursor on the JComponent itself.
        aContainer.setCursor(aCursor);
    }
}
