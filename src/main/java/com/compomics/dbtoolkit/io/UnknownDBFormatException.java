/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 14:07:15
 */
package com.compomics.dbtoolkit.io;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class represents the exception that occurs when a DB ID has been presented that is not known as
 * a public final static int on the DBConverter class.
 *
 * @author Lennart Martens
 */
public class UnknownDBFormatException extends Exception {

    /**
     * The unknown DB.
     */
    private String iDBid = null;

    /**
     * Constructor that allows the setting of a DB identifier. This constructor will automatically
     * derive a meaningful message based on this number.
     *
     * @param   aDBid    String with the unknown database identifier.
     */
    public UnknownDBFormatException(String aDBid) {
        super("DB format for '" + aDBid + "' is not known! Consult the available formats on the DBConverter class!");
        this.iDBid = aDBid;
    }

    /**
     * Constructor used by the AutoDBLoader class to specify an unknown
     * or empty file format.
     *
     * @param   aMessage    String with the message to communicate.
     * @param   aFilename   String with the filename for the unknown file.
     */
    public UnknownDBFormatException(String aMessage, String aFilename) {
        super(aMessage + " (filename: '" + aFilename + "').");
    }

    /**
     * This method reports on the unknown DB.
     *
     * @return  String with the name of the database.
     */
    public String getDBName() {
        return this.iDBid;
    }
}
