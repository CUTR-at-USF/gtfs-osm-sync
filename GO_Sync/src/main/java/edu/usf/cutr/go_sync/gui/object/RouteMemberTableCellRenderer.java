/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.cutr.go_sync.gui.object;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ktran
 */
public class RouteMemberTableCellRenderer implements TableCellRenderer{
    DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        tableRenderer = (DefaultTableCellRenderer) tableRenderer.getTableCellRendererComponent(table,
                value, isSelected, hasFocus, row, column);

        tableRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        return tableRenderer;

    }
}
