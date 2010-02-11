/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-aug-2003
 * Time: 13:35:23
 */
package com.compomics.dbtoolkit.io;

import com.compomics.dbtoolkit.toolkit.EnzymeDigest;
import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.protein.Enzyme;

import java.io.IOException;
import java.io.InputStream;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2008/11/18 16:00:55 $
 */

/**
 * This class allows the caller to load an Enzyme from a name and an optional number of mis cleavages.
 *
 * @author Lennart Martens
 */
public class EnzymeLoader {

    /**
     * This method loads the specified enzyme and optionally sets the desired number of allowed
     * missed cleavages.
     *
     * @param aName String with the enzyme name.
     * @param aMisCleavages String with the optional number of allowed missed cleavages (can be 'null').
     * @return  Enzyme with the desired enzyme.
     * @throws IOException  when the loading of the enzyme failed.
     */
    public static Enzyme loadEnzyme(String aName, String aMisCleavages) throws IOException {
        Enzyme enzyme = null;
        if(aName != null) {
            InputStream in = EnzymeDigest.class.getClassLoader().getResourceAsStream("enzymes.txt");
            if(in != null) {
                MascotEnzymeReader mer = new MascotEnzymeReader(in);
                enzyme = mer.getEnzyme(aName);
                if(enzyme == null) {
                    throw new IOException("The enzyme you specified (" + aName + ") was not found in the Mascot Enzymefile '" + EnzymeDigest.class.getClassLoader().getResource("enzymes.txt") + "'!");
                } else {
                    if(aMisCleavages != null) {
                        try {
                            int i = Integer.parseInt(aMisCleavages);
                            if(i < 0) {
                                throw new IOException("");
                            }
                            enzyme.setMiscleavages(i);
                        } catch(Exception e) {
                            throw new IOException("The number of allowed missed cleavages must be a positive whole number!");
                        }
                    }
                }
            } else {
                throw new IOException("File 'enzymes.txt' not found in current classpath!");
            }
        }
        return enzyme;
    }
}
