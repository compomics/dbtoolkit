/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-aug-2003
 * Time: 12:34:07
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.EnzymeLoader;
import com.compomics.dbtoolkit.io.FilterLoader;
import com.compomics.dbtoolkit.io.implementations.ProteinMassFilter;
import com.compomics.dbtoolkit.io.implementations.ProteinSequenceFilter;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class 
 *
 * @author Lennart
 */
public class PeptideRepresentation {

    public static void main(String[] args) {
        // Check for parameters.
        if(args == null || args.length == 0) {
            printUsage();
        }
        // Parse commandline.
        CommandLineParser clp = new CommandLineParser(args, new String[]{"filter", "filterParam", "enzyme", "mc", "lowMass", "highMass", "residue"});
        String[] temp = clp.getParameters();
        if(temp == null || temp.length != 1) {
            printUsage();
        }
        // Verify existence of input file.
        String dbFile = temp[0];
        File input = new File(dbFile);
        if(!input.exists()) {
            flagError("Input DB file '" + dbFile + "' was not found!");
        }

        // Get a DBLoader for the specified inputfile.
        DBLoader loader = null;
        try {
            loader = DBLoaderLoader.loadDB(input);
        } catch(IOException ioe) {
            flagError("Unable to load database file: " + ioe.getMessage());
        }

        // Load the filter (if applicable).
        String filter = clp.getOptionParameter("filter");
        String filterParam = clp.getOptionParameter("filterParam");
        Filter f = null;
        try {
            f = FilterLoader.loadFilter(filter, filterParam, loader);
        } catch(IOException ioe) {
            flagError("Unable to load the filter you specified: " + ioe.getMessage());
        }

        // Check the enzyme.
        String enzyme = clp.getOptionParameter("enzyme");
        String mc = clp.getOptionParameter("mc");
        Enzyme e = null;
        try {
            e = EnzymeLoader.loadEnzyme(enzyme, mc);
        } catch(IOException ioe) {
            flagError("Unable to load enzyme: " + ioe.getMessage());
        }

        // Parse lower and higher masss threshold (if applicable).
        String high = clp.getOptionParameter("highMass");
        String low = clp.getOptionParameter("lowMass");
        ProteinMassFilter pmf = null;
        if(high != null && low != null) {
            try {
                double lowMass = Double.parseDouble(low);
                double highMass = Double.parseDouble(high);
                pmf = new ProteinMassFilter(lowMass, highMass);
            } catch(Exception exc) {
                flagError("High and low mass must be positive decimal numbers!\nYou specified '" + low + "' and '" + high + "', respectively.");
            }
        }

        // The residue.
        String residue = clp.getOptionParameter("residue");
        if(residue == null || residue.trim().equals("")) {
            flagError("You must specify a residue to select for!");
        }
        System.out.println("Residue: " + residue);
        ProteinSequenceFilter psf = new ProteinSequenceFilter(residue);
        System.out.println(psf.toString());
        // Counters.
        int readCounter = 0;
        int cleavedCounter = 0;
        int passedCounter = 0;

        // The main results HashMap.
        HashMap results = new HashMap();

        // Okay, cycle DB!
        try {
            Protein p = null;

            // Filter or not filter?
            if(f != null) {
                p = loader.nextFilteredProtein(f);
            } else {
                p = loader.nextProtein();
            }
            while(p != null) {
                readCounter++;
                Protein[] intermed = null;
                if(!results.containsKey(p.getHeader().getAccession())) {
                    results.put(p.getHeader().getAccession(), new Integer(0));
                }
                // See if we have to apply enzymatic cleavage.
                if(e != null) {
                    intermed = e.cleave(p);
                } else {
                    intermed = new Protein[]{p};
                }
                cleavedCounter += intermed.length;
                // Okay, now check each protein in the array.
                for(int i = 0; i < intermed.length; i++) {
                    Protein lProtein = intermed[i];
                    // Is there a mass filter set?
                    if(pmf != null) {
                        if(!pmf.passesFilter(lProtein)) {
                            // It didn't pass the filter, so skip!
                            continue;
                        }
                    }
                    passedCounter++;
                    if(psf.passesFilter(lProtein)) {
                        String accession = lProtein.getHeader().getAccession();
                        if(results.containsKey(accession)) {
                            int tempCount = ((Integer)results.get(accession)).intValue();
                            tempCount++;
                            results.put(accession, new Integer(tempCount));
                        } else {
                            results.put(accession, new Integer(1));
                        }
                    }
                }

                // Move to the next entry.
                if(f != null) {
                    p = loader.nextFilteredProtein(f);
                } else {
                    p = loader.nextProtein();
                }
            }

            // Print out the results.
            System.err.println("\n\nCycled " + readCounter + " entries, resulting in " + cleavedCounter + " child entries, of which " + passedCounter + " passed additional selection criteria.\n");

            Iterator iter = results.keySet().iterator();
            System.out.println(";Accession;Number of occurrences of " + residue);
            while(iter.hasNext()) {
                String acc = (String)iter.next();
                System.out.println(";" + acc + ";" + results.get(acc));
            }
        } catch(IOException ioe) {
            flagError("Error cycling database: " + ioe.getMessage());
        }

    }

    /**
     * This method prints the usage for this class to stderr and then exits.
     */
    private static void printUsage() {
        System.err.println("\n\nUsage:\nPeptideRepresentation [--filter <filtername> [--filterParam <filter_parameter>]] [--enzyme <enzyme_name> [--mc <number_of_miscleavages>]] [--lowMass <lower_mass_threshold> --highMass <upper_mass_threshold>] --residue <residue_to_account_for> <source_DB>");
        System.exit(1);
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
}
