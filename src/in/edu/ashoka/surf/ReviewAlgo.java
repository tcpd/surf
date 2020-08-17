package in.edu.ashoka.surf;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import edu.tsinghua.dbgroup.EditDistanceClusterer;
import in.edu.ashoka.surf.util.Timers;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.io.IOException;

public class ReviewAlgo extends MergeAlgorithm {

    private final String fieldName; //This Review Algorithm adds data to TBRfile System
    private final Filter filter;

    ReviewAlgo(Dataset dataset, String fieldName, Filter filter) {
        super (dataset);
        this.filter = filter;
        this.fieldName = fieldName;
    }

    @Override
    public List<Collection<Row>> run() {
		Collection<Row> filteredRows = filter.isEmpty() ? dataset.getRows() : dataset.getRows().stream().filter(filter::passes).collect(toList());

		SetMultimap<String, Row> fieldValueToRows = HashMultimap.create();
		filteredRows.forEach(r -> fieldValueToRows.put(r.get(fieldName), r));

//		System.out.println("------------------------------------------------------------------------------------");
//		System.out.println(filteredRows.size());
//		filteredRows.forEach(r -> System.out.println(r.alldata()));
//		filteredRows.forEach(r -> System.out.println(r.get(fieldName)));
//		System.out.println("------------------------------------------------------------------------------------");

		List<Set<String>> clusters = new ArrayList<Set<String>>();
		
        Timers.cosineTimer.reset();
        Timers.cosineTimer.start();
        
        ArrayList<String> namex = new ArrayList<>();
        filteredRows.forEach(r -> namex.add(r.get(fieldName)));
        
        int task = 1;
        Set<String> curr = null;
        for (int i = 0; i < namex.size(); i++) {
        	if(task == 1) {
        		curr = new LinkedHashSet<String>();
        	}
			if(namex.get(i).length() != 0) {
				curr.add(namex.get(i));
				task = 0;
			}
			else {
				task++;
        		if(i!=0 && task == 1) {
        			clusters.add(curr);
        		}
			}
		}
		
		classes = new ArrayList<>();
		for (Set<String> cluster : clusters) {
			final Collection<Row> rowsForThisCluster = new ArrayList<>();
			cluster.forEach(s -> {
				rowsForThisCluster.addAll(fieldValueToRows.get(s));
			});
			classes.add(rowsForThisCluster);
		}
        
        
        Timers.cosineTimer.stop();
        Timers.log.info ("Time for Review Algo: " + Timers.cosineTimer.toString());

		return classes;

    }

    /* debug method */
    void dumpClasses() {
        for (Collection<Row> rows: classes) {
            log.info (rows.iterator().next().get(fieldName));
        }
    }

    public String toString() { return "The Review Algo Works Fine";}
    
}
