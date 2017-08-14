package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import edu.stanford.muse.util.Util;
import in.edu.ashoka.surf.util.Timers;
import in.edu.ashoka.surf.util.UnionFindSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** class that takes care of merging. calls a MergeAlgorithm to generate initial clusters, then tracks manual merges by a user. */
public class MergeManager {

    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.MergeManager.class);
	Dataset d;
    public List<Collection<Row>> listOfSimilarCandidates; // these are the groups
    private Multimap<String, Row> idToRows = LinkedHashMultimap.create();
    private MergeAlgorithm algorithm;

    // a small class to represent operations made on this dataset.
    // op is the operation to run: merge, or unmerge
    // the merge or unmerge is run on the given ids.
    // groupId is currently not needed.
    public static class MergeCommand {
        String op;
        String groupId; // not needed and not used currently
        String[] ids;
    }

    /** create a new mergeManager with the given algorithm and arguments */
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
            } catch (Exception e) {
                Util.print_exception(e, log);
            }
            algorithm = new EditDistanceMergeAlgorithm(d, "_st_" + Config.MERGE_FIELD, editDistance); // run e.d. on the _st_ version of the field
        } else if (algo.equals("dummyAllName")) {
            algorithm = new MergeAlgorithm(dataset) {
                @Override
                public void run() {
                    classes = new ArrayList<>();
                    classes.add(new ArrayList<>(d.getRows())); // just one class, with all the rows in it
                }
            };
        } else if (algo.equals("compatibleNames")) {
            CompatibleNameAlgorithm cnm = new CompatibleNameAlgorithm(d, Config.MERGE_FIELD);
            algorithm = cnm;
        }
    }

    private void mergeBasedOnAlgorithmAndIds() {
        Timers.unionFindTimer.reset();
        Timers.unionFindTimer.start();

        UnionFindSet<Row> ufs = new UnionFindSet<>();

        // do unification, the criteria are based on one of 2 factors
        // same id, or same cluster according to the merge algorithm

        for (String id: idToRows.keySet()) {
            ufs.unifyAllElementsOfCollection (idToRows.get(id));
        }

        for (Collection<Row> cluster : algorithm.classes) {
            ufs.unifyAllElementsOfCollection(cluster);
        }

        Timers.unionFindTimer.stop();
        Timers.log.info ("Time for union-find: " + Timers.unionFindTimer.toString());
        listOfSimilarCandidates = (List) ufs.getClassesSortedByClassSize();
    }

    private void computeIdToRows (Collection<Row> rows) {
        for (Row r: rows)
            idToRows.put (r.get(Config.ID_FIELD), r);
    }

    public void run() {
        algorithm.run();
        mergeBasedOnAlgorithmAndIds();
    }

    /** applies the given merge/unmerge commands on the dataset */
    public void applyUpdatesAndSave(MergeCommand[] commands) throws IOException {
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

                    log.info("Merging id " + id + " into " + firstId);
                    Collection<Row> rowsForThisId = idToRows.get(id);
                    for (Row row : rowsForThisId)
                        row.set(Config.ID_FIELD, firstId); // we wipe out the old id for this row
                }
            } else if ("unmerge".equalsIgnoreCase(command.op)) {
                // create unique id's for all rows
                for (String id : command.ids) {
                    Collection<Row> rowsForThisId = idToRows.get(id);
                    for (Row row : rowsForThisId) {

                    }
                }
            }
        }

        d.save();
    }

	/** for each element in list,
	 *  the ids in each sub-list are merged.
	 */
	public void merge(List<List<String>> list) {
		for (List<String> idGroup: list) {
			String newIdForAllRowsInThisGroup = idGroup.get(0);
			for (String id: idGroup) {
				for (Row r: idToRows.get(id)) {
					String oldId = r.get("ID");
					if (newIdForAllRowsInThisGroup.equals (oldId)) {

					} else {
						r.set(Config.ID_FIELD, newIdForAllRowsInThisGroup);
						idToRows.put (r.get(Config.ID_FIELD), r);
						idToRows.remove (oldId, r);
					}
				}
			}
		}
	}

	final public void evaluateConfidence(){
		for(Collection<Row> group:listOfSimilarCandidates){
			//Evaluate confidence for the current group
			int confidence = 0;
			confidence += group.iterator().next().get("st_Name").length();

			//set confidence for current rows
			for(Row row:group){
				row.set("confidence",String.valueOf(confidence));
			}
		}
	}

	public void sort(String sortOrder){
		Comparator<Collection<Row>> comparator;
		if ("confidence".equals (sortOrder)) {
			evaluateConfidence();	//confidence needs to be evaluated first for every row
			comparator = SurfExcel.confidenceComparator;
		}
		else
			comparator = SurfExcel.alphabeticalComparartor;

		listOfSimilarCandidates.sort(comparator);
	}

	/** returns a collection of groups. under each group is a bunch of ids. under each id has a bunch of rows.
	 * that's why it returns a List of List of List of Rows.
	 * it is assumed that:
	 * one row is only in one group, i.e. it can't belong to multiple groups.
	 * and all rows belonging to an id are also in the same group.
	 * (this should be satisfied if the inputGroupsList has been generated by a Union-Find set. */
	public List<List<List<Row>>> applyFilter (List<Collection<Row>> inputGroupsList, Filter filter) {

		List<List<List<Row>>> result = new ArrayList<>();

		for (Collection<Row> rowCollection: inputGroupsList) {
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
