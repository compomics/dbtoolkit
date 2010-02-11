/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 2-okt-02
 * Time: 16:53:44
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.io.MonitorableFileInputStream;
import com.compomics.util.io.MonitorableInputStream;
import com.compomics.util.protein.Protein;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the default functionality for a
 * simple flatfile DB loader. It takes care of associating
 * a BufferedReader (iBr) with the flatfile by implementing the
 * 'load' method from the DBLoader interface.
 * It also takes care of filtering raw entries through the implementation
 * of the 'nextFilteredRawEntry' method from the DBLoader interface.
 *
 * @author Lennart Martens
 */
public abstract class DefaultDBLoader implements DBLoader {
    /**
     * Debugger variable. Enable to receive more verbose lifecycle output.
     */
    protected final static boolean debug = false;

    /**
     * This is the BufferedReader that points to the DB flatfile.
     */
    protected BufferedReader iBr = null;

    /**
     * This is the filename we're pointing to.
     */
    protected String iFilename = null;

    /**
     * This is the InputStream on which the BufferedReader is built.
     */
    protected MonitorableInputStream iInputStream = null;

    /**
     * This boolean can be flagged when a count is in progress,
     * and will then cancel the count.
     */
    protected boolean iCancelCount = false;


    /**
     * This method allows the caller to load a DB from the specified file.
     * This implementation does not cache entries, but reads them from file as it goes along.
     *
     * @param   aFilename   String with the filename for the flatfile from which to load the SwissProt DB.
     * @exception   IOException whenever the file is inaccessible.
     */
    public void load(String aFilename) throws IOException {
        // First of all: check if the file exists.
        File lFile = new File(aFilename);
        if(!lFile.exists()) {
            throw new IOException("File '" + aFilename + "' does not exist!");
        }

        // Create a BufferedReader to the file.
        iInputStream = new MonitorableFileInputStream(lFile);
        iBr = new BufferedReader(new InputStreamReader(iInputStream));
        // Keep the filename.
        iFilename = aFilename;
    }

    /**
     * When destroying the Object, we should eliminate the file pointer.
     */
    public void finalize() {
        if(debug) {
            System.out.print("Finalizing " + this.getDBName() + "...  ");
        }
        this.close();
    }

    /**
     * This method is used by the AutoDBLoader class.
     * It cycles all available known DBLoaders until one is found that
     * returns 'true' on this query. Please note that the order in which the
     * DBLoaders are attempted is not fixed! The first instance encountered that
     * returns 'true' will load the DB!
     *
     * @param   aFile   File with the canonical name of the file to read. This
     *                  file is supposed to be existant and readable.
     * @return  boolean This default implementation always returns 'false'!
     */
    public boolean canReadFile(File aFile) {
        return false;
    }

    /**
     * This method reports on the next entry that passes the filter in
     * raw format. This method is mainly present for testing and internal use.
     *
     * @param   aFilter Filter instance against which checks are made.
     * @return  String  with the next filtered raw entry, or 'null' if no more
     *                  passed entries are found.
     * @exception   IOException when something goes wrong while reading the DB file.
     */
    public String nextFilteredRawEntry(Filter aFilter) throws IOException {
        String result = null;
        boolean lContinue = true;
        while(lContinue) {
            String tempString = this.nextRawEntry();
            // First check for 'null' (no more entries),
            // and if not 'null', check for filter passing.
            if(tempString == null) {
                lContinue = false;
                continue;
            } else if(aFilter.passesFilter(tempString)) {
                result = tempString;
                lContinue = false;
            }
        }

        return result;
    }

    /**
     * This method returns the next entry in the DB as a Protein instance.
     *
     * @return  Protein the Protein instance that corresponds to the next entry in the
     *                  DB.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public Protein nextProtein() throws IOException {
        Protein p = null;
        String fasta = this.nextFASTAEntry();
        if(fasta != null) {
            p = new Protein(fasta);
        }
        return p;
    }

    /**
     * This method returns the next filtered entry in the DB as a Protein instance.
     *
     * @return  Protein the Protein instance that corresponds to the next filtered
     *                  entry in the DB.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public Protein nextFilteredProtein(Filter aFilter) throws IOException {
        Protein p = null;
        String fasta = this.nextFilteredFASTAEntry(aFilter);
        if(fasta != null) {
            p = new Protein(fasta);
        }
        return p;
    }

    /**
     * This method resets the reader to the start of the file.
     *
     * @exception   IOException when the buffer reset operation failed.
     */
    public void reset() throws IOException {
        iBr.close();
        iInputStream.close();
        iInputStream = new MonitorableFileInputStream(this.iFilename);
        iBr = new BufferedReader(new InputStreamReader(this.iInputStream));
    }

    /**
     * This method returns the maximum amount of information (in bytes)
     * that can be read from this DB.
     *
     * @return  int with the maximum amount of information (in bytes) that can be read
 *                  from this DB.
     */
    public int getMaximum() {
        return iInputStream.getMaximum();
    }

    /**
     * This method reports on the progress of the current DBLoader in the total file.
     *
     * @return  int with the number of bytes read from the DB file up till now.
     */
    public int monitorProgress() {
        return iInputStream.monitorProgress();
    }

    /**
     * This method cancels a count in progress.
     */
    public void cancelCount() {
        this.iCancelCount = true;
    }

    /**
     * Signals the loader implementation to release all resources.
     */
    public void close() {
        try {
            if(iBr != null) {
                iBr.close();
                iBr = null;
            }
            if(iInputStream != null) {
                iInputStream.close();
                iInputStream = null;
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
