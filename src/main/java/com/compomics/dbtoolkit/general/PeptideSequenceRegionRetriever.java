/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 13-jan-03
 * Time: 18:38:57
 */
package com.compomics.dbtoolkit.general;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.implementations.SequenceRegion;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.protein.Protein;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class allows the user to obtain a sequence region from a protein,
 * based on an accession number and a peptide of that protein.
 * E.G., when searching for patterns around identified sequences.
 *
 * @author Lennart Martens
 */
public class PeptideSequenceRegionRetriever {

    /**
     * The DBLoader associated with the DB to retrieve sequences from.
     */
    private DBLoader iLoader = null;

    /**
     * This constructor allows the creation of a PeptideSequenceRegionRetriever
     * based on the database the sequences regions should be retrieved from.
     *
     * @param   aDBFilename String with the filename for the DB to be loaded.
     * @exception   UnknownDBFormatException when the DB type could not be recognized.
     * @exception   IOException when the load operation failed.
     */
    public PeptideSequenceRegionRetriever(String aDBFilename) throws UnknownDBFormatException, IOException {
        iLoader = new AutoDBLoader(new String[]{"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader"}).getLoaderForFile(aDBFilename);
    }

    /**
     * This method closes the retriever.
     */
    public void close() {
        if(iLoader != null) {
            iLoader.close();
        }
    }

    public void finalize(){
        this.close();
    }

    /**
     * This method allows the caller to retrieve a single sequence region
     * based on the SequenceRegion passed in.
     *
     * @param   aRegion     SequenceRegion to query with.
     * @return  SequenceRegion    with the filled-in SequenceRegion.
     * @exception   IOException when reading the database failed.
     */
    public SequenceRegion retrieveSequenceRegion(SequenceRegion aRegion) throws IOException {
        Vector v = new Vector(1);
        v.add(aRegion);
        v = this.retrieveSequenceRegions(v);
        SequenceRegion result = (SequenceRegion)v.get(0);
        return result;
    }

    /**
     * This method allows the caller to retrieve a set of sequence regions
     * based on the SequenceRegions contained in the Vector.
     *
     * @param   aRegions    Vector with the SequenceRegion instances to
     *                      query with.
     * @return  Vector    with the filled-in SequenceRegions in the Vector.
     * @exception   IOException when reading the database failed.
     */
    public Vector retrieveSequenceRegions(Vector aRegions) throws IOException{
        iLoader.reset();
        int liSize = aRegions.size();
        HashMap all = new HashMap(liSize);
        for(int i = 0; i < liSize; i++) {
            Object o = (Object)aRegions.elementAt(i);
            if(o instanceof SequenceRegion) {
                SequenceRegion s = (SequenceRegion)o;
                s.setQueried(true);
                // Chances are a single accession number is present more than once in
                // the query. We should allow for this! So first check whether it is present
                // already.
                if(all.containsKey(s.getAccession())) {
                    // See if a subdivision has been made.
                    Object check = all.get(s.getAccession());
                    if(check instanceof Integer) {
                        // Already a subdivision in progress.
                        // Find out how deep.
                        int count = ((Integer)check).intValue();
                        // Add it to the end.
                        count++;
                        all.put(s.getAccession() + "~" + count, s);
                        // Update the counter.
                        all.put(s.getAccession(), new Integer(count));
                    } else {
                        // First one, split it out.
                        int count = 1;
                        // Original one gets index '1'.
                        all.put(s.getAccession() + "~" + count, check);
                        // Current one gets one more.
                        count++;
                        all.put(s.getAccession() + "~" + count, s);
                        // Store a counter as a flag.
                        all.put(s.getAccession(), new Integer(count));
                    }
                } else {
                    // Normal put here.
                    all.put(s.getAccession(), s);
                }

            }
        }
        // Cycle our DB.
        Protein p = null;
        while((p = iLoader.nextProtein()) != null) {
            String accession = p.getHeader().getAccession();
            // We found an entry with a query for this DB entry.
            if(all.containsKey(accession)) {
                Object stored = all.get(accession);
                // The item can either be a SequenceRegion alone,
                // or a collection of SequenceRegions (different pieces of
                // sequences yet with the same accession number).
                if(stored instanceof SequenceRegion) {
                    this.processRegionInProtein((SequenceRegion)stored, p);
                } else {
                    // Find out how many similar ones we have.
                    int count = ((Integer)stored).intValue();
                    // Simply cycle them all.
                    for(int i=1;i<=count;i++) {
                        this.processRegionInProtein((SequenceRegion)all.get(accession + "~" + i), p);
                    }
                }
            }
        }

        return aRegions;
    }

    /**
     * This method does the real searching and retrieving per query sequence and protein.
     * The parameters are changed due to a pass by reference... Don't mess with this unless
     * you are very sure what you're doing.
     * This is NOT a very good piece of Java code!
     *
     * @param   aRegion SequenceRegion to update after being queried.
     * @param   aParent Protein with the information we want to query.
     */
    private void processRegionInProtein(SequenceRegion aRegion, Protein aParent) {
        String temp = aParent.getSequence().getSequence();
        int location = temp.indexOf(aRegion.getQuerySequence());
        if(location<0) {
            aRegion.setFound(false);
        } else {
            aRegion.setFound(true);
            // N-terminal fragment.
            int startLoc = location-aRegion.getNterminalResidueCount();
            if(startLoc < 0) {
                startLoc = 0;
            }
            aRegion.setNterminalAddition(temp.substring(startLoc, location));
            // C-terminal fragment.
            startLoc = location + aRegion.getQuerySequence().length();
            int endLoc = startLoc + aRegion.getCterminalResidueCount();
            if(endLoc > temp.length()) {
                endLoc = temp.length();
            }
            aRegion.setCterminalAddition(temp.substring(startLoc, endLoc));
        }
    }
}
