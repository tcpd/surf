package in.edu.ashoka.surf;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            Multimap<Integer,Row> matchDistance = LinkedHashMultimap.create();
            Pattern pattern = Pattern.compile(searchQuery, Pattern.CASE_INSENSITIVE);
            String falttenedSearchQuery = searchQuery.replace("[^\\p{L}\\p{Z}]","");
            for(Row row:d.getRows()){
                Matcher matcher = pattern.matcher(StringUtils.normalizeSpace(row.get("Name")));
                if(matcher.find()||matcher.matches()){
                    //group.add(row);
                    matchDistance.put(StringUtils.getLevenshteinDistance(falttenedSearchQuery,row.get("Name")),row);
                }
            }

            Collection<Integer> distanceKeys = matchDistance.keySet();
            List<Integer> distanceList = new ArrayList<>(distanceKeys);
            distanceList.sort((o1,o2)->(o1-o2));
            Collection<Row> group = new ArrayList<>();
            for(int currentDistance:distanceList){
                group.addAll(matchDistance.get(currentDistance));
            }
            listOfSimilarCandidates.add(group);
        }
        algorithmRun = true;
    }
}
