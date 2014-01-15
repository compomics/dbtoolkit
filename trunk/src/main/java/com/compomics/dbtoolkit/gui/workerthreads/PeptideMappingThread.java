/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 30-May-2007
 * Time: 09:49:15
 */
package com.compomics.dbtoolkit.gui.workerthreads;

import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.gui.interfaces.CursorModifiable;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;
import com.compomics.util.protein.Protein;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.io.*;
import java.util.*;
/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2008/07/25 10:15:49 $
 */

/**
 * This class implements a Thread-ready way of mapping a list of peptides
 * to all entries in a DB while displaying a Progress Bar.
 *
 * @author Lennart Martens
 */
public class PeptideMappingThread implements Runnable {

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
     * The Collection with the peptides to map.
     */
    private Collection iPeptides = null;

    /**
     * Optional filter for the database.
     */
    private Filter iFilter = null;

    /**
     * Length of stretch of flanking residues to retrieve.
     */
    private int iResidueLength = 0;

    /**
     * The output file.
     */
    private File iOutputFile = null;


    /**
     * This constructor takes a JFrame as owner, a DBLoader to
     * count the entries from, a database filename, the collection of
     * peptides to map, an optional filter, and the output file as parameters.
     *
     * @param   aParent JFrame with the owner for the ProgressBar.
     * @param   aLoader DBLoader implementation instance to request
     *                  the entrycount from.
     * @param   aPeptides   Collection of Strings with the peptide sequences to map.
     * @param   aFilter Filter for the database. Can be 'null' for no filter.
     * @param   aOutputFile File with the outputfile.
     */
    public PeptideMappingThread(JFrame aParent, DBLoader aLoader, Collection aPeptides, Filter aFilter, int aResidueLength, File aOutputFile) {
        this.iParent = aParent;
        this.iLoader = aLoader;
        this.iPeptides = aPeptides;
        this.iFilter = aFilter;
        this.iResidueLength = aResidueLength;
        this.iOutputFile = aOutputFile;
    }

    /**
     * The method that does all the work, once 'start' is called on the
     * owner Thread.
     */
    public void run() {
        try {
            if(iParent != null) {
                if(iParent instanceof CursorModifiable) {
                    ((CursorModifiable)iParent).setCursorOnComponents(new Cursor(Cursor.WAIT_CURSOR));
                } else {
                    iParent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                }
                if(iParent instanceof StatusView) {
                    ((StatusView)iParent).setStatus("Started mapping " + iPeptides.size() + " peptides to protein sequences...");
                }

                // The progress monitor dialog.
                iMonitor = new ProgressMonitor(iParent, "Mapping " + iPeptides.size() + " peptides to protein sequences...", "Initializing...", 0, iLoader.getMaximum()+3);
                iMonitor.setMillisToDecideToPopup(0);
                iMonitor.setMillisToPopup(0);
                iMonitor.setNote("Mapping in progress...");
                iMonitor.setProgress(1);
            }

            // Initialize the HashMap.
            HashMap iMappings = new HashMap();
            for (Iterator lIterator = iPeptides.iterator(); lIterator.hasNext();) {
                String pepSeq = (String)lIterator.next();
                iMappings.put(pepSeq, new InnerProteinCollection(pepSeq));
            }
            // Now start reading the database.
            Protein entry = null;
            boolean cancelled = false;
            // Load first entry.
            if(iFilter == null) {
                entry = iLoader.nextProtein();
            } else {
                entry = iLoader.nextFilteredProtein(iFilter);
            }
            while((entry != null) && (!cancelled)) {
                String proteinSequence = entry.getSequence().getSequence();
                String proteinAccession = entry.getHeader().getAccession();
                String proteinDescription = entry.getHeader().getDescription();
                int proteinScore = entry.getHeader().getScore();
                for (Iterator lIterator = iPeptides.iterator(); lIterator.hasNext();) {
                    String pepSequence = (String)lIterator.next();
                    int start = -1;
                    if((start = proteinSequence.indexOf(pepSequence)) > -1) {
                        InnerProteinCollection proteins = (InnerProteinCollection)iMappings.get(pepSequence);

                        // Retrieve previous stretch.
                        StringBuffer previous = new StringBuffer("");
                        int runningIndex = start;
                        int residueCounter = 0;
                        while(runningIndex > 0 && residueCounter < iResidueLength) {
                            previous.insert(0, proteinSequence.substring(runningIndex - 1, runningIndex));
                            runningIndex--;
                            residueCounter++;
                        }
                        while(previous.length()< iResidueLength) {
                            previous.insert(0, "-");
                        }

                        // Retrieve following stretch.
                        StringBuffer following = new StringBuffer("");
                        runningIndex = start+pepSequence.length();
                        residueCounter = 0;
                        while(runningIndex < proteinSequence.length() && residueCounter < iResidueLength) {
                            following.append(proteinSequence.substring(runningIndex, runningIndex+1));
                            runningIndex++;
                            residueCounter++;
                        }
                        while(following.length()<iResidueLength) {
                            following.append("-");
                        }

                        // Create mapped protein.
                        InnerMappedProtein imp = new InnerMappedProtein(proteinAccession, proteinDescription, previous.toString(), following.toString(), (start+1), start + pepSequence.length(), proteinScore);
                        proteins.addProtein(imp);
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
                // Load next entry.
                if(iFilter == null) {
                    entry = iLoader.nextProtein();
                } else {
                    entry = iLoader.nextFilteredProtein(iFilter);
                }
            }

            // Reset the Loader.
            iLoader.reset();

            // Evaluate primaries in global context.
            if(!cancelled) {
                if(iParent != null) {
                    iMonitor.setNote("Evaluating primary accession numbers...");
                }
                Set primaries = new TreeSet();
                Iterator iter = iMappings.values().iterator();
                while (iter.hasNext()) {
                    InnerProteinCollection lProteins = (InnerProteinCollection)iter.next();
                    // Only do this for those collections that actually contain something.
                    if(lProteins.getMappedProteinsCount() > 0) {
                        // Get the highest scoring protein (the primary).
                        InnerMappedProtein primary = lProteins.getPrimaryProtein();
                        // If the primary is NOT already known, we need to evaluate whether we
                        // want to add it to the list.
                        if(!primaries.contains(primary)) {
                            // OK, new primary. Now first see if any of the others is
                            // already known.
                            Collection lInnerProteins = lProteins.getMappedProteins();
                            boolean replaced = false;
                            for (Iterator lIterator = lInnerProteins.iterator(); lIterator.hasNext();) {
                                InnerMappedProtein lInnerProtein = (InnerMappedProtein)lIterator.next();
                                // If this one is known, and if its score is equal to the
                                // current primary for this collection, we'll retain it as
                                // the primary instead of the current one.
                                if(primaries.contains(lInnerProtein) && lInnerProtein.getScore() == primary.getScore()) {
                                    lProteins.setPrimary(lInnerProtein);
                                    replaced = true;
                                    // Since we keep the one that is sorted the first and
                                    // qualifies, we'll exit evaluating all isoforms here.
                                    break;
                                }
                            }
                            // See if we found an isoform that did qualify.
                            if(!replaced) {
                                // Apparently not. So add the primary to the list as it
                                // is retained.
                                primaries.add(primary);
                            }
                        }
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

                // Now write the output.
                if(iParent != null) {
                    iMonitor.setNote("Primary accession numbers assigned. Writing output...");
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(iOutputFile));
                bw.write("Accession\tStart\tStop\tPrevious (if any)\tSequence\tFollowing (if any)\tDescription\tIsoforms\n");
                iter = iMappings.keySet().iterator();
                while (iter.hasNext()) {
                    String pepSeq = (String)iter.next();
                    InnerProteinCollection mappedProteins = (InnerProteinCollection)iMappings.get(pepSeq);
                    bw.write(mappedProteins.getCSVLine());
                    bw.write("\n");
                }
                bw.flush();
                bw.close();
            }

            // Clean up.
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
                    status = "Cancelled mapping of peptides to proteins. Output file '" + iOutputFile.getAbsolutePath() + "' has been deleted.";
                    iOutputFile.delete();
                } else {
                    // Different status message.
                    status = "Mapped " + iPeptides.size() + " peptides to proteins; output written to ''" + iOutputFile.getAbsolutePath() + "'.";
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
     * Inner class that wraps the necessary information for a mapped protein.
     */
    private class InnerMappedProtein implements Comparable {
        private String iAccession = null;
        private String iDescription = null;
        private int iStart = -1;
        private int iStop = -1;
        private int iScore = -1;
        private String iPrevious = null;
        private String iFollowing = null;


        public InnerMappedProtein(String aAccession, String aDescription, String aPrevious, String aFollowing, int aStart, int aStop, int aScore) {
            iAccession = aAccession;
            iDescription = aDescription;
            iPrevious = aPrevious;
            iFollowing = aFollowing;
            iStart = aStart;
            iStop = aStop;
            iScore = aScore;
        }


        public String getAccession() {
            return iAccession;
        }

        public String getDescription() {
            return iDescription;
        }

        public String getPrevious() {
            return iPrevious;
        }

        public String getFollowing(){
            return iFollowing;
        }

        public int getStart() {
            return iStart;
        }

        public int getStop() {
            return iStop;
        }

        public int getScore() {
            return iScore;
        }

        public String getIsoformString() {
            return iAccession + " (" + iStart + "-" + iStop + ")";
        }

        public int compareTo(Object o) {
            InnerMappedProtein imp = (InnerMappedProtein)o;
            int result = 0;
            result = this.getAccession().compareTo(imp.getAccession());
            if(result == 0){
                result = this.getStart() - imp.getStart();
            }
            return result;
        }

        public boolean equals(Object o) {
            // Test class equality.
            boolean result = false;
            if(this.getClass().getName().equals(o.getClass().getName())) {
                // OK, it is the same class.
                InnerMappedProtein imp = (InnerMappedProtein)o;
                if( (this.getAccession().equals(imp.getAccession())) &&
                    (this.getStart() == imp.getStart()) ) {
                    result = true;
                }
            }
            return result;
        }
    }

    /**
     * Inner class that collects mapped proteins and can be used to assign
     * primaries and isoforms.
     */
    private class InnerProteinCollection {

        private String iPeptideSequence = null;
        private Collection iMappedProteins = null;
        private InnerMappedProtein iPrimary = null;

        public InnerProteinCollection(String aPeptideSequence) {
            iPeptideSequence = aPeptideSequence;
            iMappedProteins = new TreeSet();
        }

        /**
         * This method adds the specified InnerMappedProtein to the
         * collection, while checking whether it should become the new
         * primary. It is assigned the new primary only if one of the following
         * is satisfied: (i) there is no primary yet, or (ii) its protein score
         * is higher than the score of the current primary, or (iii) its score is
         * equal to the current primary's score AND it is sorted before the
         * current primary in alphabetical order (relies on compareTo()).
         *
         * @param aProtein  InnerMappedProtein to add.
         */
        public void addProtein(InnerMappedProtein aProtein) {
            iMappedProteins.add(aProtein);
            // See if we should make it primary.
            if(iPrimary == null) {
                iPrimary = aProtein;
            } else if(aProtein.getScore() > iPrimary.getScore()) {
                iPrimary = aProtein;
            } else if( (aProtein.getScore() == iPrimary.getScore()) && (aProtein.compareTo(iPrimary) < 0) ) {
                iPrimary = aProtein;
            }
        }

        /**
         * This protein sets the specified InnerMappedProtein as the primary.
         * If the specified InnerMappedProtein is not present in this collection,
         * an IllegalArgumentException is thrown.
         *
         * @param aProtein  InnerMappedProtein to set as primary. Must be a member of
         *                  this collection.
         */
        public void setPrimary(InnerMappedProtein aProtein) {
            // Check if this protein is in fact present in the current collection.
            if(!iMappedProteins.contains(aProtein)) {
                throw new IllegalArgumentException("The protein you specified as primary (" + aProtein.getIsoformString() + ") is not part of this collection!");
            }
            // OK, set it as primary.
            iPrimary = aProtein;
        }

        public Collection getMappedProteins() {
            return iMappedProteins;
        }

        public InnerMappedProtein getPrimaryProtein() {
            return this.iPrimary;
        }

        public String getCSVLine() {
            StringBuffer sb = new StringBuffer();

            // Check whether we have any mappings at all.
            if(iMappedProteins.size() == 0) {
                sb.append(" -- No mapping found for sequence: " + iPeptideSequence + "!");
            } else {
                // OK, we have mappings. let's print them.
                // First add the primary.
                sb.append(iPrimary.getAccession()+"\t"+iPrimary.getStart()+"\t"+iPrimary.getStop()+"\t"+((iPrimary.getPrevious()==null)?"-":iPrimary.getPrevious())+"\t"+iPeptideSequence+"\t"+((iPrimary.getFollowing()==null)?"-":iPrimary.getFollowing())+"\t"+iPrimary.getDescription()+"\t");

                // Now add the isoforms.
                Iterator iter = iMappedProteins.iterator();
                while (iter.hasNext()) {
                    InnerMappedProtein lProtein = (InnerMappedProtein)iter.next();
                    if(!lProtein.equals(iPrimary)) {
                        sb.append(lProtein.getIsoformString()+"^A");
                    }
                }
                // Remove trailing '^A' if present.
                if(sb.charAt(sb.length()-1) == 'A' && sb.charAt(sb.length()-2) == '^') {
                    sb.delete(sb.length()-2, sb.length());
                }
            }

            return sb.toString();
        }

        public int getMappedProteinsCount() {
            return iMappedProteins.size();
        }
    }
}

