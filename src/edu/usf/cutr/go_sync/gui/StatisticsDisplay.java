/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StatisticsDisplay.java
 *
 * Created on Sep 28, 2010, 10:42:37 PM
 */

package edu.usf.cutr.go_sync.gui;

import edu.usf.cutr.go_sync.object.Stop;
import edu.usf.cutr.go_sync.object.StopTableModel;
import edu.usf.cutr.go_sync.osm.HttpRequest;
import edu.usf.cutr.go_sync.tools.OsmDistance;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

/**
 *
 * @author ktran
 */
public class StatisticsDisplay extends javax.swing.JFrame {
    private Hashtable report;

    private StopTableModel editedStopTableModel;
    private final double ERROR_TO_ZERO = 0.5;       // acceptable error while calculating distance ~= consider as 0
    private HttpRequest osmRequest = new HttpRequest(null);

    public static HashSet<String> contributors = new HashSet<String>();

    /** Creates new form StatisticsDisplay */
    public StatisticsDisplay(Hashtable r) {
        report = new Hashtable();
        report.putAll(r);

        editedStopTableModel = new StopTableModel(report.size());
        insertStopDataToTableModel();

        initComponents();
    }

    private double getDisplacementTotal(ArrayList<Double> values){
        double sum=0;
        if(values.size()>0){
            for(int i=0;i<values.size(); i++){
                sum+=values.get(i);
            }
        }
        return sum;
    }

    private double getDisplacementMean(ArrayList<Double> values){
        if(values.size()>0) return getDisplacementTotal(values)/values.size();
        return 0;
    }

    private double getDisplacementMax(ArrayList<Double> values){
        double max = 0;
        if(values.size()>0) {
            max = values.get(0);
            for (int i=1; i<values.size()-1; i++){
                if(max<values.get(i)){
                    max = values.get(i);
                }
            }
        }
        return max;
    }

    private double getDisplacementMin(ArrayList<Double> values){
        double min = 0;
        if(values.size()>0) {
            min = values.get(0);
            for (int i=1; i<values.size()-1; i++){
                if(min>values.get(i)){
                    min = values.get(i);
                }
            }
        }
        return min;
    }

    private double getDisplacementStandardDeviation(ArrayList<Double> values){
        double mean = getDisplacementMean(values);
        double sumSquareDiff = 0;
        if(values.size()>0) {
            for (int i=1; i<values.size()-1; i++){
                sumSquareDiff += (values.get(i)-mean)*(values.get(i)-mean);
            }
            sumSquareDiff = sumSquareDiff/mean;
        }
        return Math.sqrt(sumSquareDiff);
    }

    private String getDisplacementDistributionChartLink(ArrayList<Double> values){
        String link="http://chart.apis.google.com/chart";
        int zero5 = 0, five10 = 0, ten15 = 0, fifteen20 = 0, twentyUp = 0;
        for(int i=0; i<values.size(); i++){
            if(values.get(i)<=5) zero5++;
            else if(values.get(i)<=10) five10++;
            else if(values.get(i)<=15) ten15++;
            else if(values.get(i)<=20) fifteen20++;
            else twentyUp++;
        }
        link+="?chxl=0:|0-5|5-10|10-15|15-20|20%2B|1:|0|"+values.size()/2+"|"+values.size()+
                "&chxr=0,5,100&chxt=x,y&chs=600x300&cht=bvg&chco=76A4FB&chd=t:"+zero5+","+five10+","+
                ten15+","+fifteen20+","+twentyUp+"&chg=20,50&chtt=Displacement+Distribution";
        return link;
    }

    private void insertStopDataToTableModel(){
        ArrayList<Stop> reportKeys = new ArrayList<Stop>();
        reportKeys.addAll(report.keySet());
        int count=0;
        Writer output = null;
        String fname = "editedStops.txt";
        File file = new File(fname);

        ArrayList<Double> allStopDisplacement = new ArrayList<Double>();

        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write("Count,GTFS_Stop_ID,Route,Gtfs_Lat,Gtfs_Lon,Osm_Lat,Osm_Lon,Location_Displacement,Tags_not_in_GTFS,Version_History\n");
            for(int i=0; i<reportKeys.size(); i++){
                Stop st = reportKeys.get(i);
                String gtfsid = st.getStopID();
                String category = st.getReportCategory();
                String routeText = st.getTag("route_ref");
                if (category.equals("UPLOAD_CONFLICT") || category.equals("UPLOAD_NO_CONFLICT")) {
                    count++;
                    editedStopTableModel.setRowValueAt(new Object[] {count, gtfsid, "N/A", "deleted", "N/A","Unknown"}, count-1);
                    output.write(count+","+gtfsid+","+routeText+","+st.getLat()+","+st.getLon()+",N/A"+",N/A,N/A,N/A"+",N/A"+ "\n");
                } else if (category.equals("MODIFY") || category.equals("NOTHING_NEW")) {
                    Stop osmst = ((ArrayList<Stop>)report.get(st)).get(0);
                    String location = "("+st.getLat()+";"+st.getLon()+")-->"+"("+osmst.getLat()+";"+osmst.getLon()+")";
                    double displacement = OsmDistance.distVincenty(st.getLat(), st.getLon(), osmst.getLat(), osmst.getLon());
                    if(displacement>ERROR_TO_ZERO) {
                        allStopDisplacement.add((Double)displacement);
                    }

                    // Get all tags not in GTFS but in OSM (notice: this is opposite from compareData)
                    Hashtable diff = osmst.compareOsmTags2(st.getTags());
                    String diffText="";
                    if(!diff.isEmpty()){
                        ArrayList<String> tagNames = new ArrayList<String>();
                        tagNames.addAll(diff.keySet());
                        for(int tag=0; tag<tagNames.size(); tag++){
                            String tagValue = (String)diff.get(tagNames.get(tag));
                            // replace all comma for excel delimiter purpose
                            tagValue = tagValue.replace(',', ';');
                            diffText+=tagNames.get(tag)+"="+tagValue+"|";
                        }
                    }

                    if(displacement>ERROR_TO_ZERO || diffText.equals("")) {
                        count++;
                        editedStopTableModel.setRowValueAt(new Object[] {count, gtfsid, routeText, location, displacement, diffText}, count-1);
                        String userHistory = osmRequest.getAllUsersOfNode(osmst.getOsmId());
                        output.write(count+","+gtfsid+","+routeText+","+st.getLat()+","+st.getLon()+","+osmst.getLat()+","+osmst.getLon()+","+displacement+","+diffText+","+userHistory+"\n");
                    }
                }
            }

            output.write("min = "+getDisplacementMin(allStopDisplacement)+" \n");
            output.write("max = "+getDisplacementMax(allStopDisplacement)+" \n");
            output.write("mean = "+getDisplacementMean(allStopDisplacement)+" \n");
            output.write("standard deviation = "+getDisplacementStandardDeviation(allStopDisplacement)+" \n");
            output.write("displacement distribution = "+getDisplacementDistributionChartLink(allStopDisplacement)+" \n");
            output.write("Contributors = "+contributors+" \n");

            output.close();
            System.out.println("Your file: "+fname+" has been written");
            System.out.println("Contributors = "+contributors);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        editedStopTable = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        editedStopTable.setModel(editedStopTableModel);
        editedStopTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        editedStopTable.setName("editedStopTable"); // NOI18N
        jScrollPane1.setViewportView(editedStopTable);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 923, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(276, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    *//*
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StatisticsDisplay().setVisible(true);
            }
        });
    }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable editedStopTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables

}
