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

import com.compomics.dbtoolkit.gui.workerthreads.ClearRedundancyThread;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.general.CommandLineParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a command-line way of calling the ClearRedundancyThread
 * class, albeit it is NOT called in threading mode.
 *
 * @author Lennart Martens
 * @see com.compomics.dbtoolkit.gui.workerthreads.ClearRedundancyThread
 */
public class ClearRedundancy {

    /**
     * Default constructor.
     */
    public ClearRedundancy() {
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
            flagError("Usage:\n\tClearRedundancy --temp <tempFolder> --output <output_file_name> <input_db_name>\n\n\tNote that an existing output file will be silently overwritten!");
        }
        CommandLineParser clp = new CommandLineParser(args, new String[]{"temp", "output"});
        String tempFolder = clp.getOptionParameter("temp");
        String outputFile = clp.getOptionParameter("output");
        String inputFile = clp.getParameters()[0];

        // See if all of this is correct.
        if(tempFolder == null) {
            flagError("You did not specify the '--temp <tempFolder>' parameter!\n\nRun program without parameters for help.");
        } else if(outputFile == null) {
            flagError("You did not specify the '--output <output_file_name>' parameter!\n\nRun program without parameters for help.");
        } else if(inputFile == null) {
            flagError("You did not specify an inputfile!\n\nRun program without parameters for help.");
        } else {
            // Parameters were all found. Let's see if we can access all files that should be accessed.
            // Note that an existing output_file will result in clean and silent overwrite of the file!
            File temp = new File(tempFolder);
            File output = new File(outputFile);
            File input = new File(inputFile);

            if(!temp.exists()) {
                flagError("The temporary storage folder you specified (" + tempFolder + ") does not exist!\nExiting...");
            } else if(!output.exists()) {
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
                // Get the props for the AutoDBLoader...
                Properties p = null;
                try {
                    InputStream is = ClearRedundancy.class.getClassLoader().getResourceAsStream("DBLoaders.properties");
                    p = new Properties();
                    if(is != null) {
                        p.load(is);
                        is.close();
                    }
                } catch(IOException ioe) {
                }
                // See if we managed to load the 'DBLoader.properties' file, else default to built-in types.
                if(p == null || p.size() == 0) {
                    System.out.println("\t - Unable to find 'DBLoaders.properties' file, defaulting to built-in types (SwissProt & FASTA only!)...");
                    p = new Properties();
                    p.put("1", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader");
                    p.put("2", "com.compomics.dbtoolkit.io.implementations.FASTADBLoader");
                }
                String[] classNames = new String[p.size()];
                Iterator it = p.values().iterator();
                int counter = 0;
                while(it.hasNext()) {
                    classNames[counter] = (String)it.next();
                    counter++;
                }

                AutoDBLoader adb = new AutoDBLoader(classNames);
                DBLoader loader = null;
                try {
                    loader = adb.getLoaderForFile(input.getAbsolutePath());
                } catch(IOException ioe) {
                } catch(UnknownDBFormatException udfe) {
                }
                if(loader == null) {
                    flagError("Unable to determine database type for your inputfile (" + inputFile + "), exiting...");
                }
                ClearRedundancyThread crt = new ClearRedundancyThread(temp, output, loader, adb);
                System.out.println("\nClearing redundancy in '" + inputFile + "'...");
                long start = System.currentTimeMillis();
                crt.run();
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
