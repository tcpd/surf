package in.edu.ashoka.lokdhaba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Multimap;

public class SimilarNameMergeManager extends MergeManager {

	public SimilarNameMergeManager(Dataset d) {
		super(d);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addSimilarCandidates() {
		listOfSimilarCandidates = new ArrayList<>();
		try {
			Multimap<String, Multimap<String, Row>> resultMap = Bihar.getSimilarPairs(d.getRows(), d);
			for(String outerKey:resultMap.keySet()){
				Collection<Row> temp = new ArrayList<>();
				Collection<Multimap<String, Row>> similarNamesMap = resultMap.get(outerKey);
				for(Multimap<String, Row> map:similarNamesMap){
					for(String innerKey:map.keySet()){
						for(Row row:map.get(innerKey)){
							temp.add(row);
							row.set("common_group_id", outerKey);
						}
					}
				}
				listOfSimilarCandidates.add(temp);
			}
			
		}catch(IOException e){
			e.printStackTrace();
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
