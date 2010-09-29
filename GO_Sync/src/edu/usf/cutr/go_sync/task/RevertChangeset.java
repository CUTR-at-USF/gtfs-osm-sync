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

package edu.usf.cutr.go_sync.task;

import java.awt.Toolkit;
import javax.swing.JTextArea;
import edu.usf.cutr.go_sync.osm.*;
import javax.swing.ProgressMonitor;

/**
 *
 * @author Khoa Tran
 */
public class RevertChangeset extends OsmTask{
    private HttpRequest osmRequest;

    private String revertChangesetId="";

    private ProgressMonitor progressMonitor;

    private JTextArea taskOutput;
    
    public RevertChangeset(String rcId, ProgressMonitor pm, JTextArea to){
        progressMonitor = pm;
        revertChangesetId = rcId;
        taskOutput = to;
        osmRequest = new HttpRequest(taskOutput);
    }

    @Override
    public Void doInBackground() {
        setProgress(0);
        
        updateProgress(2);
        this.setMessage("Checking API Version...");
        System.out.println("Initializing...");
        osmRequest.checkVersion();

        updateProgress(50);
        this.setMessage("Downloading changeset "+revertChangesetId+"...\nThis might take several minutes");
        osmRequest.downloadChangeSet(revertChangesetId);

        updateProgress(3);
        this.setMessage("Creating new changeset...");
        osmRequest.createChangeSet();

        updateProgress(40);
        this.setMessage("Uploading osmchange...");
        osmRequest.createChunks(osmRequest.getRevertUpload(), osmRequest.getRevertModify(), osmRequest.getRevertDelete(), null);

        updateProgress(2);
        this.setMessage("Closing changeset...");
        osmRequest.closeChangeSet();

        //make sure it's a complete task
        updateProgress(100);
        this.setMessage("Done...");
        System.out.println("Done...!!");
        return null;
    }

    @Override
    public void done() {
        Toolkit.getDefaultToolkit().beep();
        progressMonitor.setProgress(0);
    }
}
