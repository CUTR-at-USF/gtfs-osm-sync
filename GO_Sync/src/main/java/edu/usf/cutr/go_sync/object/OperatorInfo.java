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
package edu.usf.cutr.go_sync.object;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Khoa Tran
 */
public class OperatorInfo {

    private static List<String> possibleOperatorName = new ArrayList<String>();
    private static List<Pattern> possibleOperatorRegexPattern = new ArrayList<Pattern>();
    
    private static String ntdID, fullName, abbreviate, fileDir, aliasName, gtfsFields;

    private static int gtfs_id_digit;

    public OperatorInfo(String fName, String abbr, String aName, String regex, String id, int digit, String fDir){
        fullName = fName;
        abbreviate = abbr;
        aliasName = aName;
        addRegex(regex);
        addName(fullName);
        addName(abbreviate);
        if(aliasName!=null && !aliasName.equals("")){
            String[] temp;
            temp = aliasName.split(";");
            for(int i=0; i<temp.length; i++){
                addName(temp[i]);
            }
        }
        ntdID = id;
        gtfs_id_digit = digit;
        fileDir = fDir;
    }

    public static void setGtfsFields(String gf){
        gtfsFields = gf;
    }

    public static String getGtfsFields(){
        return gtfsFields;
    }

    public static String getNTDID(){
        return ntdID;
    }

    public static String getFullName(){
        return fullName;
    }
    public static void setFullName(String aName){
    	
    	possibleOperatorName.clear();
        fullName = aName;
        addName(fullName);
        addName(abbreviate);
        if(aliasName!=null && !aliasName.equals("")){
            String[] temp;
            temp = aliasName.split(";");
            for(int i=0; i<temp.length; i++){
                addName(temp[i]);
            }
        }
    }
    public static String getAbbreviateName(){
        return abbreviate;
    }

    public static int getGtfsIdDigit(){
        return gtfs_id_digit;
    }

    public static String getFileDirectory(){
        return fileDir;
    }

    public static void addName(String name){
        // generate possible name for operator fields e.g. HART / Hillsborough Area Regional Transit
        if(name!=null && !name.equals("")) {
            possibleOperatorName.add(name.toUpperCase());
        }
    }

    public static void addRegex(String regex) {
        // generate possible name for operator fields e.g. HART / Hillsborough Area Regional Transit
        if (regex != null && !regex.equals("")) {
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            possibleOperatorRegexPattern.add(p);
        }
    }

    public static boolean isTheSameOperator(String osmOperator) {
        for (int i=0; i<possibleOperatorName.size(); i++) {
            if (osmOperator.toUpperCase().contains(possibleOperatorName.get(i)) ||
                    possibleOperatorName.get(i).contains(osmOperator.toUpperCase())) {
                return true;
            }
        }
        for (int i = 0; i < possibleOperatorRegexPattern.size(); i++) {
            Matcher m = possibleOperatorRegexPattern.get(i).matcher(osmOperator);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }
}