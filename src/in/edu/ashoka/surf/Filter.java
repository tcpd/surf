package in.edu.ashoka.surf;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import edu.stanford.muse.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

/**
 * A filter is an object that contains a list of column_name -> { allowed values in that column}.
 * e.g. "Position" = {"1", "2", "3"}
 */
public class Filter {
    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.Filter.class);

    private SetMultimap<String, String> colNameToAllowedValues = LinkedHashMultimap.create();

    /**
     *
     * @param filterSpec is something like Position=1,2,3 ; State=Gujarat, Maharashtra
     * i.e. in the format of semicol separated column directives
     * each column directive is of the form <col name>=<val1>,<val2>, etc.
     * note: no comma or ; is allowed inside of col names or values,
     * col names and values are trimmed, so leading or training blanks
     * The filter ands all the column directives
     * throws exception if the filterSpec does not follow the above syntax
     */
    public Filter (String filterSpec) {

        List<String> directives = Util.tokenize (filterSpec, ";");

        for (String directive: directives) {
            String parts[] = directive.split("=");
            // this better give us 2 parts, otherwise we'll throw an exception
            if (parts.length != 2)
                throw new RuntimeException ("Invalid filter spec, more than 2 parts " + filterSpec);

            String col = parts[0].trim(), vals = parts[1];
            if (col.length() == 0 || vals.length() == 0)
                throw new RuntimeException ("Invalid filter spec, empty col or values " + filterSpec);

            for (String allowedVal : vals.split(",")) {
                allowedVal = allowedVal.trim();
                if (allowedVal.length() == 0)
                    continue;
                colNameToAllowedValues.put(col, allowedVal);
            }
        }

        log.info ("Parsed filter spec: " + this);
    }

    /** returns whether the row passes the given filter. if the filter is empty, always returns true. */
    public boolean passes (Row r) {
        for (String colName: colNameToAllowedValues.keySet()) {
            Set<String> allowedValues = colNameToAllowedValues.get(colName);
            if (!allowedValues.contains (r.get(colName)))
                return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String colName: colNameToAllowedValues.keySet()) {
            sb.append (colName + "=");
            Set<String> allowedValues = colNameToAllowedValues.get(colName);
            sb.append(String.join (",", allowedValues));
            sb.append (";");
        }
        return sb.toString();
    }

    public static void main (String args[]) {
        Filter f = new Filter ("Pos=1");
        Filter f1 = new Filter ("Pos=1,2,3,2,,;State=Gujarat,Maharashtra");
        Filter f2 = new Filter ("Pos=1,2,3; col2"); // should throw an exception
    }
}
