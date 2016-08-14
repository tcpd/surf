package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public abstract class MergeManager {
	
	Dataset d;
	HashMap<Row, String> rowToId;
    HashMap<String, Row> idToRow;
    
    //future algorithms will need references here; this is to keep objects in memory for faster reloading of page
    static ExactSameNameMergeManager exactSameNameMergeManager;
    static SimilarNameMergeManager similarNameMergeManagerED1;
    static SimilarNameMergeManager similarNameMergeManagerED2;
    
    static Map<String,Set<String>> attributesDataSet;
    ArrayList<Collection<Row>> listOfSimilarCandidates; 
    
    
    
    //this is a factory method which generates the right manager
    // these methods are singleton
    //for new algorithms these need to be updated
    
    
    public static MergeManager getManager(String algo, Dataset d){
    	if(algo.equals("exactSameName")){
    		if(exactSameNameMergeManager==null){
    			exactSameNameMergeManager= new ExactSameNameMergeManager(d);
    		}
    			return exactSameNameMergeManager;
    	}	
    	else if(algo.equals("editDistance1")){
    		if(similarNameMergeManagerED1==null){
    			similarNameMergeManagerED1 = new SimilarNameMergeManager(d, 1);
    		}
    			return similarNameMergeManagerED1;
    	}
    	else if(algo.equals("editDistance2")){
    		if(similarNameMergeManagerED2==null){
    			similarNameMergeManagerED2 = new SimilarNameMergeManager(d, 2);
    		}
    			return similarNameMergeManagerED2;
    	}
		return null;
    }
    
    
    
    
	
    public MergeManager(Dataset d){
    	this.d=d;
		d.addToActualColumnName("ID");
		d.addToActualColumnName("mapped_ID");
		d.addToActualColumnName("comments");
		d.addToActualColumnName("user_name");
		d.addToActualColumnName("email");
		//d.addToActualColumnName("is_processed");
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
	final public void save(String filePath){
		Collection<Row> rows = d.getRows();
		for(Row row:rows){
			row.set("mapped_ID", rowToId.get(row));
		}
		try {
			d.save(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	final public void load(){
		rowToId = new HashMap<>();
		idToRow = new HashMap<>();
		for(Row row:d.getRows()){
			idToRow.put(row.get("ID"), row);
			rowToId.put(row, row.get("mapped_ID"));
		}
	}
	
	
	final public ArrayList<Multimap<String,Row>> getIncumbents(){
		ArrayList<Multimap<String, Row>> listOfSet = new ArrayList<>();
		for(Collection<Row> similarRows:listOfSimilarCandidates){
			Multimap<String, Row> mp = LinkedHashMultimap.create();
			for(Row row:similarRows){
				mp.put(rowToId.get(row), row);
			}
			listOfSet.add(mp);
		}
		sortAlphabetically(listOfSet);
		return listOfSet;
	}
	
	//returns a list of group of similar named incumbents
	final public ArrayList<Multimap<String,Row>> getIncumbents(String attribute, String [] values){
		
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
		
		
		sortAlphabetically(listOfSet);
		return listOfSet;
	}

	
	final public void sortAlphabetically(ArrayList<Multimap<String,Row>> listOfSet){
		//Queue<String> pq = new PriorityQueue<>();
		listOfSet.sort(new Comparator<Multimap<String,Row>>(){

			@Override
			public int compare(Multimap<String, Row> o1, Multimap<String, Row> o2) {
				
				return o1.values().iterator().next().get("Name").compareTo(o2.values().iterator().next().get("Name"));
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
		int [] statusCount = new int[3];
		if(listOfSimilarCandidates==null){
			System.out.println("0");
			return null;
		}
		int i=0;
		int j=0;
		System.out.println("total number of groups:" + groupLists.size());
		for(Multimap mp:groupLists){
			//System.out.println("Unique person identified: "+mp.keySet().size());
			i+=mp.keySet().size();
			j+=mp.values().size();
		}
		//System.out.println("Unique person identified: " + i);
		//System.out.println("Unique rows identified: " + j);
		//System.out.println("Redundency removed:  "+(j-i));
		statusCount[0] = i;
		statusCount[1] = j;
		statusCount[2] = j-i;
		return statusCount;
		
	}
	
	public void updateComments(Map<String,String> map){
		for(String key:map.keySet()){
			idToRow.get(key).set("comments", map.get(key));
			//System.out.println(idToRow.get(key).get("comments"));
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
}
