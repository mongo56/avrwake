package avrwake.structures;

import java.io.Serializable;
import java.util.prefs.Preferences;

/**
 *
 * @author Blaz Majcen Telaris d.o.o. <bm@telaris.si>
 */
public class PreferencesStructure implements Serializable {
    private Preferences prefs;
    // settings
    private String avrHostname;
    // avr control
    private int avrMasterVolume;
    // alarms
    private boolean alarmEnabled, wAlarmEnabled, sleepEnabled;
    private int alarmHour, alarmMinute;
    private int wAlarmHour, wAlarmMinute;
    private int sleepHour, sleepMinute;
    private String musicPath, wMusicPath;
    private String musicName, wMusicName;
    private int alarmVolume;
    
    public PreferencesStructure(Preferences prefs) {
        this.prefs = prefs;
    }

    public void loadSettings() {
        // settings
        avrHostname = prefs.get("AVR_HOSTNAME", "10.0.0.12");
        // avr control
        avrMasterVolume = prefs.getInt("AVR_MASTER_VOLUME", -80);
        // alarms
        alarmEnabled = prefs.getBoolean("AVR_ALARM_ENABLED", false);
        alarmHour = prefs.getInt("AVR_ALARM_HOUR", 7);
        alarmMinute = prefs.getInt("AVR_ALARM_MINUTE", 0);
        musicName = prefs.get("AVR_MUSIC_NAME", "");
        musicPath = prefs.get("AVR_MUSIC_PATH", System.getProperty("user.home"));
        
        wAlarmEnabled = prefs.getBoolean("AVR_WALARM_ENABLED", false);
        wAlarmHour = prefs.getInt("AVR_WALARM_HOUR", 7);
        wAlarmMinute = prefs.getInt("AVR_WALARM_MINUTE", 0);
        wMusicPath = prefs.get("AVR_WMUSIC_PATH", System.getProperty("user.home"));
        wMusicName = prefs.get("AVR_WMUSIC_NAME", "");
        alarmVolume = prefs.getInt("AVR_ALARM_VOLUME", -80);
        
        sleepEnabled = prefs.getBoolean("AVR_SLEEP_ENABLED", false);
        sleepHour = prefs.getInt("AVR_SLEEP_HOUR", 0);
        sleepMinute = prefs.getInt("AVR_SLEEP_MINUTE", 0);
    }

    public void saveSettings(Preferences prefs) {
        // setings
        prefs.put("AVR_HOSTNAME", avrHostname);
        // avr control
        prefs.putInt("AVR_MASTER_VOLUME", avrMasterVolume);
        // alarms
        prefs.putBoolean("AVR_ALARM_ENABLED", alarmEnabled);
        prefs.putInt("AVR_ALARM_HOUR", alarmHour);
        prefs.putInt("AVR_ALARM_MINUTE", alarmMinute);
        prefs.put("AVR_MUSIC_PATH", musicPath);
        prefs.put("AVR_MUSIC_NAME", musicName);
        
        prefs.putBoolean("AVR_WALARM_ENABLED", wAlarmEnabled);
        prefs.putInt("AVR_WALARM_HOUR", wAlarmHour);
        prefs.putInt("AVR_WALARM_MINUTE", wAlarmMinute);
        prefs.put("AVR_WMUSIC_PATH", wMusicPath);
        prefs.put("AVR_WMUSIC_NAME", wMusicName);
        prefs.putInt("AVR_ALARM_VOLUME", alarmVolume);
        
        prefs.putBoolean("AVR_SLEEP_ENABLED", sleepEnabled);
        prefs.putInt("AVR_SLEEP_HOUR", sleepHour);
        prefs.putInt("AVR_SLEEP_MINUTE", sleepMinute);
    }

    public String getAvrHostname() {
        return avrHostname;
    }
    
    public void setAvrHostname(String avrHostname) {
        this.avrHostname = avrHostname;
    }

    public int getAvrMasterVolume() {
        return avrMasterVolume;
    }

    public void setAvrMasterVolume(int avrMasterVolume) {
        this.avrMasterVolume = avrMasterVolume;
    }

    public int getAlarmHour() {
        return alarmHour;
    }

    public void setAlarmHour(int alarmHour) {
        this.alarmHour = alarmHour;
    }

    public int getAlarmMinute() {
        return alarmMinute;
    }

    public void setAlarmMinute(int alarmMinute) {
        this.alarmMinute = alarmMinute;
    }

    public boolean isAlarmEnabled() {
        return alarmEnabled;
    }

    public void setAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public boolean iswAlarmEnabled() {
        return wAlarmEnabled;
    }

    public void setwAlarmEnabled(boolean wAlarmEnabled) {
        this.wAlarmEnabled = wAlarmEnabled;
    }

    public int getwAlarmHour() {
        return wAlarmHour;
    }

    public void setwAlarmHour(int wAlarmHour) {
        this.wAlarmHour = wAlarmHour;
    }

    public int getwAlarmMinute() {
        return wAlarmMinute;
    }

    public void setwAlarmMinute(int wAlarmMinute) {
        this.wAlarmMinute = wAlarmMinute;
    }

    public String getwMusicPath() {
        return wMusicPath;
    }

    public void setwMusicPath(String wMusicPath) {
        this.wMusicPath = wMusicPath;
    }

    public int getAlarmVolume() {
        return alarmVolume;
    }

    public void setAlarmVolume(int alarmVolume) {
        this.alarmVolume = alarmVolume;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getwMusicName() {
        return wMusicName;
    }

    public void setwMusicName(String wMusicName) {
        this.wMusicName = wMusicName;
    }

    public boolean isSleepEnabled() {
        return sleepEnabled;
    }

    public void setSleepEnabled(boolean sleepEnabled) {
        this.sleepEnabled = sleepEnabled;
    }

    public int getSleepHour() {
        return sleepHour;
    }

    public void setSleepHour(int sleepHour) {
        this.sleepHour = sleepHour;
    }

    public int getSleepMinute() {
        return sleepMinute;
    }

    public void setSleepMinute(int sleepMinute) {
        this.sleepMinute = sleepMinute;
    }
}