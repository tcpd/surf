package in.edu.ashoka.surf;

import com.google.common.collect.Multimap;
import edu.stanford.muse.util.UnionFindSet;
import edu.stanford.muse.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/* A class that performs loose merging on a dataset based on a primaryField, but only when the secondaryField is exactly the same. */
public class CompatibleNameManager extends MergeManager{

	boolean algorithmRun;
	private String primaryFieldName, secondaryFieldName;

	public CompatibleNameManager(Dataset d) {
		super(d);
		algorithmRun = false;
	}

	void setFields(String primaryFieldName, String secondaryFieldName) {
		this.primaryFieldName = primaryFieldName;
		this.secondaryFieldName = secondaryFieldName;
	}

    // check that each token in x maps to a token in y
	private static boolean canTokensMap (Collection<String> xTokens, Collection<String> yTokens) {
		outer:
		for (String xt: xTokens) {
			for (String yt : yTokens) {
				if (yt.startsWith(xt))
					continue outer;

				// allow yt to end with xt, but only if xt is more than 1 char!
				if (xt.length() > 2 && yt.endsWith(xt))
					continue outer;
			}
			return false;
		}
		return true;
	}

	private static int compatibility (String x, String y) {
		Set<String> xTokens = new LinkedHashSet<>(Util.tokenize(x));
		Set<String> yTokens = new LinkedHashSet<> (Util.tokenize(y));

		Set<String> multiLetterXTokens = xTokens.stream().filter (s -> s.length() > 1).collect(Collectors.toSet());
		Set<String> multiLetterYTokens = yTokens.stream().filter (s -> s.length() > 1).collect(Collectors.toSet());
		multiLetterXTokens.retainAll(multiLetterYTokens);
		if (multiLetterXTokens.size() >= 2)
			return multiLetterXTokens.size();

		if (canTokensMap(xTokens, yTokens) || canTokensMap(yTokens, xTokens))
			return 1;
		else
			return 0;
	}

	@Override
	public void addSimilarCandidates() {
		if(algorithmRun)
			return;


		// first split up all the rows into different clusters based on the secondary field.
        // this will bunch up into clusters all rows which have the exact same secondary field
		Multimap <String, Row> secondaryFieldToRows = SurfExcel.split (d.getRows(), secondaryFieldName);
        listOfSimilarCandidates = new ArrayList<>();

		// then for each cluster, do a compatibility check between primary fields of each pair of rows
        // if they are compatible, put them in a union-find data structure to indicate that that pair should be merged
		for (String secondaryField: secondaryFieldToRows.keySet()) {
            UnionFindSet<Integer> ufs = new UnionFindSet<>();
			List<Row> rows = new ArrayList<>(secondaryFieldToRows.get(secondaryField));

			for (int i = 0; i < rows.size(); i++) {
				for (int j = i+1; j < rows.size(); j++) {
					if (compatibility (rows.get(i).get(primaryFieldName), rows.get(j).get(primaryFieldName)) > 0) {
					    ufs.unify (i, j); // i and j are in the same group
					}
				}
			}

			List<List<Integer>> clusters = ufs.getClasses(); // this gives us equivalence classes of row#s that have been merged

            // now translate the row#s back to the actual rows
            for (List<Integer> cluster : clusters) {
			    if (cluster.size() < 2)
			        continue;

			    List<Row> clusterRows = new ArrayList<>();
                for (Integer I: cluster) {
                    clusterRows.add(rows.get(I));
                }
                listOfSimilarCandidates.add (clusterRows);
            }
		}

		algorithmRun = true;
	}

	public static void main (String args[]) {
        Util.ASSERT (compatibility("AJMAL SIRAJUDIN", "AJMAL SERAJ UDIN") == 0);
		Util.ASSERT (compatibility("JUGAL KISOR", "JUGAL KISOR SARMA") > 0);
		Util.ASSERT (compatibility("IERAM REDI SUBA WENKATA", "I REDI SUBA W") > 0);
		Util.ASSERT (compatibility("ADWANI LAL KRISNA", "L K ADWANI") > 0);
		Util.ASSERT (compatibility("AJMAL SIRAJUDIN", "AJMAL SIRAJ UDIN") > 0);
		Util.ASSERT (compatibility("GAJENDRA SEKAWAT SING", "GAJENDRASING SEKAWAT") > 0);
		System.out.println ("All tests successful!");
	}
}
