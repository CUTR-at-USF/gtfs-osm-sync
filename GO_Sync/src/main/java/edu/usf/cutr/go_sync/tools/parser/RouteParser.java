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

package edu.usf.cutr.go_sync.tools.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import edu.usf.cutr.go_sync.object.RelationMember;
import java.util.HashMap;
import java.util.LinkedHashSet;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Khoa Tran
 */
public class RouteParser extends DefaultHandler {
    private Hashtable tempTag;
    private LinkedHashSet<RelationMember> tempMembers;
    private ArrayList<AttributesImpl> xmlRelations;
    //xmlTags<String, String> ----------- xmlMembers<String(refID), AttributesImpl>
    private ArrayList<Hashtable> xmlTags;
    private ArrayList<LinkedHashSet<RelationMember>> xmlMembers;
    private HashMap<String, String> nodes, ways, nodeWayPtValue;
    boolean inWay, inNode;
    String nodeWayId;
    int ndcount;

    public RouteParser() {
        xmlRelations = new ArrayList<AttributesImpl>();
        xmlTags = new ArrayList<Hashtable>();
        xmlMembers = new ArrayList<LinkedHashSet<RelationMember>>();
        nodes = new HashMap<>();
        ways = new HashMap<>();
        nodeWayPtValue = new HashMap<>();
        inWay = false;
        inNode = false;
        ndcount = 0;
    }

    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("relation")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            xmlRelations.add(attImpl);
            tempTag = new Hashtable();      // start to collect tags of that relation
            tempMembers = new LinkedHashSet<RelationMember>();
        }
        if (qname.equals("tag")) {
            if (tempTag != null) {
                AttributesImpl attImpl = new AttributesImpl(attributes);
                tempTag.put(attImpl.getValue("k"), attImpl.getValue("v"));         // insert key and value of that tag into Hashtable
            }
            if (inNode || inWay) {
                AttributesImpl attImpl = new AttributesImpl(attributes);
                if (attImpl.getValue("k").equals("public_transport")) {
                    nodeWayPtValue.put(nodeWayId, attImpl.getValue("v"));
                }
            }
        }
        if (tempMembers!=null && qname.equals("member")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            RelationMember rm = new RelationMember(attImpl.getValue("ref"), attImpl.getValue("type"), attImpl.getValue("role"), "", "", "unknown");
            rm.setStatus("OSM server");
            tempMembers.add(rm);
        }
        if (qname.equals("node")) {
            inNode = true;
            AttributesImpl attImpl = new AttributesImpl(attributes);
            nodeWayId = attImpl.getValue("id");
            nodes.put(attImpl.getValue("id"), attImpl.getValue("lat") + ";" + attImpl.getValue("lon"));
        }
        if (qname.equals("way")) {
            inWay = true;
            AttributesImpl attImpl = new AttributesImpl(attributes);
            nodeWayId = attImpl.getValue("id");
        }
        if (qname.equals("nd") && inWay && ndcount == 0) {
            ndcount++;
            AttributesImpl attImpl = new AttributesImpl(attributes);
            ways.put(nodeWayId, attImpl.getValue("ref"));
        }
    }

    @Override public void endElement (String uri, String localName, String qName) throws SAXException {
        if (qName.equals("relation")) {
            xmlTags.add(tempTag);
            xmlMembers.add(tempMembers);
            tempTag = null;
            tempMembers = null;
        }
        if (qName.equals("way")) {
            inWay = false;
            ndcount = 0;
        }
        if (qName.equals("node")) {
            inNode = false;
            ndcount = 0;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        HashMap<String, String> waysWithGeo = new HashMap<>();
        for (HashMap.Entry<String, String> way : ways.entrySet()) {
            waysWithGeo.put(way.getKey(), nodes.get(way.getValue()));
        }

        for (LinkedHashSet<RelationMember> hs : xmlMembers) {
            for (RelationMember m : hs) {
                String nodeId = nodes.get(m.getRef());
                //System.out.println(String.format("Id: %s / nodeId: %s", m.getRef(), nodeId));
                if (nodeId != null) {
                    m.setLat(nodeId.split(";")[0]);
                    m.setLon(nodeId.split(";")[1]);
                } else {
                    String wayId = waysWithGeo.get(m.getRef());
                    //System.out.println(String.format("Id: %s / wayId: %s", m.getRef(), wayId));
                    if (wayId != null) {
                        m.setLat(wayId.split(";")[0]);
                        m.setLon(wayId.split(";")[1]);
                    }
                }
                if (nodeWayPtValue.containsKey(m.getRef())) {
                    m.setRefOsmPublicTransportType(nodeWayPtValue.get(m.getRef()));
                }
            }
        }
    }

    public ArrayList<AttributesImpl> getRelations(){
        return xmlRelations;
    }

    public ArrayList<Hashtable> getTags(){
        return xmlTags;
    }

    public ArrayList<LinkedHashSet<RelationMember>> getMembers(){
        return xmlMembers;
    }
}