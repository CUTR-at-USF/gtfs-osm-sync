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
public class NodeHistoryParser extends DefaultHandler{
    private String latestUser = "";
    private int latestVersion = -1;
    public NodeHistoryParser(){
        
    }
    
    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("node")) {
            AttributesImpl attImplNode = new AttributesImpl(attributes);
            if(latestVersion<Integer.parseInt(attImplNode.getValue("version"))){
                latestUser = attImplNode.getValue("user");
                latestVersion = Integer.parseInt(attImplNode.getValue("version"));
            }
        }
    }

    @Override public void endElement (String uri, String localName, String qName) throws SAXException {
    }

    public String getLatestUser(){
        return latestUser;
    }
}