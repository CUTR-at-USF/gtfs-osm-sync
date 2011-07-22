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

import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Khoa Tran
 */
public class OsmPrimitiveHistoryParser extends DefaultHandler{
    private String userHistory;

    private HashSet<String> users;
    public OsmPrimitiveHistoryParser(){
        userHistory = "";
        users = new HashSet<String>();
    }

    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes attributes) throws SAXException {
        if (qname.equals("node") || qname.equals("changeset")) {
            userHistory+= "version "+attributes.getValue("version")+" = "+attributes.getValue("user")+
                    "(changeset = "+attributes.getValue("changeset")+")|";
            users.add(attributes.getValue("user"));
        }
    }

    public String getAllUsers(){
        return userHistory.substring(0, userHistory.length()-1);
    }

    public HashSet<String> getUsersInSet(){
        return users;
    }
}