
package avrwake.sched.time;

import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class TimeJob implements Job {
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Map dataMap = jec.getJobDetail().getJobDataMap();
        TimeTask task = (TimeTask) dataMap.get("timerTask");
        task.updateTime();
    }
}
