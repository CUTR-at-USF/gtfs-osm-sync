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

public class NetexQuay extends NetexStopElement {

    public NetexQuay(String id) {
        super(id);
    }

    public String getIdAsGtfs() {
        String return_value = null;
        switch (OperatorInfo.getFullName()) {
            case "Fluo Grand Est 67":
            case "FLUO 68":
            case "Fluo88":
                return_value = getIdAsGtfsFRFluo();
                break;
            case "MOBIGO (58)":
                return_value = getIdAsGtfsFRMobigo();
                break;
            default:
                throw new UnsupportedOperationException(String.format("Method to extract gtfs id inside netex file not set for %s. Please set it in code nearby the NO_METHOD_TO_FIND_GTFSID_IN_NETEX_QUAY_ID or don't use the netex file.", OperatorInfo.getFullName()));
        }
        return return_value;
    }

    private String getIdAsGtfsFRFluo() {
        // Example: <Quay id="FR:68171:Quay:6862167:CG68" version="1">
        if (id.split(":").length >= 4) {
            return id.split(":")[3];
        }
        System.out.println(String.format("Unexpected format for Quay id %s", id));
        return null;
    }

    private String getIdAsGtfsFRMobigo() {
        // Example: <Quay id="FR:Quay:300591:" version="any">
        if (id.split(":").length >= 3) {
            return id.split(":")[2];
        }
        System.out.println(String.format("Unexpected format for Quay id %s", id));
        return null;
    }

    public String printContent() {
        return String.format("id: [%s] name: [%s] altNames: %s town: [%s]", id, name, altNames.toString(), town);
    }

}
