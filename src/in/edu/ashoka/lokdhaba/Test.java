package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multimap;

public class Test {

	public static void main(String[] args) throws IOException {
		String ID_PREFIX = "ID";
		String file = "/home/sudx/lokdhaba.java/lokdhaba/GE/candidates/csv/candidates_info.csv";
		
	    Dataset d = new Dataset(file);
	    
	    Multimap<String, Row> resultMap = Bihar.getExactSamePairs(d.getRows(),d);

	    
	    int i =0;
	    //List<Row> rowsTobeDisplayed = new ArrayList<Row>();
	    for (String canonicalVal: resultMap.keySet()) {
	         Collection<Row> idsForThisCVal = resultMap.get(canonicalVal);
	         
	         // UI should allow for merging between any 2 of these ids.
	         for (Row row: idsForThisCVal) {
	        		 
	            	 System.out.println(row.toString());
	        	 }
	             // now print these rows in one box -- its one cohesive unit, which cannot be broken down.
	        	 
	             
	             
	             
	         }
	}

}
