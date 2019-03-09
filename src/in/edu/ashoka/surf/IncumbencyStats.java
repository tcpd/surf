package in.edu.ashoka.surf;

import com.google.common.collect.Multimap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class MatchPredicate {
    private final String matchValue;
    private final String field;
    MatchPredicate(String field, String value){
        this.matchValue = value;
        this.field = field;
    }

    public boolean matches(Row r){
        return matchValue.equals(r.get(field));
    }
}

class IncumbencyStats {
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

    private static final PrintStream out = System.out;

    private static final Predicate<Row> winners_filter = x -> "1".equals (x.get("Position"));
    private static final Predicate<Row> non_winners_filter = x -> !"1".equals (x.get("Position"));
    private static Multimap<String, Row> pid_to_rows;
    public static void doPartyStuff (Dataset d, int prev_assembly_no) throws IOException {
        MatchPredicate prevAssemblyFilter = new MatchPredicate("Assembly_No", Integer.toString(prev_assembly_no));
        MatchPredicate thisAssemblyFilter = new MatchPredicate("Assembly_No", Integer.toString(prev_assembly_no+1));

        Writer fileWriter1 = new FileWriter("retained_seats.csv");
        CSVPrinter retainedSeats = new CSVPrinter(fileWriter1,csvFileFormat.withQuoteMode(QuoteMode.NON_NUMERIC));

        Writer fileWriter2 = new FileWriter("flipped_seats.csv");
        CSVPrinter flippedSeats = new CSVPrinter(fileWriter2,csvFileFormat.withQuoteMode(QuoteMode.NON_NUMERIC));

        String[] colNames = new String[]{"AC_no", "Constituency_Name", "Party_9", "Pid_9", "Winner_9", "Party_10", "Pid_10", "Winner_10", "Same_pid", "Flip"};

        List<String> columnList = Arrays.asList(colNames);
        retainedSeats.printRecord(columnList);
        flippedSeats.printRecord(columnList);

        List<Row> rows = new ArrayList<>(d.rows);
        List<Row> all_winners = rows.stream().filter(winners_filter).collect(Collectors.toList());
        List<Row> winners_prev = all_winners.stream().filter(prevAssemblyFilter::matches).collect(Collectors.toList());
        List<Row> winners_this = all_winners.stream().filter(thisAssemblyFilter::matches).collect(Collectors.toList());

        List<Row> bothWinners = new ArrayList<>();
        bothWinners.addAll (winners_prev);
        bothWinners.addAll (winners_this);

        for (int ac = 1; ac <= 224; ac++) {
            MatchPredicate acpred = new MatchPredicate("Constituency_No", Integer.toString(ac));

            List<Row> ac_rows_prev = winners_this.stream().filter (acpred::matches).collect(Collectors.toList());
            List<Row> ac_rows_this = winners_this.stream().filter (acpred::matches).collect(Collectors.toList());

            bothWinners.stream().filter (acpred::matches).collect(Collectors.toList()).iterator().next();

            if (ac_rows_prev.size() < 1 || ac_rows_this.size() < 1) {
                Row r = bothWinners.stream().filter (acpred::matches).collect(Collectors.toList()).iterator().next();
                out.println ("something is funny with ac " + ac + " = " + r.get("Constituency_Name") + ", skipping. "  + ac_rows_prev.size() +  " "  + ac_rows_this.size());
                continue;
            }

            try {
                Row prev_winner = ac_rows_prev.get(ac_rows_prev.size()-1); // get the last winner
                Row this_winner = ac_rows_this.get(ac_rows_this.size()-1); // get the last winner

                String party_prev = prev_winner.get("Party");
                String party_this = this_winner.get("Party");
                String prev_pid = prev_winner.get("pid");
                String this_pid = this_winner.get("pid");

                List<String> record = new ArrayList<>();
                record.add (Integer.toString(ac));
                record.add (prev_winner.get("Constituency_Name"));
                record.add (prev_winner.get("Party"));
                record.add (prev_pid);
                record.add (prev_winner.get("Candidate"));

                record.add (this_winner.get("Party"));
                record.add (this_pid);
                record.add (this_winner.get("Candidate"));

                boolean same_pid = prev_pid.equals(this_pid);
                record.add (Boolean.toString(same_pid));
                record.add (party_prev + "->" + party_this);

                if (party_prev.equals(party_this)) {
                    retainedSeats.printRecord (record);
                } else {
                    flippedSeats.printRecord (record);
                }
            } catch (Exception e) {
                System.out.println ("Same party");
            }
        }

        flippedSeats.close();
        retainedSeats.close();
    }

    private static void computeIncumbentStatsForAssembly (Dataset d, int prev_assembly_no) throws IOException {

        MatchPredicate prevAssemblyFilter = new MatchPredicate("Assembly_No", Integer.toString(prev_assembly_no));
        MatchPredicate thisAssemblyFilter = new MatchPredicate("Assembly_No", Integer.toString(prev_assembly_no+1));

        List<Row> rows = new ArrayList<>(d.rows);
        Writer fileWriter2 = new FileWriter("lost_mlas.csv");
        CSVPrinter lostMLAs = new CSVPrinter(fileWriter2,csvFileFormat.withQuoteMode(QuoteMode.NON_NUMERIC));

        String[] colNames = new String[]{"pid", "Name", "AC_no_9", "Constituency_Name_9", "Party_9", "Const_10", "Party_10"};
        List<String> columnList = Arrays.asList(colNames);
        lostMLAs.printRecord(columnList);

        List<Row> all_winners = rows.stream().filter(winners_filter).collect(Collectors.toList());

        List<Row> winners_prev = all_winners.stream().filter(prevAssemblyFilter::matches).collect(Collectors.toList());

        int max_ac = winners_prev.stream().map (x -> Integer.parseInt(x.get("Constituency_No"))).reduce(-1, Integer::max);
        if (max_ac == -1) {
            out.println ("Sorry! max AC_no is " + max_ac + " in assembly # " + prev_assembly_no);
            return;
        }

        List<Row> non_winners_this = rows.stream().filter(thisAssemblyFilter::matches).filter(non_winners_filter).collect(Collectors.toList());
        Multimap<String, Row> pid_to_non_winners_this = SurfExcel.split(non_winners_this, "pid");
        Multimap<String, Row> pid_to_this = SurfExcel.split(rows.stream().filter(thisAssemblyFilter::matches).collect(Collectors.toList()), "pid");

        Multimap <String, Row> acToRows = SurfExcel.split (winners_prev, "Constituency_No");
        int count = 0;

        out.println ("Checking " + acToRows.keySet().size() + " constituencies");

        for (String ac: acToRows.keySet()) {
            List<Row> ac_rows_prev_winners = new ArrayList<>(acToRows.get(ac)); // this ac's rows

            // sanity check - must be at least 1 row in winners.
            if (ac_rows_prev_winners.size() < 1) {
                out.println ("Warning! no winner at all in AC " + ac + " in assembly " + prev_assembly_no);
                continue;
            }

            // find the row with max poll_no
            Row prev_winner = ac_rows_prev_winners.stream().max(Comparator.comparing(r -> r.get("Poll_no"))).orElse(null);
            String poll_no = prev_winner.get("Poll_No");

            String pid_prev_winner = prev_winner.get("pid");
            Collection<Row> c = pid_to_non_winners_this.get(pid_prev_winner);
            if (c.size() > 0) {

                List<String> record = new ArrayList<>();
                record.add (pid_prev_winner);
                record.add (prev_winner.get("Candidate"));
                record.add (ac);
                record.add (prev_winner.get("Constituency_Name"));
                record.add (prev_winner.get("Party"));

                String lost_const = c.stream().map(r -> r.get("Constituency_Name")).collect(Collectors.joining(" "));
                record.add (lost_const);
                record.add (c.iterator().next().get("Party"));

                lostMLAs.printRecord(record);
            } else {
                if (pid_to_this.get(pid_prev_winner).size() == 0) {
                    // don't need the assembly we're looking at
                    List<Integer> other_assemblies_for_this_pid = pid_to_rows.get(pid_prev_winner).stream()
                            .map(r -> Integer.parseInt(r.get("Assembly_No")))
                            .filter(x -> x != prev_assembly_no).sorted().collect(Collectors.toList());

                    String other_assemblies_for_this_pid_str = other_assemblies_for_this_pid.stream().map(x -> Integer.toString(x)).collect(Collectors.joining(","));
                    boolean in_other_assemblies = other_assemblies_for_this_pid_str.length() > 0;

                    out.println(++count + ". " + prev_winner.get("Candidate") + " (" + prev_winner.get("Party") + "), AC#" + ac + " = " + prev_winner.get("Constituency_Name")
                            + ((ac_rows_prev_winners.size() > 1) ? " (winner in bypoll#" + poll_no + ")" : "")
                            + (in_other_assemblies ? (", pid + " + pid_prev_winner + " also candidate in assembly " + other_assemblies_for_this_pid_str) : ""));
                }
            }
        }
//        lostMLAs.flush();
 //       lostMLAs.close();
    }

    public static void main (String args[]) throws IOException {
        Dataset d = new Dataset("/Users/hangal/Downloads/kar.csv");
        pid_to_rows = SurfExcel.split(d.rows, "pid");

        for (int assembly = 9; assembly > 0; assembly--) {
            out.println ("----------\nWinners from assembly #" + assembly + " who did not contest in the next election");
            computeIncumbentStatsForAssembly(d, assembly);
        }
    }
}
