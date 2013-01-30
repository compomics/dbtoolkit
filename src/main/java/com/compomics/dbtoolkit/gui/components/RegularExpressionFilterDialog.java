/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 30-May-2007
 * Time: 10:31:47
 */
package com.compomics.dbtoolkit.gui.components;

import com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread;
import com.compomics.dbtoolkit.gui.workerthreads.PeptideMappingThread;
import com.compomics.dbtoolkit.gui.workerthreads.ProcessThread;
import com.compomics.dbtoolkit.io.implementations.FilterCollection;
import com.compomics.dbtoolkit.io.implementations.ProteinFilterCollection;
import com.compomics.dbtoolkit.io.implementations.ProteinSequenceRegExpFilter;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2007/05/30 10:24:36 $
 */

/**
 * This class implements the JDialog that will query the user for all necessary
 * information concerning peptide mapping.
 *
 * @author Lennart Martens
 */
public class RegularExpressionFilterDialog extends JDialog {

    private JFrame iParent = null;
    private JComboBox cmbPrimFilter = null;
    private JTextField txtPrimFilter = null;
    private JTextArea txtRegularExpression = null;
    private JTextArea txtTestString = null;

    private JButton btnTest = null;
    private JButton btnOK = null;
    private JButton btnCancel = null;

    /**
     * The DBLoader to load DB from.
     */
    private DBLoader iLoader = null;

    /**
     * The DB type.
     */
    private String iDBType = null;

    /**
     * The HashMap with the applicable filters for the specified DB type.
     */
    private static HashMap iFilters = null;


    /**
     * This constructor requires a parent for the dialog, a title for the window,
     * a DBLoader to load the DB from and a DBType to load the appropriate filters.
     *
     * @param   aParent JFrame to act as the parent for this dialog
     * @param   aTitle  String with the title for this window.
     * @param   aLoader DBLoader with the loader for the database.
     * @param   aDBType String with the DB type.
     */
    public RegularExpressionFilterDialog(JFrame aParent, String aTitle, DBLoader aLoader, String aDBType) {
        super(aParent, aTitle, true);

        this.iParent = aParent;
        this.iLoader = aLoader;

        // The order of variable initialization is crucial here!
        this.iDBType = aDBType;
        // Check to see if we should load the Filters list.
        if(this.iFilters == null) {
            this.loadFilters();
        }
        this.addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                close();
            }
        });
        this.constructScreen();
        this.setResizable(false);
    }

    /**
     * Method that constructs the screen.
     */
    private void constructScreen() {
        JPanel top = this.getPrimaryFilterPanel();
        JPanel middle = this.getMainPanel();
        JPanel bottom = this.getOptionPanel();

        this.getContentPane().add(top, BorderLayout.NORTH);
        this.getContentPane().add(bottom, BorderLayout.SOUTH);
        this.getContentPane().add(middle, BorderLayout.CENTER);
        this.pack();
    }

    /**
     * This method will construct the panel for the filter selection.
     *
     * @return  JPanel  with the filter selection components and listeners.
     */
    private JPanel getPrimaryFilterPanel() {

        // Components.
        txtPrimFilter = new JTextField(20);
        txtPrimFilter.setEnabled(false);

        // Note that the contents of the 'filters' combobox are
        // dynamic!
        String[] resultString = null;
        Object loTemp = iFilters.get(this.iDBType.toUpperCase());
        if(loTemp != null) {
            HashMap allFilters = (HashMap)loTemp;
            Set s = allFilters.keySet();
            resultString = new String[s.size()+1];
            s.toArray(resultString);
            // Final element will be 'None'.
            resultString[resultString.length-1] = "None";
        } else {
            // No filters specified.
            // Only supply 'None'.
            resultString = new String[] {"None"};
        }
        Arrays.sort(resultString);
        cmbPrimFilter = new JComboBox(resultString);
        cmbPrimFilter.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if((e.getStateChange() == ItemEvent.SELECTED) && ((String)e.getItem()).equalsIgnoreCase("None")) {
                    txtPrimFilter.setEnabled(false);
                } else {
                    txtPrimFilter.setEnabled(true);
                }
            }
        });
        cmbPrimFilter.setSelectedItem("None");

        // Maximum sizes.
        cmbPrimFilter.setMaximumSize(cmbPrimFilter.getPreferredSize());
        txtPrimFilter.setMaximumSize(txtPrimFilter.getPreferredSize());

        // Lay-out.
        // Main panel for this part.
        // With a horizontally aligned box-layout.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        // Titled border.
        panel.setBorder(BorderFactory.createTitledBorder("Filter settings"));

        panel.add(Box.createRigidArea(new Dimension(5, cmbPrimFilter.getHeight())));
        panel.add(cmbPrimFilter);
        panel.add(Box.createRigidArea(new Dimension(10, cmbPrimFilter.getHeight())));
        panel.add(txtPrimFilter);
        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    /**
     * This method constructs the panel for the main input (ragging & subset query).
     *
     * @return  JPanel  with the main panel.
     */
    private JPanel getMainPanel() {
        // Components.
        txtRegularExpression = new JTextArea(10, 40);
        JLabel lblRegularExpression = new JLabel("Specify regular expression pattern here");
        lblRegularExpression.setFont(txtRegularExpression.getFont());
        lblRegularExpression.setForeground(txtRegularExpression.getForeground());

        txtTestString = new JTextArea(10, 40);
        JLabel lblTestString = new JLabel("Enter test text here (optional, use 'Test' button to perform test)");
        lblTestString.setFont(txtTestString.getFont());
        lblTestString.setForeground(txtTestString.getForeground());

        // Maximum sizes.
        txtRegularExpression.setMaximumSize(txtRegularExpression.getPreferredSize());
        lblRegularExpression.setMaximumSize(lblRegularExpression.getPreferredSize());

        txtTestString.setMaximumSize(txtTestString.getPreferredSize());
        lblTestString.setMaximumSize(lblTestString.getPreferredSize());

        // Component lay-out.
        // Main panel.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Regular Expression"));

        JPanel jpanRegularExpression = new JPanel();
        JPanel jpanRegularExpressionLabel = new JPanel();
        jpanRegularExpression.add(txtRegularExpression);
        panel.add(Box.createRigidArea(new Dimension(txtRegularExpression.getWidth(), 15)));
        jpanRegularExpressionLabel.add(lblRegularExpression);

        JPanel jpanTestString = new JPanel();
        JPanel jpanTestStringLabel = new JPanel();
        jpanTestString.add(txtTestString);
        panel.add(Box.createRigidArea(new Dimension(txtTestString.getWidth(), 15)));
        jpanTestStringLabel.add(lblTestString);



        panel.add(jpanRegularExpressionLabel);
        panel.add(new JScrollPane(jpanRegularExpression, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        panel.add(jpanTestStringLabel);
        panel.add(new JScrollPane(jpanTestString, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        panel.add(Box.createVerticalGlue());


        // Finalization.
        return panel;
    }

    /**
     * This method constructs the panel for the optional components(enzymatic cleavage & mass limits).
     *
     * @return  JPanel  with the optional components panel.
     */
    private JPanel getOptionPanel() {
        // The Buttons.
        btnTest = new JButton("Test");
        btnTest.setMnemonic(KeyEvent.VK_T);
        btnTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testPressed();
            }
        });

        btnOK = new JButton("OK");
        btnOK.setMnemonic(KeyEvent.VK_O);
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.setMnemonic(KeyEvent.VK_C);
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        btnOK.setMaximumSize(btnOK.getPreferredSize());
        btnCancel.setMaximumSize(btnCancel.getPreferredSize());

        // Lay-out is next.
        // The panel for the buttons.
        JPanel jpanButtons = new JPanel();
        jpanButtons.setLayout(new BoxLayout(jpanButtons, BoxLayout.X_AXIS));

        jpanButtons.add(Box.createRigidArea(new Dimension(15, btnCancel.getHeight())));
        jpanButtons.add(btnTest);
        jpanButtons.add(Box.createHorizontalGlue());
        jpanButtons.add(btnOK);
        jpanButtons.add(btnCancel);
        jpanButtons.add(Box.createRigidArea(new Dimension(15, btnCancel.getHeight())));

        return jpanButtons;
    }

    /**
     * This method loads the available filters from disk.
     */
    private void loadFilters() {
        try {
            // First loacte the file (if any is to be found)!
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("filters.properties");
            Properties p = null;
            if(is != null) {
                // Okay, file is found!
                p = new Properties();
                p.load(is);
                is.close();
            }

            iFilters = new HashMap();
            // Now to add additionally specified stuff.
            if(p != null) {
                // Get all the keys.
                Enumeration e = p.keys();

                while(e.hasMoreElements()) {
                    String key = (String)e.nextElement();
                    String value = p.getProperty(key).trim();
                    StringTokenizer lst = new StringTokenizer(value, ",");
                    String className = lst.nextToken().trim();
                    String db_key = lst.nextToken().trim();

                    HashMap addTo = null;
                    Object tempObject = iFilters.get(db_key.toUpperCase());
                    if(tempObject == null) {
                        addTo = new HashMap();
                    } else {
                        addTo = (HashMap)tempObject;
                    }
                    addTo.put(db_key.toUpperCase() + " " + key + " filter", className);
                    iFilters.put(db_key.toUpperCase(), addTo);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to close the dialog in a visually appeasing way.
     */
    private void close() {
        this.setVisible(false);
        this.dispose();
    }

    /**
     * This method is called when the OK button is pressed.
     */
    private void okPressed() {
        // First check all the fields and their logic.
        // If all of this complies,
        // continue.
        Runnable pt = this.processInput();
        if(pt != null) {
            // OK, start the correct processing engine.
            Thread t = new Thread(pt);
            t.start();
            close();
        }
    }

    /**
     * This method is called when the Test button is pressed.
     */
    private void testPressed() {
        // First check all the fields and their logic.
        // If all of this complies,
        // continue.
        Pattern p = this.processPattern();
        if(p != null) {
            String testString = txtTestString.getText().trim();
            if(testString.equals("")) {
                JOptionPane.showMessageDialog(this, "You specified an empty test text!", "No test text specified!", JOptionPane.ERROR_MESSAGE);
            } else {
                Matcher matcher = p.matcher(testString);
                if(matcher.find()) {
                    JOptionPane.showMessageDialog(this, "Your regular expression matched the test text.", "Test text matches expression", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Your regular expression did not match the test text!", "Test text does not match expression!", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    /**
     * This method parses the specified string in a compiled regular expression pattern,
     * or flags any error to the user, which also results in a 'null' return value.
     *
     * @return  Pattern with the compiled regular expression, or 'null' in case of an error.
     */
    private Pattern processPattern() {
        Pattern result = null;

        String regExp = txtRegularExpression.getText().trim();
        if(regExp.equals("")) {
            JOptionPane.showMessageDialog(this, "You need to specify a regular expression pattern!", "No regular expression pattern specified!", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                result = Pattern.compile(regExp);
            } catch(PatternSyntaxException pse) {
                JOptionPane.showMessageDialog(this, new String[]{"The regular expression pattern could not be parsed.", "Error was:", pse.getMessage()}, "Regular expression pattern error!", JOptionPane.ERROR_MESSAGE);
            }
        }

        return result;
    }


    /**
     * This method will validate the user input and construct the correct kind of ProcessThread
     * for the selected parameters.
     *
     * @return  ProcessThread   that can perform the requested task, or 'null' if the input did not
     *                          pass the validation.
     */
    private Runnable processInput() {
        Runnable pt = null;

        Filter filter = null;

        int type = 0;

        // Filter.
        if(!((String)cmbPrimFilter.getSelectedItem()).equalsIgnoreCase("NONE")) {
            // Okay, filter selected.
            // See if we have a valid String to filter on.
            String filterName = (String)cmbPrimFilter.getSelectedItem();
            HashMap tempHM = (HashMap)iFilters.get(iDBType.toUpperCase());
            String filterClass = (String)tempHM.get(filterName);
            Class c = null;
            try {
                c = Class.forName(filterClass);
            } catch(ClassNotFoundException cnfe) {
                JOptionPane.showMessageDialog(this, "The class for the " + filterName + " cannot be found (" + filterClass + ")!", "No filter available!", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            // Try to get the constructors.
            Constructor defaultConst = null;
            Constructor constructor = null;
            try {
                defaultConst = c.getConstructor(new Class[]{});
            } catch(Exception e) {
            }
            try {
                constructor = c.getConstructor(new Class[]{"".getClass()});
            } catch(Exception e) {
            }
            // Fall-through logical checks.
            String filterString = txtPrimFilter.getText().trim();
            if((defaultConst == null) && (filterString == null) || (filterString.equals(""))) {
                JOptionPane.showMessageDialog(this, "You need to specify a filter string for use with the " + filterName + "!", "No filter string specified!", JOptionPane.ERROR_MESSAGE);
                return null;
            } else if(filterString != null) {
                if(filterString.startsWith("!")) {
                    Constructor dual = null;
                    try {
                        dual = c.getConstructor(new Class[]{"".getClass(), boolean.class});
                    } catch(Exception e) {
                    }
                    if(dual == null) {
                        JOptionPane.showMessageDialog(this, "Your request for an inverted version of the " + filterName + " cannot be processed, since this Filter does not allow inversion!", "No inverse filter available!", JOptionPane.ERROR_MESSAGE);
                        return null;
                    } else {
                        try {
                            filter = (Filter)dual.newInstance(new Object[]{filterString.substring(1), new Boolean(true)});
                        } catch(Exception ie) {
                            JOptionPane.showMessageDialog(this, new String[]{"Could not create instance of " + filterName + " with a string and boolean argument!", ie.getMessage(), "\n"}, "Unable to create filter!", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    }
                } else {
                    if(constructor == null) {
                        JOptionPane.showMessageDialog(this, "Your request for a configurable " + filterName + " cannot be processed, since this Filter does not allow specification of a filter string!", "No configurable filter available!", JOptionPane.ERROR_MESSAGE);
                        return null;
                    } else {
                        try {
                            filter = (Filter)constructor.newInstance(new Object[]{filterString});
                        } catch(Exception ie) {
                            JOptionPane.showMessageDialog(this, new String[]{"Could not create instance of " + filterName + " with a String argument!", ie.getMessage(), "\n"}, "Unable to create filter!", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    }
                }
            } else if(defaultConst != null) {
                try {
                    filter = (Filter)defaultConst.newInstance(new Object[]{});
                } catch(Exception ie) {
                    JOptionPane.showMessageDialog(this, new String[]{"Could not create instance of " + filterName + " without arguments!", ie.getMessage(), "\n"}, "Unable to create filter!", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } else {
                filter = null;
            }
        } else {
            filter = null;
        }
        // We need to create the peptide collection.
        // First get the input from the user (if any, of course).
        Pattern pattern = this.processPattern();
        if(pattern != null) {
            // Get the filename to output to.
            File file = this.getProcessOutputFile();
            if(file != null) {
                // Create regexp filter
                ProteinSequenceRegExpFilter regExpFilter = new ProteinSequenceRegExpFilter(pattern);

                // Create a task.
                pt = ProcessThread.getSubsetTask(this.iLoader, file, this.iParent, filter, null, false, -1.0, -1.0, regExpFilter);
            }
        }

        return pt;
    }

    /**
     * This method displays a filechooser that indicates that a file to output to (to save to)
     * needs to be specified.
     *
     * @return  File    with the outputfile specified by the user, or 'null' in case of a cancellation.
     */
    private File getProcessOutputFile() {
        // Okay, get a filename to output to.
        File file = null;
        JFileChooser jfc = new JFileChooser("/");
        jfc.setDialogType(JFileChooser.SAVE_DIALOG);
        jfc.setDialogTitle("Output generated DB to...");
        jfc.setApproveButtonToolTipText("Save generated DB to this file.");
        int returnVal = jfc.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            file = new File(jfc.getSelectedFile().getAbsoluteFile().toString());
            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch(IOException ioe) {
                    JOptionPane.showMessageDialog(iParent, "The specified file could not be created! (" + ioe.getMessage() + ")", "Error creating output file!", JOptionPane.ERROR_MESSAGE);
                    file = null;
                }
            }
        } else {
            file = null;
        }

        return file;
    }
}