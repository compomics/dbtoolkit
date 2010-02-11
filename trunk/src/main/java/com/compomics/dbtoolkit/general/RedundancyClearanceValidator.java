/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 16-jan-03
 * Time: 10:08:29
 */
package com.compomics.dbtoolkit.general;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.protein.Protein;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class checks whether the clearing of the redundancy did not miss any data.
 * It takes the original DB as source, collecting all the sequences and then
 * attempts to match them all in the non-redundant db.
 *
 * @author Lennart Martens.
 */
public class RedundancyClearanceValidator {

    /**
     * Two parameters:
     *  - master: DB file with the original sequences.
     *  - (result): DB file with the non-redundant entries.
     * Run without args to see usage.
     */
    public static void main(String[] args) {
        if(args == null || args.length == 0) {
            printUsage();
        }
        CommandLineParser clp = new CommandLineParser(args, new String[]{"master"});
        String master = clp.getOptionParameter("master");
        if(master == null || master.trim().equals("")) {
            System.err.println("\n\nNo 'master' DB present in command-line!");
            printUsage();
        }
        String[] temp = clp.getParameters();
        if(temp == null || temp.length == 0) {
            System.err.println("\n\nNo non-redundant DB present in command-line!");
            printUsage();
        }

        try {
            // In getting here, all's well.
            AutoDBLoader auto = new AutoDBLoader(new String[]{"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader"});

            DBLoader loader = auto.getLoaderForFile(temp[0]);
            HashMap all = new HashMap();
            // Read all in non-red.
            int counterRead = 0;
            Protein p = null;
            System.out.println("\n\nReading non-redundant DB '" + temp[0] + "'...");
            while((p = loader.nextProtein()) != null) {
                counterRead++;
                String tempSeq = p.getSequence().getSequence();
                all.put(tempSeq, "1");
            }
            loader.close();
            System.out.println("Completed reading non-redundant DB.\n\n * Found " + counterRead + " unique sequences.");

            // Verify all in original.
            HashMap missed = new HashMap();
            loader = auto.getLoaderForFile(master);
            p = null;
            int readCounter = 0;
            int foundCounter = 0;
            int missedCounter = 0;
            System.out.println("\n\nVerifying sequences in original DB '" + master + "'...");
            while((p = loader.nextProtein()) != null) {
                readCounter++;
                String tempSeq = p.getSequence().getSequence();
                if(all.containsKey(tempSeq)) {
                    foundCounter++;
                } else {
                    missedCounter++;
                    missed.put(tempSeq, "0");
                }
            }
            loader.close();
            p = null;

            double foundPercent = ((double)foundCounter/(double)readCounter) *100;
            double missedPercent = ((double)missedCounter/(double)readCounter) *100;
            System.out.println("\n\n * Read " + readCounter + " sequences.\n");
            System.out.println("   - " + foundCounter + " sequences were found (" + new BigDecimal(foundPercent).setScale(2, BigDecimal.ROUND_HALF_UP) + "% of the DB is in the non-redundant.)");
            System.out.println("   - " + missedCounter + " sequences were NOT found (" + new BigDecimal(missedPercent).setScale(2, BigDecimal.ROUND_HALF_UP) + "% of the DB is missing in the non-redundant!!)");
            System.out.println("\n * Missed sequences were:\n");
            Iterator iter = missed.keySet().iterator();
            boolean any = false;
            while(iter.hasNext()) {
                any = true;
                System.out.println("\t" + iter.next());
            }
            if(!any) {
                System.out.println("\tNone!");
            }
            System.out.println("\n\nThank you, and goodbye!\n\n");
            all = null;
            missed = null;
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
            System.exit(1);
        } catch(UnknownDBFormatException udfe) {
            System.err.println(udfe.getMessage());
            udfe.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Prints usage info + exits JVM (status == 1).
     */
    private static void printUsage() {
        System.err.println("\n\nUsage:\n\tRedundancyClearanceValidator --master <original_DB> <non_redundant_DB>\n");
        System.exit(1);
    }
}
