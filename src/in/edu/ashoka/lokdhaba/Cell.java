package in.edu.ashoka.lokdhaba;

import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;

/**
 * Created by hangal on 9/17/15.
 */
public class Cell {
    private static PrintStream out = System.out;
    private static String SEPARATOR = "========================================\n";

    public static void main(String[] args) throws IOException {
        Row.setToStringFields("Line");
        Row.setComparator(Row.rowNumComparator);
        Collection<Row> allRows = new Dataset(args[0]).rows;

        out.println(SEPARATOR + " Identical sentences");
        Multimap<String, Row> map1 = SurfExcel.split(allRows, "Line");
        Display.display(SurfExcel.filter(map1, "notequals", 1));

        out.println(SEPARATOR + "Almost identical sentences");
        Display.displayPairs(allRows, SurfExcel.valuesUnderEditDistance(allRows, "Line", 4), "Line", 3);
        System.exit(0);
    }
}
