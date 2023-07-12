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

package edu.usf.cutr.go_sync.tools.parser;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class NodeWayAttr extends AttributesImpl {
    
    String osmPrimitiveType; // node or way
    String lat;
    String lon;
    
    ArrayList<String> wayNds = new ArrayList<>();

    NodeWayAttr(Attributes attr, String object_type) {
        super(attr);
        this.osmPrimitiveType = object_type;
    }

    public ArrayList<String> getWayNds() {
        return wayNds;
    }

    public void setWayNds(ArrayList<String> wayNds) {
        //System.out.println("Adding " + wayNds.size() + " nds to way.");
        //System.out.println(wayNds.toString());
        this.wayNds = wayNds;
    }
    
    public void setLat(String lat) {
        if (osmPrimitiveType.equals("way")) {
            this.lat = lat;
        }
    }

    public void setLon(String lon) {
        if (osmPrimitiveType.equals("way")) {
            this.lon = lon;
        }
    }
    
    public String getLat() {
        if (osmPrimitiveType.equals("way")) {
            return lat;
        }
        return getValue("lat");
    }

    public String getLon() {
        if (osmPrimitiveType.equals("way")) {
            return lon;
        }
        return getValue("lon");
    }
    
    public String geOsmPrimitiveType() {
        return osmPrimitiveType;
    }
    
    public boolean shouldSaveGeoData() {
        return !osmPrimitiveType.equals("way");
    }

    @Override
    public String toString() {
        String a = "primitive type: [%s]\t- id: [%s]\t - lat: [%s]\t- lon: [%s]\t - " +
                "nodes content: %s";
        return String.format(a, osmPrimitiveType, getValue("id"), lat, lon, wayNds.toString());
    }
}
