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

package gui;

import io.GTFSReadIn;
import io.WriteFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import object.OperatorInfo;
import object.RelationMember;
import object.Route;
import object.Session;
import object.Stop;
import org.xml.sax.helpers.AttributesImpl;
import osm.HttpRequest;
import tools.OsmDistance;
import tools.OsmFormatter;

/**
 *
 * @author Khoa Tran
 */
public class MainForm extends javax.swing.JFrame {

    private List<Stop> GTFSstops = new ArrayList<Stop>();
    private ArrayList<AttributesImpl> OSMNodes = new ArrayList<AttributesImpl>();
    private ArrayList<Hashtable> OSMTags = new ArrayList<Hashtable>();
    private ArrayList<AttributesImpl> OSMRelations = new ArrayList<AttributesImpl>();
    private ArrayList<Hashtable> OSMRelationTags = new ArrayList<Hashtable>();
    private ArrayList<HashSet<RelationMember>> OSMRelationMembers = new ArrayList<HashSet<RelationMember>>();
    private Hashtable report = new Hashtable();
    private HashSet<Stop> noUpload = new HashSet<Stop>();
    private HashSet<Stop> upload = new HashSet<Stop>();
    private HashSet<Stop> modify = new HashSet<Stop>();
    private HashSet<Stop> delete = new HashSet<Stop>();
    private Hashtable routes = new Hashtable();
    private double minLat=0, minLon=0, maxLat=0, maxLon=0;
    private HttpRequest osmRequest = new HttpRequest();
    private Hashtable lastUsers = new Hashtable();

    private final double ERROR_TO_ZERO = 0.5;       // acceptable error while calculating distance ~= consider as 0
    private final double DELTA = 0.004;   // ~400m in Lat and 400m in Lon       0.00001 ~= 1.108m in Lat and 0.983 in Lon
    private final double RANGE = 400;         // FIX ME bus stop is within 400 meters
    private final String ROUTE_KEY = "route_ref";
    private String _operatorName;
    private String _operatorNameAbbreviate;
    private String _operatorNtdId;
    private String _username;
    private String _password;
    private String _changesetComment;
    private int _gtfsIdDigit;
    private String _revertChangesetId;
    public static final String FILE_NAME_IN_STOPS = "Khoa_transit\\stops.txt";
    public static final String FILE_NAME_IN_TRIPS = "Khoa_transit\\trips.txt";
    public static final String FILE_NAME_IN_STOP_TIMES = "Khoa_transit\\stop_times.txt";
    public static final String FILE_NAME_OUT_EXISTING = "existingStops.txt";
//    public static final String FILE_NAME_OUT_EXISTING = "C:\\Users\\Khoa Tran\\Desktop\\Summer REU 2010\\FloridaStops.txt";
    public static final String FILE_NAME_OUT_NEW = "newStops.txt";
    public static final String FILE_NAME_OUT_OVERLAPPED = "overlappedStops.txt";
    public static final String FILE_NAME_OUT_QUALIFIED = "qualifiedStops.txt";
    public static final String FILE_NAME_OUT_BOUND = "bounds.txt";
    public static final String FILE_NAME_OUT_UPLOAD = "UPLOAD.txt";
    public static final String FILE_NAME_OUT_MODIFY = "MODIFY.txt";
    public static final String FILE_NAME_OUT_DELETE = "DELETE.txt";
    public static final String FILE_NAME_OUT_NOUPLOAD = "NOUPLOAD.txt";
    public static final String FILE_NAME_OUT_REPORT = "REPORT.txt";

    public List<Stop> convertToStopObject(List<AttributesImpl> eNodes, String agencyName){
        List<Stop> stops = new ArrayList<Stop>(eNodes.size());
        for (int i=0; i<eNodes.size(); i++){
            AttributesImpl s = eNodes.get(i);
            stops.add(new Stop(s.getValue("id"), agencyName, s.getValue("user"), s.getValue("lat"), s.getValue("lon")));
        }
        return stops;
    }

    public void getBoundingBox(){
        // Get bound
        Iterator<Stop> it = GTFSstops.iterator();
        boolean isFirst = true;
        while (it.hasNext()) {
            Stop tempStop = it.next();
            if (isFirst) {
                isFirst = false;
                minLat = Double.parseDouble(tempStop.getLat());
                maxLat = Double.parseDouble(tempStop.getLat());
                minLon = Double.parseDouble(tempStop.getLon());
                maxLon = Double.parseDouble(tempStop.getLon());
                //                    System.out.println(minLat+","+minLon+","+maxLat+","+maxLon);
            }
            else {
                double currLat = Double.parseDouble(tempStop.getLat());
                double currLon = Double.parseDouble(tempStop.getLon());
                //                    System.out.println("currLat = "+currLat+"  currLon = "+currLon);
                if(currLat < minLat){
                    minLat = currLat;
                } else if(currLat > maxLat) {
                    maxLat = currLat;
                }

                if(currLon < minLon){
                    minLon = currLon;
                } else if(currLon > maxLon) {
                    maxLon = currLon;
                }
            }
        }
        minLat -= DELTA;
        minLon -= DELTA;
        maxLat += DELTA;
        maxLon += DELTA;

        System.out.println("Lon, Lat format = "+minLon+","+minLat + "      " + maxLon + "," + maxLat);

        List<Stop> boundList = new ArrayList<Stop>(2);
        boundList.add(new Stop("-1","Min Lat Min Lon", "UNKNOWN", Double.toString(minLat),Double.toString(minLon)));
        boundList.add(new Stop("-1","Max Lat Max Lon", "UNKNOWN",Double.toString(maxLat),Double.toString(maxLon)));
        new WriteFile(FILE_NAME_OUT_BOUND, boundList);

    }

    public void addToReport(Stop gtfs, Stop osm, boolean found) {
        Stop gtfsStop = new Stop(gtfs);
        if (osm!=null) {
            Stop osmStop = new Stop(osm);
            ArrayList<Stop> arr = new ArrayList<Stop>();
            if (report.containsKey(gtfsStop)) {
                if (!found) {
                    arr.addAll((ArrayList)report.get(gtfsStop));
                }
                report.remove(gtfsStop);
            }
            arr.add(osmStop);
            report.put(gtfsStop, arr);
        }
        // successfully added
        else {
            if (report.containsKey(gtfsStop)) {
                report.remove(gtfsStop);
            }
            report.put(gtfsStop, "none");
        }
    }

    /* Remove duplicates between upload set and noUpload set
     * Otherwise, many noUpload nodes will be in upload set
     * */
    public void reviseUpload(){
        Iterator it = noUpload.iterator();
        while (it.hasNext()) {
            Stop s = (Stop)it.next();
            if (upload.contains(s)) {
                upload.remove(s);
            }
        }
    }

    /* Remove duplicates between modify set and noUpload set
     * Otherwise, all modify nodes will be in noUpload set
     * since our algorithm add node into noUpload set everytime a gtfs node matches an OSM node
     * */
    public void reviseNoUpload(){
        Iterator it = modify.iterator();
        while (it.hasNext()) {
            Stop s = (Stop)it.next();
            if (noUpload.contains(s)) {
                noUpload.remove(s);
            }
        }
    }

    /* compare osmtag with new gtfs tag
    */
    public Hashtable compareStopTags(Hashtable osmtag, Stop s2) {
        Hashtable diff = new Hashtable();
        Hashtable t = new Hashtable();
        Iterator it = s2.keySet().iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = s2.getTag(k);
            if (osmtag.containsKey(k)) {
                String osmValue = (String)osmtag.get(k);
                if(!osmValue.equals(v)){
                    if (osmValue.indexOf(v)==-1) {
                        diff.put(k, v+";"+osmValue);
                    } else {
                        t.put(k, osmValue);
                    }
                }
            } else {
                diff.put(k, v);
            }
        }
        if(diff.size()>0 && t.size()>0) {
            diff.putAll(t);
        }
        return diff;
    }

    /*
     * Only consider uploaded bus stops 
     * e.g., stops in noUpload and (modify sets - modified stops in osm)
     * We can use report hashtable for convenience
     * ALWAYS Invoke this method AFTER compareBusStopData()
     * */
    public void compareRouteData() {
        // get all the routes and its associated bus stops
        ArrayList<Stop> reportKeys = new ArrayList<Stop>();
        reportKeys.addAll(report.keySet());
        HashSet<String> gtfsRoutes = new HashSet<String>();
        gtfsRoutes.addAll(GTFSReadIn.getAllRoutesID());
        for (int i=0; i<reportKeys.size(); i++) {
            Stop st = reportKeys.get(i);
            String category = st.getReportCategory();
            if (category.equals("MODIFY") || category.equals("NOTHING_NEW")) {
                String routeText = st.getTag(ROUTE_KEY);
                String[] routeArray;
                if (routeText==null){
                    System.out.println("null route "+st.getStopID());
                } else {
                    if(routeText.indexOf(";")!=-1) routeArray = routeText.split(";");
                    else {
                        routeArray = new String[1];
                        routeArray[0]=routeText;
                    }
                    for (int j=0; j<routeArray.length; j++) {
                        if(gtfsRoutes.contains(routeArray[j])){
                            Route r = new Route(routeArray[j], OperatorInfo.getFullName());
                            if(routes.containsKey(routeArray[j])){
                                Route rt = (Route)routes.get(routeArray[j]);
                                r.addOsmMembers(rt.getOsmMembers());
                                String osmNodeId = st.getOsmId();
                                RelationMember rm = new RelationMember(osmNodeId,"node","stop");
                                rm.setStatus("gtfs");
                                r.addOsmMember(rm);
                            }
                            r.setStatus("n");
                            routes.put(routeArray[j], r);
                        }
                    }
                }
            }
        }

        //compare with existing OSM relation
        ArrayList<Stop> routeKeys = new ArrayList<Stop>();
        routeKeys.addAll(routes.keySet());
        for(int osm=0; osm<OSMRelations.size(); osm++){
            AttributesImpl osmRelation = OSMRelations.get(osm);
            Hashtable osmtag = new Hashtable();
            osmtag.putAll(OSMRelationTags.get(osm));
            String routeRef = (String)osmtag.get("ref");
            String operator = (String)osmtag.get("operator");
            if(routeKeys.contains(routeRef) && operator!=null && OperatorInfo.isTheSameOperator(operator)) {
                HashSet<RelationMember> em = OSMRelationMembers.get(osm);
                Route r = (Route)routes.get(routeRef);
                if(!r.getOsmMembers().equals(em)){
                    r.setStatus("m");
                    r.setOsmVersion(osmRelation.getValue("version"));
                    r.setOsmId(osmRelation.getValue("id"));
                    r.addOsmMembers(em);
                    r.addAndOverwriteTags(osmtag);
                }
                else {
                    r.setStatus("e");
                }
            }
        }
        System.out.println("There are "+routeKeys.size()+" in total!");
    }

    public void compareBusStopData() {
        //Compare the OSM stops with GTFS data
//        HashSet<Stop> matched = new HashSet<Stop>();
        for (int osmindex=0; osmindex<OSMNodes.size(); osmindex++){
            Hashtable osmtag = new Hashtable();
            osmtag.putAll(OSMTags.get(osmindex));
            String osmOperator = (String)osmtag.get("operator");
            String osmStopID = (String)osmtag.get("gtfs_id");
            //add leading 0's
            if(osmStopID!=null) {
                if (!osmStopID.equals("missing")) {
                    osmStopID = OsmFormatter.getValidBusStopId(osmStopID);
                    osmtag.put("gtfs_id", osmStopID);
                }
            }
            
            boolean fixme = osmtag.containsKey("FIXME");
            boolean isOp;
            if (osmOperator!=null) {
                if (osmOperator.equals("missing")) {
                    isOp=true;
                } else {
                    isOp = OperatorInfo.isTheSameOperator(osmOperator);
                    // spell out the operator name
                    // osmOperator = _operatorName; //can't do this since it deletes the other operator
                }
            }
            // osmOperator == null --> isOp is true since we need to get to the for loop
            else {
                isOp=true;
                // set operator field to missing
                osmOperator = "missing";
            }
            String osmStopName = (String)osmtag.get("name");
            AttributesImpl node = OSMNodes.get(osmindex);
            String version = Integer.toString(Integer.parseInt(node.getValue("version")));
            if (isOp) {
                for (int gtfsindex=0; gtfsindex<GTFSstops.size(); gtfsindex++){
                    Stop gtfsStop = GTFSstops.get(gtfsindex);
                    double distance = OsmDistance.distVincenty(node.getValue("lat"), node.getValue("lon"),
                            gtfsStop.getLat(), gtfsStop.getLon());
                    if ((distance<RANGE) && !(noUpload.contains(gtfsStop)) ){//&& (!matched.contains(gtfsStop))){
                        // if has same stop_id
                        if ((osmStopID!= null) && (!osmStopID.equals("missing")) && (osmStopID.equals(gtfsStop.getStopID()))){
                            noUpload.add(gtfsStop);
                            if (distance>ERROR_TO_ZERO) {
                                Stop ns = new Stop(gtfsStop);
                                ns.addTags(osmtag);
                                ns.setOsmId(node.getValue("id"));
                                ns.setOsmVersion(version);

                                lastUsers.put(ns, node.getValue("user"));

                                Stop es = new Stop(osmStopID, osmOperator, osmStopName, node.getValue("lat"), node.getValue("lon"));
                                es.addTags(osmtag);
                                es.setOsmId(node.getValue("id"));
                                // for comparing tag
                                Hashtable diff = compareStopTags(osmtag, gtfsStop);
                                if (diff.size()==0) {
                                    es.setReportText("Stop already exists in OSM but with different location." +
                                            "\n ACTION: Modify OSM stop with new location!");
                                } else {
                                    ns.addAndOverwriteTags(diff);
                                    es.setReportText("Stop already exists in OSM but with different location.\n" +
                                            "\t   Some stop TAGs are also different." +
                                            "\n ACTION: Modify OSM stop with new location and stop tags!");
                                }

                                if (modify.contains(ns)) {
                                    modify.remove(ns);
                                }
                                modify.add(ns);
                                ns.setReportCategory("MODIFY");
                                addToReport(ns, es, true);
                            }
                            else {
                                Stop ns = new Stop(gtfsStop);
                                ns.setOsmId(node.getValue("id"));
                                ns.addTags(osmtag);
                                // for comparing tag
                                Hashtable diff = compareStopTags(osmtag, gtfsStop);
                                Stop es = new Stop(osmStopID, osmOperator, osmStopName, node.getValue("lat"), node.getValue("lon"));
                                es.addTags(osmtag);
                                es.setOsmId(node.getValue("id"));
                                if (diff.size()==0) {
                                    es.setReportText("Stop already exists in OSM. Nothing new from last upload.\n" +
                                        "\t   " + es.printOSMStop() +
                                        "\n ACTION: No upload!");
                                    ns.setReportCategory("NOTHING_NEW");
                                    addToReport(ns, es, true);
                                    noUpload.add(ns);
                                } else {
                                    ns.addAndOverwriteTags(diff);
                                    es.setReportText("Stop already exists in OSM but some TAGs are different.\n" +
                                            "\t   " + es.printOSMStop() + "\n ACTION: Modify OSM stop with new tags!");
                                    ns.setOsmVersion(version);
                                    if (modify.contains(ns)) {
                                        modify.remove(ns);
                                    }
                                    modify.add(ns);
                                    lastUsers.put(ns, node.getValue("user"));
                                    ns.setReportCategory("MODIFY");
                                    addToReport(ns, es, true);
                                }
                            }
                        }
                        // stop_id == null OR OSMnode does NOT have same stop id
                        else {
                            if (distance>ERROR_TO_ZERO) {
                                Stop ns = new Stop(gtfsStop);
                                ns.addTag("FIXME", "This bus stop could be redundant");
                                upload.add(ns);

                                Stop es = new Stop(osmStopID, osmOperator, osmStopName, node.getValue("lat"), node.getValue("lon"));
                                es.addTags(osmtag);
                                es.setOsmId(node.getValue("id"));
                                if ((osmStopID == null) || (osmStopID.equals("missing"))) {
                                    es.setReportText("Possible redundant stop. Please check again!");
                                    if ((!fixme) && (!modify.contains(es))) {
                                        Stop osms = new Stop(es);
                                        osms.addTag("FIXME", "This bus stop could be redundant");
                                        if (osmOperator==null || osmOperator.equals("missing")) {
                                            osms.addTag("note", "Please add gtfs_id and operator after removing FIXME");
                                            if (osmOperator==null) osms.addTag("operator","missing");
                                        }
                                        else {
                                            osms.addTag("note", "Please add gtfs_id after removing FIXME");
                                        }
                                        if (osmStopID==null) {
                                            osms.addAndOverwriteTag("gtfs_id","missing");
                                        }
                                        osms.setOsmVersion(version);

                                        modify.add(osms);

                                        lastUsers.put(osms, node.getValue("user"));
                                    }
                                }
                                else {
                                    es.setReportText("Different gtfs_id but in range. Possible redundant stop. Please check again!\n" +
                                        " ACTION: No modified with FIXME!");
                                }
                                ns.setReportCategory("UPLOAD_CONFLICT");
                                addToReport(ns, es, false);
                            }
                            // if same lat and lon --> possible same exact stop --> add gtfs_id, operator, stop_name
                            else {
                                Stop ns = new Stop(gtfsStop);
                                ns.addTags(osmtag);
                                ns.setOsmId(node.getValue("id"));
                                ns.setOsmVersion(version);

                                modify.add(ns);

                                lastUsers.put(ns, node.getValue("user"));

                                noUpload.add(ns);
                                Stop es = new Stop(osmStopID, osmOperator, osmStopName, node.getValue("lat"), node.getValue("lon"));
                                es.addTags(osmtag);
                                es.setOsmId(node.getValue("id"));
                                es.setOsmVersion(version);
                                es.setReportText("Possible redundant stop with gtfs_id = "+gtfsStop.getStopID() +
                                        "\n ACTION: Modify OSM stop["+node.getValue("id")+"] with expected gtfs_id and operator!");

                                ns.setReportCategory("MODIFY");
                                addToReport(ns, es, true);
                            }
                        }
                    }
                }
            }
        }
        reviseUpload();
        reviseNoUpload();
        // Add everything else without worries
        HashSet<Stop> reportKeys = new HashSet<Stop>(report.size());
        reportKeys.addAll(report.keySet());
        for (int i=0; i<GTFSstops.size(); i++) {
            if ((!noUpload.contains((GTFSstops.get(i)))) && (!reportKeys.contains(GTFSstops.get(i))) ) {
                Stop n = new Stop(GTFSstops.get(i));
                n.setReportText("New upload with no conflicts");
                n.setReportCategory("UPLOAD_NO_CONFLICT");
                upload.add(n);

                addToReport(n, null, false);
            }
        }

        Iterator it = lastUsers.keySet().iterator();
        HashSet<String> OSMUserList = new HashSet<String>();
        while (it.hasNext()) {
            Stop s = new Stop((Stop)it.next());
            OSMUserList.add((String)lastUsers.get(s));
        }
        System.out.println(OSMUserList.toString());
    }

    public void execute(){
        getBoundingBox();
        //Get the existing bus stops from the server
        System.out.println("Initializing...");
        osmRequest.checkVersion();
        ArrayList<AttributesImpl> tempOSMNodes = osmRequest.getExistingBusStops(Double.toString(minLon), Double.toString(minLat),
                Double.toString(maxLon), Double.toString(maxLat));
        ArrayList<AttributesImpl> tempOSMRelations = osmRequest.getExistingBusRelations(Double.toString(minLon), Double.toString(minLat),
                Double.toString(maxLon), Double.toString(maxLat));
        if (tempOSMNodes!=null) {
            OSMNodes.addAll(tempOSMNodes);
            OSMTags.addAll(osmRequest.getExistingBusStopsTags());
            new WriteFile(FILE_NAME_OUT_EXISTING, convertToStopObject(OSMNodes, _operatorName));
            System.out.println("Existing Nodes = "+OSMNodes.size());
            System.out.println("New Nodes = "+GTFSstops.size());
            compareBusStopData();

            if(tempOSMRelations!=null) {
                OSMRelations.addAll(tempOSMRelations);
                OSMRelationTags.addAll(osmRequest.getExistingBusRelationTags());
                OSMRelationMembers.addAll(osmRequest.getExistingBusRelationMembers());
            }
            compareRouteData();
        }
        else {
            System.out.println("There's no bus stop in the region "+minLon+", "+minLat+", "+maxLon+", "+maxLat);
        }
    }

    /** Creates new form MainForm */
    public MainForm() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        passwordField = new javax.swing.JPasswordField();
        usernameField = new javax.swing.JTextField();
        sessionCommentField = new javax.swing.JTextField();
        sessionCommentLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        exitButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        operatorNameLabel = new javax.swing.JLabel();
        OperatorAbbLabel = new javax.swing.JLabel();
        operatorNameAbbField = new javax.swing.JTextField();
        compareButton = new javax.swing.JButton();
        usernameLabel2 = new javax.swing.JLabel();
        operatorNameField = new javax.swing.JTextField();
        operatorNTDIDField = new javax.swing.JTextField();
        usernameLabel3 = new javax.swing.JLabel();
        gtfsIdDigitField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        changesetLabel = new javax.swing.JLabel();
        revertChangesetField = new javax.swing.JTextField();
        revertButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GO-Sync");
        setName("mainForm"); // NOI18N
        setResizable(false);

        usernameField.setName("usernameField"); // NOI18N

        sessionCommentField.setName("usernameField"); // NOI18N

        sessionCommentLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        sessionCommentLabel.setText("Session Comment");

        usernameLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        usernameLabel.setText("OSM Username");

        passwordLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        passwordLabel.setText("OSM Password");

        exitButton.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        exitButton.setText("Exit");
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitButtonMouseClicked(evt);
            }
        });

        operatorNameLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        operatorNameLabel.setText("Operator Name");

        OperatorAbbLabel.setFont(new java.awt.Font("Tahoma", 0, 14));
        OperatorAbbLabel.setText("Operator Abbreviate");

        operatorNameAbbField.setName("usernameField"); // NOI18N

        compareButton.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        compareButton.setText("Run");
        compareButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                compareButtonMouseClicked(evt);
            }
        });
        compareButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compareButtonActionPerformed(evt);
            }
        });

        usernameLabel2.setFont(new java.awt.Font("Tahoma", 0, 14));
        usernameLabel2.setText("Operator NTD ID");

        operatorNameField.setName("usernameField"); // NOI18N

        operatorNTDIDField.setName("usernameField"); // NOI18N

        usernameLabel3.setFont(new java.awt.Font("Tahoma", 0, 14));
        usernameLabel3.setText("GTFS ID digit");

        gtfsIdDigitField.setName("usernameField"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(OperatorAbbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(operatorNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gtfsIdDigitField, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(operatorNTDIDField, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                    .addComponent(operatorNameAbbField, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                    .addComponent(operatorNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(177, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(216, 216, 216)
                .addComponent(compareButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(243, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operatorNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(operatorNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operatorNameAbbField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OperatorAbbLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operatorNTDIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel2))
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gtfsIdDigitField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(compareButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {gtfsIdDigitField, operatorNTDIDField, operatorNameAbbField, operatorNameField});

        operatorNameAbbField.getAccessibleContext().setAccessibleName("operatorNameAbbField");
        operatorNameField.getAccessibleContext().setAccessibleName("operatorNameField");
        operatorNTDIDField.getAccessibleContext().setAccessibleName("OperatorNTDIDField");

        jTabbedPane1.addTab("Compare Data", jPanel1);

        jPanel2.setName(""); // NOI18N

        changesetLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        changesetLabel.setText("Changeset ID");

        revertChangesetField.setName("usernameField"); // NOI18N

        revertButton.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        revertButton.setText("Run");
        revertButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                revertButtonMouseClicked(evt);
            }
        });
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
                        .addGap(218, 218, 218)
                        .addComponent(revertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(165, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(revertChangesetField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changesetLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(revertButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Revert Changeset", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sessionCommentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sessionCommentField, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(passwordField)
                        .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(167, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(219, 219, 219)
                .addComponent(exitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(245, Short.MAX_VALUE))
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sessionCommentLabel)
                    .addComponent(sessionCommentField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(exitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        usernameField.getAccessibleContext().setAccessibleName("usernameField");
        usernameLabel.getAccessibleContext().setAccessibleName("usernameLabel");
        passwordLabel.getAccessibleContext().setAccessibleName("passwordLabel");
        exitButton.getAccessibleContext().setAccessibleName("exitButton");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void compareButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_compareButtonMouseClicked
        // TODO add your handling code here:
        // get data from user input
        try {
            _operatorName = operatorNameField.getText();
            _operatorNameAbbreviate = operatorNameAbbField.getText();
            _operatorNtdId = operatorNTDIDField.getText();
            _gtfsIdDigit = Integer.parseInt(gtfsIdDigitField.getText());
            _username = usernameField.getText();
            _password = new String(passwordField.getPassword());
            _changesetComment = sessionCommentField.getText();

            new OperatorInfo(_operatorName, _operatorNameAbbreviate, _operatorNtdId, _gtfsIdDigit);
            new Session(_username, _password, _changesetComment);
            GTFSReadIn data = new GTFSReadIn();
            GTFSstops.addAll(data.readBusStop(FILE_NAME_IN_STOPS, _operatorName,FILE_NAME_IN_TRIPS,FILE_NAME_IN_STOP_TIMES));
            new WriteFile(FILE_NAME_OUT_NEW, GTFSstops);

            execute();

            System.out.println("To be uploaded nodes = "+upload.size());
            System.out.println("To be modified nodes = "+modify.size());

            new WriteFile(FILE_NAME_OUT_UPLOAD, upload);

            new WriteFile(FILE_NAME_OUT_NOUPLOAD, noUpload);

            new WriteFile(FILE_NAME_OUT_MODIFY, modify);

            new WriteFile(FILE_NAME_OUT_DELETE, delete);

            new WriteFile(FILE_NAME_OUT_REPORT, report);

            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new ReportForm(GTFSstops, report, upload, modify, delete, routes).setVisible(true);
                }
            });

            System.out.println("Done...!!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Message: "+e.getMessage());
        }
}//GEN-LAST:event_compareButtonMouseClicked

    private void exitButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitButtonMouseClicked
        // TODO add your handling code here:
        System.exit(0);
}//GEN-LAST:event_exitButtonMouseClicked

    private void compareButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_compareButtonActionPerformed

    private void revertButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_revertButtonMouseClicked
        // TODO add your handling code here:
        try {
            _username = usernameField.getText();
            _password = new String(passwordField.getPassword());
            _changesetComment = sessionCommentField.getText();
            _revertChangesetId = revertChangesetField.getText();
            
            new Session(_username, _password, _changesetComment);
            System.out.println("Initializing...");
            osmRequest.checkVersion();
            osmRequest.downloadChangeSet(_revertChangesetId);
            osmRequest.checkVersion();
            osmRequest.createChangeSet();
            osmRequest.createChunks(osmRequest.getRevertUpload(), osmRequest.getRevertModify(), osmRequest.getRevertDelete(), null);
            osmRequest.closeChangeSet();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Message: "+e.getMessage());
        }
}//GEN-LAST:event_revertButtonMouseClicked

    private void revertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revertButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_revertButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel OperatorAbbLabel;
    private javax.swing.JLabel changesetLabel;
    private javax.swing.JButton compareButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField gtfsIdDigitField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField operatorNTDIDField;
    private javax.swing.JTextField operatorNameAbbField;
    private javax.swing.JTextField operatorNameField;
    private javax.swing.JLabel operatorNameLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JButton revertButton;
    private javax.swing.JTextField revertChangesetField;
    private javax.swing.JTextField sessionCommentField;
    private javax.swing.JLabel sessionCommentLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel usernameLabel2;
    private javax.swing.JLabel usernameLabel3;
    // End of variables declaration//GEN-END:variables

}
