package in.edu.ashoka.surf;

import in.edu.ashoka.surf.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileWriter;
import java.util.*;
import java.io.PrintWriter;
import java.nio.file.Files;

/*
VIP class. This class has constants/settings that generally do not change during a surf execution, and are set only at startup.
The settings can be public static fields of the Config class and can be read (but should not be written) directly by the rest of the code.
The settings are read by the properties in <user home dir>/surf.properties (or the file specified by -surf.properties=<file>)
Some settings have a default.

Similarly, resource files should be read only through this class. Resource files are not expected to change during one execution of surf.
 */
public class Config {
    private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.Config.class);
    // replacements applied at a per-token level

    public static final String admin = "hangal@ashoka.edu.in";
    public static final String ID_FIELD = "ID";
    public static String MERGE_FIELD = "Name";
    public static final int groupsPerPage = 100;
    public static final int DEFAULT_EDIT_DISTANCE = 2;
//    public static final int DEFAULT_COSINE_ACCURACY = 90;
    public static final int DEFAULT_MIN_TOKEN_OVERLAP = 2;
    public static final int DEFAULT_IGNORE_TOKEN_FREQUENCY = 200;
    public static final int DEFAULT_MIN_SPLITWEIGHT = 10; // a token in a field will be split only if it's constituent parts have appeared independently > 10 times. (However, there is an additional factor of 2x needed if the fields are only of length 3)
    public static final int DEFAULT_STREAK_LENGTH = 5;
    public static final int DEFAULT_MAX_HOLES = 1;
    public static final String SURF_HOME = System.getProperty("user.home") + File.separator + "surf-data";
    public static String PROPS_FILE = System.getProperty("user.home") + File.separator + "surf.properties"; // this need not be visible to the rest of surf

    /** SEE ALSO: we could refer to Metaphone 3 https://en.wikipedia.org/wiki/Metaphone#Metaphone_3 */
    static final String[] replacements = new String[]{
            "[^A-Za-z\\s]", "",

            // remove aspirations. these should happen before things like RAT=>RT, etc. e.g DASHARATHA => DASARATA -> DASARAT
            "TH", "T",
            "V", "W",
            "GH", "G",
            "BH", "B",
            "DH", "D",
            "JH", "J",
            "KH", "K",
            "MH", "M",
            "PH", "F",
            "SH", "S",
            "JH", "Z", // JHAVERI vs ZAVERI

            // safe replacements
            "Z", "S",
            "Y", "I",

            "NAYAK", "NAIK",
            "IYA", "IA", // # RAJORIA vs RAJORIYA
            "AGRA", "AGAR", // AGRAWAL vs AGARWAL
            "KER", "KAR", // SONKAR vs SONKER
            "HAR", "HR", // e.g. VOHARA vs VOHRA
         //   "HAT", "HT", // e.g. MAHATAB vs MAHTAB, but this breaks BHAT and makes it BT
            "RAT", "RT", // e.g. BHARATENDRA vs BHARTENDRA
            "RAJ", "RJ", // e.g. NEERJA vs NEERAJA
            "SAL", "SL", // e.g. BHONSALE vs BHONSLE
            "(.)NAG(.)", "$1NG$2", // e.g. WANAGE vs WANGE. Why, even HANAGAL vs HANGAL. but only if it's in the middle of a word. We don't want to convert NAG to NG or ANANTNAG to ANANTNG NAGALAND to NGALAND

            // could we convert the above to a general rule like consonant-vowel-consonant in the middle of a word can be converted to consonant-consonant, esp. if the vowel is A, E, U

            // suffix removal, do this before phonetic conversions.
            // but only if it has a minimum length. we don't want to replace SUKUMAR with SU or DULAL with DU.
            "(...)BAI$", "$1",
            "(...)BHAI$", "$1",
            "(...)BEN$", "$1",
            "(...)JI$", "$1",
            "(...)LAL$", "$1",
            "(...)KUMAR$", "$1",

            // phonetic corrections

            "AU", "OU",
            "OO", "U",
            "EE", "I",
            "KSH", "X",
            "Q", "K",
            "OW", "OU", // e.g. chowdhury, choudhury
             // standardize the mohammads
            "^MD$", "MOHAMMAD",
            "MOHAMAD", "MOHAMMAD",
            "MOHAMED", "MOHAMMAD",
            "^PD$", "PRASAD",
            "^PR$", "PRASAD",
            "SINH$", "SING",

            // remove an A at the end of a token, e.g. SHATRUGHAN vs SHATRUGHANA
//            "(.+)A$", "\\1"
    };

    private static final String[] ignoreTokens = new String[] {"MR", "MRS", "PROF", "DR",
            "SHRI", "SMT", "SHRIMATI", "KM", "SUSRI",
            "ENG", "ADV", "ADVOCATE",
            "COL", "GENERAL", "GEN", "RETD", "RETIRED",
            "ALIAS", "URF", "URPH",
            "CHH", // for CHHATRAPATI, as in SHRIMANT CHH. UDAYANRAJE PRATAPSINHA BHONSALE. Really?
            "SARDAR", "PANDIT", "PT", "MAULANA", "THIRU"};

    static final Set<String> ignoreTokensSet = new LinkedHashSet<>(Arrays.asList(ignoreTokens));

    static final boolean removeSuccessiveSameCharacters = true;

    // these will be customized per dataset, or even by the user at run time
    private static final String[] supplementaryColumns = new String[]{"Election_Type","Year", "Party", "Position", "Sex", "Statename", "Votes", "Poll_No"}; // supplementary columns to display. These are emitted as is, without any special processing
    //public static String[] sortColumns = new String[]{"Constituency_Name", "Year"}; // cols according to which we'll sort rows -- string vals only, integers won't work!
    public static String[] sortColumns;
    public static String[] showCols;
    public static final Map<String, List<String>> actualColumns =  new LinkedHashMap<>();
    public static final Map<String, List<String>> actualSortColumns =  new LinkedHashMap<>();
    public static final Map<String, String> keyToPath  = new LinkedHashMap<>();
    public static final Map<String, String> keyToDescription = new LinkedHashMap<>();
    public static Properties gitProps = null;

    static {
        createDatasets();
    }

    public static void createDatasets()
    {
        Properties props = readProperties();
        gitProps = new Properties();
        try { gitProps.load(getResourceAsStream("git.properties")); }
        catch (Exception e) { Util.print_exception("unable to load git.properties", e, log); }
        // new File("/path/directory").mkdirs();
        // props file should like like:
        // UP_Path: /Users/user/foo/bar/...
        // UP_Description: This is the dataset for UP
        // the "UP" part before _Path and _Description is what binds them together

        for (String key: props.stringPropertyNames()) {
            if (key.endsWith("_Path"))
                keyToPath.put(key.replace("_Path", ""), props.getProperty(key));
            if (key.endsWith("_Description"))
                keyToDescription.put(key.replace("_Description", ""), props.getProperty(key));
            if (key.endsWith("_Columns"))
            {
                //keyToColumns.put(key.replace("_Columns", ""), props.getProperty(key));
                ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(props.getProperty(key).split(",")));
                actualColumns.put(key.replace("_Columns", ""), arrayList);
            }
            if (key.endsWith("_SortBy"))
            {
                ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(props.getProperty(key).split(",")));
                actualSortColumns.put(key.replace("_SortBy", ""), arrayList);
            }
        }

        // do some sanity checking on each key
        Iterator<Map.Entry<String,String>> iter = keyToDescription.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,String> entry = iter.next();
            String key = entry.getKey(), value = entry.getValue();

            if (value == null || value.length() == 0) {
                iter.remove();
                log.warn ("Dataset key " + key + " does not have a valid description, dropping it.");
            }

            String path = keyToPath.get(key);
            File f = new File(path);
            if (!f.exists() || !f.canRead()) {
                iter.remove();
                log.warn("File for dataset with key " + key + " is not readable. Path = " + path);
            }
            
            if (actualColumns.get(key).isEmpty())
            {
                log.warn("Columns for dataset with key " + key + " are not specified. Reverting to supplementary columns.");
                ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(supplementaryColumns));
                actualColumns.put(key, arrayList);
                writeColumns(key, supplementaryColumns);
            }

            if (actualSortColumns.get(key).isEmpty())
            {
                log.warn("Sorting rule for dataset with key " + key + " is not specified. Reverting to default sorting.");
                ArrayList<String> arrayList = new ArrayList<>(actualColumns.get(key).subList(0, 1));
                actualSortColumns.put(key, arrayList);
            }
        }

        // lock these maps so there is no chance of accidental change
        // keyToPath = Collections.unmodifiableMap(keyToPath);
        // keyToDescription = Collections.unmodifiableMap(keyToDescription);

        {
            log.info("------------- Available datasets -----------------");
            for (String key: keyToPath.keySet()) {
                log.info ("Available dataset: " + keyToDescription.get(key) + " from path " + keyToPath.get(key));
            }
            log.info("------------- End available datasets -----------------");
        }
    }

    // return properties set from epadd.properties file and/or system properties
    private static Properties readProperties() {
        Properties props = new Properties();

        // PROPS_FILE is where the config is read from.
        // default <HOME>/surf.properties, but can be overridden by system property surf.properties
        String propsFile = System.getProperty("surf.properties");
        if (propsFile != null)
            PROPS_FILE = propsFile;

        File f = new File(PROPS_FILE);
        if(!f.exists())
        {
            String npath = Config.PROPS_FILE;
            File file = new File(npath);
            try{file.createNewFile();}
            catch(Exception e){
                log.warn("Unable to create " + PROPS_FILE);
            }
        }
        f = new File(PROPS_FILE);
        if (f.exists() && f.canRead()) {
            log.info("Reading configuration from: " + PROPS_FILE);
            try {
                InputStream is = new FileInputStream(PROPS_FILE);
                props.load(is);
                is.close();
            } catch (Exception e) {
                log.warn("Error reading Surf properties file " + PROPS_FILE + " " + e);
            }
        } else {
            log.warn("Surf properties file " + PROPS_FILE + " does not exist or is not readable");
        }

        // each individual property can further be overridden from the command line by a system property
        for (String key: props.stringPropertyNames()) {
            String val = System.getProperty (key);
            if (val != null && val.length() > 0)
                props.setProperty(key, val);
        }

        // {
        //     String val = props.getProperty("show-columns");
        //     if (!Util.nullOrEmpty(val)) {
        //         supplementaryColumns = val.split(",");
        //         for (int i = 0; i < supplementaryColumns.length; i++)
        //             supplementaryColumns[i] = supplementaryColumns[i].trim();
        //     }
        // }

        // {
        //     String val = props.getProperty("sort-columns");
        //     if (!Util.nullOrEmpty(val)) {
        //         sortColumns = val.split(",");
        //         for (int i = 0; i < supplementaryColumns.length; i++)
        //             sortColumns[i] = sortColumns[i].trim();
        //     }
        // }
        return props;
    }

    /** reads a resource with the given offset path. Resources should be read ONLY with this method, so there is a uniform way of finding and overriding resources.
     * Path components are always separated by forward slashes, just like resource paths in Java.
     * First looks in settings folder, then on classpath (e.g. inside war's WEB-INF/classes).
     **/
    private static InputStream getResourceAsStream(String path) {

        InputStream is = in.edu.ashoka.surf.Config.class.getClassLoader().getResourceAsStream(path);
        if (is == null)
            log.warn ("UNABLE TO READ RESOURCE FILE: " + path);
        return is;
    }

    public static void addDatasetToConfig(String path, String desc, String name, String headers) {
        String nkey = name.substring(0, name.lastIndexOf("."));  // strip out the ext. after the last part (.csv) and use that as the key
        String pathLabel = nkey + "_Path=";
        String descLabel = nkey + "_Description=";
        String colLabel = nkey + "_Columns=";
        String sortLabel = nkey + "_SortBy=";
        Properties props = new Properties();
        String pathValue = pathLabel.concat(path.replaceAll("\\\\", "\\\\\\\\")); //hacky fix for windows
        String descriptionValue = descLabel.concat(desc);
        String columnsValue = colLabel.concat(headers);
        String sortColValue = sortLabel.concat(headers.substring(0,headers.indexOf(",")));

        // PROPS_FILE is where the config is read from.
        // default <HOME>/surf.properties, but can be overridden by system property surf.properties
        String propsFile = System.getProperty("surf.properties");
        if (propsFile != null)
            PROPS_FILE = propsFile;

        File f = new File(PROPS_FILE);
        if (!f.exists()) {
            String npath = Config.PROPS_FILE;
            File file = new File(npath);
            try {
                file.createNewFile();
                f = new File(PROPS_FILE);
            } catch (Exception e) {
                log.warn("Unable to create surf.properties");
                return;
            }
        }
        
        if (f.exists() && f.canRead()) {
            try {
                InputStream is = new FileInputStream(PROPS_FILE);
                props.load(is);
                is.close();
            } catch (Exception e) {
                log.warn("Error reading Surf properties file " + PROPS_FILE + " " + e);
            }
            boolean flag = false;
            for (String key: props.stringPropertyNames()) {
                if(key.substring(0, key.lastIndexOf("_")).equals(nkey))
                {
                    flag = true;
                    props.remove(key);
                }
            }
            if(flag)
            {
                try
                {
                    PrintWriter out = new PrintWriter(new FileWriter(PROPS_FILE), true);
                    Files.lines(f.toPath()).filter(line -> !line.contains(nkey)).forEach(out::println);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    log.warn("Error deleting from props file\nException: "+e);
                }
            }
            log.info("Updating configuration in: " + PROPS_FILE);
            try {   
                FileWriter fw = new FileWriter(PROPS_FILE, true);
                fw.write(System.lineSeparator()+pathValue+System.lineSeparator()+descriptionValue+System.lineSeparator()+columnsValue+System.lineSeparator()+sortColValue);
                fw.close();
            } catch (Exception e) {
                log.warn("Error reading Surf properties file " + PROPS_FILE + " " + e);
            }
        } else {
            log.warn("Surf properties file " + PROPS_FILE + " does not exist or is not readable");
        }
        createDatasets();
    }

    private static void writeColumns(String key, String[] cols)
    {
        String propsFile = System.getProperty("surf.properties");
        if (propsFile != null)
            PROPS_FILE = propsFile;
        File f = new File(PROPS_FILE);
        if (f.exists() && f.canRead())
        {
            log.info("Updating configuration in: " + PROPS_FILE);
            try 
            {   
                FileWriter fw = new FileWriter(PROPS_FILE, true);
                fw.write(System.lineSeparator()+key+"_Columns=");
                for(int i=0; i<cols.length-1; i++)
                    fw.write(cols[i]+",");
                fw.write(cols[cols.length-1]);
                fw.close();
            } 
            catch (Exception e) 
            {
                log.warn("Error reading Surf properties file " + PROPS_FILE + " " + e);
            }
        } 
        else 
        {
            log.warn("Surf properties file " + PROPS_FILE + " does not exist or is not readable");
        }
    }


public static void refreshCols(String datasetKey)
    {
        Properties props = readProperties();
        gitProps = new Properties();
        try { gitProps.load(getResourceAsStream("git.properties")); }
        catch (Exception e) { Util.print_exception("unable to load git.properties", e, log); }
        String cols="";
        for (String key: props.stringPropertyNames()) {
            
            if(key.equalsIgnoreCase(datasetKey+"_Columns"))
            {
                cols = props.getProperty(key);
            }
        }
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(cols.split(",")));
        actualColumns.put(datasetKey, arrayList);            
    }
}
