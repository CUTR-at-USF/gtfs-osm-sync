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

package edu.usf.cutr.go_sync.gui.object;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Khoa Tran
 * TagReportTableModel.java is mainly used for displaying bus stop data in Report
 */
public class TagReportTableModel extends AbstractTableModel {
    private String[] columnNames = {"Tag Names",
    "GTFS Values","",
    "OSM Values","",
    "New Values"};
    private Object[][] data;

    public TagReportTableModel(int maxRow){
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
        /*
        if(isComponent(row, col) && col!=0 && col!=3){
            return new JCheckBox(data[row][col].toString());
        }*/
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
        /*
        if(c==1 || c==2){
            return Boolean.class;
        }*/
        if(getValueAt(0, c)!=null)
            return getValueAt(0, c).getClass();
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int row, int col){
        // only the checkbox columns are editable
        if(col==2 || col==4) {
            String gtfs = (String)data[row][1];
            String osm = (String)data[row][3];
            if((gtfs==null) || (gtfs.equals("")) || (osm!=null && osm.contains(gtfs))) return false;

            return true;
        }
        return false;
    }
    /*
     * Insert data into table
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void setRowValueAt(Object[] value, int row){
        for(int col=0; col<value.length; col++){
            setValueAt(value[col], row, col);
        }
    }
}