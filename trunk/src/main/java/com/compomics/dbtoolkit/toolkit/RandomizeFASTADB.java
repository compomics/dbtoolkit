/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 23-jul-2003
 * Time: 8:42:36
 */
package com.compomics.dbtoolkit.toolkit;


/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:52:03 $
 */

import com.compomics.dbtoolkit.io.implementations.FASTADBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.DBLoaderLoader;
import com.compomics.dbtoolkit.gui.workerthreads.ShuffleDBThread;
import com.compomics.util.protein.Protein;

import java.io.*;

/**
 * This class will allow the 'randomization' of a sequence database. Output is in FASTA.
 *
 * @author Lennart Martens
 */
public class RandomizeFASTADB {

    /**
     * The main method is the entry point for the application.
     *
     * @param args  String[] with the start-up parameters.
     */
    public static void main(String[] args) {
        if(args == null || args.length != 1) {
            System.err.println("\n\nUsage:\n\tRandomizeFASTADB <input_database_file>\n");
            System.exit(1);
        }
        File input = new File(args[0]);
        if(!input.exists()) {
            System.err.println("\n\nInput database file '" + args[0] + "' does not exist!\n");
            System.exit(1);
        }
        try {
            ShuffleDBThread sdt = new ShuffleDBThread(input);
            sdt.shuffle();
        } catch(IOException ioe) {
            System.err.println("\n\nUnable to randomize database:\n" + ioe.getMessage() + "\n\n");
            ioe.printStackTrace();
        }
    }
}
