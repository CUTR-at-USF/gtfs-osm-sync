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

/**
 *
 * @author Khoa Tran
 */
public class RelationMember {
    private String ref, type, role;
    public RelationMember(String ref, String type, String role){
        this.ref = ref;
        this.type = type;
        this.role = role;
    }

    public RelationMember(RelationMember rm){
        this.ref = rm.getRef();
        this.type = rm.getType();
        this.role = rm.getRole();
    }

    public String getRef(){
        return ref;
    }

    public String getType(){
        return type;
    }

    public String getRole(){
        return role;
    }

    public int compareTo(Object o){
        RelationMember rm = (RelationMember) o;
        if(this.ref.equals(rm.getRef()) && this.type.equals(rm.getType())){
            return 0;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof RelationMember) {
            if (this.compareTo((RelationMember) o)==0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        String id = this.ref+this.type;
        return id.hashCode();
    }
}
