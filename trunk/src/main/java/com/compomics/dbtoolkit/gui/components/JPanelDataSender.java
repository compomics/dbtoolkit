/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-okt-02
 * Time: 11:15:46
 */
package com.compomics.dbtoolkit.gui.components;

import com.compomics.dbtoolkit.gui.interfaces.GUIDataReceiver;
import com.compomics.dbtoolkit.gui.interfaces.GUIDataSender;

import javax.swing.*;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the default behaviour for a JPanel
 * that wants to be observed by a GUIDataReceiver.
 *
 * @author Lennart Martens
 */
public abstract class JPanelDataSender extends JPanel implements GUIDataSender {

    /**
     * This Vector holds the list of receivers to be notified.
     */
    private Vector iReceivers = null;

    /**
     * This method allows the caller to add a GUIDataReceiver
     * to the list of receivers currently informed by the GUIDataSender.
     * The order of addition is respected in the notification
     * process, such that the first component added is the first to be notified
     * of change.
     *
     * @param   aReceiver   GUIDataReceiver that wants to be added
     *                      to the list of informed observers of this
     *                      GUIDataSender.
     */
    public void addReceiver(GUIDataReceiver aReceiver) {
        // See if we already have a list, and if not - make one.
        if(iReceivers == null) {
            iReceivers = new Vector();
        }
        iReceivers.add(aReceiver);
    }

    /**
     * This method instructs the data sender to remove the specified receiver
     * from the list of receivers to notify when appropriate.
     *
     * @param   aReceiver   GUIDataReceiver to be removed from the list.
     */
    public void removeReceiver(GUIDataReceiver aReceiver) {
        // First see if there is anything to remove first.
        if(iReceivers != null) {
            iReceivers.remove(aReceiver);
        }
    }

    /**
     * This method allows the caller (typically a sbuclass) to inform
     * all receivers of a certain piece of information.
     *
     * @param   aData   Object with the data to transmit to the receivers.
     */
    protected void notifyReceivers(Object aData) {
        int liSize = iReceivers.size();
        for(int i=0;i<liSize;i++) {
            ((GUIDataReceiver)iReceivers.get(i)).transmitData(this, aData);
        }
    }
}
