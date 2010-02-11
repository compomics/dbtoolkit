/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-okt-02
 * Time: 11:17:15
 */
package com.compomics.dbtoolkit.gui.interfaces;



/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This interface describes the behaviour for a GUI Component class that
 * can be observed by a GUIDataReceiver.
 *
 * @author Lennart Martens
 */
public interface GUIDataSender {

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
    public abstract void addReceiver(GUIDataReceiver aReceiver);

    /**
     * This method instructs the data sender to remove the specified receiver
     * from the list of receivers to notify when appropriate.
     *
     * @param   aReceiver   GUIDataReceiver to be removed from the list.
     */
    public abstract void removeReceiver(GUIDataReceiver aReceiver);
}
