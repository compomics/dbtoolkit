/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-okt-02
 * Time: 11:10:36
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
 * This interface describes the behaviour for a class
 * that wants to be notified whenever a certain GUI
 * component has received data to send.
 *
 * @author Lennart Martens
 */
public interface GUIDataReceiver {

    /**
     * This method will be invoked by the monitored component whenever
     * it is deemed necessary by this component (e.g. : when the data
     * entered on the component is submitted).
     *
     * @param   aSource the Component issuing the call.
     * @param   aData   Object with the data the Component wishes to transmit.
     */
    public abstract void transmitData(Component aSource, Object aData);
}
