package in.edu.ashoka.surf;

import com.google.common.collect.Multimap;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by hangal on 8/12/17.
 * This is a simple merge "algorithm" that simply bunches all rows with the same ID into a cluster.
 * IDs with a single row are also included.
 * It is mainly useful for reviewing the merges in a dataset.
 */
public class OneClusterPerIDMergeAlgorithm extends MergeAlgorithm {

    private final Filter filter;

    /* set up merge algorithm parameters: d, the fieldName (col. name) of the field on which edit distance clustering is to be done, max. editDistance (inclusive) */
    OneClusterPerIDMergeAlgorithm(Dataset dataset, Filter filter) {
        super (dataset);
        this.filter = filter;
    }

    @Override
    public List<Collection<Row>> run() {
        Collection<Row> filteredRows = dataset.getRows().stream().filter(filter::passes).collect(toList());

        // compute maps we'll need later
        Multimap<String, Row> idToRows = SurfExcel.split (filteredRows, Config.ID_FIELD);

        Set<String> uniqueIDs = new LinkedHashSet<>(idToRows.keys());
        // go over all ids one by one. use a set on idToRows.keys() because otherwise it returns same key multiple times
        for (String id: uniqueIDs) {

            // create a cluster; streak rows first, and then the hole candidate rows
            Collection<Row> thisCluster = new ArrayList<>(idToRows.get(id));

            classes.add(thisCluster);
        }
        return classes;
    }

    public String toString() { return "One cluster per ID algorithm"; }
}