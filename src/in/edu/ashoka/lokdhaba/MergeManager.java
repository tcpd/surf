package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpSession;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;

public abstract class MergeManager {

	//static final String SEPERATOR = "-";
	Dataset d;
	HashMap<Row, String> rowToId;
    HashMap<String, Row> idToRow;
    String arguments;

    ArrayList<Collection<Row>> listOfSimilarCandidates;
    ArrayList<Collection<Row>> groupMergedListOfSimilarCandidates;
    Multimap<String,Row> personToRows;
    HashMap<String, Integer> rowToGroup;
    HashMap<String, Integer> rowToMergedGroup;

    
    public static MergeManager getManager(String algo, Dataset d, String arguments){
		MergeManager mergeManager = null;

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
		}
		else if(algo.equals("search")){
			mergeManager = new SearchMergeManager(d, arguments);
		}
		else{}
		return mergeManager;
	}

	Comparator<Collection<Row>> getComparator(String comparatorType){
		Comparator<Collection<Row>> comparator;
    	if(comparatorType.equals("confidence")) {
    		evaluateConfidence();	//confidence needs to be evaluated first for every row
			comparator = SurfExcel.confidenceComparator;
		}
    	else if(comparatorType.equals("alphabetical"))
    		comparator = SurfExcel.alphabeticalComparartor;
    	else
    		comparator = SurfExcel.alphabeticalComparartor;
    	return comparator;
	}
    
	
    protected MergeManager(Dataset d){
    	this.d=d;
        if(!d.hasColumnName("ID"))
            d.addToActualColumnName("ID");
        if(!d.hasColumnName("mapped_ID"))
            d.addToActualColumnName("mapped_ID");
        if(!d.hasColumnName("comments"))
		    d.addToActualColumnName("comments");
        if(!d.hasColumnName("user_name"))
		    d.addToActualColumnName("user_name");
        if(!d.hasColumnName("email"))
		    d.addToActualColumnName("email");
        if(!d.hasColumnName("is_done"))
		    d.addToActualColumnName("is_done");
		
    }
    
	//initialize the id's for each row
	final public void initializeIds(){
		SurfExcel.assignUnassignedIds(d.getRows(), "ID");
		SurfExcel.assignUnassignedIds(d.getRows(), "mapped_ID");
	}
	
	public final void performInitialMapping(){
		rowToId = new HashMap<>();
		idToRow = new HashMap<>();
		for(Row row:d.getRows()){
    		rowToId.put(row, row.get("ID"));
    		idToRow.put(row.get("ID"), row);
    		row.set("comments", ""); 	//set comments to empty on initial mapping
    	}
	}
	
	//add candiates which will be judged based on their group
	abstract public void addSimilarCandidates();
	
	final public void merge(String [] ids){
		Multimap<Integer, String> mp = LinkedHashMultimap.create();
		for(String id:ids){
			//mp.put(idToRow.get(id).get("common_group_id"), id);		//algorithm will decide the common_group_id
			mp.put(rowToMergedGroup.get(id),id);
		}
		
		for(Integer key:mp.keySet()){
			ArrayList<String> innerIds= new ArrayList<>();
			innerIds.addAll(0, mp.get(key));
			String defaultId = getRootId(innerIds.get(0));
			for(int i=1;i<innerIds.size();i++){
				Row tempRow = idToRow.get(innerIds.get(i));
				rowToId.put(tempRow, defaultId);
			}
		}

		/*//setting mapped id here instead of inside save
		Collection<Row> rows = d.getRows();
		for(Row row:rows){
			row.set("mapped_ID", rowToId.get(row));
		}*/
	}

	final public void forceMerge(String [] ids){
		String defaultId = getRootId(ids[0]);
        for(int i = 1; i<ids.length; i++){
			Row tempRow = idToRow.get(ids[i]);
			rowToId.put(tempRow, defaultId);
			tempRow.set("mapped_ID", defaultId);
		}
		setupPersonToRowMap();
		//setupRowToGroupMap();
    }

	//basic version; might need improvements
	final public void deMerge(String [] ids){
		//First take care of rows with different id and mapped id
		for(String id:ids){
			Row row = idToRow.get(id);
			//marked rows also needs to be reviewed again
			row.set("is_done", "no");
			if(row.get("ID").equals(row.get("mapped_ID")))
				continue;
			rowToId.put(row, row.get("ID"));
			row.set("mapped_ID", row.get("ID"));
			//System.out.println();
		}
		
		//Now take care of rows with same id and mapped id
		for(String id:ids){
			Row row = idToRow.get(id);
			
			if(id.equals(row.get("mapped_ID"))){
				ArrayList<Row> samePersonRows = new ArrayList<>();
				for(Row tempRow:personToRows.get(row.get("mapped_ID"))){
					if(tempRow.get("mapped_ID").equals(id) && isMappedToAnother(tempRow.get("ID"))){
						samePersonRows.add(tempRow);
					}
				}
				if(samePersonRows!=null){
					for(Row tempRow:samePersonRows){
						rowToId.put(tempRow, samePersonRows.get(0).get("ID"));
						tempRow.set("mapped_ID", samePersonRows.get(0).get("ID"));
					}
				}
			}
		}
		setupPersonToRowMap();
		//setupRowToGroupMap();
	}
	final public void save(String filePath) throws IOException{
		d.save(filePath);
	}

	final public void load(){
		rowToId = new HashMap<>();
		idToRow = new HashMap<>();
		for(Row row:d.getRows()){
			idToRow.put(row.get("ID"), row);
			rowToId.put(row, row.get("mapped_ID"));
		}
	}

	final public void setupPersonToRowMap(){
		personToRows = LinkedHashMultimap.create();
		/*for(Collection<Row> group:listOfSimilarCandidates){
			for(Row row:group){
				personToRows.put(row.get("mapped_ID"),row);
			}
		}*/
		for(Row row:d.getRows()){
			personToRows.put(row.get("mapped_ID"),row);
		}
	}

	final public void setupRowToGroupMap(){
		rowToGroup = new HashMap<>();
		for(int i=0; i<listOfSimilarCandidates.size(); i++){
			Collection<Row> group = listOfSimilarCandidates.get(i);
			for(Row row:group){
				rowToGroup.put(row.get("ID"),i);
			}
		}
	}

	final public void setupRowToMergedGroupMap(){
		rowToMergedGroup = new HashMap<>();
		for(int i=0; i<groupMergedListOfSimilarCandidates.size(); i++){
			Collection<Row> group = groupMergedListOfSimilarCandidates.get(i);
			for(Row row:group){
				rowToMergedGroup.put(row.get("ID"),i);
			}
		}
	}

	final public void onlyKeepWinners(ArrayList<Multimap<String,Row>> listOfSet){
		for(int i=0; i<listOfSet.size();i++){
			Multimap<String, Row> group = listOfSet.get(i);
			boolean isWinner = false;
			for(Row row:group.values()){
				if(row.get("Position").equals("1")){
					isWinner = true;
					break;
				}
			}
			if(!isWinner){
				listOfSet.remove(i);
				i--;
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

	
	//returns a list of group of similar named incumbents
	final public ArrayList<Multimap<String,Row>> getIncumbents(String attribute, String [] values, boolean onlyWinners, String searchQuery){
		boolean filterNeeded = (attribute!=null)&&(!attribute.equals(""))&&(values!=null);
		for(String value:values){
			if(value.equals("All Records")){
				filterNeeded = false;
				break;
			}
		}
		boolean isSearch = searchQuery!=null && !searchQuery.equals("");
		List<Collection<Row>> groupList;
			groupList = getGroupMergedListOfSimilarCandidate();
		ArrayList<Multimap<String, Row>> listOfSet = new ArrayList<>();
		for(Collection<Row> similarRows:groupList){
			Multimap<String, Row> mp = LinkedHashMultimap.create();
			for(Row row:similarRows){
				if(filterNeeded){	//APPLY FILTER IF NEEDED
					for(String value:values){
						if(row.get(attribute).equals(value))
							mp.put(rowToId.get(row), row);
					}
				} else {			//NO FILTER APPLIED
					mp.put(rowToId.get(row), row);
				}
			}
			if(mp.values().size()>1)	//check whether there are more than 1 member in a group
				listOfSet.add(mp);
		}
		
		if(onlyWinners && !isSearch)onlyKeepWinners(listOfSet);

		return listOfSet;
	}


	final private ArrayList<Collection<Row>> getGroupMergedListOfSimilarCandidate(){
		setupRowToGroupMap();
		groupMergedListOfSimilarCandidates = new ArrayList<>();
		List<Integer> mergedGroupsIndices = new ArrayList<>();
		Map<Integer, Integer> groupMap = new HashMap<>();
		for(int i=0; i<listOfSimilarCandidates.size();i++){
			Collection<Row> group = listOfSimilarCandidates.get(i);
			Collection<Row> mergedGroup = new ArrayList<>();
			Set<String> persons = new HashSet<>();
			for(Row row:group){
				persons.add(rowToId.get(row));
			}
			//IF GROUP NOT PRESENT
			if(!mergedGroupsIndices.contains(i)){
				mergedGroup.addAll(group);
				for(String person:persons){
					for(Row row:personToRows.get(person)){
						if(rowToGroup.get(row.get("ID"))==null){	//rows belong to a person but didn't get added in any group in current algorithm
							mergedGroup.add(row);
							continue;
						}else{
							if(!rowToGroup.get(row.get("ID")).equals(i)){
								if(mergedGroupsIndices.contains(rowToGroup.get(row.get("ID"))))
									continue;
								mergedGroupsIndices.add(rowToGroup.get(row.get("ID")));
								mergedGroup.addAll(listOfSimilarCandidates.get(rowToGroup.get(row.get("ID"))));
								groupMap.put( rowToGroup.get(row.get("ID")) , groupMergedListOfSimilarCandidates.size());
							}
						}
					}
				}
				groupMergedListOfSimilarCandidates.add(mergedGroup);
			}
			else{	//IF GROUP PRESENT
				int parentGroup = groupMap.get(i);
				for(String person:persons){
					for(Row row:personToRows.get(person)){
						if(rowToGroup.get(row.get("ID"))==null){
							mergedGroup.add(row);
							continue;
						}else{
							//If the current row doesn't belong to current group and it's parent's group
							if(!rowToGroup.get(row.get("ID")).equals(i) && !rowToGroup.get(row.get("ID")).equals(parentGroup)){
								if(mergedGroupsIndices.contains(rowToGroup.get(row.get("ID"))))
									continue;
								mergedGroupsIndices.add(rowToGroup.get(row.get("ID")));
								mergedGroup.addAll(listOfSimilarCandidates.get(rowToGroup.get(row.get("ID"))));
								groupMap.put(rowToGroup.get(row.get("ID")),parentGroup);
							}
						}
					}
				}
				groupMergedListOfSimilarCandidates.get(parentGroup).addAll(mergedGroup);
			}

		}
		setupRowToMergedGroupMap();
		return groupMergedListOfSimilarCandidates;
	}

	final public void sort(String comparatorType){
		listOfSimilarCandidates.sort(getComparator(comparatorType));
	}
	
	
	//check whether this row is mapped to another name
	final public boolean isMappedToAnother(String id){
		if(rowToId.get(idToRow.get(id)).equals(id))
			return false;
		else 
			return true;
	}
	
	final public String getRootId(String id){
		while(!rowToId.get(idToRow.get(id)).equals(id)){
			id = rowToId.get(idToRow.get(id));
		}
		return id;
	}
	final public void updateMappedIds(){
		for(Row row:rowToId.keySet()){
			String id = getRootId(rowToId.get(row));
			rowToId.put(row, id);
			row.set("mapped_ID", rowToId.get(row));
		}
	}
	
	//check whether reading the file for the first time
	public boolean isFirstReading(){
		Collection<Row> allRows = d.getRows();
		boolean isAssigned= true;
		for(Row row:allRows){
			if(row.get("ID").equals(""))
				isAssigned=false;
			if(row.get("mapped_ID").equals(""))
				isAssigned=false;
		}
		return !isAssigned;
	}
	
	public int [] getListCount(ArrayList<Multimap<String,Row>> groupLists){
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
	
	public void updateComments(Map<String,String> map){
		for(String key:map.keySet()){
			idToRow.get(key).set("comments", map.get(key));
			//System.out.println(idToRow.get(key).get("comments"));
		}
	}
	
	//method that updates the drop down information
	public void updateIsDone(Map<String, String> map){
		for(String key:map.keySet()){
			//idToRow.get(key).set("is_done", (map.get(key).equals("on")?"yes":"no"));

			Row row = idToRow.get(key);
			Collection<Row> person = personToRows.get(row.get("mapped_ID"));
			//This needs to be done because person to row map isn't updated after merge, it is only updated when the incumbents view is generated.
			if(person.size()<=1){
				idToRow.get(key).set("is_done", (map.get(key).equals("on")?"yes":"no"));
				continue;
			}
			for(Row record:person){
				if(map.get(key).equals("on")){
					record.set("is_done","yes");
				}else{
					record.set("is_done","no");
				}

			}

			//System.out.println(idToRow.get(key).get("is_done"));
		}
	}

	public void resetIsDone(){
		for(Row row:d.getRows()){
			if(row.get("is_done").equals("yes"))
				row.set("is_done", "no");
		}
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
	
	public final void updateUserIds(String []userRows, String userName, String email){
			for(String id:userRows){
				idToRow.get(id).set("user_name", userName);
				idToRow.get(id).set("email", email);
			}
		}

	public final void setArguments(String arguments){
		this.arguments = arguments;
	}
}
