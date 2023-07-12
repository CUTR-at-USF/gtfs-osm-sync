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

import edu.usf.cutr.go_sync.tag_defs;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 *
 * @author Khoa Tran
 */

public class Route extends OsmPrimitive implements Comparable{
    private String routeId, routeRef, operatorName;
    private LinkedHashSet<RelationMember> osmMembers;

    public Route(String rId, String rRef, String op) {
        super("relation");
        osmTags = new Hashtable();
        osmMembers = new LinkedHashSet <RelationMember>();
        routeId = rId;
        if(rId!=null) this.osmTags.put(tag_defs.OSM_ROUTE_ID_KEY, rId);
        routeRef = rRef;
        operatorName = op;
    }

    public Route(Route r) {
        super("relation");
        this.osmTags = new Hashtable();
        if(r.osmTags!=null) this.osmTags.putAll(r.osmTags);
        this.osmMembers = new LinkedHashSet <RelationMember>();
        if(r.getOsmMembers()!=null) this.osmMembers.addAll(r.getOsmMembers());
        this.routeId = r.getRouteId();
        String ori = (String)r.getTags().get(tag_defs.OSM_ROUTE_ID_KEY);
        if(ori==null) this.osmTags.put(tag_defs.OSM_ROUTE_ID_KEY, routeId);
        this.routeRef = r.getRouteRef();
        this.operatorName = r.getOperatorName();
        if(r.getOsmId()!=null) this.setOsmId(r.getOsmId());
        if(r.getOsmVersion()!=null) this.setOsmVersion(r.getOsmVersion());
        if(r.getReportCategory()!=null) this.setReportCategory(r.getReportCategory());
        if(r.getReportText()!=null) this.setReportText(r.getReportText());
        if(getStatus()!=null) this.setStatus(r.getStatus());
        this.setLastEditedOsmDate(r.getLastEditedOsmDate());
        this.setLastEditedOsmUser(r.getLastEditedOsmUser());
    }

    public void addOsmMember(RelationMember osmNodeId){
        osmMembers.add(osmNodeId);
    }

    public void addOsmMembers(LinkedHashSet <RelationMember> oMembers){
        osmMembers.addAll(oMembers);
    }

    public void removeOsmMember(RelationMember rm){
        if(rm!=null){
            osmMembers.remove(rm);
        }
    }

    public RelationMember getOsmMember(String ref) {
        Iterator it = osmMembers.iterator();
        while(it.hasNext()){
            RelationMember rm = (RelationMember)it.next();
            if(rm.getRef().equals(ref)){
                return rm;
            }
        }
        return null;
    }

    public LinkedHashSet <RelationMember> getOsmMembers(){
        return osmMembers;
    }

    public String getOperatorName(){
        return operatorName;
    }

    public String getRouteId(){
        return routeId;
    }

    public String getRouteRef(){
        return routeRef;
    }

    public boolean compareOperatorName(Route o) {
        if (o.getOperatorName()!=null && this.getOperatorName()!=null) {
            return OperatorInfo.isTheSameOperator(this.getOperatorName())
                    && OperatorInfo.isTheSameOperator(o.getOperatorName());
        }
        return false;
    }

    public int compareTo(Object o){
        Route r = (Route) o;
        if(this.compareOperatorName(r) && r.getRouteId().equals(this.getRouteId())) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Route) {
            if (this.compareTo((Route) o)==0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        String id = this.getRouteId();
        return id.hashCode();
    }

    @Override
    public String toString(){
        return this.getRouteId();
    }
}
