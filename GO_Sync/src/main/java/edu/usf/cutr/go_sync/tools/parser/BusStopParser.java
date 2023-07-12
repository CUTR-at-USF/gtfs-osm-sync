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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Khoa Tran
 */
public class BusStopParser extends DefaultHandler{
    private Hashtable tempTag;
    private NodeWayAttr attImplNode;
    private ArrayList<NodeWayAttr> xmlNodeWays;
    private ArrayList<Hashtable> xmlTags;
    private ArrayList<String> wayNds;
    private String firstNodeRefOfWay;
    private int nodeOfWayItemCount = 0;
    private Map <String, String> wayNodeMap = new HashMap<>();
    private ArrayList<String> allNodesReferencedByWays = new ArrayList<>();

    private Map <String, String> nodeLatMap = new HashMap<>();
    private Map <String, String> nodeLonMap = new HashMap<>();

    public BusStopParser(){
        xmlNodeWays = new ArrayList<NodeWayAttr>();
        xmlTags = new ArrayList<Hashtable>();
    }
    
    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("node") || qname.equals("way") || qname.equals("changeset")) {
            attImplNode = new NodeWayAttr(attributes, qname);
            tempTag = new Hashtable();      // start to collect tags of that node
            wayNds = new ArrayList<>();      // start to collect tags of that node
        }
        if (qname.equals("node")) {
            nodeLatMap.put(attributes.getValue("id"), attributes.getValue("lat"));
            nodeLonMap.put(attributes.getValue("id"), attributes.getValue("lon"));
        }
        if (qname.equals("nd")) {
            if (nodeOfWayItemCount == 0) {
                firstNodeRefOfWay = attributes.getValue("ref");
            }
            allNodesReferencedByWays.add(attributes.getValue("ref"));
            wayNds.add(attributes.getValue("ref"));
            nodeOfWayItemCount++;
        }
        if (qname.equals("tag")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            tempTag.put(attImpl.getValue("k"), attImpl.getValue("v"));         // insert key and value of that tag into Hashtable
        }
    }

    @Override public void endElement (String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            nodeOfWayItemCount = 0;
            wayNodeMap.put(attImplNode.getValue("id"), firstNodeRefOfWay);
            //System.out.println("Adding way: " + attImplNode.getValue("id"));
            attImplNode.setWayNds(wayNds);
        }
        if (qName.equals("node") || qName.equals("way") || qName.equals("changeset")) {
            xmlNodeWays.add(attImplNode);
            xmlTags.add(tempTag);
            //System.out.println("Adding '" + qName + "' object: " + attImplNode.getValue("id"));
        }
    }

    public NodeWayAttr getOneNode(){
        return attImplNode;
    }

    public Hashtable getTagsOneNode(){
        return tempTag;
    }

    public ArrayList<NodeWayAttr> getNodes(){
        return xmlNodeWays;
    }

    public ArrayList<Hashtable> getTags(){
        return xmlTags;
    }

    public ArrayList<String> getWayNds() {
        return allNodesReferencedByWays;
    }

    @Override
    public void endDocument() throws SAXException {

        ArrayList<Integer> indexOfNodesToRemove = new ArrayList<>();

        for (NodeWayAttr nodeway : xmlNodeWays) {
            // Set lat/lon of the 1st node of each way
            if (nodeway.osmPrimitiveType.equals("way")) {
                String wayId = nodeway.getValue("id");
                String nodeRef = wayNodeMap.get(wayId);
                String lat = nodeLatMap.get(nodeRef);
                String lon = nodeLonMap.get(nodeRef);

//                System.out.println(
//                        "Processing way: " + wayId
//                        + " - 1st node ref: " + nodeRef
//                        + " - lat: " + lat
//                        + " - lon: " + lon
//                );
                nodeway.setLat(lat);
                nodeway.setLon(lon);
            }

            if (allNodesReferencedByWays.contains(nodeway.getValue("id"))) {
                indexOfNodesToRemove.add(xmlNodeWays.indexOf(nodeway));
            }
        }

        // Remove nodes that are part of a way
        for (int i = xmlNodeWays.size() - 1; i >= 0; i--) {
            if (indexOfNodesToRemove.contains(i)) {
                // Don't remove node if it has a public_transport=platform tag
                if (xmlTags.get(i).containsKey("public_transport") && xmlTags.get(i).get("public_transport").equals("platform")) {
                    System.out.println("Not removing node " + i + " of lat: " + xmlNodeWays.get(i).getLat());
                    continue;
                }
                NodeWayAttr n = xmlNodeWays.remove(i);
                xmlTags.remove(i);
                //System.out.println("Removing index: " + i + " - nodeId: " + n.getValue("id"));
            }
        }

        //printNodeWayContent();
    }

    public void printNodeWayContent() {
        System.out.println("All nodes/ways after parsing:");
        for (NodeWayAttr nodeway : xmlNodeWays) {
            System.out.println(nodeway.toString());
        }
    }
}
