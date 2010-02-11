/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 21-okt-02
 * Time: 15:15:40
 */
package com.compomics.dbtoolkit.gui.workerthreads;

import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;
import com.compomics.util.io.MonitorableFileInputStream;
import com.compomics.util.io.MonitorableInputStream;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a Threading implementation to concatenate two files into a new, third file.
 * It can also be used as a file copier if the only one source file is specified.
 *
 * @author Lennart Martens
 */
public class ConcatenateThread implements Runnable {

    /**
     * The first source file to read from.
     */
    private File iSource1 = null;

    /**
     * The second source file to read from.
     */
    private File iSource2 = null;

    /**
     * The destination file to append the contents of the source file to.
     */
    private File iDestination = null;

    /**
     * A possible parent (for GUI mode).
     */
    private JFrame iParent = null;

    /**
     * A possible progressmonitor (GUI mode).
     */
    private ProgressMonitor iMonitor = null;


    /**
     * This constructor takes the source file and the destination file.
     * Constructing the instance with this constructing results in silent text mode.
     *
     * @param   aSource File with the source file to read from. This file should exist!
     * @param   aDestination    File to copy to. This file should already exist!
     */
    public ConcatenateThread(File aSource, File aDestination) {
        this(aSource, aDestination, (JFrame)null);
    }

    /**
     * This constructor takes the source file and the destination file, as well as a JFrame GUI parent
     * component. Calling this constructor results in a GUI mode of operation for the Thread, which results
     * in the displaying of a progressbar.
     *
     * @param   aSource File with the source file to read from. This file should exist!
     * @param   aDestination    File with the destination file to append to. This file should exist!
     * @param   aParent JFrame that acts as a GUI parent for this Thread (and specifically: its progress monitor).
     */
    public ConcatenateThread(File aSource, File aDestination, JFrame aParent) {
        this.iSource1 = aSource;
        this.iDestination = aDestination;
        this.iParent = aParent;
    }

    /**
     * This constructor takes the two source files and the destination file.
     * Constructing the instance with this constructing results in silent text mode.
     *
     * @param   aSource1 File with the first source file to read from. This file should exist!
     * @param   aSource2 File with the second source file to read from. This file should exist!
     * @param   aDestination    File to copy to. This file should already exist!
     */
    public ConcatenateThread(File aSource1, File aSource2, File aDestination) {
        this(aSource1, aSource2, aDestination, null);
    }

    /**
     * This constructor takes the two source files and the destination file, as well as a JFrame GUI parent
     * component. Calling this constructor results in a GUI mode of operation for the Thread, which results
     * in the displaying of a progressbar.
     *
     * @param   aSource1 File with the first source file to read from. This file should exist!
     * @param   aSource2 File with the second source file to read from. This file should exist!
     * @param   aDestination    File with the destination file to append to. This file should exist!
     * @param   aParent JFrame that acts as a GUI parent for this Thread (and specifically: its progress monitor).
     */
    public ConcatenateThread(File aSource1, File aSource2, File aDestination, JFrame aParent) {
        this.iSource1 = aSource1;
        this.iSource2 = aSource2;
        this.iDestination = aDestination;
        this.iParent = aParent;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see     java.lang.Thread#run()
     */
    public void run() {
        if(this.iSource2 == null) {
            this.copyFile();
        } else {
            this.appendTwoFiles();
        }
    }

    /**
     * This method copies the content of iSource1 into iDestination.
     */
    private void copyFile() {
        try {
            MonitorableInputStream mis = new MonitorableFileInputStream(iSource1);
            BufferedInputStream bis = new BufferedInputStream(mis, 25600);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(iDestination), 25600);
            if(iParent != null) {
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                }
                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus("Started copying file '" + iSource1.getAbsoluteFile() + "' to '" + iDestination.getAbsoluteFile() + "'...");
                }

                // The progress monitor dialog.
                iMonitor = new ProgressMonitor(iParent, "Copying file '" + iSource1.getAbsoluteFile() + "' to '" + iDestination.getAbsoluteFile() + "'...", "Initializing...", 0, mis.getMaximum()+1);
                iMonitor.setMillisToDecideToPopup(500);
                iMonitor.setMillisToPopup(500);
                iMonitor.setNote("Copying file...");
                iMonitor.setProgress(1);
            }
            int current = 0;
            int previous = 0;
            boolean cancelled = false;
            while(((current = bis.read()) != -1) && (!cancelled)) {
                bos.write(current);
                if(iParent != null) {
                    int local = mis.monitorProgress();
                    if(local > previous) {
                        // Show it on the progressbar.
                        if(local < iMonitor.getMaximum()) {
                            iMonitor.setProgress(local);
                        } else {
                            int delta = local-iMonitor.getMaximum();
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
                        previous = local;
                    }
                    if(iMonitor.isCanceled()) {
                        cancelled = true;
                    }
                }
            }
            bos.flush();
            bos.close();
            bis.close();

            if(iParent != null) {
                iMonitor.setProgress(iMonitor.getMaximum());
                // Close the progress monitor.
                iMonitor.close();
                // BEEP!
                //Toolkit.getDefaultToolkit().beep();
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.DEFAULT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                // See if cancel was pressed,
                // and if so, delete the output file!
                String status = null;
                if(cancelled) {
                    iDestination.delete();
                    status = "Cancelled copy to file '" + iDestination + "'. Deleted unfinished output file.";
                } else {
                    // Different status message.
                    StringBuffer tempSB = new StringBuffer("Copied file '" + iSource1 + "' to output file '" + iDestination + "'.");
                    status = tempSB.toString();
                }

                // If the parent component is capable of displaying status messages,
                // set one!
                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus(status);
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method copies the content of iSource1 into iDestination, and
     * then appends the contents of iSource2 to iDestination.
     */
    private void appendTwoFiles() {
        try {
            MonitorableInputStream mis1 = new MonitorableFileInputStream(iSource1);
            MonitorableInputStream mis2 = new MonitorableFileInputStream(iSource2);
            BufferedInputStream bis1 = new BufferedInputStream(mis1, 25600);
            BufferedInputStream bis2 = new BufferedInputStream(mis2, 25600);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(iDestination), 25600);

            if(iParent != null) {
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                }
                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus("Started appending files '" + iSource1 + "' and '" + iSource2 + "' to '" + iDestination + "'...");
                }
                // The progress monitor dialog.
                iMonitor = new ProgressMonitor(iParent, "Appending files '" + iSource1 + "' and '" + iSource2 + "' to '" + iDestination + "'...", "Initializing...", 0, mis1.getMaximum()+mis2.getMaximum()+1);
                iMonitor.setMillisToDecideToPopup(500);
                iMonitor.setMillisToPopup(500);
                iMonitor.setNote("Appending files...");
                iMonitor.setProgress(1);
            }
            int current = 0;
            int previous = 0;
            boolean cancelled = false;
            // First source file.
            while(((current = bis1.read()) != -1) && (!cancelled)) {
                bos.write(current);
                if(iParent != null) {
                    int local = mis1.monitorProgress();
                    if(local > previous) {
                        // Show it on the progressbar.
                        if(local < iMonitor.getMaximum()) {
                            iMonitor.setProgress(local);
                        } else {
                            int delta = local-iMonitor.getMaximum();
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
                        previous = local;
                    }

                    if(iMonitor.isCanceled()) {
                        cancelled = true;
                    }
                }
            }
            // Second source file.
            int currentProg = mis1.getMaximum();
            while(((current = bis2.read()) != -1) && (!cancelled)) {
                bos.write(current);
                if(iParent != null) {
                    int local = currentProg + mis2.monitorProgress();
                    if(local > previous) {
                        // Show it on the progressbar.
                        if(local < iMonitor.getMaximum()) {
                            iMonitor.setProgress(local);
                        } else {
                            int delta = local-iMonitor.getMaximum();
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
                        previous = local;
                    }
                    if(iMonitor.isCanceled()) {
                        cancelled = true;
                    }
                }
            }

            bos.flush();
            bos.close();
            bis1.close();
            bis2.close();

            if(iParent != null) {
                iMonitor.setProgress(iMonitor.getMaximum());
                // Close the progress monitor.
                iMonitor.close();
                // BEEP!
                //Toolkit.getDefaultToolkit().beep();
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.DEFAULT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                // See if cancel was pressed,
                // and if so, delete the output file!
                String status = null;
                if(cancelled) {
                    iDestination.delete();
                    status = "Cancelled appending to file '" + iDestination + "'. Deleted unfinished output file.";
                } else {
                    // Different status message.
                    StringBuffer tempSB = new StringBuffer("Appended files '" + iSource1 + "' and '" + iSource2 + "' to output file '" + iDestination + "'.");
                    status = tempSB.toString();
                }

                // If the parent component is capable of displaying status messages,
                // set one!
                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus(status);
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
