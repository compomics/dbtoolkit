/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 10-okt-02
 * Time: 18:10:12
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class attempts to determine the DB type from the
 * file it is presented with. If successful, it will return
 * the correct DBLoader implementation for the file,
 * else it will throw an UnknownDBFormatException.
 *
 * @author Lennart Martens
 */
public class AutoDBLoader {
    /**
     * String[] with all the DBLoader classes that are possible candidates for
     * loading the database.
     */
    private String[] iDBLoaderClasses = null;

    /**
     * The constructor for this class requires a list of DBLoaders to contact
     * when attempting to find a compliant DBLoader.
     *
     * @param   aDBLoaderClasses    String[] with the fully qualified classnames
     *                              for all known DBLoaders.
     */
    public AutoDBLoader(String[] aDBLoaderClasses) {
        iDBLoaderClasses = aDBLoaderClasses;
    }

    /**
     * This method attempts to determine the correct DBLoader
     * implementation for the specified file.
     * If it is successful, it returns that DBLoader,
     * if it is unsuccessful, it will throw an UnknownDBFormatException. <br />
     * Note that the DBLoader is automatically initialized with the file,
     * so the caller of this method need not call the 'load()' method on the
     * loader anymore.
     *
     * @param   aFilename   String with the filename for the DB to
     *                      find the DBLoader for.
     * @return  DBLoader    implementation fo rthe specified file.
     * @exception   UnknownDBFormatException    when the format of
     *                                          the DB was unknown.
     * @exception   IOException when the file could not be read.
     */
    public DBLoader getLoaderForFile(String aFilename) throws IOException, UnknownDBFormatException {
        return this.getLoaderForFile(aFilename, true);
    }

    /**
     * This method attempts to determine the correct DBLoader
     * implementation for the specified file.
     * If it is successful, it returns that DBLoader,
     * if it is unsuccessful, it will throw an UnknownDBFormatException.
     * The boolean argument allows you to specify whether the loader
     * should be initialized before returning by the AutoDBLoader.
     *
     * @param   aFilename   String with the filename for the DB to
     *                      find the DBLoader for.
     * @param   aInitLoader boolean that indicates whether the DBLoader
     *                      should already load the specified file.
     * @return  DBLoader    implementation fo rthe specified file.
     * @exception   UnknownDBFormatException    when the format of
     *                                          the DB was unknown.
     * @exception   IOException when the file could not be read.
     */
    public DBLoader getLoaderForFile(String aFilename, boolean aInitLoader) throws IOException, UnknownDBFormatException {
        DBLoader loader = null;

        // Check for the existance of the specified file.
        File temp = new File(aFilename);
        if(!temp.exists()) {
            throw new IOException("File '" + aFilename + "' not found!");
        }

        // Okay, the file exists.
        for(int i = 0; i < iDBLoaderClasses.length; i++) {
            String lClass = iDBLoaderClasses[i];
            try {
                Class c = Class.forName(lClass);
                Constructor constr = c.getConstructor(new Class[]{});
                Object o = constr.newInstance(new Object[]{});
                if(o instanceof DBLoader) {
                    DBLoader candidate = (DBLoader)o;
                    if(candidate.canReadFile(temp)) {
                        loader = candidate;
                        break;
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        // If our loader at this point is 'null', throw an UnknownDBException.
        // This means not a single loader reported an acquintance with the specified file!
        if(loader == null) {
            throw new UnknownDBFormatException("Unable to determine DB format of the specified file!", aFilename);
        }

        // See if we should already initialize the DBLoader.
        if(aInitLoader) {
            loader.load(aFilename);
        }

        return loader;
    }
}
