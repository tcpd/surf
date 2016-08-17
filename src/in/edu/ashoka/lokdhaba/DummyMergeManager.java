package in.edu.ashoka.lokdhaba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class DummyMergeManager extends MergeManager{
	
	boolean algorithmRun;

	public DummyMergeManager(Dataset d) {
		super(d);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addSimilarCandidates() {
		if(algorithmRun)
			return;
		listOfSimilarCandidates = new ArrayList<>();
		Collection<Row> allRowsCollection = d.getRows();
		Collection<Row> sortedAllRowsCollection = new ArrayList<>();
		
		//remove nota
		List<Row> notaRows = new ArrayList<>();
		for(Row row:allRowsCollection){
			if(row.get("Name").toLowerCase().equals("none of the above"))
				notaRows.add(row);
		}
		for(Row row:notaRows){
			allRowsCollection.remove(row);
		}
		
		//sort alphabetically
		Queue<Row> priorityQ = new PriorityQueue<>(new Comparator<Row>() {

			@Override
			public int compare(Row o1, Row o2) {
				return o1.get("Name").compareTo(o2.get("Name"));
			}
		});
		priorityQ.addAll(allRowsCollection);
		
		while(!priorityQ.isEmpty()){
			sortedAllRowsCollection.add(priorityQ.remove());
		}
		listOfSimilarCandidates.add(sortedAllRowsCollection);
		algorithmRun = true;
		
	}

}
