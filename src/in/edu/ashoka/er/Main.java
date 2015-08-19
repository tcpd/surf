
package in.edu.ashoka.er;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

class Row implements Comparable<Row> {
    private static PrintStream out = System.out;
    private static String FIELDSPEC_SEPARATOR = "-";

    int year = -1, position = -1, votes = -1, rowNum = -1;
    Map<String, Object> fields;
    static String[] toStringFields = new String[0];

    static Comparator<Row> positionComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.getInt("Position") - r2.getInt("Position");
        }};

    static Comparator<Row> yearComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.getInt("Year") - r2.getInt("Year");
        }};

    public Row(Map<String, String> record, int rowNum) {
        this.fields = new LinkedHashMap<>();
        for (String key: record.keySet())
            this.fields.put(key.intern(), record.get(key)); // intern the key to save memory

        this.rowNum = rowNum;
        setup(record.get("Year"), record.get("Position"), record.get("Votes"));
    }

    public static void setToStringFields(String fieldSpec) {
        toStringFields = fieldSpec.split("-");
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
        // lower positions first
        if (position != other.position)
            return (position < other.position) ? -1 : 1;
        // more votes first
        if (votes != other.votes)
            return (votes > other.votes) ? -1 : 1;

        // otherwise, more or less random (don't really expect positions and votes to be exactly the same....
        return toString().compareTo(other.toString());
    }

    public String get(String s) {
        if (s == null)
            return "";
        String x = (String) fields.get(s);
        return (x != null) ? x : "";
    }

    public void set(String key, String val) {
        fields.put(key, val);
    }

    public String getFields (String fields[], String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append (this.get(fields[i]));
            if (i < fields.length-1)
                sb.append(FIELDSPEC_SEPARATOR);
        }
        return sb.toString();
    }

    public int setAsInt(String field) {
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
    private static PrintStream out = System.out;

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
        out.println("single word keys: " + nSingleWordKeys + " total tokens: " + tokens.size() + " unique: " + tokens.elementSet().size());
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
    static void setupVersions(Collection<Row> allRows, String field)
    {
        String cfield = "_c_" + field;
        String tfield = "_t_" + field;
        String stfield = "_st_" + field;

        // compute and set cfield for all rows
        for (Row r: allRows) {
            String val = r.get(field);
            String cval = canonicalize(val);
            r.set(cfield, cval);
        }

        // split on cfield and get token frequencies
        Multimap<String, Row> cfieldValToRows = Main.split(allRows, cfield);
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

    /** canonicalizes Indian variations of spellings and replaces a run of repeated letters by a single letter */
    static String canonicalize(String s)
    {
        // canonicalize standard Indian variations of spellings
        s = s.replaceAll("TH", "T").replaceAll("V", "W").replaceAll("GH", "G").replaceAll("BH", "B").replaceAll("DH", "D").replaceAll("JH", "J").replaceAll("KH", "K").replaceAll("MH", "M").replaceAll("PH", "P").replaceAll("SH", "S").replaceAll("ZH", "Z").replaceAll("Y", "I");
        s = s.replaceAll("AU", "OU").replaceAll("OO", "U").replaceAll("EE", "I").replace("KSH", "X"); // .replaceAll("YU", "U");
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

        return result.toString();
    }
}

public class Main {

    private static PrintStream out = System.out;
    private static String SEPARATOR = "========================================\n";
    private static String FIELDSPEC_SEPARATOR = "-";

    static Comparator<String> stringLengthComparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.length() - o1.length();
        }
    };

    public static void main(String[] args) throws IOException {
        Row.setToStringFields("Name-Sex-Year-State_name-PC_name-Party-Position-Votes");

        // terminology: name, cname (canonical name), tname (name after tokenization), stname (name after tokenization, with sorted tokens)
        Multimap<String, Row> nameToRows = HashMultimap.create(), cnameToRows = HashMultimap.create();
        Set<Row> allRows = new LinkedHashSet<>();
        int nRows = 0;
//        Reader in = new FileReader("GE.csv");
        // read the names from CSV
        Iterable<CSVRecord> records = CSVParser.parse(new File("GE.csv"), Charset.forName("UTF-8"), CSVFormat.EXCEL.withHeader());
        for (CSVRecord record : records) {
            String name = record.get("Name");
            if (name == null)
                continue;

            nRows++;
            Row r = new Row(record.toMap(), nRows);
            allRows.add(r);
        }

        // perform some consistency checks
        {
            // these rows are supposed to be exact duplicates
            checkSame(allRows, "Party", "Party_dup");
            checkSame(allRows, "Year", "Year_dup");

            // check: {Name, year, PC} unique?
            // if not, it means multiple people with the same name are contesting in the same PC in the same year -- not impossible.
            out.println(SEPARATOR + " Checking if people with the same name contested the same seat in the same year");
            Multimap<String, Row> map = split(allRows, "Name-Year-PC_name-State_name");
            Display.display(filter(map, "notequals", 1));

            out.println(SEPARATOR + " Checking that there is at most 1 winner per seat");
            Collection<Row> winners = select(allRows, "Position", "1");
            Multimap<String, Row> winnersMap = split(winners, "Year-PC_name-State_name");
            Display.display(filter(winnersMap, "notequals", 1));

            out.println(SEPARATOR + " Checking that there is at least 1 winner per seat");
            Set<String> winnerKeys = winnersMap.keySet();
            map = split(allRows, "Year-PC_name-State_name");
            map = minus(map, winnerKeys);
            if (map.size() > 0) {
                out.println(" The following elections do not have a winner!?!");
                Display.display(map);
            }

            Collection<Row> selectedRows = selectNot(allRows, "Name", "NONE OF THE ABOVE");
            selectedRows = selectNot(selectedRows, "Sex", "F");
            selectedRows = selectNot(selectedRows, "Sex", "M");
            out.println(SEPARATOR + " Checking values in \"Sex\" fields other than M and F");
            Display.display("", selectedRows, Integer.MAX_VALUE);

            // check: among the non-independents, is {year, PC, Party} unique?
            // if not, it means same party has multiple candidates in the same year in the same PC!!
            out.println(SEPARATOR + " Checking uniqueness of Year-PC-State-Party (non-independents)");
            Collection<Row> nonIndependents = selectNot(allRows, "Party", "IND");
            Display.display(filter(split(nonIndependents, "Year-PC_name-State_name-Party"), "notequals", 1));

            // Check if every <year, PC> has at least 2 unique rows (otherwise its a walkover!)
            out.println(SEPARATOR + " Checking if there at least 2 candidates for every Year-PC");
            Display.display(filter(split(allRows, "Year-PC_name"), "max", 1));

            // given a PC_name, does it uniquely determine the state?
            out.println(SEPARATOR + " Checking if each PC name belongs to exactly one state");
            Display.display2Level(filter(split(split(allRows, "PC_name"), "State_name"), "min", 2), 3 /* max rows */);

            out.println(SEPARATOR + " Looking for possible misspellings in PC_name");
            Display.displayPairs(allRows, valuesUnderEditDistance(allRows, "PC_name", 1), "PC_name", 3 /* max rows */);

            out.println(SEPARATOR + " Looking for possible misspellings in Party");
            Display.displayPairs(nonIndependents, valuesUnderEditDistance(allRows, "Party", 1), "Party", 3 /* max rows */);

            Tokenizer.setupVersions(allRows, "Name");

            // given a st_name, does it uniquely determine the sex?
            out.println(SEPARATOR + " Checking if each (C-R-S) name belongs to exactly one sex");
            Display.display2Level(filter(split(split(allRows, "_st_Name"), "Sex"), "min", 2), 3 /* max rows */, false);

            Tokenizer.setupVersions(allRows, "PC_name");
            out.println(SEPARATOR + " Looking for similar PCs");
            Display.display2Level(reportSimilarValuesForField(allRows, "PC_name"), 3, false);

            out.println(SEPARATOR + " Looking for similar names");
            Display.display2Level(reportSimilarValuesForField(allRows, "Name"), 3, false);

            /*
            out.println(SEPARATOR + "Similar names (ST edit distance = 1)");
            Display.displayPairs(allRows, similarPairsForField(allRows, "Name", 2), "_st_Name", 3, false);
            */
            out.println(SEPARATOR + "New attempt: Similar names (ST edit distance = 1)");
            similarPairsForField (allRows, "Name", 1);
            Display.display2Level (sort (filter(split(split(allRows, "_est_Name"), "Name"), "min", 2), stringLengthComparator), 3);
        }
    }


    public static List<Pair<String, String>> similarPairsForField(Collection<Row> rows, String field, int ed)
    {
        List<Pair<String, String>> result = new ArrayList<>();

        Multimap<String, Row> fieldToRows = split (rows, "_st_" + field);
        List<String> listStField = new ArrayList<>(fieldToRows.keySet());
        Collections.sort(listStField, stringLengthComparator);

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

        // these non-space versions will be used for edit distance comparison.
        // order in nonSpaceStFields is the same as in listStField
        List<String> nonSpaceStFields = new ArrayList<>();
        for (String fieldVal: listStField)
            nonSpaceStFields.add(fieldVal.replaceAll(" ", ""));

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

                    if (Util.editDistance(nonSpaceStField, nonSpaceStField1) <= ed) { // make sure to use the space-removed versions to compute edit distance
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

    /** Prints similar (canonicalized, retokenized, sorted) names. Tokenizer.setupVersions(allRows, "Name"); should already been called on this field */
    static Multimap<String, Multimap<String, Row>> reportSimilarValuesForField(Collection<Row> rows, String field) {

        Multimap<String, Row> stFieldSplit = split(rows, "_st_" + field);
        Multimap<String, Multimap<String, Row>> stFieldToFieldToRows = split(stFieldSplit, field);
        Multimap<String, Multimap<String, Row>> stFieldToMultipleFieldToRows = sort(filter(stFieldToFieldToRows, "min", 2), stringLengthComparator);
        return stFieldToMultipleFieldToRows;
    }

    private static Collection<Row> select (Collection<Row> rows, String field, String value) {
        List<Row> result = new ArrayList<Row>();
        for (Row r: rows)
            if (value.equals(r.get(field)))
                result.add(r);
        return result;
    }

    private static Collection<Row> selectNot (Collection<Row> rows, String field, String value) {
        List<Row> result = new ArrayList<Row>();
        for (Row r: rows)
            if (!value.equals(r.get(field)))
                result.add(r);
        return result;
    }

    private static void checkSame(Collection<Row> allRows, String field1, String field2) {
        out.println(SEPARATOR + "Checking if fields identical: " + field1 + " and " + field2);
        for (Row r: allRows)
            if (!r.get(field1).equals(r.get(field2))) {
                out.println("Warning: " + field1 + " and " + field2 + " not the same! row=" + r + " " + field1 + "=" + r.get(field1) + " " + field2 + " = " + r.get(field2) + " row#: " + r.rowNum);
            }
    }

    /** converts a collection of rows to a map in which each unique value for the given fieldspec is the key and the value for that key is the rows with that value for the fieldspec */
    static Multimap <String, Row> split(Collection<Row> rows, String fieldSpec) {
        Multimap<String, Row> result = HashMultimap.create();
        String[] fields = fieldSpec.split(FIELDSPEC_SEPARATOR);
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
        Collections.sort (keyList, comparator);

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
    private static List<Pair<String, String>> valuesUnderEditDistance(Collection<Row> rows, String field, int maxEditDistance) {
        Multiset<String> set = HashMultiset.create();
        List<Pair<String, String>> result = new ArrayList<>();

        for (Row r: rows) {
            String val = r.get(field);
            set.add(val);
        }

        // the first element to be printed in the pair of close names should be the one with the higher count
        List<String> list = new ArrayList<>(Multisets.copyHighestCountFirst(set).elementSet());
        for (int i = 0; i < list.size(); i++) {
            String f_i = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                String f_j = list.get(j);
                if (Util.editDistance(f_i, f_j) <= maxEditDistance) {
                    result.add(new Pair<>(f_i, f_j));
                }
            }
        }
        return result;
    }


}

class Util {
    /**
     * if num > 1, pluralizes the desc. will also commatize the num if needed.
     */
    public static String pluralize(int x, String desc) {
        return commatize(x) + " " + desc + ((x > 1) ? "s" : "");
    }

    public static String commatize(long n) {
        String result = "";
        do {
            if (result.length() > 0)
                result = "," + result;
            long trio = n % 1000; // 3 digit number to be printed
            if (trio == n) // if this is the last trio, no lead of leading 0's,
                // otherwise make sure to printf %03f
                result = String.format("%d", n % 1000) + result;
            else
                result = String.format("%03d", n % 1000) + result;

            n = n / 1000;
        } while (n > 0);
        return result;
    }

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

    static void display(String prefix, Multimap<String, Row> map, int maxRows) {
        display(prefix, map, maxRows, false);
    }

    static void display(String prefix, Multimap<String, Row> map, int maxRows, boolean suppressCounting) {
        int count = 0;
        for (String s : map.keySet()) {
            Collection<Row> rows = map.get(s);
            ++count;
            out.println(prefix + (suppressCounting ? "" : count) + ") " + s + (rows.size() > maxRows ? " (" + Util.pluralize(rows.size(), "row") + ")" : "")); // print row count only if we're not going to print all the rows below
            display(("    " + prefix + count + "."), rows, maxRows);
        }
    }

    static void display(String prefix, Collection<Row> rows, int maxRows) {
        int count = 0;
        /** sort the rows by position -- we prefer to show more significant candidates first. Could also do this by year or some other criterion for the row */
        List<Row> rowList = new ArrayList<>(rows);
        Collections.sort (rowList, Row.positionComparator);

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

    static void displayPairs(Collection<Row> rows, List<Pair<String, String>> pairs, String field, int maxRows, boolean showKey) {
        Multimap<String, Row> map = Main.split(rows, field);
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

