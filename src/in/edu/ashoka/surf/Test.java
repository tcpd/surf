package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

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
		test2();
	    
	}
	
	public static void test3() throws IOException {
		String ID_PREFIX = "ID";
		String file = "/home/sudx/surf.java/surf/GE/candidates/csv/candidates_info.csv";
		
	    Dataset d = Dataset.getDataset(file);
	    MergeManager mergeManager = new SimilarNameMergeManager(d,1);
	    mergeManager.initializeIds();
	    mergeManager.performInitialMapping();
	    mergeManager.addSimilarCandidates();
//	    ((SimilarNameMergeManager)mergeManager).display2();
	    //mergeManager.merge(new String[]{"76174","76338","76689","76621"});
	    //((JspMergeManager)mergeManager).display2();
	}
	
	public static void test2() throws IOException {
		String ID_PREFIX = "ID";
		String file = "/home/sudx/surf.java/surf/GE/candidates/csv/candidates_info.csv";
		
	    Dataset d = Dataset.getDataset(file);
	    MergeManager mergeManager = new ExactSameNameMergeManager(d);
	    mergeManager.initializeIds();
	    mergeManager.performInitialMapping();
	    mergeManager.addSimilarCandidates();
	    mergeManager.merge(new String[]{"76174","76338","76689","76621"});
	    ((ExactSameNameMergeManager)mergeManager).display();
	}
	
	public static void test1() throws IOException{
		String ID_PREFIX = "ID";
		String file = "/home/sudx/surf.java/surf/GE/candidates/csv/candidates_info.csv";
		
	    Dataset d = Dataset.getDataset(file);
	    SurfExcel.assignUnassignedIds(d.getRows(), "ID");
	    
	    HashMap<Row, String> rowToId = new HashMap<>();
	    HashMap<String, Row> idToRow = new HashMap<>();
	    //Bihar.generateInitialIDMapper(d.getRows(),rowToId,idToRow);
	    
	    Multimap<String, Row> resultMap = Bihar.getExactSamePairs(d.getRows(),d);
	    
	    int i =0;
	    
	    display(resultMap,rowToId,idToRow);
	    
	    
	    String [] strs = {"76338","76689","76621"};
	    Bihar.merge(rowToId, idToRow, strs);
	    
	    
	    //testing purpose======================================
	    display(resultMap,rowToId,idToRow);
	}

}
