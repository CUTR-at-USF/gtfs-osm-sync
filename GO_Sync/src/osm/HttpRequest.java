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
package osm;

import io.OsmPrinter;
import io.WriteFile;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import object.Stop;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import object.Route;
import object.Session;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sun.misc.BASE64Encoder;

/**
 *
 * @author Khoa Tran
 */
public class HttpRequest {
    private static final String API_VERSION ="0.6";
    private static final String SERVER_URL = "http://api.openstreetmap.org/api/0.6/";
    
    private ArrayList<AttributesImpl> existingNodes = new ArrayList<AttributesImpl>();
    private ArrayList<AttributesImpl> existingRelations = new ArrayList<AttributesImpl>();
    private ArrayList<Hashtable> existingBusTags = new ArrayList<Hashtable>();
    private ArrayList<Hashtable> existingRelationTags = new ArrayList<Hashtable>();
    private ArrayList<HashSet<String>> existingRelationMembers = new ArrayList<HashSet<String>>();

    private boolean isSupportVersion = false;

    private OsmPrinter oprinter = new OsmPrinter();

    private String cSetID="";

    public static final String FILE_NAME_OUT_UPLOAD = "OSM_CHANGE_XML.txt";
    
    private class ApiVersionParser extends DefaultHandler {
        @Override public void startElement(String namespaceURI, String localName, String qname, Attributes atts) throws SAXException {
            if (qname.equals("version")) {
                AttributesImpl attImpl = new AttributesImpl(atts);
                double minVersion = Double.parseDouble(attImpl.getValue("minimum"));
                double maxVersion = Double.parseDouble(attImpl.getValue("maximum"));
                double currVersion = Double.parseDouble(API_VERSION);
                if (minVersion<=currVersion && maxVersion>=currVersion) {
                    isSupportVersion = true;
                }
            }
        }
    }

    public void checkVersion() {
        String url = SERVER_URL + "capabilities/";

        String s = sendRequest(url, "GET", "");

        try {
            InputSource inputSource = new InputSource(new StringReader(s));
            SAXParserFactory.newInstance().newSAXParser().parse(inputSource, new ApiVersionParser());
            if (!isSupportVersion) {
                System.out.println("The current api does not support version " + API_VERSION);
            }
        } catch(IOException e) {
            System.out.println(e);
        } catch(SAXException e) {
            System.out.println(e);
        } catch(ParserConfigurationException e) {
            System.out.println(e);
        }
    }

    //    ArrayList<AttributeList> existingTags = new ArrayList<AttributeList>();
    private class NodeParser extends DefaultHandler {
        Hashtable tempTag;
        private ArrayList<AttributesImpl> xmlNodes;
        private ArrayList<Hashtable> xmlTags;
        public NodeParser(){
            xmlNodes = new ArrayList<AttributesImpl>();
            xmlTags = new ArrayList<Hashtable>();
        }
        @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
            if (qname.equals("node") || qname.equals("changeset")) {
                AttributesImpl attImpl = new AttributesImpl(attributes);
                xmlNodes.add(attImpl);
                tempTag = new Hashtable();      // start to collect tags of that node
            }
            if (qname.equals("tag")) {
                AttributesImpl attImpl = new AttributesImpl(attributes);
//                System.out.println(attImpl.getValue("k") + attImpl.getValue("v"));
                tempTag.put(attImpl.getValue("k"), attImpl.getValue("v"));         // insert key and value of that tag into Hashtable
            }
        }

        @Override public void endElement (String uri, String localName, String qName) throws SAXException {
            if (qName.equals("node")) {
                xmlTags.add(tempTag);
            }
        }

        public ArrayList<AttributesImpl> getNodes(){
            return xmlNodes;
        }

        public ArrayList<Hashtable> getTags(){
            return xmlTags;
        }
    }

    public ArrayList<AttributesImpl> getExistingBusStops(String left, String bottom, String right, String top) {
//        http://www.informationfreeway.org/api/0.6/node[highway=bus_stop][bbox=-82.4269,28.0534,-82.4011,28.0699]
        String urlSuffix = "/api/0.6/node[highway=bus_stop][bbox="+left+","+bottom+","+right+","+top+"]";
        String url = "http://www.informationfreeway.org" + urlSuffix;
        try {
            // get data from server
            String s = sendRequest(url, "GET", "");
            InputSource inputSource = new InputSource(new StringReader(s));
            // get data from file - need to remove this for REAL APPLICATION
//            InputSource inputSource = new InputSource("DataFromServer.osm");
            NodeParser par = new NodeParser();
            SAXParserFactory.newInstance().newSAXParser().parse(inputSource, par);
            existingNodes.addAll(par.getNodes());
            existingBusTags.addAll(par.getTags());

        } catch(IOException e) {
            System.out.println(e);
        } catch(SAXException e) {
            System.out.println(e);
        } catch(ParserConfigurationException e) {
            System.out.println(e);
        }
        if (existingNodes.size()!=0) return existingNodes;
        System.out.println("null nodes");
        return null;
    }

    // this method needs to be invoked after getExistingBusStops
    public ArrayList<Hashtable> getExistingBusStopsTags(){
        System.out.println("tags = "+existingBusTags.size());
        if (existingBusTags.size() !=0 )
            return existingBusTags;
        return null;
    }

    private class RelationParser extends DefaultHandler {
        private Hashtable tempTag;
        private HashSet<String> tempMember;
        private ArrayList<AttributesImpl> xmlRelations;
        //xmlTags<String, String> ----------- xmlMembers<String(refID), AttributesImpl>
        private ArrayList<Hashtable> xmlTags;
        private ArrayList<HashSet<String>> xmlMembers;
        public RelationParser(){
            xmlRelations = new ArrayList<AttributesImpl>();
            xmlTags = new ArrayList<Hashtable>();
            xmlMembers = new ArrayList<HashSet<String>>();
        }
        @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
            if (qname.equals("relation")) {
                AttributesImpl attImpl = new AttributesImpl(attributes);
                xmlRelations.add(attImpl);
                tempTag = new Hashtable();      // start to collect tags of that relation
                tempMember = new HashSet<String>();
            }
            if (tempTag!=null && qname.equals("tag")) {
                AttributesImpl attImpl = new AttributesImpl(attributes);
                tempTag.put(attImpl.getValue("k"), attImpl.getValue("v"));         // insert key and value of that tag into Hashtable
            }
            if (tempMember!=null && qname.equals("member")) {
                AttributesImpl attImpl = new AttributesImpl(attributes);
                if (attImpl.getValue("type").equals("node")) {                     // only need bus_stop, which is node. [no member way]
                    tempMember.add(attImpl.getValue("ref"));
                }
            }
        }

        @Override public void endElement (String uri, String localName, String qName) throws SAXException {
            if (qName.equals("relation")) {
                xmlTags.add(tempTag);
                xmlMembers.add(tempMember);
                tempTag = null;
                tempMember = null;
            }
        }

        public ArrayList<AttributesImpl> getRelations(){
            return xmlRelations;
        }

        public ArrayList<Hashtable> getTags(){
            return xmlTags;
        }

        public ArrayList<HashSet<String>> getMembers(){
            return xmlMembers;
        }
    }

    public ArrayList<AttributesImpl> getExistingBusRelations(String left, String bottom, String right, String top) {
        String urlSuffix = "/api/0.6/relation[route=bus][bbox="+left+","+bottom+","+right+","+top+"]";
        String url = "http://www.informationfreeway.org" + urlSuffix;
        try {
            // get data from server
            String s = sendRequest(url, "GET", "");
            InputSource inputSource = new InputSource(new StringReader(s));
            // get data from file - need to remove this for REAL APPLICATION
//            InputSource inputSource = new InputSource("DataFromServerRELATION.osm");
            RelationParser par = new RelationParser();
            SAXParserFactory.newInstance().newSAXParser().parse(inputSource, par);
            existingRelations.addAll(par.getRelations());
            existingRelationTags.addAll(par.getTags());
            existingRelationMembers.addAll(par.getMembers());

        } catch(IOException e) {
            System.out.println(e);
        } catch(SAXException e) {
            System.out.println(e);
        } catch(ParserConfigurationException e) {
            System.out.println(e);
        }
        if (existingRelations.size()!=0) return existingRelations;
        System.out.println("null relations");
        return null;
    }

    // this method needs to be invoked after getExistingBusRelations
    public ArrayList<Hashtable> getExistingBusRelationTags(){
        System.out.println("relation tags = "+existingRelationTags.size());
        if (existingRelationTags.size() !=0 )
            return existingRelationTags;
        return null;
    }

    // this method needs to be invoked after getExistingBusRelations
    public ArrayList<HashSet<String>> getExistingBusRelationMembers(){
        System.out.println("tags = "+existingRelationMembers.size());
        if (existingRelationMembers.size() !=0 )
            return existingRelationMembers;
        return null;
    }

    public static String getApiVersion() {
        return API_VERSION;
    }

    private String getRequestContents() {
        String text = oprinter.header() +
                oprinter.writeChangeSet() +
                oprinter.footer();
        return text;
    }

    private String getRequestContents(String changeSetID, String lat, String lon) {
        String text = oprinter.header() +
                oprinter.writeBusStop(changeSetID, lat, lon) +
                oprinter.footer();
        return text;
    }

    private String getRequestContents(String changeSetID, HashSet<Stop> addStop, HashSet<Stop> modifyStop, HashSet<Stop> deleteStop, Hashtable r) {
        Hashtable routes = new Hashtable();
        routes.putAll(r);
        ArrayList<String> routeKeys = new ArrayList<String>();
        routeKeys.addAll(routes.keySet());
        String text = "";
        List<Stop> stops = new ArrayList<Stop>();
        stops.addAll(addStop);
        text += oprinter.osmChangeCreate();
        int id=0;
        for(int i=0; i<stops.size(); i++){
            id = (-1)*(i+1);
            text += oprinter.writeBusStop(changeSetID, Integer.toString(id), stops.get(i));
        }
        int k=0;
        while (k<routeKeys.size()){
            Route tRoute = (Route)routes.get(routeKeys.get(k));
            if(tRoute.getStatus().equals("n")){
                id--;
                text += oprinter.writeBusRoute(changeSetID, Integer.toString(id), tRoute);
                routeKeys.remove(k);
            }
            else {
                k++;
            }
        }
        stops = new ArrayList<Stop>();
        stops.addAll(modifyStop);
        text += oprinter.osmChangeModify();
        for(int i=0; i<stops.size(); i++){
            String nodeid = stops.get(i).getOsmId();
            text += oprinter.writeBusStop(changeSetID, nodeid, stops.get(i));
        }
        //all routes should be modified. Thus, k=0 after while loop
        k=0;
        while (k<routeKeys.size()){
            Route tRoute = (Route)routes.get(routeKeys.get(k));
            if(tRoute.getStatus().equals("m")){
                String routeid = tRoute.getOsmId();
                text += oprinter.writeBusRoute(changeSetID, routeid, tRoute);
                routeKeys.remove(k);
            }
            else {
                k++;
            }
        }
        stops = new ArrayList<Stop>();
        stops.addAll(deleteStop);
        text += oprinter.osmChangeDelete();
        for(int i=0; i<stops.size(); i++){
            String nodeid = stops.get(i).getOsmId();
            String nodeVersion = stops.get(i).getOsmVersion();
            text += oprinter.writeDeleteNode(nodeid, changeSetID, nodeVersion);
        }
        text += oprinter.osmChangeDeleteClose();
        return text;
    }

    public void createChangeSet() {
        String urlSuffix = "changeset/create";
        String url = SERVER_URL + urlSuffix;
        
        String responseMessage = "";
        if (isSupportVersion) {
            String s = getRequestContents();
            responseMessage = sendRequest(url, "PUT", getRequestContents());
            System.out.println(responseMessage);
            cSetID = responseMessage.substring(0, responseMessage.lastIndexOf("\n"));
            System.out.println("ChangeSet ID = "+cSetID);
        }
    }

    public void closeChangeSet() {
        String urlSuffix = "changeset/"+cSetID+"/close";
        String url = SERVER_URL + urlSuffix;

        String responseMessage = "";
        if (isSupportVersion) {
            if (!cSetID.equals("")) {
                responseMessage = sendRequest(url, "PUT", getRequestContents());
                System.out.println(responseMessage);
            }
            else {
                System.out.println("Changeset ID is not obtained yet!");
            }
        }
    }

    public void createSingleBusStop(String lat, String lon) {
        String urlSuffix = "node/create";
        String url = SERVER_URL + urlSuffix;

        String responseMessage = "";
        if (isSupportVersion) {
            if (!cSetID.equals("")) {
                responseMessage = sendRequest(url, "PUT", getRequestContents(cSetID, lat, lon));
                System.out.println(responseMessage);
            }
            else {
                System.out.println("Changeset ID is not obtained yet!");
            }
        }
    }

    public void createChunks(HashSet<Stop> n, HashSet<Stop> m, HashSet<Stop> d, Hashtable r) {
        HashSet<Stop> newStops = new HashSet<Stop>();
        HashSet<Stop> modifyStops = new HashSet<Stop>();
        HashSet<Stop> deleteStops = new HashSet<Stop>();

        newStops.addAll(n);
        modifyStops.addAll(m);
        deleteStops.addAll(d);

        Hashtable routes = new Hashtable();
        routes.putAll(r);

        String urlSuffix = "changeset/"+cSetID+"/upload";
        String url = SERVER_URL + urlSuffix;

        String responseMessage = "";
        if (isSupportVersion) {
            if (!cSetID.equals("")) {
                String osmChangeText = getRequestContents(cSetID, newStops, modifyStops, deleteStops, routes);
                new WriteFile(FILE_NAME_OUT_UPLOAD, osmChangeText);
//                responseMessage = sendRequest(url, "POST", osmChangeText);
                System.out.println(responseMessage);
            }
            else {
                System.out.println("Changeset ID is not obtained yet!");
            }
        }
    }

    public String sendRequest(String url, String method, String content) {
        HttpURLConnection conn = null;
        StringBuffer responseText = new StringBuffer();
        URL serverAddress = null;
        int retry = 1;
        while (true) {
            try {
                System.out.println("Connecting "+url+" using method "+method+" "+retry);
                serverAddress = new URL(url);
                
                // set the initial connection
                conn = (HttpURLConnection) serverAddress.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(15000);
                
                if (method.equals("PUT") || method.equals("POST") || method.equals("DELETE")) {
                    BASE64Encoder enc = new sun.misc.BASE64Encoder();
                    String usernamePassword = Session.getUserName()+":"+Session.getPassword();
                    String encodedAuthorization = enc.encode(usernamePassword.getBytes());
                    conn.setRequestProperty("Authorization", "Basic "+ encodedAuthorization);
                    
                    conn.setRequestProperty("Content-type", "text/xml");
                    conn.setDoOutput(true);
                    
                    if(content!=null) {
                        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                        osw.write(content);
                        osw.flush();
                    }
                }
                
                conn.connect();

                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: "+responseCode);

                switch(responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        BufferedReader response = new BufferedReader(new InputStreamReader (conn.getInputStream()));
                        String s;
                        s = response.readLine();
                        while(s != null) {
                            responseText.append(s);
                            responseText.append("\n");
                            s = response.readLine();
                        }
                        break;
                    case HttpURLConnection.HTTP_CONFLICT:
                        System.out.println("Conflict");
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        System.out.println("Bad request");
                        break;
                    case HttpURLConnection.HTTP_BAD_METHOD:
                        System.out.println("Method not allowed");
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        System.out.println("Not found");
                        break;
                    case HttpURLConnection.HTTP_PRECON_FAILED:
                        System.out.println("Pre-condition failed");
                        break;
                    case HttpURLConnection.HTTP_GONE:
                        System.out.println("Element has been deleted");
                        break;
                    default:
                        // get error message
                        response = new BufferedReader(new InputStreamReader (conn.getErrorStream()));
                        s = response.readLine();
                        while(s != null) {
                            responseText.append(s);
                            responseText.append("\n");
                            s = response.readLine();
                        }

                        // Look for a detailed error message from the server
                        if (conn.getHeaderField("Error") != null) {
                            System.err.println("Error: " + conn.getHeaderField("Error"));
                        } else if (responseText.length()>0) {
                            System.err.println("Error: " + responseText);
                        }
                        break;
                }
            } catch (ConnectException e) {
                retry ++;
                continue;
            } catch (SocketTimeoutException e) {
                retry ++;
                continue;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //close the connection, set to null
                conn.disconnect();
                conn = null;
            }
            
            return responseText.toString();
        }
    }
}