/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 23-jan-03
 * Time: 9:18:29
 */
package com.compomics.dbtoolkit.general;

import com.compomics.dbtoolkit.io.FilterLoader;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.toolkit.EnzymeDigest;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class will emulate the No Enzyme kind of ragging performed by mascot,
 * yet restricts the ragging to 'seed cores' with a specific sequence.
 *
 * @author Lennart Martens
 */
public class NoEnzymeSimulator {

    /**
     * This STring holds the center used for No Enzyme like ragging.
     */
    private String iCenter = null;

    /**
     * This constructor takes the center for the No Enzyme ragging.
     * In each generated sequence, the center will be present. A single
     * entry can contain multiple centers.
     *
     * @param   aCenter String with the center for the No Enzyme ragging.
     *                  This String will be uppercased.
     */
    public NoEnzymeSimulator(String aCenter) {
        this.iCenter = aCenter.toUpperCase();
    }

    /**
     * This method reports on the center currently used.
     *
     * @return  String  with the center that is currently used.
     */
    public String getCenter() {
        return this.iCenter;
    }

    /**
     * This method will perform the actual No Enzyme like ragging
     * on the specified protein sequence.
     *
     * @param   aProtein    Protein to rag No Enzyme like.
     * @return  Protein[]   with the resulting (non-redundant!) sequences.
     */
    public Protein[] performRagging(Protein aProtein) {
        return this.performRagging(aProtein, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    /**
     * This method will perform the actual No Enzyme like ragging
     * on the specified protein sequence. The Enzyme specified will be used
     * to check on the FE, CE, NE or EE kind of cleavage.
     *
     * @param   aProtein    Protein to rag No Enzyme like.
     * @param   aEnzyme enzyme to compare cleavage sites to.
     * @return  Protein[]   with the resulting (non-redundant!) sequences.
     */
    public Protein[] performRagging(Protein aProtein, Enzyme aEnzyme) {
        return this.performRagging(aProtein, aEnzyme, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    /**
     * This method will perform the actual No Enzyme like ragging
     * on the specified protein sequence, applying the specified
     * mass limits to each generated peptide. The Enzyme specified will be used
     * to check on the FE, CE, NE or EE kind of cleavage.
     *
     * @param   aProtein    Protein to rag No Enzyme like.
     * @param   aEnzyme enzyme to compare cleavage sites to.
     * @param   aLower  double with the lower mass limit.
     * @param   aUpper  double with the upper mass limit.
     * @return  Protein[]   with the resulting (non-redundant!) sequences.
     */
    public Protein[] performRagging(Protein aProtein, Enzyme aEnzyme, double aLower, double aUpper) {
        Protein[] result = null;

        // Outer loop is: cycle all occurrences of the center.
        String sequence = aProtein.getSequence().getSequence();
        int location = -1;
        HashMap all = new HashMap();
        int starting = -1;
        while((location = sequence.indexOf(iCenter, starting)) >= 0) {
            starting = location+1;
            // First handle all possible N-terminal sequences.
            this.handleNterm(aProtein, all, aEnzyme, location, aLower, aUpper);

            // Next handle all possible C-terminal sequences.
            this.handleCterm(aProtein, all, aEnzyme, location, aLower, aUpper);

            // Next handle all N-terminal substrings.
            this.handleNtermToCterm(aProtein, all, aEnzyme, location, aLower, aUpper);

            // NOTE!
            //
            // handleCtermToNterm yields THE SAME results as handleNtermToCterm.
            // That's why it has been commented out.
            //
            // Next handle all C-terminal substrings.
            // this.handleCtermToNterm(aProtein, all, aEnzyme, location, aLower, aUpper);
        }

        // Now to put all the results into our result array.
        result = new Protein[all.values().size()];
        Iterator iter = all.values().iterator();
        int counter = 0;
        while(iter.hasNext()) {
            result[counter] = (Protein)iter.next();
            counter++;
        }
        // Voila.
        return result;
    }

    /**
     * This method will perform the actual No Enzyme like ragging
     * on the specified protein sequence, applying the specified
     * mass limits to each generated peptide.
     *
     * @param   aProtein    Protein to rag No Enzyme like.
     * @param   aLower  double with the lower mass limit.
     * @param   aUpper  double with the upper mass limit.
     * @return  Protein[]   with the resulting (non-redundant!) sequences.
     */
    public Protein[] performRagging(Protein aProtein, double aLower, double aUpper) {
        return this.performRagging(aProtein, null, aLower, aUpper);
    }

    /**
     * This method specifically generates all possible sequences in which
     * the center is the C-terminus.
     *
     * @param   aProtein    the Protein instance to rag.
     * @param   aStore  the HashMap to store the ragged parts in (pass-by-reference filling!).
     * @param   aEnzyme Enzyme to compare the cleavage to (can be 'null' for no comparison).
     * @param   aLocation   int with the location of the center within the sequence.
     * @param   aLower  double with the lower mass limit.
     * @param   aUpper  double with the upper mass limit.
     */
    private void handleNterm(Protein aProtein, HashMap aStore, Enzyme aEnzyme, int aLocation, double aLower, double aUpper) {
        String sequence = aProtein.getSequence().getSequence();

        // The end location is the C-terminal residue of the center.
        int end = aLocation + iCenter.length();

        // The start location is the N-terminal residue of the center.
        int start = aLocation;

        boolean lContinue = true;
        int counter = -1;
        while(lContinue) {
            counter++;
            // The dynamic starting position.
            int newStart = start-counter;
            if(newStart < 0) {
                lContinue = false;
                continue;
            }
            AASequenceImpl seq = new AASequenceImpl(sequence.substring(newStart, end));
            double tempMass = seq.getMass();
            if(tempMass < aLower) {
                continue;
            } else if(tempMass > aUpper) {
                lContinue = false;
                continue;
            } else {
                // OK, this sequence is a keeper.
                // Check whether it is already present, in which case
                // we'll just continue.
                if(aStore.containsKey(seq.getSequence())) {
                    continue;
                } else {
                    // Not yet present, so add.
                    // First the header. We add location and a flag to see if it
                    // is FT, HT or NT.
                    Header head = (Header)aProtein.getHeader().clone();
                    // Locations are human readable.
                    head.setLocation(newStart+1, end);
                    // Do something about enzymatic nature.
                    if(aEnzyme != null) {
                        // Enzyme has been set, so check it.
                        this.annotateHeader(head, sequence, seq.getSequence(), aEnzyme);
                    }

                    // Store cleaved peptide as protein in hash with sequence as key.
                    aStore.put(seq.getSequence(), new Protein(head, seq));
                }
            }
        }
    }

    /**
     * This method specifically generates all possible sequences in which
     * the N-terminus is the center.
     *
     * @param   aProtein    the Protein instance to rag.
     * @param   aStore  the HashMap to store the ragged parts in (pass-by-reference filling!).
     * @param   aEnzyme Enzyme to compare the cleavage to (can be 'null' for no comparison).
     * @param   aLocation   int with the location of the center within the sequence.
     * @param   aLower  double with the lower mass limit.
     * @param   aUpper  double with the upper mass limit.
     */
    private void handleCterm(Protein aProtein, HashMap aStore, Enzyme aEnzyme, int aLocation, double aLower, double aUpper) {
        String sequence = aProtein.getSequence().getSequence();

        // The end location is the C-terminal residue of the center.
        int end = aLocation + iCenter.length();

        // The start location is the N-terminal residue of the center.
        int start = aLocation;

        boolean lContinue = true;
        int counter = -1;
        while(lContinue) {
            counter++;
            // The dynamic ending position.
            int newEnd = end+counter;
            if(newEnd > sequence.length()) {
                lContinue = false;
                continue;
            }
            AASequenceImpl seq = new AASequenceImpl(sequence.substring(start, newEnd));

            double tempMass = seq.getMass();
            if(tempMass < aLower) {
                continue;
            } else if(tempMass > aUpper) {
                lContinue = false;
                continue;
            } else {
                // OK, this sequence is a keeper.
                // Check whether it is already present, in which case
                // we'll just continue.
                if(aStore.containsKey(seq.getSequence())) {
                    continue;
                } else {
                    // Not yet present, so add.
                    // First the header. We add location and a flag to see if it
                    // is FT, HT or NT.
                    Header head = (Header)aProtein.getHeader().clone();
                    // Locations are human readable.
                    head.setLocation(start+1, newEnd);
                    // Do something about enzymatic nature.
                    if(aEnzyme != null) {
                        // Enzyme has been set, so check it.
                        this.annotateHeader(head, sequence, seq.getSequence(), aEnzyme);
                    }

                    // Store cleaved peptide as protein in hash with sequence as key.
                    aStore.put(seq.getSequence(), new Protein(head, seq));
                }
            }
        }
    }

    /**
     * This method specifically generates all possible sequences in which
     * the N-terminus is cycled while for each cycle all possible C-terminal additions are
     * considered.
     * The results of this method are identical to handleNtermToCterm.
     * Yet only this one is called. The other remains as an artifact.
     *
     * @param   aProtein    the Protein instance to rag.
     * @param   aStore  the HashMap to store the ragged parts in (pass-by-reference filling!).
     * @param   aEnzyme Enzyme to compare the cleavage to (can be 'null' for no comparison).
     * @param   aLocation   int with the location of the center within the sequence.
     * @param   aLower  double with the lower mass limit.
     * @param   aUpper  double with the upper mass limit.
     */
    private void handleNtermToCterm(Protein aProtein, HashMap aStore, Enzyme aEnzyme, int aLocation, double aLower, double aUpper) {
        String sequence = aProtein.getSequence().getSequence();

        // Starting location is N-terminus of the center.
        int start = aLocation;
        // End location is the C-terminus of the center.
        int end = aLocation + iCenter.length();

        // Now we need to step back on the N-terminus each time and
        // for each step back, check all possible steps towards the C-terminus.
        for(int i=0;(start-i)>=0;i++) {
            int tempStart = start-i;
            for(int j=0;(j+end)<=sequence.length();j++) {
                int tempEnd = end + j;
                // The temp sequence to consider is the substring
                // from tempStart to tempEnd.
                AASequenceImpl tempSeq = new AASequenceImpl(sequence.substring(tempStart, tempEnd));
                double tempMass = tempSeq.getMass();

                // If the mass is too low, just continue.
                if(tempMass < aLower) {
                    continue;
                } else if(tempMass > aUpper) {
                    // If the mass is too large, break inner loop.
                    // (It's no use to add more C-terminal residues!)
                    break;
                } else {
                    // See if the particular sequence is already there.
                    if(aStore.containsKey(tempSeq.getSequence())) {
                        // We've already got it. Continue.
                        continue;
                    } else {
                        // Not yet present, so add.
                        // First the header. We add location and a flag to see if it
                        // is FT, HT or NT.
                        Header head = (Header)aProtein.getHeader().clone();
                        // Locations are human readable.
                        head.setLocation(tempStart+1, tempEnd);
                        // Do something about enzymatic nature.
                        if(aEnzyme != null) {
                            // Enzyme has been set, so check it.
                            this.annotateHeader(head, sequence, tempSeq.getSequence(), aEnzyme);
                        }

                        // Store cleaved peptide as protein in hash with sequence as key.
                        aStore.put(tempSeq.getSequence(), new Protein(head, tempSeq));
                    }
                }
            }
        }
    }

    /**
     * This method specifically generates all possible sequences in which
     * the C-terminus is cycled while for each cycle all possible N-terminal additions are
     * considered.
     * The results of this method are identical to handleNtermToCterm.
     * This method is not called. Yet it remains as an artifact.
     *
     * @param   aProtein    the Protein instance to rag.
     * @param   aStore  the HashMap to store the ragged parts in (pass-by-reference filling!).
     * @param   aEnzyme Enzyme to compare the cleavage to (can be 'null' for no comparison).
     * @param   aLocation   int with the location of the center within the sequence.
     * @param   aLower  double with the lower mass limit.
     * @param   aUpper  double with the upper mass limit.
     */
    private void handleCtermToNterm(Protein aProtein, HashMap aStore, Enzyme aEnzyme, int aLocation, double aLower, double aUpper) {
        String sequence = aProtein.getSequence().getSequence();

        // Starting location is N-terminus of the center.
        int start = aLocation;
        // End location is the C-terminus of the center.
        int end = aLocation + iCenter.length();

        // Now we need to step back on the C-terminus each time and
        // for each step back, check all possible steps towards the N-terminus.
        for(int i=0;(end+i)<=sequence.length();i++) {
            int tempEnd = end + i;
            for(int j=0;(start-j) >=0;j++) {
                int tempStart = start - j;
                // The temp sequence to consider is the substring
                // from tempStart to tempEnd.
                AASequenceImpl tempSeq = new AASequenceImpl(sequence.substring(tempStart, tempEnd));
                double tempMass = tempSeq.getMass();

                // If the mass is too low, just continue.
                if(tempMass < aLower) {
                    continue;
                } else if(tempMass > aUpper) {
                    // If the mass is too large, break inner loop.
                    // (It's no use to add more C-terminal residues!)
                    break;
                } else {
                    // See if the particular sequence is already there.
                    if(aStore.containsKey(tempSeq.getSequence())) {
                        // We've already got it. Continue.
                        continue;
                    } else {
                        // Not yet present, so add.
                        // First the header. We add location and a flag to see if it
                        // is FT, HT or NT.
                        Header head = (Header)aProtein.getHeader().clone();
                        // Locations are human readable.
                        head.setLocation(tempStart+1, tempEnd);
                        // Do something about enzymatic nature.
                        if(aEnzyme != null) {
                            // Enzyme has been set, so check it.
                            this.annotateHeader(head, sequence, tempSeq.getSequence(), aEnzyme);
                        }

                        // Store cleaved peptide as protein in hash with sequence as key.
                        aStore.put(tempSeq.getSequence(), new Protein(head, tempSeq));
                    }
                }
            }
        }
    }


    /**
     * This method will annotate a header with the information about whether or
     * not the subsequence is an enzymatic cleavageproduct of the parent sequence.
     *
     * @param   aHeader Header that has to be annotated (used as a reference param, btw!)
     * @param   aParentSeq  String with the parent sequence.
     * @param   aSubSeq String with the subsequence to consider.
     * @param   aEnzyme Enzyme to verify cleavage with.
     */
    private void annotateHeader(Header aHeader, String aParentSeq, String aSubSeq, Enzyme aEnzyme) {
        int cleavage = aEnzyme.isEnzymaticProduct(aParentSeq, aSubSeq);
        String descr = aHeader.getDescription();

        switch(cleavage) {
            case Enzyme.ENTIRELY_NOT_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*EE*) " + descr);
                } else {
                    aHeader.setRest("(*EE*) " + aHeader.getRest());
                }
                break;
            case Enzyme.N_TERM_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*NE*) " + descr);
                } else {
                    aHeader.setRest("(*NE*) " + aHeader.getRest());
                }
                break;
            case Enzyme.C_TERM_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*CE*) " + descr);
                } else {
                    aHeader.setRest("(*CE*) " + aHeader.getRest());
                }
                break;
            case Enzyme.FULLY_ENZYMATIC:
                if(descr != null) {
                    aHeader.setDescription("(*FE*) " + descr);
                } else {
                    aHeader.setRest("(*FE*) " + aHeader.getRest());
                }
                break;
        }
    }

    /**
     * The main method runs this application.
     * Start-up parameters can be the following:
     *
     * @param   args    String[] with the following parameters: <br />
     *                   - lowMass (optional): lower mass limit. <br />
     *                   - highMass (optional): upper mass limit. <br />
     *                   - enzyme (optional): name of the enzyme to use. <br />
     *                   - sequence (<b>required</b>): sequence String to center on. <br />
     *                   - and the input database name.
     */
    public static void main(String[] args) {

        try {
            if(args == null || args.length == 0) {
                printUsage();
            }
            CommandLineParser clp = new CommandLineParser(args, new String[]{"lowMass", "highMass", "enzyme", "sequence", "filter", "filterParam"});

            String[] temp = clp.getParameters();
            if(temp == null || temp.length == 0) {
                System.err.println("\n\nInput DB was NOT specified!");
                printUsage();
            }
            String inputDB = temp[0];
            if(inputDB == null) {
                System.err.println("\n\nInput DB was NOT specified!");
                printUsage();
            }
            String sequence = clp.getOptionParameter("sequence");
            if(sequence == null) {
                System.err.println("\n\nSequence was NOT specified!");
                printUsage();
            }
            String lowMass = clp.getOptionParameter("lowMass");
            double low = -1.0;
            if(lowMass != null) {
                try {
                    low = Double.parseDouble(lowMass);
                } catch(Exception e) {
                    System.err.println("\n\nLower mass must be a positive decimal number!");
                    printUsage();
                }
            }
            String highMass = clp.getOptionParameter("highMass");
            double high = -1.0;
            if(highMass != null) {
                try {
                    high = Double.parseDouble(highMass);
                } catch(Exception e) {
                    System.err.println("\n\nUpper mass must be a positive decimal number!");
                    printUsage();
                }
            }
            String enz = clp.getOptionParameter("enzyme");
            Enzyme enzyme = null;
            if(enz != null) {
                try {
                    InputStream in = EnzymeDigest.class.getClassLoader().getResourceAsStream("enzymes.txt");
                    if(in != null) {
                        MascotEnzymeReader mer = new MascotEnzymeReader(in);
                        enzyme = mer.getEnzyme(enz);
                            if(enzyme == null) {
                                System.err.println("The enzyme you specified (" + enz + ") was not found in the Mascot Enzymefile '" + EnzymeDigest.class.getClassLoader().getResource("enzymes.txt") + "'!");
                            }
                    } else {
                        throw new IOException("File 'enzymes.txt' not found in current classpath!");
                    }
                } catch(IOException ioe) {
                    System.err.println("You specified enzyme '" + enz + "' for cleavage, but the Mascot Enzyme file was not found: " + ioe.getMessage());
                }
            }

            // Database loading.
            DBLoader loader = new AutoDBLoader(new String[]{"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedFASTADBLoader", "com.compomics.dbtoolkit.io.implementations.ZippedSwissProtDBLoader"}).getLoaderForFile(inputDB);
            if(loader == null) {
                System.err.println("\n\nUnable to find loader for database '" + inputDB + "'!\nExiting.\n\n");
                System.exit(1);
            }

            // See if we need to apply a filter.
            String filterName = clp.getOptionParameter("filter");
            Filter filter = null;
            if(filterName != null) {
                filter = FilterLoader.loadFilter(filterName, clp.getOptionParameter("filterParam"), loader);
            }


            NoEnzymeSimulator nes = new NoEnzymeSimulator(sequence);
            Protein current = null;
            if(filter == null) {
                current = loader.nextProtein();
            } else {
                current = loader.nextFilteredProtein(filter);
            }
            while(current != null) {

                Protein[] result = null;

                if((enzyme != null)) {
                    if((low > 0) || (high > 0)) {
                        if(low < 0) {
                            low = 0;
                        }
                        if(high < 0) {
                            high = Double.MAX_VALUE;
                        }
                        result = nes.performRagging(current, enzyme, low, high);
                    } else {
                        result = nes.performRagging(current, enzyme);
                    }
                } else if((enzyme == null) && ((low > 0) || (high > 0))) {
                    if(low < 0) {
                        low = 0;
                    }
                    if(high < 0) {
                        high = Double.MAX_VALUE;
                    }
                    result = nes.performRagging(current, low, high);
                } else {
                    result = nes.performRagging(current);
                }
                for(int i = 0; i < result.length; i++) {
                    Protein lProtein = result[i];
                    System.out.print(lProtein.getHeader().getFullHeaderWithAddenda() + "\n" + lProtein.getSequence().getSequence() + "\n");
                }
                // Read next entry.
                if(filter == null) {
                    current = loader.nextProtein();
                } else {
                    current = loader.nextFilteredProtein(filter);
                }
            }
        } catch(IOException ioe) {
            System.err.println("\n\n" + ioe.getMessage() + "\n");
            ioe.printStackTrace();
        } catch(UnknownDBFormatException udfe) {
            System.err.println("\n\n" + udfe.getMessage() + "!\nExiting.\n\n");
                System.exit(1);
        }
    }

    /**
     * This method prints usage information to stdout +
     * exits the JVM.
     */
    private static void printUsage() {
        System.err.println("\n\nUsage:\n\tNoEnzymeSimulator [--lowMass <lower_mass_limit> --highMass <upper_mass_limit> --enzyme <enzyme_name> --filter <filter_name> [--filterParam <filter_parameter>]] --sequence <center_sequence> <input_DB>\n\n");
        System.exit(1);
    }
}
