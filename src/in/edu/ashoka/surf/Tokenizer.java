package in.edu.ashoka.surf;

import java.util.*;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import edu.stanford.muse.util.Util;

public class Tokenizer {
    static String DELIMITERS = " -.,;:/'\\t<>\"`()@1234567890";

    private static List<Pattern> replacementPatterns;
    static {
        // precompile patterns for performance. the patterns to be replaced
        // // replacements is an array 2X the size of replacementPatterns.
        replacementPatterns = new ArrayList<>();
        for (int i = 0; i < Config.replacements.length; i += 2) {
            replacementPatterns.add(Pattern.compile(Config.replacements[i]));
        }
    }

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

    /** canonicalizes Indian variations of spellings and replaces a run of repeated letters by a single letter */
    public static String canonicalizeDesi(String s)
    {

        if (Config.removeSuccessiveSameCharacters) {
            char prev = ' ';

            // remove successive, duplicate chars, e.g.
            //  LOOK MAN SINGH RAI
            // LOKMAN SINGH RAI
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                if (c != prev)
                    sb.append(c);
                prev = c;
            }
            s = sb.toString();
        }

//        if (!s.equals(result.toString()))
//           out.println ("canonical: " + s + " -> " + result);

        StringBuilder result = new StringBuilder();

        // these are from Gilles
        List<String> tokens = Util.tokenize(s);
        for (int i=0; i<tokens.size(); i++) {
            String token = tokens.get(i);

            // special case hack: according to Gilles, KU in the middle of a name is KUMAR, but at the beginning its likely to be KUNWAR
            if (i == 0)
                token = token.replaceAll("^KU$", "KUNWAR");
            else
                token = token.replaceAll("^KU$", "KUMAR");

            for (int j = 0; j < replacementPatterns.size(); j++) {
                token = replacementPatterns.get(j).matcher(token).replaceAll(Config.replacements[2*j+1]);
            }

            // ignore titles
            if (Config.ignoreTokensSet.contains(token))
                continue;

            token = token.replaceAll(" ", "");
            if (token.length() < 1)
                continue;

            result.append(token);
            result.append(" ");
        }
        return result.toString().trim();
    }

    public static Map<String, String> canonicalizeDesi (Collection<String> list) {
        if (list == null)
            return null;

        Map<String, String> result = new LinkedHashMap<>();
        for (String s: list)
            result.put(s, canonicalizeDesi(s));

        return result;
    }


    public static void main (String args[]) {
        System.out.println (canonicalizeDesi("PATEL CHELABHAI PRABHUDAS"));



    }
}
