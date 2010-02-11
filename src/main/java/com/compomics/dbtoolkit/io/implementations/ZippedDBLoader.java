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
import com.compomics.util.io.MonitorableInputStream;
import com.compomics.util.protein.Protein;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the default functionality for a
 * simple zipped flatfile DB loader. It takes care of associating
 * a BufferedReader (iBr) with the zipped flatfile by implementing the
 * 'load' method from the DBLoader interface.
 * It also takes care of filtering raw entries through the implementation
 * of the 'nextFilteredRawEntry' method from the DBLoader interface.
 *
 * @author Lennart Martens
 */
public abstract class ZippedDBLoader implements DBLoader {
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
        // Create a BufferedReader to the file.
        boolean gzip = false;
        try {
            ZipFile zf = new ZipFile(aFilename);
            if(zf.size() > 1) {
                throw new IOException("Unable to load a zip file (" + aFilename + ") with more than one entry!");
            }
            zf.close();
        } catch(ZipException ze) {
            // Could be a GZip. Check this.
            gzip = true;
        }


        iInputStream = new MonitorableInputStream(new FileInputStream(aFilename), true);
        if(!gzip) {
            ZipInputStream zis = new ZipInputStream(iInputStream);
            zis.getNextEntry();
            iBr = new BufferedReader(new InputStreamReader(zis));
        } else {
            iBr = new BufferedReader(new InputStreamReader(new GZIPInputStream(iInputStream)));
        }

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

        // Create a BufferedReader to the file.
        try {
            ZipFile zf = new ZipFile(this.iFilename);
            if(zf.size() > 1) {
                throw new IOException("Unable to load a zip file (" + this.iFilename + ") with more than one entry!");
            }
            zf.close();

            iInputStream = new MonitorableInputStream(new FileInputStream(this.iFilename), true);
            ZipInputStream zis = new ZipInputStream(iInputStream);
            zis.getNextEntry();
            iBr = new BufferedReader(new InputStreamReader(zis));
        } catch(ZipException ze) {
            // GZIP file.
            iInputStream = new MonitorableInputStream(new FileInputStream(this.iFilename), true);
            iBr = new BufferedReader(new InputStreamReader(new GZIPInputStream(iInputStream)));
        }

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

    /**
     * This method attempts to get a reader for the specified zipfile.
     * If the file sepcified is not a zipfile, it will throw an IOException.
     *
     * @param   aFile   File to get a Reader into.
     * @return  BufferedReader  with a Reader into the zipfile.
     * @exception   IOException when the zipfile could not be read or contained multiple entries.
     */
    protected BufferedReader getReader(File aFile) throws IOException {
        BufferedReader result = null;
        // See if the file is a zipfile.
        try {
            try {
                ZipFile zf = new ZipFile(aFile);

                // Find entries in the zipfile.
                Enumeration lEnum = zf.entries();
                ZipEntry ze = null;
                int counter = 0;
                while(lEnum.hasMoreElements()) {
                    counter++;
                    ze = (ZipEntry)lEnum.nextElement();
                    if(counter > 1) {
                        throw new IOException("Can only read files with a single entry!");
                    }
                }
                if(ze != null) {
                    // Construct a BufferedReader around the file.
                    result = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                }
            } catch(ZipException ze) {
                // Could be a Gzip. Let's check.
                result = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(aFile))));
            }
        } catch(Throwable t) {
            if(t instanceof IOException) {
                throw (IOException)t;
            } else {
                throw new IOException("Unable to open zip file! Message: " + t.getMessage() + ".");
            }
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new MonitorableInputStream(new FileInputStream(args[0])))));
            for(int i=0;i<1000;i++) {
                System.out.println(br.readLine());
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
