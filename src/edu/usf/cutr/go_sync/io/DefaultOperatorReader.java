/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.cutr.go_sync.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import edu.usf.cutr.go_sync.object.DefaultOperator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author megordon
 */
public class DefaultOperatorReader {

        public List<DefaultOperator> readOperators(String fName){
       
        String thisLine;
        String [] elements;
        List<DefaultOperator> ops = new ArrayList<DefaultOperator>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fName));
            boolean isFirstLine = true;
            while ((thisLine = br.readLine()) != null) { 
                if (isFirstLine) {
                    isFirstLine = false;
                }
                else {
                    thisLine = thisLine.trim();
                    
                    if(thisLine.contains("\"")) {
                         String[] temp = thisLine.split("\"");
                         ArrayList l = new ArrayList();
                         l.add(temp[1].toString());
                         int commas = temp[1].replaceAll("[^,]", "").length();
                         
                         elements = thisLine.split(",");
                         
                         for(int i = 0; i < elements.length; i++) {
                             if(i > commas) {
                                 l.add(elements[i].toString());
                             }
                         }
                         elements = (String[]) l.toArray(new String[0]);
                    } 
                    else {
                        elements = thisLine.split(",");    
                    }
                    
                    
                    DefaultOperator op = new DefaultOperator(elements[0]);
                    
                    System.out.print(":: ");
                    for(int i = 0; i < elements.length; i++) {
                        System.out.print(elements[i] + " : ");
                        
                        if(i == 1 && elements[1] != null) {
                            op.setOperatorAbbreviation(elements[1]);
                        }

                        if(i == 2 && elements[2] != null) {
                            try{
                            op.setNtdID(Integer.parseInt(elements[2]));
                            } catch(NumberFormatException ex) {
                                System.err.println("Error parsing NTD ID " + elements[2] + " - " + ex.getLocalizedMessage());
                            }
                        }
                        
                        if(i == 3 && elements[3] != null) {
                            op.setGtfsURL(elements[3]);
                        }
                        
                        if(i == 4 && elements[4] != null) {
                            try{
                            op.setStopDigits(Integer.parseInt(elements[4]));
                            } catch(NumberFormatException ex) {
                                System.err.println("Error parsing stop digits " + elements[4] + " - " + ex.getLocalizedMessage());
                            }
                        }
                    }
                    System.out.println(" :: ");
                    
                    ops.add(op);
                }
            } 
            return ops;
        }
        catch (IOException e) {
            System.err.println("Error reading in default operators: " + e.getLocalizedMessage());
            return null;
        }
    }
}
