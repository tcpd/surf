
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
    private static String SEPARATOR = "========================================\n";

    String name, cname, tname, stname, sex, state, pc, party;
    int year = -1, position = -1, votes = -1, rowNum = -1;
    Map<String, String> fields;
    public Row(Map<String, String> record, int rowNum) {
        this.fields = record;
        this.rowNum = rowNum;
        String pc = record.get("PC_name");
        String state = record.get("State_name");
        setup(record.get("Name"), record.get("Sex"), record.get("Year"), state, pc, record.get("Party"), record.get("Position"), record.get("Votes"));
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
        return name + " " + sex + "-" + year + "-" + state + "-" + pc + "-" + party + "-" + position + "-" + votes;
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
}

public class Main {

    private static String DELIMITERS = " -.,;:/'\\t<>\"`()@1234567890";
    private static PrintStream out = System.out;
    private static String SEPARATOR = "========================================\n";
    private static Multiset<String> findPrefixes(List<String> sortedTokenList) {
        Multiset<String> prefixes = HashMultiset.create();

        for (int i = 0; i < sortedTokenList.size(); i++) {
            String tok_i = sortedTokenList.get(i);
            for (int j = i + 1; j < sortedTokenList.size(); j++) {
                String tok_j = sortedTokenList.get(j);
                if (!tok_j.startsWith(tok_i))
                    break;
                if (tok_j.length() < tok_i.length() + 2)
                    continue;
                // tok_j starts with tok_i and is significantly longer, so tok_i could be a prefix
                prefixes.add(tok_i);
            }
        }
        return prefixes;
    }

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

    private static Map<String, List<String>> tokenize(Collection<String> strings, Multiset<String> validTokens) {
        Map<String, List<String>> map = new LinkedHashMap<>();

        for (String s: strings)
            map.put(s, tokenize(s, validTokens));
        return map;
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
            if (allRows.contains(r))
                out.println ("Warning: exactly same name, same year, same PC " + r);
            else {
                allRows.add(r);
                if (!allRows.contains(r))
                    out.println ("Warning: exactly same name, same year, same PC " + r);
            }

            pcs.add(state + "-" + pc);
        }

        // perform some consistency checks
        {
            // these rows are supposed to be exact duplicates
            checkSame(allRows, "Party", "Party_dup");
            checkSame(allRows, "Year", "Year_dup");

            // check: {Name, year, PC} unique?
            // if not, it means multiple people with the same name are contesting in the same PC in the same year -- not impossible.
            checkUnique(allRows, "Name", "Year", "PC_name");

            // for every year and PC_name combo, there must be exactly one winner (POS=1)
            Collection<Row> winners = select(allRows, "Position", "1");
            checkUnique(winners, "Year", "PC_name");
            // should also check that every occurrence of Year-PCname has at least one winner

            Collection<Row> nonIndependents = selectNot(allRows, "Party", "IND");

            // check: among the non-independents, is {year, PC, Party} unique?
            // if not, it means same party has multiple candidates in the same year in the same PC!!
            checkUnique(nonIndependents, "Year", "PC_name", "Party");

            // Check if every <year, PC> has at least 2 unique rows (otherwise its a walkover!)
            checkMinRows(nonIndependents, 2, "Year", "PC_name");

            // given a PC_name, does it uniquely determine the state?
            checkConsistentField(allRows, "PC_name", "State_name");

            // Look for misspelt PC names? (Also provide state_name in the output for debugging)
            printCloseValuesForField(allRows, "PC_name", "State_name");

            // Look for misspelt Party names
            printCloseValuesForField(allRows, "Party", null);
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

        /*
        // print out newly tokenized names
        out.println(SEPARATOR + " newly tokenized names (same token order)\n\n");
        count = 0;
        for (String newName: tnameToCname.keySet())
            if (tnameToCname.get(newName).size() > 1)
            {
                Collection<String> originals = tnameToCname.get(newName);
                out.println (++count + ". tokenized: " + newName);

                for (String original: originals) {
                    int rowCount = cnames.count(original);
                    String rowCountStr = "";
                    if (rowCount > 1)
                        rowCountStr = " #nrows: " + rowCount;
                    out.println (original + rowCountStr);
                }
            }
        */

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

            // check each one of stNameIdxsToCompareWith for edit distance = 1
            for (Integer j : stNameIdxsToCompareWith) {
                String stname1 = stnames.get(j);

                // now compare stname with stname1
                {
                    if (Math.abs(stname.length() - stname1.length()) > 1)
                        continue; // optimization: don't bother to compute edit distance if the lengths differ by more than 1

                    if (editDistance(stname.replaceAll(" ", ""), stname1.replaceAll(" ", "")) == 1) { // remember to remove spaces before comparing edit distance of stname stname1
                        // ok, we found something that looks close enough
                        out.println(++count + ". similar but not exactly same (st)names: \n");
                        // out.println("  canonical: " + stname);
                        printRowsWithStName(stnameToCname, cnameToRows, stname);
                        out.println("  -- and -- ");
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
    public static List<String> splitToken(String token, Multiset<String> prefixesToMatch, Multiset<String> suffixesToMatch)
    {
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
        s = s.replaceAll("TH", "T").replaceAll("V", "W").replaceAll("GH", "G").replaceAll("BH", "B").replaceAll("DH", "D").replaceAll("JH", "J").replaceAll("KH", "K").replaceAll("MH", "M").replaceAll("PH", "P").replaceAll("SH", "S").replaceAll("ZH", "Z");
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
        out.println (SEPARATOR + "Checking if fields identical: " + field1 + " and " + field2);
        for (Row r: allRows)
            if (!r.get(field1).equals(r.get(field2))) {
                out.println("Warning: " + field1 + " and " + field2 + " not the same! row=" + r + " " + field1 + "=" + r.get(field1) + " " + field2 + " = " + r.get(field2) + " row#: " + r.rowNum);
            }
    }

    /** checks if the combination of given fields occurs no more than once. Returns the # of violations */
    private static int checkUnique(Collection<Row> rows, String... fields) {
        String joiner = "-";
        StringBuilder sb = new StringBuilder();
        sb.append("Checking for uniqueness of ");
        out.println (SEPARATOR + "Checking for unique occurrences of the combination of " + Joiner.on("-").join(fields));

        Multimap<String, Row> map = HashMultimap.create();
        for (Row r: rows) {
            sb = new StringBuilder();
            for (String f : fields) {
                sb.append(r.get(f) + joiner);
            }
            String key = sb.toString();
            map.put(key, r);
        }

        int num = 0;
        for (String key: map.keySet()) {
            if (map.get(key).size() > 1) {
                out.println(++num + ". Warning: field-key " + key + " is not unique:");
                for (Row r: map.get(key))
                    out.println ("    " + r);
            }
        }
        return num;
    }

    /** checks if the combination of given fields occurs no more than once. Returns the # of violations */
    private static int checkMinRows(Collection<Row> rows, int minRows, String... fields) {
        String joiner = "-";
        out.println (SEPARATOR + "Checking for min count of " +  minRows + " row(s) for every unique combination of " + Joiner.on("-").join(fields));

        // set up a map of all the fields joined together with "-"
        Multimap<String, Row> map = HashMultimap.create();
        for (Row r: rows) {
            StringBuilder sb = new StringBuilder();
            for (String f : fields) {
                sb.append(r.get(f) + joiner);
            }
            String key = sb.toString();
            map.put(key, r);
        }

        // check if each key has at least minRows Rows assigned to it
        int num = 0;
        for (String key: map.keySet()) {
            Collection<Row> keyrows = map.get(key);
            if (keyrows.size() < minRows) {
                out.println(++num + ". Warning: field-key " + key + " has only " + keyrows.size() + " row(s)");
                for (Row r: keyrows)
                    out.println ("    " + r);
            }
        }
        return num;
    }

    private static int printCloseValuesForField(Collection<Row> allRows, String field, String fieldToPrint) {
        Multimap<String, String> map = HashMultimap.create();
        for (Row r: allRows)
            map.put(r.get(field), r.get(fieldToPrint));

        int num = 0;
        out.println (SEPARATOR + "Printing similar values for field " + field);
        List<String> list = new ArrayList<>(map.keySet());
        for (int i = 0; i < list.size(); i++) {
            String f_i = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                String f_j = list.get(j);
                if (editDistance(f_i, f_j) < 2) {
                    if (fieldToPrint != null)
                        out.println(++num + ". " + f_i + " (" + Joiner.on(",").join(map.get(f_i)) + ") and " + f_j + " (" + Joiner.on(",").join(map.get(f_j)) + ")");
                    else
                        out.println(++num + ". " + f_i + " and " + f_j);
                }
            }
        }
        return num;
    }

    /** checks that all instances of field1 have a consistent value for field2 */
    public static void checkConsistentField(Collection<Row> allRows, String field1, String field2) {
        out.println (SEPARATOR + "Checking if field " + field1 + " always has a consistent value for " + field2);
        Multimap<String, String> map = HashMultimap.create();
        for (Row r: allRows)
            map.put(r.get(field1), r.get(field2));

        int num = 0;

        for (String f1: map.keySet())
            if (map.get(f1).size() > 1)
                out.println(++num + ". " + field1 + "=" + f1 + " has multiple values for " + field2 + " (" + Joiner.on(",").join(map.get(f1)) + ")");
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
