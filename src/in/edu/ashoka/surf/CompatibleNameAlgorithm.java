package in.edu.ashoka.surf;

import com.google.common.collect.*;
import edu.stanford.muse.util.Pair;
import in.edu.ashoka.surf.util.UnionFindSet;
import edu.stanford.muse.util.Util;

import java.util.*;
import java.util.stream.Collectors;

/* A class that performs loose merging on a dataset based on a primaryField, but only when the secondaryField is exactly the same. */
public class CompatibleNameAlgorithm extends MergeAlgorithm {

    private String primaryFieldName;
    private Filter filter;
    private Set<String> ignoredTokens = new LinkedHashSet<>(); // these are common tokens that won't be considered for matching

    private static final int MIN_TOKEN_OVERLAP = 3;
    private static final int IGNORE_TOKEN_THRESHOLD = 200;

	public CompatibleNameAlgorithm(Dataset d, String primaryFieldName, Filter filter) {
		super(d);
	    this.primaryFieldName = primaryFieldName;
	    this.filter = filter;

	    Map<String, Integer> map = new LinkedHashMap<>();
	    for (Row r: dataset.getRows()) {
	        String fieldVal = r.get (primaryFieldName);
	        List<String> fieldValTokens = Util.tokenize(fieldVal);
	        for (String t: fieldValTokens) {
	            Integer I = map.get(t);
	            if (I == null)
	                map.put (t, 1);
	            else
	                map.put (t, I+1);
            }
        }

        log.info ("Printing token frequencies");
        List<Pair<String, Integer>> pairs = Util.sortMapByValue(map);
	    for (Pair<String, Integer>  p: pairs) {
	        if (p.getSecond() > IGNORE_TOKEN_THRESHOLD)
                ignoredTokens.add (p.getFirst());
        }
        log.info ("Done printing token frequencies");
    }

    // check that each token in x maps to a token in y.
    // x and y should already have their common tokens removed.
    // if x is empty, return value is true.
    // Note: xTokens and yTokens will NOT be modified!!
	private static boolean canTokensMap (Multiset<String> xTokens, Multiset<String> yTokens) {
        if (xTokens.size() == 0)
            return true;

        // create a copy of ytokens, because we're not allowed to modify the input
        Multiset<String> tmp = LinkedHashMultiset.create();
        tmp.addAll (yTokens);
        yTokens = tmp;

        // check if x tokens is initials only
        boolean initialsOnly = xTokens.stream().filter(xt -> xt.length() > 1).limit(1).count() == 0;

        if (initialsOnly) {
            outer:
            for (String xt : xTokens) {
                for (String yt : yTokens) {
                    if (yt.startsWith(xt)) {
                        // we don't want to allow SUNITABEN JASHUBHAI PATEL ~ J. J. PATEL
                        // so remove JASHUBHAI after it maps to the first J
                        yTokens.remove(yt);
                        continue outer;
                    }
                }
                return false;
            }
            return true;
        } else {
            // note: in this case, the same ytoken is allowed to match multiple xtokens.
            // this can be useful e.g. when we want y:OMPRAKASH to match both x's OM and PRAKASH
            // but it can also cause false matches
            outer:
            for (String xt : xTokens) {
                for (String yt : yTokens) {
                    if (yt.startsWith(xt))
                        continue outer; // xt has been matched

                    // allow yt to end with xt, but only if xt is more than 1 char!
                    if (xt.length() >= 2 && yt.endsWith(xt))
                        continue outer;
                }
                return false;
            }
            return true;
        }
    }

	// core compatibility function: at least 2 multi-letter tokens common, or whether all tokens in one can map to the other modulo initials
	private int compatibility (String x, String y) {
		Multiset<String> xTokens = LinkedHashMultiset.create();
		xTokens.addAll(Util.tokenize(x));
        Multiset<String> yTokens = LinkedHashMultiset.create();
        yTokens.addAll (Util.tokenize(y));

        {
            Multiset<String> commonTokens = LinkedHashMultiset.create();
            commonTokens.addAll (xTokens);
            Multisets.retainOccurrences (commonTokens, yTokens);
            Multisets.removeOccurrences (xTokens, commonTokens);
            Multisets.removeOccurrences (yTokens, commonTokens);
            // if # commonTokens (barring ignoredTokens) is > MIN_TOKEN_OVERLAP then we can return straightaway
            commonTokens.removeAll (ignoredTokens);
            if (commonTokens.size() >= MIN_TOKEN_OVERLAP)
                return commonTokens.size();
        }

        // x and y have the same tokens, return true
        // but if X is a subset of Y, e.g. X = MOHAN, Y = WALA BUPAT MOHAN, but the # of tokens in X is < MIN_TOKEN_OVERLAP, we want to return FALSE
        if (xTokens.size() == 0 && yTokens.size() == 0) {
            return 1;
        }
        if (xTokens.size() == 0 || yTokens.size() == 0) {
            return 0;
        }


        // # x and y each have some tokens that are not the same. check if these tokens can map
        if (canTokensMap(xTokens, yTokens) || canTokensMap(yTokens, xTokens))
			return 1;
		else
			return 0;
	}

	@Override
	public List<Collection<Row>> run() {
		String field = primaryFieldName;

        List<Row> filteredRows = dataset.getRows().stream().filter(filter::passes).collect(Collectors.toList());

        // setup tokenToFieldIdx: is a map of token (of at least 3 chars) -> all indexes in stnames that contain that token
		// since editDistance computation is expensive, we'll only compute it for pairs that have at least 1 token in common
		Multimap<String, Integer> tokenToFieldIdx = HashMultimap.create();
		for (int i = 0; i < filteredRows.size(); i++) {
			String fieldVal = filteredRows.get(i).get(field);
			StringTokenizer st = new StringTokenizer(fieldVal, Tokenizer.DELIMITERS);
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				if (tok.length() < 3)
					continue;
				tokenToFieldIdx.put(tok, i);
			}
		}

		UnionFindSet<Row> ufs = new UnionFindSet<>();
		for (int i = 0; i < filteredRows.size(); i++) {
            String fieldVal = filteredRows.get(i).get(field);

            // collect the indexes of the values that we should compare stField with, based on common tokens
			Set<Integer> idxsToCompareWith = new LinkedHashSet<>();
			{
				StringTokenizer st = new StringTokenizer(fieldVal, Tokenizer.DELIMITERS);
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

			for (int j : idxsToCompareWith) {
			    String field_i = filteredRows.get(i).get(primaryFieldName);
                String field_j = filteredRows.get(j).get(primaryFieldName);

                float compatibility = compatibility (field_i, field_j);
				if (compatibility > 0) { // note: we could also compare primary field, not st field, to reduce noise
					ufs.unify (filteredRows.get(i), filteredRows.get(j)); // i and j are in the same group
					log.info ("merging " + field_i + " with " + field_j + " with confidence " + compatibility);
				}
			}
		}

		List<List<Row>> clusters = ufs.getClassesSortedByClassSize(); // this gives us equivalence classes of row#s that have been merged

		// now translate the row#s back to the actual rows
        classes = new ArrayList<>();
		for (List<Row> cluster : clusters) {
			classes.add (cluster);
		}
		return classes;
	}

	public static void main (String args[]) {
	    /*
	    CompatibleNameAlgorithm alg = new CompatibleNameAlgorithm();
        Util.ASSERT (compatibility("PATEL K K", "PATEL ISHVARBHAI KANJIBHAI") == 0); // unfortunately because of using sets, this mapping is true
        Util.ASSERT (compatibility("J V THAKKAR", "THAKKAR JAYENDRABHAI") == 0);
        Util.ASSERT (compatibility("AJMAL SIRAJUDIN", "AJMAL SERAJ UDIN") == 0);
		Util.ASSERT (compatibility("JUGAL KISOR", "JUGAL KISOR SARMA") > 0);
		Util.ASSERT (compatibility("IERAM REDI SUBA WENKATA", "I REDI SUBA W") > 0);
		Util.ASSERT (compatibility("ADWANI LAL KRISNA", "L K ADWANI") > 0);
		Util.ASSERT (compatibility("AJMAL SIRAJUDIN", "AJMAL SIRAJ UDIN") > 0);
		Util.ASSERT (compatibility("GAJENDRA SEKAWAT SING", "GAJENDRASING SEKAWAT") > 0);
		Util.ASSERT (compatibility("L K ADVANI", "LAL KRISHNA ADVANI") > 0);
        System.out.println ("All tests successful!");
        */
	}
}
