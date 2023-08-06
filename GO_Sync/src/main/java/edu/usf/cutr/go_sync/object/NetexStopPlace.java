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

package edu.usf.cutr.go_sync.object;

import java.util.ArrayList;

public class NetexStopPlace extends NetexStopElement {

    String parentSiteRef;
    ArrayList<String> childSiteRef;

    ArrayList<String> quayRefs;

    public NetexStopPlace(String id) {
        super(id);
        parentSiteRef = null;
        quayRefs = new ArrayList<String>();
        childSiteRef = new ArrayList<String>();
    }

    public String getParentSiteRef() {
        return parentSiteRef;
    }

    public String getGtfsEquivalentId() {
        String return_value = null;
        switch (OperatorInfo.getFullName()) {
            case "Fluo Grand Est 67":
            case "FLUO 68":
            case "Fluo88":
                return_value = getGtfsEquivalentIdFRFluo();
                break;
            case "MOBIGO (58)":
                return_value = getGtfsEquivalentIdFRMobigo();
                break;
            default:
                throw new UnsupportedOperationException(String.format("Method to extract gtfs id inside netex file not set for %s. Please set it in code nearby the NO_METHOD_TO_FIND_GTFSID_IN_NETEX_STOP_PLACE_ID or don't use the netex file.", OperatorInfo.getFullName()));
        }
        return return_value;
    }

    private String getGtfsEquivalentIdFRFluo() {
        // Example: <StopPlace id="FR:68247:StopPlace:log140712:GRANDEST2" version="1">
        String[] base_id;
        if (parentSiteRef != null) {
            base_id = parentSiteRef.split(":");
            if (base_id.length >= 4) {
                return base_id[3].replace("log", "S");
            }
        } else {
            //if (childSiteRef != null) {
            // Case when we are on a logical StopPlace or a parentStopPlace
            base_id = id.split(":");
            if (base_id.length >= 4) {
                return base_id[3].replace("log", "S");
            }
            //}
        }
        System.out.println(String.format("Unexpected format for StopPlace id %s", id));
        return null;
    }

    private String getGtfsEquivalentIdFRMobigo() {
        // Example: <StopPlace id="FR:StopPlace:Navitia_300714_bus:" version="any">
        String[] base_id;
        if (parentSiteRef != null) {
            base_id = parentSiteRef.split(":");
            if (base_id.length >= 3) {
                return base_id[2].replace("Navitia_", "");
            }
        } else {
            //if (childSiteRef != null) {
            // Case when we are on a logical StopPlace or a parentStopPlace
            base_id = id.split(":");
            if (base_id.length >= 3) {
                return base_id[2].replace("Navitia_", "");
            }
            //}
        }
        System.out.println(String.format("Unexpected format for StopPlace id %s", id));
        return null;
    }

    public void setParentSiteRef(String parentSiteRef) {
        this.parentSiteRef = parentSiteRef;
    }

    public ArrayList<String> getChildSiteRef() {
        return childSiteRef;
    }

    public void addChildSiteRef(String childSiteRef) {
        this.childSiteRef.add(childSiteRef);
    }

    public ArrayList<String> getQuayRefs() {
        return quayRefs;
    }

    public void setQuayRefs(ArrayList<String> quayRefs) {
        this.quayRefs = quayRefs;
    }

    public void addQuayRef(String quayRef) {
        this.quayRefs.add(quayRef);
    }

    public String printContent() {
        return String.format("id: [%s] name: [%s] altNames: %s town: [%s] parentSiteRef: [%s] childSiteRef: [%s] quayRefs: %s", id, name, altNames, town, parentSiteRef, childSiteRef, quayRefs.toString());
    }
}
