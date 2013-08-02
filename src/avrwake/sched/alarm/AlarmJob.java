
package avrwake.sched.alarm;

import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class AlarmJob implements Job {
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Map dataMap = jec.getJobDetail().getJobDataMap();
        AlarmTask task = (AlarmTask) dataMap.get("alarmTask");
        task.alarm();
    }
}
