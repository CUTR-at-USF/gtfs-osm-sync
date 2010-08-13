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
package io;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import object.Stop;

public class WriteFile {
//    List<Stop> stops = new ArrayList<Stop>();
    public WriteFile(String fname, HashSet<Stop> s){
        ArrayList<Stop> ls = new ArrayList<Stop>(s.size());
        ls.addAll(s);
        writeStopToFile(fname, ls);
    }
    
    public WriteFile(String fname, List<Stop> st){
        writeStopToFile(fname, st);
    }

    public WriteFile(String fname, String contents) {
        Writer output = null;
        File file = new File(fname);
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(contents);
            output.close();
            System.out.println("Your file: "+fname+" has been written");
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public WriteFile(String fname, Hashtable r) {
        Hashtable report = new Hashtable();
        report.putAll(r);

        HashSet<Stop> reportKeys = new HashSet<Stop>();
        Writer output = null;
        File file = new File(fname);
        try {
            output = new BufferedWriter(new FileWriter(file));
            reportKeys.addAll(report.keySet());
            Iterator it = reportKeys.iterator();
            int count = 1;
            // Print new gtfs stop with problems first
            while (it.hasNext()){
                Stop st = new Stop((Stop)it.next());
                // if arraylist
                if (!report.get(st).equals("none")){
                    output.write(count+". "+st.getStopID()+","+st.getStopName()+","+
                            st.getLat()+","+st.getLon()+"\n");
                    ArrayList<Stop> ls = new ArrayList<Stop>();
                    ls.addAll((ArrayList<Stop>)report.get(st));
                    for(int i=0; i<ls.size(); i++){
                        output.write("      "+(i+1)+". "+ls.get(i).printOSMStop()+"\n");
                        output.write(" REPORT: "+ls.get(i).getTag("REPORT")+"\n\n");
                    }
                    output.write("\n\n");
                    count++;
                }
            }
            // Print new gtfs stop with no issues
            Iterator iter = reportKeys.iterator();
            while (iter.hasNext()){
                Stop st = new Stop((Stop)iter.next());
                // if no arraylist, meaning successfully added or nothing needs to be changed from last upload
                if (report.get(st).equals("none")){
                    output.write(count+". "+st.getStopID()+","+st.getStopName()+","+
                            st.getLat()+","+st.getLon()+"\n");
                    output.write(" REPORT: "+st.getTag("REPORT")+"\n\n");
                    count++;
                }
            }
            output.close();
            System.out.println("Your file: "+fname+" has been written");
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public void writeStopToFile(String fname, List<Stop> st){
        List<Stop> stops = new ArrayList<Stop>();
        stops.addAll(st);
        Writer output = null;
        File file = new File(fname);
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write("stop_id,stop_name,stop_lat,stop_lon\n");
            for(int i=0; i<stops.size(); i++){
                output.write(stops.get(i).getStopID()+","+stops.get(i).getStopName()+","+
                             stops.get(i).getLat()+","+stops.get(i).getLon()+"\n");
            }
            output.close();
            System.out.println("Your file: "+fname+" has been written");
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}
