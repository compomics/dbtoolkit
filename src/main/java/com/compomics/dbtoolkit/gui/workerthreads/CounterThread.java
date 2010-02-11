/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 16-okt-02
 * Time: 16:42:17
 */
package com.compomics.dbtoolkit.gui.workerthreads;

import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;

/*
 * CVS information:
 *
 * $Revision: 1.6 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a Thread-ready way of counting the number
 * of entries in a DB while displaying a Progress Bar.
 *
 * @author Lennart Martens
 */
public class CounterThread implements Runnable {

    /**
     * The parent will be the owner of the ProgressBar.
     */
    private JFrame iParent = null;

    /**
     * The DBLoader instance is where the entrycount will originate from.
     */
    DBLoader iLoader = null;

    /**
     * The ProgressMonitor.
     */
    private ProgressMonitor iMonitor = null;

    /**
     * This String holds the database file name.
     */
    private String iDBName = null;

    /**
     * This constructor takes a JFrame as owner, a DBLoader to
     * count the entries from and a database filename as parameters.
     *
     * @param   aParent JFrame with the owner for the ProgressBar.
     * @param   aLoader DBLoader implementation instance to request
     *                  the entrycount from.
     * @param   aDBName String with the filename for the DB.
     */
    public CounterThread(JFrame aParent, DBLoader aLoader, String aDBName) {
        this.iParent = aParent;
        this.iLoader = aLoader;
        this.iDBName = aDBName;
    }

    /**
     * The method that does all the work, once 'start' is called on the
     * owner Thread.
     */
    public void run() {
        try {
            if(iParent instanceof CursorModifiable) {
                ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
            } else {
                iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            if(iParent instanceof StatusView) {
                ((StatusView)iParent).setStatus("Started counting entries in database '" + iDBName + "'...");
            }

            // Get the maximum.
            final int max = iLoader.getMaximum();

            // The progress monitor dialog.
            iMonitor = new ProgressMonitor(iParent, "Counting entries in database '" + iDBName + "'...", "Initializing...", 0, max+1);
            iMonitor.setMillisToDecideToPopup(0);
            iMonitor.setMillisToPopup(0);
            iMonitor.setNote("Counting...");
            iMonitor.setProgress(1);

            final Timer t = new Timer(500, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Show it on the progressbar.
                    int progress = iLoader.monitorProgress();
                    // Show it on the progressbar.
                    if(progress < iMonitor.getMaximum()) {
                        iMonitor.setProgress(progress);
                    } else {
                        int delta = progress-iMonitor.getMaximum();
                        double modulo = delta/1024;
                        String affix = "KB";
                        if(modulo%5 == 0.0) {
                            double temp = modulo/1024;
                            if(temp > 1.0) {
                                modulo = temp;
                                affix = "MB";
                            }
                            iMonitor.setNote("Reading from buffer (" + new BigDecimal(modulo).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + affix + ")...");
                        }
                    }
                    // See if the user pressed cancel.
                    if(iMonitor.isCanceled()) {
                        iLoader.cancelCount();
                        iMonitor.setNote("Cancelling counting operation...");
                    }
                }
            });
            // Don't forget to start timer.
            t.start();
            // The count itself.
            long count = iLoader.countNumberOfEntries();
            // Stopping timer.
            t.stop();
            // Close the progress monitor.
            iMonitor.setProgress(iMonitor.getMaximum());
            iMonitor.close();
            // BEEP!
            //Toolkit.getDefaultToolkit().beep();
            if(iParent instanceof CursorModifiable) {
                ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            iParent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            // See if cancel was pressed,
            // and if so, delete the output file!
            String status = null;
            if(count == DBLoader.CANCELLEDCOUNT) {
                status = "Count in database '" + iDBName + "' was cancelled by user.";
            } else {
                status = "Counted " + count + " entries in database '" + iDBName + "'.";
            }

            // If the parent component is capable of displaying status messages,
            // set one!
            if(iParent instanceof StatusView) {
                ((StatusView)iParent).setStatus(status);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
