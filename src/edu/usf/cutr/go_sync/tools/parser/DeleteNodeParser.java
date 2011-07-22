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

import edu.usf.cutr.go_sync.object.OsmPrimitive;
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
public class DeleteNodeParser extends DefaultHandler{
    private String osmid=null;
    private Hashtable<String, String> deletedNodes;
    private HashSet<String> inputNodeNames;

    public DeleteNodeParser(HashSet<String> in){
        deletedNodes = new Hashtable<String, String>();
        inputNodeNames = new HashSet<String>();
        inputNodeNames.addAll(in);
    }
    
    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("tag")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            if(attImpl.getValue("k").equals("gtfs_id")){
                if(inputNodeNames.contains(attImpl.getValue("v")) && osmid!=null){
                    deletedNodes.put(attImpl.getValue("v"), osmid);
                    inputNodeNames.remove(attImpl.getValue("v"));
                }
            }
        } else if (qname.equals("node")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            osmid = attImpl.getValue("id");
        }
    }

    @Override public void endElement (String uri, String localName, String qName) throws SAXException {
        if (qName.equals("node") ) {
            osmid = null;
        }
    }
    
    public Hashtable<String, String> getDeletedNodes(){
        System.out.println("input set remains "+inputNodeNames.size());
        return deletedNodes;
    }
}
