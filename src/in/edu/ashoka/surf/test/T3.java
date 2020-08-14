package in.edu.ashoka.surf.test;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import in.edu.ashoka.surf.Dataset;
import in.edu.ashoka.surf.Row;

class T3 {

	static final String path = "/Users/priyamgarrg21/Documents/Aditya/Internship@Ashoka/TCPD_GE_Delhi_2020-6-18.csv";

	public static void main(String args[]) throws IOException {
		Dataset dataset = Dataset.getDataset(path);
		String fieldName = "Candidate";
		int maxEditDistance = 5;
		List<Collection<Row>> classes;

		cos_sample mainfunc = new cos_sample();

//		System.out.println(mainfunc.cosine_similarity(mainfunc.word2vec("adity a"), mainfunc.word2vec("aditya xs")));

		Collection<Row> filteredRows = dataset.getRows().stream().collect(toList());

		SetMultimap<String, Row> fieldValueToRows = HashMultimap.create();
		filteredRows.forEach(r -> fieldValueToRows.put(r.get(fieldName), r));
		
//		Iterator<Row> iterator = filteredRows.iterator();
		
//		while(iterator.hasNext()) {
//			System.out.println(iterator.next().get(fieldName));
//		}
		
//		filteredRows.forEach(r -> System.out.println(r.get(fieldName)));

		System.out.println("--------------------------------------------------------------------");

		List<Set<String>> clusters = mainfunc.assign_similarity(filteredRows, fieldName);
		
//		for (Row fil : filteredRows) {
////			System.out.println(fil.get(fieldName));
//			Set setx = new LinkedHashSet<String>();
//			setx.add(fil.get(fieldName));
//			clusters.add(setx);
//		}
		
		int key = 0;
		classes = new ArrayList<>();
		for (Set<String> cluster : clusters) {
			System.out.println(key++ + " " + cluster);
			final Collection<Row> rowsForThisCluster = new ArrayList<>();
			cluster.forEach(s -> {
				rowsForThisCluster.addAll(fieldValueToRows.get(s));
			});
			classes.add(rowsForThisCluster);
		}

		System.out.println("--------------------------------------------------------------------");

		classes.forEach(x -> System.out.println(x.toString()));

	}
}
