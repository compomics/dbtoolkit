/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-okt-02
 * Time: 13:45:57
 */
package com.compomics.dbtoolkit;

import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.util.general.CommandLineParser;
import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class allows the caller to generate two seperate output files:
 *  - One with the length distribution for the database 
 *  - one with the mass distribution for the database.
 *
 * @author Lennart Martens
 */
public class CalculateDistributions {

    /**
     * The file to read the input from.
     */
    private String iInputFile = null;

    /**
     * The length interval to use in the distribution calculations.
     */
    private int iLengthInterval = 0;

    /**
     * The mass interval to use in the distribution calculations.
     */
    private double iMassInterval = 0.0;

    /**
     * The (optional enzyme) to digest the input db with before
     * calculating distributions.
     */
    private Enzyme iEnzyme = null;


    /**
     * Constructor that takes an input file, a length interval and a mass
     * interval for the calculations.
     *
     * @param   aInputFile  String with the filename to read the database from.
     * @param   aLengthInterval int with the length interval to use in the length distribution.
     * @param   aMassInterval   double with the mass interval to use in the mass distribution.
     */
    public CalculateDistributions(String aInputFile, int aLengthInterval, double aMassInterval) {
        this(aInputFile, aLengthInterval, aMassInterval, null);
    }

    /**
     * Constructor that takes an input file, a length interval and a mass
     * interval for the calculations, as well as an enzyme to digest the DB entries with.
     * Calculations are performed on the digest.
     *
     * @param   aInputFile  String with the filename to read the database from.
     * @param   aLengthInterval int with the length interval to use in the length distribution.
     * @param   aMassInterval   double with the mass interval to use in the mass distribution.
     * @param   aEnzyme Enzyme to digest the input DB with.
     */
    public CalculateDistributions(String aInputFile, int aLengthInterval, double aMassInterval, Enzyme aEnzyme) {
        this.iInputFile = aInputFile;
        this.iLengthInterval = aLengthInterval;
        this.iMassInterval = aMassInterval;
        this.iEnzyme = aEnzyme;
    }

    /**
     * This method calculates and outputs each distribution for this database to file.
     * The names of these files are simply "lengthDistrib_" + filename_of_db + ".csv" and
     * "massDistrib_" + filename_of_db + ".csv" respectively.
     */
    public void calculateDistribution() throws IOException, UnknownDBFormatException {
        // Create the outputfiles' objects.
        File input = new File(this.iInputFile);
        String parent = input.getParent();
        String core = input.getName();
        core = core.substring(0, core.lastIndexOf('.'));

        File outputMass = new File(parent + "/massDistrib_" + core + ".csv");
        File outputLength = new File(parent + "/lengthDistrib_" + core + ".csv");

        // We no longer need the File instance for the input.
        input = null;

        // Get an appropriate DBLoader implementation.
        DBLoader loader = new AutoDBLoader(new String[]{"com.compomics.dbtoolkit.io.implementations.FASTADBLoader", "com.compomics.dbtoolkit.io.implementations.SwissProtDBLoader"}).getLoaderForFile(iInputFile);

        // Get streamhandles to the outputfiles
        // + init them.
        PrintWriter lengthWriter = new PrintWriter(new FileWriter(outputLength));
        PrintWriter massWriter = new PrintWriter(new FileWriter(outputMass));
        lengthWriter.print(";# Residues;Count;(interval:" + iLengthInterval + " residues)\n");
        massWriter.print(";Mass (Da);Count;(interval:" + iMassInterval + " Da)\n");

        // HashMaps to hold the distribution.
        HashMap massMap = new HashMap();
        HashMap lengthMap = new HashMap();

        // Okay, cycle each entry to gather the data.
        Protein protein = null;
        while((protein = loader.nextProtein()) != null) {
            Protein[] interMed = null;
            // See if we should cleave.
            if(iEnzyme != null) {
                // Okay, cleave.
                interMed = iEnzyme.cleave(protein);
            } else {
                // No cleaving. Array will only contain the entry.
                interMed = new Protein[]{protein};
            }
            for(int i = 0; i < interMed.length; i++) {
                Protein lProtein = interMed[i];
                this.getData(lProtein, lengthMap, massMap);
            }
        }
        loader.close();
        loader = null;

        // Okay, format and output the data.
        this.outputLengthData(lengthWriter, lengthMap);
        this.outputMassData(massWriter, massMap);

        lengthWriter.flush();
        lengthWriter.close();
        massWriter.flush();
        massWriter.close();
    }

    /**
     * This method will output the length data.
     *
     * @param   aOut    PrintWriter to write the output to.
     * @param   aData   HashMap to retrieve the data from.
     */
    private void outputLengthData(PrintWriter aOut, HashMap aData) {
        // First of all, sort the keys (low to high).
        Set keys = aData.keySet();
        Long[] allKeys = new Long[keys.size()];
        keys.toArray(allKeys);

        Arrays.sort(allKeys, new Comparator() {
            public int compare(Object o1, Object o2) {
                long l1 = ((Long)o1).longValue();
                long l2 = ((Long)o2).longValue();
                return (int)(l1-l2);
            }
        });

        for(int i = 0; i < allKeys.length; i++) {
            Long lKey = allKeys[i];
            aOut.print(";" + (lKey.longValue()*iLengthInterval) + ";" + aData.get(lKey) + "\n");
        }
    }

    /**
     * This method will output the mass data.
     *
     * @param   aOut    PrintWriter to write the output to.
     * @param   aData   HashMap to retrieve the data from.
     */
    private void outputMassData(PrintWriter aOut, HashMap aData) {
        // First of all, sort the keys (low to high).
        Set keys = aData.keySet();
        Long[] allKeys = new Long[keys.size()];
        keys.toArray(allKeys);

        Arrays.sort(allKeys, new Comparator() {
            public int compare(Object o1, Object o2) {
                long l1 = ((Long)o1).longValue();
                long l2 = ((Long)o2).longValue();
                return (int)(l1-l2);
            }
        });

        for(int i = 0; i < allKeys.length; i++) {
            Long lKey = allKeys[i];
            aOut.print(";" + (lKey.longValue()*iMassInterval) + ";" + aData.get(lKey) + "\n");
        }
    }

    /**
     * This method extracts data from the specified protein instance and fills the
     * specified maps with length and mass distribution information.
     *
     * @param   aProtein    Protein to get the statistics from.
     * @param   aLengthMap  HashMap to store length distribution information in.
     * @param   aMassMap    HashMap to store mass distribution information in.
     */
    private void getData(Protein aProtein, HashMap aLengthMap, HashMap aMassMap) {
        // Get the data.
        long length = aProtein.getLength();
        double mass = aProtein.getMass();

        // Calculate the interval position for each.
        Long lengthPos = new Long(length / iLengthInterval);
        Long massPos = new Long((long)(mass / iMassInterval));

        // See if we have the key already.
        // If we do, augment value by one,
        // if we don't insert it and set value to one.
        Long lengthCount = (Long)aLengthMap.get(lengthPos);
        Long massCount = (Long)aMassMap.get(massPos);
        if(lengthCount != null) {
            lengthCount = new Long(lengthCount.longValue() + 1);
        } else {
            lengthCount = new Long(1l);
        }
        if(massCount != null) {
            massCount = new Long(massCount.longValue() + 1);
        } else {
            massCount = new Long(1l);
        }
        // Store.
        aLengthMap.put(lengthPos, lengthCount);
        aMassMap.put(massPos, massCount);

        // That's it.
        return;
    }

    public static void main(String[] args) {
        if(args == null || args.length==0) {
            System.err.println("\n\nUsage:\n\tCalculateDistributions [--length <length_interval>] [--mass <mass_interval>] [--enzyme <enzyme_name> [--mc <miscleavagecount>]] <inputFile>\n");
            System.exit(1);
        }

        CommandLineParser clp = new CommandLineParser(args, new String[]{"length", "mass", "enzyme", "mc"});
        String[] params = clp.getParameters();
        String length = clp.getOptionParameter("length");
        if(length == null) {
            length = "1";
        }
        String mass = clp.getOptionParameter("mass");
        if(mass == null) {
            mass = "0.6";
        }
        String enz = clp.getOptionParameter("enzyme");
        String mc = clp.getOptionParameter("mc");
        if(mc == null) {
            mc = "1";
        }

        try {
            Enzyme enzyme = null;
            if(enz != null) {
                MascotEnzymeReader mer = new MascotEnzymeReader(CalculateDistributions.class.getClassLoader().getResourceAsStream("enzymes.txt"));
                enzyme = mer.getEnzyme(enz);
                enzyme.setMiscleavages(Integer.parseInt(mc));
                System.out.println(enzyme.toString());
            }
            System.out.println("Length interval: " + length);
            System.out.println("Mass interval: " + mass);
            System.out.println("Input file: '" + params[0] + "'.");
            CalculateDistributions cd = new CalculateDistributions(params[0], Integer.parseInt(length), Double.parseDouble(mass), enzyme);
            cd.calculateDistribution();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
