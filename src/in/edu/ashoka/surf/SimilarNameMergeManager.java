package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Multimap;

public class SimilarNameMergeManager extends MergeManager {
	
	int distance;
	boolean algorithmRun;
	
	public SimilarNameMergeManager(Dataset d, int distance) {
		super(d);
		this.distance = distance;
	}

	@Override
	public void addSimilarCandidates() {
		if(algorithmRun)
			return;
		listOfSimilarCandidates = new ArrayList<>();
		try {
			Multimap<String, Multimap<String, Row>> resultMap = Bihar.getSimilarPairs(d.getRows(), d, distance);
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
		algorithmRun = true;
	}
}
