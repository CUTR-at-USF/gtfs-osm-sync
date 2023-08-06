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

import edu.usf.cutr.go_sync.gui.ReportViewer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import edu.usf.cutr.go_sync.gui.object.StopTableInfo;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Khoa Tran
 */
public class BooleanMouseListener implements MouseListener{
    private JTable dataTable;
    private ReportViewer reportViewer;

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

        Boolean otherCheckBox;
        String otherData, insertData = dataValue;
        int otherCheckColumn;
        int otherDataColumn;

        // look at data, checkBox info for other columns
        // FIXME: needs some cleaning up for better readability
        if (column == StopTableInfo.GTFS_CHECK_COL) {
            otherCheckColumn = StopTableInfo.OSM_CHECK_COL;
            otherDataColumn = otherCheckColumn - 1;
            otherCheckBox = (Boolean) dataTable.getValueAt(row, otherCheckColumn);
            otherData = (String) dataTable.getValueAt(row, otherDataColumn);
        } else { // column == StopTableInfo.OSM_CHECK_COL
            otherCheckColumn = StopTableInfo.GTFS_CHECK_COL;
            otherDataColumn = otherCheckColumn - 1;
            otherCheckBox = (Boolean) dataTable.getValueAt(row, otherCheckColumn);
            otherData = (String) dataTable.getValueAt(row, otherDataColumn);
        }

        if((dataValue!=null) && !(dataValue.equals(""))){
            // look at data, checkBox info for other columns
            // FIXME: needs some cleaning up for better readability
            if(column==StopTableInfo.GTFS_CHECK_COL) {
                if(otherData!=null && !(otherData.equals("")))
                    insertData = addToOSMMultiValue(dataValue, otherData);
            } else { // column == StopTableInfo.OSM_CHECK_COL
                if(otherCheckBox) {
                    if (otherData!=null && !(otherData.equals("")))
                        insertData = addToOSMMultiValue(dataValue, otherData);
                    else {
                        insertData = dataValue;
                        otherCheckBox = false;
                        otherData = "";
                    }
                } else {
                    insertData = dataValue;
                }
            }

            if(row==0 || row==1){ // lat || lon
                if((Boolean)currentValue) {
                    dataTable.setValueAt(dataValue, row, StopTableInfo.NEW_VALUE_DATA_COL);
                    dataTable.setValueAt(new Boolean(false), row, otherCheckColumn);
                } else {
                    dataTable.setValueAt(otherData, row, StopTableInfo.NEW_VALUE_DATA_COL);
                    dataTable.setValueAt(new Boolean(true), row, otherCheckColumn);
                }
                return;
            }

            if((Boolean)currentValue) {
                if(otherCheckBox)
                    dataTable.setValueAt(insertData, row, StopTableInfo.NEW_VALUE_DATA_COL);
                else {
                    dataTable.setValueAt(dataValue, row, StopTableInfo.NEW_VALUE_DATA_COL);
                }
            } else {
                if(otherCheckBox)
                    dataTable.setValueAt(otherData, row, StopTableInfo.NEW_VALUE_DATA_COL);
                else {
                    if(otherData!=null && !(otherData.equals(""))) {
                        dataTable.setValueAt(new Boolean(true), row, otherCheckColumn);
                        dataTable.setValueAt(otherData, row, StopTableInfo.NEW_VALUE_DATA_COL);
                    } else {
                        dataTable.setValueAt(new Boolean(false), row, otherCheckColumn);
                        dataTable.setValueAt(new Boolean(false), row, column);
                        dataTable.setValueAt(otherData, row, StopTableInfo.NEW_VALUE_DATA_COL);
                    }
                }
            }
        }

        if ((dataValue == null || dataValue.isEmpty()) && (otherData == null || otherData.isEmpty())) {
            insertData = null;
            dataTable.setValueAt(insertData, row, StopTableInfo.NEW_VALUE_DATA_COL);
        }

        String tagName = (String)dataTable.getValueAt(row, 0);
        if (tagName.equals("public_transport:version")) {
            reportViewer.PTVersionChanged();
        }
    }

    public BooleanMouseListener(JTable table) {
        dataTable = table;
    }

    public BooleanMouseListener(JTable table, ReportViewer rviewer) {
        dataTable = table;
        reportViewer = rviewer;
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

    private String addToOSMMultiValue(String dataValue, String otherData) {
        ArrayList<String> dataValueList = new ArrayList<>(Arrays.asList(dataValue.split(";")));
        ArrayList<String> otherDataList = new ArrayList<>(Arrays.asList(otherData.split(";")));
        dataValueList.replaceAll(String::trim);
        otherDataList.replaceAll(String::trim);
        for (String data : otherDataList) {
            if (!dataValueList.contains(data)) {
                dataValueList.add(data);
            }
        }
        return String.join(";", dataValueList);
    }
}
