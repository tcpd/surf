package in.edu.ashoka.surf;

import com.google.common.collect.Multimap;
import in.edu.ashoka.surf.util.Pair;
import in.edu.ashoka.surf.util.Util;

import java.util.*;

import static java.util.stream.Collectors.toSet;
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
public class StreakMergeAlgorithm extends MergeAlgorithm {

    private final String streakFieldName; // fieldname on which to compute edit distance
    private final int streakLength;
    private final int maxHoles;
    private final String secondaryFieldName;
    private final Filter filter;

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

    // borrowed from https://www.programcreek.com/2013/12/edit-distance-in-java/, should probably be improved
    private static int minDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        // len1+1, len2+1, because finally return dp[len1][len2]
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);

                //if last two chars equal
                if (c1 == c2) {
                    //update dp value for +1 length
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }

        return dp[len1][len2];
    }

    /** given candidateRow, computes average (edit) distance to each of the first MAX rows of rowCollection */
    private Pair<Row, Float> averageRowDistance(Row candidateRow, Collection<Row> rowCollection, String fieldName) {
        int MAX = 5;
        int totalDistance = 0, count = 0;
        for (Row r: rowCollection) {
            totalDistance += minDistance(candidateRow.get(fieldName), r.get(fieldName));

            if (++count >= MAX)
                break;
        }

        float distance = ((float) totalDistance) / count;
        return new Pair<>(candidateRow, distance);
    }

    @Override
    public List<Collection<Row>> run() {
        Collection<Row> filteredRows = dataset.getRows().stream().filter(filter::passes).collect(toList());

        // compute maps we'll need later
        Multimap<String, Row> idToRows = SurfExcel.split (filteredRows, Config.ID_FIELD);
        Multimap<String, Row> streakFieldToRows = SurfExcel.split (filteredRows, streakFieldName);

        Set<String> uniqueIDs = new LinkedHashSet<>(idToRows.keys());
        // go over all ids one by one. use a set on idToRows.keys() because otherwise it returns same key multiple times
        outer:
        for (String id: uniqueIDs) {
            final Collection<Row> rowsForThisID = idToRows.get(id);

            // get all unique vals in the streak field, converted to int
            Set<Integer> uniqueValsInStreakField = rowsForThisID.stream().map(r -> Integer.parseInt(r.get(streakFieldName))).collect(toSet());

            // no need to look at "streaks" of length just 1
            if (uniqueValsInStreakField.size() == 1)
                continue;

            // get the min and max values in the streak field and bail out if the streak doesn't have adequate length
            int minValInStreak = Collections.min(uniqueValsInStreakField), maxValInStreak =  Collections.max(uniqueValsInStreakField);
            if ((maxValInStreak - minValInStreak + 1) < streakLength)
                continue; // this streak is smaller than the length we're looking for

            // compute the holes in the streak and whether they are < maxHoles
            // TOFIX: problem here. Assume Streak length is 5, and maxHoles is 1.
            // if uniqueValsInStreakField is 1, 4, 5, 7, 8, we will think there are 3 holes (2, 3, 6) and therefore bail out
            // but if you look at the run from 4..8, that matches streak of length 5 with only one hole
            Set<Integer> holes = new LinkedHashSet<>(); // this will have the holes in the streak field
            for (int val = minValInStreak; val <= maxValInStreak; val++) {
                if (!uniqueValsInStreakField.contains(val)) {
                    // this val is a hole in the streak
                    holes.add(val);

                    if (holes.size() > maxHoles)
                        continue outer; // this id has more than maxHoles, so no need to conside it
                }
            }
            if (holes.size() == 0)
                continue; // if there are no holes, nothing to look for

            Set<String> secondaryValsForThisId = rowsForThisID.stream().map(r -> r.get(secondaryFieldName)).collect(toSet());

            log.info("Looking for streak candidates for id: " + id + " with " + rowsForThisID.size() + " rows, "
                    + holes.size() + " hole(s): " + Util.join (holes, ",")
                    + ", " + secondaryValsForThisId.size() + " secondary value(s): " + Util.join(secondaryValsForThisId, ","));
            log.info (Util.join (rowsForThisID.stream().map(r -> r.get(Config.MERGE_FIELD)).collect(toSet()), ","));

            // mark the streak rows for this PID as special, so it will be shown as .special-row in the UI
            rowsForThisID.forEach(r -> r.set("__is_special", "true"));

            // select all rows with holeValues, and secondaryField in one of the secondaryValsForThisId
            // for every such row, also compute average distance from streakRows
            List<Pair<Row, Float>> holeCandidateAndDistance = new ArrayList<>();
            for (int holeValue: holes) {
                Collection<Row> rowsInHole = streakFieldToRows.get(Integer.toString(holeValue));
                Collection<Row> holeCandidates = rowsInHole.stream().filter(r -> secondaryValsForThisId.contains(r.get(secondaryFieldName))).collect(toSet());

                // for every hole candidate row, create a pair with that row and it's distance from streak rows
                // String mergeFieldName = Config.MERGE_FIELD;
                // can optionally make it "_st" + Config.MERGE_FIELD
                String mergeFieldName = "_st_" + Config.MERGE_FIELD;

                holeCandidateAndDistance.addAll (holeCandidates.stream().map(r -> averageRowDistance(r, rowsForThisID, mergeFieldName)).collect(toList()));

                log.info(holeCandidates.size()  + " rows are candidates for hole: " + holeValue + " in id: " + id);
            }

            // bail out if we have no candidates to fill the hole - can happen if the secondary field in the hole doesn't match up
            // (e.g. with mismatch of const. names)
            if (holeCandidateAndDistance.size() == 0)
                continue;

            // sort holecandidates by decreasing distance
            Util.sortPairsBySecondElement(holeCandidateAndDistance);
            // but we actually want increasing distance
            Collections.reverse(holeCandidateAndDistance);
            // now extract only the rows
            List<Row> holeCandidateRows = holeCandidateAndDistance.stream().map (Pair::getFirst).collect(toList());

            // create a cluster; streak rows first, and then the hole candidate rows
            Collection<Row> thisCluster = new ArrayList<>();
            thisCluster.addAll(rowsForThisID);
            thisCluster.addAll(holeCandidateRows);

            classes.add(thisCluster);
        }
        return classes;
    }

    public String toString() { return "Streak algorithm on " + streakFieldName + " of length " + streakLength + " with <= " + maxHoles + " holes"; }
}