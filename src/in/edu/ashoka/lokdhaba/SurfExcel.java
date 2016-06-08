
package in.edu.ashoka.lokdhaba;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import edu.stanford.muse.util.Pair;
import edu.stanford.muse.util.UnionFindBox;
import edu.stanford.muse.util.Util;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Dataset {
    Collection<Row> rows;
    String name, description;
    Set<String> cColumns = new LinkedHashSet<>(); // this is the real list of col names (canonical) available (no aliases) for each row in this dataset.
    Multimap<String, String> cColumnToDisplayName = LinkedHashMultimap.create(); // display col names (both real and aliases)
    Map<String, String> cColumnAliases = new LinkedHashMap<>(); // cCol -> cCol as aliases.

    static String removePlural(String s)
    {
        /* S stemmer: http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.104.9828
        IF a word ends in “ies,” but not “eies” or “aies”
        THEN “ies” - “y”
        IF a word ends in “es,” but not “aes,” “ees,” or “oes”
        THEN “es” -> “e”
        IF a word ends in "s,” but not “us” or ‘ss”
           THEN "s" -> NULL
        */

        if (s.endsWith("ies") && !(s.endsWith("eies") || s.endsWith("aies")))
            return s.replaceAll("ies$", "y"); // this gets movies wrong!
        if (s.endsWith("es") && !(s.endsWith("aes") || s.endsWith("ees") || s.endsWith("oes")))
            return s.replaceAll("es$", "e");
        if (s.endsWith("es") && !(s.endsWith("aes") || s.endsWith("ees") || s.endsWith("oes")))
            return s.replaceAll("es$", "e");
        if (s.endsWith("s") && !(s.endsWith("us") || s.endsWith("ss")))
            return s.replaceAll("s$", "");
        return s;
    }

    // maintain a map for canonicalization, otherwise computing lower case, remove plurals etc takes a lot of time when reading a large dataset
    Map<String, String> cCache = new LinkedHashMap<>();
    String canonicalizeCol(String col) {
        String s = cCache.get(col);
        if (s != null)
            return s;

        String ccol = col.toLowerCase();
        ccol = ccol.replaceAll("_", "");
        ccol = ccol.replaceAll("-", "");
        ccol = removePlural(ccol).intern();
        cCache.put(col, ccol);
        return ccol;
    }

    void warnIfColumnExists(String col) {
        if (hasColumnName(col))
            System.err.println("Error: duplicate columns for repeated: " + col);
    }

    void registerColumn(String col) {
        warnIfColumnExists(col);
        String cCol = canonicalizeCol(col);
        cColumns.add(cCol);
        cColumnToDisplayName.put(cCol, col);
    }

    void registerColumnAlias(String oldCol, String newCol) {
        warnIfColumnExists(newCol);
        if (!hasColumnName(oldCol)) {
            System.err.println("Warning: no column called " + oldCol);
            return;
        }

        registerColumn(newCol);
        cColumnAliases.put(canonicalizeCol(newCol), canonicalizeCol(oldCol));
    }

    public Dataset (String filename) throws IOException {
        this.name = filename;

        Set<Row> allRows = new LinkedHashSet<>();
        int nRows = 0;
//        Reader in = new FileReader("GE.csv");
        // read the names from CSV
        Iterable<CSVRecord> records = CSVParser.parse(new File(filename), Charset.forName("UTF-8"), CSVFormat.EXCEL.withHeader());
        for (CSVRecord record : records) {
            nRows++;
            Map<String, String> map = record.toMap();

            if (nRows == 1) {
                for (String col : map.keySet()) {
                    registerColumn(col);
                }
            }

            Row r = new Row(map, nRows, this);
            allRows.add(r);
        }
        this.rows = allRows;
    }

    boolean hasColumnName(String col) {
        String ccol = canonicalizeCol(col);
        return cColumnAliases.keySet().contains(ccol) || cColumns.contains(ccol);
    }

    /** saves this dataset as a CSV  in the given file */
    public void save(String file) {
        //TO BE IMPLEMENTED
    }
}

class Row implements Comparable<Row> {
    private static PrintStream out = System.out;
    private static String FIELDSPEC_SEPARATOR = "-";
    private static Comparator currentComparator = null;

    int year = -1, position = -1, votes = -1, rowNum = -1;
    Map<String, Object> fields;
    static String[] toStringFields = new String[0];
    private Dataset d;

    static Comparator<Row> positionComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.getInt("Position") - r2.getInt("Position");
        }};

    static Comparator<Row> rowNumComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.rowNum - r2.rowNum;
        }};


    static Comparator<Row> yearComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.getInt("Year") - r2.getInt("Year");
        }};

    public Row(Map<String, String> map, int rowNum, Dataset d) {
        this.fields = new LinkedHashMap<>();
        this.d = d;

        for (String key: map.keySet())
            this.fields.put(d.canonicalizeCol(key), map.get(key)); // intern the key to save memory

        this.rowNum = rowNum;
        setup(map.get("Year"), map.get("Position"), map.get("Votes"));
    }

    public static void setToStringFields(String fieldSpec) {
        toStringFields = fieldSpec.split("-");
    }

    public static void setComparator(Comparator c) {
        currentComparator = c;
    }
    public void setup(String year, String position, String votes) {
        try { this.year = Integer.parseInt(year); } catch (NumberFormatException nfe ) { }
        try { this.position = Integer.parseInt(position); } catch (NumberFormatException nfe ) { }
        try { this.votes = Integer.parseInt(votes); } catch (NumberFormatException nfe ) { }
    }

    public String toString() {
        return getFields(toStringFields, FIELDSPEC_SEPARATOR) +  " (row# " + rowNum + ")";
    }

    boolean equal (String field, String value) {
        return this.get(field).equals(value); // ignore case?
    }

    public int compareTo(Row other)
    {
        if (currentComparator != null)
            return currentComparator.compare(this, other);

        // lower positions first
        if (position != other.position)
            return (position < other.position) ? -1 : 1;
        // more votes first
        if (votes != other.votes)
            return (votes > other.votes) ? -1 : 1;

        // otherwise, more or less random (don't really expect positions and votes to be exactly the same....
        return toString().compareTo(other.toString());
    }

    public String get(String col) {
        if (col == null)
            return "";
        col = d.canonicalizeCol(col);
        while (true) {
            String alias = d.cColumnAliases.get(col);
            if (alias == null)
                break;
            col = alias;
        }

        String x = (String) fields.get(col);
        return (x != null) ? x : "";
    }

    public void set(String col, String val) {
        col = d.canonicalizeCol(col);
        while (true) {
            String alias = d.cColumnAliases.get(col);
            if (alias == null)
                break;
            col = alias;
        }

        fields.put(col, val);
    }

    public String getFields (String fields[], String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append (this.get(fields[i]));
            if (i < fields.length-1)
                sb.append(separator);
        }
        return sb.toString();
    }

    public int setAsInt(String field) {
        field = d.canonicalizeCol(field);
        int x = Integer.MIN_VALUE;
        if (field == null || "".equals(field)){
            x = 0;
        } else {
            String val = get(field);
            try { x = Integer.parseInt(val); }
            catch (NumberFormatException nfe) { out.println ("Warning, failed to parse integer from field: " + val + "row: " + this); }
        }
        fields.put(("_i_" + field).intern(), x);
        return x;
    }

    public int getInt(String field) {
        Integer I = (Integer) fields.get("_i_" + field);
        if (I != null)
            return I;

        return setAsInt(field);
    }
}

class Tokenizer {
    static String DELIMITERS = " -.,;:/'\\t<>\"`()@1234567890";

    /** generates weighted token counts for tokens in each key. the count of a key (token) in the multiset is its weight. */
    private static Multiset<String> generateTokens(Multimap<String, Row> map) {
        // tokenize the names
        Multiset<String> tokens = HashMultiset.create();
        int nSingleWordKeys = 0;
        for (String key : map.keySet()) {
            StringTokenizer st = new StringTokenizer(key, DELIMITERS);
            if (st.countTokens() == 1)
                nSingleWordKeys++;
            int addCount = map.get(key).size();

            while (st.hasMoreElements()) {
                String token = st.nextToken();
                if (token.length() > 1) // ignore single letter tokens...
                    for (int i = 0; i < addCount; i++) // add it as many times as # rows with that name
                        tokens.add(token);
            }
        }
       // out.println("single word keys: " + nSingleWordKeys + " total tokens: " + tokens.size() + " unique: " + tokens.elementSet().size());
        return tokens;
    }

    /** splits an entire string into tokens */
    private static List<String> retokenize (String s, Multiset<String> validTokens) {
        List<String> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(s, DELIMITERS);
        while (st.hasMoreElements())
            result.addAll(splitToken(st.nextToken(), validTokens));
        return result;
    }

    /** splits an individual token according to the weight indicated by validTokens */
    private static List<String> splitToken (String s, Multiset<String> validTokens) {
        List<String> result = new ArrayList<>();

        int bestSplit = 4; // min. splitweight
        String bestFirst = "", bestSecond = "";
        for (int i = 2; i <= s.length()-2; i++) {
            String first = s.substring(0, i);
            String second = s.substring(i);
            // first and second must be both at least 2 chars long
            int splitWeight = validTokens.count(first) + validTokens.count(second);
            if (splitWeight > bestSplit)
            {
                bestSplit = splitWeight;
                bestFirst = first;
                bestSecond = second;
            }
        }

        if ("".equals(bestFirst)) {
            result.add(s);
        } else {
            // split greedily, we already pick best split here, and see if those splits can be split again. a global split may perform better in terms of weight, but we don't really care that much.
            result.addAll(splitToken(bestFirst, validTokens));
            result.addAll(splitToken(bestSecond, validTokens));
        }

        return result;
    }

    /** sets up canonicalized, retokenized and sorted-retokenized versions of the given field.
     * e.g if field is Name, fields called _c_Name, _t_Name and _st_Name are added to all rows */
    static void setupDesiVersions(Collection<Row> allRows, String field)
    {
        String cfield = "_c_" + field;
        String tfield = "_t_" + field;
        String stfield = "_st_" + field;

        // compute and set cfield for all rows
        for (Row r: allRows) {
            String val = r.get(field);
            String cval = canonicalizeDesi(val);
            r.set(cfield, cval);
        }

        // split on cfield and get token frequencies
        Multimap<String, Row> cfieldValToRows = SurfExcel.split(allRows, cfield);
        Multiset<String> tokens = generateTokens(cfieldValToRows);

        for (String val : cfieldValToRows.keySet()) {
            // compute and set retokenized val
            List<String> result = retokenize(val, tokens);
            String tval = Joiner.on(" ").join(result);

            // compute and set sorted-retokenized val
            List<String> sortedResult = new ArrayList<>(result);
            Collections.sort(sortedResult);
            String stval = Joiner.on(" ").join(sortedResult);

            for (Row r : cfieldValToRows.get(val)) {
                r.set (tfield, tval);
                r.set (stfield, stval);
            }
        }
    }

    static String[] replacements = new String[]{"[^A-Za-z\\s]", "", "TH", "T", "V", "W", "GH", "G", "BH", "B", "DH", "D", "JH", "J", "KH", "K", "MH", "M", "PH", "P", "SH", "S","ZH", "Z", "Z", "S","Y","I","AU", "OU","OO", "U","EE", "I", "KSH", "X"};
    static List<Pattern> replacementPatterns;
    static {
        // precompile patterns for performance. the patterns to be replaced
        // // replacements is an array 2X the size of replacementPatterns.
        replacementPatterns = new ArrayList<>();
        for (int i = 0; i < replacements.length; i += 2) {
            replacementPatterns.add(Pattern.compile(replacements[i]));
        }
    }

    /** canonicalizes Indian variations of spellings and replaces a run of repeated letters by a single letter */
    static String canonicalizeDesi(String s)
    {
        for (int i = 0; i < replacementPatterns.size(); i++) {
            s = replacementPatterns.get(i).matcher(s).replaceAll(replacements[2*i+1]);
        }
        char prev = ' ';

        // remove successive, duplicate chars, e.g.
        //  LOOK MAN SINGH RAI
        // LOKMAN SINGH RAI
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray())
        {
            if (c != prev)
                result.append(c);
            prev = c;
        }
//        if (!s.equals(result.toString()))
//           out.println ("canonical: " + s + " -> " + result);

        // these are from Gilles
        List<String> tokens = Util.tokenize(s);
        for (int i = 0; i < tokens.size(); i++) {
            s = s.replaceAll("MD", "MOHAMMAD");
            s = s.replaceAll("MOHAMED", "MOHAMMAD");
            s = s.replaceAll("MOHMED", "MOHAMMAD");
            s = s.replaceAll("PT", "PANDIT");
            s = s.replaceAll("PD", "PRASAD");
            s = s.replaceAll("PR", "PRASAD");
            if (i == 0)
                s = s.replaceAll("KU", "KUNWAR");
            else
                s = s.replaceAll("KU", "KUMAR"); // according to Gilles, KU in the middle of a name is KUMAR, but at the beginning its likely to be KUNWAR

            // ignore titles
            s = s.replaceAll("DR", "");
            s = s.replaceAll("MR", "");
            s = s.replaceAll("MRS", "");
            s = s.replaceAll("SMT", "");
            s = s.replaceAll("ENG", "");
            s = s.replaceAll("ADV", "");
            s = s.replaceAll("KUMAR", "");
            s = s.replaceAll("SARDAR", "");
            s = s.replaceAll("PANDIT", "");
        }
        return result.toString();
    }

    public static Map<String, String> canonicalizeDesi (Collection<String> list) {
        if (list == null)
            return null;

        Map<String, String> result = new LinkedHashMap<>();
        for (String s: list)
            result.put(s, canonicalizeDesi(s));

        return result;
    }
}

public class SurfExcel {

    private static PrintStream out = System.out;
    private static String SEPARATOR = "========================================\n";
    static String FIELDSPEC_SEPARATOR = "-";

    static Comparator<String> stringLengthComparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.length() - o1.length();
        }
    };

    public static void assignIDs(Collection<Row> allRows, String field, String filename) {
        EquivalenceHandler eh = new EquivalenceHandler(filename);
        String newField = "_id_" + field;
        for (Row r: allRows) {
            r.set(newField, eh.getClassNum(r.get(field)));
        }
    }

    public static void warn (String s) {
        out.println("WARNING " + s);
    }

    /** given a set of rows, returns pairs of strings that are within ed edit distance, after canonicalization, etc. */
    public static List<Pair<String, String>> similarPairsForField(Collection<Row> rows, String field, int ed)
    {
        List<Pair<String, String>> result = new ArrayList<>();

        Multimap<String, Row> fieldToRows = split (rows, "_st_" + field);
        List<String> listStField = new ArrayList<>(fieldToRows.keySet());
        Collections.sort(listStField, stringLengthComparator);
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
        // in order of stnames, check each name against all other stnames after it that share a token and have edit distance < 1, ignoring spaces.
        // only check with stnames after it, because we want to report a pair of stnames A and B only once (A-B, not B-A)
        Map<String, String> childToParent = new LinkedHashMap<>();
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
                    if (Math.abs(nonSpaceStField.length() - nonSpaceStField1.length()) > 1)
                        continue; // optimization: don't bother to compute edit distance if the lengths differ by more than 1

                    if (Util1.editDistance(nonSpaceStField, nonSpaceStField1) <= ed) { // make sure to use the space-removed versions to compute edit distance
                        // ok, we found something that looks close enough
                        // out.println("  canonical: " + stname);
                        result.add(new Pair<String, String>(stField, stField1));

                        // actually child can have multiple parents, but we just track one parent (the first one set) for now.
                        if (childToParent.get(stField1) == null)
                            childToParent.put(stField1, stField);
                    }
                }
            }
        }

        for (String val: fieldToRows.keySet()) {
            Collection<Row> rowsForThisVal = fieldToRows.get(val);
            String repVal = val;
            // recursive walk up to find first parent
            while (childToParent.get(repVal) != null)
                repVal = childToParent.get(repVal);
            for (Row r: rowsForThisVal)
                r.set ("_est_" + field, repVal);
        }

        // now find clusters:

        out.println ("Similar pairs found: " + result.size());
        out.println ("list size: " + listStField.size() + ", total comparisons: " + totalComparisons + " average: " + ((float) totalComparisons)/listStField.size());
        return result;
    }

    /** looks for keys in keys1 that are also in keys2, within maxEditDistance */
    public static List<Pair<String, String>> desiMatch2Lists(Collection<String> keys1, Collection<String> keys2, int maxEditDistance) {

        // cannot canonicalize by space here because what we return has to be the same string passed in, in keys1/2.
        // sort by length to make edit distance more efficient
        List<String> stream1 = (List) keys1.stream().sorted(stringLengthComparator).collect(Collectors.toList());
        List<String> stream2 = (List) keys2.stream().sorted(stringLengthComparator).collect(Collectors.toList());
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
                    result.add(new Pair<String, String>(key1, key2));
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
        List<Row> result = new ArrayList<Row>();
        for (Row r: rows)
            if (value.equals(r.get(field)))
                result.add(r);
        return result;
    }

    static Collection<Row> selectNot(Collection<Row> rows, String field, String value) {
        List<Row> result = new ArrayList<Row>();
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

    static void assign_unassignedIds(Collection<Row> allRows, String field) {
        String idField = "_id_" + field;
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

    /*
    static void profile(Collection<Row> allRows, String field) {
        String idField = "_id_" + field;
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
    static Multimap <String, Row> split(Collection<Row> rows, String fieldSpec) {
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
        Collections.sort(keyList, comparator);

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
            String idField = "_id_" + field;
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


}

class Util1 {

    static int editDistance(String word1, String word2) {
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
}

class Display {
    private static PrintStream out = System.out;

    static void displayCollection(Collection<String> c, Collection<String> reference) {
        int i = 1;
        Map<String, String> cStrings = Tokenizer.canonicalizeDesi(c);
        Map<String, String> cReference = Tokenizer.canonicalizeDesi(reference);

        for (String s: c) {
            String probablyString = "";
            if (reference != null)
                for (String s1: reference) {
                    if (Util1.editDistance(cStrings.get(s), cReference.get(s1)) < 2) {
                        probablyString = " (could be " + s1 + ")";
                        break;
                    }
                }
            out.println(i++ + ". " + s + probablyString);
        }
    }

    static void display2Level(Multimap<String, Multimap<String, Row>> map, int maxRows) {
        display2Level(map, maxRows, true);
    }

    static void display2Level(Multimap<String, Multimap<String, Row>> map, int maxRows, boolean showKey) {
        int acount = 0;
        for (String a : map.keySet()) {
            Collection<Multimap<String, Row>> bmapsfora = map.get(a);
            acount++;
            out.println(acount + ") " + (showKey ? a : "") + " (" + Util.pluralize(bmapsfora.size(), "value") + ")");
            int bcount = 0;
            for (Multimap<String, Row> bmap : bmapsfora)
                display(("    " + acount + "." + (++bcount) + "."), bmap, maxRows, true /* suppress counting */);
        }
    }

    static void display(Multimap<String, Row> map) {
        display("", map, Integer.MAX_VALUE);
    }

    private static void display(String prefix, Multimap<String, Row> map, int maxRows) {
        display(prefix, map, maxRows, false);
    }

    private static void display(String prefix, Multimap<String, Row> map, int maxRows, boolean suppressCounting) {
        int count = 0;
        for (String s : map.keySet()) {
            Collection<Row> rows = map.get(s);
            ++count;
            out.println(prefix + (suppressCounting ? "" : count) + ") " + s + (rows.size() > maxRows ? " (" + Util.pluralize(rows.size(), "row") + ")" : "")); // print row count only if we're not going to print all the rows below
            display(("    " + prefix + count + "."), rows, maxRows);
        }
    }

    /** given set1/2 with descriptor desc1/2, prints the list of things in set1 but not set2 and vice versa.
     * for these items, also prints suggested matches (<= edit distance 1) in the other set.
     */
    static void displayDiffs(String desc1, Set<String> set1, String desc2, Set<String> set2)
    {
        Set<String> tmp = new LinkedHashSet<>(set1);
        tmp.removeAll(set2);
        if (tmp.size() > 0) {
            out.println("Only in " + desc1 + " and not " + desc2);
            List<String> tmpList = new ArrayList<>(tmp);
            Collections.sort(tmpList);
            Display.displayCollection (tmpList, set2);
        }

        tmp = new LinkedHashSet<>(set2);
        tmp.removeAll(set1);
        List<String> tmpList = new ArrayList<>(tmp);
        Collections.sort(tmpList);
        if (tmp.size() > 0) {
            out.println("Only in " + desc2 + " and not " + desc1);
            Display.displayCollection (tmpList, set1);
        }
    }

    /** shows the diffs in matches (each key is shown with a few rows taken from map1/map2 for context).
     * matches consists of pairs of <S1, S2>. where S1 is a key into map1, S2 is a key into map2. */
    public static void displayListDiff( List<Pair<String, String>> matches, Multimap<String, Row> map1, Multimap<String, Row> map2, String description1, String description2) {
        int count = 0;
        int maxRows = 10;
        for (Pair<String, String> p : matches) {
            Display.display(description1 + " " + count + ".1.", map1.get(p.getFirst()), maxRows);
            Display.display(description2 + " " + count + ".2.", map2.get(p.getSecond()), maxRows);
            count++;
            out.println();
        }
    }

    static void display(String prefix, Collection<Row> rows, int maxRows) {
        int count = 0;
        /** sort the rows by position -- we prefer to show more significant candidates first. Could also do this by year or some other criterion for the row */
        List<Row> rowList = new ArrayList<>(rows);
        Collections.sort (rowList);

        for (Row r : rowList) {
            out.println(prefix + (++count) + ") " + r);
            // if we're at maxRows-1, check if we should print another row (if its the last one), or a line saying "... and NNN more rows" (if at least 2 rows remain to be printed)
            if (count == (maxRows-1) && (rows.size() - count) >= 2) {
                // for the "... and NNN more rows" line, kill the prefix, only keep the blanks. we're breaking out anyway, no harm modifying prefix
                prefix = prefix.replaceAll("[^ ]", "");
                out.println(prefix + "and " + Util.pluralize((rows.size() - count), "more row") + "...");
                break;
            }
        }
    }

    static void displayPairs(Collection<Row> rows, List<Pair<String, String>> pairs, String field, int maxRows) { displayPairs(rows, pairs, field, maxRows, true); }
    static void displaySimilarValuesForField(Collection<Row> rows, String field, int maxEditDistance, int nRows) {
        List<Pair<String, String>> closePairs = SurfExcel.valuesUnderEditDistance(rows, field, maxEditDistance);
        Display.displayPairs(rows, closePairs, field, nRows);
    }

    private static void displayPairs(Collection<Row> rows, List<Pair<String, String>> pairs, String field, int maxRows, boolean showKey) {
        Multimap<String, Row> map = SurfExcel.split(rows, field);
        int count = 0;
        for (Pair<String, String> pair : pairs) {
            String v1 = pair.getFirst();
            String v2 = pair.getSecond();
            ++count;
            out.println(count + ".1) " + (showKey ? v1 : ""));
            display("    " + count + ".1.", map.get(v1), maxRows);
            out.println(count + ".2) " + (showKey ? v2 : ""));
            display("    " + count + ".2.", map.get(v2), maxRows);
        }
    }
}

class EquivalenceHandler {
    Map<String, UnionFindBox<String>> stringToBox = new LinkedHashMap<>();

    public EquivalenceHandler(String equivalenceFile) {
        List<String> lines = new ArrayList<>();
        try { lines = Util.getLinesFromFile(equivalenceFile, true); }
        catch (Exception e) { Util.print_exception(e); }

        int lineNum = 0;
        for (String line: lines) {
            lineNum++;
            String[] equivs = line.split("=");
            Arrays.sort(equivs); // sort alphabetically
            if (equivs.length != 2) {
                SurfExcel.warn("Bad input at line# " + lineNum + " in " + equivalenceFile + ": " + line);
                continue;
            }

            String left = equivs[0], right = equivs[1];
            UnionFindBox<String> leftBox = stringToBox.get(left);
            if (leftBox == null) {
                leftBox = new UnionFindBox<String>(left);
                stringToBox.put(left, leftBox);
            }
            UnionFindBox<String> rightBox = stringToBox.get(right);
            if (rightBox == null) {
                rightBox = new UnionFindBox<String>(right);
                stringToBox.put(right, rightBox);
            }
            leftBox.unify(rightBox);
        }

        UnionFindBox.assignClassNumbers(stringToBox.values());
    }

    public String getClassNum(String s) {
        UnionFindBox<String> ufo = stringToBox.get(s);
        if (ufo == null)
            return "";
        return Integer.toString(ufo.classNum);
    }
}

