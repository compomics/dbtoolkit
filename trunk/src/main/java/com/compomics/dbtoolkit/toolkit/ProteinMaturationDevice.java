package com.compomics.dbtoolkit.toolkit;

/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 07-Jul-2007
 * Time: 18:38:01
 */
/*
 * CVS information:
 *
 * $Revision: 1.2 $
 * $Date: 2008/11/25 16:43:53 $
 */

import com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader;
import com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.interfaces.SwissProtLoader;
import com.compomics.dbtoolkit.io.FilterLoader;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.protein.Protein;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * This class reads a SwissProt formatted DB and processes all protein contained herein
 * into all their annotated chains. Output is written to the specified file.
 *
 * @author Lennart Martens
 * @version $Id: ProteinMaturationDevice.java,v 1.2 2008/11/25 16:43:53 lennart Exp $
 */
public class ProteinMaturationDevice {

    private static final int CHAIN_MODE = 1;
    private static final int SIGNAL_PROPEP_MODE = 2;
    private static final int EXCL_SIGNAL_PROPEP_MODE = 3;

    private static final String CHAIN_MODE_STRING = "CHAIN";
    private static final String SIGNAL_PROPEP_MODE_STRING = "PROPEP";
    private static final String EXCL_SIGNAL_PROPEP_MODE_STRING = "EXCL_PROPEP";


    public static void main(String[] args) {
        CommandLineParser clp = new CommandLineParser(args, new String[]{"filterSet", "mode"});

        String[] params = clp.getParameters();

        if((args == null) || (args.length < 2) || (params.length != 2)) {
            printUsage();
        }

        // Check mode.
        String modeString = clp.getOptionParameter("mode");
        int mode = -1;
        if(modeString != null) {
            // See if we recognize it.
            if(modeString.trim().equalsIgnoreCase(CHAIN_MODE_STRING)) {
                mode = ProteinMaturationDevice.CHAIN_MODE;
            } else if(modeString.trim().equalsIgnoreCase(SIGNAL_PROPEP_MODE_STRING)) {
                mode = ProteinMaturationDevice.SIGNAL_PROPEP_MODE;
            } else if(modeString.trim().equalsIgnoreCase(EXCL_SIGNAL_PROPEP_MODE_STRING)) {
                mode = ProteinMaturationDevice.EXCL_SIGNAL_PROPEP_MODE;
            }  else {
                System.err.println("\n\nThe operational mode you specified ('" + modeString + "') is unknown to me!\nSee below for valid options.");
                printUsage();
            }
        } else {
            System.err.println("\n\nNo operational mode was specified! See instructions below.");
            printUsage();
        }


        // See if the flag to include original sequences in the output is set.
        boolean includeOriginal = false;
        boolean initMetRemoval = false;
        String[] temp = clp.getFlags();
        if(temp != null && temp.length != 0) {
            if(temp.length > 2) {
                printError("You specified " + temp.length + " flags, whereas I understand only two: '-c' and '-m'!\n\nRun the application without parameters to see full usage information.");
            }

            // OK, we have one or two flags. Cycle them.
            for (int i = 0; i < temp.length; i++) {
                String s = temp[i];
                if(s.equals("c")) {
                    // We have a 'c' flag. Include original sequences in output.
                    includeOriginal = true;
                } else if(s.equals("m")) {
                    initMetRemoval = true;
                } else {
                    printError("I do not understand the flag you specified ('" + temp[0] + "'); I understand only two: '-c' and '-m'!\n\nRun the application without parameters to see full usage information.");
                }
            }
        }

        // OK, see if the input database file exists.
        File database = new File(params[0]);
        if(!database.exists()) {
            printError("The database file you specified ('" + params[0] + "') could not be found!");
        }
        // Check for directory.
        if(database.isDirectory()) {
            printError("The database file you specified ('" + params[0] + "') is a folder, not a file!");
        }

        // Check the presence of a taxID, and whether it is a number.
        String filterSet = clp.getOptionParameter("filterSet");

        try {
            SwissProtLoader dbloader = new SwissProtDBLoader();
            // Test whether the database file is indeed of the correct (SwissProt) format.
            if(!dbloader.canReadFile(database)) {
                // It could be zipped.
                dbloader = new ZippedSwissProtDBLoader();
                if(!dbloader.canReadFile(database)) {
                    printError("The database file you specified ('" + database.getAbsolutePath() + "') is not recognized as a (compressed) Swiss-Prot formatted '.dat' file!");
                }
            }
            // OK, in getting here, we should be able to load the DB file.
            // But first assemble the info string and the filter (if required).
            StringBuffer info = new StringBuffer("Processing input database ('" + database.getAbsolutePath() + "') in " + modeString.trim().toUpperCase() + " mode");
            info.append(", " + ( (includeOriginal && (mode != ProteinMaturationDevice.EXCL_SIGNAL_PROPEP_MODE))?"including":"excluding") + " original sequences, and "
                        + ((initMetRemoval || (mode != ProteinMaturationDevice.CHAIN_MODE))?"removing":"ignoring") + " initiator methionines...");
            // Now load the file.
            dbloader.load(database.getAbsolutePath());
            Filter filter = null;
            if(filterSet != null) {
                filter = FilterLoader.processFilterSetANDLogic(filterSet, dbloader);
            }
            String rawEntry = null;
            if(filter == null) {
                rawEntry = dbloader.nextRawEntry();
            } else {
                rawEntry = dbloader.nextFilteredRawEntry(filter);
            }
            // Create the output printwriter.
            PrintWriter pw = new PrintWriter(new FileWriter(new File(params[1])));
            // User info printout.
            System.out.println("\n\n" + info.toString() + "");
            // Counters.
            int inputCounter = 0;
            int outputCounter = 0;
            int originalOutputCounter = 0;
            // Cycle each entry.
            while(rawEntry != null) {
                inputCounter++;
                HashMap parsed = dbloader.processRawData(rawEntry);
                String features = (String)parsed.get("FT");
                StringReader sr = new StringReader(features);
                BufferedReader br = new BufferedReader(sr);
                // Collect mature positions for each.
                ArrayList positions = new ArrayList();
                String line = null;
                while((line = br.readLine()) != null) {
                    line = line.trim();
                    if(mode == ProteinMaturationDevice.CHAIN_MODE) {
                        processChains(line, positions);
                    } else if(mode == ProteinMaturationDevice.SIGNAL_PROPEP_MODE || mode == ProteinMaturationDevice.EXCL_SIGNAL_PROPEP_MODE) {
                        processSignalPropep(line, positions, initMetRemoval);
                    }
                }
                // OK, all positions collected.
                // Determine action for output.
                int[] result = null;
                if(mode == ProteinMaturationDevice.CHAIN_MODE || mode == ProteinMaturationDevice.SIGNAL_PROPEP_MODE) {
                    result = outputNormalMode(includeOriginal, positions, dbloader, rawEntry, pw);
                } else {
                    result = outputExclMode(includeOriginal, positions, dbloader, rawEntry, pw);
                }

                // Add tallies.
                outputCounter += result[0];
                originalOutputCounter += result[1];

                // Move to next entry.
                if(filter == null) {
                    rawEntry = dbloader.nextRawEntry();
                } else {
                    rawEntry = dbloader.nextFilteredRawEntry(filter);
                }
            }
            pw.flush();
            pw.close();
            // User-friendly feedback.
            System.out.println("\n\nRead " + inputCounter + ((filter != null)?" filtered":"") + " proteins from the input database, and\nwrote " + originalOutputCounter + " original sequences, and " + outputCounter + " mature sequences to the output database.");
            System.out.println("\nAll done.\nThank you for using the ProteinMaturationDevice.\n\n");
        } catch(IOException ioe) {
            ioe.printStackTrace();
            printError("A file read/write error occured: " + ioe.getMessage());
        }
    }

    /**
     * This method resolves all CHAIN features that are annotated in the FT lines.
     *
     * @param aLine String with the FT line to parse.
     * @param aPositions    ArrayList with the positions extracted from the FT lines of this
     *                                protein entry. Note that this a reference parameter
     *                                which should be populated here.
     */
    private static void processChains(String aLine, ArrayList aPositions) {
        // See if we have a chain annotation.
        if(aLine.startsWith("CHAIN")) {
            StringTokenizer st = new StringTokenizer(aLine, " \t");
            // 'CHAIN'
            st.nextToken();
            // Start pos.
            String startString = st.nextToken();
            int start = -1;
            if(!startString.startsWith("<") && startString.indexOf("?") < 0) {
                try {
                    start = Integer.parseInt(startString.trim());
                    // From human-readable (1-based) to machine (0-based) String
                    // positions.
                    start--;
                    if(start < 0) {
                        System.err.println("  # Decrementing start with 1 resulted in a negative value for '" + startString + "' on line '" + aLine + "'!");
                    }
                } catch(NumberFormatException nfe) {
                    System.err.println("  # Unable to parse int for start position from '" + startString + "' on line '" + aLine + "'!");
                }
            }
            // Stop pos.
            String stopString = st.nextToken();
            int stop = -1;
            if(!stopString.startsWith(">") && stopString.indexOf("?") < 0) {
                try {
                    stop = Integer.parseInt(stopString.trim());
                } catch(NumberFormatException nfe) {
                    System.err.println("  # Unable to parse int for stop position from '" + stopString + "' on line '" + aLine + "'!");
                }
            }
            // OK, if we have a valid 'start' and 'stop' position, we can use it,
            // otherwise: skip.
            if(start >= 0 && stop > 0) {
                aPositions.add(new InnerPosition(start, stop));
            }
        }
    }

    /**
     * This method only resolves those features that are annotated as SIGNAL or PROPEP.
     *
     * @param aLine String with the FT line to parse.
     * @param aPositions    ArrayList with the positions extracted from the FT lines of this
     *                                protein entry. Note that this a reference parameter
     *                                which should be populated here.
     * @param aInitMetRemoval   boolean to indicate whether we should remove initiator methionines.
     */
    private static void processSignalPropep(String aLine, ArrayList aPositions, boolean aInitMetRemoval) {
        // See if we have a chain annotation.
        if(aLine.startsWith("SIGNAL") || aLine.startsWith("PROPEP") || aLine.startsWith("TRANSIT")) {
            StringTokenizer st = new StringTokenizer(aLine, " \t");
            // 'SIGNAL', 'PROPEP', or 'TRANSIT'
            st.nextToken();
            // Start pos. Not interesting here.
            String startString = st.nextToken();
            // Stop pos.
            String stopString = st.nextToken();
            int stop = -1;
            if(!stopString.startsWith(">") && stopString.indexOf("?") < 0) {
                try {
                    stop = Integer.parseInt(stopString.trim());
                } catch(NumberFormatException nfe) {
                    System.err.println("  # Unable to parse int for stop position from '" + stopString + "' on line '" + aLine + "'!");
                }
            }
            // OK, if we have a valid 'stop' position, we can use it,
            // otherwise: skip.
            if(stop > 0) {
                aPositions.add(new InnerPosition(stop, -1));
            }
        } else if(aLine.startsWith("INIT_MET") && aInitMetRemoval) {
            aPositions.add(new InnerPosition(1, -1));
        }
    }

    /**
     * Method that writes out the desired output of (processed) proteins, and returns the number of
     * processed and original entries written.
     *
     * @param aIncludeOriginal  boolean to indicate inclusion of original sequence.
     * @param aPositions    ArrayLists with the InnerPosition objects for this protein.
     * @param aDBloader SwissProtLoader with the DBLoader to the database.
     * @param aRawEntry String with the raw entry.
     * @param aPW   PrintWriter to write th output to.
     * @return  int[]   with as '0' element the number of processed entries written,
     *                  and '1' element the number of original entries written.
     * @throws IOException  if the processing or writing field.
     */
    private static int[] outputNormalMode(boolean aIncludeOriginal, ArrayList aPositions, SwissProtLoader aDBloader, String aRawEntry, PrintWriter aPW) throws IOException {
        int originalOutputCounter = 0;
        int outputCounter = 0;
        // First see if we need to write out the original sequence.
        if(aIncludeOriginal) {
            Protein protein = new Protein(aDBloader.toFASTAString(aRawEntry, false));
            protein.writeToFASTAFile(aPW);
            originalOutputCounter++;
        }

        // Now see if there are any chains to be written.
        Iterator iter = aPositions.iterator();
        while (iter.hasNext()) {
            InnerPosition pos = (InnerPosition)iter.next();
            Protein protein = new Protein(aDBloader.toFASTAString(aRawEntry, false));
            // If the stop position was '-1', we should consider the stop position to be the protein length.
            if(pos.getStop() == -1) {
                pos.setStop(protein.getSequence().getLength());
            }
            // If the start position is equal to the length of the whole sequence String,
            // we are in SIGNAL_PROPEP_MODE, and we have C-terminal truncation. Which is
            // currently ignored.
            if(pos.getStart() == protein.getSequence().getLength()) {
                // Ignore and continue.
                continue;
            }
            protein.getHeader().setLocation(pos.getStart()+1, pos.getStop());
            String sequence = protein.getSequence().getSequence();
            sequence = sequence.substring(pos.getStart(), pos.getStop());
            protein.getSequence().setSequence(sequence);
            protein.writeToFASTAFile(aPW);
            outputCounter++;
        }
        return new int[]{outputCounter, originalOutputCounter};
    }

    /**
     * Method that writes out the desired output of (processed) proteins, and returns the number of
     * processed and original entries written.
     *
     * @param aIncludeOriginal  boolean to indicate inclusion of original sequence.
     * @param aPositions    ArrayLists with the InnerPosition objects for this protein.
     * @param aDBloader SwissProtLoader with the DBLoader to the database.
     * @param aRawEntry String with the raw entry.
     * @param aPW   PrintWriter to write th output to.
     * @return  int[]   with as '0' element the number of processed entries written,
     *                  and '1' element the number of original entries written.
     * @throws IOException  if the processing or writing field.
     */
    private static int[] outputExclMode(boolean aIncludeOriginal, ArrayList aPositions, SwissProtLoader aDBloader, String aRawEntry, PrintWriter aPW) throws IOException {
        int originalOutputCounter = 0;
        int outputCounter = 0;

        // Find the furthest processed protein.
        Iterator iter = aPositions.iterator();
        InnerPosition maxPosition = null;
        Protein protein = new Protein(aDBloader.toFASTAString(aRawEntry, false));
        // Cycle all positions.
        while (iter.hasNext()) {
            InnerPosition pos = (InnerPosition)iter.next();
            // If the stop position was '-1', we should consider the stop position to be the protein length.
            if(pos.getStop() == -1) {
                pos.setStop(protein.getSequence().getLength());
            }
            // If the start position is equal to the length of the whole sequence String,
            // we are in SIGNAL_PROPEP_MODE, and we have C-terminal truncation. Which is
            // currently ignored.
            if(pos.getStart() == protein.getSequence().getLength()) {
                // Ignore and continue.
                continue;
            }
            // Check whether we have the largest stop position?
            if(maxPosition == null || maxPosition.getStop() < pos.getStop()) {
                maxPosition = pos;
            }

        }
        // See if we have a position, if so, use it. If not, include original protein.
        if(maxPosition != null) {
            protein = new Protein(aDBloader.toFASTAString(aRawEntry, false));
            protein.getHeader().setLocation(maxPosition.getStart()+1, maxPosition.getStop());
            String sequence = protein.getSequence().getSequence();
            sequence = sequence.substring(maxPosition.getStart(), maxPosition.getStop());
            protein.getSequence().setSequence(sequence);
            protein.writeToFASTAFile(aPW);
            outputCounter++;
        } else {
            protein.writeToFASTAFile(aPW);
            originalOutputCounter++;
        }

        return new int[]{outputCounter, originalOutputCounter};
    }

    /**
     * This method prints the usage information for this class to the standard
     * error stream and exits with the error flag raised to '1'.
     */
    private static void printUsage() {
        printError("Usage:\n\n\tProteinMaturationDevice [--filterSet \"<filter1_name=filter1_param;filter2_name;filter3_name=filter3_param;...>\"]" +
                                                       " --mode <CHAIN|PROPEP|EXCL_PROPEP>" +
                                                       " [-c]" +
                                                       " [-m]" +
                                                       " <swissprot_formatted_input_database>" +
                                                       " <output_file>" +
                   "\n\n\t * Three modes are available:\n\t\tCHAIN  -- which will resolve all annotated chains for each protein\n\n\t\tPROPEP -- which only resolves SIGNAL, TRANSIT and PROPEP features for each protein\n\n\t\tEXCL_PROPEP -- which resolves only the fully mature version of each protein, which is the original protein if no N-terminal processing is indicated. NOTE: the use of the '-c' option will be ignored in this mode!\n\n\t * The optional '-c' flag will prompt the software to copy in the original entries in the output file, along with the mature versions.\n\n\t * The optional '-m' flag will remove the initiator methiones from all non-truncated proteins.\n\n\tNote that existing output files will be silently overwritten!");
    }

    /**
     * This method prints two blank lines followed by the the specified error message and another two empty lines
     * to the standard error stream and exits with the error flag raised to '1'.
     *
     * @param aMsg String with the message to print.
     */
    private static void printError(String aMsg) {
        System.err.println("\n\n" + aMsg + "\n\n");
        System.exit(1);
    }


    /**
     * Convenience inner class to hold a chain start and stop position.
     */
    private static class InnerPosition {
        private int start = -1;
        private int stop = -1;

        public InnerPosition(int aStart, int aStop) {
            start = aStart;
            stop = aStop;
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setStop(int stop) {
            this.stop = stop;
        }
    }
}