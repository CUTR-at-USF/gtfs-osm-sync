/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
 * @author ktran
 */
public class RouteMemberTableCellRenderer implements TableCellRenderer{
    DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        tableRenderer = (DefaultTableCellRenderer) tableRenderer.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);

        // set different background colors to same data vs. different data
        TableModel model = table.getModel();
        String gtfs = (String) model.getValueAt(row, 0);
        String osm = (String) model.getValueAt(row, 1);
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
