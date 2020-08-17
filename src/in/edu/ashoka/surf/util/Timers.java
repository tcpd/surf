package in.edu.ashoka.surf.util;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by hangal on 8/13/17.
 */
public class Timers {
    public static final Log log = LogFactory.getLog(in.edu.ashoka.surf.Tokenizer.class);

    public static final StopWatch canonTimer = new StopWatch();
    public static final StopWatch tokenizationTimer = new StopWatch();
    public static final StopWatch editDistanceTimer = new StopWatch();
    public static final StopWatch unionFindTimer = new StopWatch();
    public static final StopWatch cosineTimer = new StopWatch();
    public static final StopWatch CompatibleNameTimer = new StopWatch();
    public static final StopWatch ReviewTimer = new StopWatch();

    public static void print() {
        log.info ("Canonicalization: " + canonTimer);
        log.info ("Tokenization: " + tokenizationTimer);
        log.info ("Edit distance computation: " + editDistanceTimer);
        log.info ("Union Find: " + unionFindTimer);
    }
    
    
}
