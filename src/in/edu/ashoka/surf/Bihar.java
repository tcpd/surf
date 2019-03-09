package in.edu.ashoka.surf;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import in.edu.ashoka.surf.util.Pair;
import in.edu.ashoka.surf.util.Util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by hangal on 9/17/15.
 */
class Bihar {
    private static final PrintStream out = System.out;
    private static final String SEPARATOR = "========================================\n";

    public static void main(String[] args) throws IOException {
        /*
        read bihar.csv
        print rows as name - sex - year - ac_name - party - position - votes
        call cand1 name
        call sex1 sex
        call party party
        call AC_name constituency
        call constituency seat
        show profile of ac_names
        show constituencies with a similar name
        look for misspellings in constituency
        show people with the same name who contested the same constituency in the same year
        check that there's at most 1 winner per constituency per year
        check at least 1 winner per constituency per year
        check if each name belongs to exactly one sex
        */

//        Dataset d = new Dataset("/Users/hangal/workspace/surf/AE/State_Mastersheets/Bihar/Bihar_Mastersheet.csv");
         Dataset d = Dataset.getDataset("/home/sudx/surf.java/surf/GE/candidates/csv/candidates_info.csv");
        Collection<Row> allRows = d.rows;

        Row.setToStringFields("Name-Sex-Year-AC_name-Party-Position-Votes");
//        d.registerColumnAlias("Cand1", "Name");
        d.registerColumnAlias("Candidate_name", "Name");
//        d.registerColumnAlias("Sex1", "Sex");
        d.registerColumnAlias("Candidate_sex", "Sex");
//        d.registerColumnAlias("Party1", "Party");
        d.registerColumnAlias("Party_abbreviation", "Party");

        Tokenizer.setupDesiVersions(allRows, "AC_name");
        Tokenizer.setupDesiVersions(allRows, "Name");

        // terminology: name, cname (canonical name), tname (name after tokenization), stname (name after tokenization, with sorted tokens)

        // look for incumbents
        Collection<Row> winners = SurfExcel.select(allRows, "Position", "1");
        Collection<Row> candidates = SurfExcel.select(allRows, "Year", "2015");

        String stField = "_st_" + "Name";

        /*
        Multimap<String, Row> candidates2015ByName = SurfExcel.split(candidates, stField);

        for (String year: new String[]{"2010", "2005.5", "2005", "2000", "1995", "1990", "1985", "1980", "1977", "1972", "1969", "1967", "1962"}) {
            Collection<Row> winnersYear = SurfExcel.select(winners, "Year", year);
            Multimap<String, Row> winnersYearByName = SurfExcel.split(winnersYear, stField);
            List<Pair<String, String>> matches = SurfExcel.desiMatch2Lists(winnersYearByName.keySet(), candidates2015ByName.keySet(), 1);
            out.println (SEPARATOR + "Comparing Winners-" + year + " with Candidates-2015 (" + matches.size() + " possible matches)");
            Display.displayListDiff(matches, winnersYearByName, candidates2015ByName, "Winner-" + year, "Candidates-2015");
        }

        Multimap<String, Row> allWinnersByName = SurfExcel.split(winners, stField);
        List<Pair<String, String>> matches = SurfExcel.desiMatch2Lists(candidates2015ByName.keySet(), allWinnersByName.keySet(), 1);
        out.println (SEPARATOR + "Comparing Candidates-2015 with all past winners (" + matches.size() + " possible matches)");
        Display.displayListDiff(matches, candidates2015ByName, allWinnersByName, "Candidates-2015", "Past-winner");
        */

        out.println (SEPARATOR + " Checking for ac_names with same canonicalized value");
        Display.displaySimilarValuesForField(allRows, "AC_name", 2, 3 /* max rows */);
        //SurfExcel.assignUnassignedIds(allRows, "AC_name");
        SurfExcel.profile(allRows, "AC_name");

        // perform some consistency checks
        {
            // check: {Name, year, PC} unique?
            // if not, it means multiple people with the same name are contesting in the same PC in the same year -- not impossible.
            out.println(SEPARATOR + " Checking if multiple candidates with the same name contested the same constituency in the same year");
            Multimap<String, Row> map = SurfExcel.split(allRows, "Name-Year-AC_name");
            Display.display(SurfExcel.filter(map, "notequals", 1));

            {
                out.println(SEPARATOR + " Checking AC_names across successive years");
                Multimap<String, Row> yearMap = SurfExcel.split(allRows, "Year");
                List<String> years = new ArrayList<>(yearMap.keySet());
                Collections.sort(years);
                if (years.size() >= 2) {
                    for (int i = 0; i < years.size() - 2; i++) {
                        String year_i = years.get(i);
                        String year_j = years.get(i+1);
                        if (Util.nullOrEmpty(year_i) || Util.nullOrEmpty((year_j)))
                            continue;
                        Collection<Row> rows_i = yearMap.get(year_i);
                        Collection<Row> rows_j = yearMap.get(year_j);
                        Set<String> acs_i = SurfExcel.split(rows_i, "AC_name").keySet(), acs_j = SurfExcel.split(rows_j, "AC_name").keySet();
                        Display.displayDiffs("year " + year_i, acs_i, "year " + year_j, acs_j);
                        out.println ("\n------\n");
                    }
                }
            }

            out.println(SEPARATOR + " Check that there is at most 1 winner per seat per year");
            Multimap<String, Row> winnersMap = SurfExcel.split(winners, "Year-AC_name");
            Display.display(SurfExcel.filter(winnersMap, "notequals", 1));

            out.println(SEPARATOR + " Check that there is at least 1 winner per seat per year");
            Set<String> winnerKeys = winnersMap.keySet();
            map = SurfExcel.split(allRows, "Year-AC_name");
            map = SurfExcel.minus(map, winnerKeys);
            if (map.size() > 0) {
                out.println(" The following elections do not have a winner!?!");
                Display.display(map);
            }

            Collection<Row> selectedRows = SurfExcel.selectNot(allRows, "Name", "NONE OF THE ABOVE");
            selectedRows = SurfExcel.selectNot(selectedRows, "Sex", "F");
            selectedRows = SurfExcel.selectNot(selectedRows, "Sex", "M");
            out.println(SEPARATOR + " Checking values in \"Sex\" fields other than M and F");
            Display.display("", selectedRows, Integer.MAX_VALUE);

            // check: among the non-independents, is {year, PC, Party} unique?
            // if not, it means same party has multiple candidates in the same year in the same PC!!
            out.println(SEPARATOR + " Checking uniqueness of Year-AC-Party (non-independents)");
            Collection<Row> nonIndependents = SurfExcel.selectNot(allRows, "Party", "IND");
            Display.display(SurfExcel.filter(SurfExcel.split(nonIndependents, "Year-AC_name-Party"), "notequals", 1));

            // Check if every <year, PC> has at least 2 unique rows (otherwise its a walkover!)
            out.println(SEPARATOR + " Check if there are at least 2 candidates for every Year-AC");
            Display.display(SurfExcel.filter(SurfExcel.split(allRows, "Year-AC_name"), "max", 1));

            out.println(SEPARATOR + " Look for possible misspellings in constituency name");
            Display.displayPairs(allRows, SurfExcel.valuesUnderEditDistance(allRows, "AC_name", 1), "AC_name", 3 /* max rows */);

            out.println(SEPARATOR + " Look for possible misspellings in Party");
            Display.displayPairs(nonIndependents, SurfExcel.valuesUnderEditDistance(nonIndependents, "Party", 1), "Party", 3 /* max rows */);

            out.println(SEPARATOR + " Look for similar ACs");
            Display.display2Level(SurfExcel.reportSimilarDesiValuesForField(allRows, "AC_name"), 3, false);

            out.println(SEPARATOR + " Looking for similar names");
            Display.display2Level(SurfExcel.reportSimilarDesiValuesForField(allRows, "Name"), 3, false);

            // given a st_name, does it uniquely determine the sex? set up desi versions must have been done before this!
            out.println(SEPARATOR + " Checking if each (C-R-S) name belongs to exactly one sex");
            Display.display2Level(SurfExcel.filter(SurfExcel.split(SurfExcel.split(allRows, "_st_Name"), "Sex"), "min", 2), 3 /* max rows */, false);

            /*
                        out.println(SEPARATOR + "Similar names (ST edit distance = 1)");
            Display.displayPairs(allRows, similarPairsForField(allRows, "Name", 2), "_st_Name", 3, false);
            */

            Collection<Row> mainCandidates = SurfExcel.filter (allRows, "Position", "1");
            mainCandidates.addAll(SurfExcel.filter (allRows, "Position", "2"));
            mainCandidates.addAll(SurfExcel.filter (allRows, "Position", "3"));

            out.println(SEPARATOR + "New attempt: Similar names (ST edit distance = 1)");
            SurfExcel.similarPairsForField(allRows, "Name", 1);
            Display.display2Level (SurfExcel.sort(SurfExcel.filter(SurfExcel.split(SurfExcel.split(allRows, "_est_Name"), "Name"), "min", 2), SurfExcel.stringLengthComparator), 3);
            Multimap<String, Multimap<String, Row>> resultMap = SurfExcel.sort(SurfExcel.filter(SurfExcel.split(SurfExcel.split(allRows, "_est_Name"), "Name"), "min", 2), SurfExcel.stringLengthComparator);
            
            //for testing purpose
            /*Multimap<String, Multimap<String, Row>> mappedNames = getSimilarPairs("/home/sudx/surf.java/surf/GE/candidates/csv/candidates_info.csv");
            NameData nameData = new ConcreteNameData();
            nameData.initialize();
            nameData.iterateNameData();
            Iterator iterator = nameData.iterator();
            while(iterator.hasNext()) {
            	NamePair np = (NamePair)iterator.next();
            	out.println(np.getName1().get("Candidate_name"));
                out.println(np.getName2().get("Candidate_name"));
            }*/
            
            //Multimap<String,String> mappedNames = getExactSamePairs("/home/sudx/surf.java/surf/GE/candidates/csv/candidates_info.csv");
            //System.out.println();




        }

    }

    /** returns a map:
     *  canonical name -> set of {multimap for that canonical name}
     *  canonical name: ab ai bh bh da el hai hy la pa va (2 values)
     *  this cname maps to a bunch of real names, e.g.
        dahyabhai vallabhbhai patel
        patel dahyabhai vallabhbhai
     each of these real names maps to a set of rows that have that real name
        e.g. the first name maps to 2 rows:
     43.1.) dahyabhai vallabhbhai patel ->
     43.1.1.1) dahyabhai vallabhbhai patel-m-1998--ind-2- (row# 31551)
     43.1.1.2) dahyabhai vallabhbhai patel-m-2009--inc-2- (row# 16667)

        the second name maps to 2 rows:
     43.2.) patel dahyabhai vallabhbhai
     43.2.1.1) patel dahyabhai vallabhbhai-m-1999--inc-1- (row# 26834)
     43.2.1.2) patel dahyabhai vallabhbhai-m-2004--inc-1- (row# 22143)

     * @param file
     * @return
     * @throws IOException
     */
    public static Multimap<String, Multimap<String, Row>> getSimilarPairs (String file) throws IOException {
        Dataset d = Dataset.getDataset(file);
        Collection<Row> allRows = d.rows;
        Row.setToStringFields("Name-Sex-Year-AC_name-Party-Position-Votes");
//        d.registerColumnAlias("Cand1", "Name");
        d.registerColumnAlias("Candidate_name", "Name");
//        d.registerColumnAlias("Sex1", "Sex");
        d.registerColumnAlias("Candidate_sex", "Sex");
//        d.registerColumnAlias("Party1", "Party");
        d.registerColumnAlias("Party_abbreviation", "Party");

        Tokenizer.setupDesiVersions(allRows, "Name");

        Collection<Row> mainCandidates = SurfExcel.filter (allRows, "Position", "1");
        mainCandidates.addAll(SurfExcel.filter (allRows, "Position", "2"));
        mainCandidates.addAll(SurfExcel.filter (allRows, "Position", "3"));

        out.println(SEPARATOR + "New attempt: Similar names (ST edit distance = 1)");
        SurfExcel.similarPairsForField(allRows, "Name", 1);
        Display.display2Level (SurfExcel.sort(SurfExcel.filter(SurfExcel.split(SurfExcel.split(allRows, "_est_Name"), "Name"), "min", 2), SurfExcel.stringLengthComparator), 3);
        Multimap<String, Multimap<String, Row>> resultMap = SurfExcel.sort(SurfExcel.filter(SurfExcel.split(SurfExcel.split(allRows, "_est_Name"), "Name"), "min", 2), SurfExcel.stringLengthComparator);
        
        
        return resultMap;
    }
    
    public static Pair<Row, Row> getSimilarPairs (Collection<Row> allRows, Dataset d, int distance) {
        
        out.println(SEPARATOR + "New attempt: Similar names (ST edit distance = "+distance+")");
        return null; //  SurfExcel.similarPairsForField(allRows, "Name", distance);
    }

    /** return canonical name -> {ids that map to that canonical name) */
    public static Multimap<String, Row> getExactSamePairs (String file) throws IOException {
    	Dataset d = Dataset.getDataset(file);
        return getExactSamePairs(d.rows,d);
        
    }
    
    public static void initRowFormat(Collection<Row> allRows, Dataset d) {
    	//set ups what toString() of Row needs to print
		Row.setToStringFields("Name-Sex-Year-PC_name-Party-State-Position-Votes1-ID");
		
		//creates aliases for column name
		//file specific aliases
		d.registerColumnAlias("Cand1", "Candidate_name");
		d.registerColumnAlias("AC_name", "PC_Name");
        d.registerColumnAlias("Constituency", "PC_Name");
		d.registerColumnAlias("Sex1", "Candidate_sex");
		d.registerColumnAlias("Party1", "Party_abbreviation");
		d.registerColumnAlias("AC_No", "PC_number");
		d.registerColumnAlias("AC_Type", "PC_type");
		
		d.registerColumnAlias("Candidate_name", "Name");
		d.registerColumnAlias("Candidate_sex", "Sex");
		d.registerColumnAlias("Party_abbreviation", "Party");
		d.registerColumnAlias("State_name", "State");
		
		//creates canonical tokens; adds them to the row
		Tokenizer.setupDesiVersions(allRows, "PC_name");
        Tokenizer.setupDesiVersions(allRows, "Name");
    }
    
    private static Multimap<String, Row> getExactSamePairs(Collection<Row> allRows, Dataset d) {
    	
        
    		//initRowFormat(allRows, d);
	        
	        //create multimap for pairs
	        Multimap<String, Row> resultMap = LinkedHashMultimap.create();
	        
	        for(Row row:allRows){
	        	row.set("common_group_id", row.get("_st_Name"));
	        	resultMap.put(row.get("_st_Name"), row);
	        }
	        
	        //only keep duplicates
	        /*List<String> list = new ArrayList<String>();
	        for(String key:resultMap.keySet()){
	        	if(resultMap.get(key).size()<2)
	        		list.add(key);
	        }
	        for(String key:list){
	        	resultMap.asMap().remove(key);
	        }*/
	        
	        //remove nota
	        resultMap.asMap().remove("ab he ne no of ove");
	        resultMap.asMap().remove("AB NONE OF OWE TE");
	        
	        //remove groups where position is below 5==== EXPERIMENTAL
	        
	        /*list.clear();
	        for(String key:resultMap.keySet()){
	        	Collection<Row> temp = resultMap.get(key);
	        	boolean flag=true;
	        	for(Row row:temp){
	        		if((!row.get("Position").equals("NA"))&& Integer.valueOf(row.get("Position"))>5){
	        			flag=false;
	        		}
	        	}
	        	if(!flag)
	        		list.add(key);
	        }
	        for(String key:list){
	        	resultMap.asMap().remove(key);
	        }
	        */
	        
			//test print
			/*for(String key:resultMap.keySet()) {
				for(Row row:resultMap.get(key)){
					System.out.println(row);
				}
			}*/
	        
	    System.out.println("Group of Exact Same Names found: "+ resultMap.keySet().size());
	    int listSize = 0;
	    for(String key:resultMap.keySet()){
	    	listSize+=resultMap.get(key).size();
	    }
	    System.out.println("list size: "+listSize);
	    

        return resultMap;
    }

    public static Multimap<String, Row> getExactSameNameWithConstituency (Collection<Row> allRows, Dataset d, int startStringSize) {
        //split based on cname and constituency
        Multimap<String, Row> tempMap = SurfExcel.split(allRows, "PC_Name");
        Multimap<String, Row> resultMap = HashMultimap.create();
        List<String> listCField = new ArrayList<>(tempMap.keySet());
        listCField.sort(SurfExcel.stringLengthComparator);
        listCField = Collections.unmodifiableList(listCField);
        for (String aListCField : listCField) {
            Collection<Row> constituencyGroup = tempMap.get(aListCField);
            for (Row row : constituencyGroup) {
                if (row.get("c" + SurfExcel.FIELDSPEC_SEPARATOR + "Name").length() >= startStringSize) {
                    resultMap.put(row.get("PC_Name") + SurfExcel.FIELDSPEC_SEPARATOR + row.get("c" + SurfExcel.FIELDSPEC_SEPARATOR + "Name").substring(0, startStringSize), row);
                } else {
                    resultMap.put(row.get("PC_Name") + SurfExcel.FIELDSPEC_SEPARATOR + row.get("c" + SurfExcel.FIELDSPEC_SEPARATOR + "Name"), row);
                }
            }
        }

        //only keep duplicates
        /*List<String> list = new ArrayList<String>();
        for(String key:resultMap.keySet()){
            if(resultMap.get(key).size()<2)
                list.add(key);
        }
        for(String key:list){
            resultMap.asMap().remove(key);
        }*/

        //remove nota
        resultMap.asMap().remove("ab he ne no of ove");
        resultMap.asMap().remove("AB NONE OF OWE TE");

        return resultMap;

    }

    public static void merge(HashMap<Row, String> rowToId, HashMap<String, Row> idToRow, String []ids) {
    	String defaultId = ids[0];
    	for(int i=1;i<ids.length;i++) {
    		Row tempRow = idToRow.get(ids[i]);
    		rowToId.put(tempRow, defaultId);
    	}
    }
    
    
    /* what the jsp has to do:
        Row.setToStringFields("Name-Sex-Year-AC_name-Party-Position-Votes");
        Dataset d = new Dataset(file);
        d.registerColumnAlias("Candidate_name", "Name");
        d.registerColumnAlias("Candidate_sex", "Sex");
        d.registerColumnAlias("Party_abbreviation", "Party");
        Multimap<String, String> resultMap = getExactSamePairs(d.rows);

        Multimap<String, Row> idToRows = SurfExcel.split (d.rows, ID_PREFIX + "Candidate_name");

        for (String canonicalVal: resultMap.keySet() {
             Collection<String> idsForThisCVal = resultMap.get(canonicalVal);
             // UI should allow for merging between any 2 of these ids.
             for (String id: idsForThisCVal) {
                 String id = p.getSecond();
                 List<Row> rowsForThisId = idToRows.get(id);
                 // now print these rows in one box -- its one cohesive unit, which cannot be broken down.
             }
        }

        List<Pair<String, String>> mergedIds = ....
        mergeIds(mergedIds); 

     */
       
}
