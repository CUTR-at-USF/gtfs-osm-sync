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
import java.awt.TextArea;
import javax.swing.SwingWorker;
/**
 *
 * @author Khoa Tran
 */
public class UploadData extends SwingWorker<Void, Void>{
    private HttpRequest osmRequest;
    private HashSet<Stop> upload, modify, delete;
    private Hashtable finalRoutes;
    private int progress;
    private String message;

    public UploadData(HttpRequest or, HashSet<Stop> u, HashSet<Stop> m, HashSet<Stop> d, Hashtable fRoutes){
        upload = new HashSet<Stop>();
        upload.addAll(u);

        modify = new HashSet<Stop>();
        modify.addAll(m);

        delete = new HashSet<Stop>();
        delete.addAll(d);

        finalRoutes = new Hashtable();
        finalRoutes.putAll(fRoutes);

        osmRequest = or;
    }

    public String getMessage(){
        return message;
    }

    @Override
    public Void doInBackground() {
        setProgress(0);
        try{
            updateProgress("Checking version ... ",1);
            osmRequest.checkVersion();

            updateProgress("Creating changeset ... ",8);
            osmRequest.createChangeSet();

            updateProgress("Creating and uploading chunks ... \nThis might take several minutes ...", 70);
            osmRequest.createChunks(upload, modify, delete, finalRoutes);

            updateProgress("Closing changeset...",10);
            osmRequest.closeChangeSet();

            //make sure it's a complete task
            updateProgress("",100);
        } catch (InterruptedException e){}
        return null;
    }

    private void updateProgress(String message, int progressValue){
         try {
             Thread.sleep(500);
         } catch (InterruptedException ignore) {}
         progress+=progressValue;
         setProgress(Math.min(progress, 100));
         System.out.println("\n\n"+message);
         this.message = "\n\n"+message;
         if(progress>=100) done();
    }
    
    @Override
    public void done() {
        Toolkit.getDefaultToolkit().beep();
        System.out.println("Done...!!");
        message = "Done...!!";
    }
}