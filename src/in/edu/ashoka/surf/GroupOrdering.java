package in.edu.ashoka.surf;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that encapsulates comparison functions on groups.
 * Returns a comparator according to a control string that operates on list of list of rows.
 * this comparator can be used for sorting a list of list of list of rows.
 */
class GroupOrdering {
    // this comparator takes avg. length of all the rows in a group, and sorts group by decreasing avg. length
    private static final Comparator<List<List<Row>>> avgLengthComparator = (group1, group2) -> {
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

        avgLengthOfO1 = (group1.size() > 0) ? ((float) sumLength1)/group1.stream().mapToLong(List::size).sum() : 0.0f;
        avgLengthOfO2 = (group2.size() > 0) ? ((float) sumLength2)/group2.stream().mapToLong(List::size).sum() : 0.0f;

        // if same length (because many groups, esp. the smaller ones will have exactly the same total length and the same number of entries),
        // order by alpha on the merge field, just so we have some stability
        if (Math.abs (avgLengthOfO1 - avgLengthOfO2) < 0.001 ) {
            // sort all the rows in each group, take the first row and compare them
            List<String> list1 = group1.stream().flatMap(List::stream).map (r -> r.get(Config.MERGE_FIELD)).collect (Collectors.toList());
            List<String> list2 = group2.stream().flatMap(List::stream).map (r -> r.get(Config.MERGE_FIELD)).collect (Collectors.toList());
            Collections.sort (list1);
            Collections.sort (list2);
            if (list1.size() > 0 && list2.size() > 0)
                return list1.get(0).compareTo (list2.get(0));
            else
                return 0;
        } else
            return Float.compare(avgLengthOfO2, avgLengthOfO1); // return -1 if avg length of group 1 is more than avg length of group 2
    };

    private static final Comparator<List<List<Row>>> largestGroupFirstComparator = (group1, group2) -> {
        return Integer.compare(group2.size(), group1.size()); // return -1 if o2.size < group1.size, ie. bigger groups will come first
    };

    // given a list of list of rows L-L-R, returns the max # of elements in any element of L-L-R
    private static int maxRowsInOneId (List<List<Row>> listOfListOfRows) {
        if (listOfListOfRows.size() == 0)
            return 0;

        return listOfListOfRows.stream().map (List::size).max(Comparator.naturalOrder()).get();
    }

    private static final Comparator<List<List<Row>>> maxRowsInOneIdComparator = (group1, group2) -> {
        int maxRowsInOneId1 = maxRowsInOneId(group1);
        int maxRowsInOneId2 = maxRowsInOneId(group2);

        return Integer.compare(maxRowsInOneId2, maxRowsInOneId1); // return -1 if o2.size < group1.size, ie. bigger groups will come first
    };

    private static Comparator<List<List<Row>>> complexAlphaComparator = (group1, group2) -> {
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

        return Float.compare(avgFirstChar1, avgFirstChar2); // return -1 if avg length of group 1 is more than avg length of group 2
    };

    // this comparator takes first row in a group, and alphabetically compares it to the first row of the second group
    private static final Comparator<List<List<Row>>> approxAlphaComparator = (group1, group2) -> {

        // this is somewhat inefficient, we are computing sumLength repeatedly.
        // could be made faster by computing it just once for each group.

        // flatmap: flatten the list of list of rows to a list of rows first
        // map each row to it's MERGE_FIELD value
        // then map that field to it's length (0 if null)
        // convert that Integer to an int
        // them sum up the ints
        // note limit(1) here, it only considers first row
        Row firstRowInGroup1 = group1.stream().flatMap(List::stream).iterator().next();
        Row firstRowInGroup2 = group2.stream().flatMap(List::stream).iterator().next();

        return firstRowInGroup1.get(Config.MERGE_FIELD).compareTo (firstRowInGroup2.get(Config.MERGE_FIELD));
    };

    /** this method returns a comparator according to the given controlString, specifying a type of comparator. It will never return null */
    static Comparator<List<List<Row>>> getComparator(String controlString) {
        if ("approxAlpha".equals(controlString))
            return approxAlphaComparator;
        else if ("groupSize".equals(controlString))
            return largestGroupFirstComparator;
        else if ("maxRowsInOneId".equals(controlString))
            return maxRowsInOneIdComparator;
        else
            return avgLengthComparator;
    }
}
