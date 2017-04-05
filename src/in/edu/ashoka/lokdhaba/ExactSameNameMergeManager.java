package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.sun.scenario.effect.Merge;

public class ExactSameNameMergeManager extends MergeManager{
	
	boolean algorithmRun;
	
	public ExactSameNameMergeManager(Dataset d) {
		super(d);
		algorithmRun = false;
	}

	

	@Override
	public void addSimilarCandidates() {
		if(algorithmRun)
			return;
		listOfSimilarCandidates = new ArrayList<>();
		try {
			Multimap<String, Row> resultMap = Bihar.getExactSamePairs(d.getRows(), d);
			for(String canonicalVal: resultMap.keySet()){
				listOfSimilarCandidates.add(resultMap.get(canonicalVal));
				for(Row row:resultMap.get(canonicalVal)){
					row.set("common_group_id", row.get("_st_Name"));
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		algorithmRun=true;
		
		
	}
	//testing methods
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
}
