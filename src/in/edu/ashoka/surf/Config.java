package in.edu.ashoka.surf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/*
VIP class. This class has constants/settings that generally do not change during an ePADD execution, and are set only at startup.
The settings can be public static fields of the Config class and can be read (but should not be written) directly by the rest of the code.
The settings are read by the properties in <user home dir>/epadd.properties (or the file specified by -Depadd.properties=<file>)
Some settings have a default.

Similarly, resource files should be read only through this class. Resource files are not expected to change during one execution of epadd.
 */
public class Config {
    public static Log log = LogFactory.getLog(in.edu.ashoka.surf.Config.class);
    // replacements applied at a per-token level

    public static String admin = "hangal@ashoka.edu.in";
    public static String ID_FIELD = "ID", MERGE_FIELD = "Name";

    /** SEE ALSO: we could refer to Metaphone 3 https://en.wikipedia.org/wiki/Metaphone#Metaphone_3 */
    static String[] replacements = new String[]{
            "[^A-Za-z\\s]", "",
            "NAYAK", "NAIK",
            "IYA", "IA", // # RAJORIA vs RAJORIYA
            "AGRA", "AGAR", // AGRAWAL vs AGARWAL
            "KER", "KAR", // SONKAR vs SONKER
            "HAR", "HR", // e.g. VOHARA vs VOHRA
            "HAT", "HT", // e.g. MAHATAB vs MAHTAB
            "RAT", "RT", // e.g. BHARATENDRA vs BHARTENDRA
            "RAJ", "RJ", // e.g. NEERJA vs NEERAJA
            "SAL", "SL", // e.g. BHONSALE vs BHONSLE
            "NAG", "NG", // e.g. WANAGE vs WANGE. Why, even HANAGAL vs HANGAL

            // suffix removal, do this before phonetic conversions
            "BAI$", "",
            "BHAI$", "",
            "BEN$", "",
            "JI$", "",
            "LAL$", "",
            "KUMAR$", "",

            // phonetic corrections
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
            "Z", "S",
            "Y", "I",
            "AU", "OU",
            "OO", "U",
            "EE", "I",
            "KSH", "X",
            "Q", "K",
            "OW", "OU", // e.g. chowdhury, choudhury
             // standardize the mohammads
            "MD", "MOHAMMAD",
            "MOHAMAD", "MOHAMMAD",
            "MOHAMED", "MOHAMMAD",
            "PD", "PRASAD",
            "PR", "PRASAD",
            "SINH$", "SING",

            // remove an A at the end of a token, e.g. SHATRUGHAN vs SHATRUGHANA
//            "(.+)A$", "\\1"
    };

    static String ignoreTokens[] = new String[] {"MR", "MRS", "PROF", "DR",
            "SHRI", "SMT", "SHRIMATI", "KM", "SUSRI",
            "ENG", "ADV", "ADVOCATE",
            "COL", "GENERAL", "GEN", "RETD", "RETIRED",
            "ALIAS", "URF", "URPH",
            "CHH", // for CHHATRAPATI, as in SHRIMANT CHH. UDAYANRAJE PRATAPSINHA BHONSALE. Really?
            "SARDAR", "PANDIT", "PT", "MAULANA", "THIRU"};

    static Set<String> ignoreTokensSet = new LinkedHashSet<>(Arrays.asList(ignoreTokens));

    static boolean removeSuccessiveSameCharacters = true;

    // these will be customized per dataset, or even by the user at run time
    public static String[] columnsToDisplay = new String[]{"Name", "Sex", "Year", "Constituency", "Party", "State", "Position", "Votes"}; // State can probably be taken out for AE, and put in only for GE
    public static String mappedIdColumn = "pid"; // name of column into which output id's will be written
    public static String primaryColumn = "Name"; // name of primary column that is being merged
    public static String secondaryColumn = "AC_no"; // name of secondary column by which fields can be grouped (may or may not be visible on screen)
    public static String filterColumn = "Position"; // name of column on which filter can be set. (should be expandable to accommodate multiple columns)
    public static String[] filterColumnValues = new String[]{"1", "2", "3"}; // allowed values for filterColumn when set

    private static String PROPS_FILE = System.getProperty("user.home") + File.separator + "surf.properties"; // this need not be visible to the rest of ePADD
    public static Map<String, String> keyToPath  = new LinkedHashMap<>(), keyToDescription = new LinkedHashMap<>();

    static {
        Properties props = readProperties();

        // props file should like like:
        // UP_Path: /Users/user/foo/bar/...
        // UP_Description: This is the dataset for UP
        // the "UP" part before _Path and _Description is what binds them together

        for (String key: props.stringPropertyNames()) {
            if (key.endsWith("_Path"))
                keyToPath.put(key.replace("_Path", ""), props.getProperty(key));
            if (key.endsWith("_Description"))
                keyToDescription.put(key.replace("_Description", ""), props.getProperty(key));
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
        }

        // lock these maps so there is no chance of accidental change
        keyToPath = Collections.unmodifiableMap(keyToPath);
        keyToDescription = Collections.unmodifiableMap(keyToDescription);

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
        if (f.exists() && f.canRead()) {
            log.info("Reading configuration from: " + PROPS_FILE);
            try {
                InputStream is = new FileInputStream(PROPS_FILE);
                props.load(is);
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
        return props;
    }
}
