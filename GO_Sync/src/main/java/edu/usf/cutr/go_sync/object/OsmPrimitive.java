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
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author Khoa Tran
 */
public class OsmPrimitive {
    Hashtable osmTags;

    String primitiveType;
    ArrayList<String> wayNdRefs;

    //TODO change reportCategory to enum
    private String status, osmVersion, osmid, reportCategory, reportText, lastEditedOsmUser="", lastEditedOsmDate="";
    public OsmPrimitive(String primitiveType){
        osmTags = new Hashtable();
        this.primitiveType = primitiveType;
        wayNdRefs = new ArrayList<>();
    }

    public void addTag(String k, String v){
        if(!osmTags.containsKey(k)) {
            if (v!=null && !v.equals("")) {
                osmTags.put(k, v);
            }
            else {
                osmTags.put(k, "none");
            }
        }
    }

    /*
     * Cannot use osmTags.putAll(h)
     * since we don't want the new data overwrite the old one. Use addAndOverwriteTags instead
     * */
    public void addTags(Hashtable h){
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(h.keySet());
        for (int i=0; i<keys.size(); i++){
            String k = keys.get(i);
            if(!osmTags.containsKey(k)) {
                osmTags.put(k,h.get(k));
            }
        }
    }

    public void addTags(Map h){
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(h.keySet());
        for (int i=0; i<keys.size(); i++){
            String k = keys.get(i);
            if(!osmTags.containsKey(k)) {
                osmTags.put(k,h.get(k));
            }
        }
    }

    public void addAndOverwriteTag(String k, String v){
        if (v!=null && !v.equals("")) {
            osmTags.put(k, v);
        }
        else {
            osmTags.put(k, "none");
        }
    }

    public void addAndOverwriteTags(Hashtable h){
        osmTags.putAll(h);
    }

    public String getTag(String k){
        if (osmTags.containsKey(k))
            return (String)osmTags.get(k);
        return null;
    }

    public Hashtable getTags(){
        return osmTags;
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

    /* only has 4 possible value: n=new; m=modify; d=delete; e=empty
     * used for upload osmchange
     * */
    public void setStatus(String v){
        status = v;
    }

    public String getStatus(){
        return status;
    }

    public void setOsmVersion(String v){
        osmVersion = v;
    }

    public String getOsmVersion(){
        return osmVersion;
    }

    public void setOsmId(String v){
        osmid = v;
    }

    public String getOsmId(){
        return osmid;
    }

    /*
     * For stop:
     *      1) MODIFY
     *      2) NOTHING_NEW
     *      3) UPLOAD_CONFLICT
     *      4) UPLOAD_NO_CONFLICT
     * */
    public void setReportCategory(String v){
        reportCategory = v;
    }

    public String getReportCategory(){
        return reportCategory;
    }

    public void setReportText(String v){
        reportText = v;
    }

    public String getReportText(){
        return reportText;
    }

    public void setLastEditedOsmUser(String v){
        if(v!=null) lastEditedOsmUser = v;
    }

    public String getLastEditedOsmUser(){
        return lastEditedOsmUser;
    }

    public void setLastEditedOsmDate(String v){
        if(v!=null) lastEditedOsmDate = v;
    }

    public String getLastEditedOsmDate(){
        return lastEditedOsmDate;
    }

    public void setWayNdRefs(ArrayList<String> v) {
        //System.out.println("setWayNdRefs: size " + v.size());
        //System.out.println(v.toString());
        wayNdRefs = v;
    }

    public ArrayList<String> getWayNdRefs() {
        return wayNdRefs;
    }

    public void setPrimitiveType(String primitiveType) {
        this.primitiveType = primitiveType;
    }

    public String getPrimitiveType() {
        return primitiveType;
    }
}
