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
package edu.usf.cutr.go_sync.io;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import edu.usf.cutr.go_sync.tag_defs;
import edu.usf.cutr.go_sync.object.OperatorInfo;
import edu.usf.cutr.go_sync.object.Route;
import edu.usf.cutr.go_sync.object.Stop;
import edu.usf.cutr.go_sync.tools.OsmFormatter;
import edu.usf.cutr.go_sync.object.NetexQuay;
import edu.usf.cutr.go_sync.object.NetexStopElement;
import edu.usf.cutr.go_sync.object.NetexStopPlace;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

public class GTFSReadIn {
    private static Hashtable<String, Route> allRoutes;
    private static final String ROUTE_KEY = "route_ref";
    private static final String NTD_ID_KEY = "ntd_id";

    private List<Stop> stops;
    HashMap <String, NetexQuay> netexQuaysByGtfsId;
    HashMap <String, NetexQuay> netexQuaysByQuayId;
    HashMap <String, NetexStopPlace> netexLogicalSites;
    HashMap <String, NetexStopPlace> netexParentSitesByGtfsId;
    HashMap <String, NetexStopPlace> netexAllStopPlacesByGtfsId;
    HashMap <String, NetexStopPlace> netexAllStopPlacesByStopPlaceId;

    public GTFSReadIn() {
        stops = new ArrayList<Stop>();
        allRoutes = new Hashtable<String, Route>();
//        readBusStop("C:\\Users\\Khoa Tran\\Desktop\\Summer REU\\Khoa_transit\\stops.txt");
    }
    public static Set<String> getAllRoutesID(){
        return allRoutes.keySet();
    }

//TODO handle multiple agencies
    public String readAgency(String agency_fName){
        try {
            BufferedReader br = new BufferedReader(new FileReader(agency_fName));
            CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withHeader());

            for (CSVRecord csvRecord : parser) {
                String agencyName;
                if (csvRecord.get(tag_defs.GTFS_AGENCY_NAME_KEY) == null ||
                    csvRecord.get(tag_defs.GTFS_AGENCY_NAME_KEY).isEmpty())
                    agencyName = csvRecord.get(tag_defs.GTFS_AGENCY_ID_KEY);
                else agencyName = csvRecord.get(tag_defs.GTFS_AGENCY_NAME_KEY);
                br.close();
                return agencyName;
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
            return null;
        }
        return null;
    }

    public List<Stop> readBusStop(String fName, String agencyName, String routes_fName, String trips_fName, String stop_times_fName, String netexStopsFilename){
        long tStart = System.currentTimeMillis();
        Hashtable<String, HashSet<Route>> id = matchRouteToStop(routes_fName, trips_fName, stop_times_fName);
        Hashtable<String, HashSet<Route>> stopIDs = new Hashtable<String, HashSet<Route>>(id);

        if (netexStopsFilename != null && !netexStopsFilename.isEmpty()) {
            readNetexStopsFile(netexStopsFilename);
        }

        String thisLine;
        String [] elements;
        int stopIdKey=-1, stopNameKey=-1, stopLatKey=-1, stopLonKey=-1, locationTypeKey=-1, parentStationKey=-1;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(fName)),"UTF-8"));
            HashMap<String,Integer> keysIndex = new HashMap<String,Integer> ();
            thisLine = br.readLine();
            StringReader sr = new StringReader(thisLine);
            CSVParser headerParser = CSVParser.parse(sr, CSVFormat.DEFAULT.withHeader(
                    //"route_id","route_short_name","route_long_name","route_desc","route_type","route_url","color","route_text_color"
            ));

            List<String> CSVkeysList = headerParser.getHeaderNames();
            ArrayList<String> CSVkeysListNew = new ArrayList<>(CSVkeysList);
            String[] keys =  new String[CSVkeysList.size()];
            keys = CSVkeysList.toArray(keys);{
                    for(int i=0; i<keys.length; i++) {
                        switch (keys[i]) {
                            case "stop_id":
                                stopIdKey = i;
                                break;
                            case "stop_name":
                                stopNameKey = i;
                                break;
                            case "stop_lat":
                                stopLatKey = i;
                                break;
                            case "stop_lon":
                                stopLonKey = i;
                                break;
                            case tag_defs.GTFS_STOP_URL_KEY:
                                keysIndex.put(tag_defs.OSM_STOP_URL_KEY, i);
                                break;
                            case tag_defs.GTFS_ZONE_KEY:
                                keysIndex.put(tag_defs.OSM_ZONE_KEY, i);
                                break;
                            case tag_defs.GTFS_STOP_TYPE_KEY:
                                locationTypeKey = i;
                                keysIndex.put(tag_defs.OSM_STOP_TYPE_KEY, i);
                                break;
                            case tag_defs.GTFS_PARENT_STATION_KEY:
                                parentStationKey = i;
                                keysIndex.put(tag_defs.OSM_PARENT_STATION_KEY, i);
                                break;
                            case tag_defs.GTFS_WHEELCHAIR_KEY:
                                keysIndex.put(tag_defs.OSM_WHEELCHAIR_KEY, i);
                                break;
                            default:
                                String t = "gtfs_" + keys[i];
                                keysIndex.put(t, i);
                        }
                    }
                    System.out.println(keysIndex.toString());
//                    System.out.println(stopIdKey+","+stopNameKey+","+stopLatKey+","+stopLonKey);
                }
            CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withHeader(keys));
            for (CSVRecord csvRecord : parser) {
                Iterator<String> iter = csvRecord.iterator();
                Map<String,String> hm = csvRecord.toMap();
                elements =  new String[hm.size()];
                elements = hm.values().toArray(elements);
                String public_transport_type = "";
                 //add leading 0's to gtfs_id
                    String tempStopId = OsmFormatter.getValidBusStopId(elements[stopIdKey]);
                    //System.out.println("Reading stop from gtfs: " + tempStopId.toString());
                    NetexStopElement netexObject = getMatchingNetexObject(elements[locationTypeKey], elements[stopIdKey], elements[parentStationKey], elements[stopLatKey],elements[stopLonKey], elements[stopNameKey]);
                    Stop s = new Stop("node", tempStopId, agencyName, elements[stopNameKey],elements[stopLatKey],elements[stopLonKey], netexObject);
                    HashSet<String> keysn = new HashSet<String>(keysIndex.keySet());
                    Iterator it = keysn.iterator();
                    try {
                        while(it.hasNext()) {
                        	String k = (String)it.next();

                            String v = null;
                            //if(!lastIndexEmpty) v = elements[(Integer)keysIndex.get(k)];
                            if(keysIndex.get(k) < elements.length) v = elements[keysIndex.get(k)];
                            if ((v!=null) && (!v.isEmpty())) {
                                if (k.equals(tag_defs.OSM_STOP_TYPE_KEY)) {
                                    switch(Integer.parseInt(v)) {
                                        // https://developers.google.com/transit/gtfs/reference/stops-file
                                        case 0:
                                            v = public_transport_type = "platform";
                                            break;
                                        case 1:
                                            v = public_transport_type = "station";
                                            break;
                                        default: break;
                                    }
                                }
                                if (k.equals(tag_defs.OSM_WHEELCHAIR_KEY)) {
                                    String parent = "";

                                    if (keysn.contains("gtfs_parent_station"))
                                        parent = elements[keysIndex.get(k)];
                                    if (parent.isEmpty()) {
                                        switch (Integer.parseInt(v)) {
                                            // https://developers.google.com/transit/gtfs/reference/stops-file
                                            case 0:
                                                v = "";
                                                break;
                                            case 1:
                                                v = "limited";
                                                break;
                                            case 2:
                                                v = "no";
                                                break;
                                            default:
                                                break;
                                        }
                                        s.addTag(k, v);
                                    }
                                } else
                                    s.addTag(k, v.replace("  ", " ").trim());
                            }
                            //System.out.print(k+":" + v +" ");
                        }
//                        s.addTag(NTD_ID_KEY, OperatorInfo.getNTDID());
//                        s.addTag("url", s.getTag("stop_url"));

// disable source tag
//                        s.addTag("source", "http://translink.com.au/about-translink/reporting-and-publications/public-transport-performance-data");
//                        if (!tempStopId.contains("place")) s.addTag("url", "http://translink.com.au/stop/"+tempStopId);

                    } catch(Exception e) {
                        System.out.println("Error occurred! Please check your GTFS input files");
                        System.out.println(e.toString());
                        System.exit(0);
                    }
                    // TODO use routes to determine stop tags
                   // System.err.println(s.getTags());
                    String r = getRoutesInTextByBusStop(stopIDs.get(tempStopId));

//             generate tag for routes using stop
                    if (!r.isEmpty()) s.addTag(ROUTE_KEY, r);
                    HashSet<Route> asdf = stopIDs.get(tempStopId);
                    if(asdf!=null)s.addRoutes(stopIDs.get(tempStopId));

                    stops.add(s);

                    HashMap<String, String> modes = getModeTagsByBusStop(stopIDs.get(tempStopId), public_transport_type);
                    if (!r.isEmpty()) s.addTags(modes);
//                    System.out.println(thisLine);
                }
//            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
        long tDelta = System.currentTimeMillis() - tStart;
//        this.setMessage("Completed in "+ tDelta /1000.0 + "seconds");
        System.out.println("GTFSReadIn Completed in "+ tDelta /1000.0 + "seconds");
        return stops;
    }

    public Hashtable<String, Route> readRoutes(String routes_fName){
        Hashtable<String, Route> routes = new Hashtable<String, Route>();
        String thisLine;
        String [] elements;
        int routeIdKey=-1, routeShortNameKey=-1,routeLongNameKey=-1;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(routes_fName)), "UTF-8"));
            HashMap<String,Integer> keysIndex = new HashMap<String,Integer> ();
            thisLine = br.readLine();
            StringReader sr = new StringReader(thisLine);
            CSVParser headerParser = CSVParser.parse(sr, CSVFormat.DEFAULT.withHeader(
                    //"route_id","route_short_name","route_long_name","route_desc","route_type","route_url","color","route_text_color"
            ));
            List<String> CSVkeysList = headerParser.getHeaderNames();
            ArrayList<String> CSVkeysListNew = new ArrayList<>(CSVkeysList);
            String[] keysn =  new String[CSVkeysList.size()];
            keysn = CSVkeysList.toArray(keysn);
            for(int i=0; i<keysn.length; i++) {
                //read keys
                switch (keysn[i]) {
                    case tag_defs.GTFS_ROUTE_ID_KEY:
                        routeIdKey = i;
                        break;
                    case tag_defs.GTFS_ROUTE_URL_KEY:
                        keysIndex.put(tag_defs.OSM_ROUTE_URL_KEY, i);
                        break;
                    case "route_type":
                        keysIndex.put(tag_defs.OSM_ROUTE_TYPE_KEY, i);
                        break;
                    case tag_defs.GTFS_COLOUR_KEY:
                    case tag_defs.GTFS_COLOR_KEY:
                        keysIndex.put(tag_defs.OSM_COLOUR_KEY, i);
                        break;
                    case tag_defs.GTFS_ROUTE_NUM_KEY:
                        routeShortNameKey = i;
                        break;
                    case tag_defs.GTFS_ROUTE_NAME_KEY:
                        keysIndex.put(tag_defs.OSM_ROUTE_NAME_KEY, i);
                        break;
                    default:
                        String t = "gtfs_" + keysn[i];
                        keysIndex.put(t, i);
                        break;
                }
            }

            {
                final Pattern colourPattern = Pattern.compile("^[a-fA-F0-9]+$");
                CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withHeader(keysn));
                for (CSVRecord csvRecord : parser) {

                    Iterator<String> iter = csvRecord.iterator();
                    Map<String,String> hm = csvRecord.toMap();
                    elements =  new String[hm.size()];
                    elements = hm.values().toArray(elements);

                    String routeName;
                    if(elements[routeShortNameKey]==null || elements[routeShortNameKey].isEmpty()) routeName = elements[routeIdKey];
                    else routeName = elements[routeShortNameKey];
                    Route r = new Route(elements[routeIdKey], routeName, OperatorInfo.getFullName());
                    HashSet<String> keys = new HashSet<String>(keysIndex.keySet());
                    Iterator<String> it = keys.iterator();
                    try {
                        while(it.hasNext()) {
                            String k = it.next();
                            String v = null;
                            int ki = keysIndex.get(k);
                            if(/*!(lastIndexEmpty && */ki <elements.length) v = elements[ki];
                            if ((v!=null) && (!v.isEmpty())) {
                                if (k.equals(tag_defs.OSM_ROUTE_TYPE_KEY)) {
                                    String route_value;
                                    switch(Integer.parseInt(v)) {
                                        // TODO allow drop down finetuning selection on report viewer
                                        // https://developers.google.com/transit/gtfs/reference/routes-file
                                        // https://wiki.openstreetmap.org/wiki/Relation:route#Route_types_.28route.29
                                        case 0: route_value = "light_rail";	break;// 0: Tram, Streetcar, Light rail. Any light rail or street level system within a metropolitan area.
                                        case 1:	route_value = "subway";     break;	// Subway, Metro. Any underground rail system within a metropolitan area.
                                        case 2: route_value = "train";      break;	// Rail. Used for intercity or long-distance travel.
                                        case 3: route_value = "bus";        break;	// Bus. Used for short- and long-distance bus routes.
                                        case 4: route_value = "ferry";      break;	// Ferry. Used for short- and long-distance boat service.
                                        case 5: route_value = "tram";       break;	// Cable car. Used for street-level cable cars where the cable runs beneath the car.
                                        case 6: k = "aerialway";
                                                route_value = "yes";        break;	// Gondola, Suspended cable car. Typically used for aerial cable cars where the car is suspended from the cable.
                                        // TODO use railway=funicular
                                        case 7: k = "railway";
                                                route_value = "funicular";  break;	// Funicular. Any rail system designed for steep inclines.
                                        default: route_value = v; break;
                                    }
                                    v = route_value;
                                }
                                //prepend hex colours
//                                if (k.equals(tag_defs.OSM_COLOUR_KEY))
//                                    System.out.println(tag_defs.OSM_COLOUR_KEY + " "+ v + " #"+v);
                                if (k.equals(tag_defs.OSM_COLOUR_KEY) && ((v.length() == 3 || v.length() == 6) && colourPattern.matcher(v).matches()))/*^[a-fA-F0-9]+$")))*/ {
                                    v = "#".concat(v);
                                }
                                r.addTag(k, v.replace("  ", " ").trim());
                            }
                        }
                    } catch(Exception e){
                        System.out.println("Error occurred! Please check your GTFS input files");
                        System.out.println(e.toString());
                        System.exit(0);
                    }
                    routes.put(elements[routeIdKey], r);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
        return routes;
    }

    public Hashtable<String, HashSet<Route>> matchRouteToStop(String routes_fName, String trips_fName, String stop_times_fName){
        allRoutes.putAll(readRoutes(routes_fName));
        HashMap<String,String> tripIDs = new HashMap<String,String>();

        // trips.txt read-in
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(trips_fName)),"UTF-8"));
            CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withHeader());
            for (CSVRecord csvRecord : parser) {

                String tripId = csvRecord.get(tag_defs.GTFS_TRIP_ID_KEY);
                // not sure if tripId is unique in trips.txt, e.g. can 1 trip_id has multiple route_id
                if (tripIDs.containsKey(tripId)) {
                    System.out.println("Repeat "+tripId);
                }
                tripIDs.put(tripId, csvRecord.get(tag_defs.GTFS_ROUTE_ID_KEY));
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }

        // hashtable String(stop_id) vs. HashSet(routes)
        Hashtable<String, HashSet<Route>> stopIDs = new Hashtable<String, HashSet<Route>>();
        // stop_times.txt read-in
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(stop_times_fName)), "UTF-8"));

            CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withHeader());

            for (CSVRecord csvRecord : parser) {
                // This seems to be the fastest method using csvparser
                String trip = csvRecord.get(tag_defs.GTFS_TRIP_ID_KEY);
                HashSet<Route> routes = new HashSet<Route>();
                Route tr = null;
                if (tripIDs.get(trip) != null) tr = allRoutes.get(tripIDs.get(trip));
                if (tr != null) routes.add(tr);
                String sid = OsmFormatter.getValidBusStopId(csvRecord.get(tag_defs.GTFS_STOP_ID_KEY));
                if (stopIDs.containsKey(sid)) {
                    routes.addAll(stopIDs.get(sid));
                    stopIDs.remove(sid);
                }
                stopIDs.put(sid, routes);
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
        return stopIDs;
    }

    //TODO implement  this
    // https://wiki.openstreetmap.org/wiki/Public_transport
    public HashMap<String,String> getModeTagsByBusStop(HashSet<Route> r, String public_transport_type) {
        HashMap<String,String> keys = new HashMap<String,String>();
        if (r!=null) {
            //convert from hashset to arraylist
            ArrayList<Route> routes = new ArrayList<Route>(r);
            for (Route rr : routes) {
                if (public_transport_type.equals("platform")) {
                    if (rr.containsKey(tag_defs.OSM_ROUTE_TYPE_KEY)) {
                        switch (rr.getTag(tag_defs.OSM_ROUTE_TYPE_KEY)) {
                            case "bus":
                            case "trolley_bus":
                            case "share_taxi":
                                keys.put("highway", "bus_stop");
                                break;
                            case "railway":
                            case "tram":
                            case "subway":
                            case "light_rail":
                                keys.put("railway", "paltform");
                                break;
                            default:
                                break;
                        }
                    }

                } else if (public_transport_type.equals("stop_position")) {
                    keys.put(rr.getTag(tag_defs.OSM_ROUTE_TYPE_KEY), "yes");
                } else if (public_transport_type.equals("station")) {
                    if (rr.containsKey(tag_defs.OSM_ROUTE_TYPE_KEY)) {
                        switch (rr.getTag(tag_defs.OSM_ROUTE_TYPE_KEY)) {
                            case "bus":
                                keys.put("amenity", "bus_station");
                                break;
                            case "railway":
                            case "tram":
                            case "subway":
                            case "light_rail":
                                keys.put("railway", "station");
                                break;
                            case "ferry":
                                keys.put("amenity", "ferry_terminal");
                                break;
                            default:
                                break;
                        }
                    }
                    if (rr.containsKey("railway") && rr.getTag("railway").equals("funicular")) {
                        keys.put("railway", "station");
                        keys.put("station", "funicular");
                    }
                    if (rr.containsKey("aerialway")) {
                        keys.put("aerialway", "station");
                    }
                }
            }
        }
        return keys;
    }

    private class hashCodeCompare implements  Comparator
    {
        @Override
        public int compare(Object o, Object t1) {
            return o.hashCode() - t1.hashCode();
        }
    }

    public String getRoutesInTextByBusStop(HashSet<Route> r) {
        String text="";

        if (r!=null) {
            TreeSet<String> routeRefSet = new TreeSet<String>(new hashCodeCompare());
            //convert from hashset to arraylist
            ArrayList<Route> routes = new ArrayList<Route>(r);
            for (Route rr:routes) {
                routeRefSet.add(rr.getRouteRef());
            }
            text = String.join(";",routeRefSet);
        }
        return text;
    }

    private NetexStopElement getMatchingNetexObject(String gtfsLocationType, String gtfsId, String parentSite, String lat, String lon, String gtfsStopName) {
        NetexStopElement matchingObject = null;
        if (gtfsLocationType == null) {
            return null;
        }

        if (gtfsLocationType.isEmpty() || gtfsLocationType.equals("0")) {
            // 0: Stop (or Platform) in gtfs, corresponds to a Quay in Netex
            matchingObject = getMatchingNetexQuay(gtfsId, parentSite, lat, lon, gtfsStopName);
        }

        if (gtfsLocationType.isEmpty() || gtfsLocationType.equals("1")) {
            // 1: station in gtfs, corresponds to a StopPlace in Netex
            matchingObject = getMatchingNetexStopPlace(gtfsId, parentSite, lat, lon, gtfsStopName);
        }

        return matchingObject;
    }

    private NetexStopPlace getMatchingNetexStopPlace(String gtfsId, String parentSite, String lat, String lon, String gtfsStopName) {
        if (netexAllStopPlacesByGtfsId == null) {
            return null;
        }

        // Check if there is a <StopPlace id=""> matching the gtfsId
        // If it exists, return this value.
        NetexStopPlace stopPlace = netexAllStopPlacesByGtfsId.get(gtfsId);
        if (stopPlace != null) {
            return stopPlace;
        }

        // TODO: implement the case where nothing was found.
        //   iterate on all stopPlaces to find the ones with the same lat/lon
        //     if only one, return that one
        //     if more that one, check by name
        //       if only one with that name, return that one
        //       else return the first one with that name
        ArrayList<NetexStopPlace> stopPlaceMatchingLatLon = new ArrayList<>();
        for (NetexStopPlace sp : netexAllStopPlacesByGtfsId.values()) {
            if (lat.equals(sp.getLat()) && lon.equals(sp.getLon())) {
                stopPlaceMatchingLatLon.add(sp);
            }
        }
        if (stopPlaceMatchingLatLon.size() == 1) {
            return stopPlaceMatchingLatLon.get(0);
        }

        ArrayList<NetexStopPlace> stopPlaceMatchingName = new ArrayList<>();
        if (stopPlaceMatchingLatLon.size() > 1) {
            for (NetexStopPlace sp : stopPlaceMatchingLatLon) {
                if (gtfsStopName.equals(sp.getName())) {
                    stopPlaceMatchingName.add(sp);
                }
            }
        }
        if (!stopPlaceMatchingName.isEmpty()) {
            return stopPlaceMatchingName.get(0);
        }

        // if there is no match found, return null
        System.out.println("Warning: No matching StopPlace found in netex for gtfs stop_id: " + gtfsId);
        return null;
    }

    private NetexQuay getMatchingNetexQuay(String gtfsId, String parentSite, String lat, String lon, String gtfsStopName) {
        if (netexQuaysByGtfsId == null) {
            return null;
        }

        // Check if there is a <Quay id=""> matching the gtfsId
        // If it exists, return this value.
        NetexQuay quay = netexQuaysByGtfsId.get(gtfsId);
        if (quay != null) {
            return quay;
        }

        // Find the "parent StopPlace" that matches the parentSite and determine the StopPlace holding the Quays
        // Then, with these "child StopPlace"s:
        //   get all the Quay the refer to (QuayRef)
        //     if there is only one Quay, return that one.
        //     if there are more than one, return the one that matches the lat/lon.
        //       if more than one matches the lat/lon return the one whose name is same as gtfs' stop_name
        //         if more than one matches the gtfs stop_name return the 1st one
        // If still not found, search all the Quays for a match (by lat/lon)
        ArrayList<NetexQuay> quayMatches = new ArrayList<>();

        NetexStopPlace parentStopPlace = netexParentSitesByGtfsId.get(parentSite);
        ArrayList<NetexStopPlace> childStopPlaces = new ArrayList<>();
        if (parentStopPlace != null) {
            for (String childSiteRef : parentStopPlace.getChildSiteRef()) {
                childStopPlaces.add(netexAllStopPlacesByStopPlaceId.get(childSiteRef));
            }
            for (NetexStopPlace childStopPlace : childStopPlaces) {
                for (String quayRef : childStopPlace.getQuayRefs()) {
                    quayMatches.add(netexQuaysByQuayId.get(quayRef));
                }
            }
        }

        if (quayMatches.size() == 1) {
            return quayMatches.get(0);
        }

        ArrayList<NetexQuay> quayMatchingLatLon = new ArrayList<>();
        if (quayMatches.size() > 1) {
            for (NetexQuay q : quayMatches) {
                if (lat.equals(q.getLat()) && lon.equals(q.getLon())) {
                    quayMatchingLatLon.add(q);
                }
            }
        }

        if (quayMatchingLatLon.isEmpty()) {
            // Finally search in all Quays if any matches lat/lon
            for (NetexQuay q : netexQuaysByQuayId.values()) {
                if (lat.equals(q.getLat()) && lon.equals(q.getLon())) {
                    quayMatchingLatLon.add(q);
                }
            }
        }

        if (quayMatchingLatLon.size() == 1) {
            return quayMatchingLatLon.get(0);
        }

        ArrayList<NetexQuay> quayMatchingName = new ArrayList<>();
        if (quayMatchingLatLon.size() > 1) {
            for (NetexQuay qmll : quayMatchingLatLon) {
                if (gtfsStopName.equals(qmll.getName())) {
                    quayMatchingName.add(qmll);
                }
            }
        }
        if (!quayMatchingName.isEmpty()) {
            return quayMatchingName.get(0);
        }

        // if there is no match found, return null
        System.out.println("Warning: No matching Quay found in netex for gtfs stop_id: " + gtfsId);
        return null;
    }

    private void readNetexStopsFile(String netexFilePath) {
        try {
            File nextFile = new File(netexFilePath);
            NetexParser netexParser = new NetexParser();
            SAXParserFactory.newInstance().newSAXParser().parse(nextFile, netexParser);
            netexQuaysByGtfsId = netexParser.getQuayListByGtfsId();
            netexQuaysByQuayId = netexParser.getQuayListByQuayId();
            netexLogicalSites = netexParser.getLogicalSiteListByGtfsId();
            netexParentSitesByGtfsId = netexParser.getParentSiteListByGtfsId();
            netexAllStopPlacesByGtfsId = netexParser.getAllStopPlaceListByGtfsId();
            netexAllStopPlacesByStopPlaceId = netexParser.getAllStopPlaceListByStopPlaceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
