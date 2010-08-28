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
import java.util.Iterator;
import object.RelationMember;
import object.Route;
import object.Session;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sun.misc.BASE64Encoder;
import tools.parser.BusStopParser;
import tools.parser.ChangesetDownloadParser;
import tools.parser.OsmVersionParser;
import tools.parser.RouteParser;

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
    private ArrayList<HashSet<RelationMember>> existingRelationMembers = new ArrayList<HashSet<RelationMember>>();

    private HashSet<Stop> revertDelete = new HashSet<Stop>();
    private HashSet<Stop> revertModify = new HashSet<Stop>();
    private HashSet<Stop> revertUpload = new HashSet<Stop>();

    private boolean isSupportVersion = false;

    private OsmPrinter oprinter = new OsmPrinter();

    private String cSetID="";

    public static final String FILE_NAME_OUT_UPLOAD = "OSM_CHANGE_XML.txt";

    public void checkVersion() {
        String url = SERVER_URL + "capabilities/";

        String s = sendRequest(url, "GET", "");

        try {
            InputSource inputSource = new InputSource(new StringReader(s));
            OsmVersionParser vp = new OsmVersionParser("0.6");
            SAXParserFactory.newInstance().newSAXParser().parse(inputSource, vp);
            isSupportVersion = vp.isSupportVersion();
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

    public ArrayList<AttributesImpl> getExistingBusStops(String left, String bottom, String right, String top) {
        String urlSuffix = "/api/0.6/node[highway=bus_stop][bbox="+left+","+bottom+","+right+","+top+"]";
        String url = "http://xapi.openstreetmap.org" + urlSuffix;
        try {
            // get data from server
            String s = sendRequest(url, "GET", "");
            InputSource inputSource = new InputSource(new StringReader(s));
            // get data from file - need to remove this for REAL APPLICATION
//            InputSource inputSource = new InputSource("DataFromServer.osm");
            BusStopParser par = new BusStopParser();
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

    public ArrayList<AttributesImpl> getExistingBusRelations(String left, String bottom, String right, String top) {
        String urlSuffix = "/api/0.6/relation[route=bus][bbox="+left+","+bottom+","+right+","+top+"]";
        String url = "http://xapi.openstreetmap.org" + urlSuffix;
        try {
            // get data from server
            String s = sendRequest(url, "GET", "");
            InputSource inputSource = new InputSource(new StringReader(s));
            // get data from file - need to remove this for REAL APPLICATION
//            InputSource inputSource = new InputSource("DataFromServerRELATION.osm");
            RouteParser par = new RouteParser();
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
    public ArrayList<HashSet<RelationMember>> getExistingBusRelationMembers(){
        System.out.println("tags = "+existingRelationMembers.size());
        if (existingRelationMembers.size() !=0 )
            return existingRelationMembers;
        return null;
    }

    public void downloadChangeSet(String cs) {
        String urlSuffix = "changeset/"+cs+"/download";
        String url = SERVER_URL + urlSuffix;
        try {
            // get data from server
            String s = sendRequest(url, "GET", "");
            InputSource inputSource = new InputSource(new StringReader(s));
            // get data from file - need to remove this for REAL APPLICATION
//            InputSource inputSource = new InputSource("DataFromServerRELATION.osm");
            ChangesetDownloadParser par = new ChangesetDownloadParser();
            SAXParserFactory.newInstance().newSAXParser().parse(inputSource, par);

            revertDelete.addAll(par.getToBeDeletedStop());

            ArrayList<Stop> toBeModified = new ArrayList<Stop>();
            toBeModified.addAll(par.getToBeModifiedStop());
            for (int i=0; i<toBeModified.size(); i++) {
                Stop ts = toBeModified.get(i);
                Integer versionNumber = (Integer.parseInt(ts.getOsmVersion())-1);
                Stop ns = getNodeByVersion(ts.getOsmId(), versionNumber.toString(), false);
                ns.setOsmVersion(ts.getOsmVersion());
                revertModify.add(ns);
            }

            ArrayList<Stop> toBeUploaded = new ArrayList<Stop>();
            toBeUploaded.addAll(par.getToBeUploadedStop());
            for (int i=0; i<toBeUploaded.size(); i++) {
                Stop ts = toBeUploaded.get(i);
                Stop ns = getNodeByVersion(ts.getOsmId(), ts.getOsmVersion(), true);
                ns.setOsmVersion("-1");
                revertUpload.add(ns);
            }
        } catch(IOException e) {
            System.out.println(e);
        } catch(SAXException e) {
            System.out.println(e);
        } catch(ParserConfigurationException e) {
            System.out.println(e);
        }
    }

    public HashSet<Stop> getRevertUpload(){
        return revertUpload;
    }

    public HashSet<Stop> getRevertModify(){
        return revertModify;
    }

    public HashSet<Stop> getRevertDelete(){
        return revertDelete;
    }

    private Stop getNodeByVersion(String osmid, String version, boolean isNew){
        Stop st=null;
        String urlSuffix = "node/"+osmid+"/"+version;
        String url = SERVER_URL + urlSuffix;
        System.out.println("Retrieving node "+osmid+" with version "+version+"...");
        try {
            // get data from server
            String s = sendRequest(url, "GET", "");
            InputSource inputSource = new InputSource(new StringReader(s));
            // get data from file - need to remove this for REAL APPLICATION
//            InputSource inputSource = new InputSource("DataFromServerRELATION.osm");
            BusStopParser par = new BusStopParser();
            SAXParserFactory.newInstance().newSAXParser().parse(inputSource, par);
            AttributesImpl attImplNode = par.getOneNode();
            Hashtable tags = par.getTagsOneNode();
            st = new Stop(null,(String)tags.get("operator"),(String)tags.get("name"),
                    attImplNode.getValue("lat"),attImplNode.getValue("lon"));
            st.addTags(tags);
            if (!isNew) {
                st.setOsmId(attImplNode.getValue("id"));
            }
            else {
                st.setOsmId("-"+attImplNode.getValue("id"));
            }
        } catch(IOException e) {
            System.out.println(e);
        } catch(SAXException e) {
            System.out.println(e);
        } catch(ParserConfigurationException e) {
            System.out.println(e);
        }
        return st;
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
        if (r!=null) routes.putAll(r);
        ArrayList<String> routeKeys = new ArrayList<String>();
        if (r!=null) routeKeys.addAll(routes.keySet());
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
        text += oprinter.osmChangeModify();
        stops = new ArrayList<Stop>();
        stops.addAll(modifyStop);
        for(int i=0; i<stops.size(); i++){
            String nodeid = stops.get(i).getOsmId();
            text += oprinter.writeBusStop(changeSetID, nodeid, stops.get(i));
//            System.out.println(stops.get(i).getOsmId()+","+stops.get(i).getStopID()+","+stops.get(i).getOsmVersion());
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

        if(n!=null)newStops.addAll(n);
        if(m!=null)modifyStops.addAll(m);
        if(d!=null)deleteStops.addAll(d);

        Hashtable routes = new Hashtable();
        if (r!=null) routes.putAll(r);

        String urlSuffix = "changeset/"+cSetID+"/upload";
        String url = SERVER_URL + urlSuffix;

        String responseMessage = "";
        if (isSupportVersion) {
            if (!cSetID.equals("")) {
                String osmChangeText = getRequestContents(cSetID, newStops, modifyStops, deleteStops, routes);
                new WriteFile(FILE_NAME_OUT_UPLOAD, osmChangeText);
                
//                responseMessage = sendRequest(url, "POST", osmChangeText);
                System.out.println("Message: "+responseMessage);
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
                System.out.println("Response Message: "+conn.getResponseMessage());

                if (responseCode >= 500) {
                    System.out.println("response code >=500");
                    retry++;
                    continue;
                }

                BufferedReader response;
                String s;
                if(responseCode==HttpURLConnection.HTTP_OK) {
                    response = new BufferedReader(new InputStreamReader (conn.getInputStream()));
                    s = response.readLine();
                    while(s != null) {
                        responseText.append(s);
                        responseText.append("\n");
                        s = response.readLine();
                    }
                    break;
                } else {
                    // get error message
                    response = new BufferedReader(new InputStreamReader (conn.getErrorStream()));
                    s = response.readLine();
                    while(s != null) {
                        responseText.append(s);
                        responseText.append("\n");
                        s = response.readLine();
                    }
                    
                    // Look for a detailed error message from the server
                    String errMess = conn.getHeaderField("Error");
                    if (errMess != null) {
                        System.err.println("Error: " + errMess);
                    } else if (responseText.length()>0) {
                        System.err.println("Error: " + responseText);
                    }
                    break;
                }
                
            } catch (ConnectException e) {
                System.out.println(e.toString());
                retry ++;
                continue;
            } catch (SocketTimeoutException e) {
                System.out.println(e.toString());
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
        }
        return responseText.toString();
    }
}