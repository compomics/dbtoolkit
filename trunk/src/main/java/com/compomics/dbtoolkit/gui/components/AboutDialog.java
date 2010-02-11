/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 30-okt-02
 * Time: 9:27:19
 */
package com.compomics.dbtoolkit.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements a rudimentary 'about' dialog that gets the text for the main
 * TextArea from a simple textfile ('<i>about.txt</i>' is default name) in the classpath somewhere.
 *
 * @author Lennart Martens
 */
public class AboutDialog extends JDialog {

    /**
     * The textarea that will display the help text.
     */
    private JTextArea txtHelp = null;

    /**
     * This button can be pressed to exit the dialog.
     */
    private JButton btnOK = null;

    /**
     * The labels with information on top.
     */
    private JLabel[] lblLabels = null;

    /**
     * The label with the toolkit image icon.
     */
    private static JLabel lblImageTools = null;

    /**
     * The label with the RUG image icon.
     */
    private static JLabel lblImageRUG = null;


    /**
     * The ImageIcon with the RUG image icon.
     */
    private static ImageIcon iRug = null;


    /**
     * The helptext to display in the textarea.
     */
    private static String iHelpText = null;

    /**
     * The name for the textfile.
     */
    private static final String TEXTFILE = "about.txt";


    /**
     * This constructor mimics the constructor on the superclass and allows
     * specification of the parent JFrame as well as the title for the dialog.
     * Note that about dialog is always modal!
     *
     * @param   aParent JFrame that is the parent of this dialog.
     * @param   aTitle  String with the title for this dialog.
     */
    public AboutDialog(JFrame aParent, String aTitle) {
        super(aParent, aTitle, true);

        // See if we should load the display text.
        if(iHelpText == null) {
            this.loadHelpText();
        }

        // See if we should load the imagelabel and icon.
        if((lblImageTools == null) || (iRug == null)) {
            this.loadImages();
        }

        this.constructScreen();
    }

    /**
     * This method constructs all components and lays them out on the screen.
     */
    private void constructScreen() {
        // Components.
        // The textarea.
        txtHelp = new JTextArea(15, 50);
        txtHelp.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtHelp.setText(iHelpText);
        txtHelp.setCaretPosition(0);
        txtHelp.setEditable(false);

        // The OK button.
        btnOK = new JButton("OK");
        btnOK.setMnemonic(KeyEvent.VK_O);
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        // The labels.
        lblLabels = new JLabel[8];

        lblLabels[0] = new JLabel("DBToolKit program version " + this.getLastVersion());
        lblLabels[1] = new JLabel(" ");
        lblLabels[2] = new JLabel("Lennart Martens (lennart.martens@UGent.be)");
        lblLabels[3] = new JLabel("          Ghent University (www.UGent.be)");
        lblLabels[4] = new JLabel("          FWO Flanders (www.fwo.be)");
        lblLabels[5] = new JLabel(" ");
        lblLabels[6] = new JLabel("This application is freeware and open source (Apache2 license)");
        lblLabels[7] = new JLabel(" ");

        // The containers.
        // Main panel.
        JPanel jpanMain = new JPanel();
        jpanMain.setLayout(new BoxLayout(jpanMain, BoxLayout.Y_AXIS));

        // Button panel.
        JPanel jpanButton = new JPanel();
        jpanButton.setLayout(new BoxLayout(jpanButton, BoxLayout.X_AXIS));

        // Label panel.
        JPanel jpanLabels = new JPanel();
        jpanLabels.setLayout(new BoxLayout(jpanLabels, BoxLayout.X_AXIS));

        // Textlabels panel.
        JPanel jpanTextLabels = new JPanel();
        jpanTextLabels.setLayout(new BoxLayout(jpanTextLabels, BoxLayout.Y_AXIS));

        // Scrollpane for textarea + panel for scrollpane.
        JScrollPane jspText = new JScrollPane(txtHelp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel jpanScroll = new JPanel();
        jpanScroll.setLayout(new BoxLayout(jpanScroll, BoxLayout.X_AXIS));
        jpanScroll.add(Box.createRigidArea(new Dimension(20, jspText.getHeight())));
        jpanScroll.add(jspText);
        jpanScroll.add(Box.createRigidArea(new Dimension(20, jspText.getHeight())));

        // Start adding.
        jpanButton.add(Box.createHorizontalGlue());
        jpanButton.add(btnOK);
        jpanButton.add(Box.createRigidArea(new Dimension(15, btnOK.getHeight())));

        jpanMain.add(Box.createRigidArea(new Dimension(txtHelp.getWidth(), 15)));
        for(int i = 0; i < lblLabels.length; i++) {
            JLabel lLabel = lblLabels[i];
            lLabel.setForeground(Color.black);
            jpanTextLabels.add(lLabel);
            jpanTextLabels.add(Box.createRigidArea(new Dimension(txtHelp.getWidth(), 5)));
        }
        jpanLabels.add(Box.createRigidArea(new Dimension(20, jpanTextLabels.getHeight())));
        jpanLabels.add(lblImageTools);
        jpanLabels.add(Box.createRigidArea(new Dimension(20, jpanTextLabels.getHeight())));
        jpanLabels.add(jpanTextLabels);
        jpanLabels.add(Box.createRigidArea(new Dimension(20, jpanTextLabels.getHeight())));
        jpanLabels.add(lblImageRUG);
        jpanLabels.add(Box.createRigidArea(new Dimension(20, jpanTextLabels.getHeight())));
        jpanLabels.add(Box.createHorizontalGlue());

        jpanMain.add(jpanLabels);
        jpanMain.add(Box.createRigidArea(new Dimension(txtHelp.getWidth(), 20)));
        jpanMain.add(jpanScroll);
        jpanMain.add(Box.createRigidArea(new Dimension(txtHelp.getWidth(), 20)));
        jpanMain.add(jpanButton);
        jpanMain.add(Box.createRigidArea(new Dimension(txtHelp.getWidth(), 15)));

        // Pack and go.
        this.getContentPane().add(jpanMain, BorderLayout.CENTER);
        this.pack();
    }

    /**
     * Closes this dialog in a nice way.
     */
    private void close() {
        this.setVisible(false);
        this.dispose();
    }

    /**
     * This method will attempt to load the helptext from the classpath.
     */
    private void loadHelpText() {
        try {
            // First of all, try it via the classloader for this file.
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEXTFILE);
            if(is == null) {
                // Apparently not found, try again with the System (bootstrap) classloader.
                is = ClassLoader.getSystemResourceAsStream(TEXTFILE);
                if(is == null) {
                    iHelpText = "No help file (" + TEXTFILE + ") could be found in the classpath!";
                }
            }

            // See if we have an input stream.
            if(is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                iHelpText = sb.toString();
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * This method loads the appropriate image for displaying it in
     * the imagelabel and icon.
     */
    private void loadImages() {
        // Toolkit icon for the label.
        lblImageTools = new JLabel("No image found!");
        try {
            URL url = this.getClass().getClassLoader().getResource("toolbox.jpg");
            if(url != null) {
                ImageIcon icon = new ImageIcon(url);
                lblImageTools = new JLabel(icon);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        // RUG icon for the window.
        try {
            URL url = this.getClass().getClassLoader().getResource("rugLogo.jpg");
            if(url != null) {
                iRug = new ImageIcon(url);
                lblImageRUG = new JLabel(iRug);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method extracts the last version from the 'about.txt' file.
     *
     * @return  String with the String of the latest version, or '
     */
    private String getLastVersion() {
        String result = null;
        int start = iHelpText.lastIndexOf("- Version ") + 10;
        int end = iHelpText.indexOf("\n", start);
        if(start > 0 && end > 0) {
            result = iHelpText.substring(start, end).trim();
        } else {
            result = "(unknown - missing original help text)!";
        }

        return result;
    }
}
