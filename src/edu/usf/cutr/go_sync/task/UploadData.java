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
import java.util.HashSet;
import java.util.Hashtable;
import javax.swing.JProgressBar;
import edu.usf.cutr.go_sync.object.Stop;
import edu.usf.cutr.go_sync.osm.HttpRequest;
/**
 *
 * @author Khoa Tran
 */
public class UploadData extends OsmTask{
    private JProgressBar progressBar;
    private HttpRequest osmRequest;
    private HashSet<Stop> upload, modify, delete;
    private Hashtable finalRoutes;

    public UploadData(JProgressBar pb, HttpRequest or, HashSet<Stop> u, HashSet<Stop> m, HashSet<Stop> d, Hashtable fRoutes){        
        upload = new HashSet<Stop>();
        upload.addAll(u);

        modify = new HashSet<Stop>();
        modify.addAll(u);

        delete = new HashSet<Stop>();
        delete.addAll(u);

        finalRoutes = new Hashtable();
        finalRoutes.putAll(fRoutes);

        osmRequest = or;
        progressBar = pb;
    }

    @Override
    public Void doInBackground() {
        setProgress(0);
        updateProgress(1);
        this.setMessage("Checking version ... ");
        osmRequest.checkVersion();

        updateProgress(8);
        this.setMessage("Creating changeset ... ");
        osmRequest.createChangeSet();

        updateProgress(70);
        this.setMessage("Creating and uploading chunks ... \nThis might take several minutes ...");
        osmRequest.createChunks(upload, modify, delete, finalRoutes);

        updateProgress(10);
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
    }
}