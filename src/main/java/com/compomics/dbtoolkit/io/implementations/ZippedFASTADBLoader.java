/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 2-okt-02
 * Time: 16:48:34
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the DBLoader interface for any zipped FASTA database. 
 * Please note that calls to 'nextRawEntry' and 'nextFilteredRawEntry' are identical to
 * 'nextFASTAEntry' and 'nextFilteredFASTAEntry', respectively.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.io.interfaces.DBLoader
 */
public class ZippedFASTADBLoader extends ZippedDBLoader {

    private String iLastLine = null;
    private boolean iFirstLine = true;

    /**
     * Default constructor.
     */
    public ZippedFASTADBLoader() {}

    /**
     * This method returns the next raw entry as present in the flatfile.
     * It is mainly useful for testing the class, as we typically want to retrieve
     * some sort of processed version of the data.
     *
     * @return  String with the next raw entry as present in the particular DB.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public String nextRawEntry() throws IOException {
        String result = null;

        boolean first = true;

        // First check EOF condition. this is flagged by the BufferedReader by returning
        // 'null' when reading.
        String line = null;
        if(iFirstLine) {
            line = iBr.readLine();
            iFirstLine = false;
        } else {
            line = iLastLine;
        }
        if(line != null) {
            StringBuffer lSB = new StringBuffer();
            // Separate header and sequence.
            line += "\n";
            // Okay, not EOF.
            // Now read a record (end-of-record is marked by a line starting with '>').
            while((line != null) && (first || !line.trim().startsWith(">"))) {
                first = false;
                lSB.append(line);
                // Move to next line.
                line = iBr.readLine();
            }
            result = lSB.toString();
            iLastLine = line;
        }
        return result;
    }

    /**
     * This method will report on the next entry in the DB in FASTA format.
     * The information content relative to the original format is implementation dependant.
     *
     * @return  String  with the FASTA entry representing the next entry.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public String nextFASTAEntry() throws IOException {
        return this.nextRawEntry();
    }

    /**
     * This method will report on the next filtered entry in the DB in FASTA format.
     * The information content relative to the original format is implementation dependant.
     *
     * @param   aFilter Filter instance against which to check the entries.
     * @return  String  with the FASTA entry representing the next entry.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public String nextFilteredFASTAEntry(Filter aFilter) throws IOException {
        return this.nextFilteredRawEntry(aFilter);
    }

    /**
     * This method reports on the name of the Database that is read and interpreted by the
     * implemented instance. Mostly for debugging and testing purposes.
     *
     * @return  String  with the name of the DB format that is read and interpreted by the
     *                  implementation.
     */
    public String getDBName() {
        return DBLoader.FASTA;
    }

    /**
     * This method attempts to count the number of entries currently in the database. 
     * <b<Note</b> that a call to this method resets the position of the underlying reader!
     *
     * @return  long    with the number of entries or DBLoader.CANCELLEDCOUNT if the count was cancelled.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public long countNumberOfEntries() throws IOException {
        // We'll cycle the DB and read all entries,
        this.reset();
        // Clean-up any leftover cancellation stuff.
        this.iCancelCount = false;
        String line = null;
        long counter = 0;
        // Also check the cancellation boolean!
        while(((line = iBr.readLine()) != null) && !iCancelCount) {
            if(line.trim().startsWith(">")) {
                counter++;
            }
        }
        this.reset();

        // If cancelled, return the cancelled status.
        if(iCancelCount) {
            iCancelCount = false;
            counter = DBLoader.CANCELLEDCOUNT;
        }
        return counter;
    }

    /**
     * This method resets the reader to the start of the file.
     *
     * @exception   IOException when the buffer reset operation failed.
     */
    public void reset() throws IOException {
        super.reset();
        this.iFirstLine = true;
        this.iLastLine = null;
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
     * @return  boolean 'true' if this DBLoader assumes it can read this format,
     *                  'false' otherwise.
     */
    public boolean canReadFile(File aFile) {
        boolean canRead = false;

        try {
            BufferedReader br = super.getReader(aFile);
            // read first line.
            String line = br.readLine();
            // Skip leading blank lines.
            while(line != null && line.trim().equals("")) {
                line = br.readLine();
            }
            // Check for 'null'
            if(line != null) {
                if(line.trim().startsWith(">")) {
                    canRead = true;
                }
            }
            br.close();

        } catch(IOException ioe) {
        }

        return canRead;
    }
}
