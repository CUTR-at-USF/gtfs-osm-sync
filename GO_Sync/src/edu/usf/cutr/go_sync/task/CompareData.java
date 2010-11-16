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

package edu.usf.cutr.go_sync.task;

import edu.usf.cutr.go_sync.gui.ReportViewer;
import edu.usf.cutr.go_sync.osm.*;
import edu.usf.cutr.go_sync.io.GTFSReadIn;
import edu.usf.cutr.go_sync.io.WriteFile;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import edu.usf.cutr.go_sync.object.OperatorInfo;
import edu.usf.cutr.go_sync.object.OsmPrimitive;
import edu.usf.cutr.go_sync.object.RelationMember;
import edu.usf.cutr.go_sync.object.Route;
import edu.usf.cutr.go_sync.object.Stop;
import org.xml.sax.helpers.AttributesImpl;
import edu.usf.cutr.go_sync.tools.OsmDistance;
import edu.usf.cutr.go_sync.tools.OsmFormatter;

/**
 *
 * @author Khoa Tran
 */

public class CompareData extends OsmTask{
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
    private Hashtable<String, Route> routes = new Hashtable<String, Route>();
    private Hashtable agencyRoutes = new Hashtable();
    private Hashtable existingRoutes = new Hashtable();
    private double minLat=0, minLon=0, maxLat=0, maxLon=0;
    private HttpRequest osmRequest;
    private Hashtable lastUsers = new Hashtable();
    private Hashtable osmIdToGtfsId = new Hashtable();

    private final double ERROR_TO_ZERO = 0.5;       // acceptable error while calculating distance ~= consider as 0
    private final double DELTA = 0.004;   // ~400m in Lat and 400m in Lon       0.00001 ~= 1.108m in Lat and 0.983 in Lon
    private final double RANGE = 400;         // FIX ME bus stop is within 400 meters
    private final String ROUTE_KEY = "route_ref";
    private String fileNameInStops; //= "Khoa_transit\\stops.txt";
    private String fileNameInTrips; //= "Khoa_transit\\trips.txt";
    private String fileNameInRoutes;
    private String fileNameInStopTimes; //= "Khoa_transit\\stop_times.txt";
/*    public static final String FILE_NAME_OUT_EXISTING = "existingStops.txt";
//    public static final String FILE_NAME_OUT_EXISTING = "C:\\Users\\Khoa Tran\\Desktop\\Summer REU 2010\\FloridaStops.txt";
    public static final String FILE_NAME_OUT_NEW = "newStops.txt";
    public static final String FILE_NAME_OUT_OVERLAPPED = "overlappedStops.txt";
    public static final String FILE_NAME_OUT_QUALIFIED = "qualifiedStops.txt";
    public static final String FILE_NAME_OUT_BOUND = "bounds.txt";
    public static final String FILE_NAME_OUT_UPLOAD = "UPLOAD.txt";
    public static final String FILE_NAME_OUT_MODIFY = "MODIFY.txt";
    public static final String FILE_NAME_OUT_DELETE = "DELETE.txt";
    public static final String FILE_NAME_OUT_NOUPLOAD = "NOUPLOAD.txt";
    public static final String FILE_NAME_OUT_REPORT = "REPORT.txt";*/

    private ProgressMonitor progressMonitor;
    private int progress = 0;
    private JTextArea taskOutput;
    
    public CompareData(ProgressMonitor pm, JTextArea to){
        super(pm);
        taskOutput = to;
        osmRequest = new HttpRequest(taskOutput);
        fileNameInStops = OperatorInfo.getFileDirectory()+"\\stops.txt";
        fileNameInTrips = OperatorInfo.getFileDirectory()+"\\trips.txt";
        fileNameInRoutes = OperatorInfo.getFileDirectory()+"\\routes.txt";
        fileNameInStopTimes = OperatorInfo.getFileDirectory()+"\\stop_times.txt";
        progressMonitor = pm;
    }

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
//        new WriteFile(FILE_NAME_OUT_BOUND, boundList);

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
    public Hashtable compareOsmTags(Hashtable osmtag, OsmPrimitive p) {
        Hashtable diff = new Hashtable();
        Hashtable t = new Hashtable();
        Iterator it = p.keySet().iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            String v = p.getTag(k);
            if (osmtag.containsKey(k)) {
                String osmValue = (String)osmtag.get(k);
                if(!osmValue.toUpperCase().equals(v.toUpperCase())){
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
        //20% of task includes:
        //10% for reading GTFS routes from modify and nothing_new sets
        //10% for compare with existing OSM routes

        updateProgress(10);
        this.setMessage("Reading GTFS routes from modify and noupload sets...");
        // get all the routes and its associated bus stops
        ArrayList<Stop> reportKeys = new ArrayList<Stop>();
        reportKeys.addAll(report.keySet());
        HashSet<String> gtfsRoutes = new HashSet<String>();
        gtfsRoutes.addAll(GTFSReadIn.getAllRoutesID());
        for (int i=0; i<reportKeys.size(); i++) {
            Stop st = reportKeys.get(i);
            String category = st.getReportCategory();
            if (category.equals("MODIFY") || category.equals("NOTHING_NEW")) {
                ArrayList<Route> routeInOneStop = new ArrayList<Route>();
                if(st.getRoutes()!=null) {
                    routeInOneStop.addAll(st.getRoutes());
                    for(int j=0; j<routeInOneStop.size(); j++){
                        Route rios = routeInOneStop.get(j);
                        Route r;
                        if(!routes.containsKey(rios.getRouteId())){
                            r = new Route(rios);
                            //add tag
                            r.addTag("name", OperatorInfo.getAbbreviateName()+
                                    " route "+ r.getRouteRef());
                            r.addTag("operator",OperatorInfo.getFullName());
                            r.addTag("ref", r.getRouteRef());
                            r.addTag("route", "bus");
                            r.addTag("type", "route");
                        }
                        else {
                            r = new Route((Route)routes.get(rios.getRouteId()));
                            routes.remove(rios.getRouteId());
                        }
                        //add member
                        //Route rt = (Route)routes.get(routeArray[j]);
                        r.addOsmMembers(rios.getOsmMembers());
                        String osmNodeId = st.getOsmId();
                        RelationMember rm = new RelationMember(osmNodeId,"node","stop");
                        rm.setStatus("GTFS dataset");
                        rm.setGtfsId(st.getStopID());
                        r.addOsmMember(rm);
                        r.setStatus("n");
                        routes.put(rios.getRouteId(), r);
                    }
                }
            }
        }

        agencyRoutes.putAll(routes);

        updateProgress(10);
        this.setMessage("Comparing GTFS routes with OSM routes...");
        //compare with existing OSM relation
        ArrayList<String> routeKeys = new ArrayList<String>();
        routeKeys.addAll(routes.keySet());
        for(int osm=0; osm<OSMRelations.size(); osm++){
            AttributesImpl osmRelation = OSMRelations.get(osm);
            Hashtable osmtag = new Hashtable();
            osmtag.putAll(OSMRelationTags.get(osm));
            String routeName = (String)osmtag.get("ref");
            String routeId = (String)osmtag.get("gtfs_route_id");
            String operator = (String)osmtag.get("operator");
            if(routeKeys.contains(routeId) && operator!=null && OperatorInfo.isTheSameOperator(operator)) {
                HashSet<RelationMember> em = OSMRelationMembers.get(osm);
                Route r = new Route((Route)routes.get(routeId));
                Route er = new Route(routeId, routeName, operator);
                ArrayList<RelationMember> tempem = new ArrayList<RelationMember>();
                tempem.addAll(em);
                for(int i=0; i<em.size(); i++) {
                    RelationMember m = tempem.get(i);
                    m.setGtfsId((String)osmIdToGtfsId.get(m.getRef()));
                    er.addOsmMember(m);

                    RelationMember matchMember = r.getOsmMember(m.getRef());
                    if(matchMember!=null) {
                        matchMember.setStatus("both GTFS dataset and OSM server");
                    } else {
                        r.addOsmMember(new RelationMember(m));
                    }
                }
                er.addTags(osmtag);
                er.setOsmVersion(osmRelation.getValue("version"));
                
                Hashtable diff = compareOsmTags(osmtag, r);
                if(!em.containsAll(r.getOsmMembers()) || diff.size()!=0){
                    r.setStatus("m");
                    r.setOsmVersion(osmRelation.getValue("version"));
                    r.setOsmId(osmRelation.getValue("id"));
                    r.addOsmMembers(em);
                    r.addTags(osmtag);
                }
                else {
                    r.setStatus("e");
                }

                routes.remove((String)r.getRouteId());
                routes.put(r.getRouteId(), r);

                existingRoutes.put(routeId, er);
            }
        }
        System.out.println("There are "+routeKeys.size()+" in total!");
    }

    public void compareBusStopData() {
        //Compare the OSM stops with GTFS data

        //this method takes 50% of this compare task
        int totalOsmNode = OSMNodes.size();
        int timeToUpdate, progressToUpdate;
        if(totalOsmNode>=50) {
            timeToUpdate = totalOsmNode/50;
            progressToUpdate = 1;
        } else {
            timeToUpdate = 1;
            progressToUpdate = 50/totalOsmNode;
        }
        int currentTotalProgress=0;
        for (int osmindex=0; osmindex<totalOsmNode; osmindex++){
            if((osmindex%timeToUpdate)==0) {
                currentTotalProgress += progressToUpdate;
                updateProgress(progressToUpdate);
                this.setMessage("Comparing "+osmindex+"/"+totalOsmNode+" ...");
            }
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
                            osmIdToGtfsId.put(node.getValue("id"), gtfsStop.getStopID());
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
                                Hashtable diff = compareOsmTags(osmtag, gtfsStop);
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
                                Hashtable diff = compareOsmTags(osmtag, gtfsStop);
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
                                    osmIdToGtfsId.put(node.getValue("id"), gtfsStop.getStopID());
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
                                osmIdToGtfsId.put(node.getValue("id"), gtfsStop.getStopID());
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
        //make sure is 50% overall
        int tempProgress=50-currentTotalProgress;
        updateProgress(Math.max(0, tempProgress));
        this.setMessage("Finish comparing bus stop data...");
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

    public void startCompare(){
        updateProgress(1);
        this.setMessage("Getting bounding box...");
        getBoundingBox();
        //Get the existing bus stops from the server
        updateProgress(1);
        this.setMessage("Checking API version...");
        System.out.println("Initializing...");
        osmRequest.checkVersion();

        updateProgress(5);
        this.setMessage("Getting existing bus stops...");
        progressMonitor.setNote("This might take several minutes...");
        ArrayList<AttributesImpl> tempOSMNodes = osmRequest.getExistingBusStops(Double.toString(minLon), Double.toString(minLat),
                Double.toString(maxLon), Double.toString(maxLat));
        progressMonitor.setNote("");
        updateProgress(10);
        this.setMessage("Getting existing bus routes...");
        progressMonitor.setNote("This might take several minutes...");
        ArrayList<AttributesImpl> tempOSMRelations = osmRequest.getExistingBusRelations(Double.toString(minLon), Double.toString(minLat),
                Double.toString(maxLon), Double.toString(maxLat));
        progressMonitor.setNote("");
        if (tempOSMNodes!=null) {
            OSMNodes.addAll(tempOSMNodes);
            OSMTags.addAll(osmRequest.getExistingBusStopsTags());
//            new WriteFile(FILE_NAME_OUT_EXISTING, convertToStopObject(OSMNodes, OperatorInfo.getFullName()));
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

    @Override
    public Void doInBackground() {
        setProgress(0);
        updateProgress(1);
        this.setMessage("Reading GTFS files ... ");
        GTFSReadIn data = new GTFSReadIn();
        List<Stop> st = data.readBusStop(fileNameInStops, OperatorInfo.getFullName(), fileNameInRoutes, fileNameInTrips, fileNameInStopTimes);
        if(st!=null) {
            GTFSstops.addAll(st);
//            new WriteFile(FILE_NAME_OUT_NEW, GTFSstops);

            startCompare();
            
            System.out.println("To be uploaded nodes = "+upload.size());
            
            System.out.println("To be modified nodes = "+modify.size());
            
            updateProgress(4);
            this.setMessage("Writing upload, noupload, modify, delete stops to file...");
//            new WriteFile(FILE_NAME_OUT_UPLOAD, upload);

//            new WriteFile(FILE_NAME_OUT_NOUPLOAD, noUpload);

//            new WriteFile(FILE_NAME_OUT_MODIFY, modify);

//            new WriteFile(FILE_NAME_OUT_DELETE, delete);

            updateProgress(3);
            this.setMessage("Writing report to file...");
            System.out.println("progress: "+progress);
//            new WriteFile(FILE_NAME_OUT_REPORT, report);
        }
        else {this.setMessage("No GTFS stops to be processed");}
        //make sure it's a complete task
        updateProgress(100);
        this.setMessage("Done...");
        System.out.println("Done...!!");
        return null;
    }

    @Override
    public void done() {
        Toolkit.getDefaultToolkit().beep();
        boolean isCanceled = progressMonitor.isCanceled();
        progressMonitor.setProgress(0);
        progressMonitor.close();
        if(!isCanceled) generateReport();
    }

    public void generateReport(){
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ReportViewer(GTFSstops, report, upload, modify, delete, routes, agencyRoutes, existingRoutes, taskOutput).setVisible(true);
            }
        });
    }
}
