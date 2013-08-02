
package avrwake.sched.time;

import avrwake.shared.Common;
import avrwake.structures.PreferencesStructure;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class TimeTask {
    private JLabel timeLabel, dateLabel;
    private JLabel alarmLeftover, wAlarmLeftover;
    private PreferencesStructure prefs;
    
    public TimeTask(PreferencesStructure prefs, JLabel timeLabel, JLabel dateLabel, JLabel alarmLeftover, JLabel wAlarmLeftover) {
        this.prefs = prefs;
        this.timeLabel = timeLabel;
        this.dateLabel = dateLabel;
        this.alarmLeftover = alarmLeftover;
        this.wAlarmLeftover = wAlarmLeftover;
    }
    
    public void updateTime() {
        Date now = Calendar.getInstance().getTime();
        timeLabel.setText(Common.getSimpleTime(now));
        String curDate = Common.getSimpleDate(now);
        dateLabel.setText(curDate);

        DateFormat formatter ; 
        Date alarm;
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            alarm = (Date) formatter.parse(curDate + " " + prefs.getAlarmHour() + ":" + prefs.getAlarmMinute());
            long diff = alarm.getTime() - now.getTime();
            if (diff > 0) {
                int seconds = (int) (diff / 1000) % 60;
                int minutes = (int) ((diff / (1000 * 60)) % 60);
                int hours = (int) ((diff / (1000 * 60 * 60)) % 24);
                alarmLeftover.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            } else {
                alarmLeftover.setText(null);
            }
        } catch (ParseException ex) {
            Logger.getLogger(TimeTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
