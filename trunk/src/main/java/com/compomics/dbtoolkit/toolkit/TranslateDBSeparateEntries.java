package com.compomics.dbtoolkit.toolkit;

import com.compomics.util.general.CommandLineParser;
import com.compomics.util.nucleotide.NucleotideSequence;
import com.compomics.util.nucleotide.NucleotideSequenceImpl;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.DBLoaderLoader;

import java.util.*;
import java.io.*;

/**
 * This class reads a nucleotide sequence database and translates it into the six reading frames.
 * Each time a stop codon is encountered in the nucleotide reading frame, a new FASTA entry is created.
 *
 * @author Kenny Helsens
 * @version $Id: TranslateDBSeparateEntries.java,v 1.2 2006/12/18 11:37:46 kenny Exp $
 *
 */

public class TranslateDBSeparateEntries {

    /**
     * The main method is the entry point for the application.
     *
     * @param args String[] with the start-up arguments.
     */
    public static void main(String[] args) {
        // Check the start-up arguments.
        if(args == null || args.length == 0) {
            printUsage();
        }
        CommandLineParser clp = new CommandLineParser(args, new String[] {"cut", "input"});
        // Start checking the arguments.
        String cut = clp.getOptionParameter("cut");
        Properties cutProps = null;
        if(cut != null) {
            File temp = new File(cut);
            if(!temp.exists() || temp.isDirectory()) {
                printError("Unable to locate the codon usage table file you specified ('" + cut + "')!");
            }
            try {
                cutProps = readCUT(temp);
            } catch(IOException ioe) {
                printError("Unable to read the codon usage table you specified ('" + cut + "'): " + ioe.getMessage());
            }
        }
        String input = clp.getOptionParameter("input");
        if(input == null) {
            printUsage();
        }
        File inputFile = new File(input);
        if(!inputFile.exists() || inputFile.isDirectory()) {
            printError("Unabel to read the input file you specified ('" + input + "')!");
        }
        String[] params = clp.getParameters();
        if(params == null || params.length != 1) {
            printUsage();
        }
        File outputFile = new File(params[0]);
        if(outputFile.exists()) {
            printError("Output file exists.\nRefusing to overwrite.");
        }
        // Okay, start reading the DB.
        try {
            // Outputfile.
            PrintWriter pw = null;
            // Loading input DB.
            DBLoader loader = DBLoaderLoader.loadDB(inputFile);
            // Read all entries.
            String entry = null;
            int inCount = 0;
            int outCount = 0;
            while((entry = loader.nextFASTAEntry()) != null) {
                inCount++;
                BufferedReader br = new BufferedReader(new StringReader(entry));
                String header = br.readLine().substring(1);
                StringBuffer sequence = new StringBuffer();
                String line = null;
                while((line = br.readLine()) != null) {
                    sequence.append(line);
                }
                br.close();
                String lSequence = sequence.toString();
                NucleotideSequence nseq = new NucleotideSequence(Header.parseFromFASTA(header), new NucleotideSequenceImpl(lSequence, cutProps));

                /** a) The method returns a Vector with six Hashmaps. Each Hashmap corresponds to a reading frame (sense + antisense).
                       The HashMap keys are Header instances (with extra data like the nucleotide sequence in the iRest field!) with Protein instances as values.
                    b) We use the fasta header from the nucleotide sequence as a part of the Accessions for the protein entries.

                 Then loop over the translations.
                 */
                String lDatabaseIdentifier = nseq.getHeader().getRest();
                String lShortOrganism = "dm";
                Vector readingframes = nseq.getSequence().translateToStopCodonSeparatedEntries(lDatabaseIdentifier, lShortOrganism);
                for (int i = 0; i < readingframes.size(); i++) {
                    HashMap readingframe = (HashMap) readingframes.get(i);
                    Iterator iter = readingframe.keySet().iterator();
                    while (iter.hasNext()) {
                        Header lHeader = (Header) iter.next();
                        String lProteinSequence = (String) readingframe.get(lHeader);
                        Protein lProtein = new Protein(lHeader, new AASequenceImpl(lProteinSequence));

                        if(pw == null) {
                            pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
                        }
                        lProtein.writeToFASTAFile(pw);
                        outCount++;
                        pw.flush();
                    }
                }
            }
            pw.close();
            System.out.println("Finished translating " + inCount + " input sequences into " + outCount + " protein sequences" +
                               (cut != null?" employing codon usage table '" + cut + "'":"") );
        } catch(IOException ioe) {
            printError("Something went wrong: " + ioe.getMessage());
        } catch(NullPointerException npe){
            printError(npe.getMessage());
        }
    }


    /**
     * This method attempts to read the specified file into a Properties file.
     * It should be presented with an existing, readable File object, with
     * 'triplet' 'single-letter amino acid' pairs per line.
     *
     * @param aCUTFile  File with the CUT file to load
     * @return  Properties with the CUT (triplet is key, single-letter amino acid is value)
     * @throws IOException  whenever the reading failed
     */
    private static Properties readCUT(File aCUTFile) throws IOException {
        Properties result = new Properties();

        BufferedReader br = new BufferedReader(new FileReader(aCUTFile));
        String line = null;
        int lineCount = 0;
        while((line = br.readLine()) != null) {
            lineCount++;
            line = line.trim();
            // Skip empty lines.
            if(line.equals("")) {
                continue;
            }
            // Delimited by tabs or spaces.
            StringTokenizer st = new StringTokenizer(line, " \t");
            if(st.countTokens() != 2) {
                throw new IOException("Parse error in CUT file on line " + lineCount + "; line was: '" + line + "'!");
            }
            result.put(st.nextToken(), st.nextToken());
        }

        return result;
    }

    /**
     * This method prints the usage information for this class to the standard
     * error stream and exits with the error flag raised to '1'.
     */
    private static void printUsage() {
        printError("Usage:\n\n\tTranslateDBSeparateEntries [--cut <codon_usage_table>] --input <input_database> <output_database>\n\n" +
                   "\t\t - With:\n\t\t\tcodon_usage_table file: <triplet> <amino acid> (1 pair per line)");
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
