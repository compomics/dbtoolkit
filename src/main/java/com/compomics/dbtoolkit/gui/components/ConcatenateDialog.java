/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 21-okt-02
 * Time: 14:47:17
 */
package com.compomics.dbtoolkit.gui.components;

import com.compomics.dbtoolkit.gui.interfaces.GUIDataReceiver;
import com.compomics.dbtoolkit.gui.workerthreads.ConcatenateThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a dialog that gathers the information required to
 * concatenate two files.
 *
 * @author Lennart Martens.
 */
public class ConcatenateDialog extends JDialog implements GUIDataReceiver {

    /**
     * The parent frame.
     */
    private JFrame iParent = null;

    /**
     * The current file (if any).
     */
    private String iCurrentFile = null;

    /**
     * Top fileinputpanel (from-file).
     */
    private FileInputPanel top = null;

    /**
     * Middle fileinputpanel (to-file);
     */
    private FileInputPanel middle = null;

    /**
     * Bottom fileinputpanel (to-file);
     */
    private FileInputPanel bottom = null;


    /**
     * The filename for the first file from which to read.
     */
    private String iFrom1 = null;

    /**
     * The filename for the second file from which to read.
     */
    private String iFrom2 = null;

    /**
     * The filename for the file to which to append.
     */
    private String iToAppend = null;


    /**
     * This constructor takes arguments realting to GUI as well as to logic.
     *
     * @param   aParent JFrame that acts as the parent for this Dialog.
     * @param   aTitle  String with the title for the dialog window.
     * @param   aCurrentFile    String with the currently loaded file. This can be 'null', in
     *                          which case the 'current' button will be disabled.
     */
    public ConcatenateDialog(JFrame aParent, String aTitle, String aCurrentFile) {
        super(aParent, aTitle, true);
        this.iParent = aParent;
        this.iCurrentFile = aCurrentFile;

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
     * This method will be invoked by the monitored component whenever
     * it is deemed necessary by this component (e.g. : when the data
     * entered on the component is submitted).
     *
     * @param   aSource the Component issuing the call.
     * @param   aData   Object with the data the Component wishes to transmit.
     */
    public void transmitData(Component aSource, Object aData) {
        if(aSource == top) {
            this.iFrom1 = (String)aData;
            middle.setEnabled(true);
        } else if(aSource == middle) {
            this.iFrom2 = (String)aData;
        } else if(aSource == bottom) {
            this.iToAppend = (String)aData;
        }
    }

    /**
     * This method constructs the GUI and attaches listeners and logic to the functional parts.
     */
    private void constructScreen() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        top = new FileInputPanel("Select first file to append", "First file to append (or file to copy)", JFileChooser.OPEN_DIALOG);
        top.addReceiver(this);
        top.textFieldEditable(false);

        middle = new FileInputPanel("Select second file to append", "Second file to append", JFileChooser.OPEN_DIALOG);
        middle.addReceiver(this);
        middle.setEnabled(false);
        middle.textFieldEditable(false);

        bottom = new FileInputPanel("Select output file", "Output file", JFileChooser.SAVE_DIALOG);
        bottom.addReceiver(this);
        bottom.textFieldEditable(false);

        JPanel buttons = this.getButtonPanel();

        main.add(Box.createRigidArea(new Dimension(top.getWidth(), 10)));
        main.add(top);
        main.add(Box.createRigidArea(new Dimension(top.getWidth(), 5)));
        main.add(middle);
        main.add(Box.createRigidArea(new Dimension(top.getWidth(), 5)));
        main.add(bottom);
        main.add(Box.createRigidArea(new Dimension(top.getWidth(), 20)));
        main.add(buttons);

        this.getContentPane().add(main, BorderLayout.CENTER);
        this.pack();
    }

    private JPanel getButtonPanel() {
        // The components.
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
        JButton okButton = new JButton("OK");
        okButton.setMnemonic(KeyEvent.VK_O);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic(KeyEvent.VK_C);

        // Button actionlisteners.
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });

        // Maximum sizes.
        okButton.setMaximumSize(okButton.getPreferredSize());
        cancelButton.setMaximumSize(cancelButton.getPreferredSize());

        // Lay-out of components.
        result.add(Box.createHorizontalGlue());
        result.add(okButton);
        result.add(cancelButton);
        result.add(Box.createRigidArea(new Dimension(15, cancelButton.getHeight())));

        // Et voila!
        return result;
    }

    /**
     * This method is called when the OK button is pressed.
     */
    private void okPressed() {
        File in1 = null;
        File in2 = null;
        File out = null;

        // First check whether we actually have something to work with.
        if(iFrom1 == null) {
            JOptionPane.showMessageDialog(this, "You need to specify at least one file to read from!", "No input file specified!", JOptionPane.ERROR_MESSAGE);
            return;
        } else if(iToAppend == null) {
            JOptionPane.showMessageDialog(this, "You need to specify a file to append to!", "No output file specified!", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            // Okay, we have one file to read from, one to output to.
            // See if they exist (or can be made).
            in1 = new File(iFrom1);
            out = new File(iToAppend);
            if(!in1.exists()) {
                JOptionPane.showMessageDialog(this, "The input file you specified (" + iFrom1 + ") can not be found!", "Input file can not be found!", JOptionPane.ERROR_MESSAGE);
                return;
            } else{
                // Okay, the input file is accessible.
                boolean outputExisted = false;
                try {
                    if(!out.exists()) {
                        out.createNewFile();
                    } else {
                        outputExisted = true;
                    }
                } catch(IOException ioe) {
                    JOptionPane.showMessageDialog(this, "The output file you specified (" + iToAppend + ") can not be created!", "Output file can not be created!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // At this point, we have an existing input1 and a created (or existing, whatever) output.
                // Let's see if we have a second input.
                if(this.iFrom2 != null) {
                    // There is a second input. Handle it.
                    in2 = new File(iFrom2);
                    if(in2.exists()) {
                        // We need to concatenate.
                        String thirdLine = null;
                        if(outputExisted) {
                            thirdLine = "(All information in '" + iToAppend + "' will be lost!)";
                        } else {
                            thirdLine = "(Outputting in '" + iToAppend + "'.)";
                        }
                        int reply = JOptionPane.showConfirmDialog(this, new String[]{"You specified two input files!", "Do you wish to concatenate these files into the destination file?", thirdLine}, "Confirm concatenation.", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if(reply == JOptionPane.OK_OPTION) {
                            // Okay, run da thing.
                            ConcatenateThread ct = new ConcatenateThread(in1, in2, out, this.iParent);
                            Thread t = new Thread(ct);
                            t.start();
                            this.close();
                        } else {
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "The second input file you specified (" + iFrom2 + ") can not be found!", "Second input file can not be found!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    // There isn't a second input. Ask about copying.
                    String thirdLine = null;
                    if(outputExisted) {
                        thirdLine = "(All information in '" + iToAppend + "' will be lost!)";
                    } else {
                        thirdLine = "(Outputting in '" + iToAppend + "'.)";
                    }
                    int reply = JOptionPane.showConfirmDialog(this, new String[]{"You specified only one input file!", "Do you wish to copy this file into the destination file?", thirdLine}, "Confirm copy.", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if(reply == JOptionPane.OK_OPTION) {
                        // Okay, run da thing.
                        ConcatenateThread ct = new ConcatenateThread(in1, out, this.iParent);
                        Thread t = new Thread(ct);
                        t.start();
                        this.close();
                    } else {
                        return;
                    }
                }
            }

        }
    }

    /**
     * This method is called when the Cancel button is pressed.
     */
    private void cancelPressed() {
        this.close();
    }

    /**
     * This method closes the dialog.
     */
    private void close() {
        this.setVisible(false);
        this.dispose();
    }
}
