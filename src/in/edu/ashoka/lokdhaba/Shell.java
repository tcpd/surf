package in.edu.ashoka.lokdhaba;

import com.google.common.collect.Multimap;
import edu.stanford.muse.util.Util;

import java.io.IOException;
import java.util.*;

/**
 * Created by hangal on 10/21/15.
 */
public class Shell {

    private Map<String, String> aliases = new LinkedHashMap<>();

    private static String[] commandsArray = new String[]{"commands",
            "read", "write",
            "showrows",
            "alias", "call",
            "profile",
            "split", "sort",
            "filter",
            "select", "selectNot",
            "show", "print",
            "displayDiffs",
            "display2Level",
            "displayPairs",
            "display",
            "setupVersions",
            "similarPairsForField",
            "misspelling", "similar"
    };

    private static String[] rewriteTokens = new String[]{
            "one", "1",
            "two", "2",
            "three", "3",
            "four", "4",
            "five", "5",
            "six", "6",
            "seven", "7",
            "eight", "8",
            "nine", "9",
            "ten", "ten",
            "multiple", "more than 1",
    };

    private static List<String> commands = new ArrayList<String>();
    private static Map<String, String> rewriteTokensMap = new LinkedHashMap<>();

    static {
        for (String s: commandsArray) { commands.add(s); }
        for (int i = 0; i < rewriteTokens.length-1; i+=2) {
            rewriteTokensMap.put(rewriteTokens[i], rewriteTokens[i+1]);
        }
    }

    private static Map<String, Object> vars = new LinkedHashMap<String, Object>();
    private Set<String> colNames = new LinkedHashSet<>();
    Set<Row> allRows;

    private static List<String> relayout_cmdline(List<String> args) {
        for (int i = 0; i < args.size(); i++) {
            if (commands.contains(args.get(i)))
            {
                List<String> result = new ArrayList<String>();
                for (int j = i; j < args.size(); j++)
                    result.add(args.get(j));
                return result;
            }
        }
        return null;
    }

    private static String removePlural(String s)
    {
        /* S stemmer: http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.104.9828
        IF a word ends in “ies,” but not “eies” or “aies”
        THEN “ies” - “y”
        IF a word ends in “es,” but not “aes,” “ees,” or “oes”
        THEN “es” -> “e”
        IF a word ends in "s,” but not “us” or ‘ss”
           THEN "s" -> NULL
        */

        if (s.endsWith("ies") && !(s.endsWith("eies") || s.endsWith("aies")))
            return s.replaceAll("ies$", "y"); // this gets movies wrong!
        if (s.endsWith("es") && !(s.endsWith("aes") || s.endsWith("ees") || s.endsWith("oes")))
            return s.replaceAll("es$", "e");
        if (s.endsWith("es") && !(s.endsWith("aes") || s.endsWith("ees") || s.endsWith("oes")))
            return s.replaceAll("es$", "e");
        if (s.endsWith("s") && !(s.endsWith("us") || s.endsWith("ss")))
            return s.replaceAll("s$", "");
        return s;
    }

    private static List<String> removePlurals(Collection<String> c)
    {
        List<String> result = new ArrayList<>();
        for (String s: c)
            result.add(removePlural(s));
        return result;
    }

    private static List<String> rewriteTokens (List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String t: tokens) {
            String replacement = rewriteTokensMap.get(t);
            result.add(!Util.nullOrEmpty(replacement) ? replacement : t);
        }
        return result;
    }

    private static List<String> rewrite (List<String> tokens) {
        tokens = rewriteTokens(tokens);
        String s = Util.join (tokens, " ");
        s.replaceAll("more than", "morethan");
        s.replaceAll("less than", "lessthan");
        s.replaceAll("look for", "lookfor");
        return rewriteTokens(Util.tokenize(s));
    }

    private static int getTokenIndex(List<String> args, Set<String> colNames) {
        for (int i = 0; i < args.size(); i++) {
            if (colNames.contains(args.get(i)))
                return i;
        }
        return -1;
    }

    private static void error(String s) {System.err.println(s);}
    private void prompt(String s) { System.out.println(s); }
    private void print(String s) { System.out.print(s); }
    private void println(String s) { System.out.println(s); }

    private String getLineFromUser() {
        if (linesIterator != null)
            return null;
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        String s = reader.nextLine(); // Scans the next token of the input as an int."NO-COL"; }
        return s;
    }

    private boolean getYesOrNoFromUser() {
        String line = getLineFromUser();
        if (line == null)
            return false;
        line = line.toLowerCase().trim();
        return ("yes".equals(line));
    }

    private String extractCol(List<String> tokens) { return extractCol(tokens, false); }

    private String extractCol(List<String> tokens, boolean promptIfNull) {
        // consider accommodating plurals: http://www.oxforddictionaries.com/words/plurals-of-nouns
        // consider also http://grammar.about.com/od/words/a/A-List-Of-Irregular-Plural-Nouns-In-English.htm
        // https://lucene.apache.org/core/4_4_0/analyzers-common/org/apache/lucene/analysis/en/EnglishMinimalStemmer.html
        // which refers to a simple S stemmer
        // (http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.104.9828, see rules at the bottom of page 2)
        int i = getTokenIndex(tokens, colNames);
        if (i >= 0)
            return tokens.remove(i);
        else {
            if (promptIfNull) {
                prompt("on which column?");
                String col = getLineFromUser();
                return col;
            }
            return null;
        }
    }

    private Collection<String> extractCols(List<String> tokens) {
        Collection<String> cols = new ArrayList<>();
        do {
            String col = extractCol(tokens, true);
            if (col == null)
                break;
            cols.add(col);
        } while (true);
        return cols;
    }

    private Object extractVar(List<String> tokens) {
        return extractVar(tokens, true);
    }

    private Object extractVar(List<String> tokens, boolean promptIfNull) {
        int i = getTokenIndex(tokens, vars.keySet());
        if (i >= 0) {
            String var = tokens.remove(i);
            return vars.get(var);
        }
        else {
            if (promptIfNull) {
                prompt("on which data?");
                String var = getLineFromUser();
                return vars.get(var);
            }
            else
                return vars.get("it");
        }
    }

    private String getALine() {
        if (linesIterator != null)
            return linesIterator.hasNext() ? linesIterator.next() : null;
        else
            return getLineFromUser();
    }

    Iterator<String> linesIterator = null;

    public Shell(String[] args) throws IOException {
        if (args.length >= 1) {
            List<String> lines = Util.getLinesFromFile(args[0], true /* ignore comment lines */);
            linesIterator = lines.iterator();
        }
    }

    private void start() throws IOException {

        while (true) {
            if (linesIterator == null)
                print ("> ");
            try {
                String line = getALine();
                if (line == null || "quit".equals(line))
                    break;

                line = line.toLowerCase();
                List<String> tokens = Util.tokenize(line);

                tokens = rewrite(tokens); // simple rewrite
                tokens = relayout_cmdline(tokens);
                // look for commands, operators (conditions), variables, columns
                // variables can be collections of rows, split of col -> rows, pairs of diffs
                String cmd = tokens.remove(0);
                List<String> args = removePlurals(tokens);

                println("simplified cmd line: " + cmd + " " + Util.join(args, " "));

                if ("showrows".equals(cmd)) {
                    Collection<String> cols = extractCols(args);
                    Row.setToStringFields(Util.join(cols, "-"));
                }

                if ("read".equals(cmd)) {
                    String filename = args.get(0);
                    allRows = SurfExcel.readRows(filename);
                    for (String s : allRows.iterator().next().fields.keySet())
                        colNames.add(s);

                    vars.put("allrows", allRows);
                    vars.put(filename, allRows); // also put the collection under its filename
                    vars.put("it", allRows); // also put the collection under its filename
                    println("ok, it has " + Util.pluralize(allRows.size(), " row") + " and " + Util.pluralize(colNames.size(), "column"));
                    println("would you like to see a profile of the columns?");
                    boolean y = getYesOrNoFromUser();
                    if (!y)
                        println("Ok, no problem.");
                    else {
                        for (String col : colNames) {
                            print("Profile for column " + col + ":");
                            SurfExcel.profile(allRows, col);
                        }
                    }
                }

                if ("alias".equals(cmd) || "call".equals(cmd)) {
                    String oldName = args.get(0), newName = args.get(1);
                    if (commands.contains(newName)) {
                        error("Sorry, " + newName + " can't be used as a name... it gets too confusing. Please pick another name");
                        continue;
                    }

                    if (colNames.contains(oldName)) {
                        SurfExcel.setColumnAlias(allRows, oldName, newName);
                        // leave it unchanged
                    }

                    if (vars.keySet().contains(oldName)) {
                        Object o = vars.get(oldName);
                        vars.put(oldName, newName);
                        vars.put("it", o); // also put the collection under its filename
                    }
                }

                if ("profile".equals(cmd)) {
                    String col = extractCol(args);
                    if (col == null) {
                        error("sorry, need a column name");
                        continue;
                    }

                    Object o = extractVar(args);
                    SurfExcel.profile(((Collection<Row>) o), col);
                    vars.put("it", o); // also put the collection under its filename
                }

                if ("split".equals(cmd)) {
                    Collection<String> cols = extractCols(args);
                    Object o = extractVar(args);
                    if (Util.nullOrEmpty(cols)) {
                        error("sorry, need a column name");
                        continue;
                    }
                    Object var = null;
                    if (o instanceof Collection) {
                        var = SurfExcel.split((Collection<Row>) o, Util.join(cols, "-"));
                    } else if (o instanceof Multimap) {
                        var = SurfExcel.split((Multimap<String, Row>) o, Util.join(cols, "-"));
                    }

                    if (var != null)
                        vars.put("it", var); // also put the collection under its filename
                }

                if ("echo".equals(cmd)) {
                    print(Util.join(args, " "));
                }

                if ("show".equals(cmd) || "lookfor".equals(cmd) || "print".equals(cmd)) {
                    Object o = extractVar(args);
                    if (o == null) {
                        error("sorry, need a column name");
                        continue;
                    }
                    if (o instanceof Multimap) {
                        Display.display2Level((Multimap) o, 3, true);
                    }
                }

                if ("misspelling".equals(cmd) || "similar".equals(cmd)) {
                    String col = extractCol(args);
                    if (col == null) {
                        error("sorry, need a column name");
                        continue;
                    }
                    Object o = extractVar(args);
                    Display.displayPairs((Collection) o, SurfExcel.valuesUnderEditDistance((Collection) o, col, 1), col, 3);
                }
            } catch (Exception e) {
                error("Sorry, met an exception! " + e);
                e.printStackTrace(System.err);
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new Shell(args).start();
    }

}

