package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.*;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import edu.stanford.muse.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class MergeManager {

    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.MergeManager.class);
	Dataset d;
    String arguments;
    public List<Collection<Row>> listOfSimilarCandidates; // these are the groups
    private Multimap<String, Row> idToRows = LinkedHashMultimap.create();

    static final int WINNER_POSITION_CAP = 3;

    /** create a new mergeManager with the given algorithm and arguments */
    public static MergeManager getManager(Dataset d, String algo, String arguments){
		MergeManager mergeManager = null;
        if (algo.equals("editDistance")){
            int editDistance = 1;
            try {
                editDistance = Integer.parseInt(arguments);
            } catch (Exception e) {
                Util.print_exception(e, log);
            }
            mergeManager = new EditDistanceMergeManager(d, Config.MERGE_FIELD, editDistance);
        }

		if(algo.equals("exactSameName")){
			mergeManager = new ExactSameNameMergeManager(d);
		}
		else if(algo.equals("editDistance1")){
			mergeManager = new SimilarNameMergeManager(d, 1);	//TESTING
		}
		else if(algo.equals("editDistance2")){
			mergeManager = new SimilarNameMergeManager(d, 2);	//TESTING
		}
		else if(algo.equals("dummyAllName")){
			mergeManager = new DummyMergeManager(d);
		}
		else if(algo.equals("exactSameNameWithConstituency")){
			mergeManager = new ExactSameNameWithConstituencyMergeManager(d);
		} else if(algo.equals("compatibleNames")){
			CompatibleNameManager cnm = new CompatibleNameManager(d, "cand1");
			mergeManager = cnm;
		}
		else if(algo.equals("search")){
			mergeManager = new SearchMergeManager(d, arguments);
		}
		return mergeManager;
	}

    protected MergeManager(Dataset d){
    	this.d = d;
        if(!d.hasColumnName("__comments"))
		    d.addToActualColumnName("__comments");
        if(!d.hasColumnName("__reviewed"))
		    d.addToActualColumnName("__reviewed");
		computeIdToRows (d.getRows());
    }

    private void computeIdToRows (Collection<Row> rows) {
        for (Row r: rows)
            idToRows.put (r.get(Config.ID_FIELD), r);
    }

	//add candiates which will be judged based on their group
	abstract public void addSimilarCandidates();

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

	final public void save(String filePath) throws IOException{
		d.save(filePath);
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

	//returns a list of group of similar named groups
	final public List<Collection<Row>> doFilter() {
        List<Collection<Row>> result = new ArrayList<>();

        for (Collection<Row> groupOfRows:listOfSimilarCandidates) {
		    boolean includeGroup = false;
            for (Row r: groupOfRows) {
                String pc_no = r.get("PC_no");
                if ("1".equals (pc_no) || "2".equals (pc_no) || "3".equals (pc_no)) {
                    includeGroup = true;
                    break;
                }
            }
            if (includeGroup)
                result.add (groupOfRows);
        }
		return result;
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


	int [] getListCount(ArrayList<Multimap<String,Row>> groupLists){
		int [] statusCount = new int[5];
		if(listOfSimilarCandidates==null){
			System.out.println("0");
			return null;
		}
		int i=0;
		int j=0;
		int k=0;
		System.out.println("total number of groups:" + groupLists.size());
		for(Multimap mp:groupLists){
			//System.out.println("Unique person identified: "+mp.keySet().size());
			i+=mp.keySet().size();
			j+=mp.values().size();
			for (Row row:(Collection<Row>)mp.values()){
				if(row.get("is_done").equals("yes"))
					k++;
			}
		}
		//System.out.println("Unique person identified: " + i);
		//System.out.println("Unique rows identified: " + j);
		//System.out.println("Redundency removed:  "+(j-i));
		statusCount[0] = i;
		statusCount[1] = j;
		statusCount[2] = j-i;
		statusCount[3] = groupLists.size();
		statusCount[4] = k;	//rows reviewed
		return statusCount;
		
	}

	public void resetIsDone(){
		for(Row row:d.getRows()){
            row.set("is_done", "no");
		}
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

	public Map<String,Set<String>> getAttributesDataSet(String [] attributes){
		Map<String,Set<String>> attributeMap = new HashMap<>();
		
		for(String attribute:attributes){
			Set<String> valueSet = new HashSet<>();
			for(Row row:d.getRows()){
				valueSet.add(row.get(attribute));
			}
			attributeMap.put(attribute, valueSet);
		}
		
		return attributeMap; 
	}

	public final void setArguments(String arguments){
		this.arguments = arguments;
	}
}
