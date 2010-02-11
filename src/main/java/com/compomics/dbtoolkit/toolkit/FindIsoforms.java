/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-mrt-03
 * Time: 15:28:17
 */
package com.compomics.dbtoolkit.toolkit;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.protein.Protein;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class allows the user to find isoforms containing a certain peptide.
 *
 * @author Lennart Martens
 */
public class FindIsoforms {

    public static void main(String[] args) {
        // First see if we should output anything useful.
        if(args == null || args.length == 0) {
            flagError("Usage:\n\tFindIsoforms [-i] --input <input_file_name> <input_db_name>");
        }
        CommandLineParser clp = new CommandLineParser(args, new String[]{"input"});
        String sequenceFile = clp.getOptionParameter("input");
        String inputFile = clp.getParameters()[0];
        String[] tempFlags = clp.getFlags();
        boolean showOnlyIsoforms = false;
        if(tempFlags != null && tempFlags.length == 1 && tempFlags[0].equals("i")) {
            showOnlyIsoforms = true;
        }

        // See if all of this is correct.
        if(sequenceFile == null) {
            flagError("You did not specify the '--input <input_file_name>' parameter!\n\nRun program without parameters for help.");
        } else if(inputFile == null) {
            flagError("You did not specify an intput database!\n\nRun program without parameters for help.");
        } else {
            // Parameters were all found. Let's see if we can access all files that should be accessed.
            // Note that an existing output_file will result in clean and silent overwrite of the file!
            File input = new File(inputFile);
            File sequences = new File(sequenceFile);
            if(!sequences.exists()) {
                flagError("The inputfile you specified (" + sequenceFile + ") could not be found!\nExiting...");
            }
            if(!input.exists()) {
                flagError("The database you specified (" + inputFile + ") could not be found!\nExiting...");
            } else {
                // The stuff we've received as input seems to be OK.
                // Get the props for the AutoDBLoader...
                Properties p = null;
                try {
                    InputStream is = FindIsoforms.class.getClassLoader().getResourceAsStream("DBLoaders.properties");
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
                // Okay, we've got a DB to load.
                // First get all the sequences into a Hash, then we can continue.
                try {
                    HashMap allSeqs = new HashMap();
                    BufferedReader br = new BufferedReader(new FileReader(sequences));
                    String line = null;
                    while((line = br.readLine()) != null) {
                        line = line.trim();
                        if(!line.equals("")) {
                            allSeqs.put(line, null);
                        }
                    }
                    br.close();
                    String[] allSeqArray = new String[allSeqs.size()];
                    Iterator iter = allSeqs.keySet().iterator();
                    int count = 0;
                    while(iter.hasNext()) {
                        allSeqArray[count] = (String)iter.next();
                        count++;
                    }
                    // All sequences read and stored,
                    // now read the db and see if we can find anything.
                    Protein prot = null;
                    while((prot = loader.nextProtein()) != null) {
                        String sequence = prot.getSequence().getSequence();
                        String accession = prot.getHeader().getAccession();
                        for(int i = 0; i < allSeqArray.length; i++) {
                            if(sequence.indexOf(allSeqArray[i]) >= 0) {
                                Object tempObj = allSeqs.get(allSeqArray[i]);
                                if(tempObj != null) {
                                    Hit h = (Hit)tempObj;
                                    h.addHit(accession);
                                } else {
                                    allSeqs.put(allSeqArray[i], new Hit(accession));
                                }
                            }
                        }
                    }
                    loader.close();
                    // Okay, print our findings.
                    for(int i = 0; i < allSeqArray.length; i++) {
                        String s = allSeqArray[i];
                        Object tempObj = allSeqs.get(s);
                        if(tempObj != null) {
                            Hit h = (Hit)tempObj;
                            if((showOnlyIsoforms) && (h.getCount() < 2)) {
                                continue;
                            }
                            System.out.print(";" + s + ";" + h.getCount() + ";" + h.getAccessions() + "\n");
                        } else {
                            if(showOnlyIsoforms) {
                                continue;
                            }
                            System.out.print(";" + s + ";" + "0;;\n");
                        }
                    }
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    private static class Hit {
        /**
         * The relevant accession numbers.
         */
        String iAccessions = null;

        /**
         * The number of times a hit was found.
         */
        int iCounter = 0;

        /**
         * This constructor allows the creation of a hit, based on the first hit.
         *
         * @param   aAccession  String with the accession number of the hit.
         */
        public Hit(String aAccession) {
            this.iCounter = 1;
            this.iAccessions = aAccession;
        }

        /**
         *  This method allows the caller to add a hit to the list.
         *
         * @param   aAccession  String with the accession number.
         */
        public void addHit(String aAccession) {
            this.iAccessions += "^A" + aAccession;
            this.iCounter++;
        }

        public int getCount() {
            return this.iCounter;
        }

        public String getAccessions() {
            return this.iAccessions;
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
