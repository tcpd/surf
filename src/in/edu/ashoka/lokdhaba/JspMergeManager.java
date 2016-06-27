package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MutableClassToInstanceMap;

public class JspMergeManager implements MergeManager{
	
	Dataset d;
	HashMap<Row, String> rowToId;
    HashMap<String, Row> idToRow;
    Multimap<String, Row> resultMap;
    
    ArrayList<Collection<Row>> listOfSimilarCandidates; 
	
	public JspMergeManager(Dataset d) {
		this.d=d;
	}

	@Override
	public void initializeIds() {
		SurfExcel.assignUnassignedIds(d.getRows(), "ID");
		
	}

	@Override
	public void addSimilarCandidates() {
		listOfSimilarCandidates = new ArrayList<>();
		try {
			resultMap = Bihar.getExactSamePairs(d.getRows(), d);
			for(String canonicalVal: resultMap.keySet()){
				listOfSimilarCandidates.add(resultMap.get(canonicalVal));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	@Override
	public void merge(String[] ids) {
		Multimap<String, String> mp = LinkedHashMultimap.create();
		for(String id:ids){
			mp.put(idToRow.get(id).get("_st_name"), id);		//hardcoded for now; not recommended
		}
		
		for(String key:mp.keySet()){
			ArrayList<String> innerIds= new ArrayList<>();
			innerIds.addAll(0, mp.get(key));
			String defaultId = innerIds.get(0);
			for(int i=1;i<innerIds.size();i++){
				Row tempRow = idToRow.get(innerIds.get(i));
				rowToId.put(tempRow, defaultId);
			}
			
		}
		/*
		String defaultId = ids[0];
    	for(int i=1;i<ids.length;i++) {
    		Row tempRow = idToRow.get(ids[i]);
    		rowToId.put(tempRow, defaultId);
    	}*/
		
	}

	@Override
	public void deMerge(String[] ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performInitialMapping() {
		rowToId = new HashMap<>();
		idToRow = new HashMap<>();
		for(Row row:d.getRows()){
    		rowToId.put(row, row.get("ID"));
    		idToRow.put(row.get("ID"), row);
    	}
	}
	
	public void display(){
		int i=0;
		for(Collection<Row> similarRows:listOfSimilarCandidates){
			System.out.println("Collection "+i+":==========================================================");
			i++;
			for(Row row:similarRows){
				System.out.println(row);
			}
		}
	}
	
	public void display2(){
		ArrayList<Multimap<String, Row>> similarIncumbents = getIncumbents();
		int i=0;
		for(Multimap<String,Row> mp:similarIncumbents) {
			System.out.println("Group "+i+"================================================");
			for(String key:mp.keySet()){
				System.out.println("Similar people:"+key+"----------------------------------------");
				for(Row row:mp.get(key)){
					System.out.println(row);
				}
			}
		}
	}

	@Override
	public ArrayList<Multimap<String, Row>> getIncumbents() {
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
	
	
	
}
