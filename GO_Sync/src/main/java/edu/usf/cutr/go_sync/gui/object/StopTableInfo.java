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

/**
  * Reference to constants related to the StopTable
  * The goal is to make it easier to interpret the meaning of different columns
  * FIXME: Have other classes use these ints, rather than using hard coded numbers
  */

package edu.usf.cutr.go_sync.gui.object;

/**
  * @author Kevin Dalley
  */

public abstract class StopTableInfo {
    static final int GTFS_DATA_COL = 1;
    static final int GTFS_CHECK_COL = 2;
    static final int OSM_DATA_COL = 3;
    static final int OSM_CHECK_COL = 4;
    static final int NEW_VALUE_DATA_COL = 5;
};
