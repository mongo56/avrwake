
package avrwake.sched.update;

import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class UpdateJob implements Job {
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Map dataMap = jec.getJobDetail().getJobDataMap();
        UpdateTask task = (UpdateTask) dataMap.get("updateTask");
        task.refreshAVR();
    }
}
