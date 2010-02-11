/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-jul-2005
 * Time: 15:01:20
 */
package com.compomics.dbtoolkit.gui.workerthreads;

import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;
import com.compomics.util.protein.Protein;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.math.BigDecimal;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/06/04 11:14:13 $
 */

/**
 * This class
 *
 * @author Lennart
 * @version $Id: ShuffleDBThread.java,v 1.3 2007/06/04 11:14:13 lennart Exp $
 */
public class ShuffleDBThread implements Runnable {

    /**
     * The constant that indicates a task type for shuffling.
     */
    public static final int SHUFFLE= 0;

    /**
     * The constant that indicates a task type for shuffling.
     */
    public static final int REVERSE = 1;


    /**
     * This variable holds the task type. Choose an int from the constants defined on this class.
     */
    private int iTaskType = -1;

    /**
     * Database loader.
     */
    private DBLoader iInputDB = null;

    /**
     * Output writer.
     */
    private PrintWriter iOutWriter = null;

    /**
     * Output file name, if any.
     */
    private String iOutputName = null;

    /**
     * GUI parent, if any.
     */
    private JFrame iParent = null;

    /**
     * Progress bar. Only applicable if there is also a GUI parent.
     */
    private ProgressMonitor iMonitor = null;

    /**
     * This boolean is set to 'true' if the output writer needs to be closed.
     * This is only necessary when the output writer is NOT standard output stream.
     */
    private boolean ibCloseWriter = false;

    /**
     * This constructor takes an input DB, reads and randomizes it and oututs the results
     * to the standard output stream. Output format is in FASTA.
     *
     * @param aInputFile  File with the input database.
     * @exception java.io.IOException when the file could not be loaded.
     */
    public ShuffleDBThread(File aInputFile) throws IOException {
        this(aInputFile, null, -1);
    }

    /**
     * This constructor takes an input DB, reads and processes it according to the specified
     * task type and outputs the results to the standard output stream. Output format is in FASTA.
     *
     * @param aInputFile  File with the input database.
     * @param aTaskType int with the task type. To be chosen from the constants on this class.
     * @exception java.io.IOException when the file could not be loaded.
     */
    public ShuffleDBThread(File aInputFile, int aTaskType) throws IOException {
        this(aInputFile, null, aTaskType);
    }

    /**
     * This constructor takes an input DB, output file and task type. Output format is FASTA.
     *
     * @param aInputFile  File with the input database.
     * @param aOutputFile   File with the output database. Output format is FASTA.
     *                      Can be 'null' for output to StdOut.
     * @param aTaskType int with the task type. To be chosen from the constants on this class.
     * @exception IOException when the file could not be loaded.
     */
    public ShuffleDBThread(File aInputFile, File aOutputFile, int aTaskType) throws IOException {
        iInputDB = DBLoaderLoader.loadDB(aInputFile);
        if(aOutputFile == null) {
            iOutWriter = new PrintWriter(new OutputStreamWriter(System.out));
        } else {
            iOutWriter = new PrintWriter(new BufferedWriter(new FileWriter(aOutputFile)));
            ibCloseWriter = true;
            iOutputName = aOutputFile.getAbsolutePath();
        }
        this.iTaskType = aTaskType;
    }

    /**
     * This constructor takes an input DBLoader, output file and task type. Output format is FASTA.
     *
     * @param aLoader  DBLoader with the DBLoader for the input database.
     * @param aOutputFile   File with the output database. Output format is FASTA.
     *                      Can be 'null' for output to StdOut.
     * @param aTaskType int with the task type. To be chosen from the constants on this class.
     * @param aParent  JFrame with the GUI parent for this instance. Can be 'null' for no parent.
     * @exception IOException when the file could not be loaded.
     */
    public ShuffleDBThread(DBLoader aLoader, File aOutputFile, int aTaskType, JFrame aParent) throws IOException {
        iInputDB = aLoader;
        if(aOutputFile == null) {
            iOutWriter = new PrintWriter(new OutputStreamWriter(System.out));
        } else {
            iOutWriter = new PrintWriter(new BufferedWriter(new FileWriter(aOutputFile)));
            ibCloseWriter = true;
            iOutputName = aOutputFile.getAbsolutePath();
        }
        this.iTaskType = aTaskType;
        this.iParent = aParent;
    }

    /**
     * This method randomizes the database and outputs the result to the 'iOutWriter' PrintWriter.
     */
    public void shuffle() throws IOException {
        if(iParent != null) {
            if(iParent instanceof CursorModifiable) {
                ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
            } else {
                iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            if(iParent instanceof StatusView) {
                ((StatusView)iParent).setStatus("Started shuffling database " + (iOutputName != null?"to FASTA outputfile '" + iOutputName + "'":"") + "...");
            }

            // The progress monitor dialog.
            iMonitor = new ProgressMonitor(iParent, "Shuffling database " + (iOutputName != null?"to FASTA outputfile '" + iOutputName + "'":"") + "...", "Initializing...", 0, iInputDB.getMaximum()+1);
            iMonitor.setMillisToDecideToPopup(100);
            iMonitor.setMillisToPopup(100);
            iMonitor.setNote("Generating shuffled database file...");
            iMonitor.setProgress(1);
        }
        // Start processing.
        Protein current = null;
        boolean cancelled = false;
        while((current = iInputDB.nextProtein()) != null && !cancelled) {
            char[] sequence = current.getSequence().getSequence().toCharArray();
            // Replace the accession by affixing '_SHUFFLED'.
            current.getHeader().setAccession(current.getHeader().getAccession()+"_SHUFFLED");
            // Also append ' - SHUFFLED' to the description.
            Protein p = new Protein(current.getHeader().toString() + " - SHUFFLED", new String(this.shuffleSequence(sequence)));
            p.writeToFASTAFile(iOutWriter);
            if(iParent != null) {
                // Show it on the progressbar.
                if(iInputDB.monitorProgress() < iMonitor.getMaximum()) {
                    iMonitor.setProgress(iInputDB.monitorProgress());
                } else {
                    int delta = iInputDB.monitorProgress()-iMonitor.getMaximum();
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
                    cancelled = true;
                }
            }
        }
        iOutWriter.flush();
        if(ibCloseWriter) {
            iOutWriter.close();
        }
        // Reset the Loader.
        iInputDB.reset();

        // Output.
        StringBuffer tempSB = new StringBuffer("Created shuffled FASTA DB" + (iOutputName != null?" in file '" + iOutputName + "'":"") + ".");

        if(iParent != null) {
            iMonitor.setProgress(iMonitor.getMaximum());
            // Close the progress monitor.
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
            if(cancelled) {
                status = "Cancelled shuffled DB output";
                if(iOutputName != null) {
                    new File(iOutputName).delete();
                    status += " to file '" + iOutputName + "'. Deleted unfinished output file";
                }
                status += ".";
            } else {
                // Different status message.
                status = tempSB.toString();
            }

            // If the parent component is capable of displaying status messages,
            // set one!
            if(iParent instanceof StatusView) {
                ((StatusView)iParent).setStatus(status);
            }
        } else {
            // Command-line output style.
            System.out.println("\n\t" + tempSB.toString() + "\n");
        }
    }

    /**
     * This method reverses the database and outputs the result to the 'iOutWriter' PrintWriter.
     */
    public void reverse() throws IOException {
        if(iParent != null) {
            if(iParent instanceof CursorModifiable) {
                ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
            } else {
                iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            if(iParent instanceof StatusView) {
                ((StatusView)iParent).setStatus("Started reversing database " + (iOutputName != null?"to FASTA outputfile '" + iOutputName + "'":"") + "...");
            }

            // The progress monitor dialog.
            iMonitor = new ProgressMonitor(iParent, "Reversing database " + (iOutputName != null?"to FASTA outputfile '" + iOutputName + "'":"") + "...", "Initializing...", 0, iInputDB.getMaximum()+1);
            iMonitor.setMillisToDecideToPopup(100);
            iMonitor.setMillisToPopup(100);
            iMonitor.setNote("Generating reversed database file...");
            iMonitor.setProgress(1);
        }
        // Start processing.
        Protein current = null;
        boolean cancelled = false;
        while((current = iInputDB.nextProtein()) != null && !cancelled) {
            String sequence = current.getSequence().getSequence();
            // Replace the accession by affixing '_REVERSED'.
            current.getHeader().setAccession(current.getHeader().getAccession()+"_REVERSED");
            // Also append ' - REVERSED' to the description.
            Protein p = new Protein(current.getHeader().toString() + " - REVERSED", this.reverseSequence(sequence));
            p.writeToFASTAFile(iOutWriter);
            if(iParent != null) {
                // Show it on the progressbar.
                if(iInputDB.monitorProgress() < iMonitor.getMaximum()) {
                    iMonitor.setProgress(iInputDB.monitorProgress());
                } else {
                    int delta = iInputDB.monitorProgress()-iMonitor.getMaximum();
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
                    cancelled = true;
                }
            }
        }
        iOutWriter.flush();
        if(ibCloseWriter) {
            iOutWriter.close();
        }
        // Reset the Loader.
        iInputDB.reset();
        // Output.
        StringBuffer tempSB = new StringBuffer("Created reversed FASTA DB" + (iOutputName != null?" in file '" + iOutputName + "'":"") + ".");

        if(iParent != null) {
            iMonitor.setProgress(iMonitor.getMaximum());
            // Close the progress monitor.
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
            if(cancelled) {
                status = "Cancelled reversed DB output";
                if(iOutputName != null) {
                    new File(iOutputName).delete();
                    status += " to file '" + iOutputName + "'. Deleted unfinished output file";
                }
                status += ".";
            } else {
                // Different status message.
                status = tempSB.toString();
            }

            // If the parent component is capable of displaying status messages,
            // set one!
            if(iParent instanceof StatusView) {
                ((StatusView)iParent).setStatus(status);
            }
        } else {
            // Command-line output style.
            System.out.println("\n\t" + tempSB.toString() + "\n");
        }
    }

    /**
     * This method cleans up all resources.
     */
    public void finalize() throws Throwable {
        super.finalize();
    }


    /**
     * The 'main' method of a Thread.
     */
    public void run() {
        try {
            if(iTaskType == SHUFFLE) {
                shuffle();
            } else if(iTaskType == REVERSE) {
                reverse();
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * This method shuffles the characters in the sequence.
     *
     * @param sequence  char[] with the sequence.
     * @return  char[] with the shuffled sequence.
     */
    private char[] shuffleSequence(char[] sequence) {
        for(int i=0; i<sequence.length; i++) {
            int j=(int)(Math.random()*sequence.length);
            if(i!=j) {
                char temp = sequence[i];
                sequence[i] = sequence[j];
                sequence[j] = temp;
            }
        }
        return sequence;
    }

    /**
     * This method reverses the characters in the sequence.
     *
     * @param aSequence  String with the sequence.
     * @return  String with the reversed sequence.
     */
    private String reverseSequence(String aSequence) {
        StringBuffer reversed = new StringBuffer(aSequence.length());
        for(int i=1; i<=aSequence.length(); i++) {
            reversed.append(aSequence.charAt(aSequence.length()-i));
        }
        return reversed.toString();
    }
}
