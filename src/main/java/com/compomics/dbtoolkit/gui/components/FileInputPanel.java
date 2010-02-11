/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 11-okt-02
 * Time: 10:59:40
 */
package com.compomics.dbtoolkit.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a JPanel with the components and
 * logic to obtain a filename from the user.
 *
 * @author Lennart Martens
 */
public class FileInputPanel extends JPanelDataSender {

    /**
     * The JLabel that will inform the user about the file to be opened.
     */
    private JLabel lblFile = null;

    /**
     * The JTextField that will receive the user's choice.
     */
    private JTextField txtFile = null;

    /**
     * The JButton that will open up a JFileChooser for the users convenience.
     */
    private JButton btnBrowse = null;

    /**
     * This boolean indicates whether the textfield should be editable for input.
     */
    private boolean iTextFieldEditable = true;

    public static final int OPEN_DIALOG = JFileChooser.OPEN_DIALOG;
    public static final int SAVE_DIALOG = JFileChooser.SAVE_DIALOG;
    public static final int DIR_SELECT_DIALOG = Integer.MIN_VALUE;

    /**
     * Default constructor that builds the JPanel.
     */
    public FileInputPanel() {
        this(" File: ");
    }

    /**
     * This constructor requires a label that informs the user of what
     * kind of file to load (the colon is appended automatically).
     * The constructor will take care of composing and displaying all the
     * necessary GUI components.
     *
     * @param   aLabel  String with the label to show the user.
     */
    public FileInputPanel(String aLabel) {
        this(aLabel, "File selection", FileInputPanel.OPEN_DIALOG);
    }

    /**
     * This constructor requires a label that informs the user of what
     * kind of file to load (the colon is appended automatically).
     * It also takes a title for the titledborder around the inputpanel and
     * a mode for the attached filechooser.
     * The constructor will take care of composing and displaying all the
     * necessary GUI components.
     *
     * @param   aLabel  String with the label to show the user.
     * @param   aTitle  String with the title for the panel.
     * @param   aFileChooserType    int with the JFileChooser type.
     */
    public FileInputPanel(String aLabel, String aTitle, final int aFileChooserType) {
       // Check the label.
        if(!aLabel.trim().endsWith(":")) {
            aLabel = aLabel + " : ";
        }

        // The layout for this Component.
        this.setLayout(new BorderLayout(5, 5));

        // The label.
        lblFile = new JLabel(" " + aLabel);

        // The TextField.
        txtFile = new JTextField(15);

        txtFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processSubmission();
            }
        });

        // The button.
        btnBrowse = new JButton("Browse...");
        btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File startHere = new File("/");
                // Open the filechooser on the root or the folder the user
                // already specified (if it exists).
                if(!txtFile.getText().trim().equals("")) {
                    File f= new File(txtFile.getText().trim());
                    if(f.exists()) {
                        startHere = f;
                    }
                }
                JFileChooser jfc = new JFileChooser(startHere);
                int returnVal = 0;
                if(aFileChooserType == FileInputPanel.OPEN_DIALOG) {
                    jfc.setDialogType(JFileChooser.OPEN_DIALOG);
                    returnVal = jfc.showOpenDialog(txtFile);
                } else if(aFileChooserType == FileInputPanel.SAVE_DIALOG) {
                    jfc.setDialogType(JFileChooser.SAVE_DIALOG);
                    returnVal = jfc.showSaveDialog(txtFile);
                } else if(aFileChooserType == FileInputPanel.DIR_SELECT_DIALOG) {
                    jfc.setDialogType(JFileChooser.CUSTOM_DIALOG);
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    returnVal = jfc.showDialog(txtFile, "Select folder");
                } else {
                    returnVal = jfc.showDialog(txtFile, "Select");
                }
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    txtFile.setText(jfc.getSelectedFile().getAbsoluteFile().toString());
                    processSubmission();
                }
            }
        });
        btnBrowse.setMnemonic(KeyEvent.VK_B);

        // Tooltips for the components.
        if(aFileChooserType == FileInputPanel.DIR_SELECT_DIALOG) {
            txtFile.setToolTipText("Enter folder here");
            btnBrowse.setToolTipText("Select a folder");
        }
        else {
            txtFile.setToolTipText("Enter filename here + <enter>");
            btnBrowse.setToolTipText("Browse for a file");
        }

        // Adding the components.
        this.add(lblFile, BorderLayout.WEST);
        this.add(btnBrowse, BorderLayout.EAST);
        this.add(txtFile, BorderLayout.CENTER);

        // Adding a Border.
        this.setBorder(BorderFactory.createTitledBorder(aTitle));
    }

    /**
     * This method allows the caller to enable/disable the component
     * in full in one method call.
     *
     * @param   aEnabled    boolean that indicates whether the component
     *                      should be enabled.
     */
    public void setEnabled(boolean aEnabled) {
        super.setEnabled(aEnabled);
        this.txtFile.setEnabled(aEnabled);
        this.lblFile.setEnabled(aEnabled);
        this.btnBrowse.setEnabled(aEnabled);
    }

    /**
     * This method allows the caller to set the textfield on the component
     * editable or not.
     *
     * @param   aEditable   boolean to indicate whether the textfield on the component
     *                      should be editable.
     */
    public void textFieldEditable(boolean aEditable) {
        this.txtFile.setEditable(aEditable);
    }

    /**
     * This method allows the caller to set a text on the textfield.
     *
     * @param   aText   String with the text to set.
     */
    public void setTextFieldText(String aText) {
        this.txtFile.setText(aText);
    }

    /**
     * This method is called whenever the user acknowledged a filename.
     */
    private void processSubmission() {
        super.notifyReceivers(txtFile.getText());
    }
}
