/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.cutr.go_sync.gui;

import edu.usf.cutr.go_sync.osm.HttpRequest;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author ktran
 */
public class DeletedStopHistory {
//    String elements[] = { "11386",  "13625", "13624", "13623", "13622", "13621", "13620", "13619", "13617"};  // Space Coast Deleted stops

//    String elements[] = { "7914",  "7913", "5403", "6452"};  // PSTA Deleted stops

    String elements[] = { "9529",  "9528", "9527", "9526",
                            "9525", "9524", "9523", "9522",
                            "9521", "9520", "9519", "9518",
                            "9517", "9516", "9515", "9514", 
                            "9513", "9512", "9511", "9510",
                            "9509", "9508", "9507", "9506",
                            "9505", "9504", "9503", "9502",
                            "9501", "9500", "9499", "9498",
                            "9497", "9496", "9495", "9494",
                            "9493", "9492", "9491", "9490",
                            "9489", "9488", "9487", "9486"};  // MDT Deleted stops

//2637, 6964, 6497, 7612, 7613      // HART


    HashSet<String> deletedStops = new HashSet(Arrays.asList(elements));

    private HttpRequest osmRequest = new HttpRequest();
    
    public DeletedStopHistory(){
        System.out.println("Initializing...");
        osmRequest.checkVersion();
//        osmRequest.getPastHistoryOfDeletedNode("6679342", deletedStops);  // space coast
//        osmRequest.getPastHistoryOfDeletedNode("5812427", deletedStops);  // psta
        osmRequest.getPastHistoryOfDeletedNode("6670405", deletedStops);    // mdt
    }

    public static void main(String[] args){
        new DeletedStopHistory();
    }
}
