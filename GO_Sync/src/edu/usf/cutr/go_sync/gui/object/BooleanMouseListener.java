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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Khoa Tran
 */
public class BooleanMouseListener implements MouseListener{
    private JTable dataTable;

    private void checkBoxEvent(MouseEvent e) {
        TableColumnModel columnModel = dataTable.getColumnModel();
        int column = columnModel.getColumnIndexAtX(e.getX());
        int row    = e.getY() / dataTable.getRowHeight();
        Object currentValue;

        if(row >= dataTable.getRowCount() || row < 0 ||
                column >= dataTable.getColumnCount() || column < 0)
            return;

        currentValue = dataTable.getValueAt(row, column);

        if(!(currentValue instanceof Boolean))
            return;

        // add appropriate data to New Values
        String dataValue = (String)dataTable.getValueAt(row, column-1);
        if((dataValue!=null) && !(dataValue.equals(""))){
            Boolean otherCheckBox;
            String otherData, insertData="";
            int otherColumn;
            if(column==2) {
                otherCheckBox = (Boolean)dataTable.getValueAt(row, column+2);
                otherData = (String)dataTable.getValueAt(row, column+1);
                insertData = dataValue+";"+otherData;
                otherColumn = column+2;
            }
            else {
                otherCheckBox = (Boolean)dataTable.getValueAt(row, column-2);
                otherData = (String)dataTable.getValueAt(row, column-3);
                insertData = otherData+";"+dataValue;
                otherColumn = column-2;
            }

            if((Boolean)currentValue) {
                if(otherCheckBox) dataTable.setValueAt(insertData, row, 5);
                else {
                    dataTable.setValueAt(dataValue, row, 5);
                }
            }
            else {
                if(otherCheckBox) dataTable.setValueAt(otherData, row, 5);
                else {
                    dataTable.setValueAt(new Boolean(true), row, otherColumn);
                    dataTable.setValueAt(otherData, row, 5);
                }
            }
        }
    }

    public BooleanMouseListener(JTable table) {
        dataTable = table;
    }

    public void mouseClicked(MouseEvent e) {
        checkBoxEvent(e);
    }

    public void mouseEntered(MouseEvent e) {
//        checkBoxEvent(e);
    }

    public void mouseExited(MouseEvent e) {
//        checkBoxEvent(e);
    }


    public void mousePressed(MouseEvent e) {
//        checkBoxEvent(e);
    }

    public void mouseReleased(MouseEvent e) {
//        checkBoxEvent(e);
    }
}