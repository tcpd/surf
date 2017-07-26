package in.edu.ashoka.lokdhaba;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;



public class Dataset implements Serializable{
    static Map<String, Dataset> datasetMap= new HashMap<>();
    static final long TIME_INTERVAL_BETWEEN_BACKUPS = 1000*60*60*4;
    long saveTimeOfBackedUpFile = 0;
    long saveTime = 0;
	public static String NEW_LINE_SEPARATOR = "\n";
    Collection<Row> rows;
    Collection<String> actualColumnName;
    String name, description;
    Set<String> cColumns = new LinkedHashSet<>(); // this is the real list of col names (canonical) available (no aliases) for each row in this dataset.
    Multimap<String, String> cColumnToDisplayName = LinkedHashMultimap.create(); // display col names (both real and aliases)
    Map<String, String> cColumnAliases = new LinkedHashMap<>(); // cCol -> cCol as aliases.
    Timer timer;
    
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

    public static Dataset getDataset(String filename) throws IOException{
        if(!datasetMap.containsKey(filename)){
            synchronized (Dataset.class){
                if(!datasetMap.containsKey(filename)){
                    datasetMap.put(filename, new Dataset(filename));
                    //CREATE ALL NECESSARY TOKENS AND FORMATTING
                    Bihar.initRowFormat(datasetMap.get(filename).getRows(),datasetMap.get(filename));
                }
            }
        }
        return datasetMap.get(filename);
    }

    private Dataset (String filename) throws IOException {

        //TRY Backing up data after given interwell, this is a task which will continue running as a seperate thread
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    if((saveTime>saveTimeOfBackedUpFile))
                        performBackup(new File(filename));
                }catch (IOException e){
                    System.err.println("file backup failed");
                }

            }
        }, 0, TIME_INTERVAL_BETWEEN_BACKUPS);

        checkFilesForFailure(filename);
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

    private void checkFilesForFailure(String filename) throws IOException{
        String fileWithSuffixNew = filename + ".new";
        String fileWithSuffixOld = filename + ".old";
        if(new File(filename).exists()){
            File newFile = new File(fileWithSuffixNew);
            if(newFile.exists())
                newFile.delete();
        }else{
            File newFile = new File(fileWithSuffixNew);
            if(newFile.exists())
                newFile.renameTo(new File(filename));
            else{
                File oldFile = new File(fileWithSuffixOld);
                if(oldFile.exists())
                    oldFile.renameTo(new File(filename));
                else
                    throw new FileNotFoundException("file not found");
            }
        }
    }

    boolean hasColumnName(String col) {
        String ccol = canonicalizeCol(col);
        return cColumnAliases.keySet().contains(ccol) || cColumns.contains(ccol);
    }
    
    public Collection<Row> getRows(){return rows;}

    /** saves this dataset as a CSV  in the given file 
     * @throws IOException */
    synchronized public void save(String file) throws IOException {
        saveTime = System.currentTimeMillis();
    	
    	CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        //File csvFile = new File(file);

        //FIRST WRITE TO A NEW FILE
        String fileWithSuffixNew = file + ".new";
        Writer fileWriter = new FileWriter(fileWithSuffixNew);
        CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter,csvFileFormat);
        List<String> columnList = new ArrayList<>(actualColumnName);
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

        //NOW RENAME THE EXISTING FILE TO OLD FILE
        String fileWithSuffixOld = file + ".old";
        File existingFile = new File(file);
        File suffixFile = new File(fileWithSuffixOld);
        if(!existingFile.canWrite() || !existingFile.renameTo(suffixFile))
            throw new IOException("failed to rename existing file to old file");

        //RENAME THE NEW FILE TO EXISTING FILE
        suffixFile = new File(fileWithSuffixNew);
        if(!suffixFile.canWrite()||!suffixFile.renameTo(new File(file))){
            throw new IOException("failed to rename new file to existing file");
        }

        //PERFORM BACKUP
        //performBackup(new File(file));
    }

    /*
    * long upTime = System.currentTimeMillis() - IncumbencyServlet.START_TIME;
        if(force || upTime-lastBackUpTime > TIME_INTERVAL_BETWEEN_BACKUPS ||lastBackUpTime==0){
        lastBackUpTime = System.currentTimeMillis();
    * */

    synchronized public void performBackup(File file) throws IOException{
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

        //delete old backups here if needed in future

    }

    public static void destroyTimer(){
        for(Dataset d:datasetMap.values()){
            d.timer.cancel();
        }
    }
}
