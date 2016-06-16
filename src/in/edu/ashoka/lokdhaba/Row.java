package in.edu.ashoka.lokdhaba;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Row implements Comparable<Row> {
    private static PrintStream out = System.out;
    private static String FIELDSPEC_SEPARATOR = "-";
    private static Comparator currentComparator = null;

    int year = -1, position = -1, votes = -1, rowNum = -1;
    Map<String, Object> fields;
    static String[] toStringFields = new String[0];
    private Dataset d;

    static Comparator<Row> positionComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.getInt("Position") - r2.getInt("Position");
        }};

    static Comparator<Row> rowNumComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.rowNum - r2.rowNum;
        }};


    static Comparator<Row> yearComparator = new Comparator<Row>() {
        public int compare(Row r1, Row r2) {
            return r1.getInt("Year") - r2.getInt("Year");
        }};

    public Row(Map<String, String> map, int rowNum, Dataset d) {
        this.fields = new LinkedHashMap<>();
        this.d = d;

        for (String key: map.keySet())
            this.fields.put(d.canonicalizeCol(key), map.get(key)); // intern the key to save memory

        this.rowNum = rowNum;
        setup(map.get("Year"), map.get("Position"), map.get("Votes"));
    }

    public static void setToStringFields(String fieldSpec) {
        toStringFields = fieldSpec.split("-");
    }

    public static void setComparator(Comparator c) {
        currentComparator = c;
    }
    public void setup(String year, String position, String votes) {
        try { this.year = Integer.parseInt(year); } catch (NumberFormatException nfe ) { }
        try { this.position = Integer.parseInt(position); } catch (NumberFormatException nfe ) { }
        try { this.votes = Integer.parseInt(votes); } catch (NumberFormatException nfe ) { }
    }

    public String toString() {
        return getFields(toStringFields, FIELDSPEC_SEPARATOR) +  " (row# " + rowNum + ")";
    }

    boolean equal (String field, String value) {
        return this.get(field).equals(value); // ignore case?
    }

    public int compareTo(Row other)
    {
        if (currentComparator != null)
            return currentComparator.compare(this, other);

        // lower positions first
        if (position != other.position)
            return (position < other.position) ? -1 : 1;
        // more votes first
        if (votes != other.votes)
            return (votes > other.votes) ? -1 : 1;

        // otherwise, more or less random (don't really expect positions and votes to be exactly the same....
        return toString().compareTo(other.toString());
    }

    public String get(String col) {
        if (col == null)
            return "";
        col = d.canonicalizeCol(col);
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

    public String getFields (String fields[], String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append (this.get(fields[i]));
            if (i < fields.length-1)
                sb.append(separator);
        }
        return sb.toString();
    }

    public int setAsInt(String field) {
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

    public int getInt(String field) {
        Integer I = (Integer) fields.get("_i_" + field);
        if (I != null)
            return I;

        return setAsInt(field);
    }
}
