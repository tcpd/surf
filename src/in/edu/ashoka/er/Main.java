
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

    String name, cname, tname, stname, sex, state, pc, party;
    int year = -1, position = -1, votes = -1, rowNum = -1;
    Map<String, String> fields;
    static String[] toStringFields = new String[0];

    public Row(Map<String, String> record, int rowNum) {
        this.fields = record;
        this.rowNum = rowNum;
        String pc = record.get("PC_name");
        String state = record.get("State_name");
        setup(record.get("Name"), record.get("Sex"), record.get("Year"), state, pc, record.get("Party"), record.get("Position"), record.get("Votes"));
    }

    public static void setToStringFields(String fieldSpec) {
        toStringFields = fieldSpec.split("-");
    }

    public void setup(String name, String sex, String year, String state, String pc, String party, String position, String votes) {
        this.name = name; this.sex = sex; this.state = state; this.pc = pc; this.party = party;
        try { this.year = Integer.parseInt(year); } catch (NumberFormatException nfe ) { }
        try { this.position = Integer.parseInt(position); } catch (NumberFormatException nfe ) { }
        try { this.votes = Integer.parseInt(votes); } catch (NumberFormatException nfe ) { }
    }

    void setCname(String cname) { this.cname = cname;}
    void setTname(String tname) { this.tname = tname;}
    void setSTname(String stname) { this.stname = stname;}

    public String toString() {
        return getFields(toStringFields, FIELDSPEC_SEPARATOR) +  " (row# " + rowNum + ")";
//        return name + " " + sex + "-" + year + "-" + state + "-" + pc + "-" + party + "-" + position + "-" + votes;
    }

    static int nameSimilarity (Row row1, Row row2) {
        if (row1.name.equals(row2.name))
            return 10;
        if (row1.cname.equals(row2.cname))
            return 9;
        if (row1.tname.equals(row2.tname))
            return 8;
        if (row1.stname.equals(row2.stname))
            return 7;
        // else something based on edit distance
        return 0;
    }

    boolean equal (String field, String value) {
        return this.get(field).equals(value); // ignore case?
    }

    static int similarity (Row row1, Row row2) {
        int result = nameSimilarity(row1, row2);
        if (row1.year == row2.year)
            result += 5;
        /*
        if (row1.state.equals(row2.state))
            result += 5;
            */
        if (Main.editDistance(row1.pc, row2.pc) < 2)
            result += 5;
        return 0;
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
        String x = fields.get(s);
        return (x != null) ? x : "";
    }

    public String get (String fields[]) { return getFields(fields, FIELDSPEC_SEPARATOR); }

    public String getFields (String fields[], String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append (this.get(fields[i]));
            if (i < fields.length-1)
                sb.append(FIELDSPEC_SEPARATOR);
        }
        return sb.toString();
    }
}

public class Main {

    private static String DELIMITERS = " -.,;:/'\\t<>\"`()@1234567890";
    private static PrintStream out = System.out;
    private static String SEPARATOR = "========================================\n";
    private static String FIELDSPEC_SEPARATOR = "-";

    private static Multiset<String> generateTokens(Collection<String> cnames) {
        // tokenize the names
        Multiset<String> tokens = HashMultiset.create();
        int nSingleNames = 0;
        for (String name : cnames) {
            StringTokenizer st = new StringTokenizer(name, DELIMITERS);
            if (st.countTokens() == 1)
                nSingleNames++;

            while (st.hasMoreElements()) {
                String token = st.nextToken();
                if (token.length() > 1) // ignore single letter tokens...
                    tokens.add(token);
            }
        }
        out.println("nSingleTokens: " + nSingleNames);
        return tokens;
    }

    /** splits an entire string into tokens */
    private static List<String> tokenize (String s, Multiset<String> validTokens) {
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

    public static void main(String[] args) throws IOException {
        Row.setToStringFields("Name-Sex-Year-State_name-PC_name-Party-Position-Votes");
        
        // terminology: name, cname (canonical name), tname (name after tokenization), stname (name after tokenization, with sorted tokens)
        Multiset<String> cnames = HashMultiset.create();
        Multimap<String, Row> nameToRows = HashMultimap.create(), cnameToRows = HashMultimap.create();
        Set<Row> allRows = new LinkedHashSet<>();
        int nRows = 0;
        Multiset<String> pcs = HashMultiset.create();
//        Reader in = new FileReader("GE.csv");
        // read the names from CSV
        Iterable<CSVRecord> records = CSVParser.parse(new File("GE.csv"), Charset.forName("UTF-8"), CSVFormat.EXCEL.withHeader());
        for (CSVRecord record : records) {
            String name = record.get("Name");
            if (name == null)
                continue;

            nRows++;
            name = name.toUpperCase();
            String cname = canonicalize(name);
            cnames.add(cname);
            String pc = record.get("PC_name");
            String state = record.get("State_name");
            Row r = new Row(record.toMap(), nRows);

            r.setCname(cname);
            cnameToRows.put(cname, r);
            allRows.add(r);

            pcs.add(state + "-" + pc);
        }

        int count = 0;
        List<String> list = new ArrayList(pcs.elementSet());
        Collections.sort(list);

        // now names and cnameToRows is setup

        out.println(SEPARATOR + nRows + " rows, " + cnames.elementSet().size() + " unique cnames");
        /*
        for (String n : Multisets.copyHighestCountFirst(cnames).elementSet()) {
            out.println(n + ": " + cnames.count(n));
            if (cnames.count(n) > 1)
                printNameDetail(nameToRows, n);
        }
        */

        Multiset<String> tokens = generateTokens(cnames);
        out.println(SEPARATOR + tokens.size() + " tokens, " + tokens.elementSet().size() + " unique");
        /*
        for (String n : Multisets.copyHighestCountFirst(tokens).elementSet()) {
            out.println(n + ": " + tokens.count(n));
        }
        */

        // perform some consistency checks
        {
            // these rows are supposed to be exact duplicates
            checkSame(allRows, "Party", "Party_dup");
            checkSame(allRows, "Year", "Year_dup");

            // check: {Name, year, PC} unique?
            // if not, it means multiple people with the same name are contesting in the same PC in the same year -- not impossible.
            out.println (SEPARATOR + " Checking if people with the same name contested the same seat in the same year");
            Multimap<String, Row> map = split(allRows, "Name-Year-PC_name-State_name");
            display(filter(map, "notequals", 1));

            out.println (SEPARATOR + " Checking that there is at most 1 winner per seat");
            Collection<Row> winners = select(allRows, "Position", "1");
            Multimap<String, Row> winnersMap = split(winners, "Year-PC_name-State_name");
            display(filter(winnersMap, "notequals", 1));

            out.println(SEPARATOR + " Checking that there is at least 1 winner per seat");
            Set<String> winnerKeys = winnersMap.keySet();
            map = split(allRows, "Year-PC_name-State_name");
            map = minus(map, winnerKeys);
            if (map.size() > 0) {
                out.println(" The following elections do not have a winner!?!");
                display(map);
            }

            // check: among the non-independents, is {year, PC, Party} unique?
            // if not, it means same party has multiple candidates in the same year in the same PC!!
            out.println (SEPARATOR + " Checking uniqueness of Year-PC-State-Party (non-independents)");
            Collection<Row> nonIndependents = selectNot(allRows, "Party", "IND");
            display(filter(split(nonIndependents, "Year-PC_name-State_name-Party"), "notequals", 1));

            // Check if every <year, PC> has at least 2 unique rows (otherwise its a walkover!)
            out.println (SEPARATOR + " Checking if there at least 2 candidates for every Year-PC");
            display(filter(split(allRows, "Year-PC_name"), "max", 1));

            // given a PC_name, does it uniquely determine the state?
            out.println(SEPARATOR + " Checking if each PC name belongs to exactly one state");
            display2Level(filter(split(split(allRows, "PC_name"), "State_name"), "min", 2), 3 /* max rows */);

            out.println (SEPARATOR + " Looking for possible misspellings in PC_name");
            displayPairs(allRows, valuesUnderEditDistance(allRows, "PC_name", 1), "PC_name", 3 /* max rows */);
            // printCloseValuesForField(allRows, "PC_name");

            out.println (SEPARATOR + " Looking for possible misspellings in Party");
            displayPairs(nonIndependents, valuesUnderEditDistance(allRows, "Party", 1), "Party", 3 /* max rows */);
            // printCloseValuesForField(allRows, "Party");
        }

        // set up tname and stname fields (tokenized and sorted-tokenized)
        // out.println(SEPARATOR + " newly tokenized names\n\n");
        Multimap<String, String> tnameToCname = HashMultimap.create(), stnameToCname = HashMultimap.create();

        for (String cname: cnames.elementSet()) {
            List<String> result = tokenize(cname, tokens);
            List<String> sortedResult = new ArrayList<>(result);
            Collections.sort(sortedResult);

            String tname =  Joiner.on(" ").join(result);
            String stname =  Joiner.on(" ").join(sortedResult);
            tnameToCname.put(tname, cname);
            stnameToCname.put(stname, cname);
            for (Row r: cnameToRows.get(cname)) {
                r.setTname(tname);
                r.setSTname(stname);
            }

           // out.println (cname + " -> " + " (" + cnames.count(cname) + " row(s))");
        }

        // sort stnames list, so longer stnames appear first (we probably have higher confidence in them)
        List<String> stnames = new ArrayList<>(stnameToCname.keySet());
        Collections.sort(stnames, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s2.length() - s1.length();
            }
        });

        out.println(SEPARATOR + "sorted and tokenized names (stnames)\n\n");
        count = 0;
        for (String stname: stnames)
            if (stnameToCname.get(stname).size() > 1)
            {
                out.println(++count + "."); // + ". canonical: " + stname);
                printRowsWithStName(stnameToCname, cnameToRows, stname);
            }

        // note: no reset of count.
        out.println(SEPARATOR + "similar related (st) names (edit distance = 1)\n\n");

        // setup tokenToStIdx: is a map of token (of at least 3 chars) -> all indexes in stnames that contain that token
        Multimap<String, Integer> tokenToStIdx = HashMultimap.create();
        {
            for (int i = 0; i < stnames.size(); i++) {
                String stname = stnames.get(i);
                StringTokenizer st = new StringTokenizer(stname, DELIMITERS);
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    if (tok.length() < 3)
                        continue;
                    tokenToStIdx.put(tok, i);
                }
            }
        }

        // set up nonSpaceStnames, computed only once for efficiency. its expensive to do this in the inner loop
        List<String> nonSpaceStnames = new ArrayList<>();
        for (int i = 0; i < stnames.size(); i++)
            nonSpaceStnames.add(stnames.get(i).replaceAll(" ", ""));

            // in order of stnames, check each name against all other stnames after it that share a token and have edit distance < 1, ignoring spaces.
        // only check with stnames after it, because we want to report a pair of stnames A and B only once (A-B, not B-A)
        for (int i = 0; i < stnames.size(); i++) {
            String stname = stnames.get(i);

            // compute the indexes of the stnames that we should compare this stname with, based on common tokens
            Set<Integer> stNameIdxsToCompareWith = new LinkedHashSet<>();
            {
                StringTokenizer st = new StringTokenizer(stname, DELIMITERS);
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    if (tok.length() < 3)
                        continue;
                    Collection<Integer> c = tokenToStIdx.get(tok);
                    for (Integer j: c)
                        if (j > i)
                            stNameIdxsToCompareWith.add(j);
                }
            }

            String nonSpaceStname = nonSpaceStnames.get(i);
            // check each one of stNameIdxsToCompareWith for edit distance = 1
            for (Integer j : stNameIdxsToCompareWith) {
                String stname1 = stnames.get(j);
                String nonSpaceStname1 = nonSpaceStnames.get(j);

                // now compare stname with stname1
                {
                    if (Math.abs(nonSpaceStname.length() - nonSpaceStname1.length()) > 1)
                        continue; // optimization: don't bother to compute edit distance if the lengths differ by more than 1

                    if (editDistance(nonSpaceStname, nonSpaceStname1) == 1) { // remember to remove spaces before comparing edit distance of stname stname1
                        // ok, we found something that looks close enough
                        out.println(++count + ". similar but not exactly same (st)names: \n");
                        // out.println("  canonical: " + stname);
                        printRowsWithStName(stnameToCname, cnameToRows, stname);
                        printRowsWithStName(stnameToCname, cnameToRows, stname1);
                    }
                }
            }
        }
    }

    private static void printRowsWithStName(Multimap<String, String> stnameToCname, Multimap<String, Row> cnameToRows, String stname)
    {
        Collection<String> cnamesSet = stnameToCname.get(stname);
        for (String cname : cnamesSet) {
            printNameDetail(cnameToRows, cname);
            out.println();
        }
    }
    // check the best way to break up this token, if any, based on the prefix/suffix with the highest count
    // returns an array with 1 token (unchanged, original) or 2 tokens. never more than 2 tokens.
    public static List<String> splitToken(String token, Multiset<String> prefixesToMatch, Multiset<String> suffixesToMatch) {
        List<String> result = new ArrayList<>();
        result.add(token);

        int bestCount = 0;
        for (int i = 2; i < token.length()-2; i++)
        {
            String prefix = token.substring(0, i);
            if (prefixesToMatch.count(prefix) > bestCount) {
                bestCount = prefixesToMatch.count(prefix);
                result = new ArrayList<>();
                result.add(prefix);
                result.add(token.substring(i));
            }
        }

        for (int i = 2; i < token.length()-2; i++)
        {
            String suffix = token.substring(i);
            if (suffixesToMatch.count(suffix) > bestCount) {
                bestCount = suffixesToMatch.count(suffix);
                result = new ArrayList<>();
                result.add(token.substring(0, i));
                result.add(suffix);
            }
        }
        return result;
    }

    private static void printNameDetail(Multimap<String, Row> namesToInfo, String n) {
        List<Row> rows = new ArrayList<>(namesToInfo.get(n));
        Collections.sort(rows);
        for (Row row: rows)
            out.println ("   " + row);
    }

    private static String canonicalize(String s)
    {
        // remove successive, duplicate chars, e.g.
        //  LOOK MAN SINGH RAI
        // LOKMAN SINGH RAI
        s = s.replaceAll("TH", "T").replaceAll("V", "W").replaceAll("GH", "G").replaceAll("BH", "B").replaceAll("DH", "D").replaceAll("JH", "J").replaceAll("KH", "K").replaceAll("MH", "M").replaceAll("PH", "P").replaceAll("SH", "S").replaceAll("ZH", "Z").replaceAll("Y", "I");
        s = s.replaceAll("AU", "OU").replaceAll("OO", "U").replaceAll("EE", "I").replace("KSH", "X"); // .replaceAll("YU", "U");
        char prev = ' ';
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
    private static Multimap <String, Row> split(Collection<Row> rows, String fieldSpec) {
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
    private static Multimap <String, Multimap <String, Row>> split(Multimap <String, Row> map, String fieldSpec) {
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

    private static Multimap <String, Row> filter(Multimap<String, Row> map, String fieldSpec, String valueSpec) {
        Multimap<String, Row> filteredMap = HashMultimap.create(); // required extra memory instead of clearing map in place... can do away with it depending on how map.keySet() iteration works
        for (String key: map.keySet()) {
            Collection<Row> rows = map.get(key);
            Collection<Row> filteredRows = filter(rows, fieldSpec, valueSpec);
            filteredMap.putAll(key, filteredRows);
        }
        return filteredMap;
    }

    private static Collection<Row> filter (Collection<Row> rows, String fieldSpec, String valueSpec) {
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
    private static<T> Multimap<String, T> filter (Multimap<String, T> map, String op, int count) {
        Multimap<String, T> filteredMap = HashMultimap.create(); // required extra memory instead of clearing map in place... can do away with it depending on how map.keySet() iteration works
        for (String key : map.keySet()) {
            Collection<T> vals = map.get(key);
                if (checkCount(vals, op, count))
                    filteredMap.putAll(key, vals);
        }
        return filteredMap;
    }

    /** removes keysToRemove from map. warning: modifies map */
    private static<T> Multimap<String, T> minus(Multimap<String, T> map, Collection<String> keysToRemove) {
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

    private static void display2Level (Multimap<String, Multimap<String, Row>> map, int maxRows) {
        out.println(SEPARATOR);
        int count = 0;
        for (String a: map.keySet()) {
            Collection<Multimap<String, Row>> bmapsfora = map.get(a);
            out.println(++count + ") " + a + " (" + pluralize(bmapsfora.size(), "value") + ")");
            for (Multimap<String, Row> bmap: bmapsfora)
                display(("    " + count + "."), bmap, maxRows);
        }
    }

    private static void display (Multimap<String, Row> map) { display("", map, Integer.MAX_VALUE);}

    private static void display (String prefix, Multimap<String, Row> map, int maxRows) {
        int count = 0;
        for (String s: map.keySet()) {
            Collection<Row> rows = map.get(s);
            out.println(prefix + (++count) + ") " + s + (rows.size() > maxRows ? " (" + pluralize(rows.size(), "row") + ")" : "")); // print row count only if we're not going to print all the rows below
            display(("    " + prefix + count + "."), rows, maxRows);
        }
    }

    private static void display (String prefix, Collection<Row> rows, int maxRows) {
        int count = 0;
        for (Row r: rows) {
            out.println(prefix + (++count) + ") " + r);
            if (count >= maxRows && rows.size() > count) {
                out.println(prefix.replaceAll("[^ ]", "") + "and " + pluralize((rows.size() - count), "more row") + "...");
                break;
            }
        }
    }

    private static void displayPairs (Collection<Row> rows, List<Pair<String, String>> pairs, String field, int maxRows) {
        Multimap<String, Row> map = split(rows, field);
        int count = 0;
        for (Pair<String, String> pair: pairs) {
            String v1 = pair.getFirst();
            String v2 = pair.getSecond();
            ++count;
            out.println (count + ".1) " + v1);
            display ("    " + count + ".1", map.get(v1), maxRows);
            out.println (count + ".2) " + v2);
            display ("    " + count + ".2", map.get(v2), maxRows);
        }
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
                if (editDistance(f_i, f_j) <= maxEditDistance) {
                    result.add(new Pair<>(f_i, f_j));
                }
            }
        }
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

    /** if num > 1, pluralizes the desc. will also commatize the num if needed. */
    public static String pluralize(int x, String desc)
    {
        return commatize(x) + " " + desc + ((x > 1) ? "s" : "");
    }

    public static String commatize(long n)
    {
        String result = "";
        do
        {
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
}
