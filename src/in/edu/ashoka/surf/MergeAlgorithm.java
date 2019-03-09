package in.edu.ashoka.surf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by hangal on 8/13/17.
 */
abstract public class MergeAlgorithm {
    static final Log log = LogFactory.getLog(in.edu.ashoka.surf.MergeManager.class);

    public List<Collection<Row>> classes; // these are the groups
    final Dataset dataset;

    /** after run(), returns the groups of merged rows. the field classes should also be set up with the same */
    abstract public List<Collection<Row>> run() throws FileNotFoundException;

    MergeAlgorithm (Dataset dataset) {
        this.dataset = dataset;
        classes = new ArrayList<>();
    }
}
