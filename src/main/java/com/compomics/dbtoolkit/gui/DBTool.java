/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 10-okt-02
 * Time: 18:01:11
 */
package com.compomics.dbtoolkit.gui;

import com.compomics.dbtoolkit.gui.components.FileInputPanel;
import com.compomics.dbtoolkit.gui.components.StatusPanel;
import com.compomics.dbtoolkit.gui.interfaces.StatusView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.HashMap;

/*
 * CVS information:
 *
 * $Revision: 1.6 $
 * $Date: 2007/07/06 09:52:03 $
 */

/**
 * This class implements the view and basic GUI logic
 * (action- and other listeners and change of look&feel
 * functionality) for the DBTool GUI.
 *
 * @author Lennart Martens
 */
public class DBTool extends CursorModifiableJFrameImpl implements StatusView {

    /**
     * The StatusPanel component.
     */
    StatusPanel status = null;

    /**
     * The FileInput component.
     */
    FileInputPanel fip = null;
    /**
     * This component gives a preview of the
     * first two FASTA entries in a loaded DB.
     */
    JTextArea preview = null;

    /**
     * The scrollpane on which the preview TextArea is laid out.
     */
    JScrollPane previewPane = null;

    /**
     * HashMap with key = look&feel name, value = look&feel class.
     */
    HashMap lookAndFeels = null;

    /**
     * The controller for this view.
     */
    private DBToolController iController = null;

    private static final String WINDOW_ICON = "DBIcon.gif";

    /**
     * Constructor with a name (title) for the JFrame.
     *
     * @param   aName   String with the name (title) for the JFrame.
     */
    public DBTool(String aName) {
        super(aName);
        this.iController = new DBToolController(this);
    }

    /**
     * This is the real working method that gathers the components
     * and displays the Frame.
     */
    public void openWindow() {
        // Enable window closing.
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                iController.exitTriggered();
            }

        });

        // RUG icon for the window.
        try {
            URL url = this.getClass().getClassLoader().getResource(WINDOW_ICON);
            if(url != null) {
                this.setIconImage(new ImageIcon(url).getImage());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        this.addComponentListener(new ComponentAdapter() {
            /**
             * Invoked when the component's size changes.
             */
            public void componentResized(ComponentEvent e) {
                // We should inform the controller,
                // so it can reformat the preview data and show it.
                iController.triggerResized();
            }
        });

        // The menubar.
        JMenuBar bar = this.getMenuBarForFrame();
        this.setJMenuBar(bar);

        // The main GUI.
        this.getContentPane().add(this.getScreen(), BorderLayout.CENTER);
        this.pack();
        this.setSize(this.getWidth()+5, this.getHeight()+5);

        // Show the frame.
        this.setVisible(true);
    }

    /**
     * This method constructs the MenuBar for the application.
     *
     * @return JMenuBar the menubar for this application.
     */
    private JMenuBar getMenuBarForFrame() {
        // First menu is the file menu.
        JMenu file = new JMenu("File");
        file.setMnemonic('f');
        // We can either process the file that was loaded (if any),
        // or exit the program.
        JMenuItem open = new JMenuItem("Process", KeyEvent.VK_P);
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);

        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Inform the controller that a process request has been
                // triggered.
                iController.processDataTriggered();
            }
        });

        exit.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                iController.exitTriggered();
            }
        });
        // Construct the file menu.
        file.add(open);
        file.add(exit);

        // Next up is the settings menu.
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_S);
        // We can set the number of entries to show in the preview pane.
        JMenuItem numFASTALines = new JMenuItem("Preview...");
        numFASTALines.setMnemonic(KeyEvent.VK_P);
        numFASTALines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Inform the controller we want to reset the number of items to display
                // on the preview.
                iController.requestFASTALines();
            }
        });

        // Next item to the settings menu is the look and feel choice.
        JMenu lAndF = new JMenu("Look and feel...");
        lAndF.setMnemonic(KeyEvent.VK_L);
        // Get the installed look&feel list.
        UIManager.LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
        lookAndFeels = new HashMap(lfs.length);
        // Look and feel handling is all taken care of here.
        // Notice the dynamically constructed set of sub-menuitems, based on the
        // installed look&feels for the current platform.
        JMenuItem[] lafs = new JMenuItem[lfs.length];
        // So, we make a MenuItem for each L&F, and add the corresponding
        // action to it.
        for(int i = 0; i < lfs.length; i++) {
            // For each L&F...
            final UIManager.LookAndFeelInfo lF = lfs[i];
            // ...display the name in the menu...
            lafs[i] = new JMenuItem(lF.getName());
            // .. and listen for clicks...
            lafs[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // .. and when clicked, set the corresponding L&F.
                    setLAF(lF.getClassName());
                }
            });
            // Store them in a HashMap for later reference.
            lAndF.add(lafs[i]);
            lookAndFeels.put(lF.getName(), lF.getClassName());
        }
        // Complete the settings menu.
        settings.add(lAndF);
        settings.add(numFASTALines);

        // Now for the tools menu.
        JMenu tools = new JMenu("Tools");
        tools.setMnemonic(KeyEvent.VK_T);
        // Count menuitem.
        JMenuItem tool1 = new JMenuItem("Count DB entries");
        tool1.setMnemonic(KeyEvent.VK_E);
        tool1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.countRequested();
            }
        });
        // To FASTA file menuitem.
        JMenuItem toFasta = new JMenuItem("Output as FASTA file...");
        toFasta.setMnemonic(KeyEvent.VK_O);
        toFasta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.FASTAOutputRequested();
            }
        });
        // To replaced FASTA file menuitem.
        JMenuItem toReplacedFasta = new JMenuItem("Output as FASTA file after replacing residues...");
        toReplacedFasta.setMnemonic(KeyEvent.VK_O);
        toReplacedFasta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.replacedOutputRequested();
            }
        });
        // Regular expression filter menuitem.
        JMenuItem regExpLimit = new JMenuItem("Filter proteins by regular expression...");
        regExpLimit.setMnemonic(KeyEvent.VK_M);
        regExpLimit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.processRegExpFilterTriggered();
            }
        });
        // To reversed FASTA file menuitem.
        JMenuItem toReversedFasta = new JMenuItem("Output as reversed FASTA file...");
        toReversedFasta.setMnemonic(KeyEvent.VK_V);
        toReversedFasta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.reversedOutputRequested();
            }
        });
        // To shuffled FASTA file menuitem.
        JMenuItem toShuffledFasta = new JMenuItem("Output as shuffled FASTA file...");
        toShuffledFasta.setMnemonic(KeyEvent.VK_S);
        toShuffledFasta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.shuffleOutputRequested();
            }
        });
        // Concat file menuitem.
        JMenuItem concat = new JMenuItem("Concatenate DB's (or copy file)...");
        concat.setMnemonic(KeyEvent.VK_C);
        concat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.processConcatTriggered();
            }
        });
        // Map peptides menuitem.
        JMenuItem pepMap = new JMenuItem("Map peptide list to database...");
        pepMap.setMnemonic(KeyEvent.VK_M);
        pepMap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.processPeptideMappingTriggered();
            }
        });
        // ClearRed file menuitem.
        JMenuItem clearRed = new JMenuItem("Clear redundancy in DB...");
        clearRed.setMnemonic(KeyEvent.VK_R);
        clearRed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.processClearRedTriggered();
            }
        });

        tools.add(tool1);
        tools.add(toFasta);
        tools.add(toReplacedFasta);
        tools.add(regExpLimit);
        tools.add(toReversedFasta);
        tools.add(toShuffledFasta);
        tools.add(concat);
        tools.add(pepMap);
        tools.add(clearRed);

        // The help menu conatins only some info about the program.
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        // The about menuitem will pop-up the about dialog.
        JMenuItem about = new JMenuItem("About");
        about.setMnemonic(KeyEvent.VK_A);
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iController.showAbout();
            }
        });
        // Complete the Help menu.
        help.add(about);

        // Menubar construction in progress...
        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(settings);
        bar.add(tools);
        bar.add(help);

        // Voila.
        return bar;
    }


    /**
     * This method constructs the main screen in a JPanel.
     *
     * @return  JPanel with the main screen.
     */
    private JPanel getScreen() {
        // The screen.
        JPanel screen = new JPanel(new BorderLayout());

        // The status panel.
        status = new StatusPanel(false);
        status.setStatus("No file loaded.");
        status.setError("None.");
        status.setBorder(BorderFactory.createTitledBorder("Status panel"));
        screen.add(status, BorderLayout.SOUTH);

        // FileInputPanel.
        fip = new FileInputPanel("Database file");
        fip.addReceiver(iController);
        screen.add(fip, BorderLayout.NORTH);

        // Preview area.
        preview = new JTextArea(20, 120);
        preview.setFont(new Font("monospaced", Font.PLAIN, 12));
        preview.setEditable(false);
        previewPane = new JScrollPane(preview);
        previewPane.setBorder(BorderFactory.createTitledBorder("Preview pane (" + iController.getNrOfEntries() + " FASTA entries)"));
        screen.add(previewPane, BorderLayout.CENTER);

        // Voila.
        return screen;
    }


    /**
     * This method attempts to set the Look and Feel.
     *
     * @param   aClassName  the classname for the look and feel.
     */
    private void setLAF(String aClassName) {
        try {
            UIManager.setLookAndFeel(aClassName);
            SwingUtilities.updateComponentTreeUI(this);
        } catch(Exception e) {
            // Should not happen.
        }
    }

    /**
     * Overriden paint method to work well with the changing of L&F.
     */
    public void paint(Graphics g) {
        preview.setBackground(this.getBackground());
        super.paint(g);
    }

    /**
     * This method queries the previewpane for the number of characters it can display per line.
     *
     * @return  int with the umber of characters the previewpane can display per line
     */
    int getNrOfCharsOnPreviewPane() {
        FontMetrics fm = preview.getFontMetrics(preview.getFont());
        int charWidth = fm.charWidth('W');
        return (int)((previewPane.getSize().width - 5) / charWidth);
    }

    /**
     * The main method ensures that we can start the program at this point.
     * The real working method however, is the 'openWindow()' method -
     * this to allow integration from other Java programs.
     *
     * @param   args    String[] with the start-up arguments. These are not used here.
     */
    public static void main(String[] args) {
        DBTool tool = null;
        try {
            tool = new DBTool("Database processing tool");
            tool.openWindow();
        } catch(Throwable t) {
            JOptionPane.showMessageDialog(tool, new String[]{"Fatal exception occurred in program!", "\n", "     " + t.getMessage() + "'!", "\n", "Please contact the author about this if the problem persists.", "\n", "\n"}, "Program unexpectedly terminated!", JOptionPane.ERROR_MESSAGE);
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * This method returns a Point to set as location for a child window
     * that is located at a fraction of the current location and size.
     *
     * @param   aFractionWidth  int with the fraction of the width to
     *                          put the child location at.
     * @param   aFractionHeight int with the fraction of the height to
     *                          put the child location at.
     * @return  Point   with the requested location.
     */
    Point getPoint(int aFractionWidth, int aFractionHeight) {
        Point p = this.getLocation();
        Point result = new Point((int)(p.x + this.getWidth()/aFractionWidth), (int)(p.y + this.getHeight()/aFractionHeight));
        return result;
    }

    /**
     * This method allows the caller to specify the status message
     * that is being displayed.
     *
     * @param   aStatus  String with the desired status message.
     */
    public void setStatus(String aStatus) {
        this.status.setStatus(aStatus);
    }

    /**
     * This method allows the caller to specify the error message
     * that is being displayed.
     *
     * @param   aError  String with the desired error message.
     */
    public void setError(String aError) {
        this.status.setError(aError);
    }

    /**
     * This method clears the visible data on the screen.
     */
    public void clear() {
        this.preview.setText("");
        this.fip.setTextFieldText("");
        this.setStatus("No file loaded.");
    }
}
