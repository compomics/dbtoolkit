/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 04-Jun-2007
 * Time: 11:41:58
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.util.general.CommandLineParser;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.FilterLoader;
import com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread;
import com.compomics.dbtoolkit.gui.workerthreads.PeptideMappingThread;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.TreeSet;
/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2007/06/04 11:14:13 $
 */

/**
 * This class implements a command-line way of calling the PeptideMappingThread
 * class, albeit it is NOT called in threading mode.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.gui.workerthreads.PeptideMappingThread
 */
public class MapPeptides {

    /**
     * Default constructor.
     */
    public MapPeptides() {
    }

    /**
     * The main method takes the start-up parameters
     * and processes the specified DB accordingly.
     *
     * @param   args    String[] with the start-up arguments.
     */
    public static void main(String[] args) {
        // First see if we should output anything useful.
        if(args == null || args.length == 0) {
            flagError("Usage:\n\tMapPeptides [--filter <filter_name> [--filterParam <filter_parameter>]] --input <input_file_name> --peptidesFile <peptides_file_name> <CSV_output_filename>\n\n\tThe peptides file should contain one peptide sequence per line.\n\n\tNote that an existing output file will be silently overwritten!");
        }
        CommandLineParser clp = new CommandLineParser(args, new String[]{"input", "peptidesFile", "filter", "filterParam"});
        String inputFile = clp.getOptionParameter("input");
        String peptideFile = clp.getOptionParameter("peptidesFile");
        String filterString = clp.getOptionParameter("filter");
        String filterParam = clp.getOptionParameter("filterParam");
        String outputFile = clp.getParameters()[0];

        // See if all of this is correct.
        if(inputFile == null) {
            flagError("You did not specify the '--input <input_file_name>' parameter!\n\nRun program without parameters for help.");
        } else if(outputFile == null) {
            flagError("You did not specify a CSV outputfile!\n\nRun program without parameters for help.");
        } else if(peptideFile == null) {
            flagError("You did not specify a peptides input file!\n\nRun program without parameters for help.");
        } else {
            // Parameters were all found. Let's see if we can access all files that should be accessed.
            // Note that an existing output_file will result in clean and silent overwrite of the file!
            File output = new File(outputFile);
            File input = new File(inputFile);
            File peptidesFile = new File(peptideFile);

            if(!output.exists()) {
                try {
                    output.createNewFile();
                } catch(IOException ioe) {
                    flagError("Could not create outputfile (" + outputFile + "): " + ioe.getMessage());
                }
            }
            if(!input.exists()) {
                flagError("The input file you specified (" + inputFile + ") could not be found!\nExiting...");
            } else if(!peptidesFile.exists()) {
                flagError("The peptides input file you specified (" + peptideFile + ") could not be found!\nExiting...");
            } else {
                // The stuff we've received as input seems to be OK.
                // Load the peptides.
                Collection peptides = new TreeSet();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(peptidesFile));
                    String line = null;
                    while((line = br.readLine()) != null) {
                        line = line.trim();
                        // Skip empty lines.
                        if(!line.equals("")) {
                            peptides.add(line);
                        }
                    }
                    br.close();
                } catch(IOException ioe) {
                    System.err.println("\n\nUnable to read the peptides input file : " + ioe.getMessage() + "\n\n");
                    System.exit(1);
                }

                // Do the mapping.
                try {
                    DBLoader loader = DBLoaderLoader.loadDB(input);
                    // Load a filter, if necessary.
                    Filter filter = FilterLoader.loadFilter(filterString, filterParam, loader);
                    PeptideMappingThread pmt = new PeptideMappingThread(null, loader, peptides, filter, output);
                    System.out.println("\nMapping " + peptides.size() + " unique peptides to '" + inputFile + "', with output in CSV file '" + outputFile + "'...");
                    long start = System.currentTimeMillis();
                    pmt.run();
                    long end = System.currentTimeMillis();
                    System.out.println("Finished after " + ((end-start)/1000) + " seconds.");
                } catch(IOException ioe) {
                    System.err.println("\n\nUnable to load input database file: " + ioe.getMessage() + "\n\n");
                    System.exit(1);
                }
            }
        }
    }

    /**
     * This method prints the specified error message to standard out, after
     * prepending and appending two blank lines each. It then exits the JVM!
     *
     * @param   aMessage    String with the error message to display.
     */
    private static void flagError(String aMessage) {
        System.err.println("\n\n" + aMessage + "\n\n");
        System.exit(1);
    }
}
