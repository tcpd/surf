package in.edu.ashoka.surf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import in.edu.ashoka.surf.util.Pair;
import in.edu.ashoka.surf.util.Util;

class Display {
    private static final PrintStream out = System.out;

    private static void displayCollection(Collection<String> c, Collection<String> reference) {
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
