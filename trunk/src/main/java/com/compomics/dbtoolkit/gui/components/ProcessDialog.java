/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 14-okt-02
 * Time: 15:57:14
 */
package com.compomics.dbtoolkit.gui.components;

import com.compomics.dbtoolkit.gui.workerthreads.ProcessThread;
import com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread;
import com.compomics.dbtoolkit.io.QueryParser;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;
import com.compomics.dbtoolkit.io.interfaces.Filter;
import com.compomics.dbtoolkit.io.interfaces.ProteinFilter;
import com.compomics.util.io.MascotEnzymeReader;
import com.compomics.util.protein.Enzyme;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.*;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the JDialog that will query the user for all necessary
 * information concerning processing.
 *
 * @author Lennart Martens
 */
public class ProcessDialog extends JDialog {

    private JFrame iParent = null;
    private JComboBox cmbPrimFilter = null;
    private JTextField txtPrimFilter = null;
    private JCheckBox chkMassLimits = null;
    private JCheckBox chkDigest = null;
    private JTextField txtMinMass = null;
    private JTextField txtMaxMass = null;
    private JComboBox cmbEnzymes = null;
    private JTextField txtEnzymeFile = null;
    private JButton btnChangeEnzymeFile = null;
    private JTextField txtMiscleavage = null;
    private JTextField txtCleave = null;
    private JTextField txtRestrict = null;
    private JTextField txtPosition = null;

    private JRadioButton radRagging = null;
    private JRadioButton radSubset = null;
    private JRadioButton radDigestOnly = null;
    private JRadioButton radFilterOnly = null;

    private JComboBox cmbTerminus = null;
    private JTextField txtTruncate = null;
    private JCheckBox chkTruncate = null;
    private JLabel lblTruncate = null;
    private JTextArea txtQuery = null;
    private JLabel lblQuery = null;

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
     * The enzymereader to read enzymes from.
     */
    private MascotEnzymeReader iReader = null;

    /**
     * The HashMap with the applicable filters for the specified DB type.
     */
    private static HashMap iFilters = null;

    /**
     * The String with the filename for the default enzymes file.
     */
    private static final String DEFAULT_ENZYMES_FILE = "enzymes.txt";


    /**
     * This constructor requires a parent for the dialog, a title for the window,
     * a DBLoader to load the DB from and a DBType to load the appropriate filters.
     *
     * @param   aParent JFrame to act as the parent for this dialog
     * @param   aTitle  String with the title for this window.
     * @param   aLoader DBLoader with the loader for the database.
     * @param   aDBType String with the DB type.
     */
    public ProcessDialog(JFrame aParent, String aTitle, DBLoader aLoader, String aDBType) {
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
        // RadioButtons to select the desired kind of processing.
        radRagging = new JRadioButton("Ragging", true);
        radRagging.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Disable all components on the bottom panel
                // and enable all components on the top panel.
                cmbTerminus.setEnabled(true);
                chkTruncate.setEnabled(true);
                txtTruncate.setEnabled(true);
                lblTruncate.setEnabled(true);
                txtMinMass.setEnabled(true);
                txtMaxMass.setEnabled(true);
                chkMassLimits.setSelected(true);
                

                txtQuery.setEnabled(false);
                lblQuery.setEnabled(false);

                chkDigest.setEnabled(true);
                chkDigest.setSelected(true);
            }
        });
        radSubset = new JRadioButton("Sequence-based Subset", false);
        radSubset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Disable all components on the top panel
                // and enable all components on the bottom panel.

                cmbTerminus.setEnabled(false);
                chkTruncate.setEnabled(false);
                txtTruncate.setEnabled(false);
                lblTruncate.setEnabled(false);
                txtMinMass.setEnabled(true);
                txtMaxMass.setEnabled(true);
                chkMassLimits.setSelected(true);


                txtQuery.setEnabled(true);
                lblQuery.setEnabled(true);

                chkDigest.setEnabled(true);
                chkDigest.setSelected(true);
            }
        });
        radDigestOnly = new JRadioButton("Enzymatic digest only", false);
        radDigestOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Disable all components on the top
                // and bottom panel.
                cmbTerminus.setEnabled(false);
                chkTruncate.setEnabled(false);
                txtTruncate.setEnabled(false);
                lblTruncate.setEnabled(false);
                txtQuery.setEnabled(false);
                lblQuery.setEnabled(false);
                txtMinMass.setEnabled(true);
                txtMaxMass.setEnabled(true);
                chkMassLimits.setSelected(true);

                chkDigest.setSelected(true);
                chkDigest.setEnabled(false);
            }
        });
        radFilterOnly = new JRadioButton("Filter database only", false);
        radFilterOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Disable all components on the top
                // and bottom panel.
                cmbTerminus.setEnabled(false);
                chkTruncate.setEnabled(false);
                txtTruncate.setEnabled(false);
                lblTruncate.setEnabled(false);
                txtQuery.setEnabled(false);
                lblQuery.setEnabled(false);
                txtMinMass.setEnabled(false);
                txtMaxMass.setEnabled(false);
                chkMassLimits.setSelected(false);

                chkDigest.setSelected(false);
                chkDigest.setEnabled(false);

            }
        });
        // Create a ButtonGroup for the radiobuttons.
        ButtonGroup bp = new ButtonGroup();
        bp.add(radRagging);
        bp.add(radSubset);
        bp.add(radDigestOnly);
        bp.add(radFilterOnly);

        // Now the ragging components.
        cmbTerminus = new JComboBox(new String[]{"N-terminal", "C-terminal"});
        cmbTerminus.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    lblTruncate.setText(cmbTerminus.getSelectedItem() + " residus before ragging.");
                }
            }
        });
        chkTruncate = new JCheckBox("Truncate entries to", true);
        chkTruncate.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(chkTruncate.isSelected()) {
                    txtTruncate.setEnabled(true);
                } else {
                    txtTruncate.setEnabled(false);
                }
            }
        });
        txtTruncate = new JTextField("100", 5);
        lblTruncate = new JLabel("N-terminal residus before ragging.");
        lblTruncate.setFont(chkTruncate.getFont());
        lblTruncate.setForeground(chkTruncate.getForeground());
        txtQuery = new JTextArea(10, 40);
        lblQuery = new JLabel("Enter restriction query here");
        lblQuery.setFont(chkTruncate.getFont());
        lblQuery.setForeground(chkTruncate.getForeground());
        txtQuery.setEnabled(false);
        lblQuery.setEnabled(false);


        // Maximum sizes.
        radRagging.setMaximumSize(radRagging.getPreferredSize());
        radSubset.setMaximumSize(radSubset.getPreferredSize());
        cmbTerminus.setMaximumSize(cmbTerminus.getPreferredSize());
        chkTruncate.setMaximumSize(chkTruncate.getPreferredSize());
        txtTruncate.setMaximumSize(txtTruncate.getPreferredSize());
        txtQuery.setMaximumSize(txtQuery.getPreferredSize());

        lblQuery.setMaximumSize(lblQuery.getPreferredSize());

        // Component lay-out.
        // Main panel.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Processing style"));
        // Sub-panels with BoxLayouts.
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        JPanel topRadioPanel = new JPanel();
        topRadioPanel.setLayout(new BoxLayout(topRadioPanel, BoxLayout.Y_AXIS));
        JPanel bottomRadioPanel = new JPanel();
        bottomRadioPanel.setLayout(new BoxLayout(bottomRadioPanel, BoxLayout.Y_AXIS));
        JPanel extraRadioPanel = new JPanel();
        extraRadioPanel.setLayout(new BoxLayout(extraRadioPanel, BoxLayout.X_AXIS));
        JPanel extraRadioPanel2 = new JPanel();
        extraRadioPanel2.setLayout(new BoxLayout(extraRadioPanel2, BoxLayout.X_AXIS));


        JPanel jpanRagging = new JPanel();
        jpanRagging.setLayout(new BoxLayout(jpanRagging, BoxLayout.Y_AXIS));
        JPanel jpanWrapRagging = new JPanel();
        jpanWrapRagging.setLayout(new BoxLayout(jpanWrapRagging, BoxLayout.Y_AXIS));

        JPanel jpanSubset = new JPanel();
        jpanSubset.setLayout(new BoxLayout(jpanSubset, BoxLayout.Y_AXIS));
        JPanel jpanWrapSubset = new JPanel();
        jpanWrapSubset.setLayout(new BoxLayout(jpanWrapSubset, BoxLayout.Y_AXIS));

        JPanel jpanTerminus = new JPanel();
        jpanTerminus.setLayout(new BoxLayout(jpanTerminus, BoxLayout.X_AXIS));

        JPanel jpanTruncate = new JPanel();
        jpanTruncate.setLayout(new BoxLayout(jpanTruncate, BoxLayout.X_AXIS));

        JPanel jpanQuery = new JPanel();
        JPanel jpanQueryLabel = new JPanel();

        // Construction of the topPanel.
        topRadioPanel.add(Box.createRigidArea(new Dimension(5, radRagging.getHeight())));
        topRadioPanel.add(radRagging);
        topRadioPanel.add(Box.createVerticalGlue());

        jpanTerminus.add(cmbTerminus);
        jpanTerminus.add(Box.createHorizontalGlue());

        jpanTruncate.add(chkTruncate);
        jpanTruncate.add(txtTruncate);
        jpanTruncate.add(Box.createRigidArea(new Dimension(10, chkTruncate.getHeight())));
        jpanTruncate.add(lblTruncate);
        jpanTruncate.add(Box.createHorizontalGlue());

        jpanRagging.add(Box.createRigidArea(new Dimension(jpanTerminus.getWidth(), 15)));
        jpanRagging.add(jpanTerminus);
        jpanRagging.add(jpanTruncate);
        jpanRagging.add(Box.createVerticalGlue());
        jpanWrapRagging.add(jpanRagging);
        jpanWrapRagging.add(Box.createVerticalGlue());

        // Construction of the bottomPanel.
        bottomRadioPanel.add(Box.createRigidArea(new Dimension(5, radSubset.getHeight())));
        bottomRadioPanel.add(radSubset);
        bottomRadioPanel.add(Box.createVerticalGlue());

        topPanel.add(topRadioPanel);
        topPanel.add(Box.createRigidArea(new Dimension(120, topRadioPanel.getHeight())));
        topPanel.add(jpanRagging);
        topPanel.add(Box.createHorizontalGlue());

        jpanQuery.add(txtQuery);
        jpanSubset.add(Box.createRigidArea(new Dimension(txtQuery.getWidth(), 15)));
        jpanQueryLabel.add(lblQuery);
        jpanSubset.add(jpanQueryLabel);
        jpanSubset.add(new JScrollPane(jpanQuery, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        jpanSubset.add(Box.createVerticalGlue());

        jpanWrapSubset.add(jpanSubset);
        jpanWrapSubset.add(Box.createVerticalGlue());

        // Construction of extraRadioPanel.
        extraRadioPanel.add(radDigestOnly);
        extraRadioPanel.add(Box.createHorizontalGlue());

        // Construction of yet another radio panel
        extraRadioPanel2.add(radFilterOnly);
        extraRadioPanel2.add(Box.createHorizontalGlue());

        bottomPanel.add(bottomRadioPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(radSubset.getWidth()+20, bottomRadioPanel.getHeight())));
        bottomPanel.add(jpanSubset);
        bottomPanel.add(Box.createHorizontalGlue());


        // Finalization.
        panel.add(topPanel);
        panel.add(Box.createRigidArea(new Dimension(bottomPanel.getWidth(), 25)));
        panel.add(bottomPanel);
        panel.add(Box.createRigidArea(new Dimension(bottomPanel.getWidth(), 25)));
        panel.add(extraRadioPanel);
        panel.add(Box.createRigidArea(new Dimension(bottomPanel.getWidth(), 25)));
        panel.add(extraRadioPanel2);

        return panel;
    }

    /**
     * This method constructs the panel for the optional components(enzymatic cleavage & mass limits).
     *
     * @return  JPanel  with the optional components panel.
     */
    private JPanel getOptionPanel() {

        // First init the components.
        chkDigest = new JCheckBox("Enzymatic Digest", true);
        // Enable/disable rest if not checked.
        chkDigest.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean lState = false;
                if(chkDigest.isSelected()) {
                    lState = true;
                }
                txtMiscleavage.setEnabled(lState);
                btnChangeEnzymeFile.setEnabled(lState);
                cmbEnzymes.setEnabled(lState);
            }
        });
        // Miscleavages.
        txtMiscleavage = new JTextField("1", 3);
        // EnzymeFile.
        txtEnzymeFile = new JTextField(DEFAULT_ENZYMES_FILE, 15);
        txtEnzymeFile.setEnabled(false);
        // Button to change enzymefile.
        btnChangeEnzymeFile = new JButton("...");
        // Logic to pop-up a filechooser and validate the reply.
        btnChangeEnzymeFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser("/");
                int returnVal = jfc.showOpenDialog((JComponent)e.getSource());
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    txtEnzymeFile.setText(jfc.getSelectedFile().getAbsoluteFile().toString());
                    fillEnzymeList();
                }
            }
        });

        // Labels for cmbEnzymes.
        txtCleave = new JTextField("<none>", 8);
        txtCleave.setEnabled(false);
        txtRestrict = new JTextField("<none>", 8);
        txtRestrict.setEnabled(false);
        txtPosition = new JTextField("<none>", 8);
        txtPosition.setEnabled(false);

        // Init the 'cmbEnzymes' combobox in this method.
        fillEnzymeList();
        // Selection of enzymename displays data about enzyme.
        cmbEnzymes.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    String selected = (String)e.getItem();
                    stateChangedEnzyme(selected);
                }
            }
        });
        // The mass limits with a preset.
        chkMassLimits = new JCheckBox("Use mass limits", true);
        txtMinMass = new JTextField("600", 6);
        txtMaxMass = new JTextField("4000", 6);
        // Enable/disable the mass limits.
        chkMassLimits.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(chkMassLimits.isSelected()) {
                    txtMinMass.setEnabled(true);
                    txtMaxMass.setEnabled(true);
                } else {
                    txtMinMass.setEnabled(false);
                    txtMaxMass.setEnabled(false);
                }
            }
        });

        // The Buttons.
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

        // Setting maximum sizes.
        txtMiscleavage.setMaximumSize(txtMiscleavage.getPreferredSize());
        txtEnzymeFile.setMaximumSize(txtEnzymeFile.getPreferredSize());
        btnChangeEnzymeFile.setMaximumSize(btnChangeEnzymeFile.getPreferredSize());
        txtMinMass.setMaximumSize(txtMinMass.getPreferredSize());
        txtMaxMass.setMaximumSize(txtMaxMass.getPreferredSize());
        txtCleave.setMaximumSize(txtCleave.getPreferredSize());
        txtRestrict.setMaximumSize(txtRestrict.getPreferredSize());
        txtPosition.setMaximumSize(txtPosition.getPreferredSize());
        btnOK.setMaximumSize(btnOK.getPreferredSize());
        btnCancel.setMaximumSize(btnCancel.getPreferredSize());

        // Lay-out is next.
        // Main panel.
        // Vertical BoxLayout.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // The panel for the enzyme stuff.
        JPanel jpanEnzymeDigest = new JPanel();
        // Vertical boxlayout and titled border.
        jpanEnzymeDigest.setLayout(new BoxLayout(jpanEnzymeDigest, BoxLayout.Y_AXIS));
        jpanEnzymeDigest.setBorder(BorderFactory.createTitledBorder("Enzymatic digestion"));

        // The panel for the mass limits.
        // Default flowlayout is fine + titled border.
        JPanel jpanMassConstraints = new JPanel();
        jpanMassConstraints.setLayout(new BoxLayout(jpanMassConstraints, BoxLayout.X_AXIS));
        jpanMassConstraints.setBorder(BorderFactory.createTitledBorder("Mass limits"));

        // The panel for the buttons.
        JPanel jpanButtons = new JPanel();
        jpanButtons.setLayout(new BoxLayout(jpanButtons, BoxLayout.X_AXIS));

        // Top part of the enzyme stuff.
        JPanel jpanTopPart = new JPanel();
        jpanTopPart.setLayout(new BoxLayout(jpanTopPart, BoxLayout.X_AXIS));

        // Left part of the top part panel.
        JPanel jpanPart1 = new JPanel();
        jpanPart1.setLayout(new BoxLayout(jpanPart1, BoxLayout.Y_AXIS));

        // Right part of the top part panel.
        JPanel jpanPart2 = new JPanel();
        jpanPart2.setLayout(new BoxLayout(jpanPart2, BoxLayout.Y_AXIS));

        // Panel for the checkbox.
        // Horizontally aligned box layout.
        JPanel jpanDigest = new JPanel();
        jpanDigest.setLayout(new BoxLayout(jpanDigest, BoxLayout.X_AXIS));
        jpanDigest.add(Box.createRigidArea(new Dimension(5, chkDigest.getHeight())));
        jpanDigest.add(chkDigest);
        jpanDigest.add(Box.createHorizontalGlue());

        // Panel for the cleave label.
        JPanel jpanCleave = new JPanel();
        jpanCleave.setLayout(new BoxLayout(jpanCleave, BoxLayout.X_AXIS));
        jpanCleave.add(Box.createHorizontalGlue());
        jpanCleave.add(new JLabel("Cleave"));
        jpanCleave.add(Box.createRigidArea(new Dimension(10, chkDigest.getHeight())));
        jpanCleave.add(txtCleave);

        // Panel for enzyme selection.
        // Horizontally aligned box layout.
        JPanel jpanEnzymes = new JPanel();
        jpanEnzymes.setLayout(new BoxLayout(jpanEnzymes, BoxLayout.X_AXIS));
        // Label for 'cmbEnzymes' combobox.
        jpanEnzymes.add(Box.createRigidArea(new Dimension(20, cmbEnzymes.getHeight())));
        jpanEnzymes.add(new JLabel("Available Enzymes"));
        jpanEnzymes.add(Box.createRigidArea(new Dimension(15, cmbEnzymes.getHeight())));
        jpanEnzymes.add(cmbEnzymes);
        jpanEnzymes.add(Box.createHorizontalGlue());

        // Panel for the restrict label.
        JPanel jpanRestrict = new JPanel();
        jpanRestrict.setLayout(new BoxLayout(jpanRestrict, BoxLayout.X_AXIS));
        jpanRestrict.add(Box.createHorizontalGlue());
        jpanRestrict.add(new JLabel("Restrict"));
        jpanRestrict.add(Box.createRigidArea(new Dimension(10, cmbEnzymes.getHeight())));
        jpanRestrict.add(txtRestrict);

        // Panel for miscleavages.
        // Horizontally aligned box layout.
        JPanel jpanMiscleavages = new JPanel();
        jpanMiscleavages.setLayout(new BoxLayout(jpanMiscleavages, BoxLayout.X_AXIS));
        // Label for the textfield.
        jpanMiscleavages.add(Box.createRigidArea(new Dimension(20, txtMiscleavage.getHeight())));
        jpanMiscleavages.add(new JLabel("Number of miscleavages"));
        jpanMiscleavages.add(Box.createRigidArea(new Dimension(15, txtMiscleavage.getHeight())));
        jpanMiscleavages.add(txtMiscleavage);
        jpanMiscleavages.add(Box.createHorizontalGlue());

        // Panel for position label.
        JPanel jpanPosition = new JPanel();
        jpanPosition.setLayout(new BoxLayout(jpanPosition, BoxLayout.X_AXIS));
        jpanPosition.add(Box.createHorizontalGlue());
        jpanPosition.add(new JLabel("Position"));
        jpanPosition.add(Box.createRigidArea(new Dimension(10, txtMiscleavage.getHeight())));
        jpanPosition.add(txtPosition);


        // Panel for the enzymefile.
        // Horizontally aligned box layout.
        JPanel jpanEnzymeFile = new JPanel();
        jpanEnzymeFile.setLayout(new BoxLayout(jpanEnzymeFile, BoxLayout.X_AXIS));
        jpanEnzymeFile.add(Box.createRigidArea(new Dimension(20, txtEnzymeFile.getHeight())));
        jpanEnzymeFile.add(new JLabel("Enzyme definition file"));
        jpanEnzymeFile.add(Box.createRigidArea(new Dimension(15, txtEnzymeFile.getHeight())));
        jpanEnzymeFile.add(txtEnzymeFile);
        jpanEnzymeFile.add(Box.createRigidArea(new Dimension(15, txtEnzymeFile.getHeight())));
        jpanEnzymeFile.add(btnChangeEnzymeFile);
        jpanEnzymeFile.add(Box.createHorizontalGlue());

        // Constructing the entire enzyme stuff section.
        jpanPart1.add(jpanDigest);
        jpanPart1.add(Box.createRigidArea(new Dimension(jpanEnzymeDigest.getWidth(), 5)));
        jpanPart1.add(jpanEnzymes);
        jpanPart1.add(Box.createRigidArea(new Dimension(jpanEnzymeDigest.getWidth(), 5)));
        jpanPart1.add(jpanMiscleavages);
        jpanPart1.setMaximumSize(jpanPart1.getPreferredSize());

        jpanPart2.add(jpanCleave);
        jpanPart2.add(Box.createRigidArea(new Dimension(jpanEnzymeDigest.getWidth(), 5)));
        jpanPart2.add(jpanRestrict);
        jpanPart2.add(Box.createRigidArea(new Dimension(jpanEnzymeDigest.getWidth(), 5)));
        jpanPart2.add(jpanPosition);
        jpanPart2.setMaximumSize(jpanPart2.getPreferredSize());

        jpanTopPart.add(jpanPart1);
        jpanTopPart.add(Box.createRigidArea(new Dimension(20, jpanPart1.getHeight())));
        jpanTopPart.add(jpanPart2);
        jpanTopPart.add(Box.createHorizontalGlue());

        jpanEnzymeDigest.add(jpanTopPart);
        jpanEnzymeDigest.add(Box.createRigidArea(new Dimension(jpanEnzymeDigest.getWidth(), 5)));
        jpanEnzymeDigest.add(jpanEnzymeFile);

        jpanMassConstraints.add(Box.createRigidArea(new Dimension(5, chkMassLimits.getHeight())));
        jpanMassConstraints.add(chkMassLimits);
        jpanMassConstraints.add(Box.createRigidArea(new Dimension(20, chkMassLimits.getHeight())));
        jpanMassConstraints.add(new JLabel(" From "));
        jpanMassConstraints.add(Box.createRigidArea(new Dimension(5, chkMassLimits.getHeight())));
        jpanMassConstraints.add(txtMinMass);
        jpanMassConstraints.add(Box.createRigidArea(new Dimension(5, chkMassLimits.getHeight())));
        jpanMassConstraints.add(new JLabel("Da  to "));
        jpanMassConstraints.add(Box.createRigidArea(new Dimension(5, chkMassLimits.getHeight())));
        jpanMassConstraints.add(txtMaxMass);
        jpanMassConstraints.add(Box.createRigidArea(new Dimension(5, chkMassLimits.getHeight())));
        jpanMassConstraints.add(new JLabel("Da"));
        jpanMassConstraints.add(Box.createHorizontalGlue());

        jpanButtons.add(Box.createHorizontalGlue());
        jpanButtons.add(btnOK);
        jpanButtons.add(btnCancel);
        jpanButtons.add(Box.createRigidArea(new Dimension(15, btnCancel.getHeight())));

        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 5)));
        panel.add(jpanEnzymeDigest);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));
        panel.add(jpanMassConstraints);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 25)));
        panel.add(jpanButtons);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 10)));


        return panel;
    }

    /**
     * This method takes care of gathering all enzymes from the enzymes file in use
     * and feeding the list of available entries and their data to the GUI components.
     */
    private void fillEnzymeList() {
        String[] contents = {"None"};
        try {
            String file = txtEnzymeFile.getText();
            InputStream inEnzymeStream = this.getClass().getClassLoader().getResourceAsStream(file);
            if(inEnzymeStream == null) {
                // Did not find it with this classloader.
                // try the system classloader instead.
                inEnzymeStream = ClassLoader.getSystemResourceAsStream(txtEnzymeFile.getText());
            }
            if(inEnzymeStream == null) {
                // Did not find it with this classloader.
                // try to locate the file as absolute path.
                File enzymeFile = new File(txtEnzymeFile.getText());
                inEnzymeStream = new FileInputStream(enzymeFile);
            }

            iReader = new MascotEnzymeReader(inEnzymeStream);
            // Get all the enzyme names.
            String[] names = iReader.getEnzymeNames();
            // Check for empty arrays.
            if(names.length == 0) {
                JOptionPane.showMessageDialog(this, new String[]{"Unable to read Enzymes in the file '" + file + "'.", "Perhaps you selected the wrong file?", "\n"}, "Unable to read Enzymes!", JOptionPane.ERROR_MESSAGE);
            } else {
                // Only display read count if the user selected a file.
                if(cmbEnzymes != null) {
                    JOptionPane.showMessageDialog(this, new String[]{"Read " + names.length + " Enzymes in file '" + file + "'.", "\n"}, "Read " + names.length + " Enzymes!", JOptionPane.PLAIN_MESSAGE);
                }
                contents = names;
            }
            // Sort them.
            Arrays.sort(contents);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        if(cmbEnzymes == null) {
            cmbEnzymes = new JComboBox(contents);
        } else {
            cmbEnzymes.setModel(new DefaultComboBoxModel(contents));
        }
        String item = (String)cmbEnzymes.getSelectedItem();
        stateChangedEnzyme(item);
        cmbEnzymes.setMaximumSize(cmbEnzymes.getPreferredSize());
    }

    /**
     * This method takes care of updating the informative components that hold an enzymes
     * operational parameters whenever the user selects a different entry in the enzymes combobox.
     *
     * @param   aSelected   String with the currently selected enzyme in the combobox.
     */
    private void stateChangedEnzyme(String aSelected) {
        if(aSelected.equalsIgnoreCase("None")) {
            txtCleave.setText("<none>");
            txtRestrict.setText("<none>");
            txtPosition.setText("<none>");
        } else {
            Enzyme enzyme = iReader.getEnzyme(aSelected);
            char[] temp = enzyme.getCleavage();
            if(temp != null) {
                txtCleave.setText(new String(temp));
            } else {
                txtCleave.setText("<none>");
            }
            temp = enzyme.getRestrict();
            if(temp != null) {
                txtRestrict.setText(new String(temp));
            } else {
                txtRestrict.setText("<none>");
            }
            txtPosition.setText((enzyme.getPosition()==Enzyme.CTERM)?"C-Terminus":"N-Terminus");
        }
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
            // Now to add additionally specifed stuff.
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
     * This method will validate the user input and construct the correct kind of ProcessThread
     * for the selected parameters.
     *
     * @return  ProcessThread   that can perform the requested task, or 'null' if the input did not
     *                          pass the validation.
     */
    private Runnable processInput() {
        Runnable pt = null;

        // Fields to gather info for.
        boolean massLimits = false;
        double minMass = 0.0;
        double maxMass = 0.0;

        Enzyme enzyme = null;
        Filter filter = null;

        int type = 0;

        // First get the mass limits, Filter & enzyme, if required.
        // Mass limits.
        if(chkMassLimits.isSelected()) {
            massLimits = true;
            try {
                minMass = Double.parseDouble(txtMinMass.getText());
                maxMass = Double.parseDouble(txtMaxMass.getText());
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "The mass limits should be specified, and they should be correctly formatted (decimal) numbers!", "Error reading mass limits!", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else {
            massLimits = false;
        }
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
        // Enzyme.
        if((chkDigest.isSelected()) && (!((String)cmbEnzymes.getSelectedItem()).equalsIgnoreCase("NONE"))) {
            enzyme = this.iReader.getEnzyme(((String)cmbEnzymes.getSelectedItem()));
            try {
                enzyme.setMiscleavages(Integer.parseInt((String)txtMiscleavage.getText()));
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "The number of allowed missed cleavages should be specified, and it should be a correctly formatted whole number!", "Error reading allowed missed cleavages!", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else {
            enzyme = null;
        }

        // See what type of processing we should do.
        if(radRagging.isSelected()) {
            // We should rag.
            // See if we should truncate, and to what size.
            boolean truncate = false;
            int truncateSize = 0;
            if(chkTruncate.isSelected()) {
                truncate = true;
                try {
                    truncateSize = Integer.parseInt(txtTruncate.getText());
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(this, "The truncation size should be specified, and it should be a correctly formatted whole number!", "Error reading truncation size!", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            int terminus = 0;
            String term = (String)cmbTerminus.getSelectedItem();
            if(term.startsWith("N")) {
                terminus = ProcessThread.NTERMINUS;
            } else if(term.startsWith("C")) {
                terminus = ProcessThread.CTERMINUS;
            }
            // Get the filename to output to.
            File file = this.getProcessOutputFile();
            if(file != null) {
                pt = ProcessThread.getRaggingTask(this.iLoader, file, this.iParent, filter, enzyme, massLimits, minMass, maxMass, terminus, truncate, truncateSize);
            }
        } else if(radSubset.isSelected()) {
            // We need to isolate a sequence-based subset.
            // First get the query String (if any, of course).
            String query = txtQuery.getText();
            if((query == null) || (query.trim().equals(""))) {
                JOptionPane.showMessageDialog(this, "You need to specify a query, upon which a subset selection will be based!", "No query found!", JOptionPane.ERROR_MESSAGE);
                return null;
            } else {
                // Query detected!
                ProteinFilter protFilter = null;
                try {
                    protFilter = new QueryParser().parseQuery(query);
                } catch(ParseException pe) {
                    JOptionPane.showMessageDialog(this, new String[]{"Your query could not be parsed!", "\n", "Parser returned the following message:", pe.getMessage(), "\n"}, "Query could not be parsed!", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                if(protFilter == null) {
                    JOptionPane.showMessageDialog(this, new String[]{"Your query could not be parsed!", "\n", "Parser did not return a message.", "\n"}, "Query could not be parsed!", JOptionPane.ERROR_MESSAGE);
                    return null;
                } else {
                    // Get the filename to output to.
                    File file = this.getProcessOutputFile();
                    if(file != null) {
                        // Create a task.
                        pt = ProcessThread.getSubsetTask(this.iLoader, file, this.iParent, filter, enzyme, massLimits, minMass, maxMass, protFilter);
                    }
                }
            }
        } else if(radDigestOnly.isSelected()) {

            if(((String)cmbEnzymes.getSelectedItem()).equalsIgnoreCase("NONE")) {
                JOptionPane.showMessageDialog(this, "Your need to select an enzyme for the digest!", "No enzyme selected!", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            // Get the filename to output to.
            File file = this.getProcessOutputFile();
            if(file != null) {
                // Create a task.
                pt = ProcessThread.getSubsetTask(this.iLoader, file, this.iParent, filter, enzyme, massLimits, minMass, maxMass, (ProteinFilter)null);
            }
        } else if(radFilterOnly.isSelected()) {
            // Get the filename to output to.
            File file = this.getProcessOutputFile();
            if(file != null) {
                // Create a task.
                pt = new FASTAOutputThread(this.iParent, this.iLoader, file, filter);
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
