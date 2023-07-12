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
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Khoa Tran
 */
public class BusStopParser extends DefaultHandler{
    private Hashtable tempTag;
    private AttributesImpl attImplNode;
    private ArrayList<AttributesImpl> xmlNodes;
    private ArrayList<Hashtable> xmlTags;
    public BusStopParser(){
        xmlNodes = new ArrayList<AttributesImpl>();
        xmlTags = new ArrayList<Hashtable>();
    }
    
    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("node") || qname.equals("way") || qname.equals("changeset")) {
            attImplNode = new AttributesImpl(attributes);
            xmlNodes.add(attImplNode);
            tempTag = new Hashtable();      // start to collect tags of that node
        }
        if (qname.equals("tag")) {
            AttributesImpl attImpl = new AttributesImpl(attributes);
            //                System.out.println(attImpl.getValue("k") + attImpl.getValue("v"));
            tempTag.put(attImpl.getValue("k"), attImpl.getValue("v"));         // insert key and value of that tag into Hashtable
        }
    }

    @Override public void endElement (String uri, String localName, String qName) throws SAXException {
        if (qName.equals("node") || qName.equals("way")) {
            xmlTags.add(tempTag);
        }
    }

    public AttributesImpl getOneNode(){
        return attImplNode;
    }

    public Hashtable getTagsOneNode(){
        return tempTag;
    }

    public ArrayList<AttributesImpl> getNodes(){
        return xmlNodes;
    }

    public ArrayList<Hashtable> getTags(){
        return xmlTags;
    }
}