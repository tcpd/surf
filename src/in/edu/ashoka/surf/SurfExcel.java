
package in.edu.ashoka.surf;

import com.google.common.collect.*;
import in.edu.ashoka.surf.util.Pair;
import in.edu.ashoka.surf.util.Util;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;







public class SurfExcel {

    private static final PrintStream out = System.out;
    private static final String SEPARATOR = "========================================\n";
    static final String FIELDSPEC_SEPARATOR = "-";
    private static final String ID_PREFIX = "_id_";
    // When we want to assign id's to a column, we'll assign it in a special col. called id_<col>.
    // e.g. id for PC will be in col. name _id_PC and id for candName will be in col. name _id_candName.

    static final Comparator<String> stringLengthComparator = (o1, o2) -> o2.length() - o1.length();

    static Comparator<Collection<Row>> alphabeticalComparartor = (o1, o2) -> o1.iterator().next().get("Name").compareTo(o2.iterator().next().get("Name"));

    static Comparator<Collection<Row>> sizeComparator = (o1, o2) -> o2.iterator().next().get("Name").length() - o1.iterator().next().get("Name").length();

    static Comparator<Collection<Row>> confidenceComparator = (o1, o2) -> Integer.parseInt(o2.iterator().next().get("confidence")) - Integer.parseInt(o1.iterator().next().get("confidence"));

    public static void warn (String s) {
        out.println("WARNING " + s);
    }

    /** given a set of rows, returns pairs of strings that are within ed edit distance, after canonicalization, etc.
     * Note: there may be more efficient ways of doing edit distance clustering. see https://github.com/OpenRefine/EditDistanceClusterer
     **/
    public static List<Pair<String, String>> similarPairsForField(Collection<Row> rows, String field, int ed)
    {
        List<Pair<String, String>> result = new ArrayList<>();

        Multimap<String, Row> fieldToRows = split (rows, "_st_" + field);
        List<String> listStField = new ArrayList<>(fieldToRows.keySet());
        listStField.sort(stringLengthComparator);
        listStField = Collections.unmodifiableList(listStField);
        // ok, now list of stFields is frozen, we can use indexes into it which will be stable.

        // setup tokenToFieldIdx: is a map of token (of at least 3 chars) -> all indexes in stnames that contain that token
        // since editDistance computation is expensive, we'll only compute it for pairs that have at least 1 token in common
        Multimap<String, Integer> tokenToFieldIdx = HashMultimap.create();
        for (int i = 0; i < listStField.size(); i++) {
            String fieldVal = listStField.get(i);
            StringTokenizer st = new StringTokenizer(fieldVal, Tokenizer.DELIMITERS);
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (tok.length() < 3)
                    continue;
                tokenToFieldIdx.put(tok, i);
            }
        }

        // we need non-space versions for edit distance comparison.
        // order in nonSpaceStFields is exactly the same as in listStField
        List<String> nonSpaceStFields = new ArrayList<>();
        for (String fieldVal: listStField)
            nonSpaceStFields.add(fieldVal.replaceAll(" ", ""));
        nonSpaceStFields = Collections.unmodifiableList(nonSpaceStFields);

        int totalComparisons = 0;
        // in order of stnames, check each name against all other stnames after it that share a token and have edit distance < ed, ignoring spaces.
        // only check with stnames after it, because we want to report a pair of stnames A and B only once (A-B, not B-A)
        for (int i = 0; i < listStField.size(); i++) {
            String stField = listStField.get(i);

            // collect the indexes of the values that we should compare stField with, based on common tokens
            Set<Integer> idxsToCompareWith = new LinkedHashSet<>();
            {
                StringTokenizer st = new StringTokenizer(stField, Tokenizer.DELIMITERS);
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    if (tok.length() < 3)
                        continue;
                    Collection<Integer> c = tokenToFieldIdx.get(tok);
                    for (Integer j: c)
                        if (j > i)
                            idxsToCompareWith.add(j);
                }
            }

            // now check stField against all idxs
            totalComparisons += idxsToCompareWith.size();

            String nonSpaceStField = nonSpaceStFields.get(i);
            // check each one of stNameIdxsToCompareWith for edit distance = 1
            for (Integer j : idxsToCompareWith) {
                String stField1 = listStField.get(j);
                String nonSpaceStField1 = nonSpaceStFields.get(j);

                // now compare stname with stname1
                {
                    if (Math.abs(nonSpaceStField.length() - nonSpaceStField1.length()) > ed)
                        continue; // optimization: don't bother to compute edit distance if the lengths differ by more than 1

                    if (Util1.editDistance(nonSpaceStField, nonSpaceStField1) <= ed) { // make sure to use the space-removed versions to compute edit distance
                        // ok, we found something that looks close enough
                        // out.println("  canonical: " + stname);
                        result.add(new Pair<>(stField, stField1));
                    }
                }
            }
        }

        out.println ("Similar pairs found: " + result.size());
        out.println ("list size: " + listStField.size() + ", total comparisons: " + totalComparisons + " average: " + ((float) totalComparisons)/listStField.size());
        return result;
    }

    /** looks for keys in keys1 that are also in keys2, within maxEditDistance */
    public static List<Pair<String, String>> desiMatch2Lists(Collection<String> keys1, Collection<String> keys2, int maxEditDistance) {

        // cannot canonicalize by space here because what we return has to be the same string passed in, in keys1/2.
        // sort by length to make edit distance more efficient
        List<String> stream1 = keys1.stream().sorted(stringLengthComparator).collect(Collectors.toList());
        List<String> stream2 = keys2.stream().sorted(stringLengthComparator).collect(Collectors.toList());
        List<Pair<String, String>> result = new ArrayList<>();

        // stream1, stream2 are now sorted by descending length
        outer:
        for (String key1 : stream1)
            for (String key2 : stream2) {
                if (key1.length() - key2.length() > maxEditDistance) // to outer loop because no more key2 matches possible, the remaining key2's have even shorter length
                    continue outer;
                if (key2.length() - key1.length() > maxEditDistance) // no need to compare because of length difference, but the remaining key2's could still match
                    continue;
                if (Util1.editDistance(key1, key2) <= maxEditDistance)
                    result.add(new Pair<>(key1, key2));
            }

        return result;
    }

    /** Prints values of field that are different, but map to the same (canonicalized, retokenized, sorted) value.
     * Tokenizer.setupDesiVersions(allRows, field); should already been called on this field */
    static Multimap<String, Multimap<String, Row>> reportSimilarDesiValuesForField(Collection<Row> rows, String field) {

        // stField -> rows with that st field
        Multimap<String, Row> stFieldSplit = split(rows, "_st_" + field);
        // stField -> { field -> rows, field -> rows, ...}
        Multimap<String, Multimap<String, Row>> stFieldToFieldToRows = split(stFieldSplit, field);

        // filter to only those stField -> { at least 2x field -> rows}
        Multimap<String, Multimap<String, Row>> stFieldToMultipleFieldToRows = sort(filter(stFieldToFieldToRows, "min", 2), stringLengthComparator);
        return stFieldToMultipleFieldToRows;
    }

    static Collection<Row> select(Collection<Row> rows, String field, String value) {
        List<Row> result = new ArrayList<>();
        for (Row r: rows)
            if (value.equals(r.get(field)))
                result.add(r);
        return result;
    }

    static Collection<Row> selectNot(Collection<Row> rows, String field, String value) {
        List<Row> result = new ArrayList<>();
        for (Row r: rows)
            if (!value.equals(r.get(field)))
                result.add(r);
        return result;
    }

    static void checkSame(Collection<Row> allRows, String field1, String field2) {
        out.println(SEPARATOR + "Checking if fields identical: " + field1 + " and " + field2);
        for (Row r: allRows)
            if (!r.get(field1).equals(r.get(field2))) {
                out.println("Warning: " + field1 + " and " + field2 + " not the same! row=" + r + " " + field1 + "=" + r.get(field1) + " " + field2 + " = " + r.get(field2) + " row#: " + r.rowNum);
            }
    }

    /* this method will assign id's like U-1, U-2, etc. to all rows whose _id_ field column is not assigned.
    static void assignUnassignedIds(Collection<Row> allRows, String field) {
        String idField = ID_PREFIX + field;
        Map<String, String> map = new LinkedHashMap<>();
        int unassigned_id_val = 0;
        for (Row r: allRows) {
            String idVal = r.get(idField);
            if (Util.nullOrEmpty(idVal)) {
                String fieldVal = r.get(field);
                idVal = map.get(fieldVal);
                if (Util.nullOrEmpty(idVal)) {
                    idVal = " U-" + (++unassigned_id_val);
                    map.put(fieldVal, idVal);
                }
                r.set(idField, idVal);
            }
        }
        out.println ("Number of unassigned ids that were now assigned: " + unassigned_id_val);
    }

    /** given a list of pairs of ids, assigns the first one's id to all rows that have the second id */
    public void mergeIDs (List<Pair<String, String>> samePairs, Dataset d) {
    	Collection<Row> rows = d.rows;
        Multimap<String, Row> idToRows = SurfExcel.split (rows, "ID_FIELD");

        for (Pair<String, String> p: samePairs) { 
            String firstID = p.getFirst();
            String secondID = p.getSecond();
            for (Row r: idToRows.get(secondID)) {
                // assign r.ID to firstID.
            }
        }
    }

    /*
    static void profile(Collection<Row> allRows, String field) {
        String idField = ID_PREFIX + field;
        Multimap<String, String> map = LinkedHashMultimap.create();
        Set<String> seen = new LinkedHashSet<>(); // field vals that we have already seen

        for (Row r: allRows) {
            String fieldVal = r.get(field);
            String idVal = r.get(idField);
            if (!seen.contains(fieldVal)) {
                map.put(idVal, fieldVal);
                seen.add(fieldVal);
            }
        }

        List<String> list = new ArrayList<>();

        for (String s: map.keySet()) {
//            out.println (s + " -- " + Util.join(map.get(s), " | "));
            list.add(Util.join(map.get(s), " | "));
        }
        Collections.sort(list);
        for (String s: list)
            out.println (s);
    }
    */

    static void profile(Collection<Row> allRows, String field) {
        Multiset<String> set = LinkedHashMultiset.create();

        for (Row r: allRows) {
            String fieldVal = r.get(field);
            set.add(fieldVal);
        }

        List<String> list = new ArrayList<>();

        for (String s: Multisets.copyHighestCountFirst(set).elementSet())
        {
//            out.println (s + " -- " + Util.join(map.get(s), " | "));
            list.add((s.length() == 0 ? "<blank>" : s) + " (" + set.count(s) + ") | ");
        }
        for (String s: list)
            out.print(s);
        out.println();
    }

    /** converts a collection of rows to a map in which each unique value for the given fieldspec is the key and the value for that key is the rows with that value for the fieldspec */
    public static Multimap <String, Row> split(Collection<Row> rows, String fieldSpec) {
        Multimap<String, Row> result = HashMultimap.create();
        String[] fields = fieldSpec.split(FIELDSPEC_SEPARATOR);
        if (rows.size() > 0 && !(rows.iterator().next() instanceof Row)) {
            out.println ("Sorry, unable to perform this split");
        }

        for (Row r : rows) {
            String key = r.getFields(fields, FIELDSPEC_SEPARATOR);
            result.put(key, r);
        }
        return result;
    }

    /** 2nd level split, takes A -> rows multimap, and further breaks up each collection of rows for each value of A into a multimap based on the values of B
     * so field1val -> rows becomes field1val -> fieldval2 -> rows
     */
    static Multimap <String, Multimap <String, Row>> split(Multimap <String, Row> map, String fieldSpec) {
        Multimap<String, Multimap <String, Row>> result = HashMultimap.create();
        for (String a: map.keySet()) {
            Collection<Row> rows = map.get(a);
            Multimap<String, Row> arowsSplit = split(rows, fieldSpec);
            for (String b: arowsSplit.keySet()) {
                Multimap<String, Row> bmap = HashMultimap.create();
                // bmap will be a map with only 1 key. (but multiple rows associated with that key)
                bmap.putAll(b, arowsSplit.get(b));
                result.put(a, bmap); // there could be multiple b's for a single value of a
            }
        }
        return result;
    }

    static Collection<Row> filter (Collection<Row> rows, String fieldSpec, String valueSpec) {
        List<Row> result = new ArrayList<>();
        String[] fields = fieldSpec.split(FIELDSPEC_SEPARATOR);
        String[] values = valueSpec.split(FIELDSPEC_SEPARATOR);

        // sanity check
        if (fields.length != values.length)
            return result;

        for (Row r: rows)
            for (int i = 0; i < fields.length; i++)
                if (r.equal (fields[i], values[i]))
                    result.add(r);

        return result;
    }

    /** filters given map to retain only those keys whose rows.size() <op> count */
    static<T> Multimap<String, T> filter (Multimap<String, T> map, String op, int count) {
        Multimap<String, T> filteredMap = LinkedHashMultimap.create(); // required extra memory instead of clearing map in place... can do away with it depending on how map.keySet() iteration works
        for (String key : map.keySet()) {
            Collection<T> vals = map.get(key);
                if (checkCount(vals, op, count))
                    filteredMap.putAll(key, vals);
        }
        return filteredMap;
    }

    /** filters given map to retain only those keys whose rows.size() <op> count */
    static<T> Multimap<String, T> sort (Multimap<String, T> map, Comparator<String> comparator) {
        List<String> keyList = new ArrayList<>(map.keySet());
        keyList.sort(comparator);

        Multimap<String, T> sortedMap = LinkedHashMultimap.create(); // required extra memory instead of clearing map in place... can do away with it depending on how map.keySet() iteration works

        for (String key : keyList) {
            Collection<T> vals = map.get(key);
            sortedMap.putAll(key, vals);
        }
        return sortedMap;
    }

    /** removes keysToRemove from map. warning: modifies map */
    static<T> Multimap<String, T> minus(Multimap<String, T> map, Collection<String> keysToRemove) {
        Multimap<String, T> result = HashMultimap.create();
        Set<String> keysToRemoveSet = new LinkedHashSet<>(keysToRemove);

        for (String key: map.keySet())
            if (!keysToRemoveSet.contains(key))
                result.putAll (key, map.get(key));
        return result;
    }

    /** returns if rows.size() <op> count */
    private static boolean checkCount(Collection<?> coll, String op, int count) {
        if ("equals".equals(op)) {
            return coll.size() == count;
        } else if ("notequals".equals(op)) {
            return coll.size() != count;
        } else if ("min".equals(op)) {
            return coll.size() >= count;
        } else if ("max".equals(op)) {
            return coll.size() <= count;
        }
        return false;
    }


    /** returns pairs of strings in the given field that are close in edit distance (< given maxEditDistance), sorted by frequency of occurrence of the first element of the pair */
    static List<Pair<String, String>> valuesUnderEditDistance(Collection<Row> rows, String field, int maxEditDistance) {
        Multiset<String> set = HashMultiset.create();
        List<Pair<String, String>> result = new ArrayList<>();
        Map<String, String> idMap = new LinkedHashMap<>();

        for (Row r: rows) {
            String val = r.get(field);
            set.add(val);
            String idField = ID_PREFIX + field;
            String idVal = r.get(idField);
            if (!Util.nullOrEmpty(idField))
                idMap.put(val, idVal);
        }

        // the first element to be printed in the pair of close names should be the one with the higher count
        List<String> list = new ArrayList<>(Multisets.copyHighestCountFirst(set).elementSet());
        outer:
        for (int i = 0; i < list.size(); i++) {
            String f_i = list.get(i);
            String id_i = idMap.get(f_i);

            for (int j = i + 1; j < list.size(); j++) {
                String f_j = list.get(j);
                String id_j = idMap.get(f_j);
                if (Math.abs(f_j.length() - f_i.length()) > maxEditDistance)
                    continue outer;
                if (!Util.nullOrEmpty(id_i) && id_i.equals(id_j)) {
                    out.println ("same id for " + f_i + " and " + f_j);
                    continue; // at least their ids are the same, skip
                }
                if (Util1.editDistance(f_i, f_j) <= maxEditDistance) {
                    result.add(new Pair<>(f_i, f_j));
                }
            }
        }
        return result;
    }

    /* This method generates non-clashing values in the ID field for all rows that have an empty ID */
	public static void assignUnassignedIds(Collection<Row> allRows) {
		// any row which doesn't have an id assigned to it. Should be assigned one here. Each row is assigned a unique number

        Set<String> existingIds = allRows.stream().map(r -> r.get(Config.ID_FIELD)).collect(Collectors.toSet());

        int i = 1;
        for (Row r: allRows)
        {
            if (Util.nullOrEmpty(r.get(Config.ID_FIELD)))
            {
                // look for the next available ID that is not used
                while (existingIds.contains(Integer.toString(i)))
                    i++;

                r.set(Config.ID_FIELD, Integer.toString(i));
                i++;
            }
        }
	}


}





