/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.cutr.go_sync.object;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author ktran
 */

public class StopTableModel extends AbstractTableModel {
    private String[] columnNames = {"Count",
    "GTFS Stop ID",
    "Route",
    "Stop Location (GTFS --> OSM)",
    "Location displacement", "Tags not in GTFS"};
    private Object[][] data;

    public StopTableModel(int maxRow){
        if(maxRow >=0) {
            data = new Object[maxRow][columnNames.length];
        }
        else System.out.println("Invalid number of row!");
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Insert data into table
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
/*        System.out.println("Setting value at " + row + "," + col
                + " to " + value
                + " (an instance of "
                + value.getClass() + ")");
*/
        data[row][col] = value;
        fireTableCellUpdated(row, col);
/*
        System.out.println("New value of data:");
        printDebugData();*/
        
    }

    public void setRowValueAt(Object[] value, int row){
        for(int col=0; col<value.length; col++){
            setValueAt(value[col], row, col);
        }
    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + data[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}