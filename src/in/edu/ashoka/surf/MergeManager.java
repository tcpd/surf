package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.*;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import edu.stanford.muse.util.Util;
import in.edu.ashoka.surf.util.Timers;
import in.edu.ashoka.surf.util.UnionFindSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** class that takes care of running a merging algorithm and then manual merges to it.
 * Important: these functions should not depend on a web-based frontend. This class should have no dependency on servlets. */
public class MergeManager {
    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.MergeManager.class);

    /** a view is a filtered and sorted view on top of this mergemanager's results */
    public class View {
        public final List<List<List<Row>>> viewGroups; // these are the groups
        String filterSpec, sortOrder;
        public View (String filterSpec, String sortOrder, List<List<List<Row>>> viewGroups) {
            this.filterSpec = filterSpec;
            this.sortOrder = sortOrder;
            this.viewGroups = viewGroups;
        }
    }

	private Dataset d;
    private List<Collection<Row>> listOfSimilarCandidates; // these are the groups
    private Multimap<String, Row> idToRows = LinkedHashMultimap.create();
    private MergeAlgorithm algorithm;
    static View lastView; // currently we assume only 1 view

    // a small class to represent operations made on this dataset.
    // op is the operation to run: merge, or unmerge
    // the merge or unmerge is run on the given ids.
    // groupId is currently not needed.
    public static class MergeCommand {
        String op;
        String groupId; // not needed and not used currently
        String[] ids;
    }

    /** create a new mergeManager with the given algorithm and arguments, and runs the algorithm and stores the (initial) groups */
    public MergeManager(Dataset dataset, String algo, String arguments) {
        this.d = dataset;
        if (!d.hasColumnName("__comments"))
            d.addToActualColumnName("__comments");
        if (!d.hasColumnName("__reviewed"))
            d.addToActualColumnName("__reviewed");
        computeIdToRows(d.getRows());

        Tokenizer.setupDesiVersions(dataset.getRows(), Config.MERGE_FIELD);

        if (algo.equals("editDistance")) {
            int editDistance = 1;
            try {
                editDistance = Integer.parseInt(arguments);
            } catch (NumberFormatException e) {
                Util.print_exception(e, log);
            }
            algorithm = new EditDistanceMergeAlgorithm(d, "_st_" + Config.MERGE_FIELD, editDistance); // run e.d. on the _st_ version of the field
        } else if (algo.equals("dummyAllName")) {
            algorithm = new MergeAlgorithm(dataset) {
                @Override
                public List<Collection<Row>> run() {
                    classes = new ArrayList<>();
                    classes.add(new ArrayList<>(d.getRows())); // just one class, with all the rows in it
                    return classes;
                }
            };
        } else if (algo.equals("compatibleNames")) {
            CompatibleNameAlgorithm cnm = new CompatibleNameAlgorithm(d, Config.MERGE_FIELD);
            algorithm = cnm;
        }

        // this is where the groups are generated
        listOfSimilarCandidates = algorithm.run();
    }

    /** will split listOfSimilarCandidates into new groups based on the split column */
    public void splitByColumn (String splitColumn) {
        // further split by split column if specified
        if (Util.nullOrEmpty(splitColumn))
            return;

        List<Collection<Row>> result = new ArrayList<>();
        List<Collection<Row>> groupsList = this.listOfSimilarCandidates;
        for (Collection<Row> group : groupsList) {
            Multimap<String, Row> splitGroups = SurfExcel.split(group, splitColumn);
            for (String key : splitGroups.keySet())
                result.add(splitGroups.get(key));
        }
        this.listOfSimilarCandidates = result;
    }

    /** updates listOfSimilarCandidates based on id's as well as existing listOfSimilarCandidates.
     * MUST be called after id's are changed */
    public void updateMergesBasedOnIds() {
        Timers.unionFindTimer.reset();
        Timers.unionFindTimer.start();

        int initialClusters = listOfSimilarCandidates.size();
        UnionFindSet<Row> ufs = new UnionFindSet<>();

        // do unification, the criteria are based on one of 2 factors
        // same id, or same cluster according to listOfSimilarCandidates

        for (String id: idToRows.keySet()) {
            ufs.unifyAllElementsOfCollection (idToRows.get(id));
        }

        for (Collection<Row> cluster : listOfSimilarCandidates) {
            ufs.unifyAllElementsOfCollection(cluster);
        }

        Timers.unionFindTimer.stop();
        Timers.log.info ("Time for union-find: " + Timers.unionFindTimer.toString());
        listOfSimilarCandidates = (List) ufs.getClassesSortedByClassSize();
        log.info ("initial # of groups " + initialClusters + ", after merging by id, we have " + listOfSimilarCandidates.size() + " groups");
    }

    private void computeIdToRows (Collection<Row> rows) {
        for (Row r: rows)
            idToRows.put (r.get(Config.ID_FIELD), r);
    }

    /** applies the given merge/unmerge commands on the dataset */
    public void applyUpdatesAndSave(MergeCommand[] commands) throws IOException {
        log.info ("Applying " + commands.length + " command(s) to " + d);

        for (MergeCommand command: commands) {
            if ("merge".equalsIgnoreCase(command.op)) {
                // we have to merge all the ids in command.ids
                // the id of the first will be put into firstId, and copied to all the other rows with that id
                String firstId = null;
                for (String id : command.ids) {
                    if (firstId == null) {
                        firstId = id;
                        continue;
                    }

                    // update all the rows for this id to firstId
                    // also remember to update the idToRows map
                    log.info("Merging id " + id + " into " + firstId);
                    Collection<Row> rowsForThisId = idToRows.get(id);
                    for (Row row : rowsForThisId) {
                        row.set(Config.ID_FIELD, firstId); // we wipe out the old id for this row
                        idToRows.get(firstId).add (row);
                    }
                    idToRows.removeAll (id); // remove this id entirely from the map, it will not be used again
                }
                updateMergesBasedOnIds();
            } else if ("unmerge".equalsIgnoreCase(command.op)) {
                // create unique id's for all rows
                for (String id : command.ids) {
                    Collection<Row> rowsForThisId = idToRows.get(id);
                    for (Row row : rowsForThisId) {
                        // break the cluster, give each of these rows a new id, and put them in a new cluster in listOfSimilarCandidates
                    }
                }
            }
        }

        d.save();
    }

    public View getView (String filterSpec, String sortOrder) {
        List<List<List<Row>>> postFilterGroups = applyFilter(new Filter(filterSpec));
        Comparator<List<List<Row>>> comparator = GroupOrdering.getComparator (sortOrder);
        Collections.sort (postFilterGroups, comparator);
        View view = new View(filterSpec, sortOrder, postFilterGroups);
        MergeManager.lastView = view;
        return view;
    }

    /** returns a collection of groups after filtering listOfSimilarCandidates by the given filter.
     * under each group is a bunch of ids. under each id has a bunch of rows.
	 * that's why it returns a List of List of List of Rows.
	 * it is assumed that:
	 * one row is only in one group, i.e. it can't belong to multiple groups.
	 * and all rows belonging to an id are also in the same group.
	 * (this should be satisfied if the inputGroupsList has been generated by a Union-Find set. */
	public List<List<List<Row>>> applyFilter (Filter filter) {

		List<List<List<Row>>> result = new ArrayList<>();

		for (Collection<Row> rowCollection: listOfSimilarCandidates) {
			List<Row> group = new ArrayList<>(rowCollection); // convert collection to list

			// will this group be shown? yes, if ANY of the rows in this group matches the filter. otherwise move on to the next group.
			{
				long nRowsPassingFilter = group.stream().filter(r -> filter.passes(r)).limit(1).count(); // limit(1) to ensure early out at finding the first row passing the filter
				if (nRowsPassingFilter == 0)
					continue;
			}

			// ok, this group is to be shown. sort the rows in this group data based on sortColumns
			{
				String[] sortColumns = new String[]{"PC_name", "Year"}; // cols according to which we'll sort rows -- string vals only, integers won't work!

				Comparator c = new Comparator<Row>() {
					@Override
					public int compare(Row r1, Row r2) {
						for (String col : sortColumns) {
							int result = r1.get(col).toLowerCase().compareTo(r2.get(col).toLowerCase());
							if (result != 0)
								return result;
						}
						return 0;
					}
				};
				group.sort(c);
			}

			// generate a id -> RowsInThisGroup map
			Multimap<String, Row> idToRowsInThisGroup = LinkedHashMultimap.create();
			group.stream().forEach(r -> {
				idToRowsInThisGroup.put(r.get(Config.ID_FIELD), r);
			});

			// prepare list of list of rows for this group
			List<List<Row>> rowsForThisGroup = new ArrayList<>();
			for (String id: idToRowsInThisGroup.keySet()) {
				List<Row> rowsForThisId = new ArrayList<>(idToRowsInThisGroup.get(id));
				rowsForThisGroup.add (rowsForThisId);
			}
			result.add (rowsForThisGroup);
		}

		return result;
	}
}
