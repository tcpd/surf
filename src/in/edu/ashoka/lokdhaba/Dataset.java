package in.edu.ashoka.lokdhaba;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class Dataset implements Serializable{
	public static String NEW_LINE_SEPARATOR = "\n";
    Collection<Row> rows;
    Collection<String> actualColumnName;
    String name, description;
    Set<String> cColumns = new LinkedHashSet<>(); // this is the real list of col names (canonical) available (no aliases) for each row in this dataset.
    Multimap<String, String> cColumnToDisplayName = LinkedHashMultimap.create(); // display col names (both real and aliases)
    Map<String, String> cColumnAliases = new LinkedHashMap<>(); // cCol -> cCol as aliases.
    
    public void addToActualColumnName(String col){
    	if(actualColumnName.contains(col))
    		return;
    	else
    		actualColumnName.add(col);
    }
    
    static String removePlural(String s)
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

    // maintain a map for canonicalization, otherwise computing lower case, remove plurals etc takes a lot of time when reading a large dataset
    Map<String, String> cCache = new LinkedHashMap<>();
    String canonicalizeCol(String col) {
        String s = cCache.get(col);
        if (s != null)
            return s;

        String ccol = col.toLowerCase();
        ccol = ccol.replaceAll("_", "");
        ccol = ccol.replaceAll("-", "");
        ccol = removePlural(ccol).intern();
        cCache.put(col, ccol);
        return ccol;
    }

    void warnIfColumnExists(String col) {
        if (hasColumnName(col))
            System.err.println("Error: duplicate columns for repeated: " + col);
    }

    void registerColumn(String col) {
        warnIfColumnExists(col);
        String cCol = canonicalizeCol(col);
        cColumns.add(cCol);
        cColumnToDisplayName.put(cCol, col);
    }

    public void registerColumnAlias(String oldCol, String newCol) {
        warnIfColumnExists(newCol);
        if (!hasColumnName(oldCol)) {
            System.err.println("Warning: no column called " + oldCol);
            return;
        }

        registerColumn(newCol);
        cColumnAliases.put(canonicalizeCol(newCol), canonicalizeCol(oldCol));
    }

    public Dataset (String filename) throws IOException {
        this.name = filename;

        Set<Row> allRows = new LinkedHashSet<>();
        actualColumnName = new ArrayList<>();
        int nRows = 0;
//        Reader in = new FileReader("GE.csv");
        // read the names from CSV
        Iterable<CSVRecord> records = CSVParser.parse(new File(filename), Charset.forName("UTF-8"), CSVFormat.EXCEL.withHeader());
        for (CSVRecord record : records) {
            nRows++;
            Map<String, String> map = record.toMap();

            if (nRows == 1) {
                for (String col : map.keySet()) {
                	actualColumnName.add(col);
                    registerColumn(col);
                }
            }

            Row r = new Row(map, nRows, this);
            allRows.add(r);
        }
        this.rows = allRows;
    }

    boolean hasColumnName(String col) {
        String ccol = canonicalizeCol(col);
        return cColumnAliases.keySet().contains(ccol) || cColumns.contains(ccol);
    }
    
    public Collection<Row> getRows(){return rows;}

    /** saves this dataset as a CSV  in the given file 
     * @throws IOException */
    public void save(String file) throws IOException {
    	
    	CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        FileWriter fileWriter = new FileWriter(file);
        CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter,csvFileFormat);
        List<String> columnList = new ArrayList<>(actualColumnName);
        
        //columnList.add("ID");
        //columnList.add("mapped_ID");
        csvFilePrinter.printRecord(columnList);
        
        for(Row row:rows){
        	List<String> recordList = new ArrayList<>();
        	for(String column:columnList){
        		recordList.add(row.get(column));
        	}
        	csvFilePrinter.printRecord(recordList);
        }
        
        fileWriter.flush();
        fileWriter.close();
        csvFilePrinter.close();
    }

}
