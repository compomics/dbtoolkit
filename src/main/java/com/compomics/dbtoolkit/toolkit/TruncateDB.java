/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 12-jun-2003
 * Time: 16:24:21
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.implementations.ProteinMassFilter;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class implements a command-line tool to truncate each protein in a database to the
 * specified length, starting from either the N-terminus or the C-terminus.
 *
 * @author Lennart Martens
 */
public class TruncateDB {

    private static final int NTERMINUS = 0;
    private static final int CTERMINUS = 1;

    /**
     * Default constructor.
     */
    public TruncateDB() {
    }

    /**
     * The main method takes the start-up parameters
     * and processes the specified DB accordingly.
     *
     * @param   args    String[] with the start-up arguments.
     */
    public static void main(String[] args) {
        if(args == null || args.length == 0) {
            flagError("Usage:\n\tTruncateDB -(N|C) --truncate <number_of_residues> [--enzyme <enzymeName> [--mc <number_of_missed_cleavages>]] [--filter <filter_name> [--filterParam \"<filter_parameter>\"]] [--lowMass <lower_mass_treshold> --highMass <higher_mass_treshold>] --input <input_db_name> <output_db_name>\n\n\tNote that an existing output file will be silently overwritten!");
        }
        CommandLineParser clp = new CommandLineParser(args, new String[]{"enzyme", "mc", "filter", "filterParam", "truncate", "lowMass", "highMass", "input"});
        String inputFile = clp.getOptionParameter("input");
        String[] temp = clp.getParameters();
        if((temp == null) || (temp.length == 0)) {
            flagError("You need to specify an output file!\n\nRun program without parameters for help.");
        }
        String outputFile = temp[0];
        String enzymeName = clp.getOptionParameter("enzyme");
        String miscl = clp.getOptionParameter("mc");
        String truncate = clp.getOptionParameter("truncate");
        temp = clp.getFlags();
        if((temp == null) || (temp.length == 0)) {
            flagError("You need to specify a terminus to truncate (either '-C' OR '-N')!");
        }
        String terminus = temp[0];
        String filter = clp.getOptionParameter("filter");
        String lowMass =  clp.getOptionParameter("lowMass");
        String highMass  = clp.getOptionParameter("highMass");
        String filterParam = clp.getOptionParameter("filterParam");

        // See if all of this is correct.
        if(inputFile == null) {
            flagError("You did not specify the '--input <input_file_name>' parameter!\n\nRun program without parameters for help.");
        } else if(terminus == null) {
            flagError("You did not specify a truncating position (-N or -C)!\n\nRun program without parameters for help.");
        } else if(outputFile == null) {
            flagError("You did not specify an outputfile!\n\nRun program without parameters for help.");
        } else {
            // Parameters were all found. Let's see if we can access all files that should be accessed.
            // Note that an existing output_file will result in clean and silent overwrite of the file!
            File input = new File(inputFile);
            File output = new File(outputFile);

            // The terminus.
            int term = -1;
            if(terminus.equalsIgnoreCase("N")) {
                term = NTERMINUS;
            } else if(terminus.equalsIgnoreCase("C")) {
                term = CTERMINUS;
            } else {
                flagError("You need to specify a terminus to truncate, and it can be either 'C' or 'N', but nothing else!");
            }

            // The outputstream.
            PrintWriter out = null;
            if(!output.exists()) {
                try {
                    output.createNewFile();
                } catch(IOException ioe) {
                    flagError("Could not create outputfile (" + outputFile + "): " + ioe.getMessage());
                }
            }
            try {
                out = new PrintWriter(new FileWriter(output));
            } catch(IOException ioe) {
                flagError("Could not open stream to outputfile (" + outputFile + "): " + ioe.getMessage());
            }
            if(!input.exists()) {
                flagError("The input file you specified (" + inputFile + ") could not be found!\nExiting...");
            } else {
                // The stuff we've received as input seems to be OK.
                // Get the props for the AutoDBLoader...
                Properties p = null;
                try {
                    InputStream is = TruncateDB.class.getClassLoader().getResourceAsStream("DBLoaders.properties");
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

                // Parse the enzyme stuff and masses etc.
                double minMass = -1;
                if(lowMass != null) {
                    try {
                        minMass = Double.parseDouble(lowMass);
                    } catch(Exception e) {
                        flagError("You need to specify a (decimal) number for the lower mass treshold!");
                    }
                }

                double maxMass = -1;
                if(highMass != null) {
                    try {
                        maxMass = Double.parseDouble(highMass);
                    } catch(Exception e) {
                        flagError("You need to specify a (decimal) number for the higher mass treshold!");
                    }
                }

                int trunc = -1;
                if(truncate != null) {
                    try {
                        trunc = Integer.parseInt(truncate);
                    } catch(Exception e) {
                        flagError("You need to specify a whole number for the truncation size!");
                    }
                } else {
                    flagError("You need to specify an amount of resdiues to truncate to!");
                }

                // Try to load the mascot enzymefile.
                Enzyme enzyme = null;
                if(enzymeName != null) {
                    try {
                        InputStream in = RagDB.class.getClassLoader().getResourceAsStream("enzymes.txt");
                        if(in != null) {
                            MascotEnzymeReader mer = new MascotEnzymeReader(in);
                            enzyme = mer.getEnzyme(enzymeName);
                            if(enzyme == null) {
                                flagError("The enzyme you specified (" + enzymeName + ") was not found in the Mascot Enzymefile '" + RagDB.class.getClassLoader().getResource("enzymes.txt") + "'!");
                            } else {
                                if(miscl != null) {
                                    try {
                                        enzyme.setMiscleavages(Integer.parseInt(miscl));
                                    } catch(Exception e) {
                                        flagError("The number of allowed missed cleavages must be a whole number!");
                                    }
                                }
                            }
                        } else {
                            throw new IOException("File 'enzymes.txt' not found in current classpath!");
                        }
                    } catch(IOException ioe) {
                        flagError("You specified enzyme '" + enzymeName + "' for cleavage, but the Mascot Enzyme file was not found: " + ioe.getMessage());
                    }
                }

                // Try to load the filter (if any).
                Filter f = null;
                if(filter != null) {
                    try {
                        Properties props = new Properties();
                        InputStream in = RagDB.class.getClassLoader().getResourceAsStream("filters.properties");
                        if(in == null) {
                            throw new IOException("File 'filters.properties' not found in current classpath!");
                        }
                        props.load(in);
                        String filterParams = props.getProperty(filter);
                        if(filterParams == null) {
                            flagError("The filter you specified (" + filter + ") is not found in the 'filters.properties' file!");
                        }
                        StringTokenizer st = new StringTokenizer(filterParams, ",");
                        String filterClass = st.nextToken().trim();
                        String filterDB = st.nextToken().trim();
                        if(!filterDB.equals(loader.getDBName())) {
                            flagError("The filter you specified (" + filter + ") is not available for a '" + loader.getDBName() + "' database but for a '" + filterDB + "' database!");
                        } else {
                            try {
                                Constructor constr = null;
                                int type = 0;
                                Class lClass = Class.forName(filterClass);
                                if(lClass == null) {
                                    flagError("The class '" + filterClass + "' for your filter '" + filter + "' could not be found! Check your clasppath setting!");
                                }
                                if(filterParam == null) {
                                    constr = lClass.getConstructor(new Class[]{});
                                    type = 1;
                                } else if(filterParam.startsWith("!")) {
                                    constr = lClass.getConstructor(new Class[]{String.class, boolean.class});
                                    type = 2;
                                } else {
                                    constr = lClass.getConstructor(new Class[]{String.class});
                                    type = 3;
                                }
                                if(constr == null) {
                                    flagError("The filter does not support the " + ((filterParam != null)?"presence":"absence") + " of a" + ((filterParam.startsWith("!"))?"n inverted ":" ") + "parameter");
                                } else {
                                    if(type == 1) {
                                        f = (Filter)constr.newInstance(new Object[]{});
                                    } else if(type == 2) {
                                        f = (Filter)constr.newInstance(new Object[]{filterParam.substring(1), new Boolean(true)});
                                    } else {
                                        f = (Filter)constr.newInstance(new Object[]{filterParam});
                                    }
                                }
                            } catch(Exception e) {
                                flagError("Unable to load class '" + filterClass + "' for your filter '" + filter + "': " +e.getMessage());
                            }
                        }
                    } catch(IOException ioe) {
                        flagError("You specified a filter (" + filter + "), but the filter configuration file was not found: " + ioe.getMessage());
                    }
                }

                // Go about getting the job done.
                boolean massLimits = false;
                if((minMass >= 0) && (maxMass >= 0)) {
                    massLimits = true;
                }

                System.out.println("\nPerforming " + terminus + "-terminal truncation in '" + inputFile + "'.");
                System.out.println("\n\tParameters for this truncation are:");
                System.out.println("\t\t - Output file is: '" + outputFile + "'.");
                StringBuffer filterSettings = new StringBuffer();
                if(f == null) {
                    filterSettings.append("no filter specified.");
                } else {
                    filterSettings.append("filter '" + filter + "' chosen");
                    if(filterParam != null) {
                        filterSettings.append(" with " + ((filterParam.startsWith("!"))?"inverted":"") + " parameter '" + ((filterParam.startsWith("!"))?filterParam.substring(1):filterParam) + "'.");
                    } else {
                        filterSettings.append(" without parameters.");
                    }
                }
                System.out.println("\t\t - Filter settings: " + filterSettings.toString());
                System.out.println("\t\t - Masslimits: " + (massLimits?minMass + " Da to " + maxMass + " Da.":"no mass limits set."));
                System.out.println("\t\t - Truncation: truncating to " + trunc + " " + terminus + "-terminal residues.");
                System.out.println("\t\t - Enzyme: " + ((enzyme != null)?"\n" + enzyme.toString("\t\t\t"):"no enzyme specified."));
                System.out.println("\n\n");

                // Create a mass filter if necessary.
                ProteinMassFilter pmf = null;
                if(massLimits) {
                    pmf = new ProteinMassFilter(minMass, maxMass);
                }
                // Timer.
                long start = System.currentTimeMillis();
                // Counters.
                long lRead = 0l;
                long lWritten = 0l;

                try {
                    Protein protein = null;
                    // Read the first protein.
                    if(filter != null) {
                        protein = loader.nextFilteredProtein(f);
                    } else {
                        protein = loader.nextProtein();
                    }
                    // Cycle the DB.
                    while(protein != null) {
                        lRead++;
                        // First of all, truncate the protein.
                        if(term == NTERMINUS) {
                            protein = protein.getNTermTruncatedProtein(trunc);
                        } else {
                            protein = protein.getCTermTruncatedProtein(trunc);
                        }

                        // Now check whether an enzymatic digest has to take place.
                        Protein[] results = null;
                        if(enzyme != null) {
                            results = enzyme.cleave(protein);
                        } else {
                            results = new Protein[] {protein};
                        }

                        // Cycle the results, and print them.
                        for (int i = 0; i < results.length; i++) {
                            Protein result = results[i];
                            // At this point, we should see if we should check mass limits.
                            if(pmf != null) {
                                if(pmf.passesFilter(result)) {
                                    result.writeToFASTAFile(out);
                                    lWritten++;
                                }
                            } else {
                                result.writeToFASTAFile(out);
                                lWritten++;
                            }
                        }

                        // Read the next protein before completing this cycle.
                        if(filter != null) {
                            protein = loader.nextFilteredProtein(f);
                        } else {
                            protein = loader.nextProtein();
                        }
                    }
                    out.flush();
                    out.close();
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                long end = System.currentTimeMillis();
                System.out.println("Finished after " + ((end-start)/1000) + " seconds.");
                System.out.println("Read " + lRead + " entries from DB, written " + lWritten + " entries in output file.");
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
