package avrwake.sched.sleep;

import avrwake.sched.alarm.AlarmJob;
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
public class SleepSched {

    private SleepTask task;
    private Scheduler scheduler;
    private JobDetailImpl job;

    public SleepSched(SleepTask task) {
        this.task = task;
    }

    public void startAlarm(int hour, int minute) {
        try {
            job = new JobDetailImpl();
            job.setName("sleepTimer");
            job.setJobClass(SleepJob.class);
            Map dataMap = job.getJobDataMap();
            dataMap.put("sleepTask", task);

            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setName("sleepTrigger");
            String cronExpression = String.format("0 %d %d ? * * *", minute, hour);
            trigger.setCronExpression(cronExpression);

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (ParseException ex) {
            Logger.getLogger(SleepSched.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SchedulerException ex) {
            Logger.getLogger(SleepSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stopAlarm() {
        try {
            scheduler.unscheduleJob(TriggerKey.triggerKey("sleepTrigger"));
        } catch (SchedulerException ex) {
            Logger.getLogger(SleepSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isRunning() {
        try {
            return scheduler.isStarted();
        } catch (SchedulerException ex) {
            Logger.getLogger(SleepSched.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void shutdown() {
        try {
            if (isRunning()) {
                scheduler.shutdown();
            }
        } catch (SchedulerException ex) {
            Logger.getLogger(SleepSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
