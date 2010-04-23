/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 13-okt-02
 * Time: 15:37:08
 */
package com.compomics.dbtoolkit.gui;

import com.compomics.dbtoolkit.gui.components.*;
import com.compomics.dbtoolkit.gui.interfaces.GUIDataReceiver;
import com.compomics.dbtoolkit.gui.workerthreads.CounterThread;
import com.compomics.dbtoolkit.gui.workerthreads.FASTAOutputThread;
import com.compomics.dbtoolkit.gui.workerthreads.ShuffleDBThread;
import com.compomics.dbtoolkit.io.DBLoaderFactory;
import com.compomics.dbtoolkit.io.UnknownDBFormatException;
import com.compomics.dbtoolkit.io.implementations.AutoDBLoader;
import com.compomics.dbtoolkit.io.interfaces.DBLoader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.text.ParseException;

/*
 * CVS information:
 *
 * $Revision: 1.7 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a Controller for the DBTool GUI.
 *
 * @author Lennart Martens.
 */
public class DBToolController implements GUIDataReceiver {

    /**
     * Reference to the view for the DBTool GUI.
     */
    private DBTool iView = null;

    /**
     * Reference to the model for the DBTool GUI.
     */
    private DBToolModel iModel = null;

    /**
     * The constructor for the controller requires a view to passed in.
     *
     * @param   aView   DBTool with the view for the DBTool GUI.
     */
    public DBToolController(DBTool aView) {
        this.iView = aView;
        this.iModel = new DBToolModel();
    }

    /**
     * This method is called by the view whenever the
     * user tried to trigger an exit operation.
     */
    void exitTriggered() {
        iView.dispose();
        iModel = null;
        System.exit(0);
    }

    /**
     * This method reports (via the model) on the number of entries to
     * display in the preview pane.
     *
     * @return  int with the number of entries.
     */
    int getNrOfEntries() {
        return this.iModel.getNrOfEntries();
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
        if(aSource == iView.fip) {
            File file = new File((String)aData);
            if(file.exists()) {
                iView.status.setError("None");
                iView.status.setStatus("Processing incoming file ('" + aData + "')...");
                this.processIncomingFile((String)aData);
            } else {
                this.showError("File '" + aData + "' was not found!", "File not found!");
                this.clearGUI();
                this.iModel.clear();
            }
        }
    }

    /**
     * In this method, we will check for a DBLoader that's present, and
     * run though all the entries, processing the data.
     * If the Loader is not set, we display a warning and return.
     */
    public void processDataTriggered() {
        if(iModel.getLoader() != null) {
            ProcessDialog pd = new ProcessDialog(iView, "Process data options", iModel.getLoader(), iModel.getLoader().getDBName());
            pd.setLocation(iView.getPoint(4, 4));
            pd.setVisible(true);
        } else {
            showError("You need to load a database first!", "No DB file loaded!");
        }
    }

    /**
     * In this method, we will check for a DBLoader that's present, and
     * run though all the entries, filtering for a given regular expression.
     * If the Loader is not set, we display a warning and return.
     */
    public void processRegExpFilterTriggered() {
        if(iModel.getLoader() != null) {
            RegularExpressionFilterDialog refd = new RegularExpressionFilterDialog(iView, "Regular expression subset selection", iModel.getLoader(), iModel.getLoader().getDBName());
            refd.setLocation(iView.getPoint(4, 4));
            refd.setVisible(true);
        } else {
            showError("You need to load a database first!", "No DB file loaded!");
        }
    }

    /**
     * In this method, we will check for a DBLoader that's present, and
     * run though all the entries, mapping the peptides.
     * If the Loader is not set, we display a warning and return.
     */
    public void processPeptideMappingTriggered() {
        if(iModel.getLoader() != null) {
            PeptideMappingDialog pmd = new PeptideMappingDialog(iView, "Peptide mapping options", iModel.getLoader(), iModel.getLoader().getDBName());
            pmd.setLocation(iView.getPoint(4, 4));
            pmd.setVisible(true);
        } else {
            showError("You need to load a database first!", "No DB file loaded!");
        }
    }

    /**
     * This method is called when the user wants to copy or concat a set of files.
     */
    public void processConcatTriggered() {
        ConcatenateDialog cd = new ConcatenateDialog(this.iView, "Concatenate (or copy - when you select 1 input file)", null);
        cd.setLocation(iView.getPoint(4, 4));
        cd.setVisible(true);
    }

    /**
     * This method is triggered when the user indicates in the view that she
     * wants to change the number of entries in the preview.
     * It opens a dialog for the user to specify the number of lines to
     * preview in the previewPane.
     */
    public void requestFASTALines() {
        // Cycle boolean - continu asking for input until it's something decent.
        boolean lbOK = false;

        // Keep bugging the user 'till he gives up or specifies something sensible!
        while(!lbOK) {
            String reply = JOptionPane.showInputDialog(iView, "Please specify the number of FASTA entries to display in the preview area.", "Preview area settings", JOptionPane.QUESTION_MESSAGE);
            try {
                // If the user pressed cancel, we just leave.
                if((reply == null) || (reply.trim().equals(""))) {
                    break;
                }
                int nrOfEntries = Integer.parseInt(reply);
                // Set the new data on the model.
                iModel.setNrOfEntries(nrOfEntries);
                TitledBorder tbord = (TitledBorder)iView.previewPane.getBorder();
                tbord.setTitle("Preview pane (" + nrOfEntries + " FASTA entries)");
                this.prepareAndDisplayPreviewText();
                iView.previewPane.repaint();
                lbOK = true;
            } catch(NumberFormatException nfe) {
                JOptionPane.showMessageDialog(iView, "You must enter a number here!", "Invalid response!", JOptionPane.ERROR_MESSAGE);
            } catch(IOException ioe) {
                this.showError("Unable to read '" + iModel.getLoader().getDBName() + "' formatted file '" + iModel.getFilename() + "'!", "Unable to read preview!");
            }
        }
    }



    /**
     * This method prompts a dialog for the user, showing the error.
     * It also logs the error on the status bar.
     */
    private void showError(String aMessage, String aTitle) {
        JOptionPane.showMessageDialog(iView, aMessage, aTitle, JOptionPane.ERROR_MESSAGE);
        iView.status.setError(aMessage);
    }

    /**
     * This method will process an incoming file,
     * initializing all relevant fields if successfull.
     *
     * @param   aFilename   String with the filename to process.
     */
    private void processIncomingFile(String aFilename) {
        try {
            // first close the previous loader if it is still open.
            if(iModel.getLoader() != null) {
                iModel.getLoader().close();
                iModel.setLoader(null);
            }

            AutoDBLoader auto = new AutoDBLoader(this.iModel.getAllDBLoaderClassNames());
            DBLoader loader = null;

            try {
                loader = auto.getLoaderForFile(aFilename);
            } catch(UnknownDBFormatException udfe) {
                // Ask the user for the DB format.
                String choice = (String)JOptionPane.showInputDialog(iView, new String[]{"DB format could not be detected automatically!", "Please select the correct format manually.", "\n"}, "DB format unknown!", JOptionPane.QUESTION_MESSAGE, null, this.iModel.getDBNames(), DBLoader.FASTA);
                // If a format was specified, ...
                if(choice != null) {
                    // ...try retrieve the correct loader for the format...
                    loader = null;

                    // first from the factory...
                    try {
                        loader = DBLoaderFactory.getDBLoader(choice);
                    } catch(UnknownDBFormatException udfeIntern) {
                        // Okay, see if we can load it by hand.
                        String className = this.iModel.getDBLoaderClassName(choice);
                        if(className != null) {
                            try {
                                Class c = Class.forName(className);
                                Constructor constr = c.getConstructor(new Class[]{});
                                Object o = constr.newInstance(new Object[]{});
                                if(o instanceof DBLoader) {
                                    loader = (DBLoader)o;
                                }
                            } catch(Exception e) {
                                this.showError("DBLoader for database format '" + choice + "' could not be found!", "Invalid DB format!");
                                iModel.clear();
                                iView.status.setStatus("No file loaded.");
                            }
                        }
                        // See if we now have a loader.
                        if(loader == null) {
                            throw udfeIntern;
                        }
                    }
                    // Check to see if we've found a loader.
                    if(loader == null) {
                        throw new UnknownDBFormatException(choice);
                    }
                    // ... and load the DB.
                    loader.load(aFilename);
                }
            }
            if(loader != null) {
                String name = loader.getDBName();
                iModel.setFilename(aFilename);
                iModel.setLoader(loader);
                // Display the preview (after formatting).
                this.prepareAndDisplayPreviewText();
                iView.status.setStatus(name + " formatted file '" + iModel.getFilename() + "' loaded.");
            } else {
                iView.status.setStatus("No file loaded.");
                iModel.clear();
                iView.clear();
            }
        } catch(IOException ioe) {
            this.showError("File could not be read: " + ioe.getMessage(), "Unable to read file!");
            iModel.clear();
            this.clearGUI();
        } catch(UnknownDBFormatException udfe) {
            this.showError("Database format was invalid for file '" + iModel.getFilename() + "'!", "Invalid DB format!");
            iModel.clear();
            this.clearGUI();
        }
    }

    /**
     * This method prepares and sets the preview text.
     */
    public void prepareAndDisplayPreviewText() throws IOException {
        String previewText = "";

        previewText = iModel.getPreviewText(iView.getNrOfCharsOnPreviewPane());

        iView.preview.setText(previewText);
        iView.preview.setCaretPosition(0);
    }

    /**
     * This method will attempt to count the number of entries in the DB.
     */
    public void countRequested() {
        if(iModel.getFilename() != null) {
            CounterThread ct = new CounterThread(iView, iModel.getLoader(), iModel.getFilename());
            new Thread(ct).start();
        } else {
            this.showError("You need to load a DB file first!", "No DB file loaded!");
        }
    }

    /**
     * This method is called when the view is being resized.
     */
    public void triggerResized() {
        try {
            this.prepareAndDisplayPreviewText();
        } catch(IOException ioe) {
            this.showError("Unable to retrieve preview for '" + iModel.getLoader().getDBName() + "' formatted file '" + iModel.getFilename() + "!", "Preview unavailable!");
        }
    }

    /**
     * This method is called by the view when an output to a FASTA DB is requested.
     */
    public void FASTAOutputRequested() {
        // First see if we have a DB loaded.
        if(iModel.getFilename() == null) {
            this.showError("You need to load a DB file first!", "No database loaded!");
        } else {
            // Okay, we have something to output.
            // Request an ouput filename.
            boolean lbContinue = true;
            File file = new File("/");
            while(lbContinue) {
                JFileChooser jfc = new JFileChooser(file);
                jfc.setDialogType(JFileChooser.SAVE_DIALOG);
                jfc.setDialogTitle("Save as");
                int returnVal = jfc.showSaveDialog(iView);
                String result = null;
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    result = jfc.getSelectedFile().getAbsoluteFile().toString();
                }
                if(result == null) {
                    return;
                }
                file = new File(result);
                if(file.exists()) {
                    int answer = JOptionPane.showConfirmDialog(iView, new String[] {"The file '" + file.getAbsoluteFile() + "' already exists!", "Do you want to overwrite?"}, "File already exists!", JOptionPane.YES_NO_OPTION);
                    if(answer == JOptionPane.OK_OPTION) {
                        lbContinue = false;
                    }
                } else {
                    try {
                        file.createNewFile();
                        lbContinue = false;
                    } catch(IOException ioe) {
                        JOptionPane.showMessageDialog(iView, "Unable to create the file '" + file.getAbsoluteFile() + "'!", "Unable to create file!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            try {
                DBLoader loader = iModel.getLoader();

                FASTAOutputThread fot = new FASTAOutputThread(iView, loader, file);
                Thread t = new Thread(fot);
                t.start();
            } catch(Exception ioe) {
                this.showError("Error occurred while attempting to write to DB to FASTA file: '" + ioe.getMessage() + "'", "Unable to create FASTA file!");
            }
        }
    }

    public void replacedOutputRequested() {
        /**
         * This method is called by the view when a FASTA DB with replaced residues is requested.
         */
        // First see if we have a DB loaded.
        if(iModel.getFilename() == null) {
            this.showError("You need to load a DB file first!", "No database loaded!");
        } else {
            // Okay, we have something to output.
            // First obtain a substitution String.
            boolean lbContinue = true;
            HashMap substitutions = null;
            while(lbContinue) {
                String substString = (String)JOptionPane.showInputDialog(iView, new String[] {"Please specify a substitution set", "Example:", "     I,L=1;K,Q=2", "To replace 'I' and 'L' with '1', and 'K' and 'Q' with '2'."}, "Substitution set.", JOptionPane.QUESTION_MESSAGE);
                if( (substString == null) || (substString.trim().equals(""))) {
                    return;
                }
                // Attempt to parse the String.
                try {
                    substitutions = FASTAOutputThread.parseSubstitutions(substString.trim());
                    lbContinue = false;
                } catch(ParseException pe) {
                    JOptionPane.showMessageDialog(iView, new String[]{"Unable to parse the substitution set: " + substString, "Error: " + pe.getMessage(), "At position: " + pe.getErrorOffset() + "."}, "Parsing error!", JOptionPane.WARNING_MESSAGE);
                }
            }
            lbContinue = true;
            // Request an ouput filename.
            File file = new File("/");
            while(lbContinue) {
                JFileChooser jfc = new JFileChooser(file);
                jfc.setDialogType(JFileChooser.SAVE_DIALOG);
                jfc.setDialogTitle("Save as");
                String result = null;
                int returnVal = jfc.showSaveDialog(iView);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    result = jfc.getSelectedFile().getAbsoluteFile().toString();
                }
                if(result == null) {
                    return;
                }
                file = new File(result);
                if(file.exists()) {
                    int answer = JOptionPane.showConfirmDialog(iView, new String[] {"The file '" + file.getAbsoluteFile() + "' already exists!", "Do you want to overwrite?"}, "File already exists!", JOptionPane.YES_NO_OPTION);
                    if(answer == JOptionPane.OK_OPTION) {
                        lbContinue = false;
                    }
                } else {
                    try {
                        file.createNewFile();
                        lbContinue = false;
                    } catch(IOException ioe) {
                        JOptionPane.showMessageDialog(iView, "Unable to create the file '" + file.getAbsoluteFile() + "'!", "Unable to create file!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            try {
                System.out.println("Should be writing to '" + file.getAbsolutePath() + "'.");

                DBLoader loader = iModel.getLoader();

                FASTAOutputThread fot = new FASTAOutputThread(iView, loader, file, substitutions);
                Thread t = new Thread(fot);
                t.start();
            } catch(Exception ioe) {
                this.showError("Error occurred while attempting to write to DB to FASTA file: '" + ioe.getMessage() + "'", "Unable to create FASTA file!");
            }
        }
    }

    /**
     * This method is called by the view when a reversed FASTA DB is requested.
     */
    public void reversedOutputRequested() {
        // First see if we have a DB loaded.
        if(iModel.getFilename() == null) {
            this.showError("You need to load a DB file first!", "No database loaded!");
        } else {
            // Okay, we have something to output.
            // Request an ouput filename.
            boolean lbContinue = true;
            File file = new File("/");
            while(lbContinue) {
                JFileChooser jfc = new JFileChooser(file);
                jfc.setDialogType(JFileChooser.SAVE_DIALOG);
                jfc.setDialogTitle("Save as");
                String result = null;
                int returnVal = jfc.showSaveDialog(iView);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    result = jfc.getSelectedFile().getAbsoluteFile().toString();
                }
                if(result == null) {
                    return;
                }
                file = new File(result);
                if(file.exists()) {
                    int answer = JOptionPane.showConfirmDialog(iView, new String[] {"The file '" + file.getAbsoluteFile() + "' already exists!", "Do you want to overwrite?"}, "File already exists!", JOptionPane.YES_NO_OPTION);
                    if(answer == JOptionPane.OK_OPTION) {
                        lbContinue = false;
                    }
                } else {
                    try {
                        file.createNewFile();
                        lbContinue = false;
                    } catch(IOException ioe) {
                        JOptionPane.showMessageDialog(iView, "Unable to create the file '" + file.getAbsoluteFile() + "'!", "Unable to create file!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            try {
                DBLoader loader = iModel.getLoader();

                ShuffleDBThread sdt = new ShuffleDBThread(loader, file, ShuffleDBThread.REVERSE, iView);
                Thread t = new Thread(sdt);
                t.start();
            } catch(Exception ioe) {
                this.showError("Error occurred while attempting to write the DB to reversed FASTA file: '" + ioe.getMessage() + "'", "Unable to create reversed FASTA file!");
            }
        }
    }

    /**
     * This method is called by the view when a randomized FASTA DB is requested.
     */
    public void shuffleOutputRequested() {
        // First see if we have a DB loaded.
        if(iModel.getFilename() == null) {
            this.showError("You need to load a DB file first!", "No database loaded!");
        } else {
            // Okay, we have something to output.
            // Request an ouput filename.
            boolean lbContinue = true;
            File file = new File("/");
            while(lbContinue) {
                JFileChooser jfc = new JFileChooser(file);
                jfc.setDialogType(JFileChooser.SAVE_DIALOG);
                jfc.setDialogTitle("Save as");
                String result = null;
                int returnVal = jfc.showSaveDialog(iView);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    result = jfc.getSelectedFile().getAbsoluteFile().toString();
                }
                if(result == null) {
                    return;
                }
                file = new File(result);
                if(file.exists()) {
                    int answer = JOptionPane.showConfirmDialog(iView, new String[] {"The file '" + file.getAbsoluteFile() + "' already exists!", "Do you want to overwrite?"}, "File already exists!", JOptionPane.YES_NO_OPTION);
                    if(answer == JOptionPane.OK_OPTION) {
                        lbContinue = false;
                    }
                } else {
                    try {
                        file.createNewFile();
                        lbContinue = false;
                    } catch(IOException ioe) {
                        JOptionPane.showMessageDialog(iView, "Unable to create the file '" + file.getAbsoluteFile() + "'!", "Unable to create file!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            try {
                DBLoader loader = iModel.getLoader();

                ShuffleDBThread sdt = new ShuffleDBThread(loader, file, ShuffleDBThread.SHUFFLE, iView);
                Thread t = new Thread(sdt);
                t.start();
            } catch(Exception ioe) {
                this.showError("Error occurred while attempting to write the DB to reversed FASTA file: '" + ioe.getMessage() + "'", "Unable to create reversed FASTA file!");
            }
        }
    }

    /**
     * This method is called when the user desires a cleaning of the redundancy in the database.
     */
    public void processClearRedTriggered() {
        if(iModel.getFilename() == null) {
            this.showError("You need to load a DB file first!", "No database loaded!");
        } else {
            ClearRedundancyDialog crd = new ClearRedundancyDialog(this.iView, "Clear redundancy in DB", this.iModel.getLoader(), this.iModel.getAutoLoader());
            crd.setLocation(iView.getPoint(4, 4));
            crd.setVisible(true);
        }
    }

    /**
     * This method clears the GUI.
     */
    public void clearGUI() {
        this.iView.clear();
    }

    /**
     * This method shows the about dialog.
     */
    public void showAbout() {
        AboutDialog ad = new AboutDialog(this.iView, "About DBToolKit");
        ad.setLocation(this.iView.getPoint(2, 2));
        ad.setVisible(true);
    }
}
