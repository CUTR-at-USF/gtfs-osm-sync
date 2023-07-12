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

package edu.usf.cutr.go_sync.tools.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import edu.usf.cutr.go_sync.object.RelationMember;
import edu.usf.cutr.go_sync.object.Stop;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Khoa Tran
 */
public class ChangesetDownloadParser extends DefaultHandler{
    private String status="";
    private HashSet<Stop> upload, modify, delete;

    public ChangesetDownloadParser(){
        upload = new HashSet<Stop>();
        modify = new HashSet<Stop>();
        delete = new HashSet<Stop>();
    }
    
    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("create")) {
            status="create";
        } else if (qname.equals("modify")) {
            status="modify";
        } else if (qname.equals("delete")) {
            status="delete";
        } else if (qname.equals("node")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            String osmid = attImpl.getValue("id");
            String version = attImpl.getValue("version");
            Stop s = new Stop("node", osmid, null,null,"0","0");
            s.setOsmId(osmid);
            s.setOsmVersion(version);
            if (status.equals("create")) {
                delete.add(s);
            } else if (status.equals("modify")) {
                modify.add(s);
            } else if (status.equals("delete")) {
                upload.add(s);
            }
        }
    }

    @Override public void endElement (String uri, String localName, String qName) throws SAXException {
        if (qName.equals("create") || qName.equals("modify") || qName.equals("delete")) {
            status = "";
        }
    }
    
    public HashSet<Stop> getToBeDeletedStop(){
        return delete;
    }

    public HashSet<Stop> getToBeModifiedStop(){
        return modify;
    }

    public HashSet<Stop> getToBeUploadedStop(){
        return upload;
    }
}
