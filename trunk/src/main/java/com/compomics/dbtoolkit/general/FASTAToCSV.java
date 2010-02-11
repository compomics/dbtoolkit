/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 21-jan-03
 * Time: 14:01:24
 */
package com.compomics.dbtoolkit.general;

import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;

import java.io.BufferedWriter;
import java.io.FileWriter;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This program takes a FASTA file and outputs the information in a CSV file
 * for easy loading into programs into Excel.
 *
 * @author Lennart Martens
 */
public class FASTAToCSV {

    /**
     * Main method runs the application. Simply input infile and outfile as parameters.
     *
     * @param   args    String[] with infile, outfile.
     */
    public static void main(String[] args) {

        if(args == null || args.length == 0) {
            System.err.println("\n\nUsage:\n\tFASTAToCSV <input_file> <output_file>\n");
            System.exit(1);
        }

        // Get infile and outfile.
        String infile = args[0];
        String outfile = args[1];
        if(infile==null || outfile==null) {
            System.err.println("\n\nYou need to specify both an input file and an output file!\n");
            System.exit(1);
        }

        // Okay, let's get going.
        try {
            DBLoader loader = new AutoDBLoader(new String[] {"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader"}).getLoaderForFile(infile);
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
            out.write(";Accession;Start location;End location;Sequence;Description;\n");
            Protein p = null;
            while((p = loader.nextProtein()) != null) {
                Header header = p.getHeader();
                out.write(";" + header.getAccession());
                if(header.getStartLocation() >= 0) {
                    out.write(";" + header.getStartLocation());
                } else {
                    out.write(";N/A");
                }
                if(header.getEndLocation() >= 0) {
                    out.write(";" + header.getEndLocation());
                } else {
                    out.write(";N/A");
                }
                out.write(";" + p.getSequence().getSequence());
                out.write(";" + header.getDescription() + "\n");
            }
            loader.close();
            out.flush();
            out.close();
        } catch(Exception e) {
            System.err.println("\n\n" + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
