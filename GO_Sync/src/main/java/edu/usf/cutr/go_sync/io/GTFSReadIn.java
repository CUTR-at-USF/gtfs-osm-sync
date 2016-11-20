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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class GTFSReadIn {
    private List<Stop> stops;
    private final String ROUTE_KEY = "route_ref";
    private final String NTD_ID_KEY = "ntd_id";
    private static Hashtable<String, Route> allRoutes;
//TODO read agency.txt
    
    public String readAgency(String agency_fName)
    //public Hashtable<String, Route> readRoutes(String routes_fName)
    {
        Hashtable<String, Route> routes = new Hashtable<String, Route>();
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
                            if(keys[i].equals("agency_name")) agencyNameKey = i;
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
                    String angencyName;
                    if(elements[agencyNameKey]==null || elements[agencyNameKey].equals("")) angencyName = elements[agencyIdKey];
                    else angencyName = elements[agencyNameKey];
                    
                    br.close();
                    return angencyName;
                    /*
                    Route r = new Route(elements[agencyIdKey], angencyName, OperatorInfo.getFullName());
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
        Hashtable id = matchRouteToStop(routes_fName, trips_fName, stop_times_fName);
        stopIDs.putAll(id);
        
        String thisLine;
        String [] elements;
        int stopIdKey=-1, stopNameKey=-1, stopLatKey=-1, stopLonKey=-1;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fName),"UTF-8"));
            boolean isFirstLine = true;
            Hashtable keysIndex = new Hashtable();
            while ((thisLine = br.readLine()) != null) 
            { 
                if (isFirstLine) {
                    isFirstLine = false;
                    OperatorInfo.setGtfsFields(thisLine);
                    thisLine = thisLine.replace("\"", "");
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("stop_id")) stopIdKey = i;
                        else if(keys[i].equals("stop_name")) stopNameKey = i;
                        else if(keys[i].equals("stop_lat")) stopLatKey = i;
                        else if(keys[i].equals("stop_lon")) stopLonKey = i;
                        // gtfs stop_url is mapped to url tag in OSM
                        else if(keys[i].equals("stop_url")){
                            keysIndex.put("url", i);
                        }
                        else if(keys[i].equals("zone_id")){
                            keysIndex.put("transport:zone", i);
                        }
                        else {
                            String t = "gtfs_"+keys[i];
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
                            if((Integer)keysIndex.get(k)< elements.length) v = elements[(Integer)keysIndex.get(k)];
                            if ((v!=null) && (!v.equals(""))) s.addTag(k, v);
                            
                            //System.out.print(k+":" + v +" ");
                            
                        }
//                        s.addTag(NTD_ID_KEY, OperatorInfo.getNTDID());
//                        s.addTag("url", s.getTag("stop_url"));
                        if (!(s.getTag(tag_defs.GTFS_NAME_KEY).contains("platform") || s.getTag(tag_defs.GTFS_STOP_ID_KEY).contains("place")))
                        		{
                        	s.addTag("highway", "bus_stop");
                        	s.addTag("bus", "yes");
                        		}
                        if (s.getTag(tag_defs.GTFS_STOP_ID_KEY).contains("place"))
                        	s.addTag("public_transport", "station");
                        else
                        	s.addTag("public_transport", "platform");
                        
//if (s.getTag("gtfs_location_type");)
                        
// disable source tag                        s.addTag("source", "http://translink.com.au/about-translink/reporting-and-publications/public-transport-performance-data");
//                        if (!tempStopId.contains("place")) s.addTag("url", "http://translink.com.au/stop/"+tempStopId);
                        
                    } catch(Exception e){
                        System.out.println("Error occurred! Please check your GTFS input files");
                        System.out.println(e.toString());
                        System.exit(0);
                    }
                   // System.err.println(s.getTags());
                    String r = getRoutesInTextByBusStop((HashSet<Route>)stopIDs.get(tempStopId));
                    
//             disable route tagging for now      
                    if (!r.isEmpty()) s.addTag(ROUTE_KEY, r);
                    HashSet<Route> asdf = (HashSet<Route>)stopIDs.get(tempStopId);
                    if(asdf!=null)s.addRoutes((HashSet<Route>)stopIDs.get(tempStopId));
                    
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

    public Hashtable<String, Route> readRoutes(String routes_fName){
        Hashtable<String, Route> routes = new Hashtable<String, Route>();
        String thisLine;
        String [] elements;
        int routeIdKey=-1, routeShortNameKey=-1,routeLongNameKey=-1;;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(routes_fName),"UTF-8"));
            boolean isFirstLine = true;
            Hashtable<String,Integer> keysIndex = new Hashtable<String,Integer> ();
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    thisLine = thisLine.replace("\"", "");
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("route_id")) routeIdKey = i;
                        else if(keys[i].equals("route_url")){
                            keysIndex.put("url", i);
                        }
                        else {
                            if(keys[i].equals("route_short_name")) routeShortNameKey = i;
                            if(keys[i].equals("route_long_name")) routeLongNameKey = i;
                            String t = "gtfs_"+keys[i];
                            keysIndex.put(t, i);
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
                    Iterator it = keys.iterator();
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
        Hashtable tripIDs = new Hashtable();

        // trips.txt read-in
        try {
            int tripIdKey=-1, routeIdKey=-1;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trips_fName),"UTF-8"));
            boolean isFirstLine = true;
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
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
        Hashtable stopIDs = new Hashtable();
        // stop_times.txt read-in
        int stopIdKey=-1, tripIdKey = -1;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stop_times_fName),"UTF-8"));
            boolean isFirstLine = true;
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
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
                    if((String)tripIDs.get(trip)!=null) tr = (Route)allRoutes.get((String)tripIDs.get(trip));
                    if(tr!=null) routes.add(tr);
                    String sid = OsmFormatter.getValidBusStopId(elements[stopIdKey]);
                    if (stopIDs.containsKey(sid)) {
                        routes.addAll((HashSet<Route>)stopIDs.get(sid));
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

    public static Set<String> getAllRoutesID(){
        return allRoutes.keySet();
    }
    
    public GTFSReadIn() {
        stops = new ArrayList<Stop>();
        allRoutes = new Hashtable<String, Route>();
//        readBusStop("C:\\Users\\Khoa Tran\\Desktop\\Summer REU\\Khoa_transit\\stops.txt");
    }
}