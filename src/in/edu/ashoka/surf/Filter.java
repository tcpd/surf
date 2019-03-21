package in.edu.ashoka.surf;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import in.edu.ashoka.surf.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A filter is an object that contains a list of column_name -> { allowed values (or regexes) in that column}. e.g.
 * Position=1,2,3;Sex=M;Name=/Mo.*di/,Shah
 * will match records with all of the following: 1) Position = 1 or 2 or 3, 2) Sex = M and 3) Name matching regex Mo.*di or (exactly) Shah
 */
class Filter {
    private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.Filter.class);

    private final SetMultimap<String, String> colNameToAllowedValues = LinkedHashMultimap.create(); // normalized to lower case
    private final SetMultimap<String, Pattern> colNameToAllowedRegexPatterns = LinkedHashMultimap.create(); // pattenrs are case-insensitive
    private boolean isEmpty = false; /* set to true if filter is empty, i.e. it passes all rows, which is a common case */

    public boolean isEmpty() { return this.isEmpty; }

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

        if (filterSpec == null) {
            filterSpec = "";
        }
        isEmpty = Util.nullOrEmpty(filterSpec);

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

                if (allowedVal.startsWith("/") && allowedVal.endsWith("/") && allowedVal.length() >= 3) {
                    String patternString = allowedVal.substring (1, allowedVal.length()-1); // strip the leading and trailing /
                    colNameToAllowedRegexPatterns.put (col, Pattern.compile(patternString, Pattern.CASE_INSENSITIVE));
                } else {
                    colNameToAllowedValues.put(col, allowedVal.toLowerCase());
                }
            }
        }

        log.info ("Parsed filter spec: " + filterSpec);
    }

    /** returns whether the row passes the given filter. if the filter is empty, always returns true. */
    public boolean passes (Row r) {
        Set<String> allCols = Sets.union(colNameToAllowedValues.keySet(), colNameToAllowedRegexPatterns.keySet());
        outer:
        for (String colName: allCols) {
            Set<String> allowedValues = colNameToAllowedValues.get(colName);
            String fieldVal = r.get(colName).toLowerCase();
            if (!allowedValues.contains (fieldVal)) {
                // fieldVal doesn't match values directly. try patterns
                // if any pattern matches, this column is fine, go to the next
                // otherwise return false right away
                Set<Pattern> allowedPattern = colNameToAllowedRegexPatterns.get(colName);
                if (allowedPattern != null) {
                    for (Pattern p: allowedPattern) {
                        if (p.matcher(fieldVal).find()) {
                            continue outer; // go to next col, this one matches
                        }
                    }
                }
                return false;
            }
        }
        // no column had an objection, we match and return true
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

        sb.append ("\nRegex patterns: ");
        for (String colName: colNameToAllowedRegexPatterns.keySet()) {
            sb.append (colName + "=");
            Set<Pattern> allowedPatterns = colNameToAllowedRegexPatterns.get(colName);
            sb.append(String.join (",", allowedPatterns.stream().map (Pattern::toString).collect (Collectors.toSet())));
            sb.append (";");
        }
        return sb.toString();
    }

    public static void main (String args[]) {
        Filter f = new Filter ("Pos=1");
        Filter f1 = new Filter ("Pos=1,2,3,2,,;State=Gujarat,Maharashtra");
        Filter f2 = new Filter ("Pos=1,2,3; Cand1=/MODI/,SHAH"); // will regex match MODI and exact match SHAH
        Filter f3 = new Filter ("Pos=1,2,3; col2"); // should throw an exception
    }
}
