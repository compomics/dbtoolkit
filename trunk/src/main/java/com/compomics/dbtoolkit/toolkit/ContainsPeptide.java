/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 23-nov-2004
 * Time: 15:35:39
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.util.protein.Protein;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.DBLoaderFactory;
import com.compomics.dbtoolkit.io.DBLoaderLoader;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class can be used to find out whether a peptide sequence (or list of sequences)
 * is present in one or more entries of the specified database.
 *
 * @author Lennart Martens
 * @version $Id: ContainsPeptide.java,v 1.4 2007/07/06 09:52:03 lennart Exp $
 */
public class ContainsPeptide {

    /**
     * The main method is the entry point for the application.
     *
     * @param args String[] with the start-up arguments.
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            printUsage();
        }
        // First validate the database file.
        File dbFile = new File(args[1]);
        if(!dbFile.exists()) {
            printError("The database file you specified '" + args[1] + "' does not exist!");
        }
        // See if we are in 'file mode' or 'single sequence mode'.
        ArrayList allSequences = new ArrayList();
        if(args[0].startsWith("@")) {
            // Check for file existence.
            File sequences = new File(args[0].substring(1));
            if(!sequences.exists()) {
                printError("The input file with peptide sequences you specified '" + args[0].substring(1) + "' does not exist!");
            }
            // Load all sequences.
            try {
                BufferedReader br = new BufferedReader(new FileReader(sequences));
                String line = null;
                while((line = br.readLine()) != null) {
                    line = line.trim();
                    // Skip empty lines and duplicates.
                    if(!line.equals("") && !allSequences.contains(line)) {
                        allSequences.add(line);
                    }
                }
                br.close();
            } catch(IOException ioe) {
                printError("Unable to read the file with the peptide sequences ('" + args[0].substring(1) + "'): " + ioe.getMessage() + "!");
            }
        } else {
            allSequences.add(args[0]);
        }
        // User-friendly output.
        System.err.println("\n\nRead " + allSequences.size() + " unique sequences from input.\n");
        HashMap matches = null;
        try {
            matches = ContainsPeptide.processSequences(dbFile, allSequences);
        } catch(IOException ioe) {
            printError("An error occurred while reading the input database ('" + args[1] + "'): " + ioe.getMessage());
        }
        // Okay, print out a list of sequences and the accession numbers they were found in (if any).
        System.out.println("Sequence;Protein match count;Protein matches");
        for (Iterator lIterator = allSequences.iterator(); lIterator.hasNext();) {
            String sequence = (String)lIterator.next();
            StringBuffer accessions = new StringBuffer("");
            int count = 0;
            if(matches.containsKey(sequence)) {
                Collection prots = (Collection)matches.get(sequence);
                for (Iterator lIterator1 = prots.iterator(); lIterator1.hasNext();) {
                    Protein lProtein = (Protein)lIterator1.next();
                    // Find the location of the sequence.
                    int start = lProtein.getSequence().getSequence().indexOf(sequence);
                    // Correct for potential known start location.
                    if(lProtein.getHeader().getStartLocation() >= 0) {
                        start += lProtein.getHeader().getStartLocation();
                    }
                    int end = start + sequence.length();
                    if(accessions.length() > 0) {
                        accessions.append("^A");
                    }
                    accessions.append(lProtein.getHeader().getAccession() + " (" + start + "-" + end + ")");
                    count++;
                }
            }
            System.out.println(sequence + ";" + count + ";" + accessions.toString());
        }
    }

    /**
     * This method reads the specified database and for each entry tries to map each of the
     * specified peptides. The resulting HashMap contains the sequences as keys, the matched
     * proteins as values.
     *
     * @param aFile File with the database to load.
     * @param aSequences    Collection withe the (unique!) sequences to match against the database.
     * @return  HashMap holding sequences (String) as keys, and a Collection of Protein instances
     *                  as value. This Collection will be empty when no matches were found.
     * @throws IOException  when the reading of the database failed.
     */
    public static HashMap processSequences(File aFile, Collection aSequences) throws IOException {
        HashMap matches = new HashMap(aSequences.size());
        // Connect a loader to the database.
        DBLoader loader = DBLoaderLoader.loadDB(aFile);
        // Now to cycle the database.
        Protein p = null;
        while((p = loader.nextProtein()) != null) {
            // Get the protein sequence.
            String sequence = p.getSequence().getSequence();
            // Cycle the sequences to match.
            for (Iterator lIterator = aSequences.iterator(); lIterator.hasNext();) {
                String s = (String)lIterator.next();
                // If we have a match, add it to the matching protein list.
                if(sequence.indexOf(s) >= 0) {
                    // See if we already had (a) previous match(es).
                    if(matches.containsKey(s)) {
                        Collection c = (Collection)matches.get(s);
                        c.add(p);
                    } else {
                        Collection c = new ArrayList();
                        c.add(p);
                        matches.put(s, c);
                    }
                }
            }
        }
        // Add the ones we did not find.
        for (Iterator lIterator = aSequences.iterator(); lIterator.hasNext();) {
            String sequence = (String)lIterator.next();
            if(!matches.containsKey(sequence)) {
                matches.put(sequence, new ArrayList());
            }
        }
        loader.close();
        return matches;
    }

    /**
     * This method prints the usage information for this class to the standard
     * error stream and exits with the error flag raised to '1'.
     */
    private static void printUsage() {
        printError("Usage:\n\n\tContainsPeptide <peptide_sequence> <database_file>\n\n\t\tOR\n\n\tContainsPeptide @<file_with_multiple_sequences> <database_file>");
    }

    /**
     * This method prints two blank lines followed by the the specified error message and another two empty lines
     * to the standard error stream and exits with the error flag raised to '1'.
     *
     * @param aMsg String with the message to print.
     */
    private static void printError(String aMsg) {
        System.err.println("\n\n" + aMsg + "\n\n");
        System.exit(1);
    }
}
