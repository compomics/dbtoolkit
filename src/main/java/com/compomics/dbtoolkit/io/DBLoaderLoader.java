/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-aug-2003
 * Time: 12:47:21
 */
package com.compomics.dbtoolkit.io;

import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.toolkit.CountEntries;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class allows the creation of a DBLoader from a File pointer to the DB file.
 *
 * @author Lennart Martens
 */
public class DBLoaderLoader {

    /**
     * This merhod returns a DBLoader for the specified input file.
     *
     * @param aInputFile    File with the input DB file.
     * @return  DBLoader with the DBLoader for the file.
     * @throws IOException  when the DB could not be associated with a loader.
     */
    public static DBLoader loadDB(File aInputFile) throws IOException {
        DBLoader loader = null;

        Properties p = null;

        InputStream is = CountEntries.class.getClassLoader().getResourceAsStream("DBLoaders.properties");
        p = new Properties();
        if(is != null) {
            p.load(is);
            is.close();
        }

        // See if we managed to load the 'DBLoader.properties' file, else default to built-in types.
        if(p == null || p.size() == 0) {
            System.out.println("\t - Unable to find 'DBLoaders.properties' file, defaulting to built-in types (SwissProt & FASTA only!)...");
            p = new Properties();
            p.put("1", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader");
            p.put("2", "com.compomics.dbtoolkit.io.implementations.FASTADBLoader");
        }
        String[] classNames = new String[p.size()];
        Iterator it = p.values().iterator();
        int counter = 0;
        while(it.hasNext()) {
            classNames[counter] = (String)it.next();
            counter++;
        }

        AutoDBLoader adb = new AutoDBLoader(classNames);

        try {
            loader = adb.getLoaderForFile(aInputFile.getAbsolutePath());
        } catch(UnknownDBFormatException udfe) {
            throw new IOException("Unknown database format: " + udfe.getMessage());
        }

        if(loader == null) {
            throw new IOException("Unable to determine database type for your inputfile (" + aInputFile + "), exiting...");
        }

        return loader;
    }
}
