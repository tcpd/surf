package in.edu.ashoka.surf;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import edu.tsinghua.dbgroup.EditDistanceClusterer;
import in.edu.ashoka.surf.util.Timers;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.io.IOException;

public class NewReviewAlgorithm extends MergeAlgorithm {

	private final String fieldName; // FieldName on which to Cosine Similarity
	private final Filter filter;

	NewReviewAlgorithm(Dataset dataset, String fieldName, Filter filter) {
		super(dataset);
		this.filter = filter;
		this.fieldName = fieldName;
	}

	@Override
	public List<Collection<Row>> run() {
		Collection<Row> filteredRows = filter.isEmpty() ? dataset.getRows()
				: dataset.getRows().stream().filter(filter::passes).collect(toList());

		SetMultimap<String, Row> fieldValueToRows = HashMultimap.create();
		filteredRows.forEach(r -> fieldValueToRows.put(r.get(fieldName), r));
//		filteredRows.forEach(r -> System.out.println(r.get(fieldName)));
		
        Timers.ReviewTimer.reset();
        Timers.ReviewTimer.start();
		
		HashMap<Integer, Collection<Row>> map = new HashMap<Integer, Collection<Row>>();

		for (Row row : filteredRows) {
			int ridval = Integer.parseInt(row.get("Rid"));
			if (ridval != 0) {
				if (map.containsKey(ridval) == false) {
					map.put(ridval, new ArrayList<Row>());
					map.get(ridval).add(row);
				} else {
					map.get(ridval).add(row);
				}
			}
		}
		
		classes = new ArrayList<>();

		for (Integer i : map.keySet()) {
			final Collection<Row> rowsForThisCluster = map.get(i);
			classes.add(rowsForThisCluster);
		}
		
        Timers.ReviewTimer.stop();
        Timers.log.info ("TimeTaken by New Review Algo: " + Timers.ReviewTimer.toString());

//		System.out.println("--------------------------------------------------------------------");

//		classes.forEach(x -> System.out.println(x.toString()));

		return classes;
	}

	/* debug method */
	void dumpClasses() {
		for (Collection<Row> rows : classes) {
			log.info(rows.iterator().next().get(fieldName));
		}
	}

	public String toString() {
		return "The new review algo works fine";
	}

}