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

import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author Khoa Tran
 */
public class OsmPrimitive {
    Hashtable osmTags;
    public OsmPrimitive(){
        osmTags = new Hashtable();
    }
    public void addTag(String k, String v){
        if (!v.equals("")) {
            osmTags.put(k, v);
        } else {
            osmTags.put(k, "none");
        }
    }

    public void addTags(Hashtable h){
        osmTags.putAll(h);
    }

    public String getTag(String k){
        if (osmTags.containsKey(k))
            return (String)osmTags.get(k);
        return null;
    }

    public HashSet<String> keySet(){
        HashSet<String> keys = new HashSet<String>(osmTags.size());
        keys.addAll(osmTags.keySet());
        return keys;
    }

    public boolean containsKey(String k){
        return osmTags.containsKey(k);
    }

    public void removeTag(String k){
        if (osmTags.containsKey(k)) osmTags.remove(k);
    }
}
