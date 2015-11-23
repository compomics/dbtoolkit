/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-jan-03
 * Time: 18:14:40
 */
package com.compomics.dbtoolkit.io.implementations;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a sequence region.
 * It holds the accession number for the sequence,
 * the query sequence, the number of residues on either side to include and
 * the residues from either side that were retrieved. The last fields can be 'null'
 * when no retrieval operation has been performed yet.
 *
 * @author Lennart Martens
 */
public class SequenceRegion {

    /**
     * This variable holds the accession number.
     */
    private String iAccession = null;

    /**
     * This variable holds the query sequence.
     */
    private String iSequence = null;

    /**
     * This query holds the N-terminally added residues.
     */
    private String iNtermAdd = null;

    /**
     * This variable holds the C-terminally added residues.
     */
    private String iCtermAdd = null;

    /**
     * This variable holds the maximum number of N-terminal residues
     * to retrieve.
     */
    private int iNtermResidues = 0;

    /**
     * This variable holds the maximum number of C-terminal residues
     * to retrieve.
     */
    private int iCtermResidues = 0;

    /**
     * Flag to indicate whether or not a sequence match was obtained.
     */
    private boolean iFound = false;

    /**
     * Flag to indicate whether or not a query was launched.
     */
    private boolean iQueried = false;

    /**
     * This constructor takes an accession number, a query sequence
     * and a single residue count to add. This addition will be performed
     * symmetrically.
     *
     * @param   aAccession  String with the accession number for the entry.
     * @param   aSequence   String with the query sequence.
     * @param   aResidues   int with the number of residues to retrieve on both
     *                      the C and N-terminal end of the query sequence.
     */
    public SequenceRegion(String aAccession, String aSequence, int aResidues) {
        this(aAccession, aSequence, aResidues, aResidues);
    }

    /**
     * This constructor takes an accession number, a query sequence
     * and two distinct residue counts to add on the C-terminal and
     * N-terminal count.
     *
     * @param   aAccession  String with the accession number for the entry.
     * @param   aSequence   String with the query sequence.
     * @param   aNtermResidues   int with the number of residues to retrieve
     *                           on the N-terminal end of the query sequence.
     * @param   aCtermResidues   int with the number of residues to retrieve
     *                           on the C-terminal end of the query sequence.
     *
     */
    public SequenceRegion(String aAccession, String aSequence, int aNtermResidues, int aCtermResidues) {
        this.iAccession = aAccession;
        this.iSequence = aSequence;
        this.iNtermResidues = aNtermResidues;
        this.iCtermResidues = aCtermResidues;
        this.iFound = false;
        this.iQueried = false;
    }

    /**
     * This method returns the accession number for the current
     * sequenceregion.
     *
     * @return  String  with the accession number.
     */
    public String getAccession() {
        return iAccession;
    }

    /**
     * This method returns the query sequence used to obtain a Sequenceregion.
     *
     * @return  String  with the query sequence.
     */
    public String getQuerySequence() {
        return iSequence;
    }

    /**
     * This method reports on the total sequence region after retrieval,
     * or 'null' if no retrieval has taken place or no hit was found.
     *
     * @return  String  with the retrieved sequence region ('null' if no retrieval
     *                  has occurred, or no match was found).
     */
    public String getRetrievedSequence() {
        String result = null;
        if(iQueried && iFound) {
            result = iNtermAdd + iSequence + iCtermAdd;
        }
        return result;
    }

    /**
     * This method reports on the N-terminal region retrieved using the
     * query sequence. This can be 'null' if no retrieve has taken place or no
     * match was found. Empty String denotes the fact that no N-terminal residues
     * were found relative to the query String.
     *
     * @return  String  with the N-terminal part of the retrieved sequence ('null'
     *                  if no retrieval has occurred, or no match was found).
     */
    public String getNterminalAddition() {
        return iNtermAdd;
    }

    /**
     * This method reports on the C-terminal region retrieved using the
     * query sequence. This can be 'null' if no retrieve has taken place or no
     * match was found. Empty String denotes the fact that no C-terminal residues
     * were found relative to the query String.
     *
     * @return  String  with the C-terminal part of the retrieved sequence ('null'
     *                  if no retrieval has occurred, or no match was found).
     */
    public String getCterminalAddition() {
        return iCtermAdd;
    }

    /**
     * This method reports on the number of N-terminal residues the user requested
     * to be retrieved. This number is a setting, NOT a result. When the retrieved length is
     * requested, one must first request the N-terminal addition and then request the length
     * on that String. This is because a user can request many residues, but sometimes less
     * (or even no) N-terminal residues are present relative to the query sequence.
     *
     * @return  int with the requested number of N-terminally retrieved residues.
     */
    public int getNterminalResidueCount() {
        return iNtermResidues;
    }

    /**
     * This method reports on the number of C-terminal residues the user requested
     * to be retrieved. This number is a setting, NOT a result. When the retrieved length is
     * requested, one must first request the C-terminal addition and then request the length
     * on that String. This is because a user can request many residues, but sometimes less
     * (or even no) C-terminal residues are present relative to the query sequence.
     *
     * @return  int with the requested number of C-terminally retrieved residues.
     */
    public int getCterminalResidueCount() {
        return iCtermResidues;
    }

    /**
     * This method indicates whether this SequenceRegion has been queried in the database.
     *
     * @return  boolean indicating whether this sequence has been queried.
     */
    public boolean isQueried() {
        return iQueried;
    }

    /**
     * This method indicates whether this SequenceRegion has been found in the database.
     * When this boolean is 'true', it is automatically necessary that the 'isQueried()' method
     * also returns 'true'.
     *
     * @return  boolean indicating whether this sequence has been found.
     */
    public boolean isFound() {
        return iFound;
    }

    /**
     * This method can be used to set the SequenceRegion as 'queried'.
     *
     * @param   aQueried    boolean to indicate whether the sequence has
     *                      been queried or not.
     */
    public void setQueried(boolean aQueried) {
        iQueried = aQueried;
    }

    /**
     * This method can be used to indicate whether the queried sequence
     * was in fact found in the DB.
     *
     * @param   aFound  boolean to indicate whether the sequence has been found.
     */
    public void setFound(boolean aFound) {
        iFound = aFound;
    }

    /**
     * This method allows the caller to set an N-terminal addition.
     *
     * @param   aNterm  String with the found N-terminal addition.
     */
    public void setNterminalAddition(String aNterm) {
        iNtermAdd = aNterm;
    }

    /**
     * This method allows the caller to set a C-terminal addition.
     *
     * @param   aCterm  String with the C-terminal addition.
     */
    public void setCterminalAddition(String aCterm) {
        iCtermAdd = aCterm;
    }

    /**
     * This method returns a String containing a description of the
     * current object.
     *
     * @return  String  with the String representation of the current object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("SequenceRegion with:\n");
        sb.append("\t- Accession: '" + iAccession + "'\n");
        sb.append("\t- Query sequence: '" + iSequence + "'\n");
        sb.append("\t- N-term and C-term residues: " + iNtermResidues + "; " + iCtermResidues + "\n");
        sb.append("\t- Queried: " + iQueried + "\n");
        sb.append("\t- Found: " + iFound + "\n");
        sb.append("\t- Nterm addition: '" + iNtermAdd + "'\n");
        sb.append("\t- Cterm addition: '" + iCtermAdd + "'\n");

        return sb.toString();
    }
}
