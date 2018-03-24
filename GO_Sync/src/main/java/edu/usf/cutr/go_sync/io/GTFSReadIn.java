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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.usf.cutr.go_sync.tag_defs;
import edu.usf.cutr.go_sync.object.OperatorInfo;
import edu.usf.cutr.go_sync.object.Route;
import edu.usf.cutr.go_sync.object.Stop;
import edu.usf.cutr.go_sync.tools.OsmFormatter;

public class GTFSReadIn {
    private static Hashtable<String, Route> allRoutes;
    private final String ROUTE_KEY = "route_ref";
    private final String NTD_ID_KEY = "ntd_id";
    private static final String UTF8_BOM = "\uFEFF";
//TODO read agency.txt
    

    private List<Stop> stops;

    public GTFSReadIn() {
        stops = new ArrayList<Stop>();
        allRoutes = new Hashtable<String, Route>();
//        readBusStop("C:\\Users\\Khoa Tran\\Desktop\\Summer REU\\Khoa_transit\\stops.txt");
    }

    public static Set<String> getAllRoutesID(){
        return allRoutes.keySet();
    }


    public String readAgency(String agency_fName)
    //public Hashtable<String, Route> readRoutes(String routes_fName)
    {
        String thisLine;
        String [] elements;
        int agencyIdKey=-1, agencyNameKey=-1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(agency_fName));
            boolean isFirstLine = true;
            Hashtable<String,Integer> keysIndex = new Hashtable<String,Integer>();
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    thisLine = thisLine.replace("\"", "");
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("agency_id")) agencyIdKey = i;
                        else {
                            if(keys[i].equals(tag_defs.GTFS_NETWORK_KEY)) agencyNameKey = i;
                            String t = "gtfs_"+keys[i];
                            keysIndex.put(t, i);
                        }
                    }
//                    System.out.println(stopIdKey+","+stopNameKey+","+stopLatKey+","+stopLonKey);
                }
                else {
                    boolean lastIndexEmpty=false;
                    thisLine = thisLine.trim();
                    if(thisLine.contains("\"")) {
                         String[] temp = thisLine.split("\"");
                         for(int x=0; x<temp.length; x++){
                             if(x%2==1) temp[x] = temp[x].replace(",", "");
                         }
                         thisLine = "";
                         for(int x=0; x<temp.length; x++){
                             thisLine = thisLine + temp[x];
                         }
                    }
                    elements = thisLine.split(",");
                    if(thisLine.charAt(thisLine.length()-1)==',') lastIndexEmpty=true;
                    String agencyName;
                    if (elements[agencyNameKey] == null || elements[agencyNameKey].equals(""))
                        agencyName = elements[agencyIdKey];
                    else agencyName = elements[agencyNameKey];

                    br.close();
                    return agencyName;
                    /*
                    Route r = new Route(elements[agencyIdKey], agencyName, OperatorInfo.getFullName());
                    HashSet<String> keys = new HashSet<String>();
                    keys.addAll(keysIndex.keySet());
                    Iterator<String> it = keys.iterator();
                    try {
                        while(it.hasNext()){
                            String k = (String)it.next();
                            String v = null;
                            if(!lastIndexEmpty) v = elements[(Integer)keysIndex.get(k)];
                            if ((v!=null) && (!v.equals(""))) r.addTag(k, v);
                        }
                    } catch(Exception e){
                        System.out.println("Error occurred! Please check your GTFS input files");
                        System.out.println(e.toString());
                        System.exit(0);
                    }
                    routes.put(elements[agencyIdKey], r);
                    */
                }
            }
            br.close();
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
            return null;
        }
        return null;
    }

    public List<Stop> readBusStop(String fName, String agencyName, String routes_fName, String trips_fName, String stop_times_fName){
        Hashtable<String, HashSet<Route>> stopIDs = new Hashtable<String, HashSet<Route>>();
        Hashtable<String, HashSet<Route>> id = matchRouteToStop(routes_fName, trips_fName, stop_times_fName);
        stopIDs.putAll(id);

        String thisLine;
        String [] elements;
        int stopIdKey=-1, stopNameKey=-1, stopLatKey=-1, stopLonKey=-1;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fName),"UTF-8"));
            boolean isFirstLine = true;
            Hashtable<String,Integer> keysIndex = new Hashtable<String,Integer>();
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    if (thisLine.startsWith(UTF8_BOM)) {
                        thisLine = thisLine.substring(1);
                    }
                    OperatorInfo.setGtfsFields(thisLine);
                    thisLine = thisLine.replace("\"", "");
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
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
                                keysIndex.put(tag_defs.OSM_URL_KEY, i);
                                break;
                            case tag_defs.GTFS_ZONE_KEY:
                                keysIndex.put(tag_defs.OSM_ZONE_KEY, i);
                                break;
                            case tag_defs.GTFS_STOP_TYPE_KEY:
                                keysIndex.put(tag_defs.OSM_STOP_TYPE_KEY, i);
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
                else {
                    boolean lastIndexEmpty=false;
                    thisLine = thisLine.trim();

                    if(thisLine.contains("\"")) {
                         String[] temp = thisLine.split("\"");
                         for(int x=0; x<temp.length; x++){
                             if(x%2==1) temp[x] = temp[x].replace(",", "");
                         }
                         thisLine = "";
                         for(int x=0; x<temp.length; x++){
                             thisLine = thisLine + temp[x];
                         }
                    }
                    elements = thisLine.split(",");
                    //System.out.println(elements.length);
                   // for (int zxc = 0; zxc< elements.length-1; zxc++) {System.out.print(elements[zxc]+ ",");}System.out.print(elements[elements.length] );
                    if(thisLine.charAt(thisLine.length()-1)==',') lastIndexEmpty=true;
                    //add leading 0's to gtfs_id
                    String tempStopId = OsmFormatter.getValidBusStopId(elements[stopIdKey]);
                    Stop s = new Stop(tempStopId, agencyName, elements[stopNameKey],elements[stopLatKey],elements[stopLonKey]);
                    HashSet<String> keys = new HashSet<String>();
                    keys.addAll(keysIndex.keySet());
                    Iterator it = keys.iterator();
                    try {
                        while(it.hasNext()){
                        	String k = (String)it.next();

                            String v = null;
                            //if(!lastIndexEmpty) v = elements[(Integer)keysIndex.get(k)];
                            if(keysIndex.get(k) < elements.length) v = elements[keysIndex.get(k)];
                            if ((v!=null) && (!v.equals(""))) {
                                if (k.equals(tag_defs.OSM_STOP_TYPE_KEY))
                                {
                                    switch(Integer.parseInt(v))
                                    {
                                        // https://developers.google.com/transit/gtfs/reference/stops-file
                                        case 0: v="platform";break;
                                        case 1: v="station"; break;
                                        default: break;
                                    }
                                }
                                if (k.equals(tag_defs.OSM_WHEELCHAIR_KEY)) {
                                    String parent = "";

                                    if (keys.contains("gtfs_parent_station"))
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

                                    s.addTag(k, v);
                            }
                            //System.out.print(k+":" + v +" ");

                        }
//                        s.addTag(NTD_ID_KEY, OperatorInfo.getNTDID());
//                        s.addTag("url", s.getTag("stop_url"));
                     /*   if (!(s.getTag(tag_defs.GTFS_NAME_KEY).contains("platform") || s.getTag(tag_defs.GTFS_STOP_ID_KEY).contains("place")))
                        		{
                        	s.addTag("highway", "bus_stop");
                        	s.addTag("bus", "yes");
                        		}
                        if (s.getTag(tag_defs.GTFS_STOP_ID_KEY).contains("place"))
                        	s.addTag("public_transport", "station");
                        else
                        	s.addTag("public_transport", "platform");*/

//if (s.getTag("gtfs_location_type");)

// disable source tag                        s.addTag("source", "http://translink.com.au/about-translink/reporting-and-publications/public-transport-performance-data");
//                        if (!tempStopId.contains("place")) s.addTag("url", "http://translink.com.au/stop/"+tempStopId);

                    } catch(Exception e){
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


//                    System.out.println(thisLine);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
        return stops;
    }

    /*



     */
    public Hashtable<String, Route> readRoutes(String routes_fName){
        Hashtable<String, Route> routes = new Hashtable<String, Route>();
        String thisLine;
        String [] elements;
        int routeIdKey=-1, routeShortNameKey=-1,routeLongNameKey=-1;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(routes_fName),"UTF-8"));
            boolean isFirstLine = true;
            Hashtable<String,Integer> keysIndex = new Hashtable<String,Integer> ();
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    if (thisLine.startsWith(UTF8_BOM)) {
                        thisLine = thisLine.substring(1);
                    }
                    thisLine = thisLine.replace("\"", "");
                    String[] keys = thisLine.split(",");
                    //map GTFS keys to OSM keys
                    for(int i=0; i<keys.length; i++){
                        //read keys
                        switch (keys[i]) {
                            case "route_id":
                                routeIdKey = i;
                                break;
                            case tag_defs.GTFS_ROUTE_URL_KEY:
                                keysIndex.put(tag_defs.OSM_URL_KEY, i);
                                break;
                            case "route_type":
                                keysIndex.put(tag_defs.OSM_ROUTE_TYPE_KEY, i);
                                break;
                            case tag_defs.GTFS_COLOUR_KEY:
                                keysIndex.put(tag_defs.OSM_COLOUR_KEY, i);
                                break;
                            case tag_defs.GTFS_ROUTE_NUM:
                                routeShortNameKey = i;
                                break;
                            case tag_defs.GTFS_ROUTE_NAME:
                                routeLongNameKey = i;
                                break;
                            default:
                                String t = "gtfs_" + keys[i];
                                keysIndex.put(t, i);
                                break;
                        }

                    }
                    if (routeLongNameKey != -1)
                        keysIndex.put("name",routeLongNameKey);
//                    System.out.println(stopIdKey+","+stopNameKey+","+stopLatKey+","+stopLonKey);
                }
                else {
                    boolean lastIndexEmpty=false;
                    thisLine = thisLine.trim();
                    if(thisLine.contains("\"")) {
                         String[] temp = thisLine.split("\"");
                         for(int x=0; x<temp.length; x++){
                             if(x%2==1) temp[x] = temp[x].replace(",", "");
                         }
                         thisLine = "";
                         for(int x=0; x<temp.length; x++){
                             thisLine = thisLine + temp[x];
                         }
                    }
                    elements = thisLine.split(",");
                    if(thisLine.charAt(thisLine.length()-1)==',') lastIndexEmpty=true;
                    String routeName;
                    if(elements[routeShortNameKey]==null || elements[routeShortNameKey].equals("")) routeName = elements[routeIdKey];
                    else routeName = elements[routeShortNameKey];
                    Route r = new Route(elements[routeIdKey], routeName, OperatorInfo.getFullName());
                    HashSet<String> keys = new HashSet<String>();
                    keys.addAll(keysIndex.keySet());
                    Iterator<String> it = keys.iterator();
                    try {
                        while(it.hasNext()){
                            String k = it.next();
                            String v = null;
                            if(!lastIndexEmpty) v = elements[keysIndex.get(k)];
                            if ((v!=null) && (!v.equals("")))
                            {
                                if (k.equals(tag_defs.OSM_ROUTE_TYPE_KEY))
                                {
                                    String route_value;
                                    switch(Integer.parseInt(v))
                                    {
                                        // TODO allow drop down finetuning selection on report viewer
                                        // https://developers.google.com/transit/gtfs/reference/routes-file
                                        // https://wiki.openstreetmap.org/wiki/Relation:route#Route_types_.28route.29
                                        case 0: route_value = "light_rail";	break;// 0: Tram, Streetcar, Light rail. Any light rail or street level system within a metropolitan area.
                                        case 1:	route_value = "subway";     break;	// Subway, Metro. Any underground rail system within a metropolitan area.
                                        case 2: route_value = "railway";    break;	// Rail. Used for intercity or long-distance travel.
                                        case 3: route_value = "bus";        break;	// Bus. Used for short- and long-distance bus routes.
                                        case 4: route_value = "ferry";      break;	// Ferry. Used for short- and long-distance boat service.
                                        case 5: route_value = "cable_car";  break;	// Cable car. Used for street-level cable cars where the cable runs beneath the car.
                                        case 6: route_value = "gondola";    break;	// Gondola, Suspended cable car. Typically used for aerial cable cars where the car is suspended from the cable.
                                        case 7: route_value = "funicular";  break;	// Funicular. Any rail system designed for steep inclines.
                                        default: route_value = v; break;
                                    }
                                    v = route_value;
                                }
                                //prepend hex colours
//                                if (k.equals(tag_defs.OSM_COLOUR_KEY))
//                                    System.out.println(tag_defs.OSM_COLOUR_KEY + " "+ v + " #"+v);
                                if (k.equals(tag_defs.OSM_COLOUR_KEY) && ((v.length() == 3 || v.length() == 6) && v.matches("^[a-fA-F0-9]+$")) )
                                {
                                    v = "#".concat(v);
                                }


                                r.addTag(k, v);
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
        String thisLine;
        String [] elements;
        // hashtable String vs. String
        Hashtable<String,String> tripIDs = new Hashtable<String,String>();

        // trips.txt read-in
        try {
            int tripIdKey=-1, routeIdKey=-1;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trips_fName),"UTF-8"));
            boolean isFirstLine = true;
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    if (thisLine.startsWith(UTF8_BOM)) {
                        thisLine = thisLine.substring(1);
                    }
                    thisLine = thisLine.replace("\"", "");
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("route_id")) routeIdKey = i;
                        else if(keys[i].equals("trip_id")) tripIdKey = i;
                    }
                }
                else {
                    if(thisLine.contains("\"")) {
                         String[] temp = thisLine.split("\"");
                         for(int x=0; x<temp.length; x++){
                             if(x%2==1) temp[x] = temp[x].replace(",", "");
                         }
                         thisLine = "";
                         for(int x=0; x<temp.length; x++){
                             thisLine = thisLine + temp[x];
                         }
                    }
                    elements = thisLine.split(",");
                    // not sure if tripId is unique in trips.txt, e.g. can 1 trip_id has multiple route_id
                    if (tripIDs.containsKey(elements[tripIdKey])) {
                        System.out.println("Repeat "+elements[tripIdKey]);
                    }
                    tripIDs.put(elements[tripIdKey], elements[routeIdKey]);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }

        // hashtable String(stop_id) vs. HashSet(routes)
        Hashtable<String, HashSet<Route>> stopIDs = new Hashtable<String, HashSet<Route>>();
        // stop_times.txt read-in
        int stopIdKey=-1, tripIdKey = -1;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stop_times_fName),"UTF-8"));
            boolean isFirstLine = true;
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    if (thisLine.startsWith(UTF8_BOM)) {
                        thisLine = thisLine.substring(1);
                    }
                    thisLine = thisLine.replace("\"", "");
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("stop_id")) stopIdKey = i;
                        else if(keys[i].equals("trip_id")) tripIdKey = i;
                    }
                }
                else {
                    if(thisLine.contains("\"")) {
                         String[] temp = thisLine.split("\"");
                         for(int x=0; x<temp.length; x++){
                             if(x%2==1) temp[x] = temp[x].replace(",", "");
                         }
                         thisLine = "";
                         for(int x=0; x<temp.length; x++){
                             thisLine = thisLine + temp[x];
                         }
                    }
                    elements = thisLine.split(",");
                    String trip = elements[tripIdKey];
                    HashSet<Route> routes = new HashSet<Route>();
                    Route tr = null;
                    if(tripIDs.get(trip) !=null) tr = allRoutes.get(tripIDs.get(trip));
                    if(tr!=null) routes.add(tr);
                    String sid = OsmFormatter.getValidBusStopId(elements[stopIdKey]);
                    if (stopIDs.containsKey(sid)) {
                        routes.addAll(stopIDs.get(sid));
                        stopIDs.remove(sid);
                    }
                    stopIDs.put(sid, routes);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
        return stopIDs;
    }

    public String getRoutesInTextByBusStop(HashSet<Route> r) {
        String text="";
        if (r!=null) {
            ArrayList<Route> routes = new ArrayList<Route>();
            //convert from hashset to arraylist
            routes.addAll(r);
            //ordering by hashcode
            for (int i=0; i<routes.size()-1; i++) {
                int k=i;
                for (int j=i+1; j<routes.size(); j++) {
                    if (routes.get(k).getRouteRef().hashCode() > routes.get(j).getRouteRef().hashCode()) {
                        k = j;
                    }
                }
                Route temp = routes.get(i);
                routes.set(i, routes.get(k));
                routes.set(k, temp);
            }

            //to text
            for (int i=0; i<routes.size(); i++) {
                text = text + ";" + routes.get(i).getRouteRef();
            }
            //delete the 1st semi-colon
            if (!text.isEmpty()) {
                text = text.substring(1);
            }
        }
        return text;
    }



}