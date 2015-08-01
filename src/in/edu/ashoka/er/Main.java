
package in.edu.ashoka.er;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

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
        Multiset<String> names = HashMultiset.create();
        Multimap<String, String> namesToInfo = HashMultimap.create();
        int nRows = 0;
//        Reader in = new FileReader("GE.csv");
        // read the names from CSV
        Iterable<CSVRecord> records = CSVParser.parse(new File("GE.csv"), Charset.forName("UTF-8"), CSVFormat.EXCEL.withHeader());
        for (CSVRecord record : records) {
            String name = record.get("Name");
            if (name == null)
                continue;

            String votesStr = record.get("Votes");
            int votes = 100000;
            try { votes = Integer.parseInt(votesStr); } catch (NumberFormatException nfe) { }
//            if ("IND".equals(record.get("Party")) && votes < 1000 && !"1".equals(record.get("Position")))
 //               continue;
            nRows++;
            name = name.toUpperCase();
            names.add(name);

            namesToInfo.put(name, record.get("Sex") + "-" + record.get("Year") + "-" + record.get("State_name") + "-" + record.get("PC_name") + "-" + record.get("Party") + "-" + record.get("Position") + "-" + record.get("Votes"));
        }



        // tokenize the names
        Multiset<String> tokens = HashMultiset.create();
        int nSingleNames = 0;
        for (String name: names) {
            StringTokenizer st = new StringTokenizer(name, DELIMITERS);
            if (st.countTokens() == 1) {
                nSingleNames++;
                for (String s: namesToInfo.get(name))
                    if (s.contains("-1-")) {
                        out.println ("Wow: " + name + " won " + s);
                    }
            }

            while (st.hasMoreElements()) {
                String token = st.nextToken();
                if (token.length() > 1)
                    tokens.add(token);
            }
        }

        out.println("\n\n------------------------- " + nRows + " rows, " + names.elementSet().size() + " unique names, " + nSingleNames + " single word names\n\n");
        for (String n : Multisets.copyHighestCountFirst(names).elementSet()) {
            out.println(n + ": " + names.count(n));
            if (names.count(n) > 1) {
                List<String> infoList = new ArrayList<>(namesToInfo.get(n));
                Collections.sort(infoList);
                for (String info: infoList)
                    out.println ("   " + info);
            }
        }

        out.println("\n\n------------------------- " + tokens.size() + " tokens\n\n");
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

        out.println("\n\n------------------------- newly delimited names\n\n");
        for (String name: names.elementSet()) {
            List<String> result = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(name, DELIMITERS);
            while (st.hasMoreElements()) {
                String token = st.nextToken();
                if (token.length() <= 1)
                    result.add(token);
                else
                    result.addAll(splitToken(token, prefixesToMatch, suffixesToMatch));
            }
            out.println (name + " -> " + Joiner.on(" ").join(result) + " (" + names.count(name) + " row(s))");
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
}
