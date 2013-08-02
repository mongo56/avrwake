
package avrwake.sched.alarm;

import avrwake.gui.Main;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.*;
import javax.media.format.AudioFormat;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class AlarmTask {
    private final int AVR_BOOTUPTIME = 6000;
    
    private Main gui;
    private Player player;
    private String mp3Path;
    
    public AlarmTask(Main gui, String mp3Path) {
        this.gui = gui;
        this.mp3Path = mp3Path;
    }
    
    public void alarm() {
        gui.setAlarmStatusText("Alarm activated...");
        // turn the avr on and set source and volume
        if (gui.avrControl != null) {
            if (!gui.avrControl.isPowered()) {
                gui.setAlarmStatusText("Powering on...");
                gui.avrControl.powerOn();
                try {
                    Thread.sleep(AVR_BOOTUPTIME);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AlarmTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            gui.avrControl.setVolume(gui.prefsData.getAlarmVolume());
            gui.avrControl.setSource("SIBD");
            gui.avrControl.setMode("MSMCH STEREO");
            gui.loadAVRValues();
        }
        playSong();
    }
    
    private void playSong() {
        try {
            Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
            Format input2 = new AudioFormat(AudioFormat.MPEG);
            Format output = new AudioFormat(AudioFormat.LINEAR);
            PlugInManager.addPlugIn(
                    "com.sun.media.codec.audio.mp3.JavaDecoder",
                    new Format[]{input1, input2},
                    new Format[]{output},
                    PlugInManager.CODEC);
            player = Manager.createPlayer(new MediaLocator(new File(mp3Path).toURI().toURL()));
            player.start();
            gui.setAlarmStatusText("Playing...");
            gui.loadAVRValues();
        } catch (IOException ex) {
            Logger.getLogger(AlarmTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoPlayerException ex) {
            Logger.getLogger(AlarmTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopPlaying() {
        if (player != null) {
            player.stop();
            gui.setAlarmStatusText("Stopped..");
        }
    }
}
