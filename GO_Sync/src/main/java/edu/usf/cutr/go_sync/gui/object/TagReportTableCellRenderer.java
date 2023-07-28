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

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author Khoa Tran
 */
public class TagReportTableCellRenderer implements TableCellRenderer {
    DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();


    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        tableRenderer = (DefaultTableCellRenderer) tableRenderer.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);
        
        // set different background colors to same data vs. different data
        TableModel model = table.getModel();
        String gtfs = (String)model.getValueAt(row, 1);
        String osm = (String)model.getValueAt(row, 3);
//        if((gtfs==null) || (gtfs.equals("")) || (osm!=null && osm.contains(gtfs))) {
        if (!isSelected) {
            if ((osm != null && gtfs != null && osm.equalsIgnoreCase(gtfs))) {
                tableRenderer.setBackground(Color.LIGHT_GRAY);
            } else {
                tableRenderer.setBackground(table.getBackground());
            }
        }
        tableRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        return tableRenderer;

    }
}
