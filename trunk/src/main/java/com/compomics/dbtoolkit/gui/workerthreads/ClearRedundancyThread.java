/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 22-okt-02
 * Time: 11:53:24
 */
package com.compomics.dbtoolkit.gui.workerthreads;

import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class represents a threaded implementation of the
 * algorithm that clears database redundancy.
 *
 * @author Lennart Martens
 */
public class ClearRedundancyThread implements Runnable {

    /**
     * A possible parent for the display of a progress bar (when in GUI mode).
     */
    private JFrame iParent = null;

    /**
     * The progressmonitor that will be displayed when in GUI mode.
     */
    private ProgressMonitor iMonitor = null;

    /**
     * The tempfolder.
     */
    private File iTempFolder = null;

    /**
     * The output file.
     */
    private File iOutput = null;

    /**
     * The DBLoader to load the database from.
     */
    private DBLoader iLoader = null;

    /**
     * The AutoDBloader used to identify a DB when it's loaded.
     */
    AutoDBLoader iAuto = null;

    /**
     * The progress monitor uses this one.
     */
    private int iCurrentProgress = 0;

    /**
     * boolean to indicate the user pressed cancel.
     */
    private boolean iCancelled = false;

    /**
     * This constructor is meant to be used for non-GUI mode operations.
     * It requires only the tempfolder for temporary storage and the
     * output file.
     *
     * @param   aTempFolder File, pointing to the tempfolder to use.
     *                      This folder should exist!
     * @param   aOutput File with the output file.
     * @param   aLoader DBLoader to read the DB to clear from.
     * @param   aAuto    AutoDBLoader to automatically decide DB type from known DBLoaders.
     */
    public ClearRedundancyThread(File aTempFolder, File aOutput, DBLoader aLoader, AutoDBLoader aAuto) {
        this(aTempFolder, aOutput, aLoader, aAuto, null);
    }

    /**
     * This constructor is meant to be used for GUI mode operations.
     * It requires the tempfolder for temporary storage and the
     * output file, as well as a JFrame that will act as the parent
     * to the progressbar that will be displayed.
     *
     * @param   aTempFolder File, pointing to the tempfolder to use.
     *                      This folder should exist!
     * @param   aOutput File with the output file.
     * @param   aParent JFrame to act as the parent for the progressbar.
     * @param   aLoader DBLoader to read the DB to clear from.
     * @param   aAuto    AutoDBLoader to automatically decide DB type from known DBLoaders.
     */
    public ClearRedundancyThread(File aTempFolder, File aOutput, DBLoader aLoader, AutoDBLoader aAuto, JFrame aParent) {
        this.iTempFolder = aTempFolder;
        this.iOutput = aOutput;
        this.iLoader = aLoader;
        this.iAuto = aAuto;
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
        try {
            // Okay, clearing DB redundancy uses a lot of memory.
            // That's why we should make sure we have enough (or the DB size is small enough).
            // What we'll do is choose the safest (and slowest!) option.
            // We'll divide the database in sub-DB's (based on sequence length) and
            // process each of these individually.
            // The progress bar has to accommodate for that.
            int factor = 1;
            if(iParent != null) {
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                }
                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus("Started clearing redundancy from DB (outputting to '" + iOutput.getAbsoluteFile() + "')...");
                }
                // The progress monitor dialog.
                int fullProgress = iLoader.getMaximum()*2;
                iMonitor = new ProgressMonitor(iParent, "Clearing redundancy from DB." + " Outputting to '" + iOutput.getAbsoluteFile() + "'...", "Initializing...", 0, fullProgress+1);
                iMonitor.setMillisToDecideToPopup(0);
                iMonitor.setMillisToPopup(0);
                iMonitor.setNote("Subdividing database into temporary files in '" + iTempFolder.getAbsolutePath() + "'...");
                iMonitor.setProgress(1);
                iCancelled = false;
            }

            Protein protein = null;
            // The HashMap with file pointers.
            HashMap files = new HashMap();
            // The HashMap with PrintWriters.
            HashMap writers = new HashMap();

            // Counters.
            int numberOfEntriesRead = 0;
            int writtenToResultFile = 0;
            // Cycling...
            while(((protein = iLoader.nextProtein()) != null) && (!iCancelled)) {
                // Count entry.
                numberOfEntriesRead++;
                // Get the length
                long length = protein.getLength();
                String key = Long.toString(length);
                // See if the PrintWriter already exists,
                // and if it doesn't, create it.
                if(writers.get(key) != null) {
                    // Just write to noutput file and continue.
                    PrintWriter out = (PrintWriter)writers.get(key);
                    protein.writeToFASTAFile(out);
                } else {
                    // Create a new PrintWriter, write the entry and store
                    // the wirter in the 'writers' hash.
                    // Also create the file, of course.
                    File outFile = new File(this.iTempFolder + "/" + key + ".tmp");
                    outFile.createNewFile();
                    PrintWriter out = new PrintWriter(new FileWriter(outFile));
                    protein.writeToFASTAFile(out);
                    files.put(key, outFile);
                    writers.put(key, out);
                }
                if(iParent != null) {
                    // Record the current progress ...
                    this.iCurrentProgress = iLoader.monitorProgress();
                    // ... and show it on the progressbar.
                    iMonitor.setProgress(iCurrentProgress);
                    // See if the user pressed cancel.
                    if(iMonitor.isCanceled()) {
                        iCancelled = true;
                    }
                }
            }

            if(iParent != null) {
                iMonitor.setNote("Switching to generated temporary files...");
            }

            // Reset the DBLoader (we won't be needing it anymore).
            iLoader.reset();

            // Okay, selection is complete.
            // Flush and close all streams.
            Iterator iterator = writers.values().iterator();
            while(iterator.hasNext()) {
                PrintWriter out = (PrintWriter)iterator.next();
                out.flush();
                out.close();
                out = null;
            }

            // Okay, now cycle each of the temporary files and process them.
            // Some overhead to alphabetize them.
            Set col = files.keySet();
            String[] allFiles = new String[col.size()];
            col.toArray(allFiles);
            long[] sorted = new long[allFiles.length];
            int combinedSize = 0;
            for(int i=0;i<allFiles.length;i++) {
                String lFile = allFiles[i];
                sorted[i] = Long.parseLong(allFiles[i]);
                allFiles[i] = null;
                if(iParent != null) {
                    combinedSize += (int)((File)files.get(lFile)).length();
                }
            }
            Arrays.sort(sorted);
            // Reformat the progress bar.
            if(iParent != null) {
                // Also factor in that the files need be deleted.
                ProgressMonitor pm = iMonitor;
                int max = combinedSize;
                iCurrentProgress = iMonitor.getMaximum()/2;
                iMonitor = new ProgressMonitor(iParent, "Clearing redundancy from DB. Outputting to '" + iOutput.getAbsoluteFile() + "'...", "Initializing temporary files...", 0, iCurrentProgress+max);
                iMonitor.setMillisToDecideToPopup(0);
                iMonitor.setMillisToPopup(0);
                iMonitor.setNote("Processing temporary files...");
                iMonitor.setProgress(iCurrentProgress);

                pm.close();
            }

            PrintWriter outputWriter = null;
            if(!iCancelled) {
                outputWriter = new PrintWriter(new FileWriter(iOutput));
            }
            for(int i=0;(i<sorted.length && !iCancelled);i++) {
                File temp = (File)files.get(Long.toString(sorted[i]));
                writtenToResultFile += this.processRedundancy(temp, outputWriter, factor);
            }

            // Close and flush the outputwriter.
            if(outputWriter != null) {
                outputWriter.flush();
                outputWriter.close();
            }

            // Delete all the temp files.
            if(iParent != null) {
                iMonitor.setNote("Cleaning up temporary files...");
            }
            for(int i=0;i<sorted.length;i++) {
                String lFile = Long.toString(sorted[i]);
                File temp = (File)files.get(lFile);
                boolean gone = false;
                int count = 0;
                while(!gone) {
                    count++;
                    gone = temp.delete();
                    if(count > 2) {
                        System.gc();
                        count = 0;
                    }
                }
                if((iParent != null) && (iCurrentProgress < iMonitor.getMaximum())) {
                    iCurrentProgress++;
                    iMonitor.setProgress(iCurrentProgress);
                }
            }
            if(iParent != null) {
                iMonitor.setProgress(iMonitor.getMaximum());
            }

            StringBuffer tempSB = new StringBuffer("Created cleared DB file '" + iOutput.getAbsoluteFile() + "'.");
            tempSB.append(" Written " + writtenToResultFile + " entries to result file (" + numberOfEntriesRead + " entries read from original DB - reduction to " + (writtenToResultFile*100/numberOfEntriesRead) + "% of DB)");

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
                if(iCancelled) {
                    iOutput.delete();
                    status = "Cancelled DB output to file '" + iOutput.getAbsoluteFile() + "'. Deleted unfinished output file and temporary files.";
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
                // Command-line output.
                System.out.println("\n\t" + tempSB.toString() + "\n");
            }

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }


    }

    /**
     * This method will process the redundancy in a single file.
     *
     * @param   aFile   the File from which to clear all redundancy.
     * @param   aOutput PrintWriter to output to.
     * @param   aFactor int with the number of times we should divide the progress by 1024.
     * @return  int with the number of outputted entries.
     * @exception   IOException when file IO goes wrong.
     */
    private int processRedundancy(File aFile, PrintWriter aOutput, int aFactor) throws IOException {
        // Counter.
        int counter = 0;

        // First get a DBLoader for the database (it should be FASTA, but do auto anyway).
        try {
            DBLoader loader = iAuto.getLoaderForFile(aFile.getAbsolutePath());

            // The protein we're cycling.
            Protein protein = null;
            // Hash that will store the sequences.
            HashMap sequences = new HashMap();

            if(iParent != null) {
                iMonitor.setNote("Processing '" + aFile.getAbsolutePath() + "'...");
            }

            // Now read all entries.
            while(((protein = loader.nextProtein()) != null) && (!iCancelled)) {
                // Get the sequence as a String and the header as a Header.
                String sequence = protein.getSequence().getSequence();
                Header header = protein.getHeader();

                // See if the sequence is present in the hash.
                Object temp = sequences.get(sequence);
                if(temp != null) {
                    // Present! This entry is redundant!
                    // See if we should replace the header.
                    Header inHash = (Header)temp;
                    if(inHash.getScore() < header.getScore()) {
                        // Okay, we need to replace the header.
                        if(inHash.hasAddenda()) {
                            String addenda = inHash.getAddenda();
                            header.addAddendum(addenda);
                        }
                        header.addAddendum(inHash.getCoreHeader());
                        sequences.put(sequence, header);
                    } else {
                        // The cached score is higher or the same. Just add the current
                        // entry in addendum.
                        inHash.addAddendum(header.getCoreHeader());
                    }
                } else {
                    // New entry! Add it to the hash + store the header as well.
                    sequences.put(sequence, header);
                }

                // Check to see if we should monitor progress here.
                if(iParent != null) {
                    iMonitor.setProgress(iCurrentProgress + loader.monitorProgress());
                    if(iMonitor.isCanceled()) {
                        iCancelled = true;
                    }
                }
            }
            iCurrentProgress += loader.getMaximum();
            loader.close();
            loader = null;
            // Write all the sequences and their headers to file.
            Iterator iterator = sequences.keySet().iterator();
            if(iParent != null) {
                iMonitor.setNote("Writing output (progress may appear to halt)...");
            }
            while(iterator.hasNext() && !iCancelled) {
                // Sequence was key in the hash, Header value.
                String sequence = (String)iterator.next();
                Header header = (Header)sequences.get(sequence);
                // We construct a new Protein so that we can output it decently.
                Protein toWrite = new Protein(header.getFullHeaderWithAddenda(), sequence);
                // The output.
                toWrite.writeToFASTAFile(aOutput);
                counter++;
                if((iParent != null) && (iMonitor.isCanceled())) {
                    iCancelled = true;
                }
            }
            aOutput.flush();
        } catch(UnknownDBFormatException e) {
            e.printStackTrace();
        }

        return counter;
    }
}
