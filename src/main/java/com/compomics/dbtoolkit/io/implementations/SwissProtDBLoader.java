/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 25-sep-02
 * Time: 14:12:15
 */
package com.compomics.dbtoolkit.io.implementations;

import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.interfaces.SwissProtLoader;
import com.compomics.util.io.PushBackStringReader;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.6 $
 * $Date: 2008/09/08 16:14:04 $
 */

/**
 * This class implements the DBLoader interface for the SwissProt format.
 *
 * @author Lennart
 */
public class SwissProtDBLoader extends DefaultDBLoader implements SwissProtLoader {

    /**
     * This constant defines the marker that starts a subsection.
     */
    private static final String iSTARTSUBSECTION = "[";

    /**
     * This constant defines the marker that ends a subsection.
     */
    private static final String iSTOPSUBSECTION = "]";

    /**
     * This Vector instace holds the format definition of a SwissProt entry.
     */
    private static Vector iFormat = null;

    /**
     * Default constructor. It loads the SwissProt format definition if it wasn't
     * already loaded before.
     */
    public SwissProtDBLoader() {
        // First see if the SwissProt format definition has already been loaded
        // from the format file.
        // If not, do so.
        if(iFormat == null) {
            this.loadFormat();
        }
    }

    /**
     * This method reports on the name of the Database that is read and interpreted by the
     * implemented instance. Mostly for debugging and testing purposes.
     *
     * @return  String  with the name of the DB format ('SwissProt').
     */
    public String getDBName() {
        return DBLoader.SWISSPROT;
    }

    /**
     * This method will attempt to report a filtered entry of the database under consideration
     * in FASTA format. The next passed entry is reported, or 'null' if there are no more
     * passing entries left.
     *
     * @param   aFilter the Filter instance to filter against.
     * @return  String  with the next filtered entry in FASTA format.
     * @exception   IOException when something goes wrong while reading the DB file.
     */
    public String nextFilteredFASTAEntry(Filter aFilter) throws IOException {
        String result = null;

        String tempString = this.nextFilteredRawEntry(aFilter);
        // Check for 'null'.
        if(tempString != null) {
            result = this.toFASTAString(tempString, false);
        }

        return result;
    }

    /**
     * This method will report on the next entry in the DB in FASTA format.
     *
     * @return  String  with the FASTA entry representing the next entry.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public String nextFASTAEntry() throws IOException {
        // What we'll be returning.
        String result = null;
        // Get the next entry in raw format.
        String tempString = this.nextRawEntry();
        // Check for EOF, flagged by a returning 'null'.
        if(tempString == null) {
            result = null;
        } else {
            result = this.toFASTAString(tempString, false);
        }
        return result;
    }

    /**
     * This method reads the next entry from a SwissProt formatted flatfile DB.
     *
     * @return  String  with the entry as it appears in the flatfile, or 'null' if EOF.
     * @exception IOException   when the entry could not be read.
     */
    public String nextRawEntry() throws IOException {
        String result = null;
        // First check EOF condition. this is flagged by the BufferedReader by returning
        // 'null' when reading.
        String line = iBr.readLine();
        if(line != null) {
            StringBuffer lSB = new StringBuffer();
            // Okay, not EOF.
            // Now for a classical fence-post.
            while((line != null) && (!line.trim().startsWith("//"))) {
                lSB.append(line+"\n");
                // Move to next line.
                line = iBr.readLine();
            }
            result = lSB.toString();
        }
        return result;
    }

    public HashMap processRawData(String aRaw) throws IOException {
        // The PushBackStringReader.
        PushBackStringReader pbr = new PushBackStringReader(aRaw);

        // The stuff we'll return.
        HashMap result = new HashMap();

        String key = null;
        int occurrances = -2;
        // For looping.
        Iterator iter = iFormat.iterator();

        // Loop over all known keys.
        while(iter.hasNext()) {
            // get the current key.
            String temp = (String)iter.next();
            // Separator is comma.
            int location = temp.indexOf(",");
            // Key is before the comma.
            key = temp.substring(0, location);
            // Occurrances number is after the comma.
            occurrances = Integer.parseInt(temp.substring(location+1).trim());
            // If the key starts with '[', it is a subsection that can have
            // a number of repeats in its own right. We treat it separately.
            if(temp.startsWith(iSTARTSUBSECTION)) {
                HashMap subsection = this.treatSubsection(key.substring(1), occurrances, pbr, iter);
                result.put(key+iSTOPSUBSECTION, subsection);
                continue;
            }

            // Get the value for the key.
            String value = this.readValue(pbr, key, occurrances);
            // Add key, value pair to the HashMap.
            result.put(key, value);
        }

        return result;
    }

    /**
     * This method allows the treatment of a subsection which can be repeated multiple times
     * within the main structure body.
     *
     * @param   aFirstKey   the first key in the subsection. The 'start of subsection' tag should
     *                      have been removed before passing the key, so it is immediately parseable.
     * @param   aFirstOcc   int with the coded occurrances for the aFirstKey key.
     * @param   aPbr    the PushBackStringReader to read from.
     * @param   aFullKeyList    Iterator over the remaining available keys. It should be
     *                          positioned on the starting key of the subsection, so calling
     *                          'next' on it will result in returning the second key of the
     *                          subsection.
     * @return  HashMap with the subsections occurrances as HashMaps in the HashMap, with keys
     *                  "1", "2", "3", ... for each subsequent occurrance.
     * @exception   IOException when reading the buffer goes wrong.
     */
    private HashMap treatSubsection(String aFirstKey, int aFirstOcc, PushBackStringReader aPbr, Iterator aFullKeyList) throws IOException {

        HashMap toReturn = new HashMap();

        // First we make a Vector of all the subsection keys in the current Iterator.
        // We'll also have Vector of their occurrances codes.
        Vector ssKeys = new Vector(5, 5);
        Vector ssOcc = new Vector(5, 5);
        // Fence-post.
        ssKeys.add(aFirstKey);
        ssOcc.add(new Integer(aFirstOcc));
        boolean stopReached = false;
        // Cycle the keys...
        while(aFullKeyList.hasNext()) {
            String temp = (String) aFullKeyList.next();
            // Separator is comma.
            int location = temp.indexOf(",");
            // Key is before the comma.
            String key = temp.substring(0, location);
            // Occurrances number is after the comma.
            int occurrances = Integer.parseInt(temp.replace(']', ' ').substring(location+1).trim());

            // First of all, see if this key is not yet another subsection.
            // If it is, recursively call this method with that key.
            // If it isn't, check for end of subsection. If it's reached, take this last key and
            // leave the iterator alone from now on.
            if(key.startsWith(iSTARTSUBSECTION)) {
                HashMap ssWithin = this.treatSubsection(key.substring(1), occurrances, aPbr, aFullKeyList);
                toReturn.put(key+iSTOPSUBSECTION, ssWithin);
                continue;
            } else if(temp.endsWith(iSTOPSUBSECTION)) {
                stopReached = true;
            }
            // Okay, key and occurrances have to be added.
            ssKeys.add(key);
            ssOcc.add(new Integer(occurrances));

            // See if we should stop.
            if(stopReached) {
                break;
            }
        }

        // So far so good. We now have all of the keys and their occurrances we need to
        // look up (in order!) in the respective Vectors.
        // So let's parse them.
        int liSize = ssKeys.size();
        boolean hasMoreSubsections = true;
        while(hasMoreSubsections) {
            for(int i=0;i<liSize;i++) {
                // First check whether the first key of the subsection is in fact present.
                if(i==0) {
                    // We've got the first key.
                    String firstKey = (String)ssKeys.get(i);
                    // See if it is present in the current line.
                    // If it isn't, push back the read line, set the hasMoreSubsections
                    // variable to 'false' and break.
                    if(!aPbr.readLine().startsWith(firstKey)) {
                        aPbr.unreadLine();
                        hasMoreSubsections = false;
                        break;
                    } else {
                        // Just unread the line.
                        aPbr.unreadLine();
                    }

                }
                String value = this.readValue(aPbr, (String)ssKeys.get(i), ((Integer)ssOcc.get(i)).intValue());
                toReturn.put(ssKeys.get(i), value);
            }
        }

        return toReturn;
    }

    /**
     * This method takes a PushBackStringReader to read from, a key to locate and
     * a (coded) number of lines to read from the buffer from the current position.
     * It will return the value associated with the key.
     *
     * @param   aPbr PushBackStringReader   from which to read.
     * @param   aKey    String with the key to look for.
     * @param   aNumberTimes    int with the coded number of possible occurances. <br />
     *                          Coding is interpretated as follows:
     *                          <ul>
     *                            <li><b>Any number above zero:</b> There are exactly [number]
     *                            lines with the specified key. Each line's value will be concatenated
     *                            into a total value, respecting the linebreaks!</li>
     *                            <li><b>0:</b> The key is optional. Zero or more entries are possible.</li>
     *                            <li><b>-1:</b> The key has one or more occurrances.</li>
     *                          </ul>
     * @return  String  with the value for the specified key, totalled over the specified number of lines.
     * @exception   IOException whenever reading from the Buffer fails.
     */
    private String readValue(PushBackStringReader aPbr, String aKey, int aNumberTimes) throws IOException {

        // Parsing the coded number of occurrances.
        boolean multiple = false;
        boolean defined = false;
        boolean optional = false;
        // See if we need one line only, or multiple.
        if( (aNumberTimes > 1)||(aNumberTimes == -1) ) {
            // We need multiple lines.
            multiple = true;
            // See if the number of times is defined.
            if(aNumberTimes > 1) {
                defined = true;
            }
        } else if(aNumberTimes == 0) {
            optional = true;
        }
        // This will hold intermediate values.
        StringBuffer result = new StringBuffer();
        // This represents the current line.
        String line = null;
        while((line = aPbr.readLine()) != null) {
            // Check for the presence of the key.
            if(line.startsWith(aKey)) {
                // Okay, key is where it is supposed to be.
                // Get value...
                String toAdd = line.substring(5).trim();
                // See if we need to add linebreaks before the
                // addition.
                // (We add linebreaks whenever something is already in the Buffer,
                //  since this means we've already read such a line before this one).
                if(result.length() > 0) {
                    toAdd = "\n" + toAdd;
                }
                // Add the finalized value to the Buffer.
                result.append(toAdd);
                // If we need multiple entries (one or more or optional ones), continue finding them.
                if(!multiple && !optional) {
                    break;
                } else if(defined) {
                    // If the exact number of occurrences is defined, subtract 1 from that number.
                    // If the remaining number is 0, break.
                    aNumberTimes--;
                    if(aNumberTimes == 0) {
                        break;
                    }
                }
            } else {
                // Push back the last read line.
                aPbr.unreadLine();
                // Key was not found at the current position. This could mean two things:
                // Either one or more occurrances that are now terminated (and already contain some value),
                // or an optional key, which can have a blank value. Both cases just exit the loop.
                // All other cases result in an Exception thrown.
                if( (multiple && (result.length()>0)) || (optional) ) {
                    break;
                } else {
                    throw new IOException("Key '" + aKey + "' was not found at the current position, even though it was not optional! Found key '" + line.substring(0,2) + "' instead.");
                }
            }
        }
        return result.toString();
    }

    /**
     * This method will load the format file from the classpath and
     * initialize the static variable on this class.
     */
    private void loadFormat() {
        try {
            iFormat = new Vector();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("SwissProtFormat.frmt");
            if(is == null) {
                throw new IOException("File 'SwissProtFormat.frmt' was not found in the classpath!");
            }
            BufferedReader lBr = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while((line = lBr.readLine()) != null) {
                if(!line.trim().equals("")) {
                    iFormat.add(line);
                }
            }
            lBr.close();
        } catch(IOException ioe) {
            System.err.println("\n* * * * * * * * * * * * * * * * * * * * *\nUnable to locate the 'SwissProtFromat.frmt' file in the classpath!\nNo reading will be possible!\n* * * * * * * * * * * * * * * * * * * * *\n");
        }
    }

    public String toFASTAString(String aRaw, boolean aEndLines) throws IOException {
        // Make the raw data more easily accessible.
        HashMap lhmRaw = this.processRawData(aRaw);
        // We'll need some intermediate String storing.
        StringBuffer fastaString = new StringBuffer();

        // First up is the 'sw' tag, followed by '|', the primary accession number,
        // followed by '|' and the entry name + a whitespace (space).
        fastaString.append(">sw|");
        // Getting the Accessionnumber.
        String temp = (String)lhmRaw.get("AC");
        // Check for multiple Acc. numbers, and if so,
        // take the first one.
        int location = temp.indexOf(";");
        if(location < 0) {
            temp = temp.trim();
        } else {
            temp = temp.substring(0,location).trim();
        }
        fastaString.append(temp + "|");
        // And the entry name...
        temp = (String)lhmRaw.get("ID");
        // Entry name is the first element in the ID field, and is
        // separated from subsequent elements by a whitespace.
        location = temp.indexOf(" ");
        temp = temp.substring(0, location).trim();
        fastaString.append(temp + " ");

        // Next is de description, followed by an endline which marks the end of the header.
        temp = (String)lhmRaw.get("DE");
        // See if we have the post-2008 SwissProt DAT file's DE format (which differentiates
        // between recommended and alternative names). If so, use only hte recommended name,
        // otherwise, include everything.
        if(temp.startsWith("RecName: Full=")) {
            temp = temp.substring(14, temp.indexOf(";", 14)).trim();
        }
        // 'DE' element can contain multiple lines. Use a StringReader to get
        // rid of them.
        BufferedReader lBr = new BufferedReader(new StringReader(temp));
        String line = null;
        while((line = lBr.readLine()) != null) {
            fastaString.append(line);
        }
        lBr.close();
        fastaString.append("\n");

        // All that's left now is the sequence itself.
        // We'll need to clear the sequence up a little, 'though.
        // It contains endlines and whitespaces which we don't want!
        lBr = new BufferedReader(new StringReader((String)lhmRaw.get("  ")));
        StringBuffer tempSequence = new StringBuffer();
        while((line = lBr.readLine()) != null) {
            StringTokenizer lst = new StringTokenizer(line.trim(), " ");
            while(lst.hasMoreTokens()) {
                tempSequence.append(lst.nextToken().trim());
            }
        }

        if(aEndLines) {
            // Okay, sequence String is now joined.
            // Next we want to ensure only 60 characters are present on each line.
            // So at every 59th character, insert and endline.
            // First of all, see if the sequence is long enough!
            if(tempSequence.length()>59) {
                int offset = 58;
                for(int i=0;;i++) {
                    // Insert endline.
                    tempSequence.insert(offset, "\n");
                    // See if we're not overextending our reach here.
                    offset += 59;
                    if(offset > tempSequence.length()) {
                        break;
                    }
                }
            }
        }

        fastaString.append(tempSequence.toString() + "\n");

        return fastaString.toString();
    }

    /**
     * This method attempts to count the number of entries currently in the database. <br />
     * <b<Note</b> that a call to this method resets the position of the underlying reader!
     *
     * @return  long    with the number of entries or DBLoader.CANCELLEDCOUNT if the count was cancelled.
     * @exception   IOException when something goes wrong while reading the file.
     */
    public long countNumberOfEntries() throws IOException {
        // We'll cycle the DB and read all entries,
        this.reset();
        // reset possible leftover cancellation.
        this.iCancelCount = false;
        String line = null;
        long counter = 0;
        // Also check the cancellation boolean!
        while(((line = iBr.readLine()) != null) && !iCancelCount) {
            if(line.trim().startsWith("ID   ")) {
                counter++;
            }
        }
        // If cancelled, return the cancelled status.
        if(this.iCancelCount) {
            this.iCancelCount = false;
            counter = DBLoader.CANCELLEDCOUNT;
        }

        this.reset();
        return counter;
    }

        /**
     * This method is used by the AutoDBLoader class.
     * It cycles all available known DBLoaders until one is found that
     * returns 'true' on this query. Please note that the order in which the
     * DBLoaders are attempted is not fixed! The first instance encountered that
     * returns 'true' will load the DB!
     *
     * @param   aFile   File with the canonical name of the file to read. This
     *                  file is supposed to be existant and readable.
     * @return  boolean 'true' if this DBLoader assumes it can read this format,
     *                  'false' otherwise.
     */
    public boolean canReadFile(File aFile) {
        boolean canRead = false;

        try {
            // Construct a BufferedReader around the file.
            BufferedReader br = new BufferedReader(new FileReader(aFile));
            // read first line.
            String line = br.readLine();
            // Skip leading blank lines.
            while(line != null && line.trim().equals("")) {
                line = br.readLine();
            }
            // Check for 'null'
            if(line != null) {
                if(line.trim().startsWith("ID   ")) {
                    canRead = true;
                }
            }
            br.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return canRead;
    }
}
