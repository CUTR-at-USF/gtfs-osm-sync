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

import io.WriteFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ButtonGroup;
import object.RelationMember;
import object.Route;
import object.Stop;
import osm.HttpRequest;

/**
 *
 * @author Khoa Tran
 */
public class ReportForm extends javax.swing.JFrame {

    private static final String FILE_NAME_OUT_UPLOAD_REVISE = "UPLOAD_REVISE.txt";
    private static final String FILE_NAME_OUT_MODIFY_REVISE = "MODIFY_REVISE.txt";
    private static final String FILE_NAME_OUT_DELETE_REVISE = "DELETE_REVISE.txt";
    private static final String FILE_NAME_OUT_NOUPLOAD_REVISE = "NOUPLOAD_REVISE.txt";
    private static final String FILE_NAME_OUT_REPORT_REVISE = "REPORT_REVISE.txt";

    private Hashtable report;

    private HashSet<Stop> upload, modify, delete;

    private DefaultListModel gtfsRoutes = new DefaultListModel();
    private DefaultListModel gtfsRouteNoUpload = new DefaultListModel();
    private DefaultListModel gtfsRouteNew = new DefaultListModel();
    private DefaultListModel gtfsRouteModify = new DefaultListModel();
    private DefaultListModel gtfsTagKey = new DefaultListModel();
    private DefaultListModel gtfsTagValue = new DefaultListModel();
    private DefaultListModel gtfsMemberValue = new DefaultListModel();
    private DefaultListModel osmRoutes = new DefaultListModel();
    private DefaultListModel osmTagKey = new DefaultListModel();
    private DefaultListModel osmTagValue = new DefaultListModel();
    private DefaultListModel osmMemberValue = new DefaultListModel();
    private DefaultListModel newTagKey = new DefaultListModel();
    private DefaultListModel newTagValue = new DefaultListModel();
    private DefaultListModel newMemberValue = new DefaultListModel();
    
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

    private Hashtable finalRoutes;

    // List of stops from agency data
    private ArrayList<Stop> agencyData;

    /** Creates new form ReportForm */
    public ReportForm(List<Stop> aData, Hashtable r, HashSet<Stop>u, HashSet<Stop>m, HashSet<Stop>d, Hashtable routes) {
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

        finalRoutes = new Hashtable();
        finalRoutes.putAll(routes);

        for (int i=0; i<agencyData.size(); i++) {
            Stop st = agencyData.get(i);
            gtfsIDs.add(i, st.getStopID());
            agencyStops.put(st.getStopID(), st);
        }

        int ai=0, uci=0, unci=0, mi=0, nui=0;
        ArrayList<Stop> reportKeys = new ArrayList<Stop>();
        //convert to arrayList for ordering
        reportKeys.addAll(report.keySet());
        //ordering by hashcode
        for (int i=0; i<reportKeys.size()-1; i++) {
            int k=i;
            for (int j=i+1; j<reportKeys.size(); j++) {
                if (reportKeys.get(k).getStopID().hashCode() > reportKeys.get(j).getStopID().hashCode()) {
                    k = j;
                }
            }
            Stop temp = new Stop(reportKeys.get(i));
            reportKeys.set(i, reportKeys.get(k));
            reportKeys.set(k, temp);
        }

        //add data to correct list (categorizing)
        for (int i=0; i<reportKeys.size(); i++) {
            Stop st = reportKeys.get(i);
            finalStops.put(st.getStopID(), st);
            gtfsIDAll.add(ai, st.getStopID());
            ai++;
            String category = st.getReportCategory();
            if (category.equals("UPLOAD_CONFLICT")) {
                gtfsIDUploadConflict.add(uci, st.getStopID());
                uci++;
            } else if (category.equals("UPLOAD_NO_CONFLICT")) {
                gtfsIDUploadNoConflict.add(unci, st.getStopID());
                unci++;
            } else if (category.equals("MODIFY")) {
                gtfsIDModify.add(mi, st.getStopID());
                mi++;
            } else if (category.equals("NOTHING_NEW")) {
                gtfsIDNoUpload.add(nui, st.getStopID());
                nui++;
            }

        }

        // routes
        int ari=0, newri=0, modifyri=0, nouploadri=0;
        ArrayList<String> routeKeys = new ArrayList<String>();
        //convert to arrayList for ordering
        routeKeys.addAll(routes.keySet());
        //ordering by hashcode
        for (int i=0; i<routeKeys.size()-1; i++) {
            int k=i;
            for (int j=i+1; j<routeKeys.size(); j++) {
                if (routeKeys.get(k).hashCode() > routeKeys.get(j).hashCode()) {
                    k = j;
                }
            }
            String temp = routeKeys.get(i);
            routeKeys.set(i, routeKeys.get(k));
            routeKeys.set(k, temp);
        }

        //add data to correct list (categorizing)
        //int ari=0, newri=0, modifyri=0, nouploadri=0;
        for (int i=0; i<routeKeys.size(); i++) {
            String rk = routeKeys.get(i);
            gtfsRoutes.add(ari, rk);
            ari++;
            Route rtemp = (Route)finalRoutes.get(rk);
            String status = rtemp.getStatus();
            if (status.equals("n")) {
                gtfsRouteNew.add(newri, rk);
                newri++;
            } else if (status.equals("m")) {
                gtfsRouteModify.add(modifyri, rk);
                modifyri++;
            } else if (status.equals("e")) {
                gtfsRouteNoUpload.add(nouploadri, rk);
                mi++;
            }
        }
        
        initComponents();
    }

    public void updateReportMessage(Stop s) {
        Stop st = new Stop(s);
        reportMessage.setText(st.getReportText());
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
                osmIDs.add(i, oStop.getOsmId());
                osmStops.put(oStop.getOsmId(), oStop);
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
        osmDetailsKey.add(count, "lat");
        osmDetailsValue.add(count, s.getLat());
        count++;
        osmDetailsKey.add(count, "lon");
        osmDetailsValue.add(count, s.getLon());
        count++;

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
        gtfsDetailsKey.add(count, "lat");
        gtfsDetailsValue.add(count, s.getLat());
        count++;
        gtfsDetailsKey.add(count, "lon");
        gtfsDetailsValue.add(count, s.getLon());
        count++;

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
        newDetailsKey.add(count, "lat");
        newDetailsValue.add(count, s.getLat());
        count++;
        newDetailsKey.add(count, "lon");
        newDetailsValue.add(count, s.getLon());
        count++;

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

    public void gtfsStopActivated(){
        totalStopsLabel.setText(Integer.toString(gtfsIDList.getModel().getSize()));
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

    public void osmStopActivated(){
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
        gtfsStopActivated();
    }

    public void updateGtfsRouteTagList(Route r){
//        Route r = (Route)finalRoutes.get((String)gtfsRouteList.getSelectedValue());
        int count=0;
        HashSet<String> keys = r.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = r.getTag(k);
            System.out.println(k+","+v);
            if (!v.equals("none") && !v.equals("")) {
                gtfsTagKey.add(count, k);
                gtfsTagValue.add(count, v);
            }
            updateGtfsRouteMemberList(r);
        }
    }

    public void updateGtfsRouteMemberList(Route r){
//        Route r = (Route)finalRoutes.get((String)gtfsRouteList.getSelectedValue());
        int count=0;
        HashSet<RelationMember> keys = r.getOsmMembers();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            RelationMember rm = (RelationMember)it.next();
            String v = rm.getType() + ": " +rm.getRef();
            System.out.println(v);
            gtfsMemberValue.add(count, v);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        stopPanel = new javax.swing.JPanel();
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
        removeGtfsButton = new javax.swing.JButton();
        removeUploadButton = new javax.swing.JButton();
        removeOsmButton = new javax.swing.JButton();
        routePanel = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        gtfsTagKeyList = new javax.swing.JList(gtfsTagKey);
        modifyLeftButton1 = new javax.swing.JButton();
        totalRoutesLabel = new javax.swing.JLabel();
        totalStopsLabel.setText(agencyData.size()+" stops");
        jScrollPane12 = new javax.swing.JScrollPane();
        newTagKeyList = new javax.swing.JList(newTagKey);
        removeGtfsButton1 = new javax.swing.JButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        gtfsRouteList = new javax.swing.JList(gtfsRoutes);
        jScrollPane14 = new javax.swing.JScrollPane();
        newTagValueList = new javax.swing.JList(newTagValue);
        jScrollPane15 = new javax.swing.JScrollPane();
        gtfsTagValueList = new javax.swing.JList(gtfsTagValue);
        jScrollPane16 = new javax.swing.JScrollPane();
        osmMemberList = new javax.swing.JList(osmMemberValue);
        newRouteIDLabel = new javax.swing.JLabel();
        removeUploadButton1 = new javax.swing.JButton();
        jScrollPane17 = new javax.swing.JScrollPane();
        osmTagValueList = new javax.swing.JList(osmTagValue);
        modifyRightButton1 = new javax.swing.JButton();
        jScrollPane18 = new javax.swing.JScrollPane();
        osmTagKeyList = new javax.swing.JList(osmTagKey);
        gtfsRouteIDLabel = new javax.swing.JLabel();
        osmRouteIDLabel = new javax.swing.JLabel();
        removeOsmButton1 = new javax.swing.JButton();
        jScrollPane20 = new javax.swing.JScrollPane();
        gtfsMemberList = new javax.swing.JList(gtfsMemberValue);
        jScrollPane21 = new javax.swing.JScrollPane();
        newMemberList = new javax.swing.JList(newMemberValue);
        jScrollPane22 = new javax.swing.JScrollPane();
        osmRouteList = new javax.swing.JList(osmRoutes);
        uploadButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Report");
        setName("reportForm"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
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
        reportMessage.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
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

        totalStopsLabel.setText("hello");

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

        removeGtfsButton.setText("Remove");
        removeGtfsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                removeGtfsButtonMouseClicked(evt);
            }
        });

        removeUploadButton.setText("Remove");

        removeOsmButton.setText("Remove");

        javax.swing.GroupLayout stopPanelLayout = new javax.swing.GroupLayout(stopPanel);
        stopPanel.setLayout(stopPanelLayout);
        stopPanelLayout.setHorizontalGroup(
            stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stopPanelLayout.createSequentialGroup()
                .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stopPanelLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(modifyStopsRadioButton)
                            .addComponent(uploadNoConflictStopsRadioButton)
                            .addComponent(uploadConflictStopsRadioButton)
                            .addComponent(noUploadStopsRadioButton)
                            .addGroup(stopPanelLayout.createSequentialGroup()
                                .addComponent(allStopsRadioButton)
                                .addGap(77, 77, 77)))
                        .addGap(42, 42, 42)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(stopPanelLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(removeGtfsButton)))
                .addContainerGap(261, Short.MAX_VALUE))
            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(stopPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(stopPanelLayout.createSequentialGroup()
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(2, 2, 2)
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(2, 2, 2)
                            .addComponent(modifyLeftButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(removeUploadButton)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(modifyRightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(removeOsmButton)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(stopPanelLayout.createSequentialGroup()
                            .addGap(23, 23, 23)
                            .addComponent(totalStopsLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(stopPanelLayout.createSequentialGroup()
                            .addGap(125, 125, 125)
                            .addComponent(gtfsStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                            .addGap(267, 267, 267)
                            .addComponent(newStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                            .addGap(289, 289, 289)
                            .addComponent(osmStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                            .addGap(116, 116, 116)))
                    .addContainerGap()))
        );
        stopPanelLayout.setVerticalGroup(
            stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stopPanelLayout.createSequentialGroup()
                .addContainerGap(472, Short.MAX_VALUE)
                .addComponent(removeGtfsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(stopPanelLayout.createSequentialGroup()
                        .addComponent(allStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(uploadConflictStopsRadioButton)
                        .addGap(3, 3, 3)
                        .addComponent(uploadNoConflictStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifyStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(noUploadStopsRadioButton)))
                .addGap(19, 19, 19))
            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(stopPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(stopPanelLayout.createSequentialGroup()
                            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(osmStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(newStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(gtfsStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(56, 56, 56)
                            .addComponent(totalStopsLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                                .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(removeUploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(removeOsmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stopPanelLayout.createSequentialGroup()
                            .addComponent(modifyLeftButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(232, 232, 232))
                        .addGroup(stopPanelLayout.createSequentialGroup()
                            .addGap(238, 238, 238)
                            .addComponent(modifyRightButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(166, Short.MAX_VALUE)))
        );

        modifyLeftButton.getAccessibleContext().setAccessibleName("modifyLeftButton");
        gtfsStopIDLabel.getAccessibleContext().setAccessibleName("gtfsStopIDLabel");
        modifyRightButton.getAccessibleContext().setAccessibleName("modifyRightButton");
        osmStopIDLabel.getAccessibleContext().setAccessibleName("osmStopIDLabel");
        newStopIDLabel.getAccessibleContext().setAccessibleName("newStopIDLabel");

        jTabbedPane1.addTab("Stop", stopPanel);

        gtfsTagKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsTagKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsTagKeyList.setNextFocusableComponent(gtfsIDList);
        gtfsTagKeyList.setSelectedIndex(0);
        jScrollPane11.setViewportView(gtfsTagKeyList);

        modifyLeftButton1.setText("Modify");
        modifyLeftButton1.setName("okButton"); // NOI18N
        modifyLeftButton1.setNextFocusableComponent(gtfsDetailsKeyList);
        modifyLeftButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyLeftButton1ActionPerformed(evt);
            }
        });

        totalRoutesLabel.setText("hello");

        newTagKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        newTagKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newTagKeyList.setSelectedIndex(0);
        jScrollPane12.setViewportView(newTagKeyList);

        removeGtfsButton1.setText("Remove");
        removeGtfsButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                removeGtfsButton1MouseClicked(evt);
            }
        });

        gtfsRouteList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsRouteList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsRouteList.setMaximumSize(null);
        gtfsRouteList.setMinimumSize(null);
        gtfsRouteList.setNextFocusableComponent(osmIDList);
        gtfsRouteList.setSelectedIndex(0);
        gtfsRouteList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsRouteListMouseClicked(evt);
            }
        });
        jScrollPane13.setViewportView(gtfsRouteList);

        newTagValueList.setBorder(new javax.swing.border.MatteBorder(null));
        newTagValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane14.setViewportView(newTagValueList);

        gtfsTagValueList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsTagValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane15.setViewportView(gtfsTagValueList);

        osmMemberList.setBorder(new javax.swing.border.MatteBorder(null));
        osmMemberList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmMemberList.setNextFocusableComponent(modifyRightButton);
        osmMemberList.setSelectedIndex(0);
        jScrollPane16.setViewportView(osmMemberList);

        newRouteIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        newRouteIDLabel.setText("New Route");

        removeUploadButton1.setText("Remove");

        osmTagValueList.setBorder(new javax.swing.border.MatteBorder(null));
        osmTagValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane17.setViewportView(osmTagValueList);

        modifyRightButton1.setText("Modify");
        modifyRightButton1.setName("okButton"); // NOI18N
        modifyRightButton1.setNextFocusableComponent(modifyLeftButton);
        modifyRightButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                modifyRightButton1MouseClicked(evt);
            }
        });
        modifyRightButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyRightButton1ActionPerformed(evt);
            }
        });

        osmTagKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        osmTagKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmTagKeyList.setNextFocusableComponent(osmDetailsKeyList);
        osmTagKeyList.setSelectedIndex(0);
        osmTagKeyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmTagKeyListMouseClicked(evt);
            }
        });
        osmTagKeyList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmTagKeyListKeyReleased(evt);
            }
        });
        jScrollPane18.setViewportView(osmTagKeyList);

        gtfsRouteIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        gtfsRouteIDLabel.setText("GTFS Route");

        osmRouteIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        osmRouteIDLabel.setText("OSM Route");

        removeOsmButton1.setText("Remove");

        gtfsMemberList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsMemberList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane20.setViewportView(gtfsMemberList);

        newMemberList.setBorder(new javax.swing.border.MatteBorder(null));
        newMemberList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane21.setViewportView(newMemberList);

        osmRouteList.setBorder(new javax.swing.border.MatteBorder(null));
        osmRouteList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmRouteList.setNextFocusableComponent(osmDetailsKeyList);
        osmRouteList.setSelectedIndex(0);
        osmRouteList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmRouteListMouseClicked(evt);
            }
        });
        osmRouteList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmRouteListKeyReleased(evt);
            }
        });
        jScrollPane22.setViewportView(osmRouteList);

        javax.swing.GroupLayout routePanelLayout = new javax.swing.GroupLayout(routePanel);
        routePanel.setLayout(routePanelLayout);
        routePanelLayout.setHorizontalGroup(
            routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(195, 195, 195)
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(modifyLeftButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(removeGtfsButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(removeUploadButton1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(modifyRightButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(removeOsmButton1))
                    .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
            .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(routePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(routePanelLayout.createSequentialGroup()
                            .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(routePanelLayout.createSequentialGroup()
                            .addGap(23, 23, 23)
                            .addComponent(totalRoutesLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1098, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(routePanelLayout.createSequentialGroup()
                            .addGap(125, 125, 125)
                            .addComponent(gtfsRouteIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                            .addGap(267, 267, 267)
                            .addComponent(newRouteIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                            .addGap(289, 289, 289)
                            .addComponent(osmRouteIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                            .addGap(116, 116, 116)))
                    .addContainerGap()))
        );

        routePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane12, jScrollPane14, jScrollPane21});

        routePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane16, jScrollPane17, jScrollPane18, jScrollPane22});

        routePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane11, jScrollPane13, jScrollPane15, jScrollPane20});

        routePanelLayout.setVerticalGroup(
            routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(118, 118, 118)
                        .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane21, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                            .addComponent(jScrollPane12)
                            .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                            .addComponent(jScrollPane20)
                            .addComponent(jScrollPane15)
                            .addComponent(jScrollPane16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                            .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                            .addComponent(jScrollPane22, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)))
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(241, 241, 241)
                        .addComponent(modifyLeftButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(250, 250, 250)
                        .addComponent(modifyRightButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(removeOsmButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(removeGtfsButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(removeUploadButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(171, Short.MAX_VALUE))
            .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(routePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(osmRouteIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(newRouteIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gtfsRouteIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(56, 56, 56)
                    .addComponent(totalRoutesLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(routePanelLayout.createSequentialGroup()
                            .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                            .addGap(259, 259, 259))
                        .addGroup(routePanelLayout.createSequentialGroup()
                            .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap()))))
        );

        routePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jScrollPane12, jScrollPane14, jScrollPane21});

        routePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jScrollPane11, jScrollPane13, jScrollPane15, jScrollPane20});

        jTabbedPane1.addTab("Route", routePanel);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1274, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(619, Short.MAX_VALUE)
                .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(590, 590, 590))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 698, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        uploadButton.getAccessibleContext().setAccessibleName("uploadButton");

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
        gtfsStopActivated();
    }//GEN-LAST:event_gtfsIDListKeyReleased

    private void osmIDListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmIDListKeyReleased
        // TODO add your handling code here:
        osmStopActivated();
    }//GEN-LAST:event_osmIDListKeyReleased

    private void gtfsIDListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsIDListMouseClicked
        // TODO add your handling code here:
        gtfsStopActivated();
    }//GEN-LAST:event_gtfsIDListMouseClicked

    private void osmIDListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmIDListMouseClicked
        // TODO add your handling code here:
        osmStopActivated();
    }//GEN-LAST:event_osmIDListMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        gtfsStopActivated();
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

    private void reviseReport(){
        HashSet<Stop> osmStopDeleted = new HashSet<Stop>();
        ArrayList<Stop> osmStopNotDeleted = new ArrayList<Stop>();

        ArrayList<Stop> reportKeys = new ArrayList<Stop>();
        reportKeys.addAll(report.keySet());
        for(int i=0; i<reportKeys.size(); i++){
            Stop ts = (Stop)reportKeys.get(i);
            if(!gtfsIDAll.contains(ts.getStopID())){
                if(!report.get(ts).equals("none")){
                    osmStopDeleted.addAll((ArrayList<Stop>)report.get(ts));
                }

                //remove gtfs stop in associated set
                String category = ts.getReportCategory();
                if (category.equals("UPLOAD_CONFLICT") || category.equals("UPLOAD_NO_CONFLICT")) {
                    upload.remove(ts);
                } else if (category.equals("MODIFY")) {
                    modify.remove(ts);
                }

                //remove stop in the report
                report.remove(ts);
            } else {
                if(!report.get(ts).equals("none")){
                    osmStopNotDeleted.addAll((ArrayList<Stop>)report.get(ts));
                }
            }
        }

        //check if any osm Stop in revised report is the same with osmStopDeleted stop
        Iterator it = osmStopNotDeleted.iterator();
        while(it.hasNext()){
            Stop s = (Stop)it.next();
            if(osmStopDeleted.contains(s)){
                osmStopDeleted.remove(s);
            }
        }

        //remove all osmStopDeleted stops from modify set
        modify.removeAll(osmStopDeleted);

        //write data to file
        new WriteFile(FILE_NAME_OUT_REPORT_REVISE, report);
        new WriteFile(FILE_NAME_OUT_UPLOAD_REVISE, upload);
        new WriteFile(FILE_NAME_OUT_MODIFY_REVISE, modify);
        new WriteFile(FILE_NAME_OUT_DELETE_REVISE, delete);
    }

    private void uploadButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadButtonMouseClicked
        // TODO add your handling code here:
        reviseReport();
        osmRequest.checkVersion();
        osmRequest.createChangeSet();
        osmRequest.createChunks(upload, modify, delete, finalRoutes);
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

    private void removeGtfsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeGtfsButtonMouseClicked
        // TODO add your handling code here:
        String s = (String)gtfsIDList.getSelectedValue();
        int index = gtfsIDList.getSelectedIndex();
        gtfsIDAll.removeElement(s);
        gtfsIDUploadConflict.removeElement(s);
        gtfsIDModify.removeElement(s);
        gtfsIDNoUpload.removeElement(s);
        gtfsIDUploadNoConflict.removeElement(s);
        gtfsIDList.setSelectedIndex(index);
        gtfsStopActivated();
    }//GEN-LAST:event_removeGtfsButtonMouseClicked

    private void modifyLeftButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyLeftButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modifyLeftButton1ActionPerformed

    private void removeGtfsButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_removeGtfsButton1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_removeGtfsButton1MouseClicked

    private void modifyRightButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modifyRightButton1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_modifyRightButton1MouseClicked

    private void modifyRightButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyRightButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modifyRightButton1ActionPerformed

    private void osmTagKeyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmTagKeyListMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_osmTagKeyListMouseClicked

    private void osmTagKeyListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmTagKeyListKeyReleased
        // TODO add your handling code here:
}//GEN-LAST:event_osmTagKeyListKeyReleased

    private void osmRouteListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmRouteListMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_osmRouteListMouseClicked

    private void osmRouteListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmRouteListKeyReleased
        // TODO add your handling code here:
}//GEN-LAST:event_osmRouteListKeyReleased

    private void gtfsRouteListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsRouteListMouseClicked
        // TODO add your handling code here:
        Route r = (Route)finalRoutes.get((String)gtfsRouteList.getSelectedValue());
        System.out.println("here "+r.getRouteRef());
        updateGtfsRouteTagList(r);
    }//GEN-LAST:event_gtfsRouteListMouseClicked

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
    private javax.swing.JList gtfsMemberList;
    private javax.swing.JLabel gtfsRouteIDLabel;
    private javax.swing.JList gtfsRouteList;
    private javax.swing.JLabel gtfsStopIDLabel;
    private javax.swing.JList gtfsTagKeyList;
    private javax.swing.JList gtfsTagValueList;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton modifyLeftButton;
    private javax.swing.JButton modifyLeftButton1;
    private javax.swing.JButton modifyRightButton;
    private javax.swing.JButton modifyRightButton1;
    private javax.swing.JRadioButton modifyStopsRadioButton;
    private javax.swing.JList newDetailsKeyList;
    private javax.swing.JList newDetailsValueList;
    private javax.swing.JList newMemberList;
    private javax.swing.JLabel newRouteIDLabel;
    private javax.swing.JLabel newStopIDLabel;
    private javax.swing.JList newTagKeyList;
    private javax.swing.JList newTagValueList;
    private javax.swing.JRadioButton noUploadStopsRadioButton;
    private javax.swing.JList osmDetailsKeyList;
    private javax.swing.JList osmDetailsValueList;
    private javax.swing.JList osmIDList;
    private javax.swing.JList osmMemberList;
    private javax.swing.JLabel osmRouteIDLabel;
    private javax.swing.JList osmRouteList;
    private javax.swing.JLabel osmStopIDLabel;
    private javax.swing.JList osmTagKeyList;
    private javax.swing.JList osmTagValueList;
    private javax.swing.JButton removeGtfsButton;
    private javax.swing.JButton removeGtfsButton1;
    private javax.swing.JButton removeOsmButton;
    private javax.swing.JButton removeOsmButton1;
    private javax.swing.JButton removeUploadButton;
    private javax.swing.JButton removeUploadButton1;
    private javax.swing.JTextArea reportMessage;
    private javax.swing.JPanel routePanel;
    private javax.swing.JPanel stopPanel;
    private javax.swing.JLabel totalRoutesLabel;
    private javax.swing.JLabel totalStopsLabel;
    private javax.swing.JButton uploadButton;
    private javax.swing.JRadioButton uploadConflictStopsRadioButton;
    private javax.swing.JRadioButton uploadNoConflictStopsRadioButton;
    // End of variables declaration//GEN-END:variables

}
