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

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class RouteVariant {

    String trip_id;
    String route_id;
    List<String> same_trip_sequences;
    TreeMap<Integer, RouteVariantStop> stops;
    Route gtfs_route;

    public RouteVariant(String trip_id) {
        this.trip_id = trip_id;
        stops = new TreeMap<>();
        same_trip_sequences = new ArrayList<String>();
    }

    public String getTrip_id() {
        return trip_id;
    }

    public TreeMap<Integer, RouteVariantStop> getStops() {
        return stops;
    }

    public void setStops(TreeMap<Integer, RouteVariantStop> stops) {
        this.stops = stops;
    }

    public void addStop(Integer sequence_id, String stop_id, String name, String arrival_time, String departure_time, String pickup_type, String drop_off_type) {
        RouteVariantStop rvs = new RouteVariantStop(stop_id, name, arrival_time, departure_time, pickup_type, drop_off_type);
        stops.put(sequence_id, rvs);
    }

    public String toText() {
        String s = "";
        s += String.format("Trip_id [%s] | route_id [%s] | route_short_name [%s] | route_long_name [%s]\n", trip_id, route_id, getRoute_short_name(), getRoute_long_name());
        s += String.format(" Same as: %s\n", same_trip_sequences.toString());
        for (Map.Entry<Integer, RouteVariantStop> stop : stops.entrySet()) {
            Integer key = stop.getKey();
            RouteVariantStop value = stop.getValue();
            s += String.format(" num: %d, %s, %s, %s\n", key, value.getStop_id(), value.getPickup_type(), value.getDrop_off_type());
        }
        return s;
    }

    public List<String> getSame_trip_sequence() {
        return same_trip_sequences;
    }

    public void addSame_trip_sequence(String trip_id) {
        this.same_trip_sequences.add(trip_id);
    }

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getRoute_short_name() {
        return gtfs_route.getRouteRef();
    }

    public Route getRoute() {
        return gtfs_route;
    }

    public void setRoute(Route route) {
        this.gtfs_route = route;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.stops);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RouteVariant other = (RouteVariant) obj;
        return Objects.equals(this.stops, other.stops);
    }

    public boolean equalsSequenceOf(RouteVariant obj) {
        final RouteVariant other = (RouteVariant) obj;
        return Objects.equals(this.stops, other.stops);
    }

    public String getOsmValue(String key_name) {
        switch (key_name) {
            case "ref":
                return getRoute_short_name();
            case "name":
                return String.format("Bus %s: %s => %s",
                        getRoute_short_name(),
                        stops.firstEntry().getValue().getName(),
                        stops.lastEntry().getValue().getName());
            case "from":
                return stops.firstEntry().getValue().getName();
            case "to":
                return stops.lastEntry().getValue().getName();
            case "gtfs:route_id":
                return this.route_id;
            case "gtfs:trip_id:sample":
                return this.trip_id;
            case "gtfs:name":
            case "gtfs_name":
                return getRoute_long_name();
            default:
                return gtfs_route.getTag(key_name);
        }
    }

    public String getRoute_long_name() {
        return gtfs_route.getTag("gtfs:name");
    }

    private Duration getDurationAsDuration() {
        Integer firstStopIndex = Collections.min(stops.keySet());
        String tripDepartureTime = stops.get(firstStopIndex).getDeparture_time();
        Integer lastStopIndex = Collections.max(stops.keySet());
        String tripArrivalTime = stops.get(lastStopIndex).getArrival_time();

        LocalTime tripDepartureTimeLT = LocalTime.parse(tripDepartureTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime tripArrivalTimeLT = LocalTime.parse(tripArrivalTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        return Duration.between(tripDepartureTimeLT, tripArrivalTimeLT);
    }

    public String getDuration() {
        Duration d = getDurationAsDuration();
        return String.format("%02d:%02d", d.toHours(), d.minusHours(d.toHours()).toMinutes());
    }
}
