package yanagishima.util;

import static java.lang.String.format;

import javax.servlet.http.HttpServletRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class HttpRequestUtil {
  public static String getRequiredHeader(HttpServletRequest request, String key) {
    String value = request.getHeader(key);
    if (value == null) {
      throw new IllegalArgumentException(format("Missing required header '%s'", key));
    }
    return value;
  }
}
