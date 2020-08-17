package in.edu.ashoka.surf;

import com.google.common.collect.*;
import in.edu.ashoka.surf.util.Pair;
import in.edu.ashoka.surf.util.Timers;
import in.edu.ashoka.surf.util.UnionFindSet;
import in.edu.ashoka.surf.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/* A class that performs loose merging on a dataset based on a primaryField, but only when the secondaryField is exactly the same. */
public class CompatibleNameAlgorithm extends MergeAlgorithm {

    private String primaryFieldName;
    private Filter filter;
    private final Set<String> ignoredTokens = new LinkedHashSet<>(); // these are common tokens that won't be considered for matching
	private int minTokenOverlap;
	private int ignoreTokenFrequency; /* beyond this freq. threshold in the dataset, the token will not be considered */
    private boolean substringAllowed = true, initialMapping = true;

	private CompatibleNameAlgorithm(Dataset d) {
		super(d);
	}

	CompatibleNameAlgorithm(Dataset d, String primaryFieldName, Filter filter, int minTokenOverlap, int ignoreTokenFrequency, boolean substringAllowed, boolean initialMapping) {
		super(d);
	    this.primaryFieldName = primaryFieldName;
	    this.filter = filter;
		this.minTokenOverlap = minTokenOverlap;
        this.ignoreTokenFrequency = ignoreTokenFrequency;
        this.substringAllowed = substringAllowed;
        this.initialMapping = initialMapping;

	    Map<String, Integer> map = new LinkedHashMap<>();
	    for (Row r: dataset.getRows()) {
	        String fieldVal = r.get (primaryFieldName);
	        List<String> fieldValTokens = Util.tokenize(fieldVal);
	        for (String t: fieldValTokens) {
                map.merge(t, 1, (a, b) -> a + b);
            }
        }

        log.info ("Printing token frequencies");
        List<Pair<String, Integer>> pairs = Util.sortMapByValue(map);
	    for (Pair<String, Integer>  p: pairs) {
	        if (p.getSecond() > ignoreTokenFrequency)
                ignoredTokens.add (p.getFirst());
        }
        log.info ("Done printing token frequencies");
        log.info ("Ignored tokens: " + Util.join (ignoredTokens, " "));
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

	// core compatibility function: at least (minTokenOverlap) multi-letter tokens common, or whether all tokens in one can map to the other modulo initials (if initialMapping is true)
	@SuppressWarnings("SuspiciousNameCombination")
    private int compatibility (String x, String y, int minTokenOverlap, boolean substringAllowed, boolean initialMapping) {
	    
        Multiset<String> xTokens = LinkedHashMultiset.create();
		xTokens.addAll(Util.tokenize(x));
        Multiset<String> yTokens = LinkedHashMultiset.create();
        yTokens.addAll (Util.tokenize(y));

        // remove all ignored tokens
            xTokens.removeAll(ignoredTokens);
        yTokens.removeAll(ignoredTokens);

        // if either x or y consists only of ignored tokens, it is effectively an empty token, return 0
        if (xTokens.size() == 0 || yTokens.size() == 0)
            return 0;

        {
            Multiset<String> commonTokens = LinkedHashMultiset.create();
            commonTokens.addAll (xTokens);
            Multisets.retainOccurrences (commonTokens, yTokens);
            Multisets.removeOccurrences (xTokens, commonTokens);
            Multisets.removeOccurrences (yTokens, commonTokens);
            if (commonTokens.size() >= minTokenOverlap)
                return commonTokens.size();
        }

        // x and y have the same tokens, return true
        // but if X is a subset of Y, e.g. X = MOHAN, Y = WALA BUPAT MOHAN, but the # of tokens in X is < MIN_TOKEN_OVERLAP, we want to return FALSE
        if (xTokens.size() == 0 && yTokens.size() == 0) {
            return 1;
        }
        if (substringAllowed && (xTokens.size() == 0 || yTokens.size() == 0)) {
            return 1;
        }

        // # x and y each have some tokens that are not the same. check if these tokens can map
        //noinspection SuspiciousNameCombination
        if (initialMapping && (canTokensMap(xTokens, yTokens) || canTokensMap(yTokens, xTokens)))
			return 1;
		else
			return 0;
	}


	private List<List<Row>> computeClasses (List<Row> filteredRows, int minTokenOverlap, boolean substringAllowed, boolean initialMapping) {
	    log.info ("Compute classes called. #rows = " + filteredRows.size() + " minTokenOverlap: " + minTokenOverlap + " substringAllowed: " + substringAllowed + " initialMapping: " + initialMapping);
        String field = primaryFieldName;
        List<Pair<String, String>> edges = new ArrayList<>(); // for debugging only

        // setup tokenToFieldIdx: is a map of token (of at least 3 chars) -> all indexes that contain that token
        // since compat computation is expensive, we'll only compute it for pairs that have at least 1 token in common
        // another optimization we could do is to compute the version of the field with ignoreTokens stripped out
        // this computation happens on each string many times.
        Multimap<String, Integer> tokenToFieldIdx = HashMultimap.create();
        for (int i = 0; i < filteredRows.size(); i++) {
            String fieldVal = filteredRows.get(i).get(field);
            StringTokenizer st = new StringTokenizer(fieldVal, Tokenizer.DELIMITERS);
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (tok.length() < 2 || ignoredTokens.contains(tok)) // don't create this map for the ignored tokens
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
                    Collection<Integer> c = tokenToFieldIdx.get(tok);
                    log.info ("for fieldval: " + fieldVal + " tokenToFieldIDx size for " + tok + " is " + c.size());
                    for (Integer j : c)
                        if (j > i)
                            idxsToCompareWith.add(j);
                }
            }

            log.info ("idxToCompareWith size for " + fieldVal + " is "  + idxsToCompareWith.size());
            for (int j : idxsToCompareWith) {
                String field_i = filteredRows.get(i).get(primaryFieldName);
                String field_j = filteredRows.get(j).get(primaryFieldName);

                if (field_i.equals(field_j))
                    // if fields are exactly the same, even if they only contain ignored tokens, they should be considered the same
                    // no graph edge created here
                    ufs.unify(filteredRows.get(i), filteredRows.get(j)); // i and j are in the same group
                else {
                    float compatibility = compatibility(field_i, field_j, minTokenOverlap, substringAllowed, initialMapping);
                    if (compatibility > 0) { // note: we could also compare primary field, not st field, to reduce noise
                        ufs.unify(filteredRows.get(i), filteredRows.get(j)); // i and j are in the same group
                        edges.add(new Pair<>(field_i, field_j));
                    }
                }
            }
        }

        List<List<Row>> clusters = ufs.getClassesSortedByClassSize(); // this gives us equivalence classes of row#s that have been merged
        log.info ("Compute classes generated " + clusters.size() + " clusters. -- params: #rows = " + filteredRows.size() + " minTokenOverlap: " + minTokenOverlap + " substringAllowed: " + substringAllowed + " initialMapping: " + initialMapping);

        /*
        if (clusters.size() > 0) {
            List<Row> largestCluster = clusters.get(0);
            // get the biggest cluster
            Set<String> stringsInLargestCluster = largestCluster.stream().map (r -> r.get(Config.MERGE_FIELD)).collect(Collectors.toSet());
            // from the list of all edges, keep only the edges that are in this cluster
            edges = edges.stream().filter (p -> stringsInLargestCluster.contains(p.getFirst()) && stringsInLargestCluster.contains (p.getSecond())).collect(Collectors.toList());
            log.info ("In the largest cluster, #nodes is " + stringsInLargestCluster.size() + " #edges is " + edges.size());
            writeEdgesToFile_sigmaJS(stringsInLargestCluster, edges, "/tmp/data-" + minTokenOverlap + ".json");
        }
        */
        return clusters;
    }

    /* write out in sigma.js format */
    private void writeEdgesToFile_sigmaJS(Set<String> nodeNames, List<Pair<String, String>> edges, String filename) throws FileNotFoundException {
        JSONObject json = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();

        Map<String, Integer> map = new LinkedHashMap<>();

        int idx = 0;
        for (String node: nodeNames) {
            JSONObject obj = new JSONObject();
            obj.put ("label", node);
            obj.put ("group", 1);
            obj.put ("id", idx);
            map.put (node, idx);
            nodes.put (idx++, obj);
        }

        idx = 0;
        for (Pair<String, String> p: edges) {
            JSONObject obj = new JSONObject();
            obj.put ("source", map.get(p.getFirst()));
            obj.put ("target", map.get(p.getSecond()));
            obj.put ("id", idx);
            links.put (idx++, obj);
        }

        json.put ("nodes", nodes);
        json.put ("edges", links);

        String str = json.toString(4);
        PrintWriter fos = new PrintWriter(new FileOutputStream(filename));
        fos.println (str);
        fos.close();
    }

    private void writeEdgesToFile_d3(Set<String> nodeNames, List<Pair<String, String>> edges, String filename) throws FileNotFoundException {
        JSONObject json = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();

        int idx = 0;
        for (String node: nodeNames) {
            JSONObject obj = new JSONObject();
            obj.put ("label", node);
            obj.put ("id", idx);
            nodes.put (idx++, obj);
        }

        idx = 0;
        for (Pair<String, String> p: edges) {
            JSONObject obj = new JSONObject();
            obj.put ("source", p.getFirst());
            obj.put ("target", p.getSecond());
            obj.put ("value", 1);
            links.put (idx++, obj);
        }

        json.put ("nodes", nodes);
        json.put ("edges", links);

        String str = json.toString();
        PrintWriter fos = new PrintWriter(new FileOutputStream(filename));
        fos.println (str);
        fos.close();
    }


    // returns how many unique id's in this cluster
    private int idsInCluster(Collection<Row> rows) {
	    return rows.stream().map (row -> row.get(Config.ID_FIELD)).collect(Collectors.toSet()).size();
    }

    private void runRecursive(List<Collection<Row>> result, List<Row> rows, int tokenOverlap, boolean substringAllowed, boolean initialMapping) {

        String params = " (params: #rows: "+ rows.size() + "tokenOverlap: " + tokenOverlap + " substringAllowed: " + substringAllowed + " initialMapping: " + initialMapping + ")";

        List<List<Row>> clusts = computeClasses (rows, tokenOverlap, substringAllowed, initialMapping);
        long nRowsInAllClusters = clusts.stream().flatMap(List::stream).count();
        log.info ("runRecursive clustered " + rows.size() + " rows into " + clusts.size() + " clusters with " + nRowsInAllClusters + " rows in these clusters" + params);

        for (List<Row> thisCluster : clusts) {
            int nIdsInThisCluster = idsInCluster(thisCluster);
            /*
            if (nIdsInThisCluster > 20) {
                log.info ("Need to break down a large cluster with " + nIdsInThisCluster + " ids and " + thisCluster.size() + " rows " + params);

                // too big cluster... first try initiaMapping off if it was on, since it can be quite aggressive
                if (initialMapping) {
                    log.info ("Trying without initial mapping");
                    runRecursive(result, thisCluster, tokenOverlap, substringAllowed, false); // if we still haven't been able to break down the cluster with token overlap = 10, we'll instead try doing it without initials
                } else {
                    // try substring allowed if it was on
                    log.info ("Trying without substring allowed");
                    if (substringAllowed)
                        runRecursive(result, thisCluster, tokenOverlap, false, initialMapping); // if we still haven't been able to break down the cluster with token overlap = 10, we'll instead try doing it without initials
                    else {
                        log.info ("Warning: adding a large cluster with size: " + thisCluster.size());
                        thisCluster.stream().map(r -> r.get(Config.MERGE_FIELD)).forEach(System.out::println);
                        result.add(thisCluster);
                    }
                }
            } else */ {
                // normal sized cluster, no problem
                result.add(thisCluster);
            }
            // if # ids in custer > 20, try with increased size
        }
    }

    @Override
	public List<Collection<Row>> run() throws FileNotFoundException {

        List<Row> filteredRows = filter.isEmpty() ? (List<Row>) new ArrayList<>(dataset.getRows()) : dataset.getRows().stream().filter(filter::passes).collect(toList());

        Timers.CompatibleNameTimer.reset();
        Timers.CompatibleNameTimer.start();
		// now translate the row#s back to the actual rows
        classes = new ArrayList<>();
        runRecursive (classes, filteredRows, minTokenOverlap, substringAllowed, initialMapping);
        
        Timers.CompatibleNameTimer.stop();

        Timers.log.info ("Time for Compatible Name computation: " + Timers.CompatibleNameTimer.toString());

		return classes;
	}

	public String toString() {
	    return "Compatible names algorithm with min. token overlap: " + minTokenOverlap + " ignore token frequency: " + ignoreTokenFrequency + ", " + ignoredTokens.size() + " ignored tokens (" + Util.join (ignoredTokens, " ") + ")";
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
		CompatibleNameAlgorithm alg = new CompatibleNameAlgorithm(null);
		System.out.println (alg.compatibility("BISAN DUT LAKAN PAL", "BISAN DUT", 2, true, true));

        System.out.println (alg.compatibility("BABAR CH OUHAN PARSOTAM", "AN IMTIIAS KAN KAN PAT SAED", 2, true, true));
	}
}
