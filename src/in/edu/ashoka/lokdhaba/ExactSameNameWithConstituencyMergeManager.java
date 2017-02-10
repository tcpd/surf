package in.edu.ashoka.lokdhaba;

import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sudx on 28/1/17.
 */
public class ExactSameNameWithConstituencyMergeManager extends MergeManager{

    boolean algorithmRun;

    public ExactSameNameWithConstituencyMergeManager(Dataset d) {
        super(d);
        algorithmRun = false;
    }

    @Override
    public void addSimilarCandidates() {
        if(algorithmRun)
            return;
        listOfSimilarCandidates = new ArrayList<>();
        try {
            int startStringSize = 1;    //DEFAULT SIZE OF 1
            if(super.arguments!=null && arguments[0]!=null && super.arguments[0] instanceof String){
                try{
                    startStringSize = Integer.parseInt(super.arguments[0]);
                } catch(NumberFormatException e){
                    startStringSize = 1;
                }

            }
            Multimap<String, Row> resultMap = Bihar.getExactSameNameWithConstituency(d.getRows(), d,startStringSize);
            for(String canonicalVal: resultMap.keySet()){
                listOfSimilarCandidates.add(resultMap.get(canonicalVal));
                for(Row row:resultMap.get(canonicalVal)){
                    row.set("common_group_id", row.get("_st_Name-PC_Name"));
                }
            }
            sortAlphabetically(listOfSimilarCandidates);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        algorithmRun=true;


    }
}
