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

package edu.usf.cutr.go_sync.gui;

import edu.usf.cutr.go_sync.io.WriteFile;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ButtonGroup;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import edu.usf.cutr.go_sync.object.RelationMember;
import edu.usf.cutr.go_sync.object.Route;
import edu.usf.cutr.go_sync.object.Stop;
import edu.usf.cutr.go_sync.osm.HttpRequest;
import edu.usf.cutr.go_sync.task.UploadData;

/**
 *
 * @author Khoa Tran
 */
public class ReportForm extends javax.swing.JFrame implements PropertyChangeListener{

    private static final String FILE_NAME_OUT_UPLOAD_REVISE = "UPLOAD_REVISE.txt";
    private static final String FILE_NAME_OUT_MODIFY_REVISE = "MODIFY_REVISE.txt";
    private static final String FILE_NAME_OUT_DELETE_REVISE = "DELETE_REVISE.txt";
    private static final String FILE_NAME_OUT_NOUPLOAD_REVISE = "NOUPLOAD_REVISE.txt";
    private static final String FILE_NAME_OUT_REPORT_REVISE = "REPORT_REVISE.txt";

    private Hashtable report;

    private HashSet<Stop> upload, modify, delete;

    private DefaultListModel routeMemberIDs = new DefaultListModel();
    private DefaultListModel routeMemberMatchIDs = new DefaultListModel();
    private DefaultListModel routeMemberAll = new DefaultListModel();
    private DefaultListModel routeMemberBoth = new DefaultListModel();
    private DefaultListModel routeMemberGtfs = new DefaultListModel();
    private DefaultListModel routeMemberOsm = new DefaultListModel();
    private DefaultListModel routeMemberMatchAll = new DefaultListModel();
    private DefaultListModel routeMemberMatchBoth = new DefaultListModel();
    private DefaultListModel routeMemberMatchGtfs = new DefaultListModel();
    private DefaultListModel routeMemberMatchOsm = new DefaultListModel();
    private ButtonGroup memberGroup = new ButtonGroup();

    private DefaultListModel gtfsRouteIDs = new DefaultListModel();
    private DefaultListModel gtfsRouteAll = new DefaultListModel();
    private DefaultListModel gtfsRouteNoUpload = new DefaultListModel();
    private DefaultListModel gtfsRouteNew = new DefaultListModel();
    private DefaultListModel gtfsRouteModify = new DefaultListModel();
    private DefaultListModel gtfsTagKey = new DefaultListModel();
    private DefaultListModel gtfsTagValue = new DefaultListModel();
    private DefaultListModel gtfsMemberValue = new DefaultListModel();
    private DefaultListModel gtfsMatchId = new DefaultListModel();
    private DefaultListModel osmRoutes = new DefaultListModel();
    private DefaultListModel osmTagKey = new DefaultListModel();
    private DefaultListModel osmTagValue = new DefaultListModel();
    private DefaultListModel osmMemberValue = new DefaultListModel();
    private DefaultListModel osmMatchId = new DefaultListModel();
    private DefaultListModel newTagKey = new DefaultListModel();
    private DefaultListModel newTagValue = new DefaultListModel();
    private DefaultListModel newMemberValue = new DefaultListModel();
    private DefaultListModel newMatchId = new DefaultListModel();
    private ButtonGroup routeGroup = new ButtonGroup();
    
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
    private ButtonGroup stopGroup = new ButtonGroup();

    // map between gtfs id and gtfs stop
    private Hashtable agencyStops = new Hashtable();
    private Hashtable finalStops = new Hashtable();
    private Hashtable osmStops = new Hashtable();

    private JTextArea taskOutput;

    private JProgressBar progressBar;

    private HttpRequest osmRequest;

    private Hashtable finalRoutes, agencyRoutes, existingRoutes;

    // List of stops from agency data
    private ArrayList<Stop> agencyData;

    private UploadData taskUpload = null;

    /** Creates new form ReportForm */
    public ReportForm(List<Stop> aData, Hashtable r, HashSet<Stop>u, HashSet<Stop>m, HashSet<Stop>d, Hashtable routes, Hashtable nRoutes, Hashtable eRoutes, JTextArea to) {
        taskOutput = to;
        osmRequest = new HttpRequest(to);
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

        agencyRoutes = new Hashtable();
        agencyRoutes.putAll(nRoutes);

        existingRoutes = new Hashtable();
        existingRoutes.putAll(eRoutes);

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
        routeKeys.addAll(agencyRoutes.keySet());
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
            gtfsRouteAll.add(ari, rk);
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
        allStopsRadioButton.setSelected(true);
        allRoutesRadioButton.setSelected(true);
        if(ai>0) gtfsIDList.setSelectedIndex(0);
        if(ari>0) gtfsRouteList.setSelectedIndex(0);
        totalGtfsRoutesLabel.setText(Integer.toString(ari));
        gtfsRouteActivated();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            if(taskUpload!=null){
                int progress = (Integer) evt.getNewValue();
                progressBar.setIndeterminate(false);
                taskOutput.append(taskUpload.getMessage()+"\n");
                progressBar.setValue(progress);
                if(taskUpload.getMessage().contains("several minutes")){
                    progressBar.setIndeterminate(true);
                }
            }
        }
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

    public void clearAllStopLists(){
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
            Stop fStop = (Stop)finalStops.get(gtfsID);
            updateGtfsDetailsList((Stop)agencyStops.get(gtfsID));
            updateNewDetailsList(fStop);
            updateOsmIDList(fStop);
            ArrayList<Stop> arrSt = new ArrayList<Stop>();
            if(!report.get(fStop).equals("none")) arrSt = (ArrayList<Stop>)report.get(fStop);

            if(arrSt.size()>1){
                matchStopButton.setEnabled(true);
            }
            else matchStopButton.setEnabled(false);
        } else {
            clearAllStopLists();
            if (gtfsIDs.getSize()==0) reportMessage.setText("There is no GTFS stops");
        }
    }

    public void osmStopActivated(){
        String osmID = (String)osmIDList.getSelectedValue();
        updateOsmDetailsList((Stop)osmStops.get(osmID));
        updateReportMessage((Stop)osmStops.get(osmID));
    }

    public void updateGtfsIdList() {
        DefaultListModel tList = (DefaultListModel)gtfsIDList.getModel();
        if (tList.getSize()>0) gtfsIDList.setSelectedIndex(0);
        totalStopsLabel.setText(Integer.toString(tList.getSize()));
        gtfsStopActivated();
    }

    public void updateGtfsRouteIdList() {
        DefaultListModel tList = (DefaultListModel)gtfsRouteList.getModel();
        if (tList.getSize()>0) gtfsRouteList.setSelectedIndex(0);
        totalGtfsRoutesLabel.setText(Integer.toString(tList.getSize()));
        gtfsRouteActivated();
    }

    public void updateGtfsRouteTagList(Route r){
        HashSet<String> keys = r.keySet();
        Iterator it = keys.iterator();
        gtfsTagKey.clear();
        gtfsTagValue.clear();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = r.getTag(k);
            if (!v.equals("none") && !v.equals("")) {
                gtfsTagKey.addElement(k);
                gtfsTagValue.addElement(v);
            }
        }
    }

    public void updateGtfsRouteMemberList(Route r){
        HashSet<RelationMember> keys = r.getOsmMembers();
        Iterator it = keys.iterator();
        gtfsMemberValue.clear();
        gtfsMatchId.clear();
        totalGtfsRelationLabel.setText(Integer.toString(keys.size()));
        while (it.hasNext()){
            RelationMember rm = (RelationMember)it.next();
            gtfsMemberValue.addElement(rm.getRef());
            String v = rm.getGtfsId();
            if (v!=null && !v.equals("none") && !v.equals("")) {
                gtfsMatchId.addElement(v);
            } else {
                gtfsMatchId.addElement(rm.getType());
            }
        }
    }

    public void updateNewRouteTagList(Route r){
        HashSet<String> keys = r.keySet();
        Iterator it = keys.iterator();
        newTagKey.clear();
        newTagValue.clear();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = r.getTag(k);
            if (!v.equals("none") && !v.equals("")) {
                newTagKey.addElement(k);
                newTagValue.addElement(v);
            }
        }
    }

    public void updateNewRouteMemberList(Route r){
        // members
        ArrayList<RelationMember> memberKeys = new ArrayList<RelationMember>();
        //convert to arrayList for ordering
        memberKeys.addAll(r.getOsmMembers());
        //ordering by gtfsId hashcode
        for (int i=0; i<memberKeys.size()-1; i++) {
            int k=i;
            for (int j=i+1; j<memberKeys.size(); j++) {
                //get gtfs id
                String vk = memberKeys.get(k).getGtfsId();
                if (vk!=null && !vk.equals("none") && !vk.equals("")) {
                } else {
                    vk = memberKeys.get(k).getType();
                }
                String vj = memberKeys.get(j).getGtfsId();
                if (vj!=null && !vj.equals("none") && !vj.equals("")) {
                } else {
                    vj = memberKeys.get(j).getType();
                }

                //compare
                if (vk.hashCode() > vj.hashCode()) {
                    k = j;
                }
            }
            RelationMember temp = new RelationMember(memberKeys.get(i));
            memberKeys.set(i, memberKeys.get(k));
            memberKeys.set(k, temp);
        }

        //clear lists
        routeMemberMatchBoth.clear();
        routeMemberBoth.clear();
        routeMemberMatchAll.clear();
        routeMemberAll.clear();
        routeMemberMatchGtfs.clear();
        routeMemberGtfs.clear();
        routeMemberMatchOsm.clear();
        routeMemberOsm.clear();

        //add data to correct list (categorizing)
        int ami=0, bothmi=0, gtfsmi=0, osmmi=0;
        for (int i=0; i<memberKeys.size(); i++) {
            RelationMember rk = memberKeys.get(i);
            String vm = rk.getGtfsId();
            if (vm!=null && !vm.equals("none") && !vm.equals("")) {
            } else {
                vm = rk.getType();
            }
            routeMemberMatchAll.add(ami, vm);
            routeMemberAll.add(ami, rk.getRef());
            ami++;

            String status = rk.getStatus();
            if (status.equals("both GTFS dataset and OSM server")) {
                routeMemberMatchBoth.add(bothmi, vm);
                routeMemberBoth.add(bothmi, rk.getRef());
                bothmi++;
            } else if (status.equals("GTFS dataset")) {
                routeMemberMatchGtfs.add(gtfsmi, vm);
                routeMemberGtfs.add(gtfsmi, rk.getRef());
                gtfsmi++;
            } else if (status.equals("OSM server")) {
                routeMemberMatchOsm.add(osmmi, vm);
                routeMemberOsm.add(osmmi, rk.getRef());
                osmmi++;
            }
        }

        totalNewRelationLabel.setText(Integer.toString(memberKeys.size()));
        allMemberRadioButton.setSelected(true);
        newMemberList.setModel(routeMemberAll);
        newMatchIdList.setModel(routeMemberMatchAll);
        if (memberKeys.size()>0){
            newMemberList.setSelectedIndex(0);
            newMatchIdList.setSelectedIndex(0);
            updateRouteMessage();
        }
    }

    public void updateOsmRouteTagList(Route r){
        HashSet<String> keys = r.keySet();
        Iterator it = keys.iterator();
        osmTagKey.clear();
        osmTagValue.clear();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = r.getTag(k);
            if (v!=null && !v.equals("none") && !v.equals("")) {
                osmTagKey.addElement(k);
                osmTagValue.addElement(v);
            }
        }
    }

    public void updateOsmRouteMemberList(Route r){
        HashSet<RelationMember> keys = r.getOsmMembers();
        Iterator it = keys.iterator();
        osmMemberValue.clear();
        osmMatchId.clear();
        totalOsmRelationLabel.setText(Integer.toString(keys.size()));
        while (it.hasNext()){
            RelationMember rm = (RelationMember)it.next();
            osmMemberValue.addElement(rm.getRef());
            String v = rm.getGtfsId();
            if (v!=null && !v.equals("none") && !v.equals("")) {
                osmMatchId.addElement(v);
            } else {
                osmMatchId.addElement(rm.getType());
            }
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
        gtfsStopIDLabel = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        gtfsIDList = new javax.swing.JList(gtfsIDs);
        osmStopIDLabel = new javax.swing.JLabel();
        newStopIDLabel = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        osmDetailsKeyList = new javax.swing.JList(osmDetailsKey);
        jScrollPane9 = new javax.swing.JScrollPane();
        newDetailsValueList = new javax.swing.JList(newDetailsValue);
        jScrollPane10 = new javax.swing.JScrollPane();
        gtfsDetailsValueList = new javax.swing.JList(gtfsDetailsValue);
        allStopsRadioButton = new javax.swing.JRadioButton();
        allStopsRadioButton.setEnabled(true);stopGroup.add(allStopsRadioButton);
        uploadConflictStopsRadioButton = new javax.swing.JRadioButton();
        stopGroup.add(uploadConflictStopsRadioButton);
        uploadNoConflictStopsRadioButton = new javax.swing.JRadioButton();
        stopGroup.add(uploadNoConflictStopsRadioButton);
        totalStopsLabel = new javax.swing.JLabel();
        totalStopsLabel.setText(agencyData.size()+" stops");
        modifyStopsRadioButton = new javax.swing.JRadioButton();
        stopGroup.add(modifyStopsRadioButton);
        noUploadStopsRadioButton = new javax.swing.JRadioButton();
        stopGroup.add(noUploadStopsRadioButton);
        removeGtfsButton = new javax.swing.JButton();
        doneModifyStopButton = new javax.swing.JButton();
        matchStopButton = new javax.swing.JButton();
        dumbOsmChangeTextButton = new javax.swing.JButton();
        routePanel = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        gtfsTagKeyList = new javax.swing.JList(gtfsTagKey);
        totalGtfsRoutesLabel = new javax.swing.JLabel();
        totalStopsLabel.setText(agencyData.size()+" stops");
        jScrollPane12 = new javax.swing.JScrollPane();
        newTagKeyList = new javax.swing.JList(newTagKey);
        jScrollPane13 = new javax.swing.JScrollPane();
        gtfsRouteList = new javax.swing.JList(gtfsRouteAll);
        jScrollPane14 = new javax.swing.JScrollPane();
        newTagValueList = new javax.swing.JList(newTagValue);
        jScrollPane15 = new javax.swing.JScrollPane();
        gtfsTagValueList = new javax.swing.JList(gtfsTagValue);
        jScrollPane16 = new javax.swing.JScrollPane();
        osmMemberList = new javax.swing.JList(osmMemberValue);
        newRouteIDLabel = new javax.swing.JLabel();
        jScrollPane17 = new javax.swing.JScrollPane();
        osmTagValueList = new javax.swing.JList(osmTagValue);
        jScrollPane18 = new javax.swing.JScrollPane();
        osmTagKeyList = new javax.swing.JList(osmTagKey);
        gtfsRouteIDLabel = new javax.swing.JLabel();
        osmRouteIDLabel = new javax.swing.JLabel();
        jScrollPane20 = new javax.swing.JScrollPane();
        gtfsMemberList = new javax.swing.JList(gtfsMemberValue);
        jScrollPane21 = new javax.swing.JScrollPane();
        newMemberList = new javax.swing.JList(routeMemberAll);
        jScrollPane22 = new javax.swing.JScrollPane();
        osmMatchIdList = new javax.swing.JList(osmMatchId);
        jScrollPane23 = new javax.swing.JScrollPane();
        gtfsMatchIdList = new javax.swing.JList(gtfsMatchId);
        jScrollPane24 = new javax.swing.JScrollPane();
        newMatchIdList = new javax.swing.JList(routeMemberMatchAll);
        totalGtfsRelationLabel = new javax.swing.JLabel();
        totalStopsLabel.setText(agencyData.size()+" stops");
        totalNewRelationLabel = new javax.swing.JLabel();
        totalStopsLabel.setText(agencyData.size()+" stops");
        jScrollPane19 = new javax.swing.JScrollPane();
        reportRouteMessage = new javax.swing.JTextArea();
        totalOsmRelationLabel = new javax.swing.JLabel();
        totalStopsLabel.setText(agencyData.size()+" stops");
        allRoutesRadioButton = new javax.swing.JRadioButton();
        allRoutesRadioButton.setEnabled(true);
        routeGroup.add(allRoutesRadioButton);
        newRoutesRadioButton = new javax.swing.JRadioButton();
        routeGroup.add(newRoutesRadioButton);
        modifyRoutesRadioButton = new javax.swing.JRadioButton();
        routeGroup.add(modifyRoutesRadioButton);
        noUploadRoutesRadioButton = new javax.swing.JRadioButton();
        routeGroup.add(noUploadRoutesRadioButton);
        allMemberRadioButton = new javax.swing.JRadioButton();
        allMemberRadioButton.setEnabled(true);
        memberGroup.add(allMemberRadioButton);
        gtfsMemberRadioButton = new javax.swing.JRadioButton();
        memberGroup.add(gtfsMemberRadioButton);
        osmMemberRadioButton = new javax.swing.JRadioButton();
        memberGroup.add(osmMemberRadioButton);
        bothMemberRadioButton = new javax.swing.JRadioButton();
        memberGroup.add(bothMemberRadioButton);
        uploadButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exportGtfsMenuItem = new javax.swing.JMenuItem();
        exportOsmMenuItem = new javax.swing.JMenuItem();

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
        reportMessage.setRows(4);
        jScrollPane3.setViewportView(reportMessage);
        reportMessage.getAccessibleContext().setAccessibleName("reportMessage");

        osmDetailsValueList.setBorder(new javax.swing.border.MatteBorder(null));
        osmDetailsValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmDetailsValueList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmDetailsValueListMouseClicked(evt);
            }
        });
        osmDetailsValueList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmDetailsValueListKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(osmDetailsValueList);
        osmDetailsValueList.getAccessibleContext().setAccessibleName("osmDetailsValueList");

        gtfsDetailsKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsDetailsKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsDetailsKeyList.setNextFocusableComponent(gtfsIDList);
        gtfsDetailsKeyList.setSelectedIndex(0);
        gtfsDetailsKeyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsDetailsKeyListMouseClicked(evt);
            }
        });
        gtfsDetailsKeyList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsDetailsKeyListKeyReleased(evt);
            }
        });
        jScrollPane5.setViewportView(gtfsDetailsKeyList);
        gtfsDetailsKeyList.getAccessibleContext().setAccessibleName("gtfsDetailsKeyList");

        newDetailsKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        newDetailsKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newDetailsKeyList.setSelectedIndex(0);
        newDetailsKeyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newDetailsKeyListMouseClicked(evt);
            }
        });
        newDetailsKeyList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                newDetailsKeyListKeyReleased(evt);
            }
        });
        jScrollPane6.setViewportView(newDetailsKeyList);
        newDetailsKeyList.getAccessibleContext().setAccessibleName("newDetailsKeyList");

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

        osmStopIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        osmStopIDLabel.setText("OSM Stop");

        newStopIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        newStopIDLabel.setText("New Stop");

        osmDetailsKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        osmDetailsKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmDetailsKeyList.setSelectedIndex(0);
        osmDetailsKeyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmDetailsKeyListMouseClicked(evt);
            }
        });
        osmDetailsKeyList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmDetailsKeyListKeyReleased(evt);
            }
        });
        jScrollPane8.setViewportView(osmDetailsKeyList);
        osmDetailsKeyList.getAccessibleContext().setAccessibleName("osmDetailsKeyList");

        newDetailsValueList.setBorder(new javax.swing.border.MatteBorder(null));
        newDetailsValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newDetailsValueList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newDetailsValueListMouseClicked(evt);
            }
        });
        newDetailsValueList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                newDetailsValueListKeyReleased(evt);
            }
        });
        jScrollPane9.setViewportView(newDetailsValueList);
        newDetailsValueList.getAccessibleContext().setAccessibleName("newDetailsValueList");

        gtfsDetailsValueList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsDetailsValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsDetailsValueList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsDetailsValueListMouseClicked(evt);
            }
        });
        gtfsDetailsValueList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsDetailsValueListKeyReleased(evt);
            }
        });
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
        removeGtfsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeGtfsButtonActionPerformed(evt);
            }
        });

        doneModifyStopButton.setText("Done Modify");
        doneModifyStopButton.setName("uploadButton"); // NOI18N
        doneModifyStopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                doneModifyStopButtonMouseClicked(evt);
            }
        });
        doneModifyStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneModifyStopButtonActionPerformed(evt);
            }
        });

        matchStopButton.setText("Match");
        matchStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchStopButtonActionPerformed(evt);
            }
        });

        dumbOsmChangeTextButton.setText("Dumb Upload");
        dumbOsmChangeTextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dumbOsmChangeTextButtonActionPerformed(evt);
            }
        });

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
                            .addComponent(allStopsRadioButton)
                            .addComponent(noUploadStopsRadioButton))
                        .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(stopPanelLayout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 666, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(stopPanelLayout.createSequentialGroup()
                                .addGap(382, 382, 382)
                                .addComponent(doneModifyStopButton)
                                .addGap(66, 66, 66)
                                .addComponent(dumbOsmChangeTextButton))))
                    .addGroup(stopPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(stopPanelLayout.createSequentialGroup()
                                .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(removeGtfsButton)
                                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(stopPanelLayout.createSequentialGroup()
                                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(2, 2, 2)
                                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(83, 83, 83)
                                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                                        .addGap(87, 87, 87)
                                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stopPanelLayout.createSequentialGroup()
                                        .addComponent(matchStopButton)
                                        .addGap(18, 18, 18))))
                            .addGroup(stopPanelLayout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(totalStopsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1201, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(stopPanelLayout.createSequentialGroup()
                    .addGap(135, 135, 135)
                    .addComponent(gtfsStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addGap(267, 267, 267)
                    .addComponent(newStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addGap(289, 289, 289)
                    .addComponent(osmStopIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                    .addGap(116, 116, 116)))
        );
        stopPanelLayout.setVerticalGroup(
            stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stopPanelLayout.createSequentialGroup()
                .addGap(65, 65, 65)
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
                .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(matchStopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeGtfsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stopPanelLayout.createSequentialGroup()
                        .addComponent(allStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(uploadConflictStopsRadioButton)
                        .addGap(3, 3, 3)
                        .addComponent(uploadNoConflictStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifyStopsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(noUploadStopsRadioButton))
                    .addGroup(stopPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(doneModifyStopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dumbOsmChangeTextButton))))
                .addGap(31, 31, 31))
            .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(stopPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(stopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(osmStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(newStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(gtfsStopIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(591, Short.MAX_VALUE)))
        );

        gtfsStopIDLabel.getAccessibleContext().setAccessibleName("gtfsStopIDLabel");
        osmStopIDLabel.getAccessibleContext().setAccessibleName("osmStopIDLabel");
        newStopIDLabel.getAccessibleContext().setAccessibleName("newStopIDLabel");
        totalStopsLabel.getAccessibleContext().setAccessibleName("");

        jTabbedPane1.addTab("Stop", stopPanel);

        gtfsTagKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsTagKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsTagKeyList.setNextFocusableComponent(gtfsIDList);
        gtfsTagKeyList.setSelectedIndex(0);
        gtfsTagKeyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsTagKeyListMouseClicked(evt);
            }
        });
        gtfsTagKeyList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsTagKeyListKeyReleased(evt);
            }
        });
        jScrollPane11.setViewportView(gtfsTagKeyList);

        totalGtfsRoutesLabel.setText("0");

        newTagKeyList.setBorder(new javax.swing.border.MatteBorder(null));
        newTagKeyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newTagKeyList.setSelectedIndex(0);
        newTagKeyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newTagKeyListMouseClicked(evt);
            }
        });
        newTagKeyList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                newTagKeyListKeyReleased(evt);
            }
        });
        jScrollPane12.setViewportView(newTagKeyList);

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
        gtfsRouteList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsRouteListKeyReleased(evt);
            }
        });
        jScrollPane13.setViewportView(gtfsRouteList);

        newTagValueList.setBorder(new javax.swing.border.MatteBorder(null));
        newTagValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newTagValueList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newTagValueListMouseClicked(evt);
            }
        });
        newTagValueList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                newTagValueListKeyReleased(evt);
            }
        });
        jScrollPane14.setViewportView(newTagValueList);

        gtfsTagValueList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsTagValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsTagValueList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsTagValueListMouseClicked(evt);
            }
        });
        gtfsTagValueList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsTagValueListKeyReleased(evt);
            }
        });
        jScrollPane15.setViewportView(gtfsTagValueList);

        osmMemberList.setBorder(new javax.swing.border.MatteBorder(null));
        osmMemberList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmMemberList.setSelectedIndex(0);
        osmMemberList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmMemberListMouseClicked(evt);
            }
        });
        osmMemberList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmMemberListKeyReleased(evt);
            }
        });
        jScrollPane16.setViewportView(osmMemberList);

        newRouteIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        newRouteIDLabel.setText("New Route");

        osmTagValueList.setBorder(new javax.swing.border.MatteBorder(null));
        osmTagValueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmTagValueList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmTagValueListMouseClicked(evt);
            }
        });
        osmTagValueList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmTagValueListKeyReleased(evt);
            }
        });
        jScrollPane17.setViewportView(osmTagValueList);

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

        gtfsRouteIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        gtfsRouteIDLabel.setText("GTFS Route");

        osmRouteIDLabel.setFont(new java.awt.Font("Times New Roman", 1, 24));
        osmRouteIDLabel.setText("OSM Route");

        gtfsMemberList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsMemberList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsMemberList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsMemberListMouseClicked(evt);
            }
        });
        gtfsMemberList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsMemberListKeyReleased(evt);
            }
        });
        jScrollPane20.setViewportView(gtfsMemberList);

        newMemberList.setBorder(new javax.swing.border.MatteBorder(null));
        newMemberList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newMemberList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newMemberListMouseClicked(evt);
            }
        });
        newMemberList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                newMemberListKeyReleased(evt);
            }
        });
        jScrollPane21.setViewportView(newMemberList);

        osmMatchIdList.setBorder(new javax.swing.border.MatteBorder(null));
        osmMatchIdList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        osmMatchIdList.setNextFocusableComponent(osmDetailsKeyList);
        osmMatchIdList.setSelectedIndex(0);
        osmMatchIdList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                osmMatchIdListMouseClicked(evt);
            }
        });
        osmMatchIdList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                osmMatchIdListKeyReleased(evt);
            }
        });
        jScrollPane22.setViewportView(osmMatchIdList);

        gtfsMatchIdList.setBorder(new javax.swing.border.MatteBorder(null));
        gtfsMatchIdList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gtfsMatchIdList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gtfsMatchIdListMouseClicked(evt);
            }
        });
        gtfsMatchIdList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                gtfsMatchIdListKeyReleased(evt);
            }
        });
        jScrollPane23.setViewportView(gtfsMatchIdList);

        newMatchIdList.setBorder(new javax.swing.border.MatteBorder(null));
        newMatchIdList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        newMatchIdList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newMatchIdListMouseClicked(evt);
            }
        });
        newMatchIdList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                newMatchIdListKeyReleased(evt);
            }
        });
        jScrollPane24.setViewportView(newMatchIdList);

        totalGtfsRelationLabel.setText("0");

        totalNewRelationLabel.setText("0");

        reportRouteMessage.setColumns(20);
        reportRouteMessage.setEditable(false);
        reportRouteMessage.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        reportRouteMessage.setRows(3);
        jScrollPane19.setViewportView(reportRouteMessage);

        totalOsmRelationLabel.setText("0");

        allRoutesRadioButton.setText("All Routes");
        allRoutesRadioButton.setName("routeCategory"); // NOI18N
        allRoutesRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                allRoutesRadioButtonMouseClicked(evt);
            }
        });
        allRoutesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allRoutesRadioButtonActionPerformed(evt);
            }
        });

        newRoutesRadioButton.setText("New Routes");
        newRoutesRadioButton.setName("routeCategory"); // NOI18N
        newRoutesRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newRoutesRadioButtonMouseClicked(evt);
            }
        });
        newRoutesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newRoutesRadioButtonActionPerformed(evt);
            }
        });

        modifyRoutesRadioButton.setText("Modify Routes");
        modifyRoutesRadioButton.setName("routeCategory"); // NOI18N
        modifyRoutesRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                modifyRoutesRadioButtonMouseClicked(evt);
            }
        });
        modifyRoutesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyRoutesRadioButtonActionPerformed(evt);
            }
        });

        noUploadRoutesRadioButton.setText("No Upload Routes");
        noUploadRoutesRadioButton.setName("routeCategory"); // NOI18N
        noUploadRoutesRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                noUploadRoutesRadioButtonMouseClicked(evt);
            }
        });
        noUploadRoutesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noUploadRoutesRadioButtonActionPerformed(evt);
            }
        });

        allMemberRadioButton.setText("All Members");
        allMemberRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allMemberRadioButtonActionPerformed(evt);
            }
        });

        gtfsMemberRadioButton.setText("Members from GTFS only");
        gtfsMemberRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gtfsMemberRadioButtonActionPerformed(evt);
            }
        });

        osmMemberRadioButton.setText("Members from OSM only");
        osmMemberRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                osmMemberRadioButtonActionPerformed(evt);
            }
        });

        bothMemberRadioButton.setText("Members from both dataset");
        bothMemberRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bothMemberRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout routePanelLayout = new javax.swing.GroupLayout(routePanel);
        routePanel.setLayout(routePanelLayout);
        routePanelLayout.setHorizontalGroup(
            routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGap(120, 120, 120)
                .addComponent(gtfsRouteIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                .addGap(582, 582, 582))
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(totalGtfsRoutesLabel))
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(routePanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, routePanelLayout.createSequentialGroup()
                                .addGap(342, 342, 342)
                                .addComponent(newRouteIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))))
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(293, 293, 293)
                        .addComponent(totalGtfsRelationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(routePanelLayout.createSequentialGroup()
                                .addGap(274, 274, 274)
                                .addComponent(osmRouteIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
                            .addGroup(routePanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40)
                                .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(totalNewRelationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 406, Short.MAX_VALUE)
                        .addComponent(totalOsmRelationLabel)
                        .addGap(85, 85, 85))))
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGap(208, 208, 208)
                .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(allRoutesRadioButton)
                    .addComponent(modifyRoutesRadioButton)
                    .addComponent(noUploadRoutesRadioButton)
                    .addComponent(newRoutesRadioButton))
                .addGap(516, 516, 516)
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bothMemberRadioButton)
                    .addComponent(osmMemberRadioButton)
                    .addComponent(gtfsMemberRadioButton)
                    .addComponent(allMemberRadioButton))
                .addContainerGap(445, Short.MAX_VALUE))
        );

        routePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane12, jScrollPane14, jScrollPane21});

        routePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane16, jScrollPane17, jScrollPane18, jScrollPane22});

        routePanelLayout.setVerticalGroup(
            routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addComponent(totalGtfsRoutesLabel)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                .addGap(167, 167, 167))
            .addGroup(routePanelLayout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                .addContainerGap(167, Short.MAX_VALUE))
            .addGroup(routePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gtfsRouteIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newRouteIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(osmRouteIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(routePanelLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(totalNewRelationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(totalGtfsRelationLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(allMemberRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gtfsMemberRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(osmMemberRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bothMemberRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(routePanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(totalOsmRelationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(routePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                                    .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                                    .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(33, 33, 33)))
                        .addGap(7, 7, 7)
                        .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(161, 161, 161))
                    .addGroup(routePanelLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                        .addGap(41, 41, 41)
                        .addComponent(allRoutesRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newRoutesRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modifyRoutesRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(noUploadRoutesRadioButton)))
                .addContainerGap())
        );

        routePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jScrollPane11, jScrollPane12, jScrollPane13, jScrollPane14, jScrollPane15, jScrollPane16, jScrollPane17, jScrollPane18, jScrollPane20, jScrollPane21, jScrollPane23, jScrollPane24});

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

        fileMenu.setText("File");

        exportGtfsMenuItem.setText("Export StopsTo GTFS format");
        exportGtfsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGtfsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportGtfsMenuItem);

        exportOsmMenuItem.setText("Export Stops with OSM data");
        exportOsmMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOsmMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportOsmMenuItem);

        jMenuBar1.add(fileMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(600, Short.MAX_VALUE)
                .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(596, 596, 596))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 646, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(uploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
        );

        uploadButton.getAccessibleContext().setAccessibleName("uploadButton");

        getAccessibleContext().setAccessibleName("report");

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
                finalStops.remove(ts);
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
    }

    public void newRouteActivated(){
        Route r = null;
        if((String)gtfsRouteList.getSelectedValue()!=null) r = (Route)finalRoutes.get((String)gtfsRouteList.getSelectedValue());
        if(r!=null){
            updateNewRouteTagList(r);
            updateNewRouteMemberList(r);
        }
    }

    public void osmRouteActivated(){
        Route r = null;
        if((String)gtfsRouteList.getSelectedValue()!=null) r = (Route)existingRoutes.get((String)gtfsRouteList.getSelectedValue());
        if(r!=null) {
            updateOsmRouteTagList(r);
            updateOsmRouteMemberList(r);
        }
    }

    public void clearAllRouteLists(){
        gtfsTagKey.clear();
        gtfsTagValue.clear();
        gtfsMemberValue.clear();
        gtfsMatchId.clear();
        newTagKey.clear();
        newTagValue.clear();
        routeMemberAll.clear();
        newMemberList.setModel(routeMemberAll);
        newMatchIdList.setModel(routeMemberAll);
        osmTagKey.clear();
        osmTagValue.clear();
        osmMemberValue.clear();
        osmMatchId.clear();
    }

    public void gtfsRouteActivated(){
        clearAllRouteLists();
        Route r = null;
        if((String)gtfsRouteList.getSelectedValue()!=null) r = (Route)agencyRoutes.get((String)gtfsRouteList.getSelectedValue());
        if(r!=null){
            updateGtfsRouteTagList(r);
            updateGtfsRouteMemberList(r);
            newRouteActivated();
            osmRouteActivated();
        }
    }

    private void uploadButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadButtonMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_uploadButtonMouseClicked

    private void allStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_allStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDAll);
        updateGtfsIdList();
    }//GEN-LAST:event_allStopsRadioButtonMouseClicked

    private void noUploadStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noUploadStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDNoUpload);
        updateGtfsIdList();
    }//GEN-LAST:event_noUploadStopsRadioButtonMouseClicked

    private void uploadConflictStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadConflictStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDUploadConflict);
        updateGtfsIdList();
    }//GEN-LAST:event_uploadConflictStopsRadioButtonMouseClicked

    private void uploadNoConflictStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_uploadNoConflictStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDUploadNoConflict);
        updateGtfsIdList();
    }//GEN-LAST:event_uploadNoConflictStopsRadioButtonMouseClicked

    private void modifyStopsRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modifyStopsRadioButtonMouseClicked
        // TODO add your handling code here:
        gtfsIDs.clear();
        gtfsIDList.setModel(gtfsIDModify);
        updateGtfsIdList();
    }//GEN-LAST:event_modifyStopsRadioButtonMouseClicked

    private void updateRouteMessage(){
        if((String)gtfsRouteList.getSelectedValue()!=null) {
            Route r = (Route)finalRoutes.get((String)gtfsRouteList.getSelectedValue());
            RelationMember rm = r.getOsmMember((String)newMemberList.getSelectedValue());
            //rm.setStatus is invoked in CompareData and RouteParser
            if(rm.getStatus()!=null) {
                reportRouteMessage.setText("This relation member exists in "+rm.getStatus());
            }
            else if(rm.getType()!=null) reportRouteMessage.setText("This relation member is a "+rm.getType()+" in OSM");
        }
    }

    private void osmTagKeyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmTagKeyListMouseClicked
        // TODO add your handling code here:
        osmTagValueList.setSelectedIndex(osmTagKeyList.getSelectedIndex());
        osmTagValueList.ensureIndexIsVisible(osmTagKeyList.getSelectedIndex());
}//GEN-LAST:event_osmTagKeyListMouseClicked

    private void osmMatchIdListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmMatchIdListMouseClicked
        // TODO add your handling code here:
        osmMemberList.setSelectedIndex(osmMatchIdList.getSelectedIndex());
        osmMemberList.ensureIndexIsVisible(osmMatchIdList.getSelectedIndex());
}//GEN-LAST:event_osmMatchIdListMouseClicked

    private void gtfsRouteListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsRouteListMouseClicked
        // TODO add your handling code here:
        gtfsRouteActivated();
    }//GEN-LAST:event_gtfsRouteListMouseClicked

    private void gtfsRouteListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsRouteListKeyReleased
        // TODO add your handling code here:
        gtfsRouteActivated();
    }//GEN-LAST:event_gtfsRouteListKeyReleased

    private void gtfsMemberListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsMemberListMouseClicked
        // TODO add your handling code here:
        gtfsMatchIdList.setSelectedIndex(gtfsMemberList.getSelectedIndex());
        gtfsMatchIdList.ensureIndexIsVisible(gtfsMemberList.getSelectedIndex());
    }//GEN-LAST:event_gtfsMemberListMouseClicked

    private void gtfsMatchIdListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsMatchIdListMouseClicked
        // TODO add your handling code here:
        gtfsMemberList.setSelectedIndex(gtfsMatchIdList.getSelectedIndex());
        gtfsMemberList.ensureIndexIsVisible(gtfsMatchIdList.getSelectedIndex());
    }//GEN-LAST:event_gtfsMatchIdListMouseClicked

    private void gtfsTagValueListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsTagValueListMouseClicked
        // TODO add your handling code here:
        gtfsTagKeyList.setSelectedIndex(gtfsTagValueList.getSelectedIndex());
        gtfsTagKeyList.ensureIndexIsVisible(gtfsTagValueList.getSelectedIndex());
    }//GEN-LAST:event_gtfsTagValueListMouseClicked

    private void gtfsTagKeyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsTagKeyListMouseClicked
        // TODO add your handling code here:
        gtfsTagValueList.setSelectedIndex(gtfsTagKeyList.getSelectedIndex());
        gtfsTagValueList.ensureIndexIsVisible(gtfsTagKeyList.getSelectedIndex());
    }//GEN-LAST:event_gtfsTagKeyListMouseClicked

    private void gtfsDetailsValueListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsDetailsValueListMouseClicked
        // TODO add your handling code here:
        gtfsDetailsKeyList.setSelectedIndex(gtfsDetailsValueList.getSelectedIndex());
        gtfsDetailsKeyList.ensureIndexIsVisible(gtfsDetailsValueList.getSelectedIndex());
    }//GEN-LAST:event_gtfsDetailsValueListMouseClicked

    private void gtfsDetailsKeyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gtfsDetailsKeyListMouseClicked
        // TODO add your handling code here:
        gtfsDetailsValueList.setSelectedIndex(gtfsDetailsKeyList.getSelectedIndex());
        gtfsDetailsValueList.ensureIndexIsVisible(gtfsDetailsKeyList.getSelectedIndex());
    }//GEN-LAST:event_gtfsDetailsKeyListMouseClicked

    private void newDetailsValueListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newDetailsValueListMouseClicked
        // TODO add your handling code here:
        newDetailsKeyList.setSelectedIndex(newDetailsValueList.getSelectedIndex());
        newDetailsKeyList.ensureIndexIsVisible(newDetailsValueList.getSelectedIndex());
    }//GEN-LAST:event_newDetailsValueListMouseClicked

    private void newDetailsKeyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newDetailsKeyListMouseClicked
        // TODO add your handling code here:
        newDetailsValueList.setSelectedIndex(newDetailsKeyList.getSelectedIndex());
        newDetailsValueList.ensureIndexIsVisible(newDetailsKeyList.getSelectedIndex());
    }//GEN-LAST:event_newDetailsKeyListMouseClicked

    private void osmDetailsValueListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmDetailsValueListMouseClicked
        // TODO add your handling code here:
        osmDetailsKeyList.setSelectedIndex(osmDetailsValueList.getSelectedIndex());
        osmDetailsKeyList.ensureIndexIsVisible(osmDetailsValueList.getSelectedIndex());
    }//GEN-LAST:event_osmDetailsValueListMouseClicked

    private void osmDetailsKeyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmDetailsKeyListMouseClicked
        // TODO add your handling code here:
        osmDetailsValueList.setSelectedIndex(osmDetailsKeyList.getSelectedIndex());
        osmDetailsValueList.ensureIndexIsVisible(osmDetailsKeyList.getSelectedIndex());
    }//GEN-LAST:event_osmDetailsKeyListMouseClicked

    private void newTagKeyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newTagKeyListMouseClicked
        // TODO add your handling code here:
        newTagValueList.setSelectedIndex(newTagKeyList.getSelectedIndex());
        newTagValueList.ensureIndexIsVisible(newTagKeyList.getSelectedIndex());
    }//GEN-LAST:event_newTagKeyListMouseClicked

    private void newTagValueListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newTagValueListMouseClicked
        // TODO add your handling code here:
        newTagKeyList.setSelectedIndex(newTagValueList.getSelectedIndex());
        newTagKeyList.ensureIndexIsVisible(newTagValueList.getSelectedIndex());
    }//GEN-LAST:event_newTagValueListMouseClicked

    private void newMemberListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newMemberListMouseClicked
        // TODO add your handling code here:
        newMatchIdList.setSelectedIndex(newMemberList.getSelectedIndex());
        newMatchIdList.ensureIndexIsVisible(newMemberList.getSelectedIndex());
        updateRouteMessage();
    }//GEN-LAST:event_newMemberListMouseClicked

    private void newMatchIdListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newMatchIdListMouseClicked
        // TODO add your handling code here:
        newMemberList.setSelectedIndex(newMatchIdList.getSelectedIndex());
        newMemberList.ensureIndexIsVisible(newMatchIdList.getSelectedIndex());
        updateRouteMessage();
    }//GEN-LAST:event_newMatchIdListMouseClicked

    private void osmMemberListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmMemberListMouseClicked
        // TODO add your handling code here:
        osmMatchIdList.setSelectedIndex(osmMemberList.getSelectedIndex());
        osmMatchIdList.ensureIndexIsVisible(osmMemberList.getSelectedIndex());
    }//GEN-LAST:event_osmMemberListMouseClicked

    private void osmTagValueListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_osmTagValueListMouseClicked
        // TODO add your handling code here:
        osmTagKeyList.setSelectedIndex(osmTagValueList.getSelectedIndex());
        osmTagKeyList.ensureIndexIsVisible(osmTagValueList.getSelectedIndex());
    }//GEN-LAST:event_osmTagValueListMouseClicked

    private void gtfsDetailsKeyListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsDetailsKeyListKeyReleased
        // TODO add your handling code here:
        gtfsDetailsValueList.setSelectedIndex(gtfsDetailsKeyList.getSelectedIndex());
        gtfsDetailsValueList.ensureIndexIsVisible(gtfsDetailsKeyList.getSelectedIndex());
    }//GEN-LAST:event_gtfsDetailsKeyListKeyReleased

    private void gtfsDetailsValueListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsDetailsValueListKeyReleased
        // TODO add your handling code here:
        gtfsDetailsKeyList.setSelectedIndex(gtfsDetailsValueList.getSelectedIndex());
        gtfsDetailsKeyList.ensureIndexIsVisible(gtfsDetailsValueList.getSelectedIndex());
    }//GEN-LAST:event_gtfsDetailsValueListKeyReleased

    private void newDetailsKeyListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_newDetailsKeyListKeyReleased
        // TODO add your handling code here:
        newDetailsValueList.setSelectedIndex(newDetailsKeyList.getSelectedIndex());
        newDetailsValueList.ensureIndexIsVisible(newDetailsKeyList.getSelectedIndex());
    }//GEN-LAST:event_newDetailsKeyListKeyReleased

    private void newDetailsValueListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_newDetailsValueListKeyReleased
        // TODO add your handling code here:
        newDetailsKeyList.setSelectedIndex(newDetailsValueList.getSelectedIndex());
        newDetailsKeyList.ensureIndexIsVisible(newDetailsValueList.getSelectedIndex());
    }//GEN-LAST:event_newDetailsValueListKeyReleased

    private void osmDetailsKeyListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmDetailsKeyListKeyReleased
        // TODO add your handling code here:
        osmDetailsValueList.setSelectedIndex(osmDetailsKeyList.getSelectedIndex());
        osmDetailsValueList.ensureIndexIsVisible(osmDetailsKeyList.getSelectedIndex());
    }//GEN-LAST:event_osmDetailsKeyListKeyReleased

    private void osmDetailsValueListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmDetailsValueListKeyReleased
        // TODO add your handling code here:
        osmDetailsKeyList.setSelectedIndex(osmDetailsValueList.getSelectedIndex());
        osmDetailsKeyList.ensureIndexIsVisible(osmDetailsValueList.getSelectedIndex());
    }//GEN-LAST:event_osmDetailsValueListKeyReleased

    private void gtfsTagKeyListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsTagKeyListKeyReleased
        // TODO add your handling code here:
        gtfsTagValueList.setSelectedIndex(gtfsTagKeyList.getSelectedIndex());
        gtfsTagValueList.ensureIndexIsVisible(gtfsTagKeyList.getSelectedIndex());
    }//GEN-LAST:event_gtfsTagKeyListKeyReleased

    private void gtfsTagValueListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsTagValueListKeyReleased
        // TODO add your handling code here:
        gtfsTagKeyList.setSelectedIndex(gtfsTagValueList.getSelectedIndex());
        gtfsTagKeyList.ensureIndexIsVisible(gtfsTagValueList.getSelectedIndex());
    }//GEN-LAST:event_gtfsTagValueListKeyReleased

    private void gtfsMemberListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsMemberListKeyReleased
        // TODO add your handling code here:
        gtfsMatchIdList.setSelectedIndex(gtfsMemberList.getSelectedIndex());
        gtfsMatchIdList.ensureIndexIsVisible(gtfsMemberList.getSelectedIndex());
    }//GEN-LAST:event_gtfsMemberListKeyReleased

    private void gtfsMatchIdListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gtfsMatchIdListKeyReleased
        // TODO add your handling code here:
        gtfsMemberList.setSelectedIndex(gtfsMatchIdList.getSelectedIndex());
        gtfsMemberList.ensureIndexIsVisible(gtfsMatchIdList.getSelectedIndex());
    }//GEN-LAST:event_gtfsMatchIdListKeyReleased

    private void newTagKeyListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_newTagKeyListKeyReleased
        // TODO add your handling code here:
        newTagValueList.setSelectedIndex(newTagKeyList.getSelectedIndex());
        newTagValueList.ensureIndexIsVisible(newTagKeyList.getSelectedIndex());
    }//GEN-LAST:event_newTagKeyListKeyReleased

    private void newTagValueListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_newTagValueListKeyReleased
        // TODO add your handling code here:
        newTagKeyList.setSelectedIndex(newTagValueList.getSelectedIndex());
        newTagKeyList.ensureIndexIsVisible(newTagValueList.getSelectedIndex());
    }//GEN-LAST:event_newTagValueListKeyReleased

    private void newMemberListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_newMemberListKeyReleased
        // TODO add your handling code here:
        newMatchIdList.setSelectedIndex(newMemberList.getSelectedIndex());
        newMatchIdList.ensureIndexIsVisible(newMemberList.getSelectedIndex());
        updateRouteMessage();
    }//GEN-LAST:event_newMemberListKeyReleased

    private void newMatchIdListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_newMatchIdListKeyReleased
        // TODO add your handling code here:
        newMemberList.setSelectedIndex(newMatchIdList.getSelectedIndex());
        newMemberList.ensureIndexIsVisible(newMatchIdList.getSelectedIndex());
        updateRouteMessage();
    }//GEN-LAST:event_newMatchIdListKeyReleased

    private void osmMatchIdListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmMatchIdListKeyReleased
        // TODO add your handling code here:
        osmMemberList.setSelectedIndex(osmMatchIdList.getSelectedIndex());
        osmMemberList.ensureIndexIsVisible(osmMatchIdList.getSelectedIndex());
    }//GEN-LAST:event_osmMatchIdListKeyReleased

    private void osmMemberListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmMemberListKeyReleased
        // TODO add your handling code here:
        osmMatchIdList.setSelectedIndex(osmMemberList.getSelectedIndex());
        osmMatchIdList.ensureIndexIsVisible(osmMemberList.getSelectedIndex());
    }//GEN-LAST:event_osmMemberListKeyReleased

    private void osmTagValueListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmTagValueListKeyReleased
        // TODO add your handling code here:
        osmTagKeyList.setSelectedIndex(osmTagValueList.getSelectedIndex());
        osmTagKeyList.ensureIndexIsVisible(osmTagValueList.getSelectedIndex());
    }//GEN-LAST:event_osmTagValueListKeyReleased

    private void osmTagKeyListKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_osmTagKeyListKeyReleased
        // TODO add your handling code here:
        osmTagValueList.setSelectedIndex(osmTagKeyList.getSelectedIndex());
        osmTagValueList.ensureIndexIsVisible(osmTagKeyList.getSelectedIndex());
    }//GEN-LAST:event_osmTagKeyListKeyReleased

    private void exportGtfsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGtfsMenuItemActionPerformed
        // TODO add your handling code here:
        WriteFile.exportStops("GTFSformat.txt", report, true, taskOutput);
    }//GEN-LAST:event_exportGtfsMenuItemActionPerformed

    private void exportOsmMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOsmMenuItemActionPerformed
        // TODO add your handling code here:
        WriteFile.exportStops("OSMformat.txt", report, false, taskOutput);
    }//GEN-LAST:event_exportOsmMenuItemActionPerformed

    private void allRoutesRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_allRoutesRadioButtonMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_allRoutesRadioButtonMouseClicked

    private void newRoutesRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newRoutesRadioButtonMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_newRoutesRadioButtonMouseClicked

    private void modifyRoutesRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modifyRoutesRadioButtonMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_modifyRoutesRadioButtonMouseClicked

    private void noUploadRoutesRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noUploadRoutesRadioButtonMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_noUploadRoutesRadioButtonMouseClicked

    private void newRoutesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRoutesRadioButtonActionPerformed
        // TODO add your handling code here:
        gtfsRouteIDs.clear();
        gtfsRouteList.setModel(gtfsRouteNew);
        updateGtfsRouteIdList();
}//GEN-LAST:event_newRoutesRadioButtonActionPerformed

    private void modifyRoutesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyRoutesRadioButtonActionPerformed
        // TODO add your handling code here:
        gtfsRouteIDs.clear();
        gtfsRouteList.setModel(gtfsRouteModify);
        updateGtfsRouteIdList();
}//GEN-LAST:event_modifyRoutesRadioButtonActionPerformed

    private void allRoutesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allRoutesRadioButtonActionPerformed
        // TODO add your handling code here:
        gtfsRouteIDs.clear();
        gtfsRouteList.setModel(gtfsRouteAll);
        updateGtfsRouteIdList();
    }//GEN-LAST:event_allRoutesRadioButtonActionPerformed

    private void noUploadRoutesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noUploadRoutesRadioButtonActionPerformed
        // TODO add your handling code here:
        gtfsRouteIDs.clear();
        gtfsRouteList.setModel(gtfsRouteNoUpload);
        updateGtfsRouteIdList();
}//GEN-LAST:event_noUploadRoutesRadioButtonActionPerformed

    private void gtfsMemberRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gtfsMemberRadioButtonActionPerformed
        // TODO add your handling code here:
        newMemberList.setModel(routeMemberGtfs);
        newMatchIdList.setModel(routeMemberMatchGtfs);
        totalNewRelationLabel.setText(Integer.toString(routeMemberGtfs.size()));
}//GEN-LAST:event_gtfsMemberRadioButtonActionPerformed

    private void allMemberRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allMemberRadioButtonActionPerformed
        // TODO add your handling code here:
        newMemberList.setModel(routeMemberAll);
        newMatchIdList.setModel(routeMemberMatchAll);
        totalNewRelationLabel.setText(Integer.toString(routeMemberAll.size()));
}//GEN-LAST:event_allMemberRadioButtonActionPerformed

    private void osmMemberRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_osmMemberRadioButtonActionPerformed
        // TODO add your handling code here:
        newMemberList.setModel(routeMemberOsm);
        newMatchIdList.setModel(routeMemberMatchOsm);
        totalNewRelationLabel.setText(Integer.toString(routeMemberOsm.size()));
    }//GEN-LAST:event_osmMemberRadioButtonActionPerformed

    private void bothMemberRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bothMemberRadioButtonActionPerformed
        // TODO add your handling code here:
        newMemberList.setModel(routeMemberBoth);
        newMatchIdList.setModel(routeMemberMatchBoth);
        totalNewRelationLabel.setText(Integer.toString(routeMemberBoth.size()));
    }//GEN-LAST:event_bothMemberRadioButtonActionPerformed

    private void doneModifyStopButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_doneModifyStopButtonMouseClicked
        // TODO add your handling code here:
}//GEN-LAST:event_doneModifyStopButtonMouseClicked

    private void doneModifyStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneModifyStopButtonActionPerformed
        // TODO add your handling code here:
        //remove deleted stops from report
        doneModifyStopButton.setEnabled(false);
        reviseReport();
        //write data to file
        new WriteFile(FILE_NAME_OUT_REPORT_REVISE, report);
        new WriteFile(FILE_NAME_OUT_UPLOAD_REVISE, upload);
        new WriteFile(FILE_NAME_OUT_MODIFY_REVISE, modify);
        new WriteFile(FILE_NAME_OUT_DELETE_REVISE, delete);
    }//GEN-LAST:event_doneModifyStopButtonActionPerformed

    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
        // TODO add your handling code here:
        uploadButton.setEnabled(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        taskUpload = new UploadData(progressBar, osmRequest, upload, modify, delete, finalRoutes);
        taskUpload.addPropertyChangeListener(this);
        taskUpload.execute();
    }//GEN-LAST:event_uploadButtonActionPerformed

    private void matchStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchStopButtonActionPerformed
        // TODO add your handling code here:
        String gtfsSt = (String)gtfsIDList.getSelectedValue();
        if(gtfsSt!=null){
            int index = gtfsIDList.getSelectedIndex();
            Stop st = (Stop)finalStops.get(gtfsSt);
            String category = st.getReportCategory();
            if(category!=null && category.equals("UPLOAD_CONFLICT")){
                if (!report.get(st).equals("none")){
                    ArrayList<Stop> arrOsm = (ArrayList<Stop>)report.get(st);
                    for(int i=0; i<arrOsm.size(); i++){
                        String gtfsStopOsmId = arrOsm.get(i).getOsmId();
                        String osmStopOsmId = (String)osmIDList.getSelectedValue();
                        if(gtfsStopOsmId!=null && osmStopOsmId!=null && gtfsStopOsmId.equals(osmStopOsmId)){
                            report.remove(st);
                            finalStops.remove(st);
                            upload.remove(st);
                            ArrayList<Stop> newArr = new ArrayList<Stop>();
                            newArr.add(arrOsm.get(i));

                            Stop newSt = new Stop(st);
                            newSt.removeTag("FIXME");
                            newSt.setOsmId(osmStopOsmId);
                            newSt.setOsmVersion(arrOsm.get(i).getOsmVersion());
                            newSt.setReportCategory("MODIFY");
                            newSt.setReportText("User matched stop");
                            newSt.setStatus("modify");
                            newSt.addTags(arrOsm.get(i).getTags());

                            report.put(newSt, newArr);
                            finalStops.put(newSt.getStopID(), newSt);
                            modify.add(newSt);
                            break;
                        }
                    }
                }

                gtfsIDUploadConflict.removeElement(gtfsSt);
                gtfsIDModify.addElement(gtfsSt);
                gtfsIDList.setSelectedIndex(index);
                gtfsStopActivated();
            }
        }
        matchStopButton.setEnabled(false);
    }//GEN-LAST:event_matchStopButtonActionPerformed

    private void removeGtfsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeGtfsButtonActionPerformed
        // TODO add your handling code here:
        String s = (String)gtfsIDList.getSelectedValue();
        if(s!=null){
            int index = gtfsIDList.getSelectedIndex();
            gtfsIDAll.removeElement(s);
            gtfsIDUploadConflict.removeElement(s);
            gtfsIDModify.removeElement(s);
            gtfsIDNoUpload.removeElement(s);
            gtfsIDUploadNoConflict.removeElement(s);
            gtfsIDList.setSelectedIndex(index);
            gtfsStopActivated();
        }
    }//GEN-LAST:event_removeGtfsButtonActionPerformed

    private void dumbOsmChangeTextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dumbOsmChangeTextButtonActionPerformed
        // TODO add your handling code here:
        String osmChangeText = osmRequest.getRequestContents("DUMB", upload, modify, delete, finalRoutes);
        new WriteFile("DUMB_OSM_CHANGE.txt", osmChangeText);
}//GEN-LAST:event_dumbOsmChangeTextButtonActionPerformed

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
    private javax.swing.JRadioButton allMemberRadioButton;
    private javax.swing.JRadioButton allRoutesRadioButton;
    private javax.swing.JRadioButton allStopsRadioButton;
    private javax.swing.JRadioButton bothMemberRadioButton;
    private javax.swing.JButton doneModifyStopButton;
    private javax.swing.JButton dumbOsmChangeTextButton;
    private javax.swing.JMenuItem exportGtfsMenuItem;
    private javax.swing.JMenuItem exportOsmMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JList gtfsDetailsKeyList;
    private javax.swing.JList gtfsDetailsValueList;
    private javax.swing.JList gtfsIDList;
    private javax.swing.JList gtfsMatchIdList;
    private javax.swing.JList gtfsMemberList;
    private javax.swing.JRadioButton gtfsMemberRadioButton;
    private javax.swing.JLabel gtfsRouteIDLabel;
    private javax.swing.JList gtfsRouteList;
    private javax.swing.JLabel gtfsStopIDLabel;
    private javax.swing.JList gtfsTagKeyList;
    private javax.swing.JList gtfsTagValueList;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton matchStopButton;
    private javax.swing.JRadioButton modifyRoutesRadioButton;
    private javax.swing.JRadioButton modifyStopsRadioButton;
    private javax.swing.JList newDetailsKeyList;
    private javax.swing.JList newDetailsValueList;
    private javax.swing.JList newMatchIdList;
    private javax.swing.JList newMemberList;
    private javax.swing.JLabel newRouteIDLabel;
    private javax.swing.JRadioButton newRoutesRadioButton;
    private javax.swing.JLabel newStopIDLabel;
    private javax.swing.JList newTagKeyList;
    private javax.swing.JList newTagValueList;
    private javax.swing.JRadioButton noUploadRoutesRadioButton;
    private javax.swing.JRadioButton noUploadStopsRadioButton;
    private javax.swing.JList osmDetailsKeyList;
    private javax.swing.JList osmDetailsValueList;
    private javax.swing.JList osmIDList;
    private javax.swing.JList osmMatchIdList;
    private javax.swing.JList osmMemberList;
    private javax.swing.JRadioButton osmMemberRadioButton;
    private javax.swing.JLabel osmRouteIDLabel;
    private javax.swing.JLabel osmStopIDLabel;
    private javax.swing.JList osmTagKeyList;
    private javax.swing.JList osmTagValueList;
    private javax.swing.JButton removeGtfsButton;
    private javax.swing.JTextArea reportMessage;
    private javax.swing.JTextArea reportRouteMessage;
    private javax.swing.JPanel routePanel;
    private javax.swing.JPanel stopPanel;
    private javax.swing.JLabel totalGtfsRelationLabel;
    private javax.swing.JLabel totalGtfsRoutesLabel;
    private javax.swing.JLabel totalNewRelationLabel;
    private javax.swing.JLabel totalOsmRelationLabel;
    private javax.swing.JLabel totalStopsLabel;
    private javax.swing.JButton uploadButton;
    private javax.swing.JRadioButton uploadConflictStopsRadioButton;
    private javax.swing.JRadioButton uploadNoConflictStopsRadioButton;
    // End of variables declaration//GEN-END:variables

}
