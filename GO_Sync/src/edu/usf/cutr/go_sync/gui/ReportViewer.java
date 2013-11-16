/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ReportViewer.java
 *
 * Created on Oct 18, 2010, 10:15:53 PM
 */

package edu.usf.cutr.go_sync.gui;

import edu.usf.cutr.go_sync.gui.object.BooleanMouseListener;
import edu.usf.cutr.go_sync.gui.object.RouteMemberTableModel;
import edu.usf.cutr.go_sync.gui.object.TagReportTableModel;
import edu.usf.cutr.go_sync.io.WriteFile;
import edu.usf.cutr.go_sync.object.RelationMember;
import edu.usf.cutr.go_sync.object.Route;
import edu.usf.cutr.go_sync.object.Stop;
import edu.usf.cutr.go_sync.osm.HttpRequest;
import edu.usf.cutr.go_sync.task.UploadData;
import edu.usf.cutr.go_sync.tools.OsmDistance;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

/**
 *
 * @author Khoa Tran
 */
public class ReportViewer extends javax.swing.JFrame implements TableModelListener, PropertyChangeListener {

    private HttpRequest osmRequest;

    private Hashtable report;

    private HashSet<Stop> upload, modify, delete;

//    private Hashtable<Stop, Hashtable> agencyTable;

    private Hashtable<String, Stop> agencyStops = new Hashtable<String, Stop>();

    private Hashtable<String, Stop> finalStops;

    private Hashtable<String, Stop> osmDefaultFinalStops = new Hashtable<String, Stop>();
    
    private Hashtable<String, Stop> osmDefaultOnlyChangedFinalStops = new Hashtable<String, Stop>();

    private Hashtable<String, ArrayList<Boolean>> finalCheckboxes;

    private Hashtable<String, Stop> searchKeyToStop = new Hashtable<String, Stop>();

    private HashSet<String> stopsToFinish = new HashSet<String>();  // uploadConflict + modified

    private int totalNumberOfStopsToFinish = 0;

    private TagReportTableModel stopTableModel, routeTableModel;

    private RouteMemberTableModel memberTableModel;

    protected String[] tagReportColumnHeaderToolTips = {
        "Tag name",
        "<html>Values from transit agency under <br>General Transit Feeds Specification (GTFS) format</html>", null,
        "Values from OpenStreetMap", null,
        "Values to be added in OpenStreetMap"
    };

    private Stop[] gtfsStops, gtfsAll, gtfsUploadConflict, gtfsUploadNoConflict, gtfsModify, gtfsNoUpload;

    private Stop[] osmStops = new Stop[0];

    private Route[] gtfsRoutes, gtfsRouteAll, gtfsRouteUploadNoConflict, gtfsRouteModify, gtfsRouteNoUpload;

    private JXMapViewer mainMap;

    private Hashtable<GeoPosition, Stop> newStopsByGeoPos = new Hashtable<GeoPosition, Stop>();  // to interact with user on the map

    private HashSet<GeoPosition> allStopsGeo;

    private Painter<JXMapViewer> matchStopOverlayPainter, selectedGtfsOverlayPainter, selectedOsmOverlayPainter;

    private CompoundPainter mainPainter = new CompoundPainter();

    private WaypointPainter stopsPainter = new WaypointPainter();

    private TileFactory osmTf;

    private Hashtable finalRoutes, agencyRoutes, existingRoutes;

    private UploadData taskUpload = null;

    private JTextArea taskOutput;

    private JProgressBar progressBar;

    private boolean generateStopsToUploadFlag = false;

    /** Creates new form ReportViewer */
    public ReportViewer(List<Stop> aData, Hashtable r, HashSet<Stop>u, HashSet<Stop>m, HashSet<Stop>d, Hashtable routes, Hashtable nRoutes, Hashtable eRoutes, JTextArea to) {
        super("GO-Sync: Report");
        

        // set tooltip time for 10 seconds
        javax.swing.ToolTipManager.sharedInstance().setDismissDelay(10000);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        taskOutput = to;
        osmRequest = new HttpRequest(to);
//        agencyTable = new Hashtable<Stop, Hashtable>();
        for(int i=0; i<aData.size(); i++){
//            agencyTable.put(aData.get(i), aData.get(i).getTags());
            agencyStops.put(aData.get(i).toString(), aData.get(i));
        }

        report = new Hashtable();
        report.putAll(r);

        stopTableModel = new TagReportTableModel(0);

        upload = new HashSet<Stop>();
        upload.addAll(u);

        modify = new HashSet<Stop>();
        modify.addAll(m);

        delete = new HashSet<Stop>();
        delete.addAll(d);

        finalStops = new Hashtable<String, Stop>();
        finalCheckboxes = new Hashtable<String, ArrayList<Boolean>>();
/*
        finalRoutes = new Hashtable();
        finalRoutes.putAll(routes);

        agencyRoutes = new Hashtable();
        agencyRoutes.putAll(nRoutes);

        existingRoutes = new Hashtable();
        existingRoutes.putAll(eRoutes);
*/

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

        //get the total elements in each list first
        gtfsAll = new Stop[reportKeys.size()];
        int uci=0, unci=0, mi=0, nui=0;
        for (int i=0; i<reportKeys.size(); i++) {
            gtfsAll[i] = reportKeys.get(i);
            String category = gtfsAll[i].getReportCategory();
            if (category.equals("UPLOAD_CONFLICT")) {
                uci++;
            } else if (category.equals("UPLOAD_NO_CONFLICT")) {
                unci++;
            } else if (category.equals("MODIFY")) {
                mi++;
            } else if (category.equals("NOTHING_NEW")) {
                nui++;
            }
        }
        // add data to correct list (categorizing)
        gtfsUploadConflict = new Stop[uci];
        gtfsUploadNoConflict = new Stop[unci];
        gtfsModify = new Stop[mi];
        gtfsNoUpload = new Stop[nui];
        uci=0; unci=0; mi=0; nui=0;
        for (int i=0; i<reportKeys.size(); i++) {
            String category = reportKeys.get(i).getReportCategory();
            if (category.equals("UPLOAD_CONFLICT")) {
                gtfsUploadConflict[uci] = reportKeys.get(i);
                uci++;
                stopsToFinish.add(reportKeys.get(i).toString());
            } else if (category.equals("UPLOAD_NO_CONFLICT")) {
                gtfsUploadNoConflict[unci] = reportKeys.get(i);
                unci++;
            } else if (category.equals("MODIFY")) {
                gtfsModify[mi] = reportKeys.get(i);
                mi++;
                stopsToFinish.add(reportKeys.get(i).toString());
            } else if (category.equals("NOTHING_NEW")) {
                gtfsNoUpload[nui] = reportKeys.get(i);
                nui++;
            }

            totalNumberOfStopsToFinish = stopsToFinish.size();

            // for search functionality
            String stopSearchData = reportKeys.get(i).getStopName() + ";" +reportKeys.get(i).getStopID();
            searchKeyToStop.put(stopSearchData, reportKeys.get(i));
        }

        // set the list to All initially
        gtfsStops = gtfsAll;

        // set Final stops with Gtfs Value as Default
        for (int i=0; i<reportKeys.size(); i++) {
            Stop st = new Stop(reportKeys.get(i));
            String category = st.getReportCategory();

            // initialize boolean array to true for gtfs and false for osm
            // the size should be 2x of the number of tags+2(for lat,lon) since we need checkboxes for both osm and gtfs values
            // format: gtfs,osm,gtfs,osm,gtfs,osm,etc.
            int numberOfBool = (st.getTags().size()+2)*2;
            ArrayList<Boolean> arr = new ArrayList<Boolean>(numberOfBool);
            for(int j=0; j<numberOfBool; j++){
                if(category.equals("UPLOAD_CONFLICT") || category.equals("UPLOAD_NO_CONFLICT")) {
                    if(j%2==0) arr.add(true);
                    else arr.add(false);
                }
                else {
                    if(j%2==1) arr.add(true);
                    else arr.add(false);
                }
            }

            finalStops.put(st.getStopID(), st);
            finalCheckboxes.put(st.getStopID(), arr);
        }

        // set Final stops with Osm Value as Default
        for (int i=0; i<reportKeys.size(); i++) {
            Stop newStop = reportKeys.get(i);
            Stop osmStop = null;

            int numEquiv = 0;
            ArrayList<Stop> arr = null;
            if(report.get(reportKeys.get(i)) instanceof ArrayList){
                arr = (ArrayList<Stop>)report.get(reportKeys.get(i));
                if(arr.size()>1) numEquiv = 2;
                else if(arr.size()==1) numEquiv = 1;
            }
            if(numEquiv==1) {
                osmStop = new Stop((Stop)arr.get(0));
            } else {
                osmStop = new Stop((Stop)reportKeys.get(i));
            }
            osmDefaultFinalStops.put(osmStop.getStopID(), osmStop);

            String category = newStop.getReportCategory();
/*            if (category.equals("UPLOAD_CONFLICT")) {
                gtfsUploadConflict[uci] = reportKeys.get(i);
                uci++;
            } else if (category.equals("UPLOAD_NO_CONFLICT")) {
                gtfsUploadNoConflict[unci] = reportKeys.get(i);
                unci++;*/
            if ((category.equals("MODIFY") || category.equals("NOTHING_NEW")) && numEquiv==1) {
                //String stopID, String operatorName, String stopName, String lat, String lon
                Stop stopWithSelectedTags = new Stop(newStop.getStopID(), newStop.getOperatorName(), osmStop.getStopName(), osmStop.getLat(), osmStop.getLon());
                Stop agencyStop = agencyStops.get(newStop.getStopID());
                Hashtable<String, String> agencyTags = agencyStop.getTags();
                Hashtable<String, String> osmTags = osmStop.getTags();
                ArrayList<String> osmTagKeys = new ArrayList<String>();
                osmTagKeys.addAll(osmStop.keySet());
                osmTagKeys.remove("operator");
//                osmTagKeys.remove("highway");
                osmTagKeys.remove("source");
                boolean isDiff = false;
                for (int j=0; j<osmTagKeys.size(); j++){
                    String osmTagKey = osmTagKeys.get(j);
                    String agencyTagValue = agencyTags.get(osmTagKey);
                    String osmTagValue = osmTags.get(osmTagKey);
                    if((osmTagValue!=null || !osmTagValue.equals("")) && ((agencyTagValue==null) || (agencyTagValue.equals("")) || !osmTagValue.equals(agencyTagValue))) {
                        stopWithSelectedTags.addTag(osmTagKey, osmTagValue);
                        isDiff = true;
                    }
                }
                if(category.equals("MODIFY") || isDiff)
                    osmDefaultOnlyChangedFinalStops.put(stopWithSelectedTags.getStopID(), stopWithSelectedTags);
            }
        }

        // Routes
        routeTableModel = new TagReportTableModel(0);
        memberTableModel = new RouteMemberTableModel(0);

        finalRoutes = new Hashtable();
        finalRoutes.putAll(routes);

        agencyRoutes = new Hashtable();
        agencyRoutes.putAll(nRoutes);

        existingRoutes = new Hashtable();
        existingRoutes.putAll(eRoutes);

        //ordering by hashcode
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
        
        //get the total elements in each list first
        gtfsRouteAll = new Route[routeKeys.size()];

        unci=0; mi=0; nui=0;
        for (int i=0; i<routeKeys.size(); i++) {
            gtfsRouteAll[i] = (Route)finalRoutes.get(routeKeys.get(i));
            String status = gtfsRouteAll[i].getStatus();
            if (status.equals("n")) {
                unci++;
            } else if (status.equals("m")) {
                mi++;
            } else if (status.equals("e")) {
                nui++;
            }
        }
        // add data to correct list (categorizing)
        gtfsRouteUploadNoConflict = new Route[unci];
        gtfsRouteModify = new Route[mi];
        gtfsRouteNoUpload = new Route[nui];
        unci=0; mi=0; nui=0;
        for (int i=0; i<routeKeys.size(); i++) {
            Route tempr = (Route)finalRoutes.get(routeKeys.get(i));
            String status = tempr.getStatus();
            if (status.equals("n")) {
                gtfsRouteUploadNoConflict[unci] = tempr;
                unci++;
            } else if (status.equals("m")) {
                gtfsRouteModify[mi] = tempr;
                mi++;
            } else if (status.equals("e")) {
                gtfsRouteNoUpload[nui] = tempr;
                nui++;
            }
        }

        // set the list to All initially
        gtfsRoutes = gtfsRouteAll;
/*
        // set Final Routes
        for (int i=0; i<routeKeys.size(); i++) {
            Route rt = routeKeys.get(i);

            // initialize boolean array to true for gtfs and false for osm
            // the size should be 2x of the number of tags+2(for lat,lon) since we need checkboxes for both osm and gtfs values
            // format: gtfs,osm,gtfs,osm,gtfs,osm,etc.
            int numberOfBool = rt.getTags().size()*2;
            ArrayList<Boolean> arr = new ArrayList<Boolean>(numberOfBool);
            for(int j=0; j<numberOfBool; j++){
                if(j%2==0) arr.add(new Boolean(true));
                else arr.add(false);
            }

            finalStops.put(st.getStopID(), st);
            finalCheckboxes.put(st.getStopID(), arr);
        }
*/
        initComponents();

        // get main map
        mainMap = mapJXMapKit.getMainMap();

        // set text label
        if(gtfsStops.length!=0) updateStopCategory(gtfsAll, 0);

        // add waypoints to map. must be placed after updateCategory to have total zoom
        addNewBusStopToMap(new ArrayList<Stop>(report.keySet()));

        // create listener for map
        addMapListener(mainMap);

        if(gtfsRoutes.length!=0) updateRouteCategory(gtfsRouteAll);

        // for upload Task
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
    }

    private void updateStopTable(Stop selectedNewStop, Stop selectedOsmStop){
//        if(selectedNewStop==null) return;
        Stop agencyStop = agencyStops.get(selectedNewStop.toString());
        addSelectedStopsOverlay(selectedNewStop, selectedOsmStop);
        // get all the possible tag names from gtfs data and osm data
        HashSet<String> tagKeys = new HashSet<String>();
        Hashtable aTags = null;
        if(selectedNewStop!=null) {
            tagKeys.addAll(selectedNewStop.keySet());
//            aTags = (Hashtable)agencyTable.get(selectedNewStop);
            aTags = (agencyStops.get(selectedNewStop.toString())).getTags();
            // treat stops in upload_conflict differently from others
            if(selectedNewStop.getReportCategory().equals("UPLOAD_CONFLICT")) {
                tagKeys.addAll(aTags.keySet());
            }
        }
        if(selectedOsmStop!=null) tagKeys.addAll(selectedOsmStop.keySet());

        // set new size to the table
        stopTableModel = new TagReportTableModel(tagKeys.size()+2);
        dataTable.setModel(stopTableModel);

        // the same as tagKeys. For the purpose of using for loop
        ArrayList<String> tkeys = new ArrayList<String>();
        tkeys.addAll(tagKeys);

        // add data to table
        // first, add lat and lon
        Stop finalSt = (Stop)finalStops.get(selectedNewStop.getStopID());
        ArrayList<Boolean> finalCB = finalCheckboxes.get(selectedNewStop.getStopID());

        if(selectedOsmStop!=null) {
            stopTableModel.setRowValueAt(new Object[] {"lat", agencyStop.getLat(), finalCB.get(0), selectedOsmStop.getLat(), finalCB.get(1), finalSt.getLat()}, 0);
            stopTableModel.setRowValueAt(new Object[] {"lon", agencyStop.getLon(), finalCB.get(2), selectedOsmStop.getLon(), finalCB.get(3), finalSt.getLon()}, 1);
        } else {
            stopTableModel.setRowValueAt(new Object[] {"lat", agencyStop.getLat(), finalCB.get(0), "",finalCB.get(1), finalSt.getLat()}, 0);
            stopTableModel.setRowValueAt(new Object[] {"lon", agencyStop.getLon(), finalCB.get(2), "", finalCB.get(3), finalSt.getLon()}, 1);
        }
        for(int i=0; i<tkeys.size(); i++){
            String k = tkeys.get(i);

            //make sure there's null pointer
            String newValue="", osmValue="", gtfsValue="";
            if(selectedNewStop!=null) {
                newValue = (String)selectedNewStop.getTag(k);
                gtfsValue = (String)aTags.get(k);
            }
            if(selectedOsmStop!=null) osmValue = (String)selectedOsmStop.getTag(k);

            //add tag to table, index+2 because of lat and lon
            if(selectedNewStop.getReportCategory().equals("UPLOAD_CONFLICT")) {
                stopTableModel.setRowValueAt(new Object[] {k, gtfsValue, true, osmValue, false, newValue}, i+2);
            } else {
                stopTableModel.setRowValueAt(new Object[] {k, gtfsValue, finalCB.get((i+2)*2), osmValue, finalCB.get((i+2)*2+1), (String)finalSt.getTag(k)}, i+2);
            }
        }

        //set the column width with checkbox to minimum size
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn col = dataTable.getColumnModel().getColumn(2);
        int width = 15;
        col.setPreferredWidth(width);
        col = dataTable.getColumnModel().getColumn(4);
        col.setPreferredWidth(width);
        col = dataTable.getColumnModel().getColumn(0);
        col.setPreferredWidth(100);
        col = dataTable.getColumnModel().getColumn(1);
        col.setPreferredWidth(114);
        col = dataTable.getColumnModel().getColumn(3);
        col.setPreferredWidth(114);
        col = dataTable.getColumnModel().getColumn(5);
        col.setPreferredWidth(114);

        // detect any changes in table.
        // Must be placed after all data has been inserted inside the table. Otherwise, saveChangeButton is enabled
        dataTable.getModel().addTableModelListener(this);

        updateButtonTableStop("Accept", true, "Save Change", false);

        // set last edited information
        String lastEditedUser = "N/A";
        String lastEditedDate = "N/A";
        if(selectedOsmStop!=null) {
            if(selectedOsmStop.getLastEditedOsmUser()!=null && !selectedOsmStop.getLastEditedOsmUser().equals(""))
                lastEditedUser = selectedOsmStop.getLastEditedOsmUser();
            if(selectedOsmStop.getLastEditedOsmDate()!=null && !selectedOsmStop.getLastEditedOsmDate().equals(""))
                lastEditedDate = selectedOsmStop.getLastEditedOsmDate();
        }
        
        if(!lastEditedUser.equals("N/A")) lastEditedLabel.setText("Last edited by "+lastEditedUser+" on "+lastEditedDate);
        else lastEditedLabel.setText("Last edited: Information cannot be retrieved from OSM");
    }

    private void clearStopTable(){
        stopTableModel = new TagReportTableModel(0);
        dataTable.setModel(stopTableModel);
        dataTable.getModel().addTableModelListener(this);
    }

    private void updateStopCategory(Stop[] selectedCategory, int index){
        gtfsStops = selectedCategory;
        gtfsStopsComboBox.setModel(new DefaultComboBoxModel(gtfsStops));
        totalGtfsStopsLabel.setText(Integer.toString(gtfsStops.length));
        if(gtfsStops.length!=0) updateBusStop(gtfsStops[index]);
        else updateBusStop(null);
    }

    // When user click on gtfs combobox 
    private void updateBusStop(Stop st){
        //re-initialize array. set default to
        osmStops = new Stop[0];

        //map display purpose - includes gtfs stops and all possible osm matches
        HashSet<GeoPosition> tempStopsGeo = new HashSet<GeoPosition>();

        if (st!=null) {
            tempStopsGeo.add(new GeoPosition(Double.parseDouble(st.getLat()), Double.parseDouble(st.getLon())));
            if(report.get(st) instanceof ArrayList){
                // update osm combobox
                ArrayList<Stop> osmEquiv = (ArrayList<Stop>)report.get(st);
/*
                if(osmEquiv.size()>1){
                    tableStopButton.setVisible(false);
                } else {
                    tableStopButton.setVisible(true);
                }
*/
                osmStops = new Stop[osmEquiv.size()];
                for(int i=0; i<osmEquiv.size(); i++){
                    osmStops[i] = osmEquiv.get(i);
                    tempStopsGeo.add(new GeoPosition(Double.parseDouble(osmStops[i].getLat()), Double.parseDouble(osmStops[i].getLon())));
                }
                // if multiple stops, add the red overlay. If only 1 osm stop matches, it would duplicate the selected gtfs stop.
//                if((osmEquiv.size()>1) || !(OsmDistance.distVincenty(osmEquiv.get(0).getLat(), osmEquiv.get(0).getLon(), st.getLat(), st.getLon())<2)){
                    addMaskOverlay(new HashSet<Stop>(osmEquiv));
//                }
//                else {
//                    matchStopOverlayPainter = null;
//                }
                // update table
                updateStopTable(st, osmStops[0]);
            } else {
                updateStopTable(st, null);
            }
        } else {
            clearStopTable();
        }
        osmStopsComboBox.setModel(new DefaultComboBoxModel(osmStops));
        totalOsmStopsLabel.setText(Integer.toString(osmStops.length));

//        System.out.println("tempStopsGeo = "+tempStopsGeo.size());

        updateMainMap();

        if(!tempStopsGeo.isEmpty()) {
            mainMap.setZoom(1);
            ArrayList<GeoPosition> tempGeo = new ArrayList<GeoPosition>();
            tempGeo.addAll(tempStopsGeo);
            
            if(tempStopsGeo.size()>2 || (tempStopsGeo.size()==2 && OsmDistance.distVincenty(Double.toString(tempGeo.get(0).getLatitude()), Double.toString(tempGeo.get(0).getLongitude()),
                                                    Double.toString(tempGeo.get(1).getLatitude()), Double.toString(tempGeo.get(1).getLongitude()))>100)) mainMap.calculateZoomFrom(tempStopsGeo);
            else {
                mainMap.setAddressLocation(tempGeo.get(0)); // only Gtfs stop (new stop no conflict category) and match stops that are too near (<100meters)
            }
        }
    }

    private void addNewBusStopToMap(ArrayList<Stop> newStops){
        HashSet<Waypoint> waypoints = new HashSet<Waypoint>();

        //to Calculate Zoom
        allStopsGeo = new HashSet<GeoPosition>();

        for(int i=0; i<newStops.size(); i++){
            Stop st = newStops.get(i);
            waypoints.add(new Waypoint(Double.parseDouble(st.getLat()), Double.parseDouble(st.getLon())));
            GeoPosition pos = new GeoPosition(Double.parseDouble(st.getLat()), Double.parseDouble(st.getLon()));
            allStopsGeo.add(pos);
            newStopsByGeoPos.put(pos, st);
        }

        //crate a WaypointPainter to draw the points
        stopsPainter.setWaypoints(waypoints);

        stopsPainter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                Image busIcon = Toolkit.getDefaultToolkit().getImage("bus_icon.png");
                g.drawImage(busIcon, -5, -5, map);
                return true;
            }
        });

        mainMap.setZoom(1);
        mainMap.calculateZoomFrom(allStopsGeo);
        
        addMaskOverlay(null);
    }

    private void addMapListener(JXMapViewer mainMap){
        mainMap.addMouseListener(new MouseListener() {
            private Stop findStop(Point mousePt){
                JXMapViewer mainMap = mapJXMapKit.getMainMap();
                Iterator it = allStopsGeo.iterator();
                while(it.hasNext()){
                    GeoPosition st_gp = (GeoPosition)it.next();
                    //convert to pixel
                    Point2D st_gp_pt2D = mainMap.getTileFactory().geoToPixel(st_gp, mainMap.getZoom());
                    //convert to screen
                    Rectangle rect = mainMap.getViewportBounds();
                    Point st_gp_pt_screen = new Point((int)st_gp_pt2D.getX()-rect.x, (int)st_gp_pt2D.getY()-rect.y);
                    //check if near the mouse
                    if(st_gp_pt_screen.distance(mousePt)<10)
                        return newStopsByGeoPos.get(st_gp);
                }
                return null;
            }
            public void mouseClicked(MouseEvent e){
                Stop selected = findStop(e.getPoint());
                if(selected!=null){
                    System.out.println(selected);
                    System.out.println(selected.getLat()+","+selected.getLon());

                    // when the user is in multiple possible match category
                    // if user selects one of the items in osm list then only update osm list, no need to zoom in
                    boolean isInOsmList = false;
                    if(newWithMatchStopsRadioButton.isSelected()){
//                        gtfsStopsComboBox
                        // check if selected is in osm stop list
                        int num = osmStopsComboBox.getItemCount();
                        for (int i=0; i<num; i++) {
                            Stop item = (Stop)osmStopsComboBox.getItemAt(i);
                            if(item.equals(selected)){
                                isInOsmList = true;
                                break;
                            }
                        }

                        if(isInOsmList) {
                            osmStopsComboBox.setSelectedItem(selected);
                            updateStopTable((Stop)gtfsStopsComboBox.getSelectedItem(), (Stop)osmStopsComboBox.getSelectedItem());
                        }
                    }
                    // normal selection
                    if(!newWithMatchStopsRadioButton.isSelected() || !isInOsmList) {
                        updateDataWhenStopSelected(selected);
                    }
                }
            }

            public void mousePressed(MouseEvent e){}

            public void mouseReleased(MouseEvent e){}

            public void mouseEntered(MouseEvent e){}

            public void mouseExited(MouseEvent e){}
        });
    }

    private void addMaskOverlay(HashSet<Stop> mStop){
        if(mStop!=null){
            final HashSet<Stop> matchStop = new HashSet<Stop>(mStop);
            matchStopOverlayPainter = new Painter<JXMapViewer>() {
                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                    g = (Graphics2D) g.create();
                    //convert from viewport to world bitmap
                    Rectangle rect = map.getViewportBounds();
                    //                g.translate(-rect.x, -rect.y);

                    JXMapViewer mainMap = mapJXMapKit.getMainMap();
                    Iterator it = matchStop.iterator();
                    while(it.hasNext()){
                        g.setColor(new Color(255,255,0,150));
                        Stop st = (Stop)it.next();
                        GeoPosition st_gp = new GeoPosition(Double.parseDouble(st.getLat()), Double.parseDouble(st.getLon()));
                        //convert to pixel
                        Point2D st_gp_pt2D = mainMap.getTileFactory().geoToPixel(st_gp, mainMap.getZoom());
                        //convert to screen AND left 5, up 5 to have a nice square
                        Point st_gp_pt_screen = new Point((int)st_gp_pt2D.getX()-rect.x-9, (int)st_gp_pt2D.getY()-rect.y-9);

                        //draw mask
                        Rectangle yellow_mask = new Rectangle(st_gp_pt_screen, new Dimension(25,25));
                        g.fill(yellow_mask);
                        g.setColor(Color.BLACK);
                        g.draw(yellow_mask);
                    }
                    g.dispose();
                }
            };
        }
//        mainPainter.setPainters(matchStopOverlayPainter, stopsPainter, selectedGtfsOverlayPainter, selectedOsmOverlayPainter);
//        mainMap.setOverlayPainter(mainPainter);
    }

    private void addSelectedStopsOverlay(Stop gtfsStop, Stop osmStop){
        boolean isOverlapped=false;
        if(gtfsStop!=null && osmStop!=null){
            if(OsmDistance.distVincenty(gtfsStop.getLat(), gtfsStop.getLon(), osmStop.getLat(), osmStop.getLon())<10){
                isOverlapped=true;
            }
        }
        if(gtfsStop!=null && !isOverlapped){
            final Stop tempStop = new Stop(gtfsStop);
            selectedGtfsOverlayPainter = new Painter<JXMapViewer>() {
                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                    g = (Graphics2D) g.create();
                    //convert from viewport to world bitmap
                    Rectangle rect = map.getViewportBounds();
                    //                g.translate(-rect.x, -rect.y);

                    g.setColor(new Color(0,0,127,150));

                    JXMapViewer mainMap = mapJXMapKit.getMainMap();

                    GeoPosition st_gp = new GeoPosition(Double.parseDouble(tempStop.getLat()), Double.parseDouble(tempStop.getLon()));
                    //convert to pixel
                    Point2D st_gp_pt2D = mainMap.getTileFactory().geoToPixel(st_gp, mainMap.getZoom());
                    //convert to screen AND left 5, up 5 to have a nice square
                    Point st_gp_pt_screen = new Point((int)st_gp_pt2D.getX()-rect.x-9, (int)st_gp_pt2D.getY()-rect.y-9);
                    //draw mask
                    Rectangle blue_mask = new Rectangle(st_gp_pt_screen, new Dimension(25,25));
                    g.fill(blue_mask);
                    g.setColor(Color.BLACK);
                    g.draw(blue_mask);
                    g.dispose();
                }
            };
        } else {
            selectedGtfsOverlayPainter = null;
        }

        if(osmStop!=null){
            final Stop tempStop = new Stop(osmStop);
            selectedOsmOverlayPainter = new Painter<JXMapViewer>() {
                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                    g = (Graphics2D) g.create();
                    //convert from viewport to world bitmap
                    Rectangle rect = map.getViewportBounds();
                    //                g.translate(-rect.x, -rect.y);

                    g.setColor(new Color(0,127,0,150));

                    JXMapViewer mainMap = mapJXMapKit.getMainMap();

                    GeoPosition st_gp = new GeoPosition(Double.parseDouble(tempStop.getLat()), Double.parseDouble(tempStop.getLon()));
                    //convert to pixel
                    Point2D st_gp_pt2D = mainMap.getTileFactory().geoToPixel(st_gp, mainMap.getZoom());
                    //convert to screen AND left 5, up 5 to have a nice square
                    Point st_gp_pt_screen = new Point((int)st_gp_pt2D.getX()-rect.x-9, (int)st_gp_pt2D.getY()-rect.y-9);
                    //draw mask
                    Rectangle green_mask = new Rectangle(st_gp_pt_screen, new Dimension(25,25));
                    g.fill(green_mask);
                    g.setColor(Color.BLACK);
                    g.draw(green_mask);
                    g.dispose();
                }
            };
        } else {
            selectedOsmOverlayPainter = null;
        }

        updateMainMap();
    }

    private void updateMainMap(){
        if(selectedGtfsOverlayPainter!=null && selectedOsmOverlayPainter!=null && matchStopOverlayPainter!=null)
            mainPainter.setPainters(matchStopOverlayPainter, selectedGtfsOverlayPainter, selectedOsmOverlayPainter, stopsPainter);
        else if(selectedGtfsOverlayPainter==null && selectedOsmOverlayPainter!=null && matchStopOverlayPainter!=null){
            if(osmStopsComboBox.getItemCount()>1) mainPainter.setPainters(matchStopOverlayPainter, selectedOsmOverlayPainter, stopsPainter);
            else mainPainter.setPainters(matchStopOverlayPainter, stopsPainter);
        }
        else if(selectedGtfsOverlayPainter==null && selectedOsmOverlayPainter==null && matchStopOverlayPainter!=null)
            mainPainter.setPainters(matchStopOverlayPainter, stopsPainter);
        else if(selectedGtfsOverlayPainter!=null && selectedOsmOverlayPainter==null && matchStopOverlayPainter!=null)
             mainPainter.setPainters(matchStopOverlayPainter, selectedGtfsOverlayPainter, stopsPainter);
        else if(selectedGtfsOverlayPainter!=null) mainPainter.setPainters(selectedGtfsOverlayPainter, stopsPainter);
        else mainPainter.setPainters(stopsPainter);
        mainMap.setOverlayPainter(mainPainter);
    }

    private void clearRouteTable(){
        routeTableModel = new TagReportTableModel(0);
        routeTable.setModel(routeTableModel);
        routeTable.getModel().addTableModelListener(this);

        memberTableModel = new RouteMemberTableModel(0);
        memberTable.setModel(memberTableModel);
    }

    private void updateRouteTable(Route selectedNewRoute){
        saveChangeRouteButton.setEnabled(false);

        // get all the possible tag names from gtfs data and osm data
        HashSet<String> tagKeys = new HashSet<String>();
        Hashtable<String, String> aTags = new Hashtable<String, String>();
        Hashtable<String, String> eTags= new Hashtable<String, String>();
        Route aRoute = null, eRoute=null;
        if(selectedNewRoute!=null) {
            tagKeys.addAll(selectedNewRoute.keySet());
            aRoute = (Route)agencyRoutes.get(selectedNewRoute.getRouteId());
            eRoute = (Route)existingRoutes.get(selectedNewRoute.getRouteId());

            if(aRoute!=null) aTags.putAll(aRoute.getTags());
            if(eRoute!=null) eTags.putAll(eRoute.getTags());

            tagKeys.addAll(aTags.keySet());
            tagKeys.addAll(eTags.keySet());
        }
        else return;

        // set new size to the table
        routeTableModel = new TagReportTableModel(tagKeys.size());
        routeTable.setModel(routeTableModel);

        // the same as tagKeys. For the purpose of using for loop
        ArrayList<String> tkeys = new ArrayList<String>();
        tkeys.addAll(tagKeys);

        // add data to table
        // first, add lat and lon
//        Stop finalSt = (Stop)finalStops.get(selectedNewStop.getStopID());
//        ArrayList<Boolean> finalCB = finalCheckboxes.get(selectedNewStop.getStopID());
        for(int i=0; i<tkeys.size(); i++){
            String k = tkeys.get(i);

            //make sure there's null pointer
            String newValue="", osmValue="", gtfsValue="";
            if(selectedNewRoute!=null) {
                newValue = (String)selectedNewRoute.getTag(k);
                gtfsValue = (String)aTags.get(k);
                osmValue = (String)eTags.get(k);
            }

            //add tag to table
            routeTableModel.setRowValueAt(new Object[] {k, gtfsValue, true, osmValue, false, newValue}, i);
        }

        //set the column width with checkbox to minimum size
        routeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn col = routeTable.getColumnModel().getColumn(2);
        int width = 15;
        col.setPreferredWidth(width);
        col = routeTable.getColumnModel().getColumn(4);
        col.setPreferredWidth(width);
        col = routeTable.getColumnModel().getColumn(0);
        col.setPreferredWidth(100);
        col = routeTable.getColumnModel().getColumn(1);
        col.setPreferredWidth(114);
        col = routeTable.getColumnModel().getColumn(3);
        col.setPreferredWidth(114);
        col = routeTable.getColumnModel().getColumn(5);
        col.setPreferredWidth(114);

        // detect any changes in table.
        // Must be placed after all data has been inserted inside the table. Otherwise, saveChangeButton is enabled
        routeTable.getModel().addTableModelListener(this);

        allMembersRadioButton.setSelected(true);
        updateMemberList(selectedNewRoute, "all");
    }

    private void updateMemberList(Route selectedNewRoute, String criteria){
        Route aRoute = null, eRoute=null;
        if(selectedNewRoute!=null) {
            aRoute = (Route)agencyRoutes.get(selectedNewRoute.getRouteId());
            eRoute = (Route)existingRoutes.get(selectedNewRoute.getRouteId());
        }
        else return;
        
        // Member Table
        ArrayList<RelationMember> newMembers = new ArrayList<RelationMember>();
        newMembers.addAll(selectedNewRoute.getOsmMembers());

        ArrayList<RelationMember> gtfsMembers = new ArrayList<RelationMember>();
        if(aRoute!=null) gtfsMembers.addAll(aRoute.getOsmMembers());

        ArrayList<RelationMember> osmMembers = new ArrayList<RelationMember>();
        if(eRoute!=null) osmMembers.addAll(eRoute.getOsmMembers());

        // set new size to the table
        memberTableModel = new RouteMemberTableModel(newMembers.size());
        memberTable.setModel(memberTableModel);

        // create map
        // hashGtfs stores all the relation members with the key of the gtfs id.
        // if any relation member does not have gtfs id, the type would be taken as the key
        Hashtable<RelationMember, String> hashGtfs = new Hashtable<RelationMember, String>();
        for(int i=0; i<gtfsMembers.size(); i++){
            RelationMember t = gtfsMembers.get(i);
            String v = t.getGtfsId();
            if (v!=null && !v.equals("none") && !v.equals("")) hashGtfs.put(t, v);
//            else hashGtfs.put(t, t.getType() + " (" + t.getRef()+")");
        }

        Hashtable<RelationMember, String> hashOsm = new Hashtable<RelationMember, String>();
        for(int i=0; i<osmMembers.size(); i++){
            RelationMember t = osmMembers.get(i);
            String v = t.getGtfsId();
            if (v!=null && !v.equals("none") && !v.equals("")) hashOsm.put(t, v);
//            else hashOsm.put(t, t.getType() + " (" + t.getRef()+")");
        }

        int memberNewIndex = 0;
        int memberGtfsIndex = 0, memberOsmIndex = 0;
        for(int i=0; i<newMembers.size(); i++){
            RelationMember t = newMembers.get(i);
            String status = t.getStatus();
            if(status.equals(criteria) || criteria.equals("all")) {
                String v = t.getGtfsId();
//                if (v==null || v.equals("none") || v.equals("")) v = t.getType() + " (" + t.getRef() +")";

                if(hashGtfs.get(t)!=null) memberGtfsIndex++;
                if(hashOsm.get(t)!=null) memberOsmIndex++;
                
                if (v!=null && !v.equals("none") && !v.equals("")) {
                    memberTableModel.setRowValueAt(new Object[] {hashGtfs.get(t), hashOsm.get(t), v}, memberNewIndex);
                    memberNewIndex++;
                }
            }
        }
        totalGtfsMembersLabel.setText(Integer.toString(memberGtfsIndex));
        totalOsmMembersLabel.setText(Integer.toString(memberOsmIndex));
        totalNewMembersLabel.setText(Integer.toString(memberNewIndex));
    }

    private void updateRouteCategory(Route[] selectedCategory){
        gtfsRoutes = selectedCategory;
        gtfsRoutesComboBox.setModel(new DefaultComboBoxModel(gtfsRoutes));
        totalGtfsRoutesLabel.setText(Integer.toString(gtfsRoutes.length));
        if(gtfsRoutes.length!=0) updateBusRoute(gtfsRoutes[0]);
        else updateBusRoute(null);
    }

    // When user click on gtfs combobox
    private void updateBusRoute(Route rt){   
        if (rt!=null) {
            updateRouteTable(rt);
        } else {
            clearRouteTable();
        }
    }

    private void updateButtonTableStop(String uploadConflictCategoryText, boolean uploadConflictStatus, String otherCategoryText, boolean otherStatus){
        Stop selectedGtfsStop = (Stop)gtfsStopsComboBox.getSelectedItem();

        String category = selectedGtfsStop.getReportCategory();
        if((category.equals("UPLOAD_CONFLICT") || category.equals("MODIFY")) && (stopsToFinish.contains(selectedGtfsStop.toString()))) {
            tableStopButton.setText(uploadConflictCategoryText);
            tableStopButton.setEnabled(uploadConflictStatus);
        } else {
            tableStopButton.setText(otherCategoryText);
            tableStopButton.setEnabled(otherStatus);
        }

        if(category.equals("NO_UPLOAD")){
            donotUploadButton.setVisible(false);
        } else {
            donotUploadButton.setVisible(true);
        }
    }


    public void tableChanged(TableModelEvent e){
        TableModel model = (TableModel)e.getSource();
        int row = e.getFirstRow();
        int column = e.getColumn();
        Object data = model.getValueAt(row, column);
        if(model.equals(stopTableModel) && (data instanceof Boolean)){
            if(!tableStopButton.isEnabled() || tableStopButton.getText().equals("Accept")){
//                Stop selectedGtfsStop = (Stop)gtfsStopsComboBox.getSelectedItem();
                updateButtonTableStop("Accept & Save Change", true, "Save Change", true);
            }
        }
        /*
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
        System.out.println(columnName+" "+data.toString());*/
    }

    public void updateDataWhenStopSelected(Stop selected){
        String category = selected.getReportCategory();
        if (category.equals("UPLOAD_CONFLICT")) {
            newWithMatchStopsRadioButton.setSelected(true);
            updateStopCategory(gtfsUploadConflict, 0);
        } else if (category.equals("UPLOAD_NO_CONFLICT")) {
            newNoMatchStopsRadioButton.setSelected(true);
            updateStopCategory(gtfsUploadNoConflict, 0);
        } else if (category.equals("MODIFY")) {
            updateStopsRadioButton.setSelected(true);
            updateStopCategory(gtfsModify, 0);
        } else if (category.equals("NOTHING_NEW")) {
            existingStopRadioButton.setSelected(true);
            updateStopCategory(gtfsNoUpload, 0);
        }
        updateBusStop(selected);
        gtfsStopsComboBox.setSelectedItem(selected);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("progress")) {
            if(taskUpload!=null){
                progressBar.setVisible(true);
                int progress = (Integer) evt.getNewValue();
                progressBar.setValue(progress);
                generalInformationStopTextArea.append(taskUpload.getMessage()+"\n");
                /*
                if(taskUpload.getMessage().contains("several minutes")){
                    progressBar.setIndeterminate(true);
                }*/
            }
        }
    }

    private void generateStopsToUpload(Hashtable<String, Stop> stops){
        if(generateStopsToUploadFlag) return;
        generateStopsToUploadFlag = true;
        
        if(stops == null){
            JOptionPane.showMessageDialog(this, "There's no stops to upload");
            return;
        }

        upload = new HashSet<Stop>();
        modify = new HashSet<Stop>();
//        delete = new HashSet<Stop>();

        ArrayList<String> stopIds = new ArrayList<String>(stops.keySet());

        for(int i=0; i<stopIds.size(); i++){
            Stop s = stops.get(stopIds.get(i));
            String category = s.getReportCategory();
            if(category.equals("UPLOAD_NO_CONFLICT")){
                upload.add(s);
            } else if(category.equals("UPLOAD_CONFLICT")){
                // upload the new stop
                upload.add(s);
                // add FIXME to its potential matches
                Object o = report.get(s);
                if(o instanceof ArrayList){
                    HashSet<Stop> equiv = new HashSet<Stop>((ArrayList<Stop>)o);
                    modify.addAll(equiv);
                }
            } else if(category.equals("MODIFY")){
                // if s is already in modify set, meaning GO-Sync added FIXME tag for the UPLOAD_CONFLICT category
                // then, remove the stop and add FIXME tag to the current s
                if(modify.contains(s)) {
                    modify.remove(s);
                    s.addTag("FIXME", "This stop could be redundant");
                }
                modify.add(s);
            }
        }
    }

    public void AddGeneralInformationToStopTextArea(String s){
        generalInformationStopTextArea.append(s);
    }

    public String GetGeneralInformationToStopTextArea(){
        return generalInformationStopTextArea.getText();
    }

    public void SetGeneralInformationToStopTextArea(String s){
        generalInformationStopTextArea.setText(s);
    }

    public void AddGeneralInformationToRouteTextArea(String s){
        generalInformationRouteTextArea.append(s);
    }

    public String GetGeneralInformationToRouteTextArea(){
        return generalInformationRouteTextArea.getText();
    }

    public void SetGeneralInformationToRouteTextArea(String s){
        generalInformationRouteTextArea.setText(s);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

    	
    	setResizable(true); //false);
        stopsButtonGroup = new javax.swing.ButtonGroup();
        routesButtonGroup = new javax.swing.ButtonGroup();
        membersButtonGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        donotUploadButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        dataTable = new JTable(){
            public String getToolTipText(MouseEvent e){
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                int realRowIndex = convertRowIndexToModel(rowIndex);

                TableModel model = this.getModel();
                if((model instanceof TagReportTableModel) && (realRowIndex>=0) && (realColumnIndex>=0)){
                    Object o = model.getValueAt(realRowIndex, realColumnIndex);
                    if(o instanceof String) tip = (String)o;
                }
                return tip;//"<html>This is the first line<br>This is the second line</html>";
            }

            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = 
                        columnModel.getColumn(index).getModelIndex();
                        return tagReportColumnHeaderToolTips[realIndex];
                    }
                };
            }
        };
        dataTable.setDefaultRenderer(Object.class, new edu.usf.cutr.go_sync.gui.object.TagReportTableCellRenderer());
        dataTable.addMouseListener(new BooleanMouseListener(dataTable));
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        gtfsStopsComboBox = new javax.swing.JComboBox(gtfsStops);
        osmStopsComboBox = new javax.swing.JComboBox(osmStops);
        mapJXMapKit = new org.jdesktop.swingx.JXMapKit();
        final int osmMaxZoom = 19;
        TileFactoryInfo osmInfo = new TileFactoryInfo(1,osmMaxZoom-2,osmMaxZoom,
            256, true, true, // tile size is 256 and x/y orientation is normal
            "http://tile.openstreetmap.org",//5/15/10.png",
            "x","y","z") {
            public String getTileUrl(int x, int y, int zoom) {
                zoom = osmMaxZoom-zoom;
                String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
                return url;
            }
        };
        osmTf = new DefaultTileFactory(osmInfo);
        mapJXMapKit.setTileFactory(osmTf);
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        totalGtfsStopsLabel = new javax.swing.JLabel();
        totalOsmStopsLabel = new javax.swing.JLabel();
        allStopsRadioButton = new javax.swing.JRadioButton();
        newWithMatchStopsRadioButton = new javax.swing.JRadioButton();
        newNoMatchStopsRadioButton = new javax.swing.JRadioButton();
        updateStopsRadioButton = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        existingStopRadioButton = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        generalInformationStopTextArea = new javax.swing.JTextArea();
        generalInformationStopTextArea.setLineWrap(true);
        generalInformationStopTextArea.setWrapStyleWord(true);
        tableStopButton = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        searchButton = new javax.swing.JButton();
        searchTextField = new javax.swing.JTextField();
        finishProgressBar = new javax.swing.JProgressBar();
        jLabel19 = new javax.swing.JLabel();
        lastEditedLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        gtfsRoutesComboBox = new javax.swing.JComboBox(gtfsStops);
        jLabel8 = new javax.swing.JLabel();
        totalGtfsRoutesLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        allRoutesRadioButton = new javax.swing.JRadioButton();
        newRoutesRadioButton = new javax.swing.JRadioButton();
        existingRoutesWithUpdatesRadioButton = new javax.swing.JRadioButton();
        existingRoutesRadioButton = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        generalInformationRouteTextArea = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        routeTable = new JTable(){
            public String getToolTipText(MouseEvent e){
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);
                int realRowIndex = convertRowIndexToModel(rowIndex);

                TableModel model = this.getModel();
                if((model instanceof TagReportTableModel) && (realRowIndex>=0) && (realColumnIndex>=0)){
                    Object o = model.getValueAt(realRowIndex, realColumnIndex);
                    if(o instanceof String) tip = (String)o;
                }
                return tip;//"<html>This is the first line<br>This is the second line</html>";
            }

            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = 
                        columnModel.getColumn(index).getModelIndex();
                        return tagReportColumnHeaderToolTips[realIndex];
                    }
                };
            }
        };
        routeTable.setDefaultRenderer(Object.class, new edu.usf.cutr.go_sync.gui.object.TagReportTableCellRenderer());
 dataTable.addMouseListener(new BooleanMouseListener(dataTable));
        jScrollPane5 = new javax.swing.JScrollPane();
        memberTable = new javax.swing.JTable();
        memberTable.setDefaultRenderer(Object.class, new edu.usf.cutr.go_sync.gui.object.RouteMemberTableCellRenderer());
        jLabel11 = new javax.swing.JLabel();
        allMembersRadioButton = new javax.swing.JRadioButton();
        osmMembersRadioButton = new javax.swing.JRadioButton();
        gtfsMembersRadioButton = new javax.swing.JRadioButton();
        bothMembersRadioButton = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        totalGtfsMembersLabel = new javax.swing.JLabel();
        totalOsmMembersLabel = new javax.swing.JLabel();
        totalNewMembersLabel = new javax.swing.JLabel();
        saveChangeRouteButton = new javax.swing.JButton();
        dummyUploadButton = new javax.swing.JButton();
        uploadDataButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        exportGtfsValueGtfsDataOnlyMenuItem = new javax.swing.JMenuItem();
        exportGtfsValueWithOsmTagsMenuItem = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        exportOsmValueGtfsDataOnlyMenuItem = new javax.swing.JMenuItem();
        exportOsmValueWithOsmTagsMenuItem = new javax.swing.JMenuItem();
        exportOsmValueStopsWithConflictsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setFont(new java.awt.Font("Tahoma", 0, 14));
        jPanel1.setMaximumSize(new java.awt.Dimension(780, 780));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(750, 617));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        donotUploadButton.setFont(new java.awt.Font("Tahoma", 0, 12));
        donotUploadButton.setText("Don't Upload");
        donotUploadButton.setName("donotUploadButton"); // NOI18N
        donotUploadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                donotUploadButtonActionPerformed(evt);
            }
        });
        jPanel1.add(donotUploadButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 310, 110, -1));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        dataTable.setFont(new java.awt.Font("Times New Roman", 0, 14));
        dataTable.setModel(stopTableModel);
        dataTable.setName("dataTable"); // NOI18N
        dataTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        dataTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(dataTable);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(232, 111, 486, 172));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel1.setText("GTFS Stops");
        jLabel1.setName("jLabel1"); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, -1, 20));

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel2.setText("OSM Stops");
        jLabel2.setName("jLabel2"); // NOI18N
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 20, -1, 20));

        gtfsStopsComboBox.setFont(new java.awt.Font("Times New Roman", 1, 14));
        gtfsStopsComboBox.setMinimumSize(new java.awt.Dimension(60, 20));
        gtfsStopsComboBox.setName("gtfsStopsComboBox"); // NOI18N
        gtfsStopsComboBox.setPreferredSize(new java.awt.Dimension(60, 20));
        gtfsStopsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gtfsStopsComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(gtfsStopsComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(336, 49, -1, 23));

        osmStopsComboBox.setFont(new java.awt.Font("Times New Roman", 1, 14));
        osmStopsComboBox.setMinimumSize(new java.awt.Dimension(60, 20));
        osmStopsComboBox.setName("osmStopsComboBox"); // NOI18N
        osmStopsComboBox.setPreferredSize(new java.awt.Dimension(60, 20));
        osmStopsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                osmStopsComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(osmStopsComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 50, -1, 23));

        mapJXMapKit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mapJXMapKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.Custom);
        mapJXMapKit.setFont(new java.awt.Font("Tahoma", 0, 14));
        mapJXMapKit.setName("mapJXMapKit"); // NOI18N
        mapJXMapKit.setScrollableTracksViewportWidth(false);
        jPanel1.add(mapJXMapKit, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 385, 488, 260));

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel3.setText("Display on Map:");
        jLabel3.setName("jLabel3"); // NOI18N
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 350, -1, -1));

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel7.setText("Total Stops:");
        jLabel7.setName("jLabel7"); // NOI18N
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(211, 78, -1, -1));

        totalGtfsStopsLabel.setFont(new java.awt.Font("Times New Roman", 0, 14));
        totalGtfsStopsLabel.setText("N/A"); // NOI18N
        totalGtfsStopsLabel.setName("totalGtfsStopsLabel"); // NOI18N
        jPanel1.add(totalGtfsStopsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(351, 82, -1, -1));

        totalOsmStopsLabel.setFont(new java.awt.Font("Times New Roman", 0, 14));
        totalOsmStopsLabel.setText("N/A");
        totalOsmStopsLabel.setName("totalOsmStopsLabel"); // NOI18N
        jPanel1.add(totalOsmStopsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 80, -1, -1));

        stopsButtonGroup.add(allStopsRadioButton);
        allStopsRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        allStopsRadioButton.setSelected(true);
        allStopsRadioButton.setText("All");
        allStopsRadioButton.setToolTipText("All GTFS stops from travel agency"); // NOI18N
        allStopsRadioButton.setName("allStopsRadioButton"); // NOI18N
        allStopsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allStopsRadioButtonActionPerformed(evt);
            }
        });
        jPanel1.add(allStopsRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 217, -1, -1));

        stopsButtonGroup.add(newWithMatchStopsRadioButton);
        newWithMatchStopsRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        newWithMatchStopsRadioButton.setText("New GTFS stops with"); // NOI18N
        newWithMatchStopsRadioButton.setToolTipText("<html>\nNew GTFS stops to be added to OpenStreetMap.<br>\nHowever, there are some existing stops in OSM within 400 meters that could be this GTFS stop.<br>\nPlease verify if the stop is already in OSM by clicking the Match button.<br>\nOtherwise, these GTFS stops would be uploaded with a FIXME tag.\n</html>"); // NOI18N
        newWithMatchStopsRadioButton.setName("newWithMatchStopsRadioButton"); // NOI18N
        newWithMatchStopsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newWithMatchStopsRadioButtonActionPerformed(evt);
            }
        });
        jPanel1.add(newWithMatchStopsRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, -1, 20));

        stopsButtonGroup.add(newNoMatchStopsRadioButton);
        newNoMatchStopsRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        newNoMatchStopsRadioButton.setText("New GTFS stops with");
        newNoMatchStopsRadioButton.setToolTipText("New GTFS stops to be added to OpenStreetMap."); // NOI18N
        newNoMatchStopsRadioButton.setName("newNoMatchStopsRadioButton"); // NOI18N
        newNoMatchStopsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newNoMatchStopsRadioButtonActionPerformed(evt);
            }
        });
        jPanel1.add(newNoMatchStopsRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 183, 20));

        stopsButtonGroup.add(updateStopsRadioButton);
        updateStopsRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        updateStopsRadioButton.setText("Existing stops with Updates");
        updateStopsRadioButton.setToolTipText("<html>\nThese GTFS stops are already there in OpenStreetMap.<br>\nHowever, some tags are different.\n</html>"); // NOI18N
        updateStopsRadioButton.setName("updateStopsRadioButton"); // NOI18N
        updateStopsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateStopsRadioButtonActionPerformed(evt);
            }
        });
        jPanel1.add(updateStopsRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, -1, -1));

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel4.setText("Stops to view:");
        jLabel4.setName("jLabel4"); // NOI18N
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(29, 54, -1, -1));

        stopsButtonGroup.add(existingStopRadioButton);
        existingStopRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        existingStopRadioButton.setText("Existing stops");
        existingStopRadioButton.setToolTipText("<html>\nThese GTFS stops are already in OpenStreetMap.<br>\nNo new information is added.\n</html>"); // NOI18N
        existingStopRadioButton.setName("existingStopRadioButton"); // NOI18N
        existingStopRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingStopRadioButtonActionPerformed(evt);
            }
        });
        jPanel1.add(existingStopRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, -1, -1));

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel5.setText("General Information");
        jLabel5.setName("jLabel5"); // NOI18N
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 350, -1, -1));

        jLabel13.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel13.setText("No OSM Matches");
        jLabel13.setToolTipText("New GTFS stops to be added to OpenStreetMap."); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, -1, -1));

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        generalInformationStopTextArea.setColumns(20);
        generalInformationStopTextArea.setEditable(false);
        generalInformationStopTextArea.setRows(5);
        generalInformationStopTextArea.setWrapStyleWord(true);
        generalInformationStopTextArea.setName("generalInformationStopTextArea"); // NOI18N
        jScrollPane2.setViewportView(generalInformationStopTextArea);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 385, 216, 260));

        tableStopButton.setFont(new java.awt.Font("Tahoma", 0, 12));
        tableStopButton.setText("setTextByCode");
        tableStopButton.setName("tableStopButton"); // NOI18N
        tableStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableStopButtonActionPerformed(evt);
            }
        });
        jPanel1.add(tableStopButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 310, 180, -1));

        jLabel14.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel14.setText("Potential Matches in OSM");
        jLabel14.setToolTipText("<html>\nNew GTFS stops to be added to OpenStreetMap.<br>\nHowever, there are some existing stops in OSM within 400 meters that could be this GTFS stop.<br>\nPlease verify if the stop is already in OSM by clicking the Match button.<br>\nOtherwise, these GTFS stops would be uploaded with a FIXME tag.\n</html>"); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/usf/cutr/go_sync/gui/yellow.png"))); // NOI18N
        jLabel15.setText("Potential Match Stops");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setOpaque(true);
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 330, -1, 30));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/usf/cutr/go_sync/gui/green.png"))); // NOI18N
        jLabel16.setText("Selected Osm Stop");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setOpaque(true);
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 360, -1, -1));

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/usf/cutr/go_sync/gui/blue.png"))); // NOI18N
        jLabel17.setText("Selected Gtfs Stop");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setOpaque(true);
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 360, -1, -1));

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/usf/cutr/go_sync/gui/bus_icon.png"))); // NOI18N
        jLabel18.setText("New Stop");
        jLabel18.setName("jLabel18"); // NOI18N
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 330, 70, 30));

        searchButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        searchButton.setText("Search");
        searchButton.setIconTextGap(2);
        searchButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        searchButton.setName("searchButton"); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        jPanel1.add(searchButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 50, -1, -1));

        searchTextField.setToolTipText("Input stop name or stop id to search for stop");
        searchTextField.setName("searchTextField"); // NOI18N
        jPanel1.add(searchTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 50, 90, 20));

        finishProgressBar.setToolTipText("<html>\nStops that you should visit before uploading into OSM<br>\nThese stops are either new stops that GO_Sync can't find a match for in OSM or existing stops in OSM that have new data to be inserted.\n</html>"); // NOI18N
        finishProgressBar.setName("finishProgressBar"); // NOI18N
        finishProgressBar.setString("0"); // NOI18N
        finishProgressBar.setStringPainted(true);
        jPanel1.add(finishProgressBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 120, 20));

        jLabel19.setFont(new java.awt.Font("Times New Roman", 0, 14));
        jLabel19.setText("Finish");
        jLabel19.setName("jLabel19"); // NOI18N
        jPanel1.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        lastEditedLabel.setFont(new java.awt.Font("Times New Roman", 0, 14));
        lastEditedLabel.setText("N/A");
        lastEditedLabel.setName("lastEditedLabel"); // NOI18N
        jPanel1.add(lastEditedLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 290, 330, -1));

        jTabbedPane1.addTab("Bus Stop", jPanel1);

        jPanel2.setFont(new java.awt.Font("Tahoma", 0, 14));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel6.setText("GTFS Routes");
        jLabel6.setName("jLabel6"); // NOI18N
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(318, 23, -1, 14));

        gtfsRoutesComboBox.setFont(new java.awt.Font("Times New Roman", 1, 14));
        gtfsRoutesComboBox.setMinimumSize(new java.awt.Dimension(60, 20));
        gtfsRoutesComboBox.setName("gtfsRoutesComboBox"); // NOI18N
        gtfsRoutesComboBox.setPreferredSize(new java.awt.Dimension(60, 20));
        gtfsRoutesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gtfsRoutesComboBoxActionPerformed(evt);
            }
        });
        jPanel2.add(gtfsRoutesComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(338, 47, -1, 23));

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel8.setText("Total Stops:");
        jLabel8.setName("jLabel8"); // NOI18N
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 19, -1, -1));

        totalGtfsRoutesLabel.setFont(new java.awt.Font("Times New Roman", 0, 14));
        totalGtfsRoutesLabel.setText("N/A"); // NOI18N
        totalGtfsRoutesLabel.setName("totalGtfsRoutesLabel"); // NOI18N
        jPanel2.add(totalGtfsRoutesLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(469, 50, -1, -1));

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel9.setText("Routes to view:");
        jLabel9.setName("jLabel9"); // NOI18N
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 48, -1, -1));

        routesButtonGroup.add(allRoutesRadioButton);
        allRoutesRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        allRoutesRadioButton.setSelected(true);
        allRoutesRadioButton.setText("All");
        allRoutesRadioButton.setName("allRoutesRadioButton"); // NOI18N
        allRoutesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allRoutesRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(allRoutesRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, -1, -1));

        routesButtonGroup.add(newRoutesRadioButton);
        newRoutesRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        newRoutesRadioButton.setText("New GTFS routes"); // NOI18N
        newRoutesRadioButton.setName("newRoutesRadioButton"); // NOI18N
        newRoutesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newRoutesRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(newRoutesRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 187, -1));

        routesButtonGroup.add(existingRoutesWithUpdatesRadioButton);
        existingRoutesWithUpdatesRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        existingRoutesWithUpdatesRadioButton.setText("Existing routes with Updates");
        existingRoutesWithUpdatesRadioButton.setName("existingRoutesWithUpdatesRadioButton"); // NOI18N
        existingRoutesWithUpdatesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingRoutesWithUpdatesRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(existingRoutesWithUpdatesRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, -1));

        routesButtonGroup.add(existingRoutesRadioButton);
        existingRoutesRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        existingRoutesRadioButton.setText("Existing routes");
        existingRoutesRadioButton.setName("existingRoutesRadioButton"); // NOI18N
        existingRoutesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingRoutesRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(existingRoutesRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, -1, -1));

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel10.setText("General Information");
        jLabel10.setName("jLabel10"); // NOI18N
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 340, -1, -1));

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        generalInformationRouteTextArea.setColumns(20);
        generalInformationRouteTextArea.setLineWrap(true);
        generalInformationRouteTextArea.setRows(5);
        generalInformationRouteTextArea.setWrapStyleWord(true);
        generalInformationRouteTextArea.setName("generalInformationRouteTextArea"); // NOI18N
        jScrollPane3.setViewportView(generalInformationRouteTextArea);

        jPanel2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 216, 251));

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        routeTable.setFont(new java.awt.Font("Times New Roman", 0, 14));
        routeTable.setModel(routeTableModel);
        routeTable.setName("routeTable"); // NOI18N
        routeTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        routeTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(routeTable);

        jPanel2.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(241, 82, 486, 173));

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        memberTable.setFont(new java.awt.Font("Times New Roman", 0, 14));
        memberTable.setModel(stopTableModel);
        memberTable.setName("memberTable"); // NOI18N
        memberTable.setSelectionForeground(new java.awt.Color(0, 0, 0));
        memberTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(memberTable);

        jPanel2.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 390, 486, 223));

        jLabel11.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel11.setText("Members to view:");
        jLabel11.setName("jLabel11"); // NOI18N
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 340, -1, -1));

        membersButtonGroup.add(allMembersRadioButton);
        allMembersRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        allMembersRadioButton.setSelected(true);
        allMembersRadioButton.setText("All");
        allMembersRadioButton.setName("allMembersRadioButton"); // NOI18N
        allMembersRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allMembersRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(allMembersRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 320, -1, -1));

        membersButtonGroup.add(osmMembersRadioButton);
        osmMembersRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        osmMembersRadioButton.setText("From OSM only"); // NOI18N
        osmMembersRadioButton.setName("osmMembersRadioButton"); // NOI18N
        osmMembersRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                osmMembersRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(osmMembersRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 350, 160, -1));

        membersButtonGroup.add(gtfsMembersRadioButton);
        gtfsMembersRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        gtfsMembersRadioButton.setText("From GTFS only");
        gtfsMembersRadioButton.setName("gtfsMembersRadioButton"); // NOI18N
        gtfsMembersRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gtfsMembersRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(gtfsMembersRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 320, -1, -1));

        membersButtonGroup.add(bothMembersRadioButton);
        bothMembersRadioButton.setFont(new java.awt.Font("Times New Roman", 0, 14));
        bothMembersRadioButton.setText("From both dataset");
        bothMembersRadioButton.setName("bothMembersRadioButton"); // NOI18N
        bothMembersRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bothMembersRadioButtonActionPerformed(evt);
            }
        });
        jPanel2.add(bothMembersRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 350, -1, -1));

        jLabel12.setFont(new java.awt.Font("Times New Roman", 1, 18));
        jLabel12.setText("Total:");
        jLabel12.setName("jLabel12"); // NOI18N
        jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 620, -1, -1));

        totalGtfsMembersLabel.setFont(new java.awt.Font("Times New Roman", 0, 14));
        totalGtfsMembersLabel.setText("N/A"); // NOI18N
        totalGtfsMembersLabel.setName("totalGtfsMembersLabel"); // NOI18N
        jPanel2.add(totalGtfsMembersLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 620, -1, 20));

        totalOsmMembersLabel.setFont(new java.awt.Font("Times New Roman", 0, 14));
        totalOsmMembersLabel.setText("N/A"); // NOI18N
        totalOsmMembersLabel.setName("totalOsmMembersLabel"); // NOI18N
        jPanel2.add(totalOsmMembersLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 620, -1, 20));

        totalNewMembersLabel.setFont(new java.awt.Font("Times New Roman", 0, 14));
        totalNewMembersLabel.setText("N/A"); // NOI18N
        totalNewMembersLabel.setName("totalNewMembersLabel"); // NOI18N
        jPanel2.add(totalNewMembersLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 620, -1, -1));

        saveChangeRouteButton.setText("Save Change");
        saveChangeRouteButton.setEnabled(false);
        saveChangeRouteButton.setName("saveChangeRouteButton"); // NOI18N
        saveChangeRouteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveChangeRouteButtonActionPerformed(evt);
            }
        });
        jPanel2.add(saveChangeRouteButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 261, -1, -1));

        jTabbedPane1.addTab("Bus Route", jPanel2);

        dummyUploadButton.setFont(new java.awt.Font("Tahoma", 0, 14));
        dummyUploadButton.setText("Dummy Upload");
        dummyUploadButton.setName("dummyUploadButton"); // NOI18N
        dummyUploadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dummyUploadButtonActionPerformed(evt);
            }
        });

        uploadDataButton.setFont(new java.awt.Font("Tahoma", 0, 14));
        uploadDataButton.setText("Upload Data To OSM");
        uploadDataButton.setName("uploadDataButton"); // NOI18N
        uploadDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadDataButtonActionPerformed(evt);
            }
        });

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText("File");
        jMenu1.setName("jMenu1"); // NOI18N

        jMenu3.setText("Export Stops");
        jMenu3.setName("jMenu3"); // NOI18N

        jMenu2.setText("GTFS Values by Default");
        jMenu2.setName("jMenu2"); // NOI18N

        exportGtfsValueGtfsDataOnlyMenuItem.setText("GTFS data only");
        exportGtfsValueGtfsDataOnlyMenuItem.setName("exportGtfsValueGtfsDataOnlyMenuItem"); // NOI18N
        exportGtfsValueGtfsDataOnlyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGtfsValueGtfsDataOnlyMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(exportGtfsValueGtfsDataOnlyMenuItem);

        exportGtfsValueWithOsmTagsMenuItem.setText("With OSM tags");
        exportGtfsValueWithOsmTagsMenuItem.setName("exportGtfsValueWithOsmTagsMenuItem"); // NOI18N
        exportGtfsValueWithOsmTagsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGtfsValueWithOsmTagsMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(exportGtfsValueWithOsmTagsMenuItem);

        jMenu3.add(jMenu2);

        jMenu4.setText("OSM Values by Default");
        jMenu4.setName("jMenu4"); // NOI18N

        exportOsmValueGtfsDataOnlyMenuItem.setText("GTFS data only");
        exportOsmValueGtfsDataOnlyMenuItem.setName("exportOsmValueGtfsDataOnlyMenuItem"); // NOI18N
        exportOsmValueGtfsDataOnlyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOsmValueGtfsDataOnlyMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(exportOsmValueGtfsDataOnlyMenuItem);

        exportOsmValueWithOsmTagsMenuItem.setText("With OSM tags");
        exportOsmValueWithOsmTagsMenuItem.setName("exportOsmValueWithOsmTagsMenuItem"); // NOI18N
        exportOsmValueWithOsmTagsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOsmValueWithOsmTagsMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(exportOsmValueWithOsmTagsMenuItem);

        exportOsmValueStopsWithConflictsMenuItem.setText("Only Stops with Conflicts");
        exportOsmValueStopsWithConflictsMenuItem.setName("exportOsmValueStopsWithConflictsMenuItem"); // NOI18N
        exportOsmValueStopsWithConflictsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportOsmValueStopsWithConflictsMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(exportOsmValueStopsWithConflictsMenuItem);

        jMenu3.add(jMenu4);

        jMenu1.add(jMenu3);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(226, Short.MAX_VALUE)
                .addComponent(dummyUploadButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(uploadDataButton)
                .addGap(218, 218, 218))
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 676, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uploadDataButton)
                    .addComponent(dummyUploadButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private Stop[] removeOneStopFromArray(Stop[] arrayStops, Stop s){
        Stop[] gtfsTemp = new Stop[arrayStops.length-1];
        int i = 0;
        while(!arrayStops[i].equals(s)){
            gtfsTemp[i] = arrayStops[i];
            i++;
        }
        while(i<gtfsTemp.length) {
            gtfsTemp[i] = arrayStops[i+1];
            i++;
        }
        // set the temporary array to its original reference
        arrayStops = new Stop[gtfsTemp.length];
        for(i=0; i<gtfsTemp.length; i++) arrayStops[i] = gtfsTemp[i];
        return arrayStops;
    }

    private void donotUploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_donotUploadButtonActionPerformed
        // TODO add your handling code here:
        Stop s = (Stop)gtfsStopsComboBox.getSelectedItem();

        if(s==null) return;

        int index = gtfsStopsComboBox.getSelectedIndex();

        String sid = s.getStopID();
        String category = s.getReportCategory();
        if(category.equals("UPLOAD_CONFLICT")){
            gtfsUploadConflict = removeOneStopFromArray(gtfsUploadConflict, s);
            updateStopCategory(gtfsUploadConflict, index);
        } else if(category.equals("UPLOAD_NO_CONFLICT")) {
            gtfsUploadNoConflict = removeOneStopFromArray(gtfsUploadNoConflict, s);
            updateStopCategory(gtfsUploadNoConflict, index);
        } else if(category.equals("MODIFY")){
            gtfsModify = removeOneStopFromArray(gtfsModify, s);
            updateStopCategory(gtfsModify, index);
        }
        gtfsAll = removeOneStopFromArray(gtfsAll, s);

        finalStops.remove(sid);
        osmDefaultFinalStops.remove(sid);
        osmDefaultOnlyChangedFinalStops.remove(sid);
        finalCheckboxes.remove(sid);
}//GEN-LAST:event_donotUploadButtonActionPerformed

    private void gtfsStopsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gtfsStopsComboBoxActionPerformed
        // TODO add your handling code here:
        updateBusStop((Stop)gtfsStopsComboBox.getSelectedItem());
    }//GEN-LAST:event_gtfsStopsComboBoxActionPerformed

    private void uploadDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadDataButtonActionPerformed
        // TODO add your handling code here:
        //new Session(_username, _password, _changesetComment);

        OSMSessionForm osmLogin = new OSMSessionForm();
        if (!osmLogin.showDialog()) //if user hit cancel and didn't enter OSM credentials
        {
            JOptionPane.showMessageDialog(this, "To edit OSM data, you must log in to OSM.");
            return;
        }
        
        generateStopsToUpload(finalStops);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        uploadDataButton.setEnabled(false);
//        progressBar.setVisible(true);
//        progressBar.setIndeterminate(false);
        taskUpload = new UploadData(osmRequest, upload, modify, delete, finalRoutes);
        taskUpload.addPropertyChangeListener(this);
        taskUpload.execute();
}//GEN-LAST:event_uploadDataButtonActionPerformed

    private void newWithMatchStopsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newWithMatchStopsRadioButtonActionPerformed
        // TODO add your handling code here:
        updateStopCategory(gtfsUploadConflict, 0);
    }//GEN-LAST:event_newWithMatchStopsRadioButtonActionPerformed

    private void newNoMatchStopsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newNoMatchStopsRadioButtonActionPerformed
        // TODO add your handling code here:
        updateStopCategory(gtfsUploadNoConflict, 0);
    }//GEN-LAST:event_newNoMatchStopsRadioButtonActionPerformed

    private void updateStopsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateStopsRadioButtonActionPerformed
        // TODO add your handling code here:
        updateStopCategory(gtfsModify, 0);
    }//GEN-LAST:event_updateStopsRadioButtonActionPerformed

    private void existingStopRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingStopRadioButtonActionPerformed
        // TODO add your handling code here:
        updateStopCategory(gtfsNoUpload, 0);
    }//GEN-LAST:event_existingStopRadioButtonActionPerformed

    private void allStopsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allStopsRadioButtonActionPerformed
        // TODO add your handling code here:
        updateStopCategory(gtfsAll, 0);
    }//GEN-LAST:event_allStopsRadioButtonActionPerformed

    private void osmStopsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_osmStopsComboBoxActionPerformed
        // TODO add your handling code here:
        updateStopTable((Stop)gtfsStopsComboBox.getSelectedItem(), (Stop)osmStopsComboBox.getSelectedItem());
    }//GEN-LAST:event_osmStopsComboBoxActionPerformed

    private void gtfsRoutesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gtfsRoutesComboBoxActionPerformed
        // TODO add your handling code here:
        updateRouteTable((Route)gtfsRoutesComboBox.getSelectedItem());
}//GEN-LAST:event_gtfsRoutesComboBoxActionPerformed

    private void allRoutesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allRoutesRadioButtonActionPerformed
        // TODO add your handling code here:
        updateRouteCategory(gtfsRouteAll);
}//GEN-LAST:event_allRoutesRadioButtonActionPerformed

    private void newRoutesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRoutesRadioButtonActionPerformed
        // TODO add your handling code here:
        updateRouteCategory(gtfsRouteUploadNoConflict);
}//GEN-LAST:event_newRoutesRadioButtonActionPerformed

    private void existingRoutesWithUpdatesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingRoutesWithUpdatesRadioButtonActionPerformed
        // TODO add your handling code here:
        updateRouteCategory(gtfsRouteModify);
}//GEN-LAST:event_existingRoutesWithUpdatesRadioButtonActionPerformed

    private void existingRoutesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingRoutesRadioButtonActionPerformed
        // TODO add your handling code here:
        updateRouteCategory(gtfsRouteNoUpload);
}//GEN-LAST:event_existingRoutesRadioButtonActionPerformed

    private void allMembersRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allMembersRadioButtonActionPerformed
        // TODO add your handling code here:
        updateMemberList((Route)gtfsRoutesComboBox.getSelectedItem(),"all");
}//GEN-LAST:event_allMembersRadioButtonActionPerformed

    private void osmMembersRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_osmMembersRadioButtonActionPerformed
        // TODO add your handling code here:
        updateMemberList((Route)gtfsRoutesComboBox.getSelectedItem(),"OSM server");
}//GEN-LAST:event_osmMembersRadioButtonActionPerformed

    private void gtfsMembersRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gtfsMembersRadioButtonActionPerformed
        // TODO add your handling code here:
        updateMemberList((Route)gtfsRoutesComboBox.getSelectedItem(),"GTFS dataset");
}//GEN-LAST:event_gtfsMembersRadioButtonActionPerformed

    private void bothMembersRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bothMembersRadioButtonActionPerformed
        // TODO add your handling code here:
        updateMemberList((Route)gtfsRoutesComboBox.getSelectedItem(),"both GTFS dataset and OSM server");
}//GEN-LAST:event_bothMembersRadioButtonActionPerformed

    private void tableStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableStopButtonActionPerformed
        // TODO add your handling code here:
        Stop selectedGtfsStop = (Stop)gtfsStopsComboBox.getSelectedItem();
        String selectedGtfs = selectedGtfsStop.toString();

        String tableStopButtonText = tableStopButton.getText();
        if(tableStopButtonText.contains("Save Change")) {
            // Save Checkboxes values
            // no need to add 2 since lat and lon are already there (counted)
            ArrayList<Boolean> saveValues = new ArrayList<Boolean>(stopTableModel.getRowCount()*2);
            for(int i=0; i<stopTableModel.getRowCount(); i++){
                saveValues.add((Boolean)stopTableModel.getValueAt(i, 2));
                saveValues.add((Boolean)stopTableModel.getValueAt(i, 4));
            }
            finalCheckboxes.put(selectedGtfs, saveValues);

            // Save to final Stops
            Stop st = finalStops.get(selectedGtfs);     //not creating new object
            for(int i=0; i<stopTableModel.getRowCount(); i++){
                String tagName = (String)stopTableModel.getValueAt(i, 0);
                String tagValue = (String)stopTableModel.getValueAt(i, 5);
                if(tagName.equals("lat")) st.setLat(tagValue);
                else if(tagName.equals("lon")) st.setLon(tagValue);
                else {
                    st.addAndOverwriteTag(tagName, tagValue);
                }
            }

            if(!tableStopButtonText.contains("Accept")) JOptionPane.showMessageDialog(this,"Changes have been made!");
        }
        if(tableStopButtonText.contains("Accept"))
        {
            // stops to finish
            if(stopsToFinish.contains(selectedGtfsStop.toString()))
            {
            	
                stopsToFinish.remove(selectedGtfsStop.toString());
                int visited = totalNumberOfStopsToFinish - stopsToFinish.size();
                finishProgressBar.setString(Integer.toString(visited)
                                                +"/"+totalNumberOfStopsToFinish+" stops");
                if(!stopsToFinish.isEmpty())
                {
                    int progressValue = finishProgressBar.getValue();
                    if((100/totalNumberOfStopsToFinish)<=0)
                    {
                        progressValue = (visited*100)/totalNumberOfStopsToFinish;
                    } else {
                        progressValue += 100/totalNumberOfStopsToFinish;
                    }
                    finishProgressBar.setValue(progressValue);
                } else {
                    finishProgressBar.setValue(100);
                }
            }

            if(!tableStopButtonText.contains("Save Change")) JOptionPane.showMessageDialog(this,"Stop is accepted!");
            
            
    		// 14thchanges the OSM COMbo box but not the gtfs one
    		// 16-10 only seems to work if tags not changed!?
     		if (gtfsStopsComboBox.getSelectedIndex() < gtfsStopsComboBox.getItemCount())
            {
             
             gtfsStopsComboBox.setSelectedIndex(gtfsStopsComboBox.getSelectedIndex()+1);
            // updateBusStop((Stop)gtfsStopsComboBox.getSelectedItem());
            }
        }

        if(tableStopButtonText.equals("Accept & Save Change")) {
            JOptionPane.showMessageDialog(this,"Stop is accepted and changes have been made!");
            updateButtonTableStop("Save Change", false, "Save Change", false);
        } else {
            updateButtonTableStop("Accept", true, "Save Change", false);
        }
}//GEN-LAST:event_tableStopButtonActionPerformed

    private void saveChangeRouteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveChangeRouteButtonActionPerformed
        // TODO add your handling code here:
        saveChangeRouteButton.setEnabled(false);
}//GEN-LAST:event_saveChangeRouteButtonActionPerformed

    private void exportGtfsValueGtfsDataOnlyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGtfsValueGtfsDataOnlyMenuItemActionPerformed
        // TODO add your handling code here:
        WriteFile.exportStops("exportGtfsValueGtfsOnly.csv", agencyStops, true);
        JOptionPane.showMessageDialog(this, "exportGtfsValueGtfsOnly.csv has been written to "+ (new File(".")).getAbsolutePath());
    }//GEN-LAST:event_exportGtfsValueGtfsDataOnlyMenuItemActionPerformed

    private void exportGtfsValueWithOsmTagsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGtfsValueWithOsmTagsMenuItemActionPerformed
        // TODO add your handling code here:
        ArrayList<String> keys = new ArrayList<String>(agencyStops.keySet());
        Hashtable<String, Stop> gtfsDefaultFinalStops = new Hashtable<String, Stop>();
        gtfsDefaultFinalStops.putAll(agencyStops);
        for(int i=0; i<keys.size(); i++){
            String sid = keys.get(i);
            Stop s = gtfsDefaultFinalStops.get(sid);
            s.addTags(finalStops.get(sid).getTags());
        }
        WriteFile.exportStops("exportGtfsValueWithOsmTags.csv", gtfsDefaultFinalStops, false);
        JOptionPane.showMessageDialog(this, "exportGtfsValueWithOsmTags.csv has been written to "+ (new File(".")).getAbsolutePath());
    }//GEN-LAST:event_exportGtfsValueWithOsmTagsMenuItemActionPerformed

    private void exportOsmValueGtfsDataOnlyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOsmValueGtfsDataOnlyMenuItemActionPerformed
        // TODO add your handling code here:
        WriteFile.exportStops("exportOsmValueGtfsOnly.csv", finalStops, true);
        JOptionPane.showMessageDialog(this, "exportOsmValueGtfsOnly.csv has been written to "+ (new File(".")).getAbsolutePath());
    }//GEN-LAST:event_exportOsmValueGtfsDataOnlyMenuItemActionPerformed

    private void exportOsmValueWithOsmTagsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOsmValueWithOsmTagsMenuItemActionPerformed
        // TODO add your handling code here:
        WriteFile.exportStops("exportOsmValueWithOsmTags.csv", finalStops, false);
        JOptionPane.showMessageDialog(this, "exportOsmValueWithOsmTags.csv has been written to "+ (new File(".")).getAbsolutePath());
    }//GEN-LAST:event_exportOsmValueWithOsmTagsMenuItemActionPerformed

    private void exportOsmValueStopsWithConflictsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportOsmValueStopsWithConflictsMenuItemActionPerformed
        // TODO add your handling code here:
        WriteFile.exportStops("exportOsmValueWithConflictsOnly.csv", osmDefaultOnlyChangedFinalStops, false);
        JOptionPane.showMessageDialog(this, "exportOsmValueWithConflictsOnly.csv has been written to "+ (new File(".")).getAbsolutePath());
    }//GEN-LAST:event_exportOsmValueStopsWithConflictsMenuItemActionPerformed

    private void dummyUploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dummyUploadButtonActionPerformed
        // TODO add your handling code here:
        generateStopsToUpload(finalStops);
        String osmChangeText = osmRequest.getRequestContents("DUMMY", upload, modify, delete, finalRoutes);
        new WriteFile("DUMMY_OSM_CHANGE.txt", osmChangeText);
        JOptionPane.showMessageDialog(this, "DUMMY_OSM_CHANGE.txt has been written to "+ (new File(".")).getAbsolutePath());
    }//GEN-LAST:event_dummyUploadButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:
        String userInput = searchTextField.getText();
        ArrayList<String> keys = new ArrayList<String>(searchKeyToStop.keySet());
        for (int i=0; i<keys.size(); i++){
            if(keys.get(i).toUpperCase().contains(userInput.toUpperCase())){
                updateDataWhenStopSelected(searchKeyToStop.get(keys.get(i)));
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "No stop found!\nPlease check your spelling!");
    }//GEN-LAST:event_searchButtonActionPerformed

    /**
    * @param args the command line arguments
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ReportViewer().setVisible(true);
            }
        });
    }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allMembersRadioButton;
    private javax.swing.JRadioButton allRoutesRadioButton;
    private javax.swing.JRadioButton allStopsRadioButton;
    private javax.swing.JRadioButton bothMembersRadioButton;
    private javax.swing.JTable dataTable;
    private javax.swing.JButton donotUploadButton;
    private javax.swing.JButton dummyUploadButton;
    private javax.swing.JRadioButton existingRoutesRadioButton;
    private javax.swing.JRadioButton existingRoutesWithUpdatesRadioButton;
    private javax.swing.JRadioButton existingStopRadioButton;
    private javax.swing.JMenuItem exportGtfsValueGtfsDataOnlyMenuItem;
    private javax.swing.JMenuItem exportGtfsValueWithOsmTagsMenuItem;
    private javax.swing.JMenuItem exportOsmValueGtfsDataOnlyMenuItem;
    private javax.swing.JMenuItem exportOsmValueStopsWithConflictsMenuItem;
    private javax.swing.JMenuItem exportOsmValueWithOsmTagsMenuItem;
    private javax.swing.JProgressBar finishProgressBar;
    private javax.swing.JTextArea generalInformationRouteTextArea;
    private javax.swing.JTextArea generalInformationStopTextArea;
    private javax.swing.JRadioButton gtfsMembersRadioButton;
    private javax.swing.JComboBox gtfsRoutesComboBox;
    private javax.swing.JComboBox gtfsStopsComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lastEditedLabel;
    private org.jdesktop.swingx.JXMapKit mapJXMapKit;
    private javax.swing.JTable memberTable;
    private javax.swing.ButtonGroup membersButtonGroup;
    private javax.swing.JRadioButton newNoMatchStopsRadioButton;
    private javax.swing.JRadioButton newRoutesRadioButton;
    private javax.swing.JRadioButton newWithMatchStopsRadioButton;
    private javax.swing.JRadioButton osmMembersRadioButton;
    private javax.swing.JComboBox osmStopsComboBox;
    private javax.swing.JTable routeTable;
    private javax.swing.ButtonGroup routesButtonGroup;
    private javax.swing.JButton saveChangeRouteButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchTextField;
    private javax.swing.ButtonGroup stopsButtonGroup;
    private javax.swing.JButton tableStopButton;
    private javax.swing.JLabel totalGtfsMembersLabel;
    private javax.swing.JLabel totalGtfsRoutesLabel;
    private javax.swing.JLabel totalGtfsStopsLabel;
    private javax.swing.JLabel totalNewMembersLabel;
    private javax.swing.JLabel totalOsmMembersLabel;
    private javax.swing.JLabel totalOsmStopsLabel;
    private javax.swing.JRadioButton updateStopsRadioButton;
    private javax.swing.JButton uploadDataButton;
    // End of variables declaration//GEN-END:variables

}
