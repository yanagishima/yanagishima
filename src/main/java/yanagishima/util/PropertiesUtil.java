package yanagishima.util;

import java.util.Properties;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class PropertiesUtil {
  public static String getParam(Properties properties, String key) {
    String value = properties.getProperty(key);
    if (value == null) {
      throw new RuntimeException("Missing required parameter '" + key + "'.");
    }
    return value;
  }
}
