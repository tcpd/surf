package in.edu.ashoka.surf;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import in.edu.ashoka.surf.util.Util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Program to generate ideas for canonicalization.
 * Looks up Ids that have been mapped manually and judged to be the same.
 * prints their canon versions so we can see why canon didn't map to the same string.
 */
class CanonMiner {

    private static final PrintStream out = System.out;

    public static void main (String args[]) throws IOException {
        String file = "/Users/hangal/tcpd_data/AE/Data/Gujarat/primary/candidates_electoral_info.csv";
        Dataset d = Dataset.getDataset(file);
        Multimap<String, Row> map = LinkedHashMultimap.create();
        for (Row r: d.getRows()) {
            map.put (r.get("pid"), r);
        }

        int idx = 0;
        for (String pid: map.keySet()) {
            Collection<Row> vals = map.get(pid);
            if (vals.size() <= 1)
                continue;

            Multiset<String> cands = LinkedHashMultiset.create();
            for (Row r: vals) {
                String cand = r.get("Cand");
                cand = cand.replace ("\\.", " ");
                cand = Tokenizer.canonicalizeDesi (cand);
                List<String> candWords = Util.tokenize(cand);
                Collections.sort (candWords);
                cand = String.join (" ", candWords);
                cands.add (cand);
            }

            if (cands.elementSet().size() <= 1)
                continue;

            out.println ("------ " + ++idx + ".");
            for (String cand: cands.elementSet()) {
                int count = cands.count (cand);
                out.println (cand + (count > 1 ? " x" + count : ""));
            }
        }
    }
}
