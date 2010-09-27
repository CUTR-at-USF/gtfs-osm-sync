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

package tools.parser;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author Khoa Tran
 */
public class OsmVersionParser extends DefaultHandler{
    private String apiVersion;
    private boolean isCorrect=false;
    public OsmVersionParser(String v){
        apiVersion = v;
    }
    @Override public void startElement(String namespaceURI, String localName, String qname, Attributes atts) throws SAXException {
        if (qname.equals("version")) {
            AttributesImpl attImpl = new AttributesImpl(atts);
            double minVersion = Double.parseDouble(attImpl.getValue("minimum"));
            double maxVersion = Double.parseDouble(attImpl.getValue("maximum"));
            double currVersion = Double.parseDouble(apiVersion);
            if (minVersion<=currVersion && maxVersion>=currVersion) {
                isCorrect = true;
            }
        }
    }
    public boolean isSupportVersion(){
        return isCorrect;
    }
}
