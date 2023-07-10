/**
 * Copyright (C) 2023 University of South Florida and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.usf.cutr.go_sync.io;

import edu.usf.cutr.go_sync.object.NetexQuay;
import edu.usf.cutr.go_sync.object.NetexStopPlace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NetexParser extends DefaultHandler {

    HashMap<String, NetexQuay> quayListByGtfsId;
    HashMap<String, NetexQuay> quayListByQuayId;
    HashMap<String, NetexStopPlace> stopPlaceListByGtfsId;
    HashMap<String, NetexStopPlace> stopPlaceListByStopPlaceId;
    HashMap<String, NetexStopPlace> parentSiteListByGtfsId;
    HashMap<String, NetexStopPlace> parentSiteListByStopPlaceId;
    HashMap<String, NetexStopPlace> logicalSiteListByGtfsId;
    HashMap<String, NetexStopPlace> stopPlaceIdByLatLon;

    NetexQuay quay = null;
    NetexStopPlace stopPlace = null;

    private StringBuilder tagTextContent = new StringBuilder();

    List<String> xPath;
    String pathBegin = "PublicationDelivery/dataObjects/GeneralFrame/members/";

    public NetexParser() {
        xPath = new ArrayList<String>();
        quayListByGtfsId = new HashMap<String, NetexQuay>();
        quayListByQuayId = new HashMap<String, NetexQuay>();
        stopPlaceListByGtfsId = new HashMap<String, NetexStopPlace>();
        stopPlaceListByStopPlaceId = new HashMap<String, NetexStopPlace>();
        parentSiteListByGtfsId = new HashMap<String, NetexStopPlace>();
        parentSiteListByStopPlaceId = new HashMap<String, NetexStopPlace>();
        logicalSiteListByGtfsId = new HashMap<String, NetexStopPlace>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody

        xPath.add(qName);

        if (qName.equals("Quay")) {
            quay = new NetexQuay(attributes.getValue("id"));
        }

        if (qName.equals("StopPlace")) {
            stopPlace = new NetexStopPlace(attributes.getValue("id"));
        }

        if (qName.equals("Name")) {
            if (String.join("/", xPath).equals(pathBegin + "Quay/Name")) {
                tagTextContent.setLength(0);
            }

            if (String.join("/", xPath).equals(pathBegin + "Quay/alternativeNames/AlternativeName/Name")) {
                tagTextContent.setLength(0);
            }

            if (String.join("/", xPath).equals(pathBegin + "StopPlace/Name")) {
                tagTextContent.setLength(0);
            }
        }

        if (qName.equals("Town")) {
            if (String.join("/", xPath).equals(pathBegin + "Quay/PostalAddress/Town")) {
                tagTextContent.setLength(0);

            }
            if (String.join("/", xPath).equals(pathBegin + "StopPlace/PostalAddress/Town")) {
                tagTextContent.setLength(0);
            }
        }

        if (qName.equals("alternativeNames")) {
            // Quay/alternativeNames
        }

        if (qName.equals("AlternativeName")) {
            // Quay/alternativeNames/AlternativeName
        }

        if (qName.equals("PostalAddress")) {
            // Quay/PostalAddress

            // StopPlace/PostalAddress
        }

        if (qName.equals("quays")) {
            // StopPlace/quays
        }

        if (qName.equals("QuayRef")) {
            if (String.join("/", xPath).equals(pathBegin + "StopPlace/quays/QuayRef")) {
                stopPlace.addQuayRef(attributes.getValue("ref"));
            }
        }

        if (qName.equals("ParentSiteRef")) {
            if (String.join("/", xPath).equals(pathBegin + "StopPlace/ParentSiteRef")) {
                stopPlace.setParentSiteRef(attributes.getValue("ref"));
            }
        }

        if (qName.equals("Longitude") || qName.equals("Latitude")) {
            if (String.join("/", xPath).equals(pathBegin + "StopPlace/Centroid/Location/" + qName)) {
                tagTextContent.setLength(0);
            }
            if (String.join("/", xPath).equals(pathBegin + "Quay/Centroid/Location/" + qName)) {
                tagTextContent.setLength(0);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        tagTextContent.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody

        if (qName.equals("Quay")) {
            quayListByGtfsId.put(quay.getIdAsGtfs(), quay);
            quayListByQuayId.put(quay.getId(), quay);
        }

        if (qName.equals("StopPlace")) {
            // Store stopPlace to different list depending on whether it is a parentSite or not.
            if (stopPlace.getParentSiteRef() == null) {
                parentSiteListByGtfsId.put(stopPlace.getGtfsEquivalentId(), stopPlace);
                parentSiteListByStopPlaceId.put(stopPlace.getId(), stopPlace);
            } else {
                stopPlaceListByGtfsId.put(stopPlace.getGtfsEquivalentId(), stopPlace);
                stopPlaceListByStopPlaceId.put(stopPlace.getId(), stopPlace);
            }
        }

        if (qName.equals("Name")) {
            if (String.join("/", xPath).equals(pathBegin + "Quay/Name")) {
                quay.setName(tagTextContent.toString());
            }

            if (String.join("/", xPath).equals(pathBegin + "Quay/alternativeNames/AlternativeName/Name")) {
                quay.addAltName(tagTextContent.toString());
            }

            if (String.join("/", xPath).equals(pathBegin + "StopPlace/Name")) {
                stopPlace.setName(tagTextContent.toString());
            }
        }

        if (qName.equals("Town")) {
            if (String.join("/", xPath).equals(pathBegin + "Quay/PostalAddress/Town")) {
                quay.setTown(tagTextContent.toString());

            }
            if (String.join("/", xPath).equals(pathBegin + "StopPlace/PostalAddress/Town")) {
                stopPlace.setTown(tagTextContent.toString());
            }
        }

        if (qName.equals("Latitude")) {
            if (String.join("/", xPath).equals(pathBegin + "StopPlace/Centroid/Location/" + qName)) {
                stopPlace.setLat(tagTextContent.toString());
            }
            if (String.join("/", xPath).equals(pathBegin + "Quay/Centroid/Location/" + qName)) {
                quay.setLat(tagTextContent.toString());
            }
        }

        if (qName.equals("Longitude")) {
            if (String.join("/", xPath).equals(pathBegin + "StopPlace/Centroid/Location/" + qName)) {
                stopPlace.setLon(tagTextContent.toString());
            }
            if (String.join("/", xPath).equals(pathBegin + "Quay/Centroid/Location/" + qName)) {
                quay.setLon(tagTextContent.toString());
            }
        }

        if (xPath.get(xPath.size() - 1).equals(qName)) {
            xPath.remove(xPath.size() - 1);
        }

    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody

        boolean found = false;
        // create logical stop place based on the stopPlaceListByGtfsId
        for (NetexStopPlace p : parentSiteListByGtfsId.values()) {
            found = false;
            for (NetexStopPlace c : stopPlaceListByGtfsId.values()) {
                if (p.getId().equals(c.getParentSiteRef())) {
                    found = true;
                    p.addChildSiteRef(c.getId());
                    // Save current Id to childSiteRef
                    c.addChildSiteRef(c.getId());
                    c.setParentSiteRef(null);
                    // Use id of parentSite
                    c.setId(p.getId());
                    // Use name of parentSite as altName
                    c.addAltName(p.getName());
                    logicalSiteListByGtfsId.put(c.getGtfsEquivalentId(), c);
                    break;
                }
                // We consider a StopPlace can only have one StopPlacechild (to be checked)
            }
            if (!found) {
                // Case where no match found:
                System.out.println("No child stopPlace in netext for : " + p.getId());
                logicalSiteListByGtfsId.put(p.getGtfsEquivalentId(), p);
            }
        }

        //printResult();
    }

    public void printResult() {
        System.out.println("All logical stopPlaces after parsing:");
        for (Map.Entry<String, NetexStopPlace> set : logicalSiteListByGtfsId.entrySet()) {
            System.out.println(set.getKey() + "> " + set.getValue().printContent());
        }
        System.out.println("All Quay after parsing:");
        for (Map.Entry<String, NetexQuay> set : quayListByGtfsId.entrySet()) {
            System.out.println(set.getKey() + "> " + set.getValue().printContent());
        }
    }

    public HashMap<String, NetexQuay> getQuayListByGtfsId() {
        return quayListByGtfsId;
    }

    public HashMap<String, NetexQuay> getQuayListByQuayId() {
        return quayListByQuayId;
    }

    public HashMap<String, NetexStopPlace> getLogicalSiteListByGtfsId() {
        return logicalSiteListByGtfsId;
    }

    public HashMap<String, NetexStopPlace> getParentSiteListByGtfsId() {
        return parentSiteListByGtfsId;
    }

    public HashMap<String, NetexStopPlace> getStopPlaceListByGtfsId() {
        return stopPlaceListByGtfsId;
    }

    public HashMap<String, NetexStopPlace> getStopPlaceListByStopPlaceId() {
        return stopPlaceListByStopPlaceId;
    }

    public HashMap<String, NetexStopPlace> getAllStopPlaceListByStopPlaceId() {
        HashMap<String, NetexStopPlace> allStopPlaceListByStopPlaceId = new HashMap<>();
        allStopPlaceListByStopPlaceId.putAll(stopPlaceListByStopPlaceId);
        allStopPlaceListByStopPlaceId.putAll(parentSiteListByStopPlaceId);
        return allStopPlaceListByStopPlaceId;
    }

    public HashMap<String, NetexStopPlace> getAllStopPlaceListByGtfsId() {
        HashMap<String, NetexStopPlace> allStopPlaceListByGtfsId = new HashMap<>();
        allStopPlaceListByGtfsId.putAll(stopPlaceListByGtfsId);
        allStopPlaceListByGtfsId.putAll(parentSiteListByGtfsId);
        return allStopPlaceListByGtfsId;
    }

}
