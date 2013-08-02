
package avrwake.sched.update;

import avrwake.gui.Main;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class UpdateTask {
    
    private Main view;
    
    public UpdateTask(Main view) {
        this.view = view;
    }
    
    public void refreshAVR() {
       view.loadAVRValues();
       view.setControlStatusText("Updated...");
    }
}
