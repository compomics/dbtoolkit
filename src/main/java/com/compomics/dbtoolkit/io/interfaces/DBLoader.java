/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 13:50:29
 */
package com.compomics.dbtoolkit.io.interfaces;

import com.compomics.util.interfaces.Monitorable;
import com.compomics.util.protein.Protein;

import java.io.File;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This interface describes the behaviour for a DBLoader implementation.
 * These classes are used to access a sequence database in a certain
 * format from a flatfile.
 *
 * @author Lennart Martens
 */
public interface DBLoader extends Monitorable {

    /**
     * Constant with the name for the SwissProt DB.
     */
    public static final String SWISSPROT = "SwissProt";

    /**
     * Constant with the name for FASTA DB.
     */
    public static final String FASTA = "FASTA";

    /**
     * Constant for the automatic detection loader.
     */
    public static final String AUTO = "Automatic";

    /**
     * The int you can expect from a counting method when the count was cancelled.
     * Callers of the count methods are advised to first check their result against this value.
     */
    public static final long CANCELLEDCOUNT = -1;

    /**
     * This method allows the caller to load a DB from the specified file.
     * It is implementation dependent whether the DB is cached or even loaded fully
     * in memory.
     *
     * @param   aFilename   String with the filename for the flatfile from which to load the DB.
     * @exception   IOException whenever the file is inaccessible.
     */
    public abstract void load(String aFilename) throws IOException;

    /**
     * This method returns the next raw entry as present in the flatfile.
     * It is mainly useful for testing the class, as we typically want to retrieve
     * some sort of processed version of the data.
     *
     * @return  String with the next raw entry as present in the particular DB.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public abstract String nextRawEntry() throws IOException;

    /**
     * This method will report on the next entry in the DB in FASTA format.
     * The information content relative to the original format is implementation dependant.
     *
     * @return  String  with the FASTA entry representing the next entry.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public abstract String nextFASTAEntry() throws IOException;

    /**
     * This method returns the next filtered raw entry as present in the flatfile.
     * It is mainly useful for testing the class, as we typically want to retrieve
     * some sort of processed version of the data.
     *
     * @param   aFilter Filter instance against which to check the entries.
     * @return  String with the next raw entry as present in the particular DB.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public abstract String nextFilteredRawEntry(Filter aFilter) throws IOException;

    /**
     * This method will report on the next filtered entry in the DB in FASTA format.
     * The information content relative to the original format is implementation dependant.
     *
     * @param   aFilter Filter instance against which to check the entries.
     * @return  String  with the FASTA entry representing the next entry.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public abstract String nextFilteredFASTAEntry(Filter aFilter) throws IOException;

    /**
     * This method reports on the name of the Database that is read and interpreted by the
     * implemented instance. Mostly for debugging and testing purposes.
     *
     * @return  String  with the name of the DB format that is read and interpreted by the
     *                  implementation.
     */
    public abstract String getDBName();

    /**
     * This method returns the next entry in the DB as a Protein instance.
     *
     * @return  Protein the Protein instance that corresponds to the next entry in the
     *                  DB.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public abstract Protein nextProtein() throws IOException;

    /**
     * This method returns the next filtered entry in the DB as a Protein instance.
     *
     * @return  Protein the Protein instance that corresponds to the next filtered
     *                  entry in the DB.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public abstract Protein nextFilteredProtein(Filter aFilter) throws IOException;

    /**
     * This method attempts to count the number of entries currently in the database. 
     * Performance can be quite slow, depending on the specific implementation.
     *
     * @return  long    with the number of entries.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public abstract long countNumberOfEntries() throws IOException;

    /**
     * This method resets the reader to the start of the file.
     *
     * @exception   IOException when the buffer reset operation failed.
     */
    public abstract void reset() throws IOException;

     /**
     * This method cancels a count in progress.
      * Typically, a cancelled count is expected to return the
      * CANCELLEDCOUNT value as defined on this interface.
     */
    public abstract void cancelCount();

    /**
     * Signals the loader implementation to release all resources.
     */
    public abstract void close();

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
    public boolean canReadFile(File aFile);
}
