package in.edu.ashoka.lokdhaba;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sudx on 6/4/17.
 */
public class SearchMergeManager extends MergeManager {
    boolean algorithmRun;
    String searchQuery;

    public SearchMergeManager(Dataset d, String search){
        super(d);
        algorithmRun = false;
        searchQuery = search;
    }

    @Override
    public void addSimilarCandidates(){
        if(algorithmRun)
            return;
        listOfSimilarCandidates = new ArrayList<>();
        if(searchQuery==null||searchQuery.equals(""))
        {}
        else{
            int thresholdDistance = 3;
            int weight =10;
            Multimap<Integer,Row> matchDistance = LinkedHashMultimap.create();
            String searchQueryFlattened = searchQuery.replace(" ","").toLowerCase();
            for(Row row:d.getRows()){
                String combined = "";
                combined += row.get("Name")+" "+row.get("Year")+" "+row.get("PC_name")+ " " + row.get("State");
                String nameFlattened = row.get("Name").replace(" ","").toLowerCase();
                if(nameFlattened.contains(searchQueryFlattened)){
                    matchDistance.put(StringUtils.getLevenshteinDistance(searchQueryFlattened,nameFlattened),row);
                    continue;
                }
                int distance =  StringUtils.getLevenshteinDistance(searchQueryFlattened,nameFlattened);
                if(distance < thresholdDistance){
                    matchDistance.put(distance*weight,row);
                }
            }
            Collection<Integer> distanceKeys = matchDistance.keySet();
            List<Integer> distanceList = new ArrayList<>(distanceKeys);
            distanceList.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1-o2;
                }
            });
            Collection<Row> group = new ArrayList<>();
            for(int currentDistance:distanceList){
                group.addAll(matchDistance.get(currentDistance));
            }
            listOfSimilarCandidates.add(group);
        }
        algorithmRun = true;
    }
}
