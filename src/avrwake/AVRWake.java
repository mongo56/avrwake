package avrwake;

import avrwake.gui.Main;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class AVRWake {

    public static Main mainGui;
    
    
    public static void main(String[] args) {
        // handle args later on
        setNativeLook();
        
        // launch main gui
        mainGui = new Main();
        mainGui.setVisible(true);
    }
    
    public static void setNativeLook() {
        // set native look and feel
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Logger.getLogger(AVRWake.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public static void setPreferences() {
        
    }
}
