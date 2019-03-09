package in.edu.ashoka.surf;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import in.edu.ashoka.surf.util.Util;
import org.apache.commons.csv.*;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Dataset implements Serializable, Cloneable {
    private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.Dataset.class);

    private static final Map<String, Dataset> datasetMap = new LinkedHashMap<>(); // this is a static map of all datasets

    private static final long TIME_INTERVAL_BETWEEN_BACKUPS = 1000*60*60*4;
    private long saveTimeOfBackedUpFile = 0;
    private long saveTime = 0;
    private String filename;

	private static final String NEW_LINE_SEPARATOR = "\n";
    Collection<Row> rows;
    private Collection<String> columnsToSave; // this is what is used to decide which columns get written out when the dataset is saved
    private String name;
    String description;
    final Set<String> cColumns = new LinkedHashSet<>(); // this is the real list of col names (canonical) available (no aliases) for each row in this dataset.
    final Multimap<String, String> cColumnToDisplayName = LinkedHashMultimap.create(); // display col names (both real and aliases)
    final Map<String, String> cColumnAliases = new LinkedHashMap<>(); // cCol -> cCol as aliases.
    private final Timer timer;
    
    private void addToColumnsToSave(String col){
    	if(columnsToSave.contains(col)) {
        }
    	else
    		columnsToSave.add(col);
    }

    /** returns the TBR version of this dataset. reads from disk if available, otherwise creates a new dataset with the same column structure, but no rows */
    public Dataset getTBRDataset() {
        Dataset d = null;
        try {
            String filename = this.filename;
            if (filename.endsWith(".csv"))
                filename = filename.substring (0, filename.length() - ".csv".length());
            String tbrFilename = filename + ".tbr.csv";

            // if tbr file exists, read from there, otherwise start a clone of this, and clear the rows
            if (new File(tbrFilename).exists()) {
                d = new Dataset(tbrFilename);
            } else {
                d = (Dataset) this.clone();
                d.description = "To be reviewed rows for " + this.description;
                d.rows = new ArrayList<>(); // reset the rows
                d.filename = tbrFilename; // set the filename to the tbrFilename
            }
        } catch (CloneNotSupportedException | IOException cnse) {
            Util.print_exception("Clone failed", cnse, log);
        }
        return d;
    }

    /* returns the actual display names of the columns in this dataset */
    public Set<String> getColumnDisplayNames() {
        Set<String> result = new LinkedHashSet<>();
        for (String col: cColumns) {
            Collection<String> displayNames = cColumnToDisplayName.get(col);
            if (displayNames.size() > 0) {
                result.add (displayNames.iterator().next()); // pick up only the first, in case there are multiple display names (is this possible??)
            }
        }
        return result;
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
    final Map<String, String> cCache = new LinkedHashMap<>();
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

    private void warnIfColumnExists(String col) {
        if (hasColumnName(col))
            System.err.println("Error: duplicate columns for repeated: " + col);
    }

    private void registerColumn(String col) { registerColumn(col, false); }

    private void registerColumn(String col, boolean isAlias) {
        warnIfColumnExists(col);
        String cCol = canonicalizeCol(col);
        cColumns.add(cCol);
        cColumnToDisplayName.put(cCol, col);
        if (!isAlias) // don't add if an alias, otherwise we get the column twice in the saved csv
            addToColumnsToSave(col);
    }

    //check whether any row has an ID field that is empty. returns true if even one row does not have an ID field
    public boolean hasIds(){
        Collection<Row> allRows = getRows();
        for (Row row:allRows) {
            if (row.get(Config.ID_FIELD).equals(""))
                return false;
        }
        return true;
    }

    //initialize the id's for each row
    final public void initializeIds(){
        if (!hasColumnName(Config.ID_FIELD))
            registerColumn(Config.ID_FIELD);
        SurfExcel.assignUnassignedIds(getRows());
    }

    public void registerColumnAlias(String oldCol, String newCol) {
        warnIfColumnExists(newCol);
        if (!hasColumnName(oldCol)) {
            System.err.println("Warning: no column called " + oldCol);
            return;
        }

        registerColumn(newCol, true /* is an alias */);
        cColumnAliases.put(canonicalizeCol(newCol), canonicalizeCol(oldCol));
    }

    public static Dataset getDataset(String filename) throws IOException{
        if(!datasetMap.containsKey(filename)){
            synchronized (Dataset.class){
                if(!datasetMap.containsKey(filename)){
                    datasetMap.put(filename, new Dataset(filename));
                    //CREATE ALL NECESSARY TOKENS AND FORMATTING
//                    Bihar.initRowFormat(datasetMap.get(filename).getRows(),datasetMap.get(filename));
                }
            }
        }
        return datasetMap.get(filename);
    }

    Dataset(String filename) throws IOException {

        //TRY Backing up data after given interwell, this is a task which will continue running as a seperate thread
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    if((saveTime > saveTimeOfBackedUpFile))
                        performBackup(new File(filename));
                }catch (IOException e){
                    System.err.println("file backup failed");
                }

            }
        }, 0, TIME_INTERVAL_BETWEEN_BACKUPS);

        checkFilesForFailure(filename);
        this.name = filename;

        Set<Row> allRows = new LinkedHashSet<>();
        columnsToSave = new ArrayList<>();
        int nRows = 0;
//        Reader in = new FileReader("GE.csv");
        // read the names from CSV
        Iterable<CSVRecord> records = CSVParser.parse(new File(filename), Charset.forName("UTF-8"), CSVFormat.EXCEL.withHeader());
        for (CSVRecord record : records) {
            nRows++;
            Map<String, String> map = record.toMap();

            if (nRows == 1) {
                for (String col : map.keySet()) {
                	columnsToSave.add(col);
                    registerColumn(col);
                }
            }

            Row r = new Row(map, nRows, this);
            allRows.add(r);
        }
        this.rows = allRows;
        this.filename = filename;
    }

    private void checkFilesForFailure(String filename) throws IOException{
        String fileWithSuffixNew = filename + ".new";
        String fileWithSuffixOld = filename + ".old";
        if(new File(filename).exists()){
            File newFile = new File(fileWithSuffixNew);
            if(newFile.exists())
                newFile.delete();
        }else{
            File newFile = new File(fileWithSuffixNew);
            if(newFile.exists()) {
                FileUtils.copyFile(newFile, new File(fileWithSuffixNew));
                if(!FileUtils.deleteQuietly(newFile))
                    throw new FileExistsException("failed to DeleteDatasetServlet .new file");
            }
            else{
                File oldFile = new File(fileWithSuffixOld);
                if(oldFile.exists()) {
                    FileUtils.copyFile(oldFile, new File(filename));
                    if(!FileUtils.deleteQuietly(oldFile))
                        throw new FileExistsException("failed to DeleteDatasetServlet .old file");
                }
               // else
                //    throw new FileNotFoundException("file not found");
            }
        }
    }

    boolean hasColumnName(String col) {
        String ccol = canonicalizeCol(col);
        return cColumnAliases.keySet().contains(ccol) || cColumns.contains(ccol);
    }
    
    public Collection<Row> getRows(){return rows;}

    private int nCols(){
        if (rows == null || rows.size() == 0)
            return 0;
        Row row = rows.iterator().next();
        return row.nFields();
    }

    public Set<String> getColumnNames () {
        if (rows == null || rows.size() == 0)
            return new HashSet<>();

        Row row = rows.iterator().next();
        return row.getAllFieldNames();
    }

    /** save to original file this dataset was loaded from */
    synchronized public void save() throws IOException {
        save(this.filename);
    }

    /** saves this dataset as a CSV  in the given file 
     * @throws IOException */
    private synchronized void save(String file) throws IOException {
        saveTime = System.currentTimeMillis();
    	
        //FIRST WRITE TO A NEW FILE
        String fileWithSuffixNew = file + ".new";
        Writer fileWriter = new FileWriter(fileWithSuffixNew);

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter,csvFileFormat.withQuoteMode(QuoteMode.NON_NUMERIC));

        List<String> columnList = new ArrayList<>(columnsToSave);
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

        //NOW RENAME THE ORIGINAL FILE TO OLD FILE (may not always exist for tbr files, so check if old file even exists)
        File existingFile = new File(file);
        if (existingFile.exists()) {
            String fileWithSuffixOld = file + ".old";
            File backupFile = new File(fileWithSuffixOld);
            FileUtils.copyFile(existingFile, backupFile);
        }

        //RENAME THE NEW FILE TO EXISTING FILE
        File newFile = new File(fileWithSuffixNew);
        FileUtils.copyFile(newFile, new File(file));
        if(!FileUtils.deleteQuietly(newFile))
            throw new FileExistsException("failed to DeleteDatasetServlet .new file");
    }

    /*
    * long upTime = System.currentTimeMillis() - MergeServlet.START_TIME;
        if(force || upTime-lastBackUpTime > TIME_INTERVAL_BETWEEN_BACKUPS ||lastBackUpTime==0){
        lastBackUpTime = System.currentTimeMillis();
    * */

    private synchronized void performBackup(File file) throws IOException{
        saveTimeOfBackedUpFile = saveTime;
        //CREATE BACKUP FILE
        //first check for directory; if doesn't exist create
        if(!new File(file.getParent()+File.separator+"backups").exists()){
            new File(file.getParent()+File.separator+"backups").mkdir();
        }
        String backupPath = file.getParent()+File.separator+"backups";
        DateFormat df = new SimpleDateFormat("dd-MM-yy-HH.mm.ss");
        Date dateobj = new Date();
        String timestamp = "."+df.format(dateobj);
        String backupFilePath = backupPath+File.separator+file.getName()+timestamp;
        File backupFile = new File(backupFilePath);

        InputStream inputStream = new FileInputStream(file);
        OutputStream outputStream = new FileOutputStream(backupFile);

        byte[] buffer = new byte[1024];

        int length;
        //copy the file content in bytes
        while ((length = inputStream.read(buffer)) > 0){
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();

        //DeleteDatasetServlet old backups here if needed in future

    }

    public static void destroyTimer(){
        for(Dataset d:datasetMap.values()){
            d.timer.cancel();
        }
    }

    public String toString() {
        return "Dataset with " + getRows().size() + " rows and " + nCols() + " columns read from " + filename;
    }
}
