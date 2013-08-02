
package avrwake.shared;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Blaz Majcen <bm@telaris.si>
 */
public class Common {
    public static final String SIMPLE_TIME_FORMAT = "HH:mm:ss";
    public static final String SIMPLE_DATE_FORMAT = "dd.MM.yyyy";
    
    public static String getSimpleTime(Date timeDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_TIME_FORMAT);
        return sdf.format(timeDate);
    }
    
    public static String getSimpleDate(Date timeDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
        return sdf.format(timeDate);
    }
}
