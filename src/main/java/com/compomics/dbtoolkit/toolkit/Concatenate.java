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

import com.compomics.dbtoolkit.gui.workerthreads.ConcatenateThread;
import com.compomics.util.general.CommandLineParser;

import java.io.File;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a command-line way of calling the ConcatenateThread
 * class, albeit it is NOT called in threading mode.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.gui.workerthreads.ConcatenateThread
 */
public class Concatenate {

    /**
     * Default constructor.
     */
    public Concatenate() {
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
            flagError("Usage:\n\tConcatenate --input1 <input1_file_name> [--input2 <input2_file_name>] <output_file_name>\n\n\tNote that an existing output file will be silently overwritten!");
        }
        CommandLineParser clp = new CommandLineParser(args, new String[]{"input1", "input2"});
        String input1File = clp.getOptionParameter("input1");
        String input2File = clp.getOptionParameter("input2");
        String outputFile = clp.getParameters()[0];

        // See if all of this is correct.
        if(input1File == null) {
            flagError("You did not specify the '--input1 <input_file_name>' parameter!\n\nRun program without parameters for help.");
        } else if(outputFile == null) {
            flagError("You did not specify an outputfile!\n\nRun program without parameters for help.");
        } else {
            // Parameters were all found. Let's see if we can access all files that should be accessed.
            // Note that an existing output_file will result in clean and silent overwrite of the file!
            File input1 = new File(input1File);
            File input2 = null;
            if(input2File != null) {
                input2 = new File(input2File);
            }
            File output = new File(outputFile);

            if(!input1.exists()) {
                flagError("The input file you specified (" + input1File + ") does not exist!\nExiting...");
            } else if((input2 != null) &&(!input2.exists())) {
                 flagError("The input file you specified (" + input2File + ") could not be found!\nExiting...");
            } else {
                // If the output file does not yet exist, create it.
                if(!output.exists()) {
                    try {
                        output.createNewFile();
                    } catch(IOException ioe) {
                        flagError("Could not create outputfile (" + outputFile + "): " + ioe.getMessage());
                    }
                }
                // The stuff we've received as input seems to be OK.
                ConcatenateThread ct = null;
                if(input2 == null) {
                    System.out.println("\n\nOnly one input file specified, copying file '" + input1File + "' to output file '" + outputFile + "'...");
                    ct = new ConcatenateThread(input1, output);
                } else {
                    System.out.println("\n\nTwo input files specified, concatenating file '" + input2File + "' to  file '" + input1File + "' in output file '" + outputFile + "'...");
                    ct = new ConcatenateThread(input1, input2, output);
                }

                long start = System.currentTimeMillis();
                ct.run();
                long end = System.currentTimeMillis();
                System.out.println("Finished after " + ((end-start)/1000) + " seconds.");
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
