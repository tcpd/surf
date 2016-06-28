package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MutableClassToInstanceMap;

public class ExactSameNameMergeManager extends MergeManager{
	
	
	
	public ExactSameNameMergeManager(Dataset d) {
		super(d);
		
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

	

	

	

	
	
	
	
}
