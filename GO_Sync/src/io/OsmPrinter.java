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

import java.util.HashSet;
import java.util.Iterator;
import object.OperatorInfo;
import object.RelationMember;
import object.Route;
import object.Session;
import object.Stop;
import tools.OsmFormatter;

public class OsmPrinter {

    public static final String DEFAULT_API_VERSION = "0.6";
    private final String APPLICATION_CREATOR_KEY = "source";
    private final String APPLICATION_CREATOR_NAME = "GO_Sync";

    public String header() {
        String s = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<osm version='" + DEFAULT_API_VERSION + "' generator='"+Session.getUserName()+"'>\n";
        return s;
    }
    public String footer() {
        return "</osm>";
    }
    public String osmChangeCreate(){
        return "<osmChange version='0.6'" + " generator='"+Session.getUserName()+"'>\n" +
                "<create>\n";
    }
    public String osmChangeModify(){
        return "</create>\n" +
                "<modify>\n";
    }
    
    public String osmChangeDelete(){
        return "</modify>\n" +
                "<delete>\n";
    }

    public String osmChangeDeleteClose(){
        return "</delete>\n" +
                "</osmChange>\n";
    }

    public String writeChangeSet() {

        return "<changeset>\n" +
                "<tag k='created_by' v='"+OsmFormatter.getValidXmlText(Session.getUserName())+"'/>\n" +
                "<tag k='comment' v='"+OsmFormatter.getValidXmlText(Session.getChangeSetComment())+"'/>\n" +
                "</changeset>\n";
    }

    public String writeDeleteNode(String nodeID, String changeSetID, String version) {
        return "<node id='"+nodeID+"' changeset='"+ changeSetID + "' version='"+ version +"' />\n";
    }

    public String writeBusStop(String changeSetID, String lat, String lon) {
        return "<node changeset='" + changeSetID+ "' lat='" + lat + "' lon='" + lon + "'>\n" +
                "<tag k='highway' v='bus_stop'/>\n" +
                "</node>";
    }

    public String writeBusStop(String changeSetID, String nodeID, Stop st) {
        String text="";
        Stop s = new Stop(st);
        // if modify, we need version number
        if(st.getOsmVersion()!=null) {
            text += "<node changeset='" + changeSetID + "' id='" + nodeID
                    + "' lat='" + st.getLat() + "' lon='" + st.getLon()
                    + "' version='"+st.getOsmVersion() + "'>\n";
        }
        // mainly for create new node
        else {
            text += "<node changeset='" + changeSetID + "' id='" + nodeID
                    + "' lat='" + st.getLat() + "' lon='" + st.getLon() + "'>\n";
            if(st.getTag(APPLICATION_CREATOR_KEY)!=null && !st.getTag(APPLICATION_CREATOR_KEY).equals("none")) {
                text += "<tag k='"+APPLICATION_CREATOR_KEY+"' v='"+APPLICATION_CREATOR_NAME+"' />\n";
            }
        }
        HashSet<String> keys = new HashSet<String>(s.keySet().size());
        keys.addAll(s.keySet());
        Iterator it = keys.iterator();
        while (it.hasNext()){
            String k = (String) it.next();
            if (!s.getTag(k).equals("none")) {
                text += "<tag k='"+OsmFormatter.getValidXmlText(k)+"' v='"+OsmFormatter.getValidXmlText(s.getTag(k))+"' />\n";
            }
        }
        text += "</node>\n";
        return text;
    }

    public String writeBusRoute(String changeSetID, String routeID, Route r) {
        String text="";
        Route route = new Route(r);
        // if modify, we need version number
        if(r.getOsmVersion()!=null) {
            text += "<relation changeset='" + changeSetID + "' id='" + routeID
                    + "' version='"+route.getOsmVersion() + "'>\n";
        }
        // mainly for create new node
        else {
            text += "<relation changeset='" + changeSetID + "' id='" + routeID
                    + "' version='"+ routeID +"'>\n";
            text += "<tag k='"+APPLICATION_CREATOR_KEY+"' v='"+APPLICATION_CREATOR_NAME+"' />\n";
        }
        //add member
        HashSet<RelationMember> members = route.getOsmMembers();
        Iterator it = members.iterator();
        while (it.hasNext()){
            RelationMember rm = (RelationMember) it.next();
            if(rm.getRole()!=null) text += "<member type='"+rm.getType()+"' ref='"+rm.getRef()+
                    "' role='"+OsmFormatter.getValidXmlText(rm.getRole())+"' />\n";
            else text += "<member type='"+rm.getType()+"' ref='"+rm.getRef()+"' role='' />\n";
        }
        //add tag
        route.addTag("name", OsmFormatter.getValidXmlText(OperatorInfo.getAbbreviateName())+
                                " route "+OsmFormatter.getValidXmlText(route.getRouteRef()));
        route.addTag("operator",OsmFormatter.getValidXmlText( OperatorInfo.getFullName()));
        route.addTag("ref", route.getRouteRef());
        route.addTag("route", "bus");
        route.addTag("type", "route");
        HashSet<String> keys = new HashSet<String>(route.keySet().size());
        keys.addAll(route.keySet());
        it = keys.iterator();
        while (it.hasNext()){
            String k = (String) it.next();
            if (!route.getTag(k).equals("none")) {
                text += "<tag k='"+OsmFormatter.getValidXmlText(k)+
                        "' v='"+OsmFormatter.getValidXmlText(route.getTag(k))+"' />\n";
            }
        }
        text += "</relation>\n";
        return text;
    }
}