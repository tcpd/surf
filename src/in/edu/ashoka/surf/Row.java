package in.edu.ashoka.surf;

import java.io.PrintStream;
import java.util.*;

public class Row implements Comparable<Row> {
    private static final PrintStream out = System.out;
    private static final String FIELDSPEC_SEPARATOR = "-";
    private static String[] toStringFields = new String[0];

    int rowNum = -1;
    private final Map<String, Object> fields;
    private final Dataset d;

    public Row(Map<String, String> map, int rowNum, Dataset d) {
        this.fields = new LinkedHashMap<>();
        this.d = d;

        for (String key: map.keySet())
            this.fields.put(d.canonicalizeCol(key), map.get(key)); // intern the key to save memory

        this.rowNum = rowNum;
    }

    static void setToStringFields(String fieldSpec) {
        toStringFields = fieldSpec.split("-");
    }

    public String toString() {
        return getFields(toStringFields, FIELDSPEC_SEPARATOR) +  " (row# " + rowNum + ")";
    }

    boolean equal (String field, String value) {
        return this.get(field).equals(value); // ignore case?
    }

    public int compareTo(Row other)
    {
        // otherwise, more or less random (don't really expect positions and votes to be exactly the same....
        return toString().compareTo(other.toString());
    }

    public String get(String col) {
        if (col == null)
            return "";

        // read from the cached col cache (should always work), otherwise, canonicalize col name again
        String col1 = d.cCache.get(col);
        if (col1 == null)
            col1 = d.canonicalizeCol(col); // prob. should not reach here
        col = col1;

        while (true) {
            String alias = d.cColumnAliases.get(col);
            if (alias == null)
                break;
            col = alias;
        }

        String x = (String) fields.get(col);
        return (x != null) ? x : "";
    }

    public void set(String col, String val) {
        col = d.canonicalizeCol(col);
        while (true) {
            String alias = d.cColumnAliases.get(col);
            if (alias == null)
                break;
            col = alias;
        }

        fields.put(col, val);
    }

    /* returns # of columns in this row */
    public int nFields () {
        return this.fields.keySet().size();
    }

    public Set<String> getAllFieldNames () {
        return fields.keySet();
    }

    String getFields(String fields[], String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append (this.get(fields[i]));
            if (i < fields.length-1)
                sb.append(separator);
        }
        return sb.toString();
    }

    private int setAsInt(String field) {
        field = d.canonicalizeCol(field);
        int x = Integer.MIN_VALUE;
        if (field == null || "".equals(field)){
            x = 0;
        } else {
            String val = get(field);
            try { x = Integer.parseInt(val); }
            catch (NumberFormatException nfe) { out.println ("Warning, failed to parse integer from field: " + val + "row: " + this); }
        }
        fields.put(("_i_" + field).intern(), x);
        return x;
    }
}
