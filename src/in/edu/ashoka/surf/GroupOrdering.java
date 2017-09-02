package in.edu.ashoka.surf;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class that encapsulates comparison functions on groups.
 * Returns a comparator according to a control string that operates on list of list of rows.
 * this comparator can be used for sorting a list of list of list of rows.
 */
public class GroupOrdering {
    // this comparator takes avg. length of all the rows in a group, and sorts group by decreasing avg. length
    private static Comparator<List<List<Row>>> avgLengthComparator = new Comparator<List<List<Row>>>() {
        @Override
        public int compare(List<List<Row>> group1, List<List<Row>> group2) {
            float avgLengthOfO1, avgLengthOfO2;

            // this is somewhat inefficient, we are computing sumLength repeatedly.
            // could be made faster by computing it just once for each group.

            // flatmap: flatten the list of list of rows to a list of rows first
            // map each row to it's MERGE_FIELD value
            // then map that field to it's length (0 if null)
            // convert that Integer to an int
            // them sum up the ints
            int sumLength1 = group1.stream().flatMap(List::stream).map (row -> row.get(Config.MERGE_FIELD)).map (fieldValue -> ((fieldValue != null) ? fieldValue.length() : 0)).mapToInt(Integer::intValue).sum();
            int sumLength2 = group2.stream().flatMap(List::stream).map (row -> row.get(Config.MERGE_FIELD)).map (fieldValue -> ((fieldValue != null) ? fieldValue.length() : 0)).mapToInt(Integer::intValue).sum();

            avgLengthOfO1 = (group1.size() > 0) ? ((float) sumLength1)/group1.size() : 0.0f;
            avgLengthOfO2 = (group2.size() > 0) ? ((float) sumLength2)/group2.size() : 0.0f;

            return ((Float) avgLengthOfO2).compareTo(avgLengthOfO1); // return -1 if avg length of group 1 is more than avg length of group 2
        }
    };

    private static Comparator<List<List<Row>>> largestGroupFirstComparator = new Comparator<List<List<Row>>>() {
        @Override
        public int compare(List<List<Row>> group1, List<List<Row>> group2) {
            return ((Integer) group2.size()).compareTo (group1.size()); // return -1 if o2.size < group1.size, ie. bigger groups will come first
        }
    };

    // this comparator takes avg. length of all the rows in a group, and sorts group by decreasing avg. length
    private static Comparator<List<List<Row>>> approxAlphaComparator = new Comparator<List<List<Row>>>() {
        @Override
        public int compare(List<List<Row>> group1, List<List<Row>> group2) {
            float avgFirstChar1, avgFirstChar2;

            // this is somewhat inefficient, we are computing sumLength repeatedly.
            // could be made faster by computing it just once for each group.

            // flatmap: flatten the list of list of rows to a list of rows first
            // map each row to it's MERGE_FIELD value
            // then map that field to it's length (0 if null)
            // convert that Integer to an int
            // them sum up the ints
            int sumFirstChar1 = group1.stream().flatMap(List::stream).map (row -> row.get(Config.MERGE_FIELD)).map (fieldValue -> ((fieldValue != null) ? ((int) fieldValue.charAt(0)) : 0)).mapToInt(Integer::intValue).sum();
            int sumFirstChar2 = group2.stream().flatMap(List::stream).map (row -> row.get(Config.MERGE_FIELD)).map (fieldValue -> ((fieldValue != null) ? ((int) fieldValue.charAt(0)) : 0)).mapToInt(Integer::intValue).sum();

            avgFirstChar1 = (group1.size() > 0) ? ((float) sumFirstChar1)/group1.size() : 0.0f;
            avgFirstChar2 = (group2.size() > 0) ? ((float) sumFirstChar2)/group2.size() : 0.0f;

            return ((Float) avgFirstChar1).compareTo(avgFirstChar2); // return -1 if avg length of group 1 is more than avg length of group 2
        }
    };

    /** this method returns a comparator according to the given controlString, specifying a type of comparator. It will never return null */
    public static Comparator<List<List<Row>>> getComparator (String controlString) {
        if ("approxAlpha".equals(controlString))
            return approxAlphaComparator;
        else if ("groupSize".equals(controlString))
            return largestGroupFirstComparator;
        else
            return avgLengthComparator;
    }
}
