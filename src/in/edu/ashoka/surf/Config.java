package in.edu.ashoka.surf;

import edu.stanford.muse.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    public static int groupsPerPage = 100;
    public static final int DEFAULT_EDIT_DISTANCE = 2;
    public static final int DEFAULT_MIN_TOKEN_OVERLAP = 2;
    public static final int DEFAULT_IGNORE_TOKEN_FREQUENCY = 200;
    public static final int DEFAULT_MIN_SPLITWEIGHT = 10; // a token in a field will be split only if it's constituent parts have appeared independently > 10 times. (However, there is an additional factor of 2x needed if the fields are only of length 3)

    /** SEE ALSO: we could refer to Metaphone 3 https://en.wikipedia.org/wiki/Metaphone#Metaphone_3 */
    static String[] replacements = new String[]{
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
    public static String[] supplementaryColumns = new String[]{"Year", "Party", "Position", "Sex", "Statename", "Votes"}; // supplementary columns to display. These are emitted as is, without any special processing
    public static String[] sortColumns = new String[]{"Constituency_Name", "Year"}; // cols according to which we'll sort rows -- string vals only, integers won't work!

    private static String PROPS_FILE = System.getProperty("user.home") + File.separator + "surf.properties"; // this need not be visible to the rest of ePADD
    public static Map<String, String> keyToPath  = new LinkedHashMap<>(), keyToDescription = new LinkedHashMap<>();
    public static Properties gitProps = null;

    static {
        Properties props = readProperties();
        gitProps = new Properties();
        try { gitProps.load(getResourceAsStream("git.properties")); }
        catch (Exception e) { Util.print_exception("unable to load git.properties", e, log); }

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

    /** reads a resource with the given offset path. Resources should be read ONLY with this method, so there is a uniform way of finding and overriding resources.
     * Path components are always separated by forward slashes, just like resource paths in Java.
     * First looks in settings folder, then on classpath (e.g. inside war's WEB-INF/classes).
     **/
    public static InputStream getResourceAsStream(String path) {

        InputStream is = in.edu.ashoka.surf.Config.class.getClassLoader().getResourceAsStream(path);
        if (is == null)
            log.warn ("UNABLE TO READ RESOURCE FILE: " + path);
        return is;
    }
}
