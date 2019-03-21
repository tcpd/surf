package in.edu.ashoka.surf;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import edu.tsinghua.dbgroup.EditDistanceClusterer;
import in.edu.ashoka.surf.util.Timers;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by hangal on 8/12/17.
 * New simplified edit distance merge manager.
 */
public class EditDistanceMergeAlgorithm extends MergeAlgorithm {

    private final int maxEditDistance;
    private final String fieldName; // fieldname on which to compute edit distance
    private final Filter filter;

    /* set up merge algorithm parameters: d, the fieldName (col. name) of the field on which edit distance clustering is to be done, max. editDistance (inclusive) */
    EditDistanceMergeAlgorithm(Dataset dataset, String fieldName, int editDistance, Filter filter) {
        super (dataset);
        this.filter = filter;

        // set up desi versions of the given field. we'll perform edit distance computation on this version of the given field, not the original one.
        this.fieldName = fieldName;
        this.maxEditDistance = editDistance;
    }

    @Override
    public List<Collection<Row>> run() {

        Collection<Row> filteredRows = filter.isEmpty() ? dataset.getRows() : dataset.getRows().stream().filter(filter::passes).collect(toList());

        // create map of fieldValueToRows
        SetMultimap<String, Row> fieldValueToRows = HashMultimap.create();
        filteredRows.forEach (r -> fieldValueToRows.put (r.get(fieldName), r));

        // do the clustering based on ed (but only if ed > 0)
        Timers.editDistanceTimer.reset();
        Timers.editDistanceTimer.start();

        List<Set<String>> clusters;

        if (maxEditDistance >= 1) {
            final EditDistanceClusterer edc = new EditDistanceClusterer(maxEditDistance);
            filteredRows.forEach(r -> edc.populate(r.get(fieldName)));
            clusters = (List) edc.getClusters();
        } else {
            // handle the case when edit distance is 0 by creating a list of single-element sets with all unique fieldVal's
            clusters = new ArrayList<>();
            for (String fieldVal: fieldValueToRows.keySet()) {
                // create a set with a single val
                Set set = new LinkedHashSet<String>();
                set.add(fieldVal);
                clusters.add(set);
            }

            // fieldValueToRows.keys().stream().map(val -> { Set set = new LinkedHashSet<String>(); set.add(val); return set;}).collect(Collectors.toList());
        }

        Timers.editDistanceTimer.stop();

        Timers.log.info ("Time for edit distance computation: " + Timers.editDistanceTimer.toString());

        // compute the result of this algorithm
        classes = new ArrayList<>();
        for (Set<String> cluster : clusters) {
            final Collection<Row> rowsForThisCluster = new ArrayList<>();
            // cluster just has strings, convert each string in the cluster to its rows, and add it to rowsForThisCluster
            cluster.forEach (s -> { rowsForThisCluster.addAll (fieldValueToRows.get(s)); });
            classes.add (rowsForThisCluster);
        }
        return classes;
    }

    public String toString() { return "Edit distance algorithm with maximum edit distance " + maxEditDistance; }
}