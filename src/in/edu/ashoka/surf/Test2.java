package in.edu.ashoka.surf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

class Test2 {

	public static void main(String[] args) throws IOException {
		Dataset d = Dataset.getDataset("/Users/priyamgarrg21/Documents/Aditya/EX/TCPD_GE_Delhi_2020-6-18.csv");
		Collection<Row> rows = d.rows;
		
		//set ups what toString() of Row needs to print
		Row.setToStringFields("Name-Sex-Year-PC_name-Party-Position-Votes");
		
		//creates aliases for column name
		d.registerColumnAlias("Candidate_name", "Name");
		d.registerColumnAlias("Candidate_sex", "Sex");
		d.registerColumnAlias("Party_abbreviation", "Party");
		
		//creates canonical tokens; adds them to the row
		Tokenizer.setupDesiVersions(rows, "PC_name");
        Tokenizer.setupDesiVersions(rows, "Name");
        
        //create multimap for pairs
        Multimap<String, Row> resultMap = LinkedHashMultimap.create();
        
        for(Row row:rows){
        	resultMap.put(row.get("_st_Name"), row);
        }
        
        //only keep duplicates
        List<String> list = new ArrayList<>();
        for(String key:resultMap.keySet()){
        	if(resultMap.get(key).size()<2)
        		list.add(key);
        }
        for(String key:list){
        	resultMap.asMap().remove(key);
        }
        
		//test print
		for(String key:resultMap.keySet()) {
			for(Row row:resultMap.get(key)){
				System.out.println(row);
			}
		}
	}

}
