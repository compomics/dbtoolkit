/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 8-nov-02
 * Time: 14:39:43
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.FilterLoader;
import com.compomics.dbtoolkit.io.QueryParser;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.protein.Protein;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class counts the occurrence of entries in a given database.
 *
 * @author Lennart Martens
 */
public class CountEntries {

    public static void main(String[] args) {
        CommandLineParser clp = new CommandLineParser(args, new String[]{"query", "filter", "filterParam"});
        if((args == null) || (args.length == 0)) {
            System.err.println("\n\nUsage: CountEntries [--filter <filter_name> [--filterParam \"<filter_parameter>\"]] --query \"<comma_separated_list_of residues>\" <input_file>\n\n");
            System.exit(1);
        }
        File inputFile = null;
        String[] temp = clp.getParameters();
        if(temp.length < 1) {
            CountEntries.flagError("You did not specify an outputfile!");
        } else {
            inputFile = new File(temp[0]);
            if(!inputFile.exists()) {
                flagError("Inputfile '" + temp[0] + "' does not exist!");
            } else {
                String query = clp.getOptionParameter("query");
                if((query == null) || (query.trim().equals(""))){
                    flagError("You need to specify a query for the residues to count!");
                } else {
                    // Get all residues (or stretches) to count.
                    StringTokenizer lst = new StringTokenizer(query, ",");
                    int size = lst.countTokens();
                    HashMap toCheck = new HashMap(size);
                    QueryParser qp = new QueryParser();
                    try {
                        for(int i=0;i<size;i++) {
                            String queryPart = lst.nextToken().trim();
                            ProteinFilter pf = qp.parseQuery(queryPart);
                            toCheck.put(queryPart, new CountEntries.Count(pf));
                        }
                    } catch(ParseException pe) {
                        flagError("Parser exception on your query String '" + query + "'!\n" + pe.getMessage());
                    }
                    // The stuff we've received as input seems to be OK.
                    DBLoader loader = null;
                    try {
                        loader = DBLoaderLoader.loadDB(inputFile);
                    } catch(IOException ioe) {
                        flagError("Unable to load database file: " + ioe.getMessage());
                    }
                    // Try to load the filter (if any).
                    String filter = clp.getOptionParameter("filter");
                    String filterParam = clp.getOptionParameter("filterParam");
                    Filter f = null;
                    try {
                        f= FilterLoader.loadFilter(filter, filterParam, loader);
                    } catch(IOException ioe) {
                        flagError("Unable to load filter: " + ioe.getMessage());
                    }

                    // Okay, do our thing.
                    Protein entry = null;
                    int entryCounter = 0;
                    Set keySet = toCheck.keySet();
                    String[] keys = new String[keySet.size()];
                    keySet.toArray(keys);
                    System.out.println("\n\nCounting occurrances in DB, settings are:");
                    System.out.println("\t - Input DB file: '" + inputFile + "'.");
                    StringBuffer filterSettings = new StringBuffer();
                    if(f == null) {
                        filterSettings.append("no filter specified.");
                    } else {
                        filterSettings.append("filter '" + filter + "' chosen");
                        if(filterParam != null) {
                            filterSettings.append(" with " + ((filterParam.startsWith("!"))?"inverted":"") + " parameter '" + ((filterParam.startsWith("!"))?filterParam.substring(1):filterParam) + "'.");
                        } else {
                            filterSettings.append(" without parameters.");
                        }
                    }
                    System.out.println("\t - Filter settings: " + filterSettings.toString());
                    System.out.println("\t - Query: '" + query + "', yielding following individual residues to count:");
                    for(int i = 0; i < keys.length; i++) {
                        String lKey = keys[i];
                        System.out.println("\t\t # '" + lKey + "'" + ": " + ((Count)toCheck.get(lKey)).getFilter().toString());
                    }
                    System.out.println("\nCycling database...");
                    try {
                        if(f!= null) {
                            entry = loader.nextFilteredProtein(f);
                        } else {
                            entry = loader.nextProtein();
                        }
                        while(entry != null) {
                            entryCounter++;
                            for(int i = 0; i < keys.length; i++) {
                                String lKey = keys[i];
                                Count c = (Count)toCheck.get(lKey);
                                c.passesFilter(entry);
                            }
                            // Complete the cycle by advancing to the next element.
                            if(f!= null) {
                                entry = loader.nextFilteredProtein(f);
                            } else {
                                entry = loader.nextProtein();
                            }
                        }
                    } catch(IOException ioe) {
                        flagError("IOException occurred while reading file!\n" + ioe.getMessage());
                    }
                    System.out.println("\n\n");
                    // Okay, output results.
                    System.out.println("Cycled " + entryCounter + " entries in database. Printing results:\n");
                    for(int i = 0; i < keys.length; i++) {
                        String lKey = keys[i];
                        int found = ((Count)toCheck.get(lKey)).getCount();
                        double percent = 100*(((double)found)/entryCounter);
                        BigDecimal bd = new BigDecimal(percent);
                        percent = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        System.out.println("\t - " + lKey + " was found in " + found + " entries (" + percent + "% of DB entries)");
                    }
                }
            }
        }
    }

    /**
     *  This method prints the specified message to the System error
     * stream and exits the JVM. It automatically adds two leading blank lines
     * and to trailing ones. It also adds the informational message to run the program
     * without arguments for the help.
     *
     * @param   aMsg    String with the message to display in System.err.
     */
    private static void flagError(String aMsg) {
        System.err.println("\n\n" + aMsg + "\n\nRun program without parameters for help.\n\n");
        System.exit(1);
    }

    /**
     * Nifty little inner class.
     */
    private static class Count {
        private ProteinFilter iFilter = null;
        private int iCount = 0;

        public Count(ProteinFilter aFilter) {
            this.iFilter = aFilter;
            this.iCount = 0;
        }

        public void passesFilter(Protein aProtein) {
            if(iFilter.passesFilter(aProtein)) {
                iCount++;
            }
        }

        public int getCount() {
            return iCount;
        }

        public ProteinFilter getFilter() {
            return iFilter;
        }
    }
}
