/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 22-okt-02
 * Time: 11:02:53
 */
package com.compomics.dbtoolkit.gui.components;

import com.compomics.dbtoolkit.gui.interfaces.GUIDataReceiver;
import com.compomics.dbtoolkit.gui.workerthreads.ClearRedundancyThread;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the GUI for collecting all information to clear the
 * redundancy from a database.
 *
 * @author Lennart Martens
 */
public class ClearRedundancyDialog extends JDialog implements GUIDataReceiver {

    /**
     * The DBLoader to load from.
     */
    private DBLoader iLoader = null;

    /**
     * AutoDBLoader for the processing thread.
     */
    private AutoDBLoader iAuto = null;

    /**
     * The output file.
     */
    private File iOutput = null;

    /**
     * The temp storage folder.
     */
    private File iTempFolder = null;

    /**
     * Parent JFrame for this JDialog.
     */
    private JFrame iParent = null;

    /**
     * FileInputPanel for the temp folder.
     */
    private FileInputPanel top = null;

    /**
     * FileInputPanel for the output file.
     */
    private FileInputPanel middle = null;

    /**
     * This constructor takes a parent JFrame, a title and a DBLoader as parameters.
     * It takes care of constructing and laying out the GUI, setting the dialog visible is
     * up to the caller.
     *
     * @param   aParent JFrame with the parent for this dialog.
     * @param   aTitle  String with the title for the dialog.
     * @param   aLoader DBLoader to load the database from.
     * @param   aAuto    AutoDBLoader to automatically decide DB type from known DBLoaders.
     */
    public ClearRedundancyDialog(JFrame aParent, String aTitle, DBLoader aLoader, AutoDBLoader aAuto) {
        super(aParent, aTitle, true);
        this.iParent = aParent;
        this.iLoader = aLoader;
        this.iAuto = aAuto;

        this.addWindowListener(new WindowAdapter() {
            /**
             * Invoked whent a window is in the process of being closed.
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
     * This method will be invoked by the monitored component whenever
     * it is deemed necessary by this component (e.g. : when the data
     * entered on the component is submitted).
     *
     * @param   aSource the Component issuing the call.
     * @param   aData   Object with the data the Component wishes to transmit.
     */
    public void transmitData(Component aSource, Object aData) {
        if(aSource == top) {
            this.iTempFolder = new File((String)aData);
        } else if(aSource == middle) {
            this.iOutput = new File((String)aData);
        }
    }

    /**
     * This method will assemble and lay-out the GUI components on the screen.
     */
    private void constructScreen() {
        // The components.
        // The main panel.
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        // The tempfolder fileinputpanel.
        top = new FileInputPanel("Specify temporary storage folder here", "Temporary folder", FileInputPanel.DIR_SELECT_DIALOG);
        top.textFieldEditable(false);
        top.addReceiver(this);

        // The outputfile fileinputpanel.
        middle = new FileInputPanel("Specify output file here", "Output file", FileInputPanel.SAVE_DIALOG);
        middle.textFieldEditable(false);
        middle.addReceiver(this);

        // Get the buttonpanel.
        JPanel buttons = this.getButtonPanel();

        // Lay-out.
        main.add(Box.createRigidArea(new Dimension(top.getWidth(), 10)));
        main.add(top);
        main.add(Box.createRigidArea(new Dimension(top.getWidth(), 5)));
        main.add(middle);
        main.add(Box.createRigidArea(new Dimension(top.getWidth(), 20)));
        main.add(buttons);

        this.getContentPane().add(main, BorderLayout.CENTER);
        this.pack();
    }

    /**
     * This method closes the dialog.
     */
    private void close() {
        this.setVisible(false);
        this.dispose();
    }

    /**
     * This method is called when the user presses the OK button.
     */
    private void okPressed() {
        // Okay, see if we have files and whether they exist.
        if(this.iTempFolder == null) {
            JOptionPane.showMessageDialog(this, "You need to select a temporary storage folder!", "No temporary folder selected!", JOptionPane.ERROR_MESSAGE);
            return;
        } else if(!this.iTempFolder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "The temporary storage folder has to be a directory!", "Temporary folder selected!", JOptionPane.ERROR_MESSAGE);
            return;
        } else if(this.iOutput == null) {
            JOptionPane.showMessageDialog(this, "You need to select an output file!", "No output file selected!", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            // Okay, the folder and file are selected. See if they exist!
            if(!iTempFolder.exists()) {
                JOptionPane.showMessageDialog(this, new String[] {"The temporary storage folder does not exist!", "Please create the folder before procedding!"}, "Temporary folder does not exist!", JOptionPane.ERROR_MESSAGE);
                return;
            } else if(iOutput.exists()) {
                int choice = JOptionPane.showConfirmDialog(this, new String[] {"The output file already exists!", "Do you wish to delete all information in file '" + iOutput.getAbsolutePath() + "'?"}, "Output file already exists!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(choice == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            // Okay, all information is present.
            // Proceed!
            ClearRedundancyThread crd = new ClearRedundancyThread(iTempFolder, iOutput, iLoader, iAuto, iParent);
            Thread t = new Thread(crd);
            t.start();

        }

        close();
    }

    /**
     * This method constructs the buttonpanel.
     *
     * @return  JPanel  with the buttons.
     */
    private JPanel getButtonPanel() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        JButton okButton = new JButton("OK");
        okButton.setMnemonic(KeyEvent.VK_O);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        // Max sizes.
        okButton.setMaximumSize(okButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());

        // Lay-out.
        buttons.add(Box.createHorizontalGlue());
        buttons.add(okButton);
        buttons.add(cancelButton);
        buttons.add(Box.createRigidArea(new Dimension(15, okButton.getHeight())));

        return buttons;
    }
}
