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

public enum ProcessingOptions {
    // General

    // For Stops
    SKIP_GTFS_STATIONS,

    // For Routes
    DONT_REPLACE_EXISING_OSM_ROUTE_COLOR,
    DONT_ADD_GTFS_ROUTE_TEXT_COLOR_TO_ROUTE,
    DONT_ADD_GTFS_AGENCY_ID_TO_ROUTE,

    // For Route Members
    SKIP_NODES_HAVING_ROLE_EMPTY,
    SKIP_NODES_HAVING_ROLE_STOP_ALL,
    SKIP_NODES_HAVING_ROLE_STOP_WITHOUT_MATCHING_PLATFORM,
    CREATE_ROUTE_AS_PTV2,
    SKIP_NODES_HAVING_ROLE_PLATFORM_NOT_IN_GTFS_TRIP_FROM_OSM_RELATION;

}
