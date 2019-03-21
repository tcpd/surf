package in.edu.ashoka.surf;

import com.google.common.collect.Multimap;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by hangal on 8/12/17.
 * Looks for streaks of a given ID in streakFieldName (which must be of type integer).
 * If an ID has a streak up to streakLength with less than maxHoles holes, we look
 * e.g. in the elections dataset, streakFieldName is the assembly #, maxHoles = 1, streakLength = 5.
 * if a PID is present in say assembly #s 3, 4, 6, 7, this algorithm would build a cluster of
 * all rows for this PID
 * all rows in the hole (assembly #5) where the secondaryFieldName (Const# or Const. name) is the same as any value for that pid in the non-hole assemblies.
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
        Collection<Row> filteredRows = filter.isEmpty() ? dataset.getRows() : dataset.getRows().stream().filter(filter::passes).collect(toList());
        // compute maps we'll need later
        Multimap<String, Row> idToRows = SurfExcel.split(filteredRows, Config.ID_FIELD);

        Set<String> uniqueIDs = new LinkedHashSet<>(idToRows.keys());
        // go over all ids one by one. use a set on idToRows.keys() because otherwise it returns same key multiple times
        for (String id : uniqueIDs) {
            // create a cluster; streak rows first, and then the hole candidate rows
            Collection<Row> thisCluster = new ArrayList<>(idToRows.get(id));
            classes.add(thisCluster);
        }
        return classes;
    }

    public String toString() { return "One cluster per id algorithm "; }
}