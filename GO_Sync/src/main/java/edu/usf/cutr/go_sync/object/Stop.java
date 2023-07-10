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

package edu.usf.cutr.go_sync.object;

import java.util.List;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import edu.usf.cutr.go_sync.tools.OsmDistance;
import edu.usf.cutr.go_sync.tag_defs;
import java.util.ArrayList;
import java.util.Arrays;
/**
 *
 * @author Khoa Tran
 */

public class Stop extends OsmPrimitive implements Comparable{
	private final double ERROR_TO_ZERO = 0.5;	
    private String lat, lon;
    private HashSet<Route> routes;
    public Stop(String osmPrimitiveType, String stopID, String operatorName, String stopName, String lat, String lon,
            NetexStopElement netexStopElement) {
        super(osmPrimitiveType);
        osmTags = new Hashtable();
        if (operatorName == null || operatorName.equals("")) operatorName="none";
        if (stopID == null || stopID.equals("")) stopID="none";
        if (stopName == null || stopName.equals("")) stopName="none";
//        osmTags.put("highway", "bus_stop");
        osmTags.put(tag_defs.OSM_STOP_ID_KEY, stopID);
        osmTags.put(tag_defs.OSM_NETWORK_KEY, operatorName);

        //System.out.println("Creating stop " + stopID.toString());
        // Use Quay Name of NetEx instead of GTFS (netexQuayName is null if not created with GTFSReadIn)
        if (netexStopElement != null) {
            osmTags.put(tag_defs.OSM_STOP_NAME_KEY, netexStopElement.getLogicalName());
            // Get alt_names
            List<String> altNames = netexStopElement.getLogicalAltNames();
            // Add gtfs stop name to alt_name if it is different than the logicalName from Netex
            if (!stopName.equals(netexStopElement.getLogicalName()) && !netexStopElement.getLogicalAltNames().isEmpty()) {
                if (!altNames.contains(stopName)) {
                    altNames.add(stopName.replace(";", "_"));
                }
            }
            if (!altNames.isEmpty()) {
                osmTags.put("alt_name", String.join(";", altNames));
            }
        } else {
            osmTags.put(tag_defs.OSM_STOP_NAME_KEY, stopName);
        }

        //        osmTags.put("bus", "yes");
//        osmTags.put("public_transport", "plaform");

//        if (!stopID.contains("place")) osmTags.put("url", "http://translink.com.au/stop/"+stopID);
//        osmTags.put("url", "http://translink.com.au/stop/"+stopID);
//        osmTags.put("source", "http://translink.com.au/about-translink/reporting-and-publications/public-transport-performance-data");
        
//       osmTags.put("network", getOperatorName());
//      osmTags.put(tag_defs.OSM_NETWORK_KEY, "");
        this.lat = lat;
        this.lon = lon;
        routes = new HashSet<Route>();
    }

    public Stop(Stop s) {
        super(s.primitiveType);
        this.osmTags = new Hashtable();
        this.osmTags.putAll(s.osmTags);
//        this.osmTags.put("highway", "bus_stop");

        this.osmTags.put(tag_defs.OSM_STOP_ID_KEY, s.getStopID());
        this.osmTags.put(tag_defs.OSM_NETWORK_KEY, s.getOperatorName());
        this.osmTags.put(tag_defs.OSM_STOP_NAME_KEY, s.getStopName());
        if (s.getStopAltName() != null) {
            this.osmTags.put("alt_name", s.getStopAltName());
        }
        
//        this.osmTags.put("url", "http://translink.com.au/stop/"+s.getStopID());
//        if (!s.getStopID().contains("place")) this.osmTags.put("url", "http://translink.com.au/stop/"+s.getStopID()); 
//        this.osmTags.put("url", s.getTag("source_ref"));
//        System.out.println(s.getTag("source_ref"));
//        this.osmTags.put("source", "http://translink.com.au/about-translink/reporting-and-publications/public-transport-performance-data");

        
//        this.osmTags.put("network", s.getOperatorName());
//        this.osmTags.put(tag_defs.OSM_NETWORK_KEY, "");
        this.lat = s.lat;
        this.lon = s.lon;
        this.setOsmId(s.getOsmId());
        this.setOsmVersion(s.getOsmVersion());
        this.setReportCategory(s.getReportCategory());
        this.setReportText(s.getReportText());
        this.setStatus(s.getStatus());
        this.setLastEditedOsmDate(s.getLastEditedOsmDate());
        this.setLastEditedOsmUser(s.getLastEditedOsmUser());
        this.wayNdRefs.addAll(s.wayNdRefs);
        routes = new HashSet<Route>();
        routes.addAll(s.getRoutes());
    }

    public void addRoutes(HashSet<Route> r){
        routes.addAll(r);
    }

    public HashSet<Route> getRoutes(){
        return routes;
    }

    public void setLat(String v){
        lat = v;
    }

    public void setLon(String v){
        lon = v;
    }

    public String getStopID(){
        return (String)osmTags.get(tag_defs.OSM_STOP_ID_KEY);
    }

    public String getOperatorName(){
        return (String)osmTags.get(tag_defs.OSM_NETWORK_KEY);
    }

    public String getStopName(){
        return (String)osmTags.get(tag_defs.OSM_STOP_NAME_KEY);
    }

    public String getStopAltName(){
        return (String)osmTags.get("alt_name");
    }

    public static List<String> stopAltNamesToList(String alt_names) {
        if (alt_names == null)
            return null;

        String[] altNamesArray = alt_names.split(";");
        return new ArrayList<>(Arrays.asList(altNamesArray));
    }

    public String getLat(){
        return lat;
    }

    public String getLon(){
        return lon;
    }

    public boolean compareOperatorName(Stop o) {
        if ((!this.getOperatorName().equals("none")) && (!o.getOperatorName().equals("none"))) {
            return OperatorInfo.isTheSameOperator(this.getOperatorName())
                    && OperatorInfo.isTheSameOperator(o.getOperatorName());
        }
        else if ((this.getOperatorName().equals("none")) && (o.getOperatorName().equals("none"))) {
            return true;
        }
        return false;
    }

    public int compareTo(Object o){
        Stop s = (Stop) o;
        if (!(this.getStopID().equals("none")) && !(this.getStopID().equals("missing"))
                && (!(s.getStopID().equals("none"))) && (!(s.getStopID().equals("missing")))
                && (!(this.getStopID().equals("New"))) && (!(s.getStopID().equals("New")))
                && (!this.getOperatorName().equals("none")) && (!s.getOperatorName().equals("none"))
                && (!this.getOperatorName().equals("missing")) && (!s.getOperatorName().equals("missing"))) {
            if ((s.getStopID().equals(this.getStopID())) && (this.compareOperatorName(s))) {
                return 0;
            }
        } else if (this.getStopID().equals("New") || s.getStopID().equals("New")) {
            if ((s.getStopID().equals(this.getStopID()))) {
                return 0;
            }
            return 1;
        } else {
            double distance = OsmDistance.distVincenty(this.getLat(), this.getLon(),
                    s.getLat(), s.getLon());
            if (distance < ERROR_TO_ZERO) {
                return 0;
            }
        }
        return 1;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Stop) {
            if (this.compareTo((Stop) o)==0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        String id = this.getStopID();
        return id.hashCode();
    }

    public String printOSMStop(){
        String temp="";
        Stop st = new Stop(this);
        if (st.getOsmId()!=null) {
            temp = temp+"node_id:"+st.getOsmId()+";name:"+st.getStopName()+";lat:"+
                    st.getLat()+";lon:"+st.getLon();
        }
        else {
            temp = temp+"name:"+st.getStopName()+";lat:"+
                    st.getLat()+";lon:"+st.getLon();
        }
        HashSet<String> keys = st.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String k = (String)it.next();
            temp = temp+";"+k+":"+st.getTag(k);
        }
        return temp;
    }

    @Override
    public String toString(){
        return this.getStopID();
    }

    public String getOsmPublicTransportType() {
        return (String) osmTags.get(tag_defs.OSM_STOP_TYPE_KEY);
    }
}
