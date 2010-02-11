/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 14:01:18
 */
package com.compomics.dbtoolkit.io;

import com.compomics.dbtoolkit.io.implementations.FASTADBLoader;
import com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class provides a Factory for creation of specific DBLoader
 * implementations.
 *
 * @author Lennart Martens.
 */
public class DBLoaderFactory {

    /**
     * Private constructor.
     */
    private DBLoaderFactory() {
    }

    /**
     * This method allows the creation of a DBLoader implementation based on the
     * int parameter, which is verified against the DBConverter inventory of known DB's.
     *
     * @param   aDBid   String with the DB identifier. This ID should be one of the
     *                      public static final Strings defined on the DBLoader interface.
     * @return  DBLoader    the DBLoader implementation for the specified identifier.
     * @exception   UnknownDBFormatException    when the identifier was not recognized.
     * @see com.compomics.dbtoolkit.DBConverter
     */
    public static DBLoader getDBLoader(String aDBid) throws UnknownDBFormatException {
        DBLoader result = null;
        if(aDBid == null || aDBid.trim().equals("")) {
            // Error. Do nothing.
        } else if(aDBid.equals(DBLoader.SWISSPROT)) {
            result = new SwissProtDBLoader();
        } else if(aDBid.equals(DBLoader.FASTA)) {
            result = new FASTADBLoader();
        }
        // See if we have something, throw an error if we don't.
        if(result == null) {
            throw new UnknownDBFormatException(aDBid);
        } else {
            return result;
        }
    }
}
