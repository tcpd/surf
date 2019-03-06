package in.edu.ashoka.surf;

import com.google.common.collect.Multimap;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by hangal on 8/12/17.
 * Looks for streaks of a given ID in streakFieldName (which must be of type integer).
 * If an ID has a streak up to streakLength with less than maxHoles holes, we look
 * e.g. in the elections dataset, streakFieldName is the assembly #, maxHoles = 1, streakLength = 5.
 * if a PID is present in say assembly #s 3, 4, 6, 7, this algorithm would build a cluster of
 * all rows for this PID
 * all rows in the hole (assembly #5) where the secondaryFieldName (Const# or Const. name) is the same as any value for that pid in the non-hole assemblies.
 */
public class StreakMergeAlgorithm extends MergeAlgorithm {

    private String streakFieldName; // fieldname on which to compute edit distance
    private int streakLength;
    private int maxHoles;
    private String secondaryFieldName;
    private Filter filter;

    // small class to get around the problem of non-final vars not being available inside the lambda
    class InternalPredicate implements Predicate<Row> {
        int v;
        InternalPredicate(int val) { this.v = val;}
        @Override
        public boolean test(Row r) { return Integer.parseInt(r.get(streakFieldName)) == v; }
    }

    /* set up merge algorithm parameters: d, the fieldName (col. name) of the field on which edit distance clustering is to be done, max. editDistance (inclusive) */
    StreakMergeAlgorithm(Dataset dataset, Filter filter, String streakFieldName, int streakLength, int maxHoles, String secondaryFieldName) {
        super (dataset);
        this.streakFieldName = streakFieldName;
        this.filter = filter;
        this.streakLength = streakLength;
        this.maxHoles = maxHoles;
        this.secondaryFieldName = secondaryFieldName;

        try {
            for (Row r: dataset.getRows()) {
                Integer.parseInt(r.get(streakFieldName));
            }
        } catch (Exception e) {
            log.warn ("Not all values in field " + streakFieldName + " are integers, cannot run streak algorithm! " + e);
        }
    }

    @Override
    public List<Collection<Row>> run() {
        Collection<Row> filteredRows = dataset.getRows().stream().filter(filter::passes).collect(Collectors.toList());

        // compute maps we'll need later
        Multimap<String, Row> idToRows = SurfExcel.split (filteredRows, Config.ID_FIELD);
        Multimap<String, Row> streakFieldToRows = SurfExcel.split (filteredRows, streakFieldName);

        // go over all ids one by one. use a set on idToRows.keys() because otherwise it returns same key multiple times
        outer:
        for (String id: new LinkedHashSet<>(idToRows.keys())) {
            Collection<Row> rowsForThisID = idToRows.get(id);

            // get all unique vals in the streak field, converted to int
            Set<Integer> uniqueValsInStreakField = rowsForThisID.stream().map(r -> Integer.parseInt(r.get(streakFieldName))).collect(Collectors.toSet());

            // no need to look at streaks of length just 1
            if (uniqueValsInStreakField.size() == 1)
                continue;

            int minValInStreak = Collections.min(uniqueValsInStreakField), maxValInStreak =  Collections.max(uniqueValsInStreakField);

            if ((maxValInStreak - minValInStreak + 1) < streakLength)
                continue; // this streak is smaller than the length we're looking for

            // prepare a cluster, starting with all the rows for this ID
            Collection<Row> thisCluster = new ArrayList<>(rowsForThisID);

            // compute the holes in the streak, and secondary vals in them
            Set<Integer> holeValues = new LinkedHashSet<>();
            Set<String> secondaryValsInNonHoles = new LinkedHashSet<>();
            for (int val = minValInStreak; val <= maxValInStreak; val++) {
                if (!uniqueValsInStreakField.contains(val)) {
                    // this val is a hole in the streak
                    holeValues.add(val);
                    if (holeValues.size() > maxHoles)
                        continue outer; // this id has more than maxHoles, so no need to conside it
                } else {
                    // this streak val is present for this id. Collect all the secondary field names in these rows, so we can extract the holes with the secondary field values later
                    Predicate<Row> filterConditionForThisVal = new InternalPredicate(val);
                    Collection<Row> rowsWithThisIdAndVal = rowsForThisID.stream().filter (filterConditionForThisVal).collect(Collectors.toList());
                    secondaryValsInNonHoles.addAll(rowsWithThisIdAndVal.stream().map (r -> r.get(secondaryFieldName)).collect(Collectors.toSet()));
                }
            }

            if (holeValues.size() == 0)
                continue; // if there are no holes, nothing to look for

            log.info("Looking for streak matching candidates for id: " + id + " with " + thisCluster.size() + " rows, holes:" + holeValues.size());
            thisCluster.stream().forEach(r -> { log.info (r.get(Config.MERGE_FIELD)); r.set("__is_special", "true"); });

            // select all rows with holeValues, and secondaryField in one of the secondaryValsInNonHoles
            for (int holeValue: holeValues) {
                Collection<Row> rowsWithHoleVal = streakFieldToRows.get(Integer.toString(holeValue));
                Collection<Row> rowsWithHoleValAndMatchingSecondaryField = rowsWithHoleVal.stream().filter(r -> secondaryValsInNonHoles.contains(r.get(secondaryFieldName))).collect(Collectors.toSet());
                thisCluster.addAll(rowsWithHoleValAndMatchingSecondaryField);
                log.info("After adding streak candidates for hole: " + holeValue + ", cluster for id: " + id + " has " + thisCluster.size() + " rows");
            }

            classes.add(thisCluster);
        }
        return classes;
    }

    public String toString() { return "Streak algorithm on " + streakFieldName + " of length " + streakLength + " with <= " + maxHoles + " holes"; }
}