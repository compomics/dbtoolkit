/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 28-okt-02
 * Time: 14:10:14
 */
package com.compomics.dbtoolkit.io;

import com.compomics.dbtoolkit.io.implementations.ProteinFilterCollection;
import com.compomics.dbtoolkit.io.implementations.ProteinResiduCountFilter;
import com.compomics.dbtoolkit.io.implementations.ProteinSequenceFilter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;

import java.text.ParseException;
import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the behaviour for a component that can parse a
 * sequence subset query into a ProteinFilterCollection that performs
 * the boolean request as specified in the query.
 *
 * @author Lennart Martens
 */
public class QueryParser {

    public static final String iANDToken = "AND";
    public static final String iORToken = "OR";

    /**
     * Default constructor.
     */
    public QueryParser() {
    }

    /**
     * This method parses a sequence-based subset query
     * into an equivalent ProteinFilterCollection.
     *
     * @param   aQuery  String with the sequence-based subset query to parse.
     * @return  ProteinFilter   that implements the same boolean logic as the
     *                          original query.
     * @exception ParseException    when the query String could not be parsed.
     */
    public ProteinFilter parseQuery(String aQuery) throws ParseException {
        ProteinFilter pf = null;

        // Let's start parsing.
        pf = this.parseInternal(aQuery, 1);

        return pf;
    }

    /**
     * This method is the workhorse here.
     * It does the real parsing, and the combining of ProteinFilterCollections.
     * It is called recursively.
     *
     * @param   aQuery  String with the sequence-based subset query to parse.
     * @param   aLocation   int with the start location of the parameter string (useful for error message).
     * @return  ProteinFilter   that implements the same boolean logic as the
     *                          original query.
     * @exception ParseException    when the query String could not be parsed.
     */
    private ProteinFilter parseInternal(String aQuery, int aLocation) throws ParseException {
        ProteinFilter pf = null;
        String upper = aQuery.toUpperCase();
        // Simple implementation first.
        // See if we are managing '()', 'AND', 'OR' or nothing.
        if(upper.indexOf("(") >= 0) {
            // Okay, we have a subset here.
            // Find the subset.
            pf = this.processInternalSubset(upper, aLocation);
        } else if((upper.indexOf(iANDToken) >= 0) || (upper.indexOf(iORToken) >= 0)) {
            // If the query contains an ')', there is something wrong here!
            int errorLoc = 0;
            if((errorLoc = upper.indexOf(')')) >= 0) {
                throw new ParseException("Unbalanced parenthesis in query, found closing bracket without opening bracket at position " + (aLocation + errorLoc) + "!", aLocation + errorLoc);
            }
            // This is a combination.
            // Just flag what kind.
            String token = null;
            boolean isAnd = false;
            if((upper.indexOf(iANDToken) >= 0)) {
                isAnd = true;
                token = iANDToken;
                pf = new ProteinFilterCollection(ProteinFilterCollection.AND);
            } else {
                isAnd = false;
                token = iORToken;
                pf = new ProteinFilterCollection(ProteinFilterCollection.OR);
            }

            // See if the query does not erroneously starts with the token.
            if(upper.trim().startsWith(token)) {
                throw new ParseException("Query cannot start with '" + token + "' at position " + aLocation + "! Unbalanced boolean terms!", aLocation);
            }

            // Okay, get all sides of the story.
            int location = 0;
            int prevLoc = 0;
            Vector allElements = new Vector(3, 2);
            Vector allLocations = new Vector(3, 2);
            while((location = upper.indexOf(token, prevLoc)) > prevLoc) {
                String toAdd = upper.substring(prevLoc, location).trim();
                if(toAdd.length() == 0) {
                    throw new ParseException("Unbalanced terms at either side of '" + token + "' at position " + (aLocation + prevLoc) + ".", (aLocation + prevLoc));
                }
                allElements.add(toAdd);
                allLocations.add(new Integer(aLocation + prevLoc));
                prevLoc = location + token.length();
            }
            // Now add the last element as well.
            // (reverse fence-post).
            String toAdd = upper.substring(prevLoc, upper.length()).trim();
            allElements.add(toAdd);
            allLocations.add(new Integer(aLocation + prevLoc));
            if(toAdd.length() == 0) {
                throw new ParseException("Unbalanced terms at either side of '" + token + "' at position " + (aLocation + prevLoc) + ".", (aLocation + prevLoc));
            }
            String[] elements = new String[allElements.size()];
            allElements.toArray(elements);
            for(int i = 0; i < elements.length; i++) {
                String lElement = elements[i];
                int lLoc = ((Integer)allLocations.get(i)).intValue();
                ((ProteinFilterCollection)pf).add(this.parseInternal(lElement, lLoc));
            }
        } else {
            // If the query contains an ')', there is something wrong here!
            int errorLoc = 0;
            if((errorLoc = upper.indexOf(')')) >= 0) {
                throw new ParseException("Unbalanced parenthesis in query, found closing bracket without opening bracket at position " + (aLocation + errorLoc) + "!", (aLocation + errorLoc));
            }
            // This is a regular filter.
            if(upper.length() == 0) {
                throw new ParseException("Empty query found!", 0);
            }
            pf = this.parseSimpleFilter(upper, aLocation);
        }

        return pf;
    }

    /**
     * This method is the workhorse for subset-containing queries.
     * It does the real parsing, and the combining of ProteinFilterCollections.
     * It is called recursively.
     *
     * @param   aLocation   int with the start location of the parameter string (useful for error message).
     * @return  ProteinFilter   that implements the same boolean logic as the
     *                          original query.
     * @exception ParseException    when the query String could not be parsed.
     */
    private ProteinFilter processInternalSubset(String aUpper, int aLocation) throws ParseException {
        ProteinFilter pf = null;
        String internal = aUpper;
        Vector filters = new Vector();
        int filterIndex = 0;
        int start = 0;
        int end = 0;
        int counter = 0;
        while((start = internal.indexOf('(')) >= 0) {
            counter = 1;
            // Find the end.
            for(int i=start+1;i<internal.length();i++) {
                char c = internal.charAt(i);
                if(c == '(') {
                    counter++;
                } else if(c == ')') {
                    counter--;
                }
                if(counter==0) {
                    // End found!
                    end = i;
                    break;
                }
            }

            // See if there was an end found!
            if(end == 0) {
                throw new ParseException("Unbalanced parentheses for subsection starting at position " + (start+aLocation) + "!", (start+aLocation));
            } else {
                // Subsection found!
                // Isolate it and replace with '$' + an index.
                // Note that we omit the '()'.
                String temp = internal.substring(start+1, end);
                // See if the subsection needs to be inverted.
                boolean inversion = false;
                int tempLoc = start;
                if((start > 0) && (internal.charAt(start-1)) == '!') {
                    inversion = true;
                    tempLoc--;
                }
                internal = internal.substring(0, tempLoc) + "$" + filterIndex + internal.substring(end+1, internal.length());
                ProteinFilter pfInterim = this.parseInternal(temp, (aLocation + start+1));
                if(pfInterim == null) {
                    throw new ParseException("Unable to parse your query subsection from position " + (aLocation + start) + " to position " + (end + aLocation) + ": (" + temp + ")!", (aLocation + start));
                }

                pfInterim.setInversion(inversion);
                filters.add(pfInterim);
                filterIndex++;
            }
        }

        // Okay, we now have a collection of ProteinFilters that each handle a specific subsection, we now need to
        // find out how to combine them!
        boolean isAND = false;
        String token = null;
        // If we now have aclosing bracket left, we have unbalanced parentheses.
        int errorLoc = 0;
        if((errorLoc = internal.indexOf(')')) >= 0) {
            throw new ParseException("Unbalanced parenthesis in query, found closing bracket without opening bracket at location " + (aLocation + errorLoc) + "!", (aLocation + errorLoc));
        }
        if(internal.indexOf(iANDToken) >= 0) {
            isAND = true;
            token = iANDToken;
            pf = new ProteinFilterCollection(ProteinFilterCollection.AND);
        } else if(internal.indexOf(iORToken) >= 0) {
            isAND = false;
            token = iORToken;
            pf = new ProteinFilterCollection(ProteinFilterCollection.OR);
        } else {
            if(filters.size() == 1) {
                pf = (ProteinFilter)filters.get(0);
            } else {
                throw new ParseException("You specified multiple subsections in the query, without specifying a boolean operator to join them!", 0);
            }
        }

        // Okay, we now have the correct kind of ProteinFilterCollection, start adding all filters.
        if(token != null) {
            // See if the query does not erroneously starts with the token.
            if(internal.trim().startsWith(token)) {
                throw new ParseException("Query cannot start with '" + token + "'! Unbalanced boolean terms!", aLocation);
            }

            // Okay, get all sides of the story.
            int location = 0;
            int prevLoc = 0;
            Vector allElements = new Vector(3, 2);
            Vector allLocations = new Vector(3, 2);
            while((location = internal.indexOf(token, prevLoc)) > prevLoc) {
                String toAdd = internal.substring(prevLoc, location).trim();
                if(toAdd.length() == 0) {
                    throw new ParseException("Unbalanced terms at either side of '" + token + "' at position " + (aLocation + prevLoc) + ".", (aLocation + prevLoc));
                }
                allElements.add(toAdd);
                allLocations.add(new Integer(aLocation + prevLoc));
                prevLoc = location + token.length();
            }
            // Now add the last element as well.
            // (reverse fence-post).
            String toAdd = internal.substring(prevLoc, internal.length()).trim();
            allElements.add(toAdd);
            allLocations.add(new Integer(aLocation + prevLoc));
            if(toAdd.length() == 0) {
                throw new ParseException("Unbalanced terms at either side of '" + token + "' at position " + (aLocation + prevLoc) + ".", (aLocation + prevLoc));
            }
            String[] elements = new String[allElements.size()];
            allElements.toArray(elements);
            for(int i = 0; i < elements.length; i++) {
                String lElement = elements[i];
                int lLoc = ((Integer)allLocations.get(i)).intValue();
                if(lElement.startsWith("$")) {
                    int elementIndex = Integer.parseInt(lElement.substring(1));
                    ((ProteinFilterCollection)pf).add((ProteinFilter)filters.get(elementIndex));
                } else {
                    ((ProteinFilterCollection)pf).add(this.parseInternal(lElement, lLoc));
                }
            }
        }

        return pf;
    }

    /**
     * This method parses a simple filter from a String.
     *
     * @param   aParam  the String to parse th filter from.
     * @param   aLocation   int with the tsart location of this substring in the total query.
     * @return  ProteinFilter   for the specified String, or 'null' if there was a problem.
     * @exception ParseException    when the query String could not be parsed.
     */
    private ProteinFilter parseSimpleFilter(String aParam, int aLocation) throws ParseException {
        ProteinFilter pf = null;
        // See if any number is detected.
        boolean containsNbr = false;
        int nbrIndex = -1;

        for(int i=0;i<aParam.length();i++) {
            if(Character.isDigit(aParam.charAt(i))) {
                containsNbr = true;
                nbrIndex = i;
                break;
            }
        }

        // If there is a number, it should be a
        // ProteinResiduCountFilter.
        // Else it is a simple sequencefilter.
        if(containsNbr) {
            // See what other characters are present before our number, if any.
            boolean invert=false;
            int mode = ProteinResiduCountFilter.EQUALS_TO;
            if(nbrIndex>0) {
                char currentChar = aParam.charAt(nbrIndex-1);
                if(currentChar == '=') {
                    mode = ProteinResiduCountFilter.EQUALS_TO;
                    if((nbrIndex-1) > 0) {
                        currentChar = aParam.charAt(nbrIndex-2);
                    }
                } else if(currentChar == '>') {
                    mode = ProteinResiduCountFilter.GREATER_THAN;
                    if((nbrIndex-1) > 0) {
                        currentChar = aParam.charAt(nbrIndex-2);
                    }
                } else if(currentChar == '<') {
                    mode = ProteinResiduCountFilter.LESS_THAN;
                    if((nbrIndex-1) > 0) {
                        currentChar = aParam.charAt(nbrIndex-2);
                    }
                }
                // Now see if we must invert.
                if(currentChar == '!') {
                    invert = true;
                }
            }
            // Now check for true residu or stretch.
            int resLocation = 0;
            for(int i=nbrIndex;i<aParam.length();i++) {
                if(Character.isLetter(aParam.charAt(i))) {
                    resLocation = i;
                    break;
                }
            }
            // If there is no non-numerical found past the
            // number, the residu to count is not specified, throw an error.
            if(resLocation == 0) {
                throw new ParseException("No residu (or stretch) specified for counting at position " + (aLocation + nbrIndex) + "!", (aLocation + nbrIndex));
            } else {
                // Okay, get the residu (or stretch), get the number and construct the appropriate Filter for it.
                String residu = aParam.substring(resLocation, aParam.length());
                int counter = 0;
                try {
                    counter = Integer.parseInt(aParam.substring(nbrIndex, resLocation));
                } catch(Exception e) {
                    // Number was not parseable.
                    throw new ParseException("The whole number you should have specified at position " + (nbrIndex + aLocation) + " could not be parsed!", (nbrIndex + aLocation));
                }
                pf = new ProteinResiduCountFilter(residu, counter, mode, invert);
            }
        } else {
            pf = new ProteinSequenceFilter(aParam);
        }

        return pf;
    }
}
