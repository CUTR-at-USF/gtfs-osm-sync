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
 * RouteMemberTableModel.java is mainly used for displaying bus route data in Report
 */
public class RouteMemberTableModel extends AbstractTableModel {
    private String[] columnNames = {"GTFS Member List",
    "OSM Member List (ordered by 'New Member List' if PTv2)",
    "New Member List"};
    private Object[][] data;

    public RouteMemberTableModel(int maxRow){
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

    @Override
    public Class getColumnClass(int c) {
        if(getValueAt(0, c)!=null)
            return getValueAt(0, c).getClass();
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int row, int col){
        // only the checkbox columns are editable
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