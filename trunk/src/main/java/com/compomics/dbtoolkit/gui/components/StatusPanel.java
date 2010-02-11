/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-okt-02
 * Time: 10:15:58
 */
package com.compomics.dbtoolkit.gui.components;

import com.compomics.dbtoolkit.gui.interfaces.StatusView;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a status JPanel that
 * can be located on any screen to indicate the
 * status of the application. It displays
 * a 'state' and 'error' line on which
 * messages may be specified.
 *
 * @author Lennart Martens
 */
public class StatusPanel extends JPanel implements StatusView {

    /**
     * The JLabel holding the state information.
     * Used when NOT in log mode.
     */
    private JLabel lblState = null;

    /**
     * The JLabel holding the error information.
     * Used when NOT in log mode.
     */
    private JLabel lblError = null;

    /**
     * The JTextArea used for holding the state log.
     * Used when in log mode.
     */
    private JTextArea txtState = null;

    /**
     * The JTextArea used for holding the error log.
     * Used when in log mode.
     */
    private JTextArea txtError = null;

    /**
     * The JLabel for the 'Status : ' label.
     */
    JLabel lblStatus = null;

    /**
     * The JLabel for the 'Error : ' label.
     */
    JLabel lblErrors = null;

    /**
     * The boolean that indicates whether a log is being kept.
     */
    private boolean iLog = false;

    /**
     * Formatter for the timestamp when in logging mode.
     */
    private SimpleDateFormat iSDF = null;

    /**
     * The constructor takes care of creating and locating
     * all components plus sets a default tooltip text.
     */
    public StatusPanel() {
        this("Status report", true, true);
    }

    /**
     * The constructor takes care of creating and locating
     * all components plus sets a default tooltip text.
     * It is also possible to bypass the adding of a Titled border.
     *
     * @param   aBorder boolean to indicate whether a border should be added.
     */
    public StatusPanel(boolean aBorder) {
        this("Status report", true, aBorder);
    }

    /**
     * The constructor takes care of creating and
     * locating all components, while setting the
     * specified tooltip text. The user must indicate whether a log
     * of the messages is required, or whether a single line will
     * suffice.
     * It is also possible to bypass the adding of a Titled border.
     *
     * @param   aToolTip    String with the ToolTip to set on the Panel.
     * @param   aLog    boolean to indicate whether a log is necessary.
     * @param   aBorder boolean to indicate whether a border should be added.
     */
    public StatusPanel(String aToolTip, boolean aLog, boolean aBorder) {
        super();
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Set the log mode.
        this.iLog = aLog;

        JPanel status = new JPanel(new BorderLayout());
        lblStatus = new JLabel(" Status : ", JLabel.LEFT);
        lblStatus.setForeground(Color.blue);
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD));
        if(!iLog) {
            lblState = new JLabel("", JLabel.LEFT);
            lblState.setForeground(Color.blue);
            status.add(lblStatus, BorderLayout.WEST);
            status.add(lblState, BorderLayout.CENTER);
        } else {
            txtState = new JTextArea(3, 15);
            txtState.setForeground(Color.blue);
            txtState.setEditable(false);
            status.add(lblStatus, BorderLayout.NORTH);
            status.add(new JScrollPane(txtState), BorderLayout.CENTER);
        }

        JPanel error = new JPanel(new BorderLayout());
        lblErrors = new JLabel(" Errors : ", JLabel.LEFT);
        lblErrors.setForeground(Color.red);
        lblErrors.setFont(lblErrors.getFont().deriveFont(Font.BOLD));
        if(!iLog) {
            lblError = new JLabel("", JLabel.LEFT);
            lblError.setForeground(Color.red);
            error.add(lblErrors, BorderLayout.WEST);
            error.add(lblError, BorderLayout.CENTER);
        } else {
            txtError = new JTextArea(3, 15);
            txtError.setForeground(Color.red);
            txtError.setEditable(false);
            error.add(lblErrors, BorderLayout.NORTH);
            error.add(new JScrollPane(txtError), BorderLayout.CENTER);
        }

        if(aBorder) {
            this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        }
        this.setToolTipText(aToolTip);
        this.add(status);
        this.add(Box.createRigidArea(new Dimension(status.getWidth(), 5)));
        this.add(error);
        this.iSDF = new SimpleDateFormat("dd-MM-yyyy (HH:mm:ss) : ");
    }

    /**
     * This method allows the caller to specify the status message
     * that is being displayed.
     *
     * @param   aState  String with the desired status message.
     */
    public void setStatus(String aState) {
        if(!iLog) {
            this.lblState.setText(aState);
        } else {
            this.txtState.append(this.getTimestamp() + aState + "\n");
            this.txtState.setCaretPosition(txtState.getText().length());
        }
    }

    /**
     * This method allows the caller to specify the error message
     * that is being displayed.
     *
     * @param   aError  String with the desired error message.
     */
    public void setError(String aError) {
        if(!iLog) {
            this.lblError.setText(aError);
        } else {
            this.txtError.append(this.getTimestamp() + aError + "\n");
            this.txtError.setCaretPosition(txtError.getText().length());
        }
    }

    /**
     * This method allows the caller to specify the color of
     * the status line. (Default is blue).
     *
     * @param   aColor  the Color for the status line.
     */
    public void setStatusColor(Color aColor) {
        lblStatus.setForeground(aColor);
        if(!iLog) {
            lblState.setForeground(aColor);
        } else {
            txtState.setForeground(aColor);
        }
    }

    /**
     * This method allows the caller to specify the color of
     * the error line. (Default is red).
     *
     * @param   aColor  the Color for the error line.
     */
    public void setErrorColor(Color aColor) {
        lblErrors.setForeground(aColor);
        if(!iLog) {
            lblError.setForeground(aColor);
        } else {
            txtError.setForeground(aColor);
        }
    }

    /**
     * This method returns a timestamp.
     * Format is: 'dd-MM-yyyy (HH:mm:ss) : '
     *
     * @return  String  with the timestamp, formatted as
     *                  'dd-MM-yyyy (HH:mm:ss) : '.
     */
    private String getTimestamp() {
        return iSDF.format(new Date());
    }
}
