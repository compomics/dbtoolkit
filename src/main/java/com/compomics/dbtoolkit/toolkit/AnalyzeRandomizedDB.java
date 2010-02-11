/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 24-jul-2003
 * Time: 12:16:36
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.dbtoolkit.io.implementations.FASTADBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class will analyze the results from randomizing a DB.
 *
 * @author Lennart Martens
 */
public class AnalyzeRandomizedDB {

    /**
     * The pointer to the original DB.
     */
    private DBLoader iOriginalDB = null;
    private String iOriginalDBFile = null;

    /**
     * The pointer to the randomized version of that DB.
     */
    private DBLoader iRandomizedDB = null;
    private String iRandomizedDBFile = null;

    /**
     * The enzyme, if any.
     */
    private Enzyme iEnzyme = null;

    /**
     * The constructor for this class takes the filenames for the original
     * and randomized DB and the enzyme to generate the peptides with.
     * The enzyme is set to be 'null', so no peptides are generated.
     *
     * @param aOriginalFile String with the filename for the original DB. The file should be in FASTA format, and
     *                      the file should exist.
     * @param aRandomizedFile   String with the filename for the randomized DB. The file should be in FASTA format
     *                          and the file should exist.
     * @exception IOException when the files could not be read.
     */
    public AnalyzeRandomizedDB(String aOriginalFile, String aRandomizedFile) throws IOException {
        this(aOriginalFile, aRandomizedFile, null, 0);
    }

    /**
     * The constructor for this class takes the filenames for the original
     * and randomized DB and the enzyme to generate the peptides with.
     * The enzyme can be 'null' in which case no peptides are generated.
     *
     * @param aOriginalFile String with the filename for the original DB. The file should be in FASTA format, and
     *                      the file should exist.
     * @param aRandomizedFile   String with the filename for the randomized DB. The file should be in FASTA format
     *                          and the file should exist.
     * @param aEnzyme   String with the enzyme to use. This can be null, in which case no enzyme is used.
     * @param aMiscleavages int with the number of miscleavages for the enzyme. Is only used when the enzyme parameter
     *                      is not 'null'.
     * @exception IOException when the files could not be read.
     */
    public AnalyzeRandomizedDB(String aOriginalFile, String aRandomizedFile, String aEnzyme, int aMiscleavages) throws IOException {
        // Init DB loaders.
        this.iOriginalDBFile = aOriginalFile;
        this.iOriginalDB = new FASTADBLoader();
        iOriginalDB.load(iOriginalDBFile);
        this.iRandomizedDBFile = aRandomizedFile;
        this.iRandomizedDB = new FASTADBLoader();
        iRandomizedDB.load(iRandomizedDBFile);

        // Check enzyme.
        if(aEnzyme != null) {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("enzymes.txt");
            if(in != null) {
                MascotEnzymeReader mer = new MascotEnzymeReader(in);
                iEnzyme = mer.getEnzyme(aEnzyme);
                if(iEnzyme == null) {
                    throw new IOException("The enzyme you specified (" + aEnzyme + ") was not found in the Mascot Enzymefile '" + this.getClass().getClassLoader().getResource("enzymes.txt") + "'!");
                } else {
                    iEnzyme.setMiscleavages(aMiscleavages);
                }
            } else {
                throw new IOException("File 'enzymes.txt' not found in current classpath!");
            }
        }
    }

    /**
     * This method starts the class to do what it is intended to do: analyse the two databases.
     *
     * @param aPrintRequested boolean to indicate whether the entire list of redundant sequences
     *              should be printed to stdout.
     * @param aVerbose  boolean to indicate whether verbose output is required.
     */
    public void doAnalysis(boolean aPrintRequested, boolean aVerbose) {
        PrintStream output = null;
        if(aPrintRequested && aVerbose) {
            output = System.err;
        } else {
            output = System.out;
        }
        if(aVerbose) {
            output.println("\n\nAnalyzing randomized database '" + iRandomizedDBFile + "':");
            output.println(" - Original DB was: '" + iOriginalDBFile + "', and");
            if(iEnzyme != null) {
                output.println(" - the enzyme used is:");
                output.println(iEnzyme.toString("\t"));
            } else {
                output.println(" - no enzyme was specified.");
            }
        }

        try {
            // We'll read the source DB and grab all sequences as keys in a HashMap.
            // Note that we'll have to check whether we want whole protein( no enzyme)
            // or peptide (an enzyme) sequences.
            HashMap allSeqs = new HashMap();
            int proteinCounter = 0;
            int peptideCounter = 0;
            Protein current = null;
            if(aVerbose) {
                output.println("\nReading original DB for unique sequences" + ((iEnzyme == null)?" (no cleaving applied)":" (cleaving entries with " + iEnzyme.getTitle() + ")") + "...");
            }
            while((current = iOriginalDB.nextProtein()) != null) {
                proteinCounter++;
                Protein[] temp = null;
                if(iEnzyme != null) {
                    temp = iEnzyme.cleave(current);
                } else {
                    temp = new Protein[]{current};
                }
                peptideCounter += temp.length;
                for(int i = 0; i < temp.length; i++) {
                    String sequence = temp[i].getSequence().getSequence();
                    allSeqs.put(sequence, "");
                }
            }
            if(aVerbose) {
                output.println("\nOriginal DB analyzed:\n - read " + proteinCounter + " entries in the original DB,\n - resulting in " + peptideCounter + " child sequences, and");
                output.println(" - " + allSeqs.size() + " unique sequences to match.");
            }
            // This one is over and done with.
            iOriginalDB.close();

            // Next part, cycle the randomized DB and see if these peptides are found somewhere.
            HashMap redundantSeqs = new HashMap();
            proteinCounter = 0;
            peptideCounter = 0;
            current = null;
            if(aVerbose) {
                output.println("\nReading randomized DB to match unique sequences from original DB" + ((iEnzyme == null)?" (no cleaving applied)":" (cleaving entries with " + iEnzyme.getTitle() + ")") + "...");
            }
            while((current = iRandomizedDB.nextProtein()) != null) {
                proteinCounter++;
                Protein[] temp = null;
                if(iEnzyme != null) {
                    temp = iEnzyme.cleave(current);
                } else {
                    temp = new Protein[]{current};
                }
                peptideCounter += temp.length;
                for(int i = 0; i < temp.length; i++) {
                    String sequence = temp[i].getSequence().getSequence();
                    if(allSeqs.containsKey(sequence)) {
                        redundantSeqs.put(sequence, "");
                    }
                }
            }
            if(aVerbose) {
                output.println("\nRandomized DB analyzed:\n - read " + proteinCounter + " entries in the DB,\n - resulting in " + peptideCounter + " child sequences, and");
                output.println(" - " + redundantSeqs.size() + " sequences which were redundant with the original DB (roughly " + (100-((100*redundantSeqs.size())/allSeqs.size())) + "% scrambling efficiency).");
            }

            // This one is over and done with.
            iRandomizedDB.close();
            // See if we need to dump the entire list to stdout.
            if(aPrintRequested) {
                String[] temp = new String[redundantSeqs.size()];
                redundantSeqs.keySet().toArray(temp);
                Arrays.sort(temp);
                for(int i = 0; i < temp.length; i++) {
                    System.out.println(temp[i]);
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method is the entry point for the application.
     *
     * @param args  String[] with the start-up arguments.
     */
    public static void main(String[] args) {
        // Check args.
        if(args == null || args.length == 0) {
            printUsage();
        }
        CommandLineParser clp = new CommandLineParser(args, new String[] {"enzyme", "mc"});
        String[] dbs = clp.getParameters();
        if(dbs.length != 2) {
            printUsage();
        }
        String original = dbs[0];
        String randomized = dbs[1];

        boolean print = false;
        boolean verbose = false;
        String[] flags = clp.getFlags();
        if(flags.length > 2) {
            printUsage();
        } else if(flags.length == 0) {
            System.err.println("\n\nIt would be best to specify either the 'p' or the 'v' flag, or both, since this program is otherwise completely silent and useless!!.\n");
            System.exit(1);
        }
        if(clp.hasFlag("p")) {
            print = true;
        }
        if(clp.hasFlag("v")) {
            verbose = true;
        }

        String enzyme = clp.getOptionParameter("enzyme");
        int mc = 0;
        if(enzyme != null) {
            String tempMC = clp.getOptionParameter("mc");
            if(tempMC == null) {
                mc = 1;
            } else {
                try {
                    mc = Integer.parseInt(tempMC);
                    if(mc < 0) {
                        throw new NumberFormatException("");
                    }
                } catch(NumberFormatException nfe) {
                    System.err.println("\n\nThe number of miscleavages must be a positive, whole number.\nYou incorrectly specified '" + tempMC + "'!\n");
                    System.exit(1);
                }
            }
        }
        // Check original DB file.
        if(!new File(original).exists()) {
            System.err.println("\n\nOriginal database file '" + args[0] + "' was not found!\n");
            System.exit(1);
        }
        // Check randomized DB file.
        if(!new File(randomized).exists()) {
            System.err.println("\n\nRandomized database file '" + args[1] + "' was not found!\n");
            System.exit(1);
        }
        // Okay, we should be able to run.
        try {
            AnalyzeRandomizedDB ard = null;
            if(enzyme != null) {
                ard = new AnalyzeRandomizedDB(original, randomized, enzyme, mc);
            } else {
                ard = new AnalyzeRandomizedDB(original, randomized);
            }
            ard.doAnalysis(print, verbose);
        } catch(IOException ioe) {
            System.err.println("\n\nError:\n\t" + ioe.getMessage() + "\n");
            ioe.printStackTrace();
        }
    }

    /**
     * This metho dprints the usage of the class and exits.
     */
    private static void printUsage() {
        System.err.println("\n\nUsage:\n\tAnalyzeRandomizedDB [-p] [-v] [--enzyme <enzymeName> [--mc <number_of_missed_cleavages>]] <original_DB> <randomized_DB>");
        System.err.println("\n\tFlag significance:\n\t - p : print all redundant sequences\n\t - v : verbose output (application flow and basic statistics)\n");
        System.exit(1);
    }
}
