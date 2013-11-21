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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import edu.usf.cutr.go_sync.object.OperatorInfo;
import edu.usf.cutr.go_sync.task.CompareData;
import edu.usf.cutr.go_sync.task.OsmTask;
import edu.usf.cutr.go_sync.task.RevertChangeset;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import edu.usf.cutr.go_sync.io.DefaultOperatorReader;
import edu.usf.cutr.go_sync.object.DefaultOperator;
import java.awt.event.ItemEvent;
import java.awt.event.KeyListener;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;

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
        initComponents();

        DefaultOperatorReader reader = new DefaultOperatorReader(); //create a new reader
        ops = reader.readOperators(new File("operators.csv").getAbsolutePath()); //read a file with operator info for autocompletion

        //TODO Fix textfield with ops is null (operators.csv doesn't exist)
        
        List l = new ArrayList(); //create a new list to store operator names
        l.add(""); //first entry in the list will be blank
        if (ops != null) {
            for (DefaultOperator op : ops) { //for each operator
                l.add(op.getOperatorName()); //add their name to the list for autocompletion
            }
        }

        fileDirTextField.setText("/home/reuben/downloads/SEQ_GTFS");
        
        //create a new textfield with autocomplete for operator names
        operatorNameField = new edu.usf.cutr.go_sync.gui.object.AutoCompleteTextField(l);
        operatorNameField.setText("Translink SEQ");
        //add the textfield to the panel
        compareDataPanel.add(operatorNameField, "4, 2, 5, 1, fill, top");
        
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

        operatorNameField.requestFocusInWindow(); //set the cursor in the operator name autocomplete text field
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        exitButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        compareDataPanel = new javax.swing.JPanel();
        operatorNameLabel = new javax.swing.JLabel();
        OperatorAbbLabel = new javax.swing.JLabel();
        operatorNameAbbField = new javax.swing.JTextField();
        compareButton = new javax.swing.JButton();
        operatorNTDIDLabel = new javax.swing.JLabel();
        operatorNTDIDField = new javax.swing.JTextField();
        gtfsIdDigitLabel = new javax.swing.JLabel();
        gtfsIdDigitField = new javax.swing.JTextField();
        operatorAliasLabel = new javax.swing.JLabel();
        operatorAliasField = new javax.swing.JTextField();
        gtfsDataPanel = new javax.swing.JPanel();
        rbURL = new javax.swing.JRadioButton();
        rbFileFolder = new javax.swing.JRadioButton();
        fileNameLabel = new javax.swing.JLabel();
        fileDirTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        changesetLabel = new javax.swing.JLabel();
        revertChangesetField = new javax.swing.JTextField();
        revertButton = new javax.swing.JButton();
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
        compareDataPanel.setLayout(new FormLayout(new ColumnSpec[] {
        		FormFactory.RELATED_GAP_COLSPEC,
        		ColumnSpec.decode("121px"),
        		FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        		ColumnSpec.decode("10px"),
        		ColumnSpec.decode("131px"),
        		ColumnSpec.decode("3px"),
        		FormFactory.UNRELATED_GAP_COLSPEC,
        		ColumnSpec.decode("76px"),
        		FormFactory.UNRELATED_GAP_COLSPEC,
        		ColumnSpec.decode("50px"),
        		FormFactory.RELATED_GAP_COLSPEC,
        		ColumnSpec.decode("120px"),
        		FormFactory.RELATED_GAP_COLSPEC,
        		ColumnSpec.decode("30px"),},
        	new RowSpec[] {
        		FormFactory.PARAGRAPH_GAP_ROWSPEC,
        		RowSpec.decode("19px"),
        		RowSpec.decode("21px"),
        		RowSpec.decode("19px"),
        		FormFactory.PARAGRAPH_GAP_ROWSPEC,
        		RowSpec.decode("95px"),
        		FormFactory.PARAGRAPH_GAP_ROWSPEC,
        		RowSpec.decode("25px"),}));

        operatorNameLabel.setText("Operator Full Name (*)");
        compareDataPanel.add(operatorNameLabel, "2, 2, 3, 1, fill, top");

        OperatorAbbLabel.setText("Operator Abbreviation (*)");
        compareDataPanel.add(OperatorAbbLabel, "10, 2, 3, 1, right, bottom");

        operatorNameAbbField.setName("usernameField"); // NOI18N
        operatorNameAbbField.setText("Translink");
        compareDataPanel.add(operatorNameAbbField, "12, 2, 3, 1, center, top");
        operatorNameAbbField.getAccessibleContext().setAccessibleName("operatorNameAbbField");

        compareButton.setText("Run");
        compareButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compareButtonActionPerformed(evt);
            }
        });
        compareDataPanel.add(compareButton, "6, 8, 3, 1, left, top");

        operatorNTDIDLabel.setText("Operator NTD ID");
        compareDataPanel.add(operatorNTDIDLabel, "8, 4, 3, 1, left, top");

        operatorNTDIDField.setName("usernameField"); // NOI18N
        compareDataPanel.add(operatorNTDIDField, "10, 4, fill, top");
        operatorNTDIDField.getAccessibleContext().setAccessibleName("OperatorNTDIDField");

        gtfsIdDigitLabel.setText("Length of GTFS Stop IDs");
        compareDataPanel.add(gtfsIdDigitLabel, "12, 4, fill, top");

        gtfsIdDigitField.setName("usernameField"); // NOI18N
        compareDataPanel.add(gtfsIdDigitField, "14, 4, fill, top");

        operatorAliasLabel.setText("Operator Alias");
        compareDataPanel.add(operatorAliasLabel, "2, 4, right, top");

        operatorAliasField.setName("usernameField"); // NOI18N
        compareDataPanel.add(operatorAliasField, "2, 4, 5, 1, fill, top");

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

        fileNameLabel.setText("Folder or Zip File (*)");

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(gtfsDataPanel);
        gtfsDataPanel.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(fileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(fileDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(172, 172, 172)
                        .addComponent(rbFileFolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbURL)))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {rbFileFolder, rbURL});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbURL)
                    .addComponent(rbFileFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileNameLabel)
                    .addComponent(fileDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addContainerGap())
        );

        fileNameLabel.getAccessibleContext().setAccessibleParent(gtfsDataPanel);
        fileDirTextField.getAccessibleContext().setAccessibleParent(gtfsDataPanel);
        browseButton.getAccessibleContext().setAccessibleParent(gtfsDataPanel);

        compareDataPanel.add(gtfsDataPanel, "2, 6, 13, 1, fill, top");

        jLabel1.setText("Fields marked with an asterisk(*) are required");
        compareDataPanel.add(jLabel1, "2, 6, 7, 3, center, bottom");

        jTabbedPane1.addTab("Compare Data", compareDataPanel);

        jPanel2.setName(""); // NOI18N

        changesetLabel.setText("Changeset ID");

        revertChangesetField.setName("usernameField"); // NOI18N

        revertButton.setText("Run");
        revertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(changesetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(revertChangesetField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(277, 277, 277)
                        .addComponent(revertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(240, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(revertChangesetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changesetLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(revertButton)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Revert Changeset", jPanel2);

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
                _fileDir = fileDirTextField.getText(); //else use the folder selected with GTFS files in it
                //TODO - validate that a folder was selected and that it does have GTFS files
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

            //TODO test that URL and file are actually ZIP files

            if (zipFile == null) {
                System.out.println("Unzipping " + zipURL.toString() + " to " + unzipLocation);
                zip = new ZipInputStream(zipURL.openStream());
            } else {
                System.out.println("Unzipping " + zipFile.getAbsolutePath() + " to " + unzipLocation);
                zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            }

            ZipEntry next_file;

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
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
        } catch (InstantiationException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Error setting LookAndFeel: " + e.getLocalizedMessage());
        }

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
    private javax.swing.JPanel compareDataPanel;
    private javax.swing.JPanel jPanel2;
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
