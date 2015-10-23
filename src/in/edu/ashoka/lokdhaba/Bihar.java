package in.edu.ashoka.lokdhaba;

import com.google.common.collect.Multimap;
import edu.stanford.muse.util.Util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by hangal on 9/17/15.
 */
public class Bihar {
    private static PrintStream out = System.out;
    private static String SEPARATOR = "========================================\n";

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

        Collection<Row> allRows = SurfExcel.readRows("Bihar.csv");
        Row.setToStringFields("Name-Sex-Year-AC_name-Party-Position-Votes");

        // terminology: name, cname (canonical name), tname (name after tokenization), stname (name after tokenization, with sorted tokens)

//        SurfExcel.assignIDs(allRows, "PC_name", "/Users/hangal/Downloads/control.pc-names.txt");
        SurfExcel.setColumnAlias(allRows, "Cand1", "Name");
        SurfExcel.setColumnAlias(allRows, "Sex1", "Sex");
        SurfExcel.setColumnAlias(allRows, "Party1", "Party");

        Display.displayPairs(allRows, SurfExcel.valuesUnderEditDistance(allRows, "AC_name", 2), "AC_name", 3 /* max rows */);
        SurfExcel.assign_unassignedIds(allRows, "AC_name");
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
            Collection<Row> winners = SurfExcel.select(allRows, "Position", "1");
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
            out.println(SEPARATOR + " Check if there at least 2 candidates for every Year-AC");
            Display.display(SurfExcel.filter(SurfExcel.split(allRows, "Year-AC_name"), "max", 1));

            out.println(SEPARATOR + " Look for possible misspellings in constituency name");
            Display.displayPairs(allRows, SurfExcel.valuesUnderEditDistance(allRows, "AC_name", 1), "AC_name", 3 /* max rows */);

            out.println(SEPARATOR + " Look for possible misspellings in Party");
            Display.displayPairs(nonIndependents, SurfExcel.valuesUnderEditDistance(allRows, "Party", 1), "Party", 3 /* max rows */);

            Tokenizer.setupVersions(allRows, "Name");

            // given a st_name, does it uniquely determine the sex?
            out.println(SEPARATOR + " Checking if each (C-R-S) name belongs to exactly one sex");
            Display.display2Level(SurfExcel.filter(SurfExcel.split(SurfExcel.split(allRows, "_st_Name"), "Sex"), "min", 2), 3 /* max rows */, false);

            Tokenizer.setupVersions(allRows, "PC_name");
            out.println(SEPARATOR + " Look for similar ACs");
            Display.display2Level(SurfExcel.reportSimilarValuesForField(allRows, "AC_name"), 3, false);

            out.println(SEPARATOR + " Looking for similar names");
            Display.display2Level(SurfExcel.reportSimilarValuesForField(allRows, "Name"), 3, false);

            /*
            out.println(SEPARATOR + "Similar names (ST edit distance = 1)");
            Display.displayPairs(allRows, similarPairsForField(allRows, "Name", 2), "_st_Name", 3, false);
            */
            out.println(SEPARATOR + "New attempt: Similar names (ST edit distance = 1)");
            SurfExcel.similarPairsForField(allRows, "Name", 1);
            Display.display2Level (SurfExcel.sort(SurfExcel.filter(SurfExcel.split(SurfExcel.split(allRows, "_est_Name"), "Name"), "min", 2), SurfExcel.stringLengthComparator), 3);
        }
    }
}
