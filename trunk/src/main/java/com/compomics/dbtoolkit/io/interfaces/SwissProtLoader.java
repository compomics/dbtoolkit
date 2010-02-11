/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 11-Jul-2007
 * Time: 15:13:22
 */
package com.compomics.dbtoolkit.io.interfaces;

import java.util.HashMap;
import java.io.IOException;
/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2007/07/11 17:34:12 $
 */

/**
 * This interface describes the behaviour for
 *
 * @author martlenn
 * @version $Id: SwissProtLoader.java,v 1.1 2007/07/11 17:34:12 lennart Exp $
 */
public interface SwissProtLoader extends DBLoader {
    /**
     * This method parses the raw SwissProt entry into a HashMap in which each element is represented.
     * It is not the objective of this method to generate context information, rather it simply isolates
     * the data contained in the raw format per accession code for easier retrieval later.
     *
     * @param   aRaw    String  with the raw format. This method requires line seperators as
     *                          in the original flatfile!
     * @return  HashMap with the code as key and the data contained behind the code as value.
     * @exception java.io.IOException when String reading goes wrong. This is normally not to be expected.
     */
    public abstract HashMap processRawData(String aRaw) throws IOException;

    /**
     * This method converts a SwissProt raw String into a FASTA format String.
     *
     * @param   aRaw    String with the raw SwissProt entry.
     * @param   aEndLines   boolean that indicates whether each sequence line should be clipped at 60 characters.
     * @return  String  with the FASTA version of the argument String.
     * @exception   java.io.IOException when the conversion caused problems (not likely to occur).
     */
    public abstract String toFASTAString(String aRaw, boolean aEndLines) throws IOException;
}
