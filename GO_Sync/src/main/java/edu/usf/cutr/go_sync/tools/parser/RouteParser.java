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
import java.util.Hashtable;
import edu.usf.cutr.go_sync.object.RelationMember;
import java.util.LinkedHashSet;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Khoa Tran
 */
public class RouteParser extends DefaultHandler {
    private Hashtable tempTag;
    private LinkedHashSet<RelationMember> tempMembers;
    private ArrayList<AttributesImpl> xmlRelations;
    //xmlTags<String, String> ----------- xmlMembers<String(refID), AttributesImpl>
    private ArrayList<Hashtable> xmlTags;
    private ArrayList<LinkedHashSet<RelationMember>> xmlMembers;
    public RouteParser(){
        xmlRelations = new ArrayList<AttributesImpl>();
        xmlTags = new ArrayList<Hashtable>();
        xmlMembers = new ArrayList<LinkedHashSet<RelationMember>>();
    }
    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("relation")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            xmlRelations.add(attImpl);
            tempTag = new Hashtable();      // start to collect tags of that relation
            tempMembers = new LinkedHashSet<RelationMember>();
        }
        if (tempTag!=null && qname.equals("tag")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            tempTag.put(attImpl.getValue("k"), attImpl.getValue("v"));         // insert key and value of that tag into Hashtable
        }
        if (tempMembers!=null && qname.equals("member")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            RelationMember rm = new RelationMember(attImpl.getValue("ref"),attImpl.getValue("type"),attImpl.getValue("role"));
            rm.setStatus("OSM server");
            tempMembers.add(rm);
        }
    }

    @Override public void endElement (String uri, String localName, String qName) throws SAXException {
        if (qName.equals("relation")) {
            xmlTags.add(tempTag);
            xmlMembers.add(tempMembers);
            tempTag = null;
            tempMembers = null;
        }
    }

    public ArrayList<AttributesImpl> getRelations(){
        return xmlRelations;
    }

    public ArrayList<Hashtable> getTags(){
        return xmlTags;
    }

    public ArrayList<LinkedHashSet<RelationMember>> getMembers(){
        return xmlMembers;
    }
}