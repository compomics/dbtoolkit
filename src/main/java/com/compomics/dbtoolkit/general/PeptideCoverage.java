/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 8-jan-03
 * Time: 13:59:57
 */
package com.compomics.dbtoolkit.general;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.protein.Protein;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class allows the calculation of protein peptide coverage for a list
 * of peptides. This means that the number of occurrences for unique accession numbers
 * are counted.
 * E.G., when checking how many (and possibly which) proteins are covered by a given
 * peptide database.
 *
 * @author Lennart Martens
 */
public class PeptideCoverage {

    /**
     * The main method of the application.
     * Arguments are:
     * <ul>
     *   <li><b>--source</b>: Protein db where the original accession numbers are read from.</li>
     *   <li><b>--control</b>: peptide DB wherein to look for the matching accession numbers.</li>
     * </ul>
     */
    public static void main(String[] args) {
        // Check command-line.
        if(args == null || args.length == 0) {
            printUsage();
        }

        CommandLineParser clp = new CommandLineParser(args, new String[]{"source", "control", });
        String source = null;
        if((source = clp.getOptionParameter("source")) == null) {
            System.err.println("\n\nNo protein DB defined as source!");
            printUsage();
        }

        String control = null;
        if((control = clp.getOptionParameter("control")) == null) {
            System.err.println("\n\nNo peptide DB defined to check against!");
            printUsage();
        }

        // Okay, being here we have a source and control DB.
        // See if the files exist.
        try {
            File sFile = new File(source);
            File cFile = new File(control);

            if(!sFile.exists()) {
                System.err.println("\n\nSource DB '" + source + "' does not exist!\nExiting...\n");
                System.exit(1);
            }

            if(!cFile.exists()) {
                System.err.println("\n\nControl DB '" + control + "' does not exist!\nExiting...\n");
                System.exit(1);
            }

            // Okay, both files exist.
            // Load the protein DB first.
            AutoDBLoader auto = new AutoDBLoader(new String[]{"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader"});
            DBLoader proteinDB = auto.getLoaderForFile(sFile.getCanonicalPath());

            HashMap all = new HashMap();
            // Gather all
            System.out.println("\n\nCollecting all unique accession numbers from source DB (" + source + ")...");
            int accCount = 0;
            Protein temp = null;
            while((temp = proteinDB.nextProtein()) != null) {
                String accession = temp.getHeader().getAccession();
                all.put(accession, new Integer(0));
                accCount++;
            }
            System.out.println("All accession numbers read (" + accCount + ").");
            // Close protein db.
            proteinDB.close();

            // Open the peptide DB.
            DBLoader peptideDB = auto.getLoaderForFile(cFile.getCanonicalPath());
            // Cycle all.
            System.out.println("\nCycling peptide database for matches...");
            temp = null;
            int pepCount = 0;
            while((temp = peptideDB.nextProtein()) != null) {
                String accession = temp.getHeader().getAccession();
                int loc = -1;
                if((loc = accession.indexOf(" ")) >= 0) {
                    accession = accession.substring(0, loc);
                }
                int count = ((Integer)all.get(accession)).intValue();
                count++;
                all.put(accession, new Integer(count));
                pepCount++;
            }
            System.out.println("All peptides cycled (" + pepCount + " entries checked).\nOutputting results...");
            // Close peptide DB.
            peptideDB.close();

            // Output results.
            int missed = 0;
            int exactOne = 0;
            int plusOne = 0;
            int total = 0;
            Iterator iter = all.keySet().iterator();
            while(iter.hasNext()) {
                int value = ((Integer)all.get(iter.next())).intValue();
                total+=value;
                switch(value) {
                    case 0:
                        missed++;
                        break;
                    case 1:
                        exactOne++;
                        break;
                    default:
                        plusOne++;
                        break;
                }
            }

            // Print results.
            System.out.println("\nFound " + missed + " proteins with NO coverage at all,");
            System.out.println("Found " + (exactOne + plusOne) + " proteins which can be found,");
            System.out.println("\t - " + exactOne + " had a single identifying peptide, and");
            System.out.println("\t - " + plusOne + " had more than one identifying peptide.");
            int sum = missed+exactOne+plusOne;
            System.out.println("\nChecksum for proteins (this should be 0): " + accCount + " - " + sum + " = " + (accCount-sum) + ".");
            System.out.println("\nChecksum for peptides (this should be 0): " + pepCount + " - " + total + " = " + (pepCount-total) + ".");
            System.out.println("\nPercentages:");
            double percentMissed = ((double)missed / (double)sum)*100;
            double percentOnce = ((double)exactOne / (double)sum)*100;
            double percentPlus = ((double)plusOne / (double)sum)*100;
            double percentFound = percentOnce+percentPlus;
            System.out.println("\t - " + (new BigDecimal(percentMissed).setScale(2, BigDecimal.ROUND_HALF_UP)) + "% MISSED.");
            System.out.println("\t - " + (new BigDecimal(percentFound).setScale(2, BigDecimal.ROUND_HALF_UP)) + "% FOUND,");
            System.out.println("\t\t - " + (new BigDecimal(percentOnce).setScale(2, BigDecimal.ROUND_HALF_UP)) + "% by one peptide");
            System.out.println("\t\t - " + (new BigDecimal(percentPlus).setScale(2, BigDecimal.ROUND_HALF_UP)) + "% by more than one peptide.");
        } catch(IOException ioe) {
            System.err.println("\n\nIOException occurred!\n");
            ioe.printStackTrace();
        } catch(UnknownDBFormatException udfe) {
            System.err.println("\n\nUnable to identify DB!\n");
            udfe.printStackTrace();
        }
    }

    /**
     * This method prints the usage of this class to stderr.
     * It subsequently exits the program (status int will be set to 1).
     */
    private static void printUsage() {
        System.err.println("\n\nUsage:\n\t PeptideCoverage --source <protein_DB> -- control <derived_peptide_DB>\n\n");
        System.exit(1);
    }
}
