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
        String[] base_id;
        if (parentSiteRef != null) {
            base_id = parentSiteRef.split(":");
            return base_id[3].replace("log", "S");
        } else {
            //if (childSiteRef != null) {
            // Case when we are on a logical StopPlace or a parentStopPlace
            base_id = id.split(":");
            return base_id[3].replace("log", "S");
            //}
        }
        //return "Unexpected case: TODO";
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
