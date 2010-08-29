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

package task;

import javax.swing.SwingWorker;

/**
 *
 * @author Khoa Tran
 */
public class OsmTask extends SwingWorker<Void, Void>{
    private static final int SLEEP_TIME = 500;

    private String message;
    private int progress;
    
    public Void doInBackground() {
        return null;
    }

    public void setMessage(String m){
        message = m;
    }

    public void appendMessage(String m){
        message += "\n"+m;
    }

    public String getMessage(){
        return message;
    }

    public void updateProgress(int p){
        try {
            Thread.sleep(SLEEP_TIME);
            progress+=p;
            setProgress(Math.min(progress, 100));
        } catch (InterruptedException e) {
            this.setMessage(e.getMessage());
            setProgress(100);
        }
    }
}