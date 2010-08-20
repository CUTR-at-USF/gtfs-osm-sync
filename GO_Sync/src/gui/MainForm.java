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
    private ArrayList<HashSet<String>> OSMRelationMembers = new ArrayList<HashSet<String>>();
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
    private final String _operatorName = "Hillsborough Area Regional Transit";
    private final String _operatorNameAbbreviate = "HART";
    private final String _operatorNtdId = "4041"; // 4046 for Sarasota
    private final String _username = "ktran9";
    private final String _password = "testingosm";
    private final String _changesetComment = "testing connection";
    private final int _gtfsIdDigit = 4;
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
        Iterator it = s2.keySet().iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = s2.getTag(k);
            if (osmtag.containsKey(k)) {
                if (!((String)osmtag.get(k)).equals(v)) {
                    diff.put(k, "modified");
                }
            } else {
                diff.put(k, "new");
            }
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
                        Route r = new Route(routeArray[j], OperatorInfo.getFullName());
                        if(routes.containsKey(routeArray[j])){
                            Route rt = (Route)routes.get(routeArray[j]);
                            r.addOsmMembers(rt.getOsmMembers());
                            String osmNodeId = st.getOsmId();
                            r.addOsmMember(osmNodeId);
                        }
                        r.setStatus("n");
                        routes.put(routeArray[j], r);
                    }
                }
            }
        }
 /*
        private ArrayList<AttributesImpl> OSMRelations = new ArrayList<AttributesImpl>();
    private ArrayList<Hashtable> OSMRelationTags = new ArrayList<Hashtable>();
    private ArrayList<Hashtable> OSMRelationMembers = new ArrayList<Hashtable>();*/
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
                HashSet<String> em = OSMRelationMembers.get(osm);
                Route r = (Route)routes.get(routeRef);
                if(!r.getOsmMembers().equals(em)){
                    r.setStatus("m");
                    r.setOsmVersion(osmRelation.getValue("version"));
                    r.setOsmId(osmRelation.getValue("id"));
                    r.addOsmMembers(em);
                    r.addTags(osmtag);
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

                                if (modify.contains(ns)) {
                                    modify.remove(ns);
                                }
                                modify.add(ns);

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
                                    es.setReportText("Stop already exists in OSM but with different location.\n" +
                                            "\t   Some stop TAGs are also different." +
                                            "\n ACTION: Modify OSM stop with new location and stop tags!");
                                }

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
                                        if (osmOperator==null) {
                                            osms.addTag("note", "Please add gtfs_id and operator after removing FIXME");
                                            osms.addTag("operator","missing");
                                        }
                                        else {
                                            osms.addTag("note", "Please add gtfs_id after removing FIXME");
                                        }
                                        if (osmStopID==null) {
                                            osms.addTag("gtfs_id","missing");
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

            OSMRelations.addAll(tempOSMRelations);
            OSMRelationTags.addAll(osmRequest.getExistingBusRelationTags());
            OSMRelationMembers.addAll(osmRequest.getExistingBusRelationMembers());
            compareRouteData();
        }
        else {
            System.out.println("There's no bus stop in the region "+minLon+", "+minLat+", "+maxLon+", "+maxLat);
        }
    }

    public void deleteNodesInChangeSet(String csetID) {
        getBoundingBox();
        //Get the existing bus stops from the server
        System.out.println("Initializing...");
        osmRequest.checkVersion();
        ArrayList<AttributesImpl> tempOSMNodes = osmRequest.getExistingBusStops(Double.toString(minLon), Double.toString(minLat),
                Double.toString(maxLon), Double.toString(maxLat));
        OSMNodes.addAll(tempOSMNodes);
        OSMTags.addAll(osmRequest.getExistingBusStopsTags());
        System.out.println("Existing Nodes = "+OSMNodes.size());
        for (int osmindex=0; osmindex<OSMNodes.size(); osmindex++) {
            Hashtable osmtag = new Hashtable();
            osmtag.putAll(OSMTags.get(osmindex));
            String osmSource = (String)osmtag.get("source");
            AttributesImpl node = OSMNodes.get(osmindex);
            if (osmSource!=null){
                if (osmSource.equals("GO_Sync") && node.getValue("changeset").equals(csetID) && node.getValue("user").equals("ktran9")) {
                    Stop s = new Stop(node.getValue("id"),"no need","no need","0","0");
                    s.setOsmId(node.getValue("id"));
                    s.setOsmVersion(node.getValue("version"));
                    delete.add(s);
                }
            }
        }
        System.out.println("Deleting "+delete.size()+" stops in changeset +"+csetID);

        osmRequest.checkVersion();
        osmRequest.createChangeSet();
        osmRequest.createChunks(upload, modify, delete, routes);
        osmRequest.closeChangeSet();
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

        runButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        passwordField = new javax.swing.JPasswordField();
        usernameField = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        usernameLabel1 = new javax.swing.JLabel();
        passwordLabel1 = new javax.swing.JLabel();
        operatorNameField = new javax.swing.JTextField();
        operatorNameAbbField = new javax.swing.JTextField();
        usernameLabel2 = new javax.swing.JLabel();
        OperatorNTDIDField = new javax.swing.JTextField();
        sessionCommentLabel = new javax.swing.JLabel();
        sessionCommentField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GO-Sync");
        setName("mainForm"); // NOI18N
        setResizable(false);

        runButton.setFont(new java.awt.Font("Times New Roman", 1, 18));
        runButton.setText("Run");
        runButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                runButtonMouseClicked(evt);
            }
        });
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        exitButton.setFont(new java.awt.Font("Times New Roman", 1, 18));
        exitButton.setText("Exit");
        exitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitButtonMouseClicked(evt);
            }
        });

        usernameField.setName("usernameField"); // NOI18N

        usernameLabel.setFont(new java.awt.Font("Tahoma", 0, 14));
        usernameLabel.setText("OSM Username");

        passwordLabel.setFont(new java.awt.Font("Tahoma", 0, 14));
        passwordLabel.setText("OSM Password");

        usernameLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        usernameLabel1.setText("Operator Name");

        passwordLabel1.setFont(new java.awt.Font("Tahoma", 0, 14));
        passwordLabel1.setText("Operator Abbreviate");

        operatorNameField.setName("usernameField"); // NOI18N

        operatorNameAbbField.setName("usernameField"); // NOI18N

        usernameLabel2.setFont(new java.awt.Font("Tahoma", 0, 14));
        usernameLabel2.setText("Operator NTD ID");

        OperatorNTDIDField.setName("usernameField"); // NOI18N

        sessionCommentLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        sessionCommentLabel.setText("Session Comment");

        sessionCommentField.setName("usernameField"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usernameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(passwordField)
                            .addComponent(usernameField, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(90, 90, 90)
                        .addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85))
            .addGroup(layout.createSequentialGroup()
                .addGap(145, 145, 145)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sessionCommentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sessionCommentField, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usernameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usernameLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(OperatorNTDIDField)
                            .addComponent(operatorNameAbbField)
                            .addComponent(operatorNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operatorNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operatorNameAbbField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OperatorNTDIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sessionCommentField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sessionCommentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 97, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34))
        );

        exitButton.getAccessibleContext().setAccessibleName("exitButton");
        usernameField.getAccessibleContext().setAccessibleName("usernameField");
        usernameLabel.getAccessibleContext().setAccessibleName("usernameLabel");
        passwordLabel.getAccessibleContext().setAccessibleName("passwordLabel");
        operatorNameField.getAccessibleContext().setAccessibleName("operatorNameField");
        operatorNameAbbField.getAccessibleContext().setAccessibleName("operatorNameAbbField");
        OperatorNTDIDField.getAccessibleContext().setAccessibleName("OperatorNTDIDField");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_runButtonMouseClicked
        // TODO add your handling code here:
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
/*
        try {
            deleteNodesInChangeSet("5282878");// second upload csetID: 5282592
        } catch (OsmTransferException e) {
            System.out.println(e);
        }*/
        System.out.println("Done...!!");
    }//GEN-LAST:event_runButtonMouseClicked

    private void exitButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitButtonMouseClicked
        // TODO add your handling code here:
        System.exit(0);
}//GEN-LAST:event_exitButtonMouseClicked

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_runButtonActionPerformed

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
    private javax.swing.JTextField OperatorNTDIDField;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField operatorNameAbbField;
    private javax.swing.JTextField operatorNameField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel passwordLabel1;
    private javax.swing.JButton runButton;
    private javax.swing.JTextField sessionCommentField;
    private javax.swing.JLabel sessionCommentLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel usernameLabel1;
    private javax.swing.JLabel usernameLabel2;
    // End of variables declaration//GEN-END:variables

}
