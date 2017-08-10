package yanagishima.util;

import java.util.Properties;

public class PropertiesUtil {

    public static String getParam(Properties properties, String key) {
        String p = properties.getProperty(key);
        if (p == null) {
            throw new RuntimeException("Missing required parameter '" + key + "'.");
        } else {
            return p;
        }
    }
}
