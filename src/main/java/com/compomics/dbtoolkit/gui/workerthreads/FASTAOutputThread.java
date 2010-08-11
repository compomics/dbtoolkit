/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 15-okt-02
 * Time: 18:48:25
 */
package com.compomics.dbtoolkit.gui.workerthreads;

import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Protein;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.text.ParseException;

/*
 * CVS information:
 *
 * $Revision: 1.8 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class 
 *
 * @author Lennart
 */
public class FASTAOutputThread implements Runnable {

    /**
     * The DBLoader for the input DB.
     */
    private DBLoader iLoader = null;
    /**
     * The output file.
     */
    private File iOutput = null;
    /**
     * When in GUI mode, this JFrame points to the parent.
     */
    private JFrame iParent = null;
    /**
     * When in GUI mode, this will indicate progress.
     */
    private ProgressMonitor iMonitor = null;
    /**
     * The filter to use for the DB, if any.
     */
    private Filter iFilter = null;

    /**
     * This HashMap contains substitutions. Can be 'null' for no substitutions.
     */
    private HashMap iSubstitutions = null;

    /**
     * double with the minimal mass limit to pass the filter. Ignored of less than 0.
     */
    private double iMinMass = -1.0;

    /**
     * double with the maximal mass limit to pass the filter. Ignored of less than 0.
     */
    private double iMaxMass = -1.0;

    /**
     * This constructor creates a FASTA output thread based upon a possible parent,
     * DBLoader and an output file.
     *
     * @param aParent   JFrame with the parent if in GUI mode, can be 'null' for command-line mode.
     * @param aLoader   DBLoader with the pointer to the DB file.
     * @param aOutput   File with the pointer to the destination file.
     */
    public FASTAOutputThread(JFrame aParent, DBLoader aLoader, File aOutput) {
        this(aParent, aLoader, aOutput, null, null, -1.0, -1.0);
    }

    /**
     * This constructor creates a FASTA output thread based upon a possible parent,
     * DBLoader, output file and a Filter.
     *
     * @param aParent   JFrame with the parent if in GUI mode, can be 'null' for command-line mode.
     * @param aLoader   DBLoader with the pointer to the DB file.
     * @param aOutput   File with the pointer to the destination file.
     * @param aFilter   Filter to apply. Can be 'null' for no filter.
     */
    public FASTAOutputThread(JFrame aParent, DBLoader aLoader, File aOutput, Filter aFilter) {
        this(aParent, aLoader, aOutput, aFilter, null, -1.0, -1.0);
    }

    /**
     * This constructor creates a FASTA output thread based upon a possible parent,
     * DBLoader, output file, a Filter and mass limits. If both mass limits are < 0, the
     * mass filter is ignored.
     *
     * @param aParent   JFrame with the parent if in GUI mode, can be 'null' for command-line mode.
     * @param aLoader   DBLoader with the pointer to the DB file.
     * @param aOutput   File with the pointer to the destination file.
     * @param aFilter   Filter to apply. Can be 'null' for no filter.
     * @param aMinMass  double with the minimal mass to pass the mass filter.
     * @param aMaxMass  double with the maximal mass to pass the mass filter.
     */
    public FASTAOutputThread(JFrame aParent, DBLoader aLoader, File aOutput, Filter aFilter, double aMinMass, double aMaxMass) {
        this(aParent, aLoader, aOutput, aFilter, null, aMinMass, aMaxMass);
    }

    /**
     * This constructor creates a FASTA output thread based upon a possible parent,
     * DBLoader, output file and a set of substitutions.
     *
     * @param aParent   JFrame with the parent if in GUI mode, can be 'null' for command-line mode.
     * @param aLoader   DBLoader with the pointer to the DB file.
     * @param aOutput   File with the pointer to the destination file.
     * @param aSubstitutions    HashMap with the substitutions to perform. Can be 'null' for no
     *                          substitutions.
     */
    public FASTAOutputThread(JFrame aParent, DBLoader aLoader, File aOutput, HashMap aSubstitutions) {
        this(aParent, aLoader, aOutput, null, aSubstitutions, -1.0, -1.0);
    }

    /**
     * This constructor creates a FASTA output thread based upon a possible parent,
     * DBLoader, output file, Filter, a set of substitions, and mass limits. If both
     * mass limits are < 0, the mass filter is ignored.
     *
     * @param aParent   JFrame with the parent if in GUI mode, can be 'null' for command-line mode.
     * @param aLoader   DBLoader with the pointer to the DB file.
     * @param aOutput   File with the pointer to the destination file.
     * @param aFilter   Filter to apply. Can be 'null' for no filter.
     * @param aSubstitutions    HashMap with the substitutions to perform. Can be 'null' for no
     *                         substitutions.
     * @param aMinMass  double with the minimal mass to pass the mass filter.
     * @param aMaxMass  double with the maximal mass to pass the mass filter.

     */
    public FASTAOutputThread(JFrame aParent, DBLoader aLoader, File aOutput, Filter aFilter, HashMap aSubstitutions, double aMinMass, double aMaxMass) {
        this.iParent = aParent;
        this.iLoader = aLoader;
        this.iOutput = aOutput;
        this.iFilter = aFilter;
        this.iSubstitutions = aSubstitutions;
        this.iMinMass = aMinMass;
        this.iMaxMass = aMaxMass;
    }

    /**
     * This method parses the specified String into a Map containing
     * the substitions. Each residue to substitute will be a key, with the
     * destination String as the value. The substitution String should be
     * formatted like this: "I,L=1;K,Q=2". This String replaces I and L residues
     * by 1 and K and Q by 2.
     *
     * @param aSubstitutionString   String with a correctly formatted substition String.
     * @return HashMap with each residue to substitute as key and the replacements as keys.
     * @throws  ParseException whenever parsing of the substitution String failed.
     */
    public static HashMap parseSubstitutions(String aSubstitutionString) throws ParseException {
        HashMap result = new HashMap();
        // Check for 'null' substitution String;
        if(aSubstitutionString == null) {
            throw new IllegalArgumentException("The substitution String you tried to parse was 'null'!");
        }
        aSubstitutionString = aSubstitutionString.trim();
        // Remove leading and/or trailing ';'.
        if(aSubstitutionString.startsWith(";")) {
            aSubstitutionString = aSubstitutionString.substring(1);
        }
        if(aSubstitutionString.endsWith(";")) {
            aSubstitutionString = aSubstitutionString.substring(0, aSubstitutionString.length()-1);
        }
        // Find each substitution set.
        String substString = aSubstitutionString;
        while(substString.length() > 0) {
            // Extract this set.
            String set = null;
            if(substString.indexOf(";") > 0) {
                set = substString.substring(0, substString.indexOf(";"));
                // Remove extracted set + separator from substitution String.
                substString = substString.substring(substString.indexOf(";")+1).trim();
            } else {
                set = substString;
                substString = "";
            }
            int equals = set.indexOf("=");
            if(equals <= 0) {
                throw new ParseException("Unable to parse substitutions: missing equals sign in set!", aSubstitutionString.indexOf(set));
            } else {
                String targets = set.substring(0, equals).trim();
                String destination = set.substring(equals+1).trim();
                // Find all targets.
                int comma = -1;
                while((comma = targets.indexOf(",")) > 0) {
                    String target = targets.substring(0, comma).trim();
                    result.put(target, destination);
                    targets = targets.substring(comma+1).trim();
                }
                // Do last fence-post.
                result.put(targets, destination);
            }
        }
        return result;
    }

    public void run() {
        try {
            int entriesWritten = 0;
            if(iParent != null) {
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                }

                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus("Started outputting FASTA database to '" + iOutput.getAbsoluteFile() + "'...");
                }

                // The progress monitor dialog.
                iMonitor = new ProgressMonitor(iParent, "Outputting FASTA database to '" + iOutput.getAbsoluteFile() + "'...", "Initializing...", 0, iLoader.getMaximum()+1);
                iMonitor.setMillisToDecideToPopup(0);
                iMonitor.setMillisToPopup(0);
                iMonitor.setNote("Writing database file...");
                iMonitor.setProgress(1);
            }

            // The outputstream.
            PrintWriter out = new PrintWriter(new FileWriter(iOutput));

            String entry = null;
            boolean cancelled = false;
            // Load first entry.
            if(iFilter == null) {
                entry = iLoader.nextFASTAEntry();
            } else {
                entry = iLoader.nextFilteredFASTAEntry(iFilter);
            }
            while((entry != null) && (!cancelled)) {
                // Write the entry, optionally substituting some stuff.
                if(iSubstitutions == null) {
                    Protein p = new Protein(entry);
                    // If we need to use a mass filter (iMinMass and iMaxMass non-negative)
                    // we should check it. Otherwise just proceed.
                    if((iMinMass < 0 && iMaxMass < 0) || (iMinMass >= 0 && iMaxMass > 0 && p.getMass() >= iMinMass && p.getMass() <= iMaxMass)) {
                        out.print(entry+"\n");
                        entriesWritten++;
                    }
                } else {
                    BufferedReader br = new BufferedReader(new StringReader(entry));
                    String header = br.readLine();
                    String sequenceLine = null;
                    while((sequenceLine = br.readLine()) != null) {
                        sequenceLine = sequenceLine.trim();
                        Iterator iter = iSubstitutions.keySet().iterator();
                        while (iter.hasNext()) {
                            String target = (String)iter.next();
                            String destination = (String)iSubstitutions.get(target);
                            while(sequenceLine.indexOf(target) >= 0) {
                                int start = sequenceLine.indexOf(target);
                                sequenceLine = sequenceLine.substring(0, start) + destination + sequenceLine.substring(start + target.length());
                            }
                        }
                        AASequenceImpl seq = new AASequenceImpl(sequenceLine);
                        if((iMinMass < 0 && iMaxMass < 0) || (iMinMass >= 0 && iMaxMass > 0 && seq.getMass() >= iMinMass && seq.getMass() <= iMaxMass)) {
                            out.print(header+"\n");
                            out.print(sequenceLine+"\n");
                            entriesWritten++;
                        }
                    }
                    br.close();
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
                // Load next entry.
                if(iFilter == null) {
                    entry = iLoader.nextFASTAEntry();
                } else {
                    entry = iLoader.nextFilteredFASTAEntry(iFilter);
                }
            }

            // Flush and close the outputStream.
            out.flush();
            out.close();
            // Reset the Loader.
            iLoader.reset();
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
                    status = "Cancelled output to file '" + iOutput.getAbsoluteFile() + "'. Deleted unfinished output file.";
                } else {
                    // Different status message.
                    status = "Created FASTA DB file '" + iOutput.getAbsoluteFile() + "', written " + entriesWritten + " entries.";
                }

                // If the parent component is capable of displaying status messages,
                // set one!
                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus(status);
                }
            } else {
                System.out.println("Created FASTA DB file '" + iOutput.getAbsoluteFile() + "', written " + entriesWritten + " entries.");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
