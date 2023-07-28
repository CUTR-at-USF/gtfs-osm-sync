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

public class ProcessingParams {
    ArrayList<String> stopCities;
    Double stopMinLat, stopMaxLat;
    Double stopMinLon, stopMaxLon;

    public ArrayList<String> getStopCities() {
        return stopCities;
    }

    public void setStopCities(ArrayList<String> stopCities) {
        this.stopCities = stopCities;
    }

    public Double getStopMinLat() {
        return stopMinLat;
    }

    public void setStopMinLat(String stopMinLat) {
        this.stopMinLat = Double.valueOf(stopMinLat);
    }

    public Double getStopMaxLat() {
        return stopMaxLat;
    }

    public void setStopMaxLat(String stopMaxLat) {
        this.stopMaxLat = Double.valueOf(stopMaxLat);
    }

    public Double getStopMinLon() {
        return stopMinLon;
    }

    public void setStopMinLon(String stopMinLon) {
        this.stopMinLon = Double.valueOf(stopMinLon);
    }

    public Double getStopMaxLon() {
        return stopMaxLon;
    }

    public void setStopMaxLon(String stopMaxLon) {
        this.stopMaxLon = Double.valueOf(stopMaxLon);
    }
}
