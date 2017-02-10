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

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import edu.usf.cutr.go_sync.object.OperatorInfo;
import edu.usf.cutr.go_sync.task.CompareData;
import edu.usf.cutr.go_sync.task.OsmTask;
import edu.usf.cutr.go_sync.task.RevertChangeset;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.usf.cutr.go_sync.io.DefaultOperatorReader;
import edu.usf.cutr.go_sync.object.DefaultOperator;

import java.awt.event.ItemEvent;
import java.awt.event.KeyListener;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;

//TODO add radius selection
/**
 *
 * @author Khoa Tran and Marcy Gordon
 */
public class MainForm extends javax.swing.JFrame implements PropertyChangeListener {

    private String _operatorName;
    private String _operatorNameAbbreviate;
    private String _operatorAlias;
    private String _operatorNtdId = "";
    private int _gtfsIdDigit;
    private String _revertChangesetId;
    private String _fileDir;
    private CompareData compareTask;
    private RevertChangeset revertTask;
    private ProgressMonitor progressMonitor;
    private OsmTask task;
    private List<DefaultOperator> ops;


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
        
        List<String> l = new ArrayList<String>(); //create a new list to store operator names
        l.add(""); //first entry in the list will be blank
        if (ops != null) {
            for (DefaultOperator op : ops) { //for each operator
                l.add(op.getOperatorName()); //add their name to the list for autocompletion
            }
        }
        initComponents(l);

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
    private void initComponents(List<String> l) {

        buttonGroup1 = new javax.swing.ButtonGroup();
        exitButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        compareDataPanel = new javax.swing.JPanel();
        revertChangesetPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taskOutput = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GO-Sync");
        setName("mainForm"); // NOI18N
        setResizable(true); //false);

        exitButton.setText("Exit");
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitButtonMouseClicked(evt);
            }
        });
        GridBagLayout gbl_compareDataPanel = new GridBagLayout();
        gbl_compareDataPanel.columnWidths = new int[]{151, 20, 161, 3, 126, 70, 57, 0, 0, 150, 0, 150, 0, 0};
        gbl_compareDataPanel.rowHeights = new int[]{19, 19, 95, 25, 0, 0};
        gbl_compareDataPanel.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gbl_compareDataPanel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        compareDataPanel.setLayout(gbl_compareDataPanel);
        operatorNameLabel = new javax.swing.JLabel();
        
                operatorNameLabel.setText("Operator Full Name (*)");
                GridBagConstraints gbc_operatorNameLabel = new GridBagConstraints();
                gbc_operatorNameLabel.anchor = GridBagConstraints.EAST;
                gbc_operatorNameLabel.insets = new Insets(0, 0, 5, 5);
                gbc_operatorNameLabel.gridwidth = 2;
                gbc_operatorNameLabel.gridx = 0;
                gbc_operatorNameLabel.gridy = 0;
                compareDataPanel.add(operatorNameLabel, gbc_operatorNameLabel);
        
        //create a new textfield with autocomplete for operator names
        operatorNameField = new edu.usf.cutr.go_sync.gui.object.AutoCompleteTextField(l);
        //add the textfield to the panel
        GridBagConstraints gbc_operatorNameField = new GridBagConstraints();
        gbc_operatorNameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_operatorNameField.insets = new Insets(0, 0, 5, 5);
        gbc_operatorNameField.gridwidth = 4;
        gbc_operatorNameField.gridx = 2;
        gbc_operatorNameField.gridy = 0;
        compareDataPanel.add(operatorNameField, gbc_operatorNameField);
        
                operatorNameField.requestFocusInWindow(); //set the cursor in the operator name autocomplete text field
        OperatorAbbLabel = new javax.swing.JLabel();
        
                OperatorAbbLabel.setText("Operator Abbreviation (*)");
                GridBagConstraints gbc_OperatorAbbLabel = new GridBagConstraints();
                gbc_OperatorAbbLabel.anchor = GridBagConstraints.EAST;
                gbc_OperatorAbbLabel.insets = new Insets(0, 0, 5, 5);
                gbc_OperatorAbbLabel.gridwidth = 3;
                gbc_OperatorAbbLabel.gridx = 6;
                gbc_OperatorAbbLabel.gridy = 0;
                compareDataPanel.add(OperatorAbbLabel, gbc_OperatorAbbLabel);
        operatorNameAbbField = new javax.swing.JTextField();
        
                operatorNameAbbField.setName("usernameField");
                GridBagConstraints gbc_operatorNameAbbField = new GridBagConstraints();
                gbc_operatorNameAbbField.gridwidth = 3;
                gbc_operatorNameAbbField.fill = GridBagConstraints.HORIZONTAL;
                gbc_operatorNameAbbField.insets = new Insets(0, 0, 5, 5);
                gbc_operatorNameAbbField.gridx = 9;
                gbc_operatorNameAbbField.gridy = 0;
                compareDataPanel.add(operatorNameAbbField, gbc_operatorNameAbbField);
                operatorNameAbbField.getAccessibleContext().setAccessibleName("operatorNameAbbField");
        operatorAliasField = new javax.swing.JTextField();
        
                operatorAliasField.setName("usernameField"); // NOI18N
                GridBagConstraints gbc_operatorAliasField = new GridBagConstraints();
                gbc_operatorAliasField.fill = GridBagConstraints.HORIZONTAL;
                gbc_operatorAliasField.insets = new Insets(0, 0, 5, 5);
                gbc_operatorAliasField.gridwidth = 3;
                gbc_operatorAliasField.gridx = 1;
                gbc_operatorAliasField.gridy = 1;
                compareDataPanel.add(operatorAliasField, gbc_operatorAliasField);
        operatorAliasLabel = new javax.swing.JLabel();
        
                operatorAliasLabel.setText("Operator Alias");
                GridBagConstraints gbc_operatorAliasLabel = new GridBagConstraints();
                gbc_operatorAliasLabel.anchor = GridBagConstraints.EAST;
                gbc_operatorAliasLabel.insets = new Insets(0, 0, 5, 5);
                gbc_operatorAliasLabel.gridx = 0;
                gbc_operatorAliasLabel.gridy = 1;
                compareDataPanel.add(operatorAliasLabel, gbc_operatorAliasLabel);
        gtfsDataPanel = new javax.swing.JPanel();
        
                gtfsDataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("GTFS Data"));
                gtfsDataPanel.setName("pnlGTFSData"); // NOI18N
                                                        operatorNTDIDLabel = new javax.swing.JLabel();
                                                        
                                                                operatorNTDIDLabel.setText("Operator NTD ID");
                                                                GridBagConstraints gbc_operatorNTDIDLabel = new GridBagConstraints();
                                                                gbc_operatorNTDIDLabel.gridwidth = 2;
                                                                gbc_operatorNTDIDLabel.anchor = GridBagConstraints.WEST;
                                                                gbc_operatorNTDIDLabel.insets = new Insets(0, 0, 5, 5);
                                                                gbc_operatorNTDIDLabel.gridx = 4;
                                                                gbc_operatorNTDIDLabel.gridy = 1;
                                                                compareDataPanel.add(operatorNTDIDLabel, gbc_operatorNTDIDLabel);
                                                        operatorNTDIDField = new javax.swing.JTextField();
                                                        
                                                                operatorNTDIDField.setName("usernameField"); // NOI18N
                                                                GridBagConstraints gbc_operatorNTDIDField = new GridBagConstraints();
                                                                gbc_operatorNTDIDField.fill = GridBagConstraints.HORIZONTAL;
                                                                gbc_operatorNTDIDField.insets = new Insets(0, 0, 5, 5);
                                                                gbc_operatorNTDIDField.gridx = 6;
                                                                gbc_operatorNTDIDField.gridy = 1;
                                                                compareDataPanel.add(operatorNTDIDField, gbc_operatorNTDIDField);
                                                                operatorNTDIDField.getAccessibleContext().setAccessibleName("OperatorNTDIDField");
                                                        gtfsIdDigitLabel = new javax.swing.JLabel();
                                                        
                                                                gtfsIdDigitLabel.setText("Length of GTFS Stop IDs");
                                                                GridBagConstraints gbc_gtfsIdDigitLabel = new GridBagConstraints();
                                                                gbc_gtfsIdDigitLabel.gridwidth = 3;
                                                                gbc_gtfsIdDigitLabel.anchor = GridBagConstraints.EAST;
                                                                gbc_gtfsIdDigitLabel.insets = new Insets(0, 0, 5, 5);
                                                                gbc_gtfsIdDigitLabel.gridx = 7;
                                                                gbc_gtfsIdDigitLabel.gridy = 1;
                                                                compareDataPanel.add(gtfsIdDigitLabel, gbc_gtfsIdDigitLabel);
                                                        gtfsIdDigitField = new javax.swing.JTextField();
                                                        gtfsIdDigitField.setMinimumSize(new Dimension(25, 19));
                                                        
                                                                gtfsIdDigitField.setName("usernameField"); // NOI18N
                                                                GridBagConstraints gbc_gtfsIdDigitField = new GridBagConstraints();
                                                                gbc_gtfsIdDigitField.fill = GridBagConstraints.HORIZONTAL;
                                                                gbc_gtfsIdDigitField.insets = new Insets(0, 0, 5, 5);
                                                                gbc_gtfsIdDigitField.gridx = 11;
                                                                gbc_gtfsIdDigitField.gridy = 1;
                                                                compareDataPanel.add(gtfsIdDigitField, gbc_gtfsIdDigitField);
                                                                                
                                                                                        GridBagConstraints gbc_gtfsDataPanel = new GridBagConstraints();
                                                                                        gbc_gtfsDataPanel.fill = GridBagConstraints.HORIZONTAL;
                                                                                        gbc_gtfsDataPanel.insets = new Insets(0, 0, 5, 5);
                                                                                        gbc_gtfsDataPanel.gridwidth = 12;
                                                                                        gbc_gtfsDataPanel.gridx = 0;
                                                                                        gbc_gtfsDataPanel.gridy = 2;
                                                                                        compareDataPanel.add(gtfsDataPanel, gbc_gtfsDataPanel);
                                                                                        GridBagLayout gbl_gtfsDataPanel = new GridBagLayout();
                                                                                        gbl_gtfsDataPanel.columnWidths = new int[]{100, 187, 158, 74, 0};
                                                                                        gbl_gtfsDataPanel.rowHeights = new int[]{23, 25, 0};
                                                                                        gbl_gtfsDataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
                                                                                        gbl_gtfsDataPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
                                                                                        gtfsDataPanel.setLayout(gbl_gtfsDataPanel);
                                                                                        browseButton = new javax.swing.JButton();
                                                                                        
                                                                                                browseButton.setText("Browse");
                                                                                                browseButton.addActionListener(new java.awt.event.ActionListener() {
                                                                                                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                                                                        browseButtonActionPerformed(evt);
                                                                                                    }
                                                                                                });
                                                                                                browseButton.getAccessibleContext().setAccessibleParent(gtfsDataPanel);
                                                                                                fileNameLabel = new javax.swing.JLabel();
                                                                                                
                                                                                                        fileNameLabel.setText("Folder or Zip File (*)");
                                                                                                        
                                                                                                                fileNameLabel.getAccessibleContext().setAccessibleParent(gtfsDataPanel);
                                                                                                                rbURL = new javax.swing.JRadioButton();
                                                                                                                
                                                                                                                        buttonGroup1.add(rbURL);
                                                                                                                        rbURL.setText("URL");
                                                                                                                        rbURL.setName("rbURL"); // NOI18N
                                                                                                                        rbURL.addItemListener(new java.awt.event.ItemListener() {
                                                                                                                            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                                                                                                                rbURLItemStateChanged(evt);
                                                                                                                            }
                                                                                                                        });
                                                                                                                        rbFileFolder = new javax.swing.JRadioButton();
                                                                                                                        
                                                                                                                                buttonGroup1.add(rbFileFolder);
                                                                                                                                rbFileFolder.setSelected(true);
                                                                                                                                rbFileFolder.setText("Folder or Zip File");
                                                                                                                                rbFileFolder.setName("rbFolderFile"); // NOI18N
                                                                                                                                rbFileFolder.addItemListener(new java.awt.event.ItemListener() {
                                                                                                                                    public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                                                                                                                        rbFileFolderItemStateChanged(evt);
                                                                                                                                    }
                                                                                                                                });
                                                                                                                                GridBagConstraints gbc_rbFileFolder = new GridBagConstraints();
                                                                                                                                gbc_rbFileFolder.anchor = GridBagConstraints.NORTHEAST;
                                                                                                                                gbc_rbFileFolder.insets = new Insets(0, 0, 5, 5);
                                                                                                                                gbc_rbFileFolder.gridx = 1;
                                                                                                                                gbc_rbFileFolder.gridy = 0;
                                                                                                                                gtfsDataPanel.add(rbFileFolder, gbc_rbFileFolder);
                                                                                                                        GridBagConstraints gbc_rbURL = new GridBagConstraints();
                                                                                                                        gbc_rbURL.anchor = GridBagConstraints.NORTHWEST;
                                                                                                                        gbc_rbURL.insets = new Insets(0, 0, 5, 5);
                                                                                                                        gbc_rbURL.gridx = 2;
                                                                                                                        gbc_rbURL.gridy = 0;
                                                                                                                        gtfsDataPanel.add(rbURL, gbc_rbURL);
                                                                                                                GridBagConstraints gbc_fileNameLabel = new GridBagConstraints();
                                                                                                                gbc_fileNameLabel.fill = GridBagConstraints.HORIZONTAL;
                                                                                                                gbc_fileNameLabel.insets = new Insets(0, 0, 0, 5);
                                                                                                                gbc_fileNameLabel.gridx = 0;
                                                                                                                gbc_fileNameLabel.gridy = 1;
                                                                                                                gtfsDataPanel.add(fileNameLabel, gbc_fileNameLabel);
                                                                                                fileDirTextField = new javax.swing.JTextField();
                                                                                                fileDirTextField.getAccessibleContext().setAccessibleParent(gtfsDataPanel);
                                                                                                GridBagConstraints gbc_fileDirTextField = new GridBagConstraints();
                                                                                                gbc_fileDirTextField.fill = GridBagConstraints.HORIZONTAL;
                                                                                                gbc_fileDirTextField.insets = new Insets(0, 0, 0, 5);
                                                                                                gbc_fileDirTextField.gridwidth = 2;
                                                                                                gbc_fileDirTextField.gridx = 1;
                                                                                                gbc_fileDirTextField.gridy = 1;
                                                                                                gtfsDataPanel.add(fileDirTextField, gbc_fileDirTextField);
                                                                                                GridBagConstraints gbc_browseButton = new GridBagConstraints();
                                                                                                gbc_browseButton.anchor = GridBagConstraints.NORTH;
                                                                                                gbc_browseButton.fill = GridBagConstraints.HORIZONTAL;
                                                                                                gbc_browseButton.gridx = 3;
                                                                                                gbc_browseButton.gridy = 1;
                                                                                                gtfsDataPanel.add(browseButton, gbc_browseButton);

        jTabbedPane1.addTab("Compare Data", compareDataPanel);
                jLabel1 = new javax.swing.JLabel();
                
                        jLabel1.setText("Fields marked with an asterisk(*) are required");
                        GridBagConstraints gbc_jLabel1 = new GridBagConstraints();
                        gbc_jLabel1.fill = GridBagConstraints.HORIZONTAL;
                        gbc_jLabel1.insets = new Insets(0, 0, 5, 5);
                        gbc_jLabel1.gridwidth = 12;
                        gbc_jLabel1.gridx = 0;
                        gbc_jLabel1.gridy = 3;

                        compareDataPanel.add(jLabel1, gbc_jLabel1);


        distanceThreshold = new JSpinner();
        distanceThreshold.setValue(400);
        GridBagConstraints gbc_threshold = new GridBagConstraints();
        JLabel threshold_label = new JLabel("Approximate Comparision Distance Threshold (m)");
//        gbc_threshold.fill = GridBagConstraints.HORIZONTAL;
        gbc_threshold.insets = new Insets(0, 0, 5, 5);
        gbc_threshold.gridwidth = 12;

        gbc_threshold.gridy = 4;
        gbc_threshold.gridx = 0;
        compareDataPanel.add(threshold_label,gbc_threshold);
        gbc_threshold.gridx = 6;
        compareDataPanel.add(distanceThreshold, gbc_threshold);

                compareButton = new javax.swing.JButton();
                
                        compareButton.setText("Run");
                        compareButton.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                compareButtonActionPerformed(evt);
                            }
                        });
                        GridBagConstraints gbc_compareButton = new GridBagConstraints();
                        gbc_compareButton.insets = new Insets(0, 0, 0, 5);
                        gbc_compareButton.gridwidth = 12;
                        gbc_compareButton.gridx = 0;
                        gbc_compareButton.gridy = 5;
                        compareDataPanel.add(compareButton, gbc_compareButton);

        revertChangesetPanel.setName(""); // NOI18N

        jTabbedPane1.addTab("Revert Changeset", revertChangesetPanel);
        GridBagLayout gbl_revertChangesetPanel = new GridBagLayout();
        gbl_revertChangesetPanel.columnWidths = new int[]{120, 225, 0};
        gbl_revertChangesetPanel.rowHeights = new int[]{78, 19, 110, 25, 0};
        gbl_revertChangesetPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_revertChangesetPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        revertChangesetPanel.setLayout(gbl_revertChangesetPanel);
        changesetLabel = new javax.swing.JLabel();
        
                changesetLabel.setText("Changeset ID");
                GridBagConstraints gbc_changesetLabel = new GridBagConstraints();
                gbc_changesetLabel.anchor = GridBagConstraints.EAST;
                gbc_changesetLabel.insets = new Insets(0, 0, 5, 5);
                gbc_changesetLabel.gridx = 0;
                gbc_changesetLabel.gridy = 1;
                revertChangesetPanel.add(changesetLabel, gbc_changesetLabel);
        revertChangesetField = new javax.swing.JTextField();
        
                revertChangesetField.setName("usernameField"); // NOI18N
                GridBagConstraints gbc_revertChangesetField = new GridBagConstraints();
                gbc_revertChangesetField.anchor = GridBagConstraints.NORTH;
                gbc_revertChangesetField.fill = GridBagConstraints.HORIZONTAL;
                gbc_revertChangesetField.insets = new Insets(0, 0, 5, 0);
                gbc_revertChangesetField.gridx = 1;
                gbc_revertChangesetField.gridy = 1;
                revertChangesetPanel.add(revertChangesetField, gbc_revertChangesetField);
        revertButton = new javax.swing.JButton();
        
                revertButton.setText("Run");
                revertButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        revertButtonActionPerformed(evt);
                    }
                });
                GridBagConstraints gbc_revertButton = new GridBagConstraints();
                gbc_revertButton.gridwidth = 2;
                gbc_revertButton.anchor = GridBagConstraints.NORTH;
                gbc_revertButton.gridx = 0;
                gbc_revertButton.gridy = 3;
                revertChangesetPanel.add(revertButton, gbc_revertButton);

        taskOutput.setColumns(20);
        taskOutput.setRows(5);
        jScrollPane1.setViewportView(taskOutput);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        						.addComponent(jTabbedPane1, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
        						.addComponent(jScrollPane1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE))
        					.addGap(12))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(279)
        					.addComponent(exitButton, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
        					.addGap(280)))
        			.addContainerGap())
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(exitButton)
        			.addContainerGap())
        );
        getContentPane().setLayout(layout);

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
                if (!UnzipGTFS(null, new URL(fileDirTextField.getText()))) { //try to unzip from the URL
                    JOptionPane.showMessageDialog(this, "Unable to unzip from URL. Please try again with another URL.");
                    return;
                }
            } catch (MalformedURLException ex) {
                JOptionPane.showMessageDialog(this, "Invalid URL. Please try again with another URL.");
                System.err.println("Error: " + ex.getLocalizedMessage());
                return;
            }
            _fileDir = new File("GTFS_Temp").getAbsolutePath() + System.getProperty("file.separator");//"\\"; //set the actual location to the GTFS_Temp folder
        } else if (rbFileFolder.isSelected()) { //else user selected a local file/folder
            if (fileDirTextField.getText().toLowerCase().contains(".zip")) { //if a zip file was selected
                if (!UnzipGTFS(chooser.getSelectedFile(), null)) { //unzip it to a temporary folder
                    JOptionPane.showMessageDialog(this, "Unable to unzip from file. Please try again with another file.");
                    return;
                }
                _fileDir = new File("GTFS_Temp").getAbsolutePath() + System.getProperty("file.separator");//"\\"; //set the actual location to the GTFS_Temp folder
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

        //can't leave blank
        try {
            _operatorAlias = operatorAliasField.getText();
            _operatorName = operatorNameField.getText();
            
            _operatorNameAbbreviate = operatorNameAbbField.getText();

            if (!_operatorName.isEmpty() && !_operatorNameAbbreviate.isEmpty() && !_fileDir.isEmpty()) {
                new OperatorInfo(_operatorName, _operatorNameAbbreviate, _operatorAlias, _operatorNtdId, _gtfsIdDigit, _fileDir);

                progressMonitor = new ProgressMonitor(MainForm.this, "Comparing GTFS and OSM data", "", 0, 100);
                progressMonitor.setProgress(0);
                compareButton.setEnabled(false);
                compareTask = new CompareData(progressMonitor, taskOutput);
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
    private boolean UnzipGTFS(File zipFile, URL zipURL) {
        //TODO display a progress bar to user so they know a file is being unzipped

        File unzipFolder = new File("GTFS_Temp");
        String unzipLocation = unzipFolder.getAbsolutePath() + System.getProperty("file.separator"); //"\\"; //temporary folder to store unzipped files
        try {
            unzipFolder.mkdir(); //create the directory if not already created
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
            	if (!(Files.probeContentType(zipFile.toPath())).equals("application/zip"))
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
    private edu.usf.cutr.go_sync.gui.object.AutoCompleteTextField operatorNameField;
    private javax.swing.JFileChooser chooser;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel OperatorAbbLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel changesetLabel;
    private javax.swing.JButton compareButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField fileDirTextField;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField gtfsIdDigitField;
    private javax.swing.JLabel jLabel1;
    private JSpinner distanceThreshold;
    private javax.swing.JPanel compareDataPanel;
    private javax.swing.JPanel revertChangesetPanel;
    private javax.swing.JPanel gtfsDataPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField operatorAliasField;
    private javax.swing.JTextField operatorNTDIDField;
    private javax.swing.JTextField operatorNameAbbField;
    private javax.swing.JLabel operatorNameLabel;
    private javax.swing.JLabel operatorAliasLabel;
    private javax.swing.JRadioButton rbFileFolder;
    private javax.swing.JRadioButton rbURL;
    private javax.swing.JButton revertButton;
    private javax.swing.JTextField revertChangesetField;
    private javax.swing.JTextArea taskOutput;
    private javax.swing.JLabel operatorNTDIDLabel;
    private javax.swing.JLabel gtfsIdDigitLabel;
    // End of variables declaration//GEN-END:variables
}
