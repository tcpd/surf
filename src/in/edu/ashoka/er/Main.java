
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
    String name, cname, tname, stname, sex, state, pc, party;
    int year = -1, position = -1, votes = -1, rowNum;

    public Row(String name, String sex, String year, String state, String pc, String party, String position, String votes) {
        this.name = name; this.sex = sex; this.state = state; this.pc = pc; this.party = party;
        try { this.year = Integer.parseInt(year); } catch (NumberFormatException nfe ) { }
        try { this.position = Integer.parseInt(position); } catch (NumberFormatException nfe ) { }
        try { this.votes = Integer.parseInt(votes); } catch (NumberFormatException nfe ) { }
    }

    public void setCname(String cname) { this.cname = cname;}
    public void setTname(String tname) { this.tname = tname;}
    public void setSTname(String stname) { this.stname = stname;}

    public String toString() {
        return name + "->" + cname + "->" + tname + "->" + stname + " " + sex + "-" + year + "-" + state + "-" + pc + "-" + party + "-" + position + "-" + votes;
    }

    public static int nameSimilarity (Row row1, Row row2) {
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

    public static int similarity (Row row1, Row row2) {
        int result = nameSimilarity(row1, row2);
        if (row1.year == row2.year)
            result += 5;
        if (row1.state.equals(row2.state))
            result += 5;
        if (row1.pc.equals(row2.pc))
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

    public static String DELIMITERS = " -.,;:/'\\t<>\"`()@1234567890";
    static PrintStream out = System.out;

    public static Multiset<String> findPrefixes(List<String> sortedTokenList) {
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
            Row r = new Row(name, record.get("Sex"), record.get("Year"), record.get("State_name"), pc, record.get("Party"), record.get("Position"), record.get("Votes"));
            if (!record.get("Party").equals(record.get("Party_dup"))) {
                 out.println ("Warning: party and party dup not the same! row=" + r + " Party_dup=" + record.get("Party_dup") + " row#: " + nRows);
            }
            if (!record.get("Year").equals(record.get("Year_dup"))) {
                out.println ("Warning: year and year dup not the same! row=" + r + " dup=" + record.get("Year_dup") + " row#: " + nRows);
            }

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

        int count = 0;
        List<String> list = new ArrayList(pcs.elementSet());
        Collections.sort(list);
        for (String pc: list)
            out.println (count++ + "PC: " + pc + " count: " + pcs.count(pc));

        // now names and namesToInfo is setup

        // tokenize the names
        Multiset<String> tokens = HashMultiset.create();
        int nSingleNames = 0;
        for (String name: cnames) {
            StringTokenizer st = new StringTokenizer(name, DELIMITERS);
            if (st.countTokens() == 1) {
                nSingleNames++;
                for (Row info: cnameToRows.get(name))
                    if (info.position == 1) {
                        out.println ("Wow: single name " + name + " won " + info);
                    }
            }

            while (st.hasMoreElements()) {
                String token = st.nextToken();
                if (token.length() > 1) // ignore single letter tokens...
                    tokens.add(token);
            }
        }

        out.println("\n\n------------------------- " + nRows + " rows, " + cnames.elementSet().size() + " unique names, " + nSingleNames + " single word names\n\n");
        for (String n : Multisets.copyHighestCountFirst(cnames).elementSet()) {
            out.println(n + ": " + cnames.count(n));
            if (cnames.count(n) > 1)
                printNameDetail(nameToRows, n);
        }

        out.println("\n\n------------------------- " + tokens.size() + " tokens, " + tokens.elementSet().size() + " unique");
        for (String n : Multisets.copyHighestCountFirst(tokens).elementSet()) {
            out.println(n + ": " + tokens.count(n));
        }

        // detect prefixes

        // first sort the tokens
        List<String> sortedTokenList = new ArrayList<String>(tokens.elementSet());
        Collections.sort(sortedTokenList);

        Multiset<String> prefixes = findPrefixes (sortedTokenList);
        Multiset<String> prefixesToMatch = HashMultiset.create(); // with prefix count > 1 and token count > 1

        out.println("\n\n------------------------- " + prefixes.size() + " prefixes\n\n");
        for (String n : Multisets.copyHighestCountFirst(prefixes).elementSet()) {
            out.println(n + " as prefix=" + prefixes.count(n) + " as token=" + tokens.count(n));
            if (n.length() >= 3 && prefixes.count(n) > 1 && tokens.count(n) > 2) {
                prefixesToMatch.add(n);
                prefixesToMatch.setCount(n, tokens.count(n));
            }
        }

        // detect suffixes
        // first reverse all tokens and sort
        List<String> revTokens = new ArrayList<>();
        for (String s: tokens.elementSet())
            revTokens.add(new StringBuilder(s).reverse().toString());
        Collections.sort(revTokens);

        // use findPrefixes on the reversed strings to find the suffixes to look for
        Multiset<String> suffixes = findPrefixes (revTokens);

        out.println("\n\n------------------------- " + suffixes.size() + " suffixes\n\n");
        Multiset<String> suffixesToMatch = HashMultiset.create();
        for (String n : Multisets.copyHighestCountFirst(suffixes).elementSet()) {
            String proper_n = new StringBuilder(n).reverse().toString();
            out.println(proper_n + " as suffix=" + suffixes.count(n) + " as token=" + tokens.count(proper_n));
            if (proper_n.length() >= 3 && suffixes.count(n) > 1 && tokens.count(proper_n) > 2) {
                suffixesToMatch.add(proper_n);
                suffixesToMatch.setCount(proper_n, tokens.count(proper_n));
            }
        }

        out.println("\n\n------------------------- newly tokenized names\n\n");
        Multimap<String, String> tnameToCname = HashMultimap.create();
        Multimap<String, String> stnameToCname = HashMultimap.create();
        Map<String, String> cnameToTname = new LinkedHashMap<>();
        Map<String, String> newNameToSortedNewName = new LinkedHashMap<>();

        for (String cname: cnames.elementSet()) {
            List<String> result = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(cname, DELIMITERS);
            while (st.hasMoreElements()) {
                String token = st.nextToken();
                if (token.length() <= 1)
                    result.add(token);
                else
                    result.addAll(splitToken(token, prefixesToMatch, suffixesToMatch));
            }

            List<String> sortedResult = new ArrayList<>(result);
            Collections.sort(sortedResult);

            String tname =  Joiner.on(" ").join(result);
            String stname =  Joiner.on(" ").join(sortedResult);
            tnameToCname.put(tname, cname);
            stnameToCname.put(stname, cname);
            //cnameToTname.put(cname, tname);
            newNameToSortedNewName.put(tname, stname);
            for (Row r: cnameToRows.get(cname)) {
                r.setTname(tname);
                r.setSTname(stname);
            }

            out.println (cname + " -> " + " (" + cnames.count(cname) + " row(s))");
        }

        out.println("\n\n------------------------- newly tokenized names (same token order)\n\n");
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

        out.println("\n\n------------------------- newly tokenized names (sorted)\n\n");
        count = 0;
        for (String sorted: stnameToCname.keySet())
            if (stnameToCname.get(sorted).size() > 1)
            {
                Collection<String> cnamesSet = stnameToCname.get(sorted);
                out.println(++count + ". canonical: " + sorted);
                for (String cname: cnamesSet) {
                    /*
                    int rowCount = names.count(original);
                    String rowCountStr = "";
                    if (rowCount > 1)
                        rowCountStr = " #nrows: " + rowCount;
                    out.println (original + rowCountStr);
                    */
                    printNameDetail(cnameToRows, cname);
                }
            }
    }

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

    public static void printNameDetail(Multimap<String, Row> namesToInfo, String n) {
        List<Row> rows = new ArrayList<>(namesToInfo.get(n));
        Collections.sort(rows);
        for (Row row: rows)
            out.println ("   " + row);
    }

    public static String canonicalize(String s)
    {
        // remove successive, duplicate chars, e.g.
        //  LOOK MAN SINGH RAI
        // LOKMAN SINGH RAI
        s = s.replaceAll("TH", "T").replaceAll("GH", "G").replaceAll("BH", "B").replaceAll("DH", "D").replaceAll("JH", "J").replaceAll("KH", "K").replaceAll("MH", "M").replaceAll("PH", "P").replaceAll("SH", "S").replaceAll("ZH", "Z");
        s = s.replaceAll("AU", "OU").replaceAll("OO", "U").replaceAll("EE", "I"); // .replaceAll("YU", "U");
        char prev = ' ';
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray())
        {
            if (c != prev)
                result.append(c);
            prev = c;
        }
        if (!s.equals(result.toString()))
            out.println ("canonical: " + s + " -> " + result + "-");

        return result.toString();
    }
}
