package yanagishima.util;

import javax.servlet.http.HttpServletRequest;

import static java.lang.String.format;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class HttpRequestUtil {
    public static String getOrDefaultParameter(HttpServletRequest request, String key, String defaultValue) {
        String value = request.getParameter(key);
        return value == null ? defaultValue : value;
    }

    public static boolean getOrDefaultParameter(HttpServletRequest request, String key, boolean defaultValue) {
        String value = request.getParameter(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static String getRequiredParameter(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        if (value == null) {
            throw new IllegalArgumentException(format("Missing required parameter '%s'", key));
        }
        return value;
    }

    public static String getRequiredHeader(HttpServletRequest request, String key) {
        String value = request.getHeader(key);
        if (value == null) {
            throw new IllegalArgumentException(format("Missing required header '%s'", key));
        }
        return value;
    }
}
