package in.edu.ashoka.surf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.List;

/**
 * Created by hangal on 8/13/17.
 */
abstract public class MergeAlgorithm {
    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.MergeManager.class);

    public List<Collection<Row>> classes; // these are the groups
    Dataset dataset;

    /** after run(), the field classes should be set up with the groups of merges */
    abstract public void run();

    MergeAlgorithm (Dataset dataset) {
        this.dataset = dataset;
    }
}
