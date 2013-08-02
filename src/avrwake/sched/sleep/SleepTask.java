
package avrwake.sched.sleep;

import avrwake.gui.Main;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class SleepTask {
    
    private Main gui;
    
    public SleepTask(Main gui) {
        this.gui = gui;
    }
    
    public void alarm() {
        // put the avr to sleep
        if (gui.avrControl != null) {
            if (gui.avrControl.isPowered()) {
                gui.setAlarmStatusText("Sleeping...");
                gui.avrControl.standby();
                gui.loadAVRValues();
            }
        }
    }
}
