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
import java.util.regex.Pattern;

public class NetexStopElement {

    String id;
    String town;
    String name;
    String lat;
    String lon;
    ArrayList<String> altNames;

    public NetexStopElement(String id) {
        this.id = id;
        altNames = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public ArrayList<String> getAltNames() {
        return altNames;
    }

    public String getAltNamesJoined() {
        return String.join(";", altNames);
    }

    public void setAltNames(ArrayList<String> altNames) {
        this.altNames = altNames;
    }

    public void addAltName(String altName) {
        this.altNames.add(altName.replace(';', '_'));
    }

    public String getLogicalName() {
        // Return the logical name as format: "TOWN - Name (without town prefix)"

        if (town == null || town.isEmpty()) {
            return name.replace("  ", " ").trim();
        } else {
            String nameWithoutTown = removeLeadingTownFromName(name, town);
            nameWithoutTown = removeTrailingTownFromName(nameWithoutTown, town);
            String logicalName = nameCleanup(prependTown(nameWithoutTown, town));

            return logicalName;
        }

    }

    public ArrayList<String> getLogicalAltNames() {
        ArrayList<String> _altNames = new ArrayList<>();

        // Add the name of the stop to _altNames if it doesn't match the logicalName
        if (!name.equals(getLogicalName())) {
            _altNames.add(name);
        }

        for (String altName : altNames) {
            if (town == null || town.isEmpty()) {
                if (!nameCleanup(altName).equals(getLogicalName())) {
                    _altNames.add(nameCleanup(altName));
                }
            } else {
                // Cleanup alternativeName: remove TOWN (since we added it in getLogiclName())
                String nameWithoutTown = removeLeadingTownFromName(altName, town);
                nameWithoutTown = removeTrailingTownFromName(nameWithoutTown, town);
                if (!nameWithoutTown.equals(getLogicalName()) && !_altNames.contains(nameWithoutTown)) {
                    _altNames.add(nameWithoutTown);
                }
            }
        }

        return _altNames;
    }

    private static String removeLeadingTownFromName(String name, String town) {
        String n_town = removeAccents(town);
        String n_name = removeAccents(name);

        String nameWithoutTown = n_name.replaceAll("(?i)^\\s*" + Pattern.quote(n_town), "");
        nameWithoutTown = nameWithoutTown.replaceAll("^\\s*-\\s*", "");

        // To preserve accents of the initial string, rebuild the name with a substring.
        nameWithoutTown = name.substring(name.length() - nameWithoutTown.length());
        nameWithoutTown = nameCleanup(nameWithoutTown);

        return nameWithoutTown;
    }

    private static String removeTrailingTownFromName(String name, String town) {
        String n_town = removeAccents(town);
        String n_name = removeAccents(name);

        String nameWithoutTown = n_name.replaceAll("(?i)\\s*" + Pattern.quote(n_town) + "$", "");
        nameWithoutTown = nameWithoutTown.replaceAll("^\\s*-\\s*", "");

        // To preserve accents of the initial string, rebuild the name with a substring.
        nameWithoutTown = name.substring(0, nameWithoutTown.length());
        nameWithoutTown = nameCleanup(nameWithoutTown);

        return nameWithoutTown;
    }

    private String prependTown(String nameWithoutTown, String town) {
        ArrayList<String> townsWithoutPrepend = new ArrayList();
        // TODO move this to configuration.

        if (townsWithoutPrepend.contains(town.toLowerCase())) {
            return nameWithoutTown;
        } else {
            return town.toUpperCase() + " - " + nameWithoutTown;
        }
    }

    private static String normalize(String input) {
        return input == null ? null : java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFKD);
    }

    static String removeAccents(String input) {
        return normalize(input).replaceAll("\\p{M}", "");
    }

    private static String nameCleanup(String label) {
        return label.replace("  ", " ").trim();
    }
}
