/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 20-okt-02
 * Time: 12:03:42
 */
package com.compomics.dbtoolkit.gui.workerthreads;

import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;
import com.compomics.dbtoolkit.io.QueryParser;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;
import com.compomics.util.gui.FlamableJFrame;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;
import java.util.Date;
import java.math.BigDecimal;

/*
 * CVS information:
 *
 * $Revision: 1.11 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class implements a processing task for the database,
 * the currently listed tasks being:
 *  <ul>
 *    <li><N-terminal or C-terminal ragging/li>
 *    <li>Selection of a distinct subsection of the DB through sequence queries</li>
 *  </ul>
 * This class is designed as it's own factory.
 *
 * @author Lennart Martens
 */
public class ProcessThread implements Runnable {

// Generic variables.---------------------------------------------------------------------------------------------------
    /**
     * This variable holds the DBLoader tro load the DB from.
     */
    private DBLoader iLoader = null;

    /**
     * The file for the outputfile.
     */
    private File iOutput = null;

    /**
     * The parent JFrame (if any).
     */
    private JFrame iParent = null;

    /**
     * The ProgressMonitor.
     */
    private ProgressMonitor iMonitor = null;

    /**
     * This variable holds a Filter to be applied when
     * reading the DB, if any.
     */
    private Filter iFilter = null;

    /**
     * This variable holds the coded task to perform.
     */
    private int iTaskType = 0;

    /**
     * This variable is set when mass limits should be applied.
     */
    private boolean iMassLimits = false;

    /**
     * This variable holds the lower mass limit, if required.
     */
    private double iMinMass = 0.0;

    /**
     * This variable holds the upper mass limit, if required.
     */
    private double iMaxMass = 0.0;

    /**
     * This variable holds the enzyme to be used for digestion, if required.
     */
    private Enzyme iEnzyme = null;

    /**
     * This is the code for a task that should rag a DB.
     */
    public static final int RAGGING = 0;

    /**
     * This is the code for a task that should isolate a subset of the DB.
     */
    public static final int SUBSET = 1;

// Variables specific to a ragging task.--------------------------------------------------------------------------------
    /**
     * This boolean indicates whether ragging is desired.
     * This variable is specific for a ragging task.
     */
    private boolean iTruncate = false;

    /**
     * When ragging is desired, this variable will hold the
     * amount of residus to rag.
     * This variable is specific for a ragging task.
     */
    private int iTruncateSize = 0;

    /**
     * This variable indicates at which terminus ragging should be performed.
     */
    private int iTerminus = 0;

    /**
     * This variable holds the code for N-Terminal ragging.
     */
    public static final int NTERMINUS = 0;

    /**
     * This variable holds the code for C-Terminal ragging.
     */
    public static final int CTERMINUS = 1;

// Variables specific to a subset isolation task.-----------------------------------------------------------------------

    /**
     * This variable will the complete subset query as defined by the user.
     */
    private String iSubsetQuery = null;

    /**
     * This ProteinFilter contains the logic of the query.
     */
    private ProteinFilter iProteinFilter = null;

// Variables specific to run-time information.--------------------------------------------------------------------------
    /**
     * This variable holds the number of entries that were ommitted because they were below the lower mass
     * limit.
     */
    private int iOmmittedLower = 0;

    /**
     * This variable holds the number of entries that were ommitted because they were above the upper mass
     * limit.
     */
    private int iOmmittedUpper = 0;

    /**
     * This variable holds the total number of entries read.
     */
    private int iNumberOfEntriesRead = 0;

    /**
     * This variable holds the number of cleaved entries generated.
     */
    private int iNumberOfCleavedEntries = 0;

    /**
     * This variable holds the number of entries written to the resultfile.
     */
    private int iWrittenToResultFile = 0;

// Constructors.--------------------------------------------------------------------------------------------------------
    /**
     * This constructor is designed to handle all generic settings.
     *
     * @param   aLoader DBLoader to read the DB from.
     * @param   aOutput the File to pipe the output to.
     * @param   aParent JFrame that is the parent for the ProgressMonitor that will be displayed (can be
     *                  'null' if no ProgressMonitor is to be displayed).
     * @param   aFilter Filter to apply to the DB (can be 'null', if no filter is required).
     * @param   aEnzyme Enzyme to perform the digest with (can be 'null' if no digestion is to take place).
     * @param   aMassLimits boolean that indicates whether mass limits should be considered.
     * @param   aMinMass    double with the lower mass limit (will be ignored if aMassLimits is 'false').
     * @param   aMaxMass    double with the upper mass limit  (will be ignored if aMassLimits is 'false').
     */
    protected ProcessThread(DBLoader aLoader, File aOutput, JFrame aParent, Filter aFilter, Enzyme aEnzyme, boolean aMassLimits, double aMinMass, double aMaxMass) {
        this.iLoader = aLoader;
        this.iOutput = aOutput;
        this.iParent = aParent;
        this.iFilter = aFilter;
        this.iEnzyme = aEnzyme;
        this.iMassLimits = aMassLimits;
        this.iMinMass = aMinMass;
        this.iMaxMass = aMaxMass;
    }


// Public methods.------------------------------------------------------------------------------------------------------

    /**
     * This method returns a ProcessThread, designed to isolate a sequence-based subset of the DB.
     *
     * @param   aLoader DBLoader to read the DB from.
     * @param   aOutput the File to pipe the output to.
     * @param   aParent JFrame that is the parent for the ProgressMonitor that will be displayed (can be
     *                  'null' if no ProgressMonitor is to be displayed).

     * @param   aFilter Filter to apply to the DB (can be 'null', if no filter is required).
     * @param   aEnzyme Enzyme to digest each entry in the DB with. If this parameter is 'null',
     *                  no digestion is performed.
     * @param   aMassLimits boolean that indicates whether mass limits need to be applied.
     * @param   aMinMass    double with the lower mass limit (will be ignored if aMassLimits is 'false').
     * @param   aMaxMass    double with the upper mass limit (will be ignored if aMassLimits is 'false').
     * @param   aTerminus   int with the code for the terminus (N or C) to apply the ragging to.
     * @param   aTruncate   boolean to indicate whether truncation is desired.
     * @param   aTruncateSize   int with the number of residus to truncate to (will be ignored if aTruncate is
     *                          'false').
     * @return  ProcessThread   configured for ragging a database (either N-terminal or C-terminal).
     */
    public static ProcessThread getRaggingTask(DBLoader aLoader, File aOutput, JFrame aParent, Filter aFilter, Enzyme aEnzyme, boolean aMassLimits, double aMinMass, double aMaxMass, int aTerminus, boolean aTruncate, int aTruncateSize) {
        ProcessThread pt = new ProcessThread(aLoader, aOutput, aParent, aFilter, aEnzyme, aMassLimits, aMinMass, aMaxMass);
        pt.iTaskType = ProcessThread.RAGGING;
        pt.iTerminus = aTerminus;
        pt.iTruncate = aTruncate;
        pt.iTruncateSize = aTruncateSize;
        return pt;
    }

    /**
     * This method returns a ProcessThread, designed to rag a DB.
     *
     * @param   aLoader DBLoader to read the DB from.
     * @param   aOutput the File to pipe the output to.
     * @param   aParent JFrame that is the parent for the ProgressMonitor that will be displayed (can be
     *                  'null' if no ProgressMonitor is to be displayed).

     * @param   aFilter Filter to apply to the DB (can be 'null', if no filter is required).
     * @param   aEnzyme Enzyme to digest each entry in the DB with. If this parameter is 'null',
     *                  no digestion is performed.
     * @param   aMassLimits boolean that indicates whether mass limits need to be applied.
     * @param   aMinMass    double with the lower mass limit (will be ignored if aMassLimits is 'false').
     * @param   aMaxMass    double with the upper mass limit (will be ignored if aMassLimits is 'false').
     * @param   aQueryString    String with the subset query as specified by the user.
     * @return  ProcessThread   configured for isolating a subset of the db based on the sequence.
     */
    public static ProcessThread getSubsetTask(DBLoader aLoader, File aOutput, JFrame aParent, Filter aFilter, Enzyme aEnzyme, boolean aMassLimits, double aMinMass, double aMaxMass, String aQueryString) {
        ProcessThread pt = new ProcessThread(aLoader, aOutput, aParent, aFilter, aEnzyme, aMassLimits, aMinMass, aMaxMass);
        pt.iTaskType = ProcessThread.SUBSET;
        pt.iSubsetQuery = aQueryString;
        return pt;
    }

    /**
     * This method returns a ProcessThread, designed to rag a DB.
     *
     * @param   aLoader DBLoader to read the DB from.
     * @param   aOutput the File to pipe the output to.
     * @param   aParent JFrame that is the parent for the ProgressMonitor that will be displayed (can be
     *                  'null' if no ProgressMonitor is to be displayed).

     * @param   aFilter Filter to apply to the DB (can be 'null', if no filter is required).
     * @param   aEnzyme Enzyme to digest each entry in the DB with. If this parameter is 'null',
     *                  no digestion is performed.
     * @param   aMassLimits boolean that indicates whether mass limits need to be applied.
     * @param   aMinMass    double with the lower mass limit (will be ignored if aMassLimits is 'false').
     * @param   aMaxMass    double with the upper mass limit (will be ignored if aMassLimits is 'false').
     * @param   aSequenceFilter    ProteinFilter that comprises the subset isoaltion logic.
     * @return  ProcessThread   configured for isolating a subset of the db based on the sequence.
     */
    public static ProcessThread getSubsetTask(DBLoader aLoader, File aOutput, JFrame aParent, Filter aFilter, Enzyme aEnzyme, boolean aMassLimits, double aMinMass, double aMaxMass, ProteinFilter aSequenceFilter) {
        ProcessThread pt = new ProcessThread(aLoader, aOutput, aParent, aFilter, aEnzyme, aMassLimits, aMinMass, aMaxMass);
        pt.iTaskType = ProcessThread.SUBSET;
        pt.iProteinFilter = aSequenceFilter;
        return pt;
    }

    /**
     * This method is called when the wrapped Thread starts threaded execution.
     * It can also be calle directly for non-threaded execution.
     */
    public void run() {
        if(this.iTaskType == ProcessThread.RAGGING) {
            this.startRagging();
        } else if(this.iTaskType == ProcessThread.SUBSET) {
            this.generateSubset();
        }
    }

// Private methods.-----------------------------------------------------------------------------------------------------
    /**
     * This method will use all information present on the current instance to rag a database.
     */
    private void startRagging() {
        iNumberOfEntriesRead = 0;
        iOmmittedLower = 0;
        iOmmittedUpper = 0;

        try {
            if(iParent != null) {
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                }

                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus("Started ragging database to FASTA outputfile '" + iOutput.getAbsoluteFile() + "'...");
                }

                // The progress monitor dialog.
                iMonitor = new ProgressMonitor(iParent, "Ragging database to FASTA outputfile '" + iOutput.getAbsoluteFile() + "'...", "Initializing...", 0, iLoader.getMaximum()+1);
                iMonitor.setMillisToDecideToPopup(100);
                iMonitor.setMillisToPopup(100);
                iMonitor.setNote("Generating " + ((this.iTerminus==NTERMINUS)?"N-terminal":"C-terminal") + " ragged database file...");
                iMonitor.setProgress(1);
            }

            // The PrintWriter to output to.
            PrintWriter pw = new PrintWriter(new FileWriter(iOutput));
            Protein protein = null;
            // Fence-post.
            if(iFilter == null) {
                protein = iLoader.nextProtein();
            } else {
                protein = iLoader.nextFilteredProtein(iFilter);
            }

            // Loop all.
            boolean cancelled  = false;
            while((protein != null) && (!cancelled)) {
                // Count a read entry.
                iNumberOfEntriesRead++;
                // Get the ragged entries.
                Protein[] result = getRaggedEntries(protein);

                for(int i=0;i<result.length;i++) {
                    result[i].writeToFASTAFile(pw);
                    // Count the written entry.
                    iWrittenToResultFile++;
                }
                if(iParent != null) {
                    // Show it on the progressbar.
                    if(iLoader.monitorProgress() < iMonitor.getMaximum()) {
                        iMonitor.setProgress(iLoader.monitorProgress());
                    } else {
                        int delta = iLoader.monitorProgress()-iMonitor.getMaximum();
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

                // Take next entry & continue.
                if(iFilter == null) {
                    protein = iLoader.nextProtein();
                } else {
                    protein = iLoader.nextFilteredProtein(iFilter);
                }
            }
            // Finalize output.
            pw.flush();
            pw.close();
            // Reset the Loader.
            iLoader.reset();

            // Output.
            StringBuffer tempSB = new StringBuffer("Created ragged FASTA DB file '" + iOutput.getAbsoluteFile() + "'.");
            tempSB.append(" Written " + iWrittenToResultFile + " entries to result file (" + iNumberOfEntriesRead + " entries read)");
            if(iMassLimits) {
                tempSB.append(" and skipped " + (iOmmittedLower+iOmmittedUpper) + " entries because they were out of mass range (" + iMinMass + "Da - " + iMaxMass + "Da.)");
            }

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
                    iOutput.delete();
                    status = "Cancelled ragged DB output to file '" + iOutput.getAbsoluteFile() + "'. Deleted unfinished output file.";
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
        } catch(IOException ioe) {
            // Update statuspanel is IOException occurs, do'nt display full stacktrace, not to frighten the users. Still warn them that something went unexpectedly.
            if(iParent != null) {
                ((StatusView)iParent).setError("An error occurred during processing. The resulting database may be corrupted. Errormessage: '" + ioe.getMessage() + "'.");
                JOptionPane.showMessageDialog(iParent, new String[]{"Error occurred writing results file!", ioe.getMessage()}, "Error writing output file!", JOptionPane.ERROR_MESSAGE);
            }
            ioe.printStackTrace();

        }
    }

    /**
     * This method takes a single protein and rags it N- or C-terminally.
     * It also removes redundancy that is the consequence of this ragging.
     * The resulting peptides are returned in the Protein array.
     *
     * @param   aProtein    the Protein (or enzymatically digested peptide)
     *                      to rag N or C-terminally.
     * @return  Protein[]   the results of the ragging process.
     */
    private Protein[] getRaggedEntries(Protein aProtein) throws IOException {
        Vector result = new Vector();
        // Okay, see if we need to truncate.
        if(iTruncate) {
            if(iTerminus == ProcessThread.NTERMINUS) {
                aProtein = aProtein.getNTermTruncatedProtein(iTruncateSize);
            } else if(iTerminus == ProcessThread.CTERMINUS) {
                aProtein = aProtein.getCTermTruncatedProtein(iTruncateSize);
            }
        }
        Protein[] proteins = null;
        // See if we need to digest the protein enzymatically.
        if(iEnzyme != null) {
            proteins = iEnzyme.cleave(aProtein);
            iNumberOfCleavedEntries += proteins.length;
        } else {
            proteins = new Protein[] {aProtein};
        }

        // Okay, possible truncation & digestion has been taken care of.
        // Start ragging.
        for(int i=0; i<proteins.length; i++) {
            Protein lProtein = proteins[i];

            // The first entry to retain is the original peptide (if, of course, it
            // complies to the mass limits if these are set.
            if(iMassLimits) {
                double mass = lProtein.getMass();
                if((mass < iMinMass) || (iMaxMass < mass)) {
                    // If the peptide has a mass lower than the
                    // lower limit, don't even bother to rag it.
                    if(mass < iMinMass) {
                        // Count the entry as ommitted because mass too low.
                        iOmmittedLower++;
                        continue;
                    } else {
                        // Count entry as ommitted because mass too high.
                        iOmmittedUpper++;
                    }
                } else {
                    // Passed mass limits, add original.
                    result.add(lProtein);
                }
            } else {
                // No mass limits set, just add the original.
                result.add(lProtein);
            }
            // Get header and sequence.
            Header header = lProtein.getHeader();
            AASequenceImpl sequence = lProtein.getSequence();
            // See if start- and endlocation are specified.
            int endLocation = header.getEndLocation();
            if(endLocation < 0) {
                endLocation = sequence.getLength();
            }
            int start = header.getStartLocation();
            if(start < 0) {
                start = 1;
            }
            boolean lContinue = true;
            int counter = 0;

            // Ragging...
            while(lContinue) {
                // Counting, always counting.
                counter++;
                // If there is nothing left to rag, we quit!
                // This is only useful when no mass limits are used, of course.
                if(sequence.getLength() == 1) {
                    break;
                }
                // Rag the protein according to user specs (N-term or C-term).
                AASequenceImpl tempSequence = null;
                if(this.iTerminus == NTERMINUS) {
                    tempSequence = new AASequenceImpl(sequence.getSequence().substring(1));
                } else {
                    tempSequence = new AASequenceImpl(sequence.getSequence().substring(0, sequence.getLength()-1));
                }
                // Set the correct Header info.
                Header tempHeader = (Header)header.clone();
                if(this.iTerminus == NTERMINUS) {
                    tempHeader.setLocation(start + counter, endLocation);
                } else {
                    tempHeader.setLocation(start, endLocation-counter);
                }
                // Create a ragged protein.
                Protein tempProtein = new Protein(tempHeader, tempSequence);
                // See if we should set mass limits.
                if(iMassLimits) {
                    double mass = tempProtein.getMass();
                    if((mass < iMinMass) || (iMaxMass < mass)) {
                        // Skip it.
                        // Oh, by the way, if the mass is lower than the
                        // lower limit, skip the rest. They will all be lighter anyway.
                        if(mass < iMinMass) {
                            lContinue = false;
                        }
                    } else {
                        // Passed mass limits, continue.
                        result.add(tempProtein);
                    }
                } else {
                    // No mass limits, automatic pass.
                    result.add(tempProtein);
                }
                // Move the floating sequence to the last generated one.
                sequence = tempSequence;
            }
        }

        // Clean out toReturn (clear redundancy based upon the headers.
        int liSize = result.size();
        HashMap headers = new HashMap(liSize);
        Vector nonRed = new Vector(liSize);
        for(int i=0;i<result.size();i++) {
            Protein lProtein = (Protein)result.get(i);
            String h = lProtein.getHeader().toString();
            // Only put in the nonRed Vector
            // when the header is not already present in the header list.
            if(headers.get(h) == null) {
                headers.put(h, "1");
                nonRed.add(lProtein);
            }
        }

        Protein[] toReturn = new Protein[nonRed.size()];
        nonRed.toArray(toReturn);

        // Specify the enzymicity.
        for (int i = 0; i < toReturn.length; i++) {
            Protein protein = toReturn[i];
            this.annotateHeader(protein.getHeader(), aProtein.getSequence().getSequence(), protein.getSequence().getSequence(), this.iEnzyme);
        }

        return toReturn;
    }

    /**
     * This method will generate a subset from a certain database, based
     * on a sequence query.
     */
    private void generateSubset() {
        iNumberOfEntriesRead = 0;
        iNumberOfCleavedEntries = 0;
        iOmmittedLower = 0;
        iOmmittedUpper = 0;
        boolean cancelled = false;
        boolean error = false;

        // Because this method also outputs an enzymatic digest of a DB, we need to be able to switch
        // comments on GUI.
        String monitorText = "Writing sequence-based subset to outputfile '";
        String initialNote = "Parsing subset query...";
        String runningNote = "Generating sequence-based subset database file...";
        if((iProteinFilter == null) && (iSubsetQuery == null) && iEnzyme != null) {
            // Cleavage only mode.
            monitorText = "Writing " + iEnzyme.getTitle() + " digest of database to outputfile '";
            initialNote = "Acquiring enzyme...";
            runningNote = "Digesting database...";
        }
        try {
            if(iParent != null) {
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                }

                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus(monitorText + iOutput.getAbsoluteFile() + "'...");
                }

                // The progress monitor dialog.
                iMonitor = new ProgressMonitor(iParent, monitorText + iOutput.getAbsoluteFile() + "'...", "Initializing...", 0, iLoader.getMaximum()+1);
                iMonitor.setMillisToDecideToPopup(0);
                iMonitor.setMillisToPopup(0);
                iMonitor.setNote(initialNote);
                iMonitor.setProgress(1);
            }
            // First we need to see if we have to parse the query String.
            if(iSubsetQuery != null) {
                QueryParser qp = new QueryParser();
                this.iProteinFilter = qp.parseQuery(this.iSubsetQuery);
                if(iProteinFilter == null) {
                    throw new ParseException("(No message returned from parser - general failure)", 0);
                }
            }

            try {
                // If we reach this point, the query is parsed into a FilterCollection, and
                // the rest is peanuts.
                if(iParent != null) {
                    iMonitor.setNote(runningNote);
                }

                // The PrintWriter to output to.
                PrintWriter pw = new PrintWriter(new FileWriter(iOutput));
                Protein protein = null;
                // Fence-post.
                if(iFilter == null) {
                    protein = iLoader.nextProtein();
                } else {
                    protein = iLoader.nextFilteredProtein(iFilter);
                }

                // Loop all.
                cancelled = false;
                while((protein != null) && (!cancelled)) {
                    // Count a read entry.
                    iNumberOfEntriesRead++;

                    // The Protein[] we'll be writing.
                    Protein[] proteins = null;
                    if(iEnzyme != null) {
                        proteins = iEnzyme.cleave(protein);
                        iNumberOfCleavedEntries += proteins.length;
                    } else {
                        proteins = new Protein[] {protein};
                    }

                    // Okay, cycle all proteins and apply conditions.
                    for(int i=0;i<proteins.length;i++) {
                        // Check for masslimits.
                        if(iMassLimits) {
                            double mass = proteins[i].getMass();
                            if(mass < iMinMass) {
                                iOmmittedLower++;
                                continue;
                            } else if(mass > iMaxMass) {
                                iOmmittedUpper++;
                                continue;
                            }
                        }
                        // So, we either do not use mass limits, or we passed them.
                        // Either way, check for passing of Filters.
                        if((iProteinFilter == null) || (iProteinFilter.passesFilter(proteins[i]))) {
                            proteins[i].writeToFASTAFile(pw);
                            // Count the written entry.
                            iWrittenToResultFile++;
                        }
                    }
                    if(iParent != null) {
                        // Show it on the progressbar.
                        if(iLoader.monitorProgress() < iMonitor.getMaximum()) {
                            iMonitor.setProgress(iLoader.monitorProgress());
                        } else {
                            int delta = iLoader.monitorProgress()-iMonitor.getMaximum();
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

                    // Take next entry & continue.
                    if(iFilter == null) {
                        protein = iLoader.nextProtein();
                    } else {
                        protein = iLoader.nextFilteredProtein(iFilter);
                    }
                }
                // Finalize output.
                pw.flush();
                pw.close();
                // Reset the Loader.
                iLoader.reset();

            } catch(IOException ioe) {
                ioe.printStackTrace();
                error = true;
            }
        } catch(ParseException pe) {
            if(iParent != null) {
                JOptionPane.showMessageDialog(iParent, new String[]{"ParseException raised when attempting to parse your query String!", pe.getMessage()}, "Could not parse query!", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("ParseException raised for query '" + iSubsetQuery + "': " + pe.getMessage());
                pe.printStackTrace();
            }
            error = true;
        }

        StringBuffer tempSB = new StringBuffer("Created sequence-based subset FASTA DB file '" + iOutput.getAbsoluteFile() + "'.");
        tempSB.append(" Written " + iWrittenToResultFile + " entries to result file" + ((iProteinFilter == null)?"":", rejected " + ((Math.max(iNumberOfCleavedEntries, iNumberOfEntriesRead))-iWrittenToResultFile-(iOmmittedLower+iOmmittedUpper)) + " elements based on the filter (" + iNumberOfEntriesRead + " entries read)"));
        if(iMassLimits) {
            tempSB.append(" and skipped " + (iOmmittedLower+iOmmittedUpper) + " elements because they were out of mass range (" + iMinMass + "Da - " + iMaxMass + "Da.)");
        }

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
                iOutput.delete();
                status = "Cancelled sequence-based subset output to file '" + iOutput.getAbsoluteFile() + "'. Deleted unfinished output file.";
            } else if(error) {
                iOutput.delete();
                status = "Error encountered while writing output to file '" + iOutput.getAbsoluteFile() + "'. Deleted unfinished output file.";
            } else {
                // Different status message.
                status = tempSB.toString();
            }

            // If the parent component is capable of displaying status messages,
            // set one!
            if(iParent instanceof StatusView) {
                ((StatusView)iParent).setStatus(status);
                if(error) {
                    ((StatusView)iParent).setError("Error writing output file for subset.");
                }
            }
        } else {
            // Command-line output.
            System.out.println("\n\t" + tempSB.toString() + "\n");
        }
    }

    /**
     * This method will annotate a header with the information about whether or
     * not the subsequence is an enzymatic cleavageproduct of the parent sequence.
     *
     * @param   aHeader Header that has to be annotated (used as a reference param, btw!)
     * @param   aParentSeq  String with the parent sequence.
     * @param   aSubSeq String with the subsequence to consider.
     * @param   aEnzyme Enzyme to verify cleavage with.
     */
    private void annotateHeader(Header aHeader, String aParentSeq, String aSubSeq, Enzyme aEnzyme) {
        // Do not process non-digested entries.
        if(aEnzyme == null) {
            return;
        }
        int cleavage = aEnzyme.isEnzymaticProduct(aParentSeq, aSubSeq);
        String descr = aHeader.getDescription();

        switch(cleavage) {
            case Enzyme.ENTIRELY_NOT_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*EE*) " + descr);
                } else {
                    aHeader.setRest("(*EE*) " + aHeader.getRest());
                }
                break;
            case Enzyme.N_TERM_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*NE*) " + descr);
                } else {
                    aHeader.setRest("(*NE*) " + aHeader.getRest());
                }
                break;
            case Enzyme.C_TERM_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*CE*) " + descr);
                } else {
                    aHeader.setRest("(*CE*) " + aHeader.getRest());
                }
                break;
            case Enzyme.FULLY_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*FE*) " + descr);
                } else {
                    aHeader.setRest("(*FE*) " + aHeader.getRest());
                }
                break;
        }
    }
}
