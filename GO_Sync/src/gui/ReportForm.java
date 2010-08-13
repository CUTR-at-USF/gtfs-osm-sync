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
 * ReportForm.java
 *
 * Created on Jul 20, 2010, 8:18:58 PM
 */

package gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ButtonGroup;
import object.Stop;
import osm.HttpRequest;

/**
 *
 * @author Khoa Tran
 */
public class ReportForm extends javax.swing.JFrame {

    private Hashtable report;

    private HashSet<Stop> upload, modify, delete;

    private DefaultListModel gtfsIDs = new DefaultListModel();
    private DefaultListModel gtfsIDAll = new DefaultListModel();
    private DefaultListModel gtfsIDUploadConflict = new DefaultListModel();
    private DefaultListModel gtfsIDUploadNoConflict = new DefaultListModel();
    private DefaultListModel gtfsIDModify = new DefaultListModel();
    private DefaultListModel gtfsIDNoUpload = new DefaultListModel();
    private DefaultListModel gtfsDetailsKey = new DefaultListModel();
    private DefaultListModel gtfsDetailsValue = new DefaultListModel();
    private DefaultListModel osmIDs = new DefaultListModel();
    private DefaultListModel osmDetailsKey = new DefaultListModel();
    private DefaultListModel osmDetailsValue = new DefaultListModel();
    private DefaultListModel newDetailsKey = new DefaultListModel();
    private DefaultListModel newDetailsValue = new DefaultListModel();
    private ButtonGroup bGroup = new ButtonGroup();

    // map between gtfs id and gtfs stop
    private Hashtable agencyStops = new Hashtable();
    private Hashtable finalStops = new Hashtable();
    private Hashtable osmStops = new Hashtable();

    private HttpRequest osmRequest = new HttpRequest();

    // List of stops from agency data
    private ArrayList<Stop> agencyData;

    /** Creates new form ReportForm */
    public ReportForm(List<Stop> aData, Hashtable r, HashSet<Stop>u, HashSet<Stop>m, HashSet<Stop>d) {
        agencyData = new ArrayList<Stop>();
        agencyData.addAll(aData);

        report = new Hashtable();
        report.putAll(r);

        upload = new HashSet<Stop>();
        upload.addAll(u);

        modify = new HashSet<Stop>();
        modify.addAll(m);

        delete = new HashSet<Stop>();
        delete.addAll(d);

        for (int i=0; i<agencyData.size(); i++) {
            Stop st = agencyData.get(i);
            gtfsIDs.add(i, st.getStopID());
            agencyStops.put(st.getStopID(), st);
        }

        int ai=0, uci=0, unci=0, mi=0, nui=0;
        HashSet<Stop> reportKeys = new HashSet<Stop>();
        reportKeys.addAll(report.keySet());
        Iterator it = reportKeys.iterator();
        while (it.hasNext()){
            Stop st = new Stop((Stop)it.next());
            finalStops.put(st.getStopID(), st);
            gtfsIDAll.add(ai, st.getStopID());
            ai++;
            if (st.getTag("REPORT_CATEGORY").equals("UPLOAD_CONFLICT")) {
                gtfsIDUploadConflict.add(uci, st.getStopID());
                uci++;
            } else if (st.getTag("REPORT_CATEGORY").equals("UPLOAD_NO_CONFLICT")) {
                gtfsIDUploadNoConflict.add(unci, st.getStopID());
                unci++;
            } else if (st.getTag("REPORT_CATEGORY").equals("MODIFY")) {
                gtfsIDModify.add(mi, st.getStopID());
                mi++;
            } else if (st.getTag("REPORT_CATEGORY").equals("NOTHING_NEW")) {
                gtfsIDNoUpload.add(nui, st.getStopID());
                nui++;
            }

        }
        
        initComponents();
    }

    public void updateReportMessage(Stop s) {
        Stop st = new Stop(s);
        reportMessage.setText(st.getTag("REPORT"));
    }

    public void updateOsmIDList(Stop fs) {
        Stop fStop = new Stop(fs);
        osmIDs.clear();
        osmStops = new Hashtable();
        if (!report.get(fStop).equals("none")){
            ArrayList<Stop> ls = new ArrayList<Stop>();
            ls.addAll((ArrayList<Stop>)report.get(fStop));
            for(int i=0; i<ls.size(); i++){
                Stop oStop = ls.get(i);
                if (i==0) {
                    updateReportMessage(oStop);
                    updateOsmDetailsList(oStop);
                }       // the first node
                osmIDs.add(i, oStop.getTag("OSM_NODE_ID"));
                osmStops.put(oStop.getTag("OSM_NODE_ID"), oStop);
            }
        }
        else {
            updateReportMessage(fStop);
            osmDetailsKey.clear();
            osmDetailsValue.clear();
        }
    }

    public void updateOsmDetailsList(Stop os) {
        Stop s = new Stop(os);
        osmDetailsKey.clear();
        osmDetailsValue.clear();
        
        int count=0;
        osmDetailsKey.add(count, "OSM ID");
        osmDetailsValue.add(count, s.getTag("OSM_NODE_ID"));
        count++;
        osmDetailsKey.add(count, "name");
        osmDetailsValue.add(count, s.getStopName());
        count++;
        osmDetailsKey.add(count, "lat");
        osmDetailsValue.add(count, s.getLat());
        count++;
        osmDetailsKey.add(count, "lon");
        osmDetailsValue.add(count, s.getLon());
        count++;

        s.removeTag("OSM_NODE_ID");
        s.removeTag("REPORT");
        s.removeTag("version");
        HashSet<String> keys = s.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = s.getTag(k);
            if (!v.equals("none") && !v.equals("")) {
                osmDetailsKey.add(count, k);
                osmDetailsValue.add(count, v);
            }
        }
    }

    public void updateGtfsDetailsList(Stop gs) {
        Stop s = new Stop(gs);
        gtfsDetailsKey.clear();
        gtfsDetailsValue.clear();

        int count=0;
        gtfsDetailsKey.add(count, "name");
        gtfsDetailsValue.add(count, s.getStopName());
        count++;
        gtfsDetailsKey.add(count, "lat");
        gtfsDetailsValue.add(count, s.getLat());
        count++;
        gtfsDetailsKey.add(count, "lon");
        gtfsDetailsValue.add(count, s.getLon());
        count++;

        s.removeTag("REPORT");
        s.removeTag("version");
        HashSet<String> keys = s.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = s.getTag(k);
            if (!v.equals("none") && !v.equals("")) {
                gtfsDetailsKey.add(count, k);
                gtfsDetailsValue.add(count, v);
            }
        }
    }

    public void updateNewDetailsList(Stop ns) {
        Stop s = new Stop(ns);
        newDetailsKey.clear();
        newDetailsValue.clear();

        int count=0;
        newDetailsKey.add(count, "name");
        newDetailsValue.add(count, s.getStopName());
        count++;
        newDetailsKey.add(count, "lat");
        newDetailsValue.add(count, s.getLat());
        count++;
        newDetailsKey.add(count, "lon");
        newDetailsValue.add(count, s.getLon());
        count++;

        s.removeTag("REPORT");
        s.removeTag("version");
        HashSet<String> keys = s.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = s.getTag(k);
            if (!v.equals("none") && !v.equals("")) {
                newDetailsKey.add(count, k);
                newDetailsValue.add(count, v);
            }
        }
    }

    public void clearAllLists(){
        gtfsDetailsKey.clear();
        gtfsDetailsValue.clear();
        newDetailsKey.clear();
        newDetailsValue.clear();
        osmIDs.clear();
        osmDetailsKey.clear();
        osmDetailsValue.clear();
    }

    public void gtfsActivated(){
        String gtfsID = (String)gtfsIDList.getSelectedValue();
        if (gtfsID!=null) {
            updateGtfsDetailsList((Stop)agencyStops.get(gtfsID));
            updateNewDetailsList((Stop)finalStops.get(gtfsID));
            updateOsmIDList((Stop)finalStops.get(gtfsID));
        } else {
            clearAllLists();
            if (gtfsIDs.getSize()==0) reportMessage.setText("There is no GTFS stops");
        }
    }

    public void osmActivated(){
        String osmID = (String)osmIDList.getSelectedValue();
        updateOsmDetailsList((Stop)osmStops.get(osmID));
        updateReportMessage((Stop)osmStops.get(osmID));
    }

    public void updateGtfsIdList(String criterion) {
/*        gtfsIDs.clear();
        HashSet<Stop> reportKeys = new HashSet<Stop>();
        reportKeys.addAll(report.keySet());
        Iterator it = reportKeys.iterator();
        int i=0;
        while (it.hasNext()){
            Stop st = new Stop((Stop)it.next());
            if (criterion.equals("ALL") || st.getTag("REPORT_CATEGORY").equals(criterion)) {
                gtfsIDs.add(i, st.getStopID());
                i++;
            }
        }*/
        DefaultListModel tList = (DefaultListModel)gtfsIDList.getModel();
        if (tList.getSize()>0) gtfsIDList.setSelectedIndex(0);
        totalStopsLabel.setText(Integer.toString(tList.getSize()));
        gtfsActivated();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        uploadButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        osmIDList = new javax.swing.JList(osmIDs);
        jScrollPane3 = new javax.swing.JScrollPane();
        reportMessage = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        osmDetailsValueList = new javax.swing.JList(osmDetailsValue);
        jScrollPane5 = new javax.swing.JScrollPane();
        gtfsDetailsKeyList = new javax.swing.JList(gtfsDetailsKey);
        jScrollPane6 = new javax.swing.JScrollPane();
        newDetailsKeyList = new javax.swing.JList(newDetailsKey);
        modifyLeftButton = new javax.swing.JButton();
        gtfsStopIDLabel = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        gtfsIDList = new javax.swing.JList(gtfsIDs);
        modifyRightButton = new javax.swing.JButton();
        osmStopIDLabel = new javax.swing.JLabel();
        newStopIDLabel = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        osmDetailsKeyList = new javax.swing.JList(osmDetailsKey);
        jScrollPane9 = new javax.swing.JScrollPane();
        newDetailsValueList = new javax.swing.JList(newDetailsValue);
        jScrollPane10 = new javax.swing.JScrollPane();
        gtfsDetailsValueList = new javax.swing.JList(gtfsDetailsValue);
        allStopsRadioButton = new javax.swing.JRadioButton();
        allStopsRadioButton.setEnabled(true);bGroup.add(allStopsRadioButton);
        uploadConflictStopsRadioButton = new javax.swing.JRadioButton();
        bGroup.add(uploadConflictStopsRadioButton);
        uploadNoConflictStopsRadioButton = new javax.swing.JRadioButton();
        bGroup.add(uploadNoConflictStopsRadioButton);
        totalStopsLabel = new javax.swing.JLabel();
        totalStopsLabel.setText(agencyData.size()+" stops");
        modifyStopsRadioButton = new javax.swing.JRadioButton();
        bGroup.add(modifyStopsRadioButton);
        noUploadStopsRadioButton = new javax.swing.JRadioButton();
        bGroup.add(noUploadStopsRadioButton);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Report");
        setName("reportForm"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        uploadButton.setText("Upload");
        uploadButton.setName("uploadButton"); // NOI18N
        uploadButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                uploadButtonMouseClicked(evt);
            }
        });
        uploadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadButtonActionPerformed(evt);
            }
        });

        osmIDList.setBorder(new javax.swing.border.MatteBorder(null));
        osmIDList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmIDList.setNextFocusableComponent(osmDetailsKeyList);
        osmIDList.setSelectedIndex(0);
        osmIDList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmIDListMouseClicked(evt);
            }
        });
        osmIDList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmIDListKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(osmIDList);
        osmIDList.getAccessibleContext().setAccessibleName("osmIDList");

        reportMessage.setColumns(20);
        reportMessage.setFont(new java.awt.Font("Times New Roman", 0, 14));
        reportMessage.setRows(5);
        jScrollPane3.setViewportView(reportMessage);
        reportMessage.getAccessibleContext().setAccessibleName("reportMessage");

        osmDetailsValueList.setBorder(new javax.swing.border.MatteBorder(null));
        osmDetailsValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(osmDetailsValueList);
        osmDetailsValueList.getAccessibleContext().setAccessibleName("osmDetailsValueList");

        gtfsDetailsKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsDetailsKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsDetailsKeyList.setNextFocusableComponent(gtfsIDList);
        gtfsDetailsKeyList.setSelectedIndex(0);
        jScrollPane5.setViewportView(gtfsDetailsKeyList);
        gtfsDetailsKeyList.getAccessibleContext().setAccessibleName("gtfsDetailsKeyList");

        newDetailsKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        newDetailsKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newDetailsKeyList.setSelectedIndex(0);
        jScrollPane6.setViewportView(newDetailsKeyList);
        newDetailsKeyList.getAccessibleContext().setAccessibleName("newDetailsKeyList");

        modifyLeftButton.setText("Modify");
        modifyLeftButton.setName("okButton"); // NOI18N
        modifyLeftButton.setNextFocusableComponent(gtfsDetailsKeyList);
        modifyLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyLeftButtonActionPerformed(evt);
            }
        });

        gtfsStopIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        gtfsStopIDLabel.setText("GTFS Stop");

        gtfsIDList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsIDList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsIDList.setMaximumSize(null);
        gtfsIDList.setMinimumSize(null);
        gtfsIDList.setNextFocusableComponent(osmIDList);
        gtfsIDList.setSelectedIndex(0);
        gtfsIDList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsIDListMouseClicked(evt);
            }
        });
        gtfsIDList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsIDListKeyReleased(evt);
            }
        });
        jScrollPane7.setViewportView(gtfsIDList);
        gtfsIDList.getAccessibleContext().setAccessibleName("gtfsIDList");

        modifyRightButton.setText("Modify");
        modifyRightButton.setName("okButton"); // NOI18N
        modifyRightButton.setNextFocusableComponent(modifyLeftButton);
        modifyRightButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                modifyRightButtonMouseClicked(evt);
            }
        });
        modifyRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyRightButtonActionPerformed(evt);
            }
        });

        osmStopIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        osmStopIDLabel.setText("OSM Stop");

        newStopIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        newStopIDLabel.setText("New Stop");

        osmDetailsKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        osmDetailsKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmDetailsKeyList.setNextFocusableComponent(modifyRightButton);
        osmDetailsKeyList.setSelectedIndex(0);
        jScrollPane8.setViewportView(osmDetailsKeyList);
        osmDetailsKeyList.getAccessibleContext().setAccessibleName("osmDetailsKeyList");

        newDetailsValueList.setBorder(new javax.swing.border.MatteBorder(null));
        newDetailsValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane9.setViewportView(newDetailsValueList);
        newDetailsValueList.getAccessibleContext().setAccessibleName("newDetailsValueList");

        gtfsDetailsValueList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsDetailsValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane10.setViewportView(gtfsDetailsValueList);
        gtfsDetailsValueList.getAccessibleContext().setAccessibleName("gtfsDetailsValueList");

        allStopsRadioButton.setText("All");
        allStopsRadioButton.setName("stopCategory"); // NOI18N
        allStopsRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                allStopsRadioButtonMouseClicked(evt);
            }
        });

        uploadConflictStopsRadioButton.setText("Upload Stops with Conflict");
        uploadConflictStopsRadioButton.setName("stopCategory"); // NOI18N
        uploadConflictStopsRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                uploadConflictStopsRadioButtonMouseClicked(evt);
            }
        });

        uploadNoConflictStopsRadioButton.setText("Upload Stops with no Conflict");
        uploadNoConflictStopsRadioButton.setName("stopCategory"); // NOI18N
        uploadNoConflictStopsRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                uploadNoConflictStopsRadioButtonMouseClicked(evt);
            }
        });

        modifyStopsRadioButton.setText("No Upload Stops but Modify");
        modifyStopsRadioButton.setName("stopCategory"); // NOI18N
        modifyStopsRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                modifyStopsRadioButtonMouseClicked(evt);
            }
        });

        noUploadStopsRadioButton.setText("No Upload");
        noUploadStopsRadioButton.setName("stopCategory"); // NOI18N
        noUploadStopsRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                noUploadStopsRadioButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addComponent(gtfsStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addGap(267, 267, 267)
                .addComponent(newStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addGap(289, 289, 289)
                .addComponent(osmStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addGap(150, 150, 150))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(modifyLeftButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modifyRightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(modifyStopsRadioButton)
                            .addComponent(uploadNoConflictStopsRadioButton)
                            .addComponent(allStopsRadioButton)
                            .addComponent(uploadConflictStopsRadioButton)
                            .addComponent(totalStopsLabel)
                            .addComponent(noUploadStopsRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(214, 214, 214))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(574, 574, 574))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(osmStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gtfsStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addComponent(modifyLeftButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(131, 131, 131)
                        .addComponent(modifyRightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(totalStopsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(allStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(uploadConflictStopsRadioButton)
                        .addGap(3, 3, 3)
                        .addComponent(uploadNoConflictStopsRadioButton)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifyStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(noUploadStopsRadioButton)))
                .addContainerGap(127, Short.MAX_VALUE))
        );

        uploadButton.getAccessibleContext().setAccessibleName("uploadButton");
        modifyLeftButton.getAccessibleContext().setAccessibleName("modifyLeftButton");
        gtfsStopIDLabel.getAccessibleContext().setAccessibleName("gtfsStopIDLabel");
        modifyRightButton.getAccessibleContext().setAccessibleName("modifyRightButton");
        osmStopIDLabel.getAccessibleContext().setAccessibleName("osmStopIDLabel");
        newStopIDLabel.getAccessibleContext().setAccessibleName("newStopIDLabel");

        getAccessibleContext().setAccessibleName("report");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_uploadButtonActionPerformed

    private void modifyLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyLeftButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_modifyLeftButtonActionPerformed

    private void modifyRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyRightButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_modifyRightButtonActionPerformed

    private void gtfsIDListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsIDListKeyReleased
        // TODO add your handling code here:
        gtfsActivated();
    }//GEN-LAST:event_gtfsIDListKeyReleased

    private void osmIDListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmIDListKeyReleased
        // TODO add your handling code here:
        osmActivated();
    }//GEN-LAST:event_osmIDListKeyReleased

    private void gtfsIDListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsIDListMouseClicked
        // TODO add your handling code here:
        gtfsActivated();
    }//GEN-LAST:event_gtfsIDListMouseClicked

    private void osmIDListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmIDListMouseClicked
        // TODO add your handling code here:
        osmActivated();
    }//GEN-LAST:event_osmIDListMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        gtfsActivated();
    }//GEN-LAST:event_formWindowOpened

    private void modifyRightButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modifyRightButtonMouseClicked
        // TODO add your handling code here:
        if (!osmDetailsKeyList.isSelectionEmpty()) {
            int index = osmDetailsKeyList.getSelectedIndex();
            String k = (String)osmDetailsKey.get(index);
            String v = (String)osmDetailsValue.get(index);
            if (newDetailsKey.contains(k)){
                newDetailsValue.set(newDetailsKey.indexOf(k), v);
            } else {
                newDetailsKey.addElement(k);
                newDetailsValue.addElement(v);
            }
        }
    }//GEN-LAST:event_modifyRightButtonMouseClicked

    private void uploadButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadButtonMouseClicked
        // TODO add your handling code here:
        osmRequest.checkVersion();
        osmRequest.createChangeSet();
        osmRequest.createChunks(upload, modify, delete);
        osmRequest.closeChangeSet();
    }//GEN-LAST:event_uploadButtonMouseClicked

    private void allStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_allStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDAll);
        updateGtfsIdList("ALL");
    }//GEN-LAST:event_allStopsRadioButtonMouseClicked

    private void noUploadStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noUploadStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDNoUpload);
        updateGtfsIdList("NOTHING_NEW");
    }//GEN-LAST:event_noUploadStopsRadioButtonMouseClicked

    private void uploadConflictStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadConflictStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDUploadConflict);
        updateGtfsIdList("UPLOAD_CONFLICT");
    }//GEN-LAST:event_uploadConflictStopsRadioButtonMouseClicked

    private void uploadNoConflictStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadNoConflictStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDUploadNoConflict);
        updateGtfsIdList("UPLOAD_NO_CONFLICT");
    }//GEN-LAST:event_uploadNoConflictStopsRadioButtonMouseClicked

    private void modifyStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modifyStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDModify);
        updateGtfsIdList("MODIFY");
    }//GEN-LAST:event_modifyStopsRadioButtonMouseClicked

    /**
    * @param args the command line arguments
    */
    /*
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ReportForm().setVisible(true);
            }
        });
    }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allStopsRadioButton;
    private javax.swing.JList gtfsDetailsKeyList;
    private javax.swing.JList gtfsDetailsValueList;
    private javax.swing.JList gtfsIDList;
    private javax.swing.JLabel gtfsStopIDLabel;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JButton modifyLeftButton;
    private javax.swing.JButton modifyRightButton;
    private javax.swing.JRadioButton modifyStopsRadioButton;
    private javax.swing.JList newDetailsKeyList;
    private javax.swing.JList newDetailsValueList;
    private javax.swing.JLabel newStopIDLabel;
    private javax.swing.JRadioButton noUploadStopsRadioButton;
    private javax.swing.JList osmDetailsKeyList;
    private javax.swing.JList osmDetailsValueList;
    private javax.swing.JList osmIDList;
    private javax.swing.JLabel osmStopIDLabel;
    private javax.swing.JTextArea reportMessage;
    private javax.swing.JLabel totalStopsLabel;
    private javax.swing.JButton uploadButton;
    private javax.swing.JRadioButton uploadConflictStopsRadioButton;
    private javax.swing.JRadioButton uploadNoConflictStopsRadioButton;
    // End of variables declaration//GEN-END:variables

}
