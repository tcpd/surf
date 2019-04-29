package in.edu.ashoka.surf;

import java.util.*;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import com.google.common.collect.Multisets;
import in.edu.ashoka.surf.util.Timers;
import in.edu.ashoka.surf.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Tokenizer {
    private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.Tokenizer.class);

    static final String DELIMITERS = " -.,;:/'\\t<>\"`()@1234567890";

    private static final Map<Pattern, String> patternToReplacement = new LinkedHashMap<>(); // VERY IMP: should be linked hashmap so the replacements are applied in order!
    static {
        // precompile patterns for performance. the patterns to be replaced
        for (int i = 0; i < Config.replacements.length; i += 2) {
            Pattern p = Pattern.compile(Config.replacements[i]);
            patternToReplacement.put (p, Config.replacements[i+1]);
        }
    }

    /** generates weighted token counts for tokens in each key. the count of a key (token) in the multiset is its weight. */
    private static Multiset<String> generateTokenCounts(Multimap<String, Row> map) {
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
        log.info ("single word keys: " + nSingleWordKeys + " total tokens: " + tokens.size() + " unique: " + tokens.elementSet().size());

        int i = 0;
        for (String s: Multisets.copyHighestCountFirst(tokens).elementSet())
            log.info (++i + " " + s + " " + tokens.count(s));

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

        int bestSplit = Config.DEFAULT_MIN_SPLITWEIGHT; // min. splitweight
        String bestFirst = "", bestSecond = "";
        for (int i = 2; i <= s.length()-2; i++) {
            String first = s.substring(0, i);
            String second = s.substring(i);

            // first and second must be both at least 2 chars long

            {
                // we can break s into first and second, only if first and second have been seen at least once in the dataset
                // this means RAMLAL can break into RAM and LAL if RAM and LAL are words in the dataset
                // RAMJI can break into RAM and JI if RAM and JI ...
                // but RAMESH will not break into RAM and ESH because "ESH" may not be in the dataset. this is what we want.
                // further if we are breaking up in a way that we are introducing new tokens of length, we expect that 2 letter word to have a freq. of at least 5.
                // and similarly for 3 letter words to have a freq. of at least 2.
                // for 4 letter words, can have freq. of just 1.
                boolean skip = false;
                if (validTokens.count(first) == 0 || validTokens.count(second) == 0) {
                    skip = true;
                }

                final int MIN_FREQ_FOR_TOKENS_OF_LEN_2 = 5;
                final int MIN_FREQ_FOR_TOKENS_OF_LEN_3 = 2;
                // = 1 for the others

                if ((first.length() <= 2 && validTokens.count(first) < MIN_FREQ_FOR_TOKENS_OF_LEN_2) ||
                    (second.length() <= 2 && validTokens.count(second) < MIN_FREQ_FOR_TOKENS_OF_LEN_2)) {
                    skip = true;
                }

                if ((first.length() <= 3 && validTokens.count(first) < MIN_FREQ_FOR_TOKENS_OF_LEN_3) ||
                    (second.length() <= 3 && validTokens.count(first) < MIN_FREQ_FOR_TOKENS_OF_LEN_3)) {
                    skip = true;
                }

                if (skip)
                    continue;
            }

            int splitWeight = validTokens.count(first) + validTokens.count(second);

            // we penalize splits that result in tokens of length just 2
            // this is to prevent DUTA breaking up into DU TA
            if (i == 2)
                splitWeight /= 2;
            if (i == s.length()-3)
                splitWeight /= 2;

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
     * e.g if field is Name, fields called _c_Name, _t_Name and _st_Name are added to all rows.
     * We have to be careful about tokenization. It can have a disproportionate effect.
     * BIREN DUTA => BIREN DUTA(canonicalization) => BI REN DU TA (retokenized) => BI REN DU TA (sorted) => BIDURENTA
     * BIREN DATA => BIREN DATA => BIREN DATA => BIDATAREN
     * These strings after sorting are very far away after tokenization and sorting.
     * */
    static void setupDesiVersions(Collection<Row> allRows, String field)
    {
        String cfield = "_c_" + field;
        String tfield = "_t_" + field;
        String stfield = "_st_" + field;

        // compute and set cfield for all rows
        Timers.canonTimer.reset();
        Timers.canonTimer.start();
        {
            for (Row r : allRows) {
                String val = r.get(field);
                val = val.toUpperCase();
                String cval = canonicalizeDesi(val);
                if (log.isDebugEnabled())
                    log.debug ("canonicalization result, val: " + val + " cval: " + cval);
                r.set(cfield, cval);
            }
        }
        Timers.canonTimer.stop();
        Timers.log.info ("Time for canonicalization: " + Timers.canonTimer.toString());

        // split on cfield and get token frequencies
        Timers.tokenizationTimer.reset();
        Timers.tokenizationTimer.start();
        {
            Multimap<String, Row> cfieldValToRows = SurfExcel.split(allRows, cfield);
            Multiset<String> tokens = generateTokenCounts(cfieldValToRows);

            for (String val : cfieldValToRows.keySet()) {
                // compute and set retokenized val
                List<String> result = retokenize(val, tokens);
                String tval = Joiner.on(" ").join(result);
                if (log.isDebugEnabled()) {
                    log.debug ("Retokenization result val:" + val + " retokenized: " + tval);
                }
                // log.info ("Retokenization result val:" + val + " retokenized: " + tval);

                // compute and set sorted-retokenized val
                List<String> sortedResult = new ArrayList<>(result);
                Collections.sort(sortedResult);
                String stval = Joiner.on(" ").join(sortedResult);

                for (Row r : cfieldValToRows.get(val)) {
                    r.set(tfield, tval);
                    r.set(stfield, stval);
                }
            }
        }
        Timers.tokenizationTimer.stop();
        Timers.log.info ("Time for Tokenization: " + Timers.tokenizationTimer.toString());
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

        List<String> tokens = Util.tokenize(s, " \t."); // very important to always tokenize on periods. M.F. SOLANKI should become M F SOLANKI, not MF SOLANKI
        for (int i=0; i<tokens.size(); i++) {
            String token = tokens.get(i);

            // special case hack: according to Gilles, KU in the middle of a name is KUMAR, but at the beginning its likely to be KUNWAR
            if (i == 0)
                token = token.replaceAll("^KU$", "KUNWAR");
            else
                token = token.replaceAll("^KU$", "KUMAR");

            // ignore titles
            if (Config.ignoreTokensSet.contains(token))
                continue;

            for (Pattern p: patternToReplacement.keySet())
                token = p.matcher(token).replaceAll(patternToReplacement.get(p));

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
        System.out.println (canonicalizeDesi("BRAHMKUMAR RANCHHODLAL BHATT"));
        System.out.println (canonicalizeDesi("PATEL CHELABHAI PRABHUDAS"));

        System.out.println (canonicalizeDesi("M.F. SOLANKI"));


    }
}
