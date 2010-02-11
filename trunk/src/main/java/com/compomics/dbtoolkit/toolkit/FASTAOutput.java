/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 31-okt-02
 * Time: 19:41:43
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread;
import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.io.FilterLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.general.CommandLineParser;

import java.io.File;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.7 $
 * $Date: 2008/11/25 16:43:53 $
 */

/**
 * This class implements a command-line way of calling the FASTAOutputThread
 * class, albeit it is NOT called in threading mode.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread
 */
public class FASTAOutput {

    /**
     * Default constructor.
     */
    public FASTAOutput() {
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
            showUsage();
        }
        CommandLineParser clp = new CommandLineParser(args, new String[]{"input", "filter", "filterParam", "lowMass", "highMass", "filterSet"});
        String inputFile = clp.getOptionParameter("input");
        String filterString = clp.getOptionParameter("filter");
        String filterParam = clp.getOptionParameter("filterParam");
        String filterSet = clp.getOptionParameter("filterSet");
        String lowMass =  clp.getOptionParameter("lowMass");
        String highMass  = clp.getOptionParameter("highMass");
        String outputFile = clp.getParameters()[0];

        // FilterSet and filterName/filterParan are mutually exclusive.
        if(filterString != null && filterSet != null) {
            flagError("You can not specify both '--filter' and 'filterSet'!\n\nPlease run the program without parameters to display correct usage information.");
        }
        if(filterParam != null && filterSet != null) {
            flagError("You can not specify both '--filterParam' and 'filterSet'!\n\nPlease run the program without parameters to display correct usage information.");
        }


        // See if all of this is correct.
        // Parse the enzyme stuff and masses etc.
        double minMass = -1;
        if(lowMass != null) {
            try {
                minMass = Double.parseDouble(lowMass);
                if(minMass < 0) {
                    throw new NumberFormatException();
                }
            } catch(Exception e) {
                flagError("You need to specify a positive (decimal) number for the lower mass treshold!\nYou provided '" + lowMass + "' instead!");
            }
        }

        double maxMass = -1;
        if(highMass != null) {
            try {
                maxMass = Double.parseDouble(highMass);
                if(maxMass < 0) {
                    throw new NumberFormatException();
                }
            } catch(Exception e) {
                flagError("You need to specify a positive (decimal) number for the higher mass treshold!\\nYou provided '" + highMass + "' instead!");
            }
        }

        if(inputFile == null) {
            flagError("You did not specify the '--input <input_file_name>' parameter!\n\nRun program without parameters for help.");
        } else if(outputFile == null) {
            flagError("You did not specify an outputfile!\n\nRun program without parameters for help.");
        } else {
            // Parameters were all found. Let's see if we can access all files that should be accessed.
            // Note that an existing output_file will result in clean and silent overwrite of the file!
            File output = new File(outputFile);
            File input = new File(inputFile);

            if(!output.exists()) {
                try {
                    output.createNewFile();
                } catch(IOException ioe) {
                    flagError("Could not create outputfile (" + outputFile + "): " + ioe.getMessage());
                }
            }
            if(!input.exists()) {
                flagError("The input file you specified (" + inputFile + ") could not be found!\nExiting...");
            } else {
                // The stuff we've received as input seems to be OK.
                try {
                    DBLoader loader = DBLoaderLoader.loadDB(input);
                    // Load a filter, or set of filters, if necessary.
                    Filter filter = null;
                    if(filter != null) {
                        filter = FilterLoader.loadFilter(filterString, filterParam, loader);
                    } else if(filterSet != null) {
                        filter = FilterLoader.processFilterSetANDLogic(filterSet, loader);
                    }
                    FASTAOutputThread fot = new FASTAOutputThread(null, loader, output, filter, minMass, maxMass);
                    System.out.println("\nOutputting DB in '" + inputFile + "' as FASTA DB in file '" + outputFile + "'...");
                    long start = System.currentTimeMillis();
                    fot.run();
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

    /**
     * Show the usage of the software.
     */
    private static void showUsage() {
        System.err.println("Usage:\n\tFASTAOutput [--filter <filter_name> [--filterParam <filter_parameter>]] [--lowMass <lower_mass_treshold> --highMass <higher_mass_treshold>] --input <input_file_name> <output_db_name>\n");
        System.err.println("\t\tOR\n");
        System.err.println("\tFASTAOutput [--filterSet \"<filter1_name=filter1_param;filter2_name;filter3_name=filter3_param;...>\"] [--lowMass <lower_mass_treshold> --highMass <higher_mass_treshold>] --input <input_file_name> <output_db_name>\n");
        System.err.println("\tThe former can be used to specify a single filter, and optionally its parameter, while the latter can be used to specify multiple filters. If the latter is used, parameters can be specified, separated by semicolons (;), and with (optional) parameters for each filter after an equals sign (=). Note that in a set, AND logic is used for all filters in the set.");
        System.err.println("\n\tNote that an existing output file will be silently overwritten in either mode!");
        System.exit(1);
    }
}
