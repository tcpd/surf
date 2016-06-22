package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.common.collect.Multimap;

public class Test {
	
	public static boolean isSame(
			HashMap<Row, String> rowToId, 
			HashMap<String, Row> idToRow,
			String id1,
			String id2) {
		if(id1.equals(id2))
			return true;
		String root1=id1, root2=id2;
		while(root1!=idToRow.get(root1).get("ID")){
			Row tempRow = idToRow.get(root1);
			root1 = rowToId.get(tempRow);
		}
		while(root2!=idToRow.get(root2).get("ID")){
			Row tempRow = idToRow.get(root2);
			root2 = rowToId.get(tempRow);
		}
		
		return root1==root2;
	}
	
	public static void display(Multimap<String, Row> resultMap,HashMap<Row, String> rowToId, HashMap<String, Row> idToRow) {
		//List<Row> rowsTobeDisplayed = new ArrayList<Row>();
		HashSet<String> set = new LinkedHashSet<>();
	    for (String canonicalVal: resultMap.keySet()) {
	         Collection<Row> idsForThisCVal = resultMap.get(canonicalVal);
	         
	         // UI should allow for merging between any 2 of these ids.
	         
	         for (Row row: idsForThisCVal) {
	        	 if(row.get("ID").equals(rowToId.get(row)))
	            	 set.add(rowToId.get(row));
	        	 }
	         
	             // now print these rows in one box -- its one cohesive unit, which cannot be broken down.
	        }
	    
	    for(String id:set){
       	 System.out.println(idToRow.get(id));
        }
	}

	public static void main(String[] args) throws IOException {
		String ID_PREFIX = "ID";
		String file = "/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info.csv";
		
	    Dataset d = new Dataset(file);
	    SurfExcel.assignUnassignedIds(d.getRows(), "ID");
	    
	    HashMap<Row, String> rowToId = new HashMap<>();
	    HashMap<String, Row> idToRow = new HashMap<>();
	    Bihar.generateInitialIDMapper(d.getRows(),rowToId,idToRow);
	    
	    Multimap<String, Row> resultMap = Bihar.getExactSamePairs(d.getRows(),d);
	    
	    int i =0;
	    
	    display(resultMap,rowToId,idToRow);
	    
	    
	    String [] strs = {"76338","76689","76621"};
	    Bihar.merge(rowToId, idToRow, strs);
	    
	    
	    //testing purpose======================================
	    display(resultMap,rowToId,idToRow);
	    
	}

}
