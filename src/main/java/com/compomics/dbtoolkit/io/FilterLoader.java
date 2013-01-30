/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-aug-2003
 * Time: 12:16:30
 */
package com.compomics.dbtoolkit.io;

import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.implementations.FilterCollection;
import com.compomics.dbtoolkit.toolkit.EnzymeDigest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2008/11/25 16:43:53 $
 */

/**
 * This class allows the loading of a Filter from a name, an optional parameter and a DBLoader
 * to associate the FIlter with.
 *
 * @author Lennart Martens
 */
public class FilterLoader {


    /**
     * This method loads the filter from a Properties file ('filter.properties') which should be found in the
     * classpath.
     *
     * @param aFilterName   String with the name of the filter to load.
     * @param aParam    String with the filterparameter (this can be 'null' for no parameter).
     * @param aLoader   DBLoader to associate the Filter with.
     * @return  Filter which was requested.
     * @throws IOException  when the Filter could not be loaded.
     */
    public static Filter loadFilter(String aFilterName, String aParam, DBLoader aLoader) throws IOException {
        Filter f = null;
        if(aFilterName != null) {
            Properties props = new Properties();
            InputStream in = EnzymeDigest.class.getClassLoader().getResourceAsStream("filters.properties");
            if(in == null) {
                throw new IOException("File 'filters.properties' not found in current classpath!");
            }
            props.load(in);
            String filterParams = props.getProperty(aFilterName);
            if(filterParams == null) {
                Enumeration e = props.keys();
                StringBuffer sb = new StringBuffer();
                while(e.hasMoreElements()) {
                    sb.append("  - " + (String)e.nextElement() + "\n");
                }
                throw new IOException("The filter you specified (" + aFilterName + ") is not found in the 'filters.properties' file!\n\nAvailable filters:\n" + sb.toString());
            }
            StringTokenizer st = new StringTokenizer(filterParams, ",");
            String filterClass = st.nextToken().trim();
            String filterDB = st.nextToken().trim();
            if(!filterDB.equals(aLoader.getDBName())) {
                throw new IOException("The filter you specified (" + aFilterName + ") is not available for a '" + aLoader.getDBName() + "' database but for a '" + filterDB + "' database!");
            } else {
                try {
                    Constructor constr = null;
                    int type = 0;
                    Class lClass = Class.forName(filterClass);
                    if(lClass == null) {
                        throw new IOException("The class '" + filterClass + "' for your filter '" + aFilterName + "' could not be found! Check your clasppath setting!");
                    }
                    if(aParam == null) {
                        try {
                            constr = lClass.getConstructor(new Class[]{});
                        } catch(Exception exc) {
                        }
                        type = 1;
                    } else if(aParam.startsWith("!")) {
                        try {
                            constr = lClass.getConstructor(new Class[]{String.class, boolean.class});
                        } catch(Exception exc) {
                        }
                        type = 2;
                    } else {
                        try {
                            constr = lClass.getConstructor(new Class[]{String.class});
                        } catch(Exception exc) {
                        }
                        type = 3;
                    }
                    if(constr == null) {
                        throw new IOException("The '" + aFilterName + "' filter does not support the " + ((aParam != null)?"presence":"absence") + " of a" + (((aParam != null) && (aParam.startsWith("!")))?"n inverted ":" ") + "parameter!");
                    } else {
                        if(type == 1) {
                            f = (Filter)constr.newInstance(new Object[]{});
                        } else if(type == 2) {
                            f = (Filter)constr.newInstance(new Object[]{aParam.substring(1), new Boolean(true)});
                        } else {
                            f = (Filter)constr.newInstance(new Object[]{aParam});
                        }
                    }
                } catch(ClassNotFoundException e) {
                    throw new IOException("Unable to load class '" + filterClass + "' for your filter '" + aFilterName + "': " + e.getMessage());
                } catch(InstantiationException ie) {
                    throw new IOException("Unable to instantiate class '" + filterClass + "' for your filter '" + aFilterName + "': " + ie.getMessage());
                } catch(IllegalAccessException iae) {
                    throw new IOException("Unable to access constructor for class '" + filterClass + "' for your filter '" + aFilterName + "': " + iae.getMessage());
                } catch(InvocationTargetException ite) {
                    throw new IOException("Unable to invoke constructor for class '" + filterClass + "' for your filter '" + aFilterName + "': " + ite.getMessage());
                }
            }
        }
        return f;
    }

    /**
     * This method processes the specified filter string and processes it into
     * a filterSet instance, with the filters combined in boolean AND logic.
     *
     * @param aFilterSet    String with the filter set configured in the start-up
     *                      parameters.
     * @param aLoader   DBLoader with the database to load. This is used to check
     *                  whether the filter applies to the currently loaded databases.
     * @return  Filter  with the filterset.
     */
    public static Filter processFilterSetANDLogic(String aFilterSet, DBLoader aLoader) throws IOException {
        FilterCollection result = new FilterCollection(FilterCollection.AND);
        String[] parts =  aFilterSet.split(";");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            String filterName = null;
            String filterParam = null;
            if(part.indexOf("=") > 0) {
                String[] filter_and_param = part.split("=");
                filterName = filter_and_param[0].trim();
                filterParam = filter_and_param[1].trim();
            } else {
                filterName = part;
            }
            result.add(FilterLoader.loadFilter(filterName, filterParam, aLoader));
        }
        return result;
    }
}
