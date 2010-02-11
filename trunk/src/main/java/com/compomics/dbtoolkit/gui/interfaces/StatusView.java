/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 16-okt-02
 * Time: 14:54:57
 */
package com.compomics.dbtoolkit.gui.interfaces;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This interface describes the behaviour for any view capabale of presenting a status,
 * status being defined as a true status and an error message.
 *
 * @author Lennart Martens
 */
public interface StatusView {

    /**
     * This method allows the caller to specify the status message
     * that is being displayed.
     *
     * @param   aStatus  String with the desired status message.
     */
    public abstract void setStatus(String aStatus);

    /**
     * This method allows the caller to specify the error message
     * that is being displayed.
     *
     * @param   aError  String with the desired error message.
     */
    public abstract void setError(String aError);
}
