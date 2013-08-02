package avrwake.sched.time;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.SimpleTriggerImpl;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class TimeSched {
    public static long DEFAULT_INTERVAL = 1000;
    
    private TimeTask task;
    private long updateInterval;
    
    private Scheduler scheduler;

    public TimeSched(TimeTask task, long updateInterval) {
        this.task = task;
        this.updateInterval = updateInterval;
    }

    public void startTimeDateUpdate() {
        try {
            JobDetailImpl job = new JobDetailImpl();
            job.setName("eggTimer");
            job.setJobClass(TimeJob.class);

            Map dataMap = job.getJobDataMap();
            dataMap.put("timerTask", task);

            SimpleTriggerImpl trigger = new SimpleTriggerImpl();
            trigger.setName("secondTrigger");
            trigger.setStartTime(new Date(System.currentTimeMillis()));
            trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            trigger.setRepeatInterval(updateInterval);

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            Logger.getLogger(TimeSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pause() {
        try {
            scheduler.pauseJob(JobKey.jobKey("eggTimer"));
        } catch (SchedulerException ex) {
            Logger.getLogger(TimeSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
