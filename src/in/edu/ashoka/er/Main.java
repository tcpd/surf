
package in.edu.ashoka.er;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.MultiHashtable;
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

    public int hashCode() {
        return state.hashCode() ^ pc.hashCode() ^ year;
    }

    public boolean equals (Object other) {
        if (!(other instanceof Row)) return false;
        Row r1 = (Row) other;
        return /* name.equals(r1.name) && state.equals(r1.state) && */ pc.equals(r1.pc) && (year == r1.year) && party.equals(r1.party) && !party.equals("IND");
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

        checkSame(allRows, "Party", "Party_dup");
        checkSame(allRows, "Year", "Year_dup");

        checkConsistentField(allRows, "PC_name", "State_name");
        printCloseValuesForField(allRows, "PC_name", "State_name");
        printCloseValuesForField(allRows, "Party", null);

        checkUnique (allRows, "Name", "Year", "PC_name");

        // for every year and PC_name combo, there must be exactly one row with POS=1
        Collection<Row> winners = select(allRows, "Position", "1");
        checkUnique (winners, "Year", "PC_name");
        // should also check that Year-PCname has at least one winner

        int count = 0;
        List<String> list = new ArrayList(pcs.elementSet());
        Collections.sort(list);

        // now names and namesToInfo is setup

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

        out.println(SEPARATOR + " newly tokenized names\n\n");
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

            out.println (cname + " -> " + " (" + cnames.count(cname) + " row(s))");
        }

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

        out.println(SEPARATOR + "newly tokenized names (sorted)\n\n");
        count = 0;
        // print in sorted order, so longer stnames first (we probably have higher confidence in them)
        List<String> stnames = new ArrayList<>(stnameToCname.keySet());
        Collections.sort(stnames, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s2.length() - s1.length();
            }
        });

        for (String stname: stnames)
            if (stnameToCname.get(stname).size() > 1)
            {
                Collection<String> cnamesSet = stnameToCname.get(stname);
                out.println(++count + ". canonical: " + stname);
                for (String cname: cnamesSet)
                    printNameDetail(cnameToRows, cname);
            }

        out.println(SEPARATOR + "closely related names (sorted, 1 edit distance)\n\n");
        Multimap<String, String> tokenToSt = HashMultimap.create();
        for (String stname: stnames) {
            StringTokenizer st = new StringTokenizer(stname, DELIMITERS);
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (tok.length() < 3)
                    continue;
                tokenToSt.put(tok, stname);
            }
        }

        for (String stname: stnames) {
            Set<String> stnamesToCompareWith = new LinkedHashSet<String>();
            StringTokenizer st = new StringTokenizer(stname, DELIMITERS);
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (tok.length() < 3)
                    continue;
                stnamesToCompareWith.addAll(tokenToSt.get(tok));
            }

            for (String stname1 : stnamesToCompareWith) {
                if (editDistance(stname.replaceAll(" ", ""), stname1.replaceAll(" ", "")) == 1) {
                    out.println("similar but not exactly same (st)names: \n");
                   // out.println("  canonical: " + stname);
                    Collection<String> cnamesSet = stnameToCname.get(stname);
                    for (String cname: cnamesSet)
                        printNameDetail(cnameToRows, cname);
                    out.println ("  -- and -- ");
                    // out.println("  canonical: " + stname);
                    cnamesSet = stnameToCname.get(stname1);
                    for (String cname: cnamesSet)
                        printNameDetail(cnameToRows, cname);
                }
            }
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
        out.println (SEPARATOR + "Checking for uniqueness of " + Joiner.on("-").join(fields));

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
                out.println(++num + ". Warning: key " + key + " is not unique:");
                for (Row r: map.get(key))
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
