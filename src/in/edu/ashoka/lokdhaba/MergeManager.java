package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public abstract class MergeManager {
	
	Dataset d;
	HashMap<Row, String> rowToId;
    HashMap<String, Row> idToRow;
    
    //future algorithms will need references here
    static ExactSameNameMergeManager exactSameNameMergeManager;
    static SimilarNameMergeManager similarNameMergeManagerED1;
    static SimilarNameMergeManager similarNameMergeManagerED2;
    
    ArrayList<Collection<Row>> listOfSimilarCandidates; 
    
    //this is a factory method which generates the right manager
    // these methods are singleton
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
    			similarNameMergeManagerED1 = new SimilarNameMergeManager(d, 2);
    		}
    			return similarNameMergeManagerED2;
    	}
		return null;
    }
	
    public MergeManager(Dataset d){
    	this.d=d;
		d.addToActualColumnName("ID");
		d.addToActualColumnName("mapped_ID");
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
    	}
	}
	
	//add candiates which will be judged based on their group
	abstract public void addSimilarCandidates();
	
	final public void merge(String [] ids){
		Multimap<String, String> mp = LinkedHashMultimap.create();
		for(String id:ids){
			mp.put(idToRow.get(id).get("common_group_id"), id);		//algorithm will decide the common_group_id
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
	final public void deMerge(String [] ids){
		//TO BE IMPLEMENTED
	}
	final public void save(){
		Collection<Row> rows = d.getRows();
		for(Row row:rows){
			row.set("mapped_ID", rowToId.get(row));
		}
		try {
			d.save("/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info_updated.csv");
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
	
	//returns a list of group of similar named incumbents
	final public ArrayList<Multimap<String,Row>> getIncumbents(){
		ArrayList<Multimap<String, Row>> listOfSet = new ArrayList<>();
		for(Collection<Row> similarRows:listOfSimilarCandidates){
			Multimap<String, Row> mp = LinkedHashMultimap.create();
			for(Row row:similarRows){
				mp.put(rowToId.get(row), row);
			}
			listOfSet.add(mp);
		}
		return listOfSet;
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
	
}
