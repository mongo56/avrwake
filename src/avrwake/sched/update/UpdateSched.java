package avrwake.sched.update;

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
public class UpdateSched {
    public static long DEFAULT_INTERVAL = 120000;
    
    private UpdateTask task;
    private long updateInterval;
    
    private Scheduler scheduler;

    public UpdateSched(UpdateTask task, long updateInterval) {
        this.task = task;
        this.updateInterval = updateInterval;
    }

    public void startUpdatingAVRValues() {
        try {
            JobDetailImpl job = new JobDetailImpl();
            job.setName("updateTimer");
            job.setJobClass(UpdateJob.class);

            Map dataMap = job.getJobDataMap();
            dataMap.put("updateTask", task);

            SimpleTriggerImpl trigger = new SimpleTriggerImpl();
            trigger.setName("updateTrigger");
            trigger.setStartTime(new Date(System.currentTimeMillis() + DEFAULT_INTERVAL));
            trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            trigger.setRepeatInterval(updateInterval);

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            Logger.getLogger(UpdateSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pause() {
        try {
            scheduler.pauseJob(JobKey.jobKey("updateTimer"));
        } catch (SchedulerException ex) {
            Logger.getLogger(UpdateSched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
