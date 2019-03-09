package in.edu.ashoka.surf;

import com.google.common.collect.Multimap;
import in.edu.ashoka.surf.util.Pair;
import in.edu.ashoka.surf.util.Util;

import java.io.IOException;
import java.util.*;

/**
 * Created by hangal on 10/21/15.
 */
class Shell {

    private Map<String, String> aliases = new LinkedHashMap<>();

    private static final String[] commandsArray = new String[]{"commands",
            "read", "write",
            "showrows",
            "alias", "call",
            "profile",
            "split", "sort",
            "filter",
            "diff", "difference", "differences",
            "clear",
            "select", "selectNot",
            "show", "print", "about",
            "displayDiffs",
            "display2Level",
            "displayPairs",
            "display",
            "misspelt", "similar",
            "desisimilar",
            "morethan", "atmost", "echo"
    };

    private static final String[] rewriteTokens = new String[]{
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

    private static final List<String> commands = new ArrayList<>();
    private static final Map<String, String> rewriteTokensMap = new LinkedHashMap<>();

    static {
        for (String s: commandsArray) { commands.add(s); }
        for (int i = 0; i < rewriteTokens.length-1; i+=2) {
            rewriteTokensMap.put(rewriteTokens[i], rewriteTokens[i+1]);
        }
    }

    private static final Map<String, Object> vars = new LinkedHashMap<>();

    // strip out everything before a valid command
    private static List<String> relayout_cmdline(List<String> args) {
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i).toLowerCase();
            if (commands.contains(arg))
            {
                List<String> result = new ArrayList<>();
                for (int j = i; j < args.size(); j++)
                    result.add(args.get(j));
                return result;
            }
        }
        return null;
    }

    private static List<String> removePlurals(Collection<String> c)
    {
        List<String> result = new ArrayList<>();
        for (String s: c)
            result.add(Dataset.removePlural(s));
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
        s = s.replaceAll("more than", "morethan");
        s = s.replaceAll("less than", "lessthan");
        s = s.replaceAll("look for", "lookfor");
        return rewriteTokens(Util.tokenize(s));
    }

    private static int getIndexOfColumnName(List<String> args, Dataset d) {
        for (int i = 0; i < args.size(); i++) {
            if (d.hasColumnName(args.get(i)))
                return i;
        }
        return -1;
    }

    private static int getIndexOfNumber(List<String> args, Dataset d) {
        for (int i = 0; i < args.size(); i++) {
            try { Integer.parseInt(args.get(i)); return i; }
            catch (NumberFormatException ignored) { }
        }
        return 1;
    }

    private static int getIndexOfVarName(List<String> args, Set<String> vars) {
        for (int i = 0; i < args.size(); i++) {
            if (vars.contains(args.get(i)))
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
        return (line.startsWith("yes") || line.equals("y") || line.startsWith("yeah") || line.equals("ya"));
    }

    private String extractCol(List<String> tokens, Dataset d) { return extractCol(tokens, d, false); }

    private int extractNum(List<String> tokens, Dataset d) {
        int i = getIndexOfNumber(tokens, d);

        if (i >= 0) {
            int x = Integer.parseInt(tokens.get(i));
            return x;
        }
        return 1;
    }

    private String extractCol(List<String> tokens, Dataset d, boolean promptIfNull) {
        // consider accommodating plurals: http://www.oxforddictionaries.com/words/plurals-of-nouns
        // consider also http://grammar.about.com/od/words/a/A-List-Of-Irregular-Plural-Nouns-In-English.htm
        // https://lucene.apache.org/core/4_4_0/analyzers-common/org/apache/lucene/analysis/en/EnglishMinimalStemmer.html
        // which refers to a simple S stemmer
        // (http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.104.9828, see rules at the bottom of page 2)
        int i = getIndexOfColumnName(tokens, d);
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

    private Collection<String> extractCols(List<String> tokens, Dataset d) {
        Collection<String> cols = new ArrayList<>();
        do {
            String col = extractCol(tokens, d, (cols.size() == 0) /* prompt only if cols.size is 0 */);
            if (col == null)
                break;
            cols.add(col);
        } while (true);
        return cols;
    }

    private Pair<String, Object> extractVar(List<String> tokens) {
        return extractVar(tokens, true);
    }

    private Pair<String, Object> extractVar(List<String> tokens, boolean promptIfNull) {
        int i = getIndexOfVarName(tokens, vars.keySet());
        if (i >= 0) {
            String var = tokens.remove(i);
            return new Pair<>(var, vars.get(var));
        }
        if (vars.get("it") != null)
            return new Pair<>("it", vars.get("it"));
        if (promptIfNull) {
            prompt("on which data?");
            String var = getLineFromUser();
            new Pair<>(var, vars.get(var));
        }
        return null;
    }

    private String getALine() {
        if (linesIterator != null)
            return linesIterator.hasNext() ? linesIterator.next() : null;
        else
            return getLineFromUser();
    }

    private Iterator<String> linesIterator = null;

    private Shell(String[] args) throws IOException {
        if (args.length >= 1) {
            List<String> lines = Util.getLinesFromFile(args[0], true /* ignore comment lines */);
            linesIterator = lines.iterator();
        }
    }

    private void start() {
        Dataset d = null;

        while (true) {
            if (linesIterator == null)
                println("What next?");
            try {
                String line = getALine();
                if (line == null || "quit".equals(line))
                    break;

               // line = line.toLowerCase();
                List<String> tokens = Util.tokenize(line);
                if (tokens.size() == 0)
                    continue;

                tokens = rewrite(tokens); // simple rewrite
                tokens = relayout_cmdline(tokens);
                if (Util.nullOrEmpty(tokens)) {
                    print ("Sorry, I didn't understand that...");
                    continue;
                }
                // look for commands, operators (conditions), variables, columns
                // variables can be collections of rows, split of col -> rows, pairs of diffs
                String cmd = tokens.remove(0);
                List<String> args = removePlurals(tokens);

                println("simplified cmd line: " + cmd + " " + Util.join(args, " "));

                if ("showrows".equals(cmd)) {
                    Collection<String> cols = extractCols(args, d);
                    Row.setToStringFields(Util.join(cols, "-"));
                }

                if ("read".equals(cmd)) {
                    String filename = args.get(0);
                    d = Dataset.getDataset(filename);
                    Collection<Row> allRows = d.rows;

                    vars.put("allrows", allRows);
                    vars.put(filename, allRows); // also put the collection under its filename
                    vars.put("it", allRows); // also put the collection under its filename
                    println("ok, it has " + Util.pluralize(allRows.size(), " row") + " and " + Util.pluralize(d.cColumns.size(), "column"));
                    println("would you like to see a profile of the columns?");
                    boolean y = getYesOrNoFromUser();
                    if (!y)
                        println("Ok, no problem.");
                    else {
                        for (String cCol : d.cColumns) {
                            print("Profile for column " + Util.join(d.cColumnToDisplayName.get(cCol), "|") + ":");
                            SurfExcel.profile(allRows, cCol);
                        }
                    }
                }

                if ("alias".equals(cmd) || "call".equals(cmd)) {
                    String oldName = args.get(0), newName = args.get(1);
                    if (commands.contains(newName)) {
                        error("Sorry, " + newName + " can't be used as a name... it gets too confusing. Please pick another name.");
                        continue;
                    }

                    if (d.hasColumnName(oldName)) {
                        d.registerColumnAlias(oldName, newName);
                        // leave it unchanged
                    } else {
                        if (vars.keySet().contains(oldName)) {
                            Object o = vars.get(oldName);
                            // vars.put(oldName, newName);
                            vars.put(newName, o); // also put the collection under its filename
                            vars.put("it", o); // also put the collection under its filename
                        }
                    }
                }

                if ("diff".equals(cmd) || "difference".equals(cmd) || "differences".equals(cmd)) {
                    Pair<String, Object> p1 = extractVar(args);
                    Pair<String, Object> p2 = extractVar(args);
                    Object o1 = p1.getSecond();
                    Object o2 = p2.getSecond();
                    if (o1 instanceof Multimap)
                        o1 = ((Multimap) o1).keySet();
                    if (o2 instanceof Multimap)
                        o2 = ((Multimap) o2).keySet();

                    Display.displayDiffs(p1.getFirst(), (Set<String>) o1, p2.getFirst(), (Set<String>) o2);
                }

                if ("profile".equals(cmd)) {
                    String col = extractCol(args, d, true);
                    if (col == null) {
                        error("sorry, need a column name");
                        continue;
                    }

                    Object o = extractVar(args).getSecond();
                    SurfExcel.profile(((Collection<Row>) o), col);
                    vars.put("it", o); // also put the collection under its filename
                }

                if ("split".equals(cmd)) {
                    Collection<String> cols = extractCols(args, d);
                    Object o = extractVar(args).getSecond();
                    if (Util.nullOrEmpty(cols)) {
                        error("sorry, need a column name");
                        continue;
                    }
                    Object var = null;
                    if (o instanceof Collection) {
                        var = SurfExcel.split((Collection<Row>) o, Util.join(cols, SurfExcel.FIELDSPEC_SEPARATOR));
                    } else if (o instanceof Multimap) {
                        var = SurfExcel.split((Multimap<String, Row>) o, Util.join(cols, SurfExcel.FIELDSPEC_SEPARATOR));
                    }

                    if (var != null)
                        vars.put("it", var);
                }

                if ("clear".equals(cmd)) {
                    vars.put("it", d.rows);
                }

                if ("filter".equals(cmd)) {
                    Object o = extractVar(args).getSecond();
                    Object var = null;
                    if (o instanceof Collection) {
                        // Collection<String> cols = extractCols(args, d);
                        // need a fieldspec and valuespec
                        var = SurfExcel.filter((Collection<Row>) o, args.get(0), args.get(1));
                    } else if (o instanceof Multimap) {
                        String op = (args.size() < 2) ? "equals" : args.get(0);
                        int val = Integer.parseInt((args.size() < 2) ? args.get(0) : args.get(1));
                        var = SurfExcel.filter((Multimap<String, Row>) o, op, val);
                    }

                    if (var != null)
                        vars.put("it", var);
                }

                if ("echo".equals(cmd)) {
                    print(Util.join(args, " ") + "\n");
                }

                if ("show".equals(cmd) || "lookfor".equals(cmd) || "print".equals(cmd) || "about".equals(cmd)) {
                    Object o = extractVar(args).getSecond();
                    if (o == null) {
                        error("sorry, need a column name");
                        continue;
                    }
                    if (o instanceof Multimap) {
                        Display.display2Level((Multimap) o, 3, true); // could be 1 or 2 level??
                    }
                    if (o instanceof Collection) {
                        Display.display ("", (Collection) o, 3);
                        // can also check if show has a parameter
                    }
                }

                if ("similar".equals(cmd)) {
                    String col = extractCol(args, d);
                    if (col == null) {
                        error("sorry, need a column name");
                        continue;
                    }
                    Object o = extractVar(args).getSecond();
                    Display.displayPairs((Collection) o, SurfExcel.valuesUnderEditDistance((Collection) o, col, 1), col, 3);
                }

                if ("misspelt".equals(cmd)) {
                    String col = extractCol(args, d);
                    if (col == null) {
                        error("sorry, need a column name");
                        continue;
                    }
                    Object o = extractVar(args).getSecond();
                    Tokenizer.setupDesiVersions((Collection<Row>) o, col);
                    Display.display2Level(SurfExcel.reportSimilarDesiValuesForField((Collection<Row>) o, col), 3, false);
                    Display.displaySimilarValuesForField((Collection<Row>) o, col, 2 /* edit distance */, 3 /* max rows */);
                }

                if ("morethan".equals(cmd) || "atmost".equals(cmd)) {
                    int num = extractNum(args, d);
                    Collection<String> cols = extractCols(args, d);
                    String allCols = Util.join(cols, "-");
                    Object o = extractVar(args).getSecond();
                    Multimap<String, Row> map = SurfExcel.split((Collection<Row>) o, allCols);
                    if ("morethan".equals(cmd))
                        Display.display(SurfExcel.filter(map, "min", num+1));
                    else if ("atmost".equals(cmd))
                        Display.display(SurfExcel.filter(map, "max", num));
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

