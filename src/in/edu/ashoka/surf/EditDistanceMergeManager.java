package in.edu.ashoka.surf;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import edu.tsinghua.dbgroup.EditDistanceClusterer;
import in.edu.ashoka.surf.util.UnionFindSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hangal on 8/12/17.
 * New simplified edit distance merge manager.
 */
public class EditDistanceMergeManager extends MergeManager {

    int editDistance;
    UnionFindSet<Row> ufs = new UnionFindSet<>();
    String fieldName; // fieldname on which to compute edit distance

    /* set up merge algorithm parameters: d, the fieldName (col. name) of the field on which edit distance clustering is to be done, max. editDistance (inclusive) */
    protected EditDistanceMergeManager(Dataset dataset, String fieldName, int editDistance) {
        super (dataset);
        Collection<Row> allRows = dataset.getRows();

        // set up desi versions of the given field. we'll perform edit distance computation on this version of the given field, not the original one.
        Tokenizer.setupDesiVersions (allRows, fieldName);
        this.fieldName = "_st_" + fieldName; // this is hardcoded for now
        this.editDistance = editDistance;
    }

    @Override
    public void addSimilarCandidates() {

        Collection<Row> allRows = d.getRows();

        // create map of fieldValueToRows and idToRows
        SetMultimap<String, Row> fieldValueToRows = HashMultimap.create();
        allRows.stream().forEach (r -> { fieldValueToRows.put (r.get(fieldName), r);});

        SetMultimap<String, Row> idToRows = HashMultimap.create();
        allRows.stream().forEach (r -> { idToRows.put (r.get(Config.ID_FIELD), r);});

        // do the clustering based on ed (but only if ed > 0)
        final EditDistanceClusterer edc = (editDistance > 0) ? new EditDistanceClusterer(editDistance): null;
        if (editDistance > 0) {
            allRows.stream().forEach(r -> edc.populate(r.get(fieldName)));
        }

        // do unification, the criteria are based on one of 3 factors (on the desi version of the name):
        // exact same name, (they already have the same id), or same cluster in edit distance clusterer
        for (String stName: fieldValueToRows.keySet()) {
            ufs.unifyAllElementsOfCollection (fieldValueToRows.get(stName));
        }

        for (String id: idToRows.keySet()) {
            ufs.unifyAllElementsOfCollection (idToRows.get(id));
        }

        if (editDistance > 0) {
            List<Set<String>> clusters = (List) edc.getClusters();

            for (Set<String> cluster : clusters) {
                // cluster just has strongs, convert each string in the cluster to a representative row -- which is just the first row with that stName, obtained with iterator.next()
                Collection<Row> clusterRows = cluster.stream().map(s -> fieldValueToRows.get(s).iterator().next()).collect(Collectors.toList());
                ufs.unifyAllElementsOfCollection(clusterRows);
            }
        }

        // now read off the equivalence classes, this gives us our candidates
        listOfSimilarCandidates = (List) ufs.getClassesSortedByClassSize();
    }
}