/**
Copyright 2010 University of South Florida

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 **/

/*
 * MainForm.java
 *
 * Created on Jul 20, 2010, 9:15:56 PM
 */
package edu.usf.cutr.go_sync.gui;

import edu.usf.cutr.go_sync.io.DefaultOperatorReader;
import edu.usf.cutr.go_sync.object.DefaultOperator;
import edu.usf.cutr.go_sync.object.OperatorInfo;
import edu.usf.cutr.go_sync.object.ProcessingOptions;
import edu.usf.cutr.go_sync.task.CompareData;
import edu.usf.cutr.go_sync.task.OsmTask;
import edu.usf.cutr.go_sync.task.RevertChangeset;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

//TODO add radius selection
/**
 *
 * @author Khoa Tran and Marcy Gordon
 */
public class MainForm extends javax.swing.JFrame implements PropertyChangeListener {

    public static EnumSet<ProcessingOptions> processingOptions = EnumSet.noneOf(ProcessingOptions.class);

    private String _operatorName;
    private String _operatorNameAbbreviate;
    private String _operatorAlias;
    private String _operatorRegex;
    private String _operatorNtdId = "";
    private int _gtfsIdDigit;
    private String _revertChangesetId;
    private String _fileDir;
    private CompareData compareTask;
    private RevertChangeset revertTask;
    private ProgressMonitor progressMonitor;
    private OsmTask task;
    private List<DefaultOperator> ops;
    List<String> l;


    /** Creates new form MainForm */
    public MainForm() {
    	setMinimumSize(new Dimension(660, 460));

        DefaultOperatorReader reader = new DefaultOperatorReader(); //create a new reader
        try {
        	ops = reader.readOperators(getClass().getResourceAsStream("/operators.csv")); //read a file with operator info for autocompletion
        }
        catch (Exception e) {
        	ops = reader.readOperators(new File("operators.csv").getAbsolutePath()); //read a file with operator info for autocompletion
        }

        //TODO Fix textfield with ops is null (operators.csv doesn't exist)
        
        l = new ArrayList<String>(); //create a new list to store operator names
        l.add(""); //first entry in the list will be blank
        if (ops != null) {
            for (DefaultOperator op : ops) { //for each operator
                l.add(op.getOperatorName()); //add their name to the list for autocompletion
            }
        }
        initComponents();
        //operatorNameField = new edu.usf.cutr.go_sync.gui.object.AutoCompleteTextField(l);
        operatorNameField.requestFocusInWindow();

        if (ops != null) {
            KeyListener listener = new KeyListener() { //create key listener for autocomple text field

                public void keyTyped(KeyEvent e) {
                    ///System.out.println("Typed!");
                }

                public void keyPressed(KeyEvent e) {
                    //System.out.println("Pressed!");
                }

                public void keyReleased(KeyEvent e) {
                    //System.out.println("Released!");
                    boolean isFound = false;
                    for (DefaultOperator op : ops) { //look through all known operators
                        if (op.getOperatorName().equalsIgnoreCase(operatorNameField.getText())) { //if we have info on the operator
                            operatorNameAbbField.setText(op.getOperatorAbbreviation()); //automatically fill out the abbreviation
                            operatorNTDIDField.setText(String.valueOf(op.getNtdID())); //fill in the NTD ID
                            if (op.getStopDigits() > 0) { //if we know the length of the stop IDs
                                gtfsIdDigitField.setText(String.valueOf(op.getStopDigits())); //fill in the GTFS stop ID length
                            }
                            if (op.getGtfsURL() != null && !op.getGtfsURL().isEmpty()) { //if we know the GTFS URL
                                rbURL.setSelected(true); //select the URL radio button
                                fileDirTextField.setText(op.getGtfsURL()); //fill in the GTFS URL
                            }

                            isFound = true;

                            break;
                        }
                    }
                    // clear all fields
                    if(!isFound){
                        operatorNameAbbField.setText("");
                        operatorNTDIDField.setText("");
                        gtfsIdDigitField.setText("");
                        rbURL.setSelected(true);
                        fileDirTextField.setText("");
                    }
                }
            };
            operatorNameField.addKeyListener(listener); //add the listener to the textfield so that once an operator is recognized, other known values will populate
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        compareDataPanel = new javax.swing.JPanel();
        operatorPanel = new javax.swing.JPanel();
        operatorNameLabel = new javax.swing.JLabel();
        operatorNameField = new edu.usf.cutr.go_sync.gui.object.AutoCompleteTextField(l);
        OperatorAbbLabel = new javax.swing.JLabel();
        operatorNameAbbField = new javax.swing.JTextField();
        operatorAliasLabel = new javax.swing.JLabel();
        operatorAliasField = new javax.swing.JTextField();
        operatorNTDIDLabel = new javax.swing.JLabel();
        operatorNTDIDField = new javax.swing.JTextField();
        operatorRegexLabel = new javax.swing.JLabel();
        operatorRegexField = new javax.swing.JTextField();
        optionsPanel = new javax.swing.JPanel();
        stopOptionsPanel = new javax.swing.JPanel();
        gtfsStopIdLengthPanel = new javax.swing.JPanel();
        gtfsIdDigitLabel = new javax.swing.JLabel();
        gtfsIdDigitField = new javax.swing.JTextField();
        distanceThresholdPanel = new javax.swing.JPanel();
        threshold_label = new javax.swing.JLabel();
        distanceThreshold = new javax.swing.JSpinner();
        routeOptionsPanel = new javax.swing.JPanel();
        dontReplaceExistingOSMRouteColorCb = new javax.swing.JCheckBox();
        skipNodesWithRoleEmptyCb = new javax.swing.JCheckBox();
        skipNodesWithRoleStopCb = new javax.swing.JCheckBox();
        moveNodesBeforeWaysCb = new javax.swing.JCheckBox();
        removePlatformsNotInGtfsFromOSMRelationCb = new javax.swing.JCheckBox();
        dontAddGtfsRouteTextColorCb = new javax.swing.JCheckBox();
        dontAddGtfsAgencyIdCb = new javax.swing.JCheckBox();
        gtfsDataPanel = new javax.swing.JPanel();
        rbURL = new javax.swing.JRadioButton();
        rbFileFolder = new javax.swing.JRadioButton();
        fileNameLabel = new javax.swing.JLabel();
        fileDirTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        netexPanel = new javax.swing.JPanel();
        netexZipFileLabel = new javax.swing.JLabel();
        netexZipFile = new javax.swing.JTextField();
        netexBrowseButton = new javax.swing.JButton();
        netexStopFilenameLabel = new javax.swing.JLabel();
        netexStopFilename = new javax.swing.JTextField();
        requiredFieldsLabel = new javax.swing.JLabel();
        compareButton = new javax.swing.JButton();
        revertChangesetPanel = new javax.swing.JPanel();
        changesetLabel = new javax.swing.JLabel();
        revertChangesetField = new javax.swing.JTextField();
        revertButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        taskOutput = new javax.swing.JTextArea();
        exitButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GO-Sync");
        setName("mainForm"); // NOI18N
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWeights = new double[] {1.0};
        layout.rowWeights = new double[] {0.0, 0.0, 0.0};
        getContentPane().setLayout(layout);

        java.awt.GridBagLayout compareDataPanelLayout = new java.awt.GridBagLayout();
        compareDataPanelLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.5};
        compareDataPanelLayout.rowWeights = new double[] {1.0, 1.0, 1.0, 1.0, 1.0};
        compareDataPanel.setLayout(compareDataPanelLayout);

        operatorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Operator"));
        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {50, 50, 50, 50};
        jPanel1Layout.columnWeights = new double[] {0.1, 0.5, 0.1, 0.5};
        operatorPanel.setLayout(jPanel1Layout);

        operatorNameLabel.setFont(new java.awt.Font("Sans Serif", 0, 13)); // NOI18N
        operatorNameLabel.setForeground(java.awt.Color.blue);
        operatorNameLabel.setText("Op. Full Name (*)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        operatorPanel.add(operatorNameField, gridBagConstraints);

        OperatorAbbLabel.setFont(new java.awt.Font("Sans Serif", 0, 13)); // NOI18N
        OperatorAbbLabel.setForeground(java.awt.Color.blue);
        OperatorAbbLabel.setText("Op. Abbreviation (*)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(OperatorAbbLabel, gridBagConstraints);

        operatorNameAbbField.setName("usernameField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorNameAbbField, gridBagConstraints);
        operatorNameAbbField.getAccessibleContext().setAccessibleName("operatorNameAbbField");

        operatorAliasLabel.setText("Op. Aliases (separated by ';')");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorAliasLabel, gridBagConstraints);

        operatorAliasField.setName("usernameField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorAliasField, gridBagConstraints);

        operatorNTDIDLabel.setText("Op. NTD ID");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorNTDIDLabel, gridBagConstraints);

        operatorNTDIDField.setName("usernameField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorNTDIDField, gridBagConstraints);
        operatorNTDIDField.getAccessibleContext().setAccessibleName("OperatorNTDIDField");

        operatorRegexLabel.setText("Op. Alias Regex");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorRegexLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        operatorPanel.add(operatorRegexField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        compareDataPanel.add(operatorPanel, gridBagConstraints);

        java.awt.GridBagLayout optionsPanelLayout = new java.awt.GridBagLayout();
        optionsPanelLayout.columnWeights = new double[] {0.5, 0.5};
        optionsPanel.setLayout(optionsPanelLayout);

        stopOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Stop options"));
        stopOptionsPanel.setLayout(new javax.swing.BoxLayout(stopOptionsPanel, javax.swing.BoxLayout.Y_AXIS));

        gtfsStopIdLengthPanel.setLayout(new javax.swing.BoxLayout(gtfsStopIdLengthPanel, javax.swing.BoxLayout.LINE_AXIS));

        gtfsIdDigitLabel.setText("Length of GTFS Stop IDs");
        gtfsStopIdLengthPanel.add(gtfsIdDigitLabel);

        gtfsIdDigitField.setName("usernameField"); // NOI18N
        gtfsStopIdLengthPanel.add(gtfsIdDigitField);

        stopOptionsPanel.add(gtfsStopIdLengthPanel);

        distanceThresholdPanel.setToolTipText("routeOptions");
        distanceThresholdPanel.setLayout(new javax.swing.BoxLayout(distanceThresholdPanel, javax.swing.BoxLayout.LINE_AXIS));

        threshold_label.setText("Approximate Comparision Distance Threshold (m)");
        distanceThresholdPanel.add(threshold_label);

        distanceThreshold.setValue(400);
        distanceThresholdPanel.add(distanceThreshold);

        stopOptionsPanel.add(distanceThresholdPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        optionsPanel.add(stopOptionsPanel, gridBagConstraints);

        routeOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Route options"));
        routeOptionsPanel.setLayout(new javax.swing.BoxLayout(routeOptionsPanel, javax.swing.BoxLayout.Y_AXIS));

        dontReplaceExistingOSMRouteColorCb.setText("Don't replace existing route color in OSM");
        routeOptionsPanel.add(dontReplaceExistingOSMRouteColorCb);

        skipNodesWithRoleEmptyCb.setText("Don't keep node members with empty role");
        routeOptionsPanel.add(skipNodesWithRoleEmptyCb);

        skipNodesWithRoleStopCb.setText("Don't keep node members with role 'stop'");
        routeOptionsPanel.add(skipNodesWithRoleStopCb);

        moveNodesBeforeWaysCb.setText("Move node members before ways");
        routeOptionsPanel.add(moveNodesBeforeWaysCb);

        removePlatformsNotInGtfsFromOSMRelationCb.setText("Remove platforms not in Gtfs from OSM route");
        routeOptionsPanel.add(removePlatformsNotInGtfsFromOSMRelationCb);

        dontAddGtfsRouteTextColorCb.setText("Don't add Gtfs 'route_text_color' to OSM");
        routeOptionsPanel.add(dontAddGtfsRouteTextColorCb);

        dontAddGtfsAgencyIdCb.setText("Don't add Gtfs 'agency_id' to OSM");
        routeOptionsPanel.add(dontAddGtfsAgencyIdCb);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        optionsPanel.add(routeOptionsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        compareDataPanel.add(optionsPanel, gridBagConstraints);

        gtfsDataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("GTFS Data"));
        gtfsDataPanel.setName("pnlGTFSData"); // NOI18N

        buttonGroup1.add(rbURL);
        rbURL.setText("URL");
        rbURL.setName("rbURL"); // NOI18N
        rbURL.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbURLItemStateChanged(evt);
            }
        });

        buttonGroup1.add(rbFileFolder);
        rbFileFolder.setSelected(true);
        rbFileFolder.setText("Folder or Zip File");
        rbFileFolder.setName("rbFolderFile"); // NOI18N
        rbFileFolder.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbFileFolderItemStateChanged(evt);
            }
        });

        fileNameLabel.setFont(new java.awt.Font("Sans Serif", 0, 13)); // NOI18N
        fileNameLabel.setForeground(java.awt.Color.blue);
        fileNameLabel.setText("Folder or Zip File (*)");

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout gtfsDataPanelLayout = new javax.swing.GroupLayout(gtfsDataPanel);
        gtfsDataPanel.setLayout(gtfsDataPanelLayout);
        gtfsDataPanelLayout.setHorizontalGroup(
            gtfsDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gtfsDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gtfsDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbFileFolder)
                    .addComponent(fileNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(gtfsDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gtfsDataPanelLayout.createSequentialGroup()
                        .addComponent(fileDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(rbURL))
                .addContainerGap(85, Short.MAX_VALUE))
        );

        gtfsDataPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {rbFileFolder, rbURL});

        gtfsDataPanelLayout.setVerticalGroup(
            gtfsDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gtfsDataPanelLayout.createSequentialGroup()
                .addGroup(gtfsDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbFileFolder)
                    .addComponent(rbURL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(gtfsDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileNameLabel)
                    .addComponent(fileDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        fileNameLabel.getAccessibleContext().setAccessibleParent(gtfsDataPanel);
        fileDirTextField.getAccessibleContext().setAccessibleParent(gtfsDataPanel);
        browseButton.getAccessibleContext().setAccessibleParent(gtfsDataPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        compareDataPanel.add(gtfsDataPanel, gridBagConstraints);

        netexPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Netex Data (for Stops label improvement)"));
        java.awt.GridBagLayout netexPanelLayout = new java.awt.GridBagLayout();
        netexPanelLayout.columnWeights = new double[] {0.1, 0.8, 0.1};
        netexPanel.setLayout(netexPanelLayout);

        netexZipFileLabel.setText("NetEx Zip file");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        netexPanel.add(netexZipFileLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        netexPanel.add(netexZipFile, gridBagConstraints);

        netexBrowseButton.setText("Browse");
        netexBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                netexBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        netexPanel.add(netexBrowseButton, gridBagConstraints);

        netexStopFilenameLabel.setText("NetEx Stops filename");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        netexPanel.add(netexStopFilenameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        netexPanel.add(netexStopFilename, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        compareDataPanel.add(netexPanel, gridBagConstraints);

        requiredFieldsLabel.setFont(new java.awt.Font("Sans Serif", 0, 13)); // NOI18N
        requiredFieldsLabel.setForeground(java.awt.Color.blue);
        requiredFieldsLabel.setText("Fields marked with an asterisk(*) are required");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        compareDataPanel.add(requiredFieldsLabel, gridBagConstraints);

        compareButton.setText("Run");
        compareButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compareButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        compareDataPanel.add(compareButton, gridBagConstraints);

        jTabbedPane1.addTab("Compare Data", compareDataPanel);

        revertChangesetPanel.setName(""); // NOI18N

        changesetLabel.setText("Changeset ID");

        revertChangesetField.setName("usernameField"); // NOI18N

        revertButton.setText("Run");
        revertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout revertChangesetPanelLayout = new javax.swing.GroupLayout(revertChangesetPanel);
        revertChangesetPanel.setLayout(revertChangesetPanelLayout);
        revertChangesetPanelLayout.setHorizontalGroup(
            revertChangesetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(revertChangesetPanelLayout.createSequentialGroup()
                .addGroup(revertChangesetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(revertChangesetPanelLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(changesetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(revertChangesetField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(revertChangesetPanelLayout.createSequentialGroup()
                        .addGap(277, 277, 277)
                        .addComponent(revertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(269, Short.MAX_VALUE))
        );
        revertChangesetPanelLayout.setVerticalGroup(
            revertChangesetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(revertChangesetPanelLayout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addGroup(revertChangesetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(revertChangesetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changesetLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 323, Short.MAX_VALUE)
                .addComponent(revertButton)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Revert Changeset", revertChangesetPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(jTabbedPane1, gridBagConstraints);

        taskOutput.setColumns(20);
        taskOutput.setRows(5);
        jScrollPane1.setViewportView(taskOutput);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        exitButton.setText("Exit");
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitButtonMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        getContentPane().add(exitButton, gridBagConstraints);
        exitButton.getAccessibleContext().setAccessibleName("exitButton");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitButtonMouseClicked
        System.exit(0);
}//GEN-LAST:event_exitButtonMouseClicked

    private void compareButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareButtonActionPerformed
        // clear the text area output
        taskOutput.setText("");
        taskOutput.setLineWrap(true);

        // get data from user input

        if (rbURL.isSelected()) { //if user selected a URL
            try {
                if (!UnzipGTFS(null, new URL(fileDirTextField.getText()), "GTFS")) { //try to unzip from the URL
                    JOptionPane.showMessageDialog(this, "Unable to unzip from URL. Please try again with another URL.");
                    return;
                }
            } catch (MalformedURLException ex) {
                JOptionPane.showMessageDialog(this, "Invalid URL. Please try again with another URL.");
                System.err.println("Error: " + ex.getLocalizedMessage());
                return;
            }
            _fileDir = new File("TempUnzip").getAbsolutePath() + System.getProperty("file.separator");//"\\"; //set the actual location to the GTFS_Temp folder
        } else if (rbFileFolder.isSelected()) { //else user selected a local file/folder
            if (fileDirTextField.getText().toLowerCase().contains(".zip")) { //if a zip file was selected
                if (!UnzipGTFS(chooser.getSelectedFile(), null, "GTFS")) { //unzip it to a temporary folder
                    JOptionPane.showMessageDialog(this, "Unable to unzip from file. Please try again with another file.");
                    return;
                }
                _fileDir = new File("TempUnzip").getAbsolutePath() + System.getProperty("file.separator");//"\\"; //set the actual location to the GTFS_Temp folder
            } else {
                _fileDir = fileDirTextField.getText().replace("file://",""); //else use the folder selected with GTFS files in it
                if (!(Files.isDirectory(new File(_fileDir).toPath())))
                {
                    JOptionPane.showMessageDialog(this, "Path does not specify a folder.");
                    return;
                }
                //TODO - validate that a folder does have GTFS files
            }
        }

        // unzip netex zip
        if (!netexZipFile.getText().isEmpty()) {
            if (netexZipFile.getText().toLowerCase().contains(".zip")) { //if a zip file was selected
                if (!UnzipGTFS(new File(netexZipFile.getText()), null, "Netex")) { //unzip it to a temporary folder
                    JOptionPane.showMessageDialog(this, "Unable to unzip netex file. Please try again with another file.");
                    return;
                }
                _fileDir = new File("TempUnzip").getAbsolutePath() + System.getProperty("file.separator");//"\\"; //set the actual location to the GTFS_Temp folder
            }
        } else {
            if (new File("TempUnzip").listFiles().length > 0) { //if the folder has old files in it
                for (File f : new File("TempUnzip").listFiles()) {
                    f.delete(); //delete all the old files
                }
            }
        }

        //optional field
        _operatorNtdId = operatorNTDIDField.getText();
        if (gtfsIdDigitField.getText() != null && !gtfsIdDigitField.getText().equals("")) {
            try {
                _gtfsIdDigit = Integer.parseInt(gtfsIdDigitField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Message: " + e.getMessage());
                return;
            }
        }

        // Processing options
        if (skipNodesWithRoleEmptyCb.isSelected()) {
            processingOptions.add(ProcessingOptions.SKIP_NODES_WITH_ROLE_EMPTY);
        }
        if (moveNodesBeforeWaysCb.isSelected()) {
            processingOptions.add(ProcessingOptions.MOVE_NODES_BEFORE_WAYS);
        }
        if (removePlatformsNotInGtfsFromOSMRelationCb.isSelected()) {
            processingOptions.add(ProcessingOptions.REMOVE_PLATFORMS_NOT_IN_GTFS_TRIP_FROM_OSM_RELATION);
        }
        if (dontReplaceExistingOSMRouteColorCb.isSelected()) {
            processingOptions.add(ProcessingOptions.DONT_REPLACE_EXISING_OSM_ROUTE_COLOR);
        }
        if (skipNodesWithRoleStopCb.isSelected()) {
            processingOptions.add(ProcessingOptions.SKIP_NODES_WITH_ROLE_STOP);
        }
        if (dontAddGtfsRouteTextColorCb.isSelected()) {
            processingOptions.add(ProcessingOptions.DONT_ADD_GTFS_ROUTE_TEXT_COLOR_TO_ROUTE);
        }
        if (dontAddGtfsAgencyIdCb.isSelected()) {
            processingOptions.add(ProcessingOptions.DONT_ADD_GTFS_AGENCY_ID_TO_ROUTE);
        }

        //can't leave blank
        try {
            _operatorAlias = operatorAliasField.getText();
            _operatorName = operatorNameField.getText();
            
            _operatorNameAbbreviate = operatorNameAbbField.getText();
            _operatorRegex = operatorRegexField.getText();

            if (!_operatorName.isEmpty() && !_operatorNameAbbreviate.isEmpty() && !_fileDir.isEmpty()) {
                new OperatorInfo(_operatorName, _operatorNameAbbreviate, _operatorAlias, _operatorRegex, _operatorNtdId, _gtfsIdDigit, _fileDir);

                progressMonitor = new ProgressMonitor(MainForm.this, "Comparing GTFS and OSM data", "", 0, 100);
                progressMonitor.setProgress(0);
                compareButton.setEnabled(false);
                compareTask = new CompareData(progressMonitor, taskOutput, netexStopFilename.getText());
                compareTask.setRangeThreshold(Double.parseDouble(distanceThreshold.getValue().toString()));
                task = compareTask;
                task.addPropertyChangeListener(this);
                try{
                    compareTask.execute();
                } catch(Exception e){
                    System.out.println("MainForm: "+e);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all the required fields!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Message: " + e.getMessage());
        }
}//GEN-LAST:event_compareButtonActionPerformed

    private void revertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revertButtonActionPerformed
        try {
            _revertChangesetId = revertChangesetField.getText();

            OSMSessionForm osmLogin = new OSMSessionForm();
            if (!osmLogin.showDialog()) //if user hit cancel and didn't enter OSM credentials
            {
                JOptionPane.showMessageDialog(this, "To revert an OSM changeset, you must log in to OSM.");
                return;
            }
            progressMonitor = new ProgressMonitor(MainForm.this, "Reverting Openstreetmap changeset", "", 0, 100);
            progressMonitor.setProgress(0);
            revertTask = new RevertChangeset(_revertChangesetId, progressMonitor, taskOutput);
            task = revertTask;
            task.addPropertyChangeListener(this);
            revertTask.execute();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Message: " + e.getMessage());
        }
}//GEN-LAST:event_revertButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Browse for GTFS file...");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        chooser.showOpenDialog(this);
        if (chooser.getSelectedFile() != null) {
            fileDirTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    /*
     * Unzips a GTFS zip file to a temporary directory called GTFS_Temp, which is created in the current directory
     * Pass either a file or URL and NULL for the other - do not send both
     * Returns true if it successfully unzipped the file
     */
    private boolean UnzipGTFS(File zipFile, URL zipURL, String tempDirBasename) {
        //TODO display a progress bar to user so they know a file is being unzipped

        File unzipFolder = new File("TempUnzip" + File.separator + tempDirBasename);
        String unzipLocation = unzipFolder.getAbsolutePath() + System.getProperty("file.separator"); //"\\"; //temporary folder to store unzipped files
        try {
            unzipFolder.mkdirs(); //create the directory if not already created
        } catch (SecurityException ex) {
            System.err.println("Unable to create temporary directory to unzip the GTFS data to. \n" + ex.getLocalizedMessage());
            return false;
        }
        if (unzipFolder.listFiles().length > 0) { //if the folder has old files in it
            for (File f : unzipFolder.listFiles()) {
                f.delete(); //delete all the old files
            }
        }

        try { //try to unzip the file and write the files into the temporary directory
            OutputStream out = null;
            ZipInputStream zip;

            
            //TODO test that URL is actually ZIP files

            if (zipFile == null) {
                //TODO check ftp codes
                if (zipURL.getProtocol().equals("http"))
                {
                    HttpURLConnection zipHttpCon = ((HttpURLConnection)zipURL.openConnection());
                    int response_code = zipHttpCon.getResponseCode();
                    if (response_code != 200 ) {
                        taskOutput.append("HTTP server returned " +response_code + " " + zipHttpCon.getResponseMessage());
                        System.err.println("HTTP server returned " +response_code);
                        return false;
                    }
                }
                System.out.println("Unzipping " + zipURL.toString() + " to " + unzipLocation);
                InputStream zipstr = zipURL.openStream();
                System.out.println(zipstr + " " );
                zip = new ZipInputStream(new BufferedInputStream(zipstr));
            } else {
                String fileContentType = Files.probeContentType(zipFile.toPath());
                if (!(fileContentType.equals("application/zip") || fileContentType.equals("application/x-zip-compressed")))
            		{System.out.println((Files.probeContentType(zipFile.toPath())));
            		return false;}
                System.out.println("Unzipping " + zipFile.getAbsolutePath() + " to " + unzipLocation);
                zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            }

            ZipEntry next_file;
//            System.out.println("Zip is " + zip.available() + zip.getNextEntry());

            while ((next_file = zip.getNextEntry()) != null) {

                System.out.println("Reading the file: " + next_file.getName() + " Size: " + next_file.getSize());

                out = new FileOutputStream(unzipLocation + next_file.getName());
                byte[] buf = new byte[1024];
                int len;

                while ((len = zip.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }


                out.close();
                //out.flush();
                //do any processing here if you’d like
                zip.closeEntry();
            }
            zip.close();
            System.out.println("Files have been written");
        } catch (Exception e) {
            System.err.println("Error writing a file: " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void rbFileFolderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbFileFolderItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED && rbFileFolder.isSelected()) {
            fileNameLabel.setText("Folder or Zip File (*)");
            browseButton.setVisible(true);
            if (chooser != null) {
                fileDirTextField.setText(chooser.getSelectedFile().getAbsolutePath());
            } else {
                fileDirTextField.setText("");
            }
        }
    }//GEN-LAST:event_rbFileFolderItemStateChanged

    private void rbURLItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbURLItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED && rbURL.isSelected()) {
            fileNameLabel.setText("URL of Zip File (*)");
            browseButton.setVisible(false);
            fileDirTextField.setText("");
        }
    }//GEN-LAST:event_rbURLItemStateChanged

    private void netexBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_netexBrowseButtonActionPerformed
        netexChooser = new JFileChooser();
        netexChooser.setCurrentDirectory(new java.io.File("."));
        netexChooser.setDialogTitle("Browse for Netex file...");
        netexChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        netexChooser.setMultiSelectionEnabled(true);
        netexChooser.showOpenDialog(this);
        if (netexChooser.getSelectedFile() != null) {
            netexZipFile.setText(netexChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_netexBrowseButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            // Set system native Java L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException|ClassNotFoundException|InstantiationException|IllegalAccessException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
/*        } catch (ClassNotFoundException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
        } catch (InstantiationException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
*/        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message = task.getMessage() + "   " + progress + "% \n";
            taskOutput.append(message);
            if (progressMonitor.isCanceled() || task.isDone()) {
                progressMonitor.close();
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled() || progress<100 || task.flagIsDone) {
                    task.cancel(true);
                    taskOutput.append("Task canceled.\n");
                    compareButton.setEnabled(true);
                } else {
                    taskOutput.append("Task completed.\n");
//                    if (task==compareTask) compareTask.generateReport();
                    this.dispose();
                }
            }
        }
        taskOutput.setCaretPosition(taskOutput.getText().length());
    }
    private javax.swing.JFileChooser chooser;
    private javax.swing.JFileChooser netexChooser;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel OperatorAbbLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel changesetLabel;
    private javax.swing.JButton compareButton;
    private javax.swing.JPanel compareDataPanel;
    private javax.swing.JSpinner distanceThreshold;
    private javax.swing.JPanel distanceThresholdPanel;
    private javax.swing.JCheckBox dontAddGtfsAgencyIdCb;
    private javax.swing.JCheckBox dontAddGtfsRouteTextColorCb;
    private javax.swing.JCheckBox dontReplaceExistingOSMRouteColorCb;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField fileDirTextField;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JPanel gtfsDataPanel;
    private javax.swing.JTextField gtfsIdDigitField;
    private javax.swing.JLabel gtfsIdDigitLabel;
    private javax.swing.JPanel gtfsStopIdLengthPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JCheckBox moveNodesBeforeWaysCb;
    private javax.swing.JButton netexBrowseButton;
    private javax.swing.JPanel netexPanel;
    private javax.swing.JTextField netexStopFilename;
    private javax.swing.JLabel netexStopFilenameLabel;
    private javax.swing.JTextField netexZipFile;
    private javax.swing.JLabel netexZipFileLabel;
    private javax.swing.JTextField operatorAliasField;
    private javax.swing.JLabel operatorAliasLabel;
    private javax.swing.JTextField operatorNTDIDField;
    private javax.swing.JLabel operatorNTDIDLabel;
    private javax.swing.JTextField operatorNameAbbField;
    private javax.swing.JTextField operatorNameField;
    private javax.swing.JLabel operatorNameLabel;
    private javax.swing.JPanel operatorPanel;
    private javax.swing.JTextField operatorRegexField;
    private javax.swing.JLabel operatorRegexLabel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JRadioButton rbFileFolder;
    private javax.swing.JRadioButton rbURL;
    private javax.swing.JCheckBox removePlatformsNotInGtfsFromOSMRelationCb;
    private javax.swing.JLabel requiredFieldsLabel;
    private javax.swing.JButton revertButton;
    private javax.swing.JTextField revertChangesetField;
    private javax.swing.JPanel revertChangesetPanel;
    private javax.swing.JPanel routeOptionsPanel;
    private javax.swing.JCheckBox skipNodesWithRoleEmptyCb;
    private javax.swing.JCheckBox skipNodesWithRoleStopCb;
    private javax.swing.JPanel stopOptionsPanel;
    private javax.swing.JTextArea taskOutput;
    private javax.swing.JLabel threshold_label;
    // End of variables declaration//GEN-END:variables
}
