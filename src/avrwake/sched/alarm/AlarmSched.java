package avrwake.sched.alarm;

import java.text.ParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class AlarmSched {
    private AlarmTask task;
    
    private Scheduler scheduler;
    private JobDetailImpl job;

    public AlarmSched(AlarmTask task) {
        this.task = task;
    }

    public void startAlarm(int hour, int minute, boolean weekend) {
        try {
            job = new JobDetailImpl();
            if (weekend) {
                job.setName("wAlarmTimer");
            } else {
                job.setName("alarmTimer");
            }
            job.setJobClass(AlarmJob.class);

            Map dataMap = job.getJobDataMap();
            dataMap.put("alarmTask", task);

            CronTriggerImpl trigger = new CronTriggerImpl();
            if (weekend) {
                trigger.setName("wAlarmTrigger");
            } else {
                trigger.setName("alarmTrigger");
            }
            
            String cronExpression = String.format("0 %d %d ? * * *", minute, hour);
            if (weekend) {
                cronExpression = String.format("0 %d %d ? * SAT,SUN *", minute, hour);
            }
            trigger.setCronExpression(cronExpression);

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (ParseException ex) {
            Logger.getLogger(AlarmSched.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SchedulerException ex) {
            Logger.getLogger(AlarmSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stopAlarm(boolean weekend) {
        try {
            if (weekend) {
                scheduler.unscheduleJob(TriggerKey.triggerKey("wAlarmTrigger"));
            } else {
                scheduler.unscheduleJob(TriggerKey.triggerKey("alarmTrigger"));
            }
        } catch (SchedulerException ex) {
            Logger.getLogger(AlarmSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isRunning() {
        try {
            return scheduler.isStarted();
        } catch (SchedulerException ex) {
            Logger.getLogger(AlarmSched.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void shutdown() {
        try {
            if (isRunning()) {
                scheduler.shutdown();
            }
        } catch (SchedulerException ex) {
            Logger.getLogger(AlarmSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
