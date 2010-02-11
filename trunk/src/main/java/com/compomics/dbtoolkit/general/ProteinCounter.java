/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 22-jan-03
 * Time: 14:46:07
 */
package com.compomics.dbtoolkit.general;

import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.protein.Protein;

import java.util.HashMap;
import java.util.Iterator;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class counts the number of unique accession numbers found in the database.
 *
 * @author Lennart Martens
 */
public class ProteinCounter {

    /**
     * Main method runs this class. You specify the DB to load and optionally a
     * verification flag ('v').
     *
     * @param   args    String[] with start-up args. Should be the input DB,
     *                  optional is the 'v' (verification) flag.
     */
    public static void main(String[] args) {
        try {
            if(args == null || args.length == 0) {
                printUsage();
            }
            CommandLineParser clp = new CommandLineParser(args);
            String[] tempAr = clp.getParameters();
            if(tempAr == null || tempAr.length == 0) {
                printUsage();
            }
            String input = tempAr[0];
            if(input == null) {
                printUsage();
            }
            boolean verify = false;
            if((clp.getFlags() != null) && (clp.getFlags().length > 0) && (clp.getFlags()[0].trim().equals("v"))) {
                verify = true;
            }

            DBLoader loader = new AutoDBLoader(new String[]{"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader"}).getLoaderForFile(input);
            Protein p = null;
            HashMap accessions = new HashMap();
            int readCounter = 0;
            System.out.println("\n\nReading '" + input + "' with" + (verify?"":"out") + " verification...");
            while((p = loader.nextProtein()) != null) {
                readCounter++;
                String acc = p.getHeader().getAccession();
                if(accessions.containsKey(acc)) {
                    int current = ((Integer)accessions.get(acc)).intValue();
                    current++;
                    accessions.put(acc, new Integer(current));
                } else {
                    accessions.put(acc, new Integer(1));
                }
            }
            loader.close();

            System.out.println("\n\nRead " + readCounter + " entries from DB, representing " + accessions.keySet().size() + " unique proteins (by accession number.)\n");

            if(verify) {
                Iterator iter = accessions.values().iterator();
                int temp = 0;
                while(iter.hasNext()) {
                    temp += ((Integer)iter.next()).intValue();
                }
                System.out.println("Verification should yield '0': " + (readCounter-temp) + ".\n\n");
            }
        } catch(Exception e) {
            System.err.println("\n\n" + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    /**
     * This method prints the correct usage of the class to the stderr and then exits
     * the JVM.
     */
    private static void printUsage() {
        System.err.println("\n\nUsage:\n\tProteinCounter [-v] <input_database>\n");
        System.exit(1);
    }
}
