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

package tools;

import java.util.HashMap;
import object.OperatorInfo;

/**
 *
 * @author Khoa Tran
 */
public class OsmFormatter {
    final private static HashMap<Character, String> specialCharacter = new HashMap<Character, String>();
    static {
        specialCharacter.put('>', "&gt;");
        specialCharacter.put('<', "&lt;");
        specialCharacter.put('"', "&quot;");
        specialCharacter.put('\'', "&apos;");
        specialCharacter.put('&', "&amp;");
    }
    public static String getValidBusStopId(String bsid) {
        String id = bsid;
        for (int i=0; i<OperatorInfo.getGtfsIdDigit()-bsid.length(); i++){
            id = "0"+id;
        }
        return id;
    }

    public static String getValidXmlText(String v){
        String s="";
        for(int i=0; i<v.length();i++){
            if(specialCharacter.containsKey(v.charAt(i))){
                s = s+(String)specialCharacter.get(v.charAt(i));
            } else {
                s = s+v.charAt(i);
            }
        }
        return s;
    }
}
