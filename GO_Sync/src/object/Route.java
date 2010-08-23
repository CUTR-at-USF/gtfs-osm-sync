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

public class Route extends OsmPrimitive implements Comparable{
    private String routeRef, operatorName;
    private HashSet<RelationMember> osmMembers;

    public Route(String rref, String op) {
        osmTags = new Hashtable();
        osmMembers = new HashSet<RelationMember>();
        routeRef = rref;
        operatorName = op;
    }

    public Route(Route r) {
        this.osmTags = new Hashtable();
        this.osmTags.putAll(r.osmTags);
        this.osmMembers = new HashSet<RelationMember>();
        this.osmMembers.addAll(r.getOsmMembers());
        this.routeRef = r.getRouteRef();
        this.operatorName = r.getOperatorName();
        this.setOsmId(r.getOsmId());
        this.setOsmVersion(r.getOsmVersion());
        this.setReportCategory(r.getReportCategory());
        this.setReportText(r.getReportText());
        this.setStatus(r.getStatus());
    }

    public void addOsmMember(RelationMember osmNodeId){
        osmMembers.add(osmNodeId);
    }

    public void addOsmMembers(HashSet<RelationMember> oMembers){
        osmMembers.addAll(oMembers);
    }

    public HashSet<RelationMember> getOsmMembers(){
        return osmMembers;
    }

    public String getOperatorName(){
        return operatorName;
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
        if(this.compareOperatorName(r) && r.getRouteRef().equals(this.getRouteRef())) {
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
        String id = this.getRouteRef();
        return id.hashCode();
    }
}