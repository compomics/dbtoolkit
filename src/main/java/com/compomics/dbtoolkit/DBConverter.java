/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 13:42:33
 */
package com.compomics.dbtoolkit;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.util.general.CommandLineParser;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class can be used to convert a database from one format into another.
 *
 * @author Lennart Martens
 */
public class DBConverter {

    /**
     * The auto database loader.
     */
    private AutoDBLoader iAutoDBLoader = null;

    /**
     * Default constructor.
     */
    public DBConverter() {
        Properties p = null;
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("DBLoaders.properties");
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
        iAutoDBLoader = new AutoDBLoader(classNames);
    }

    /**
     * This method will read a database in the specified format from the specified inputfile,
     * and output a FASTA database with the standard information as expected by most
     * other programs.
     *
     * @param   aInputFilename  String  with the inputfile name.
     * @param   aOutputFilename String  with the outputfile name.
     */
    public void toFASTAFile(String aInputFilename, String aOutputFilename) {
        // First load the original DB from file.
        // We need the correct DBLoader implementation for that...
        try {
            DBLoader db = iAutoDBLoader.getLoaderForFile(aInputFilename);
            // Create the DB outputfile.
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(aOutputFilename)));

            // Convert...
            String fastaLine = null;
            while((fastaLine = db.nextFASTAEntry()) != null) {
                // The FASTA entry already contains an endline at the end.
                pw.print(fastaLine);
            }
            // Clear the inputDB.
            db = null;

            // Flush and close the PrintWriter.
            pw.flush();
            pw.close();
        } catch(UnknownDBFormatException udfe) {
            System.err.println(udfe.getMessage());
        } catch(IOException ioe) {
            System.err.println("An IOException occurred while trying to convert the '" + aInputFilename + "' DB file to FASTA format.\n");
            ioe.printStackTrace();
        }
    }

    /**
     * This method will read a database in the specified format from the specified inputfile,
     * and output a filtered FASTA database with the standard information as expected by most
     * other programs.
     *
     * @param   aFilter Filter instance to apply.
     * @param   aInputFilename  String  with the inputfile name.
     * @param   aOutputFilename String  with the outputfile name.
     */
    public void toFilteredFASTAFile(Filter aFilter, String aInputFilename, String aOutputFilename) {
        // First load the original DB from file.
        // We need the correct DBLoader implementation for that...
        try {
            DBLoader db = iAutoDBLoader.getLoaderForFile(aInputFilename);
            // Create the DB outputfile.
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(aOutputFilename)));

            // Convert...
            String fastaLine = null;
            while((fastaLine = db.nextFilteredFASTAEntry(aFilter)) != null) {
                // The FASTA entry already contains an endline at the end.
                pw.print(fastaLine);
            }
            // Clear the inputDB.
            db = null;

            // Flush and close the PrintWriter.
            pw.flush();
            pw.close();
        } catch(UnknownDBFormatException udfe) {
            System.err.println(udfe.getMessage());
        } catch(IOException ioe) {
            System.err.println("An IOException occurred while trying to convert the '" + aInputFilename + "' DB file to FASTA format.\n");
            ioe.printStackTrace();
        }
    }


    /**
     * The main method can be used to convert databases into FASTA format from the command line.
     *
     * @param   args    String[]    with the necessary arguments.
     */
    public static void main(String[] args) {
        final int INPUT = 0;
        final int OUTPUT = 1;
        final String FILTER = "filter";
        final String FILTERPARAM = "filterParam";
        final String[] FORMATS = {DBLoader.SWISSPROT, DBLoader.FASTA};

        DBConverter dbConv = new DBConverter();

        CommandLineParser clp = new CommandLineParser(args, new String[] {FILTER, FILTERPARAM, });

        String[] params = clp.getParameters();
        String filterID = clp.getOptionParameter(FILTER);
        String filterParam = clp.getOptionParameter(FILTERPARAM);

        if( (params == null || params.length != 2) ) {
            System.err.println("\nUsage:\n\n\tDBConverter [--filter <filterName> [--filterParam <filter_parameter>]] <inputfile> <outputfile>\n\n");
            System.err.println();
            System.exit(1);
        } else {
            // See if we have a filter.
            if(filterID == null) {
                try {
                    System.out.println("\nConverting...");
                    dbConv.toFASTAFile(params[INPUT], params[OUTPUT]);
                    System.out.println("Done.\n");
                } catch(Exception e) {
                    System.err.println("\n"+e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // Try to load the filter (if any).
                Filter f = null;
                try {
                    Properties props = new Properties();
                    InputStream in = DBConverter.class.getClassLoader().getResourceAsStream("filters.properties");
                    if(in == null) {
                        throw new IOException("File 'filters.properties' not found in current classpath!");
                    }
                    props.load(in);
                    String filterParams = props.getProperty(filterID);
                    if(filterParams == null) {
                        flagError("The filter you specified (" + filterID + ") is not found in the 'filters.properties' file!");
                    }
                    StringTokenizer st = new StringTokenizer(filterParams, ",");
                    String filterClass = st.nextToken().trim();
                    String filterDB = st.nextToken().trim();
                    try {
                        Constructor constr = null;
                        int type = 0;
                        Class lClass = Class.forName(filterClass);
                        if(lClass == null) {
                            flagError("The class '" + filterClass + "' for your filter '" + filterID + "' could not be found! Check your clasppath setting!");
                        }
                        if(filterParam == null) {
                            try {
                                constr = lClass.getConstructor(new Class[]{});
                            } catch(Exception exc) {
                            }
                            type = 1;
                        } else if(filterParam.startsWith("!")) {
                            try {
                                constr = lClass.getConstructor(new Class[]{String.class, boolean.class});
                            } catch(Exception exc) {
                            }
                            type = 2;
                        } else {
                            try {
                                constr = lClass.getConstructor(new Class[]{String.class});
                            } catch(Exception exc) {
                            }
                            type = 3;
                        }
                        if(constr == null) {
                            flagError("The '" + filterID + "' filter does not support the " + ((filterParam != null)?"presence":"absence") + " of a" + (((filterParam != null) && (filterParam.startsWith("!")))?"n inverted ":" ") + "parameter!");
                        } else {
                            if(type == 1) {
                                f = (Filter)constr.newInstance(new Object[]{});
                            } else if(type == 2) {
                                f = (Filter)constr.newInstance(new Object[]{filterParam.substring(1), new Boolean(true)});
                            } else {
                                f = (Filter)constr.newInstance(new Object[]{filterParam});
                            }
                        }
                        System.out.println("\nConverting...");
                        dbConv.toFilteredFASTAFile(f, params[INPUT], params[OUTPUT]);
                        System.out.println("Done.\n");
                    } catch(Exception e) {
                        e.printStackTrace();
                        flagError("Unable to load class '" + filterClass + "' for your filter '" + filterID + "': " +e.getMessage());
                    }
                } catch(IOException ioe) {
                    flagError("You specified a filter (" + filterID + "), but the filter configuration file was not found: " + ioe.getMessage());
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
