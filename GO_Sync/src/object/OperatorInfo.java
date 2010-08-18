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
package object;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Khoa Tran
 */
public class OperatorInfo {

    private static List<String> possibleOperatorName = new ArrayList<String>();
    
    private static String ntdID, fullName, abbreviate;

    public OperatorInfo(String fName, String abbr, String id){
        fullName = fName;
        abbreviate = abbr;
        addName(fullName);
        addName(abbreviate);
        ntdID = id;
    }

    public static String getNTDID(){
        return ntdID;
    }

    public static String getFullName(){
        return fullName;
    }

    public static String getAbbreviateName(){
        return abbreviate;
    }

    public static void addName(String name){
        // generate possible name for operator fields e.g. HART / Hillsborough Area Regional Transit
        possibleOperatorName.add(name.toUpperCase());
    }

    public static boolean isTheSameOperator(String osmOperator) {
        for (int i=0; i<possibleOperatorName.size(); i++) {
            if (osmOperator.toUpperCase().contains(possibleOperatorName.get(i))) {
                return true;
            }
        }
        return false;
    }
}