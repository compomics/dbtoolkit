/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 13-okt-02
 * Time: 15:17:36
 */
package com.compomics.dbtoolkit.gui;

import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class holds all the data and operations on the data for
 * the DBTool GUI.
 *
 * @author Lennart Martens
 */
public class DBToolModel {

    /**
     * The filename of the DB to process.
     */
    private String iFilename = null;

    /**
     * The DBLoader for the DB to process.
     */
    private DBLoader iLoader = null;

    /**
     * Automatic DBLoader recognition component.
     */
    private AutoDBLoader iAuto = null;

    /**
     * This Vector will hold the original entries to display in the preview pane.
     */
    private Vector iEntries = null;

    /**
     * This String is the filename from which the iEntries cache has been read.
     */
    private String iCacheFilename = null;

    /**
     * The number of preview entries.
     */
    private int iNrOfEntries = 2;


    /**
     * All the names of the known DBLoaders.
     */
    private static String[] iDBNames = null;

    /**
     * Properties with the classnames for all the known DBLoaders.
     */
    private static Properties iDBLoaders = null;

    /**
     * The classnames for all the known DBLoaders.
     */
    private static String[] iDBLoaderClasses = null;

    /**
     * Default constructor.
     */
    public DBToolModel() {
        // See if we need to update the list of known DBLoaders.
        if((iDBLoaders == null) || (iDBNames == null)) {
            this.loadDBs();
        }
        iAuto = new AutoDBLoader(this.getAllDBLoaderClassNames());
    }

    /**
     * Simple getter for the filename.
     *
     * @return  String with the filename, or 'null' if none is set.
     */
    public String getFilename() {
        return iFilename;
    }

    /**
     * Setter for the filename.
     *
     * @param aFilename String with the filename.
     */
    public void setFilename(String aFilename) {
        iFilename = aFilename;
    }

    /**
     * Simple getter for the number of entries to display.
     *
     * @return  int with the number of entries currently cached for
     *              display.
     */
    public int getNrOfEntries() {
        return iNrOfEntries;
    }

    /**
     * Simple getter for the DBLoader.
     *
     * @return  DBLoader    with the DBLoader implementation.
     */
    public DBLoader getLoader() {
        return iLoader;
    }

    /**
     * Setter for the DBLoader.
     *
     * @param   aLoader the DBLoader to set.
     */
    public void setLoader(DBLoader aLoader) {
        iLoader = aLoader;
    }

    /**
     * Setter for the number of entries to display.
     *
     */
    public void setNrOfEntries(int aNrOfEntries) {
        iNrOfEntries = aNrOfEntries;
    }

    /**
     * This method reports on the preview text, formatted for the specified number
     * of characters available per line. <br />
     * Note that this method will re-read the file if necessary.
     *
     * @param   aCharsPerLine   int with the maximum number of characters per line.
     * @return  String  with the text for the preview pane.
     */
    public String getPreviewText(int aCharsPerLine) throws IOException {
        // The cache for our text inside this method.
        StringBuffer text = new StringBuffer("");

        // First of all, see if there is text to preview.
        // If there isn't (basically: no filename present)
        // just return the empty String.
        if(iFilename != null) {
            // Okay, since we have a filename set, we should also have a
            // pointer to the DB in DBLoader and possibly even a cached set of
            // preview lines in the Vector.
            // Let's check these.
            if((iEntries == null) || ((iEntries.size()/2) != iNrOfEntries) || (!iFilename.equals(iCacheFilename))) {
                // Cache is invalid. We'll have to re-read the entries.
                // First reset the reader.
                iLoader.reset();
                // Now reset the cache.
                iEntries = new Vector();
                // Now do the re-reading to reconstruct the cache.
                for(int i=0;i<iNrOfEntries;i++) {
                    String entry = iLoader.nextFASTAEntry();
                    // Check if we really do have an entry.
                    if(entry != null) {
                        // Split in Header and sequence.
                        StringTokenizer lst = new StringTokenizer(entry, "\n");
                        // Add the header first.
                        iEntries.add(lst.nextToken());
                        // Next add the sequence.
                        iEntries.add(lst.nextToken());
                    }
                }
                // Reset the DBLoader.
                iLoader.reset();

                // initiliaze the cached filename.
                this.iCacheFilename = iFilename;
            }
           // Okay, our cache should now be complete.
            // Format it!
            text = this.reformatCache(aCharsPerLine);
        }

        // Voila.
        return text.toString();
    }

    /**
     * This method clears the model.
     */
    public void clear() {
        this.iEntries = null;
        this.iFilename = null;
        this.iLoader = null;
    }

    /**
     * This method returns all the known DBLoader names.
     *
     * @return  String[]    with the DBLoader names. These can be used to
     *                      retrieve the DBLoaders class.
     */
    public String[] getDBNames() {
        return this.iDBNames;
    }

    /**
     * This method returns all the known DB loaders.
     *
     * @return  Properties with all the DBLoader classes as values, and their names as keys.
     */
    public Properties getALlKnownDBLoaders() {
        return this.iDBLoaders;
    }

    /**
     * This method returns the classname for a known DBLoader.
     *
     * @param   aDBLoaderName   String with the name for the DBLoader.
     * @return  String  with the classname for the specified DBLoader, or 'null'
     *                  if the specified DBLoader was not known.
     */
    public String getDBLoaderClassName(String aDBLoaderName) {
        return iDBLoaders.getProperty(aDBLoaderName);
    }

    /**
     * This method returns a String[] with all the known DBLoader classnames.
     *
     * @return  String[]    with all the known DBLoader classnames.
     */
    public String[] getAllDBLoaderClassNames() {
        return this.iDBLoaderClasses;
    }

    /**
     * This method returns an initialized AutoDBLoader who is
     * aware of the current list of known DBLoaders.
     */
    public AutoDBLoader getAutoLoader() {
        return this.iAuto;
    }

    /**
     * This method will reformat the cache according to the specified number of characters
     * per line.
     *
     * @param   aCharsPerLine   int with the maximum number of characters per line.
     */
    private StringBuffer reformatCache(int aCharsPerLine) {
        StringBuffer result = new StringBuffer();
        aCharsPerLine -= 5;
        // See how many we should do.
        int liSize = iEntries.size();

        // Cycle all.
        for(int i=0;i<liSize;i++) {
            // First the header.
            StringBuffer lTemp = new StringBuffer((String)iEntries.get(i)+"\n");
            if(lTemp.length()>aCharsPerLine) {
                int offset = aCharsPerLine-1;
                for(int j=0;;j++) {
                    // Find the last space.
                    int tempOffset = lTemp.toString().lastIndexOf(" ", offset);
                    // See if there is a space, otherwise just split bluntly.
                    if(tempOffset > 0) {
                        // Insert endline.
                        lTemp.replace(tempOffset, tempOffset + 1, "\n ");
                    } else {
                        // Split bluntly.
                        lTemp.replace(offset, offset + 1, "\n ");
                    }
                    // See if we're not overextending our reach here.
                    offset += (aCharsPerLine-1);
                    if(offset > lTemp.length()) {
                        break;
                    }
                }
            }
            // Okay, header is OK.
            result.append(lTemp.toString());

            // Advance one (to the corresponding sequence).
            i++;

            // Now do the sequence.
            lTemp = new StringBuffer((String)iEntries.get(i)+"\n");
            if(lTemp.length()>aCharsPerLine) {
                int offset = aCharsPerLine-1;
                for(int j=0;;j++) {
                    // Insert endline.
                    lTemp.insert(offset, "\n");
                    // See if we're not overextending our reach here.
                    offset += aCharsPerLine;
                    if(offset > lTemp.length()) {
                        break;
                    }
                }
            }
            // Okay, sequence is OK.
            result.append(lTemp.toString() + "\n");
        }

        return result;
    }

    /**
     * This method attempts to load all known DBLoaders from
     * the DBLoader.properties file.
     */
    private void loadDBs() {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("DBLoaders.properties");
            if(is == null) {
                throw new RuntimeException("Unable to load 'DBLoaders.properties' file from classpath! (File not found)");
            } else {
                this.iDBLoaders = new Properties();
                iDBLoaders.load(is);
                is.close();
                Set s = iDBLoaders.keySet();
                iDBNames = new String[s.size()];
                iDBLoaderClasses = new String[iDBNames.length];
                Iterator lIterator = s.iterator();
                int counter = 0;
                while(lIterator.hasNext()) {
                    String o = (String)lIterator.next();
                    iDBNames[counter] = o;
                    counter++;
                }
                // Sort the array with the names alphabetically.
                Arrays.sort(iDBNames);
                // Now retrieve all the classes.
                for(int i = 0; i < iDBNames.length; i++) {
                    String lName = iDBNames[i];
                    iDBLoaderClasses[i] = iDBLoaders.getProperty(lName);
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException("Unable to load 'DBLoaders.properties' file from classpath! (" + ioe.getMessage() + ")");
        }
    }
}
