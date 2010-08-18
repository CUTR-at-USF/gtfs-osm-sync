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
package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import object.Stop;
import tools.OsmFormatter;

public class GTFSReadIn {
    private List<Stop> stops;
    private final String ROUTE_KEY = "route_ref";
    private HashSet<String> allRoutes;

    public List<Stop> readBusStop(String fName, String agencyName, String trips_fName, String stop_times_fName){
        Hashtable stopIDs = new Hashtable();
        Hashtable id = readRoutes(trips_fName, stop_times_fName);
        stopIDs.putAll(id);
        
        String thisLine;
        String [] elements;
        int stopIdKey=-1, stopNameKey=-1, stopLatKey=-1, stopLonKey=-1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fName));
            boolean isFirstLine = true;
            Hashtable keysIndex = new Hashtable();
            while ((thisLine = br.readLine()) != null) { 
                if (isFirstLine) {
                    isFirstLine = false;
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("stop_id")) stopIdKey = i;
                        else if(keys[i].equals("stop_name")) stopNameKey = i;
                        else if(keys[i].equals("stop_lat")) stopLatKey = i;
                        else if(keys[i].equals("stop_lon")) stopLonKey = i;
                        // gtfs stop_url is mapped to source_ref tag in OSM
                        else if(keys[i].equals("stop_url")){
                            keysIndex.put("source_ref", i);
                        }
                        else {
                            String t = "gtfs_"+keys[i];
                            keysIndex.put(t, i);
                        }
                    }
//                    System.out.println(stopIdKey+","+stopNameKey+","+stopLatKey+","+stopLonKey);
                }
                else {
                    elements = thisLine.split(",");
                    //add leading 0's to gtfs_id
                    String tempStopId = OsmFormatter.getValidBusStopId(elements[stopIdKey]);
                    Stop s = new Stop(tempStopId, agencyName, elements[stopNameKey],elements[stopLatKey],elements[stopLonKey]);
                    HashSet<String> keys = new HashSet<String>();
                    keys.addAll(keysIndex.keySet());
                    Iterator it = keys.iterator();
                    while(it.hasNext()){
                        String k = (String)it.next();
                        String v = elements[(Integer)keysIndex.get(k)];
                        if ((v!=null) && (!v.equals(""))) s.addTag(k, v);
                    }
                    String r = getRoutesInTextByBusStop((HashSet<String>)stopIDs.get(elements[stopIdKey]));
                    if (!r.isEmpty()) s.addTag(ROUTE_KEY, r);
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

    public Hashtable readRoutes(String trips_fName, String stop_times_fName){
        String thisLine;
        String [] elements;
        // hashtable String vs. String
        Hashtable tripIDs = new Hashtable();

        // trips.txt read-in
        try {
            int tripIdKey=-1, routeIdKey=-1;
            BufferedReader br = new BufferedReader(new FileReader(trips_fName));
            boolean isFirstLine = true;
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("route_id")) routeIdKey = i;
                        else if(keys[i].equals("trip_id")) tripIdKey = i;
                    }
                }
                else {
                    elements = thisLine.split(",");
                    // not sure if tripId is unique in trips.txt, e.g. can 1 trip_id has multiple route_id
                    if (tripIDs.containsKey(elements[tripIdKey])) {
                        System.out.println("Repeat "+elements[tripIdKey]);
                    }
                    tripIDs.put(elements[tripIdKey], elements[routeIdKey]);
                    allRoutes.add(elements[routeIdKey]);
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
            BufferedReader br = new BufferedReader(new FileReader(stop_times_fName));
            boolean isFirstLine = true;
            while ((thisLine = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    String[] keys = thisLine.split(",");
                    for(int i=0; i<keys.length; i++){
                        if(keys[i].equals("stop_id")) stopIdKey = i;
                        else if(keys[i].equals("trip_id")) tripIdKey = i;
                    }
                }
                else {
                    elements = thisLine.split(",");
                    String trip = elements[tripIdKey];
                    HashSet<String> routes = new HashSet<String>();
                    routes.add((String)tripIDs.get(trip));
                    if (stopIDs.containsKey(elements[stopIdKey])) {
                        routes.addAll((HashSet<String>)stopIDs.get(elements[stopIdKey]));
                        stopIDs.remove(elements[stopIdKey]);
                    }
                    stopIDs.put(elements[stopIdKey], routes);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
        return stopIDs;
    }

    public String getRoutesInTextByBusStop(HashSet<String> r) {
        String text="";
        if (r!=null) {
            ArrayList<String> routes = new ArrayList<String>();
            //convert from hashset to arraylist
            routes.addAll(r);
            //ordering by hashcode
            for (int i=0; i<routes.size()-1; i++) {
                int k=i;
                for (int j=i+1; j<routes.size(); j++) {
                    if (routes.get(k).hashCode() > routes.get(j).hashCode()) {
                        k = j;
                    }
                }
                String temp = routes.get(i);
                routes.set(i, routes.get(k));
                routes.set(k, temp);
            }

            //to text
            for (int i=0; i<routes.size(); i++) {
                text = text + ";" + routes.get(i);
            }
            //delete the 1st semi-colon
            if (!text.isEmpty()) {
                text = text.substring(1);
            }
        }
        return text;
    }

    public HashSet<String> getAllRoutesID(){
        System.out.println(allRoutes);
        return allRoutes;
    }
    
    public GTFSReadIn() {
        stops = new ArrayList<Stop>();
        allRoutes = new HashSet<String>();
//        readBusStop("C:\\Users\\Khoa Tran\\Desktop\\Summer REU\\Khoa_transit\\stops.txt");
    }
}