package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpSession;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public abstract class MergeManager {

	static final String SEPERATOR = "-";
	Dataset d;
	HashMap<Row, String> rowToId;
    HashMap<String, Row> idToRow;
    String [] arguments;
    
    //future algorithms will need references here; this is to keep objects in memory for faster reloading of page
    static Map<HttpSession,ExactSameNameMergeManager> exactSameNameMergeManagerMaps = new HashMap<>();
    static Map<HttpSession,SimilarNameMergeManager> similarNameMergeManagerED1Maps = new HashMap<>();
    static Map<HttpSession,SimilarNameMergeManager> similarNameMergeManagerED2Maps = new HashMap<>();
    static Map<HttpSession,DummyMergeManager> dummyMergeManagerMaps = new HashMap<>();
    
    static Map<String,Set<String>> attributesDataSet;
    ArrayList<Collection<Row>> listOfSimilarCandidates; 
    
    
    
    //this is a factory method which generates the right manager
    // these methods are singleton
    //for any new algorithms, these need to be updated
    
    //DEPRECIATED CODE
    /*public static MergeManager getManager(HttpSession session, String algo, Dataset d, boolean forceRefresh){
    	//System.out.println("I am in MergeManager");
    	if(algo.equals("exactSameName")){
    		if(exactSameNameMergeManagerMaps.get(session)==null||forceRefresh){
    			exactSameNameMergeManagerMaps.put(session, new ExactSameNameMergeManager(d));
    		}
    			return exactSameNameMergeManagerMaps.get(session);
    	}	
    	else if(algo.equals("editDistance1")){
    		if(similarNameMergeManagerED1Maps.get(session)==null||forceRefresh){
    			similarNameMergeManagerED1Maps.put(session, new SimilarNameMergeManager(d, 1));
    		}
    			return similarNameMergeManagerED1Maps.get(session);
    	}
    	else if(algo.equals("editDistance2")){
    		if(similarNameMergeManagerED2Maps.get(session)==null||forceRefresh){
    			similarNameMergeManagerED2Maps.put(session, new SimilarNameMergeManager(d, 2));
    		}
    			return similarNameMergeManagerED2Maps.get(session);
    	}
    	else if(algo.equals("dummyAllName")){
    		if(dummyMergeManagerMaps.get(session)==null||forceRefresh){
    			dummyMergeManagerMaps.put(session, new DummyMergeManager(d));
    		}
    		return dummyMergeManagerMaps.get(session);
    	}
		return null;
    }*/
    
    public static MergeManager getManager(String algo, Dataset d){
		MergeManager mergeManager = null;
		if(algo.equals("exactSameName")){
			mergeManager = new ExactSameNameMergeManager(d);
		}
		else if(algo.equals("editDistance1")){
			mergeManager = new SimilarNameMergeManager(d, 1);
		}
		else if(algo.equals("editDistance2")){
			mergeManager = new SimilarNameMergeManager(d, 2);
		}
		else if(algo.equals("dummyAllName")){
			mergeManager = new DummyMergeManager(d);
		}
		else if(algo.equals("exactSameNameWithConstituency")){
			mergeManager = new ExactSameNameWithConstituencyMergeManager(d);
		}
		else{}
		return mergeManager;
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
		Multimap<String, String> mp = LinkedHashMultimap.create();
		for(String id:ids){
			mp.put(idToRow.get(id).get("common_group_id"), id);		//algorithm will decide the common_group_id
			//idToRow.get(id).set("is_processed", "true"); 	//set is_processed to true; this will keep track of number of rows being processed
		}
		
		for(String key:mp.keySet()){
			ArrayList<String> innerIds= new ArrayList<>();
			innerIds.addAll(0, mp.get(key));
			String defaultId = getRootId(innerIds.get(0));
			for(int i=1;i<innerIds.size();i++){
				Row tempRow = idToRow.get(innerIds.get(i));
				rowToId.put(tempRow, defaultId);
			}
		}

		//setting mapped id here instead of inside save
		Collection<Row> rows = d.getRows();
		for(Row row:rows){
			row.set("mapped_ID", rowToId.get(row));
		}
	}

	final public void forceMerge(String [] ids){
		String defaultId = getRootId(ids[0]);
        for(int i = 1; i<ids.length; i++){
			Row tempRow = idToRow.get(ids[i]);
			rowToId.put(tempRow, defaultId);
		}

		Collection<Row> rows = d.getRows();
		for(Row row:rows){
			row.set("mapped_ID", rowToId.get(row));
		}
    }

	//basic version; might need improvements
	final public void deMerge(String [] ids){
		//First take care of rows with different id and mapped id
		for(String id:ids){
			Row row = idToRow.get(id);
			if(row.get("ID").equals(row.get("mapped_ID")))
				continue;
			rowToId.put(row, row.get("ID"));
			//System.out.println();
		}
		
		//Now take care of rows with same id and mapped id
		for(String id:ids){
			Row row = idToRow.get(id);
			
			if(id.equals(row.get("mapped_ID"))){
				ArrayList<Row> samePersonRows = new ArrayList<>();
				for(Row tempRow:d.getRows()){
					if(tempRow.get("mapped_ID").equals(id) && isMappedToAnother(tempRow.get("ID"))){
						samePersonRows.add(tempRow);
					}
				}
				if(samePersonRows!=null){
					for(Row tempRow:samePersonRows){
						rowToId.put(tempRow, samePersonRows.get(0).get("ID"));
					}
				}
			}
		}
	}
	final public void save(String filePath) throws IOException{

		/*try {
			d.save(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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
	
	
	final public ArrayList<Multimap<String,Row>> getIncumbents(boolean onlyWinners){
		ArrayList<Multimap<String, Row>> listOfSet = new ArrayList<>();
		for(Collection<Row> similarRows:listOfSimilarCandidates){
			Multimap<String, Row> mp = LinkedHashMultimap.create();
			for(Row row:similarRows){
				mp.put(rowToId.get(row), row);
			}
			listOfSet.add(mp);
		}
		if(onlyWinners)onlyKeepWinners(listOfSet);
		//sortAlphabetically(listOfSet);
		return listOfSet;
	}
	
	//returns a list of group of similar named incumbents
	final public ArrayList<Multimap<String,Row>> getIncumbents(String attribute, String [] values, boolean onlyWinners){
		
		ArrayList<Multimap<String, Row>> listOfSet = new ArrayList<>();
		for(Collection<Row> similarRows:listOfSimilarCandidates){
			Multimap<String, Row> mp = LinkedHashMultimap.create();
			for(Row row:similarRows){
				for(String value:values){
					if(row.get(attribute).equals(value))
						mp.put(rowToId.get(row), row);
					//else
					//	continue;
				}
				
			}
			if(mp.values().size()>1)	//check whether there are more than 1 member in a group
				listOfSet.add(mp);
		}
		
		if(onlyWinners)onlyKeepWinners(listOfSet);
		//sortAlphabetically(listOfSet);	Tesing the correct place to put this
		return listOfSet;
	}

	/*
	final public void sortAlphabetically(ArrayList<Multimap<String,Row>> listOfSet){
		//Queue<String> pq = new PriorityQueue<>();
		listOfSet.sort(new Comparator<Multimap<String,Row>>(){

			@Override
			public int compare(Multimap<String, Row> o1, Multimap<String, Row> o2) {
				
				return o1.values().iterator().next().get("Name").compareTo(o2.values().iterator().next().get("Name"));
			}
			
		});
		
	}*/

	final public void sortAlphabetically(ArrayList<Collection<Row>> listOfSet){
		listOfSet.sort(new Comparator<Collection<Row>>(){

			@Override
			public int compare(Collection<Row> o1, Collection<Row> o2) {

				return o1.iterator().next().get("Name").compareTo(o2.iterator().next().get("Name"));
			}

		});

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
			idToRow.get(key).set("is_done", (map.get(key).equals("on")?"yes":"no"));
			//System.out.println(idToRow.get(key).get("is_done"));
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
		this.arguments = arguments.split(SEPERATOR);
	}
}
