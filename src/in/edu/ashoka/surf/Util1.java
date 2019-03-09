package in.edu.ashoka.surf;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import in.edu.ashoka.surf.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Util1 {
    private static final Log log = LogFactory.getLog(in.edu.ashoka.surf.Util1.class);

    private static final boolean RUNNING_ON_JETTY = false;

    // key finding after a lot of experimentation with jetty and tomcat.
    // make all pages UTF-8 encoded.
    // setRequestEncoding("UTF-8") before reading any parameter
    // even with this, with tomcat, GET requests are iso-8859-1.
    // so convert in that case only...
    // converts an array of strings from iso-8859-1 to utf8. useful for converting i18n chars in http request parameters
    private static String convertRequestParamToUTF8(String param) {
        if (RUNNING_ON_JETTY)
        {
            log.info("running on jetty: no conversion for " + param);
            return param;
        }
        if (param == null)
            return null;
        String newParam = new String(param.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        if (!newParam.equals(param))
            log.info("Converted to utf-8: " + param + " -> " + newParam);
        return newParam;
    }

    /**
     * converts an array of strings from iso-8859-1 to utf8. useful for
     * converting i18n chars in http request parameters.
     * if throwExceptionIfUnsupportedEncoding is true, throws an exception,
     * otherwise returns
     */
    private static String[] convertRequestParamsToUTF8(String params[], boolean throwExceptionIfUnsupportedEncoding) throws UnsupportedEncodingException {
        if (RUNNING_ON_JETTY)
            return params;
        if (params == null)
            return null;

        // newParams will contain only the strings that successfully can be converted to utf-8
        // others will be reported and ignored
        List<String> newParams = new ArrayList<>();
        for (String param : params) {
            newParams.add(convertRequestParamToUTF8(param));
        }
        return newParams.toArray(new String[newParams.size()]);
    }

    static int editDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        // len1+1, len2+1, because finally return dp[len1][len2]
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);

                //if last two chars equal
                if (c1 == c2) {
                    //update dp value for +1 length
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }

        return dp[len1][len2];
    }

    public static Multimap<String, String> convertRequestToMultimap(HttpServletRequest request) {
        Multimap<String, String> params = LinkedHashMultimap.create();
        {
            if (true) {
                // regular file encoding
                Enumeration<String> paramNames = request.getParameterNames();

                while (paramNames.hasMoreElements()) {
                    String param = paramNames.nextElement();
                    String[] vals = request.getParameterValues(param);
                    if (vals != null)
                        for (String val : vals)
                            params.put(param, convertRequestParamToUTF8(val));
                }
            }
        }

        return params;
    }

    /** a safe parseInt that doesn't care about NumberFormatException, returns defaultVal instead */
    public static int parseInt (String x, int defaultValue) {
        try {
            int result = Integer.parseInt (x);
            return result;
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static Map<String, String> convertRequestToMap(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        {
            if (true) {
                // regular file encoding
                Enumeration<String> paramNames = request.getParameterNames();

                while (paramNames.hasMoreElements()) {
                    String param = paramNames.nextElement();
                    String[] vals = request.getParameterValues(param);
                    if (vals != null)
                        for (String val : vals)
                            params.put(param, convertRequestParamToUTF8(val));
                }
            }
        }

        return params;
    }
}

