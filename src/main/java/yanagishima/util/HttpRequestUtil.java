package yanagishima.util;

import javax.servlet.http.HttpServletRequest;

import static java.lang.String.format;

public final class HttpRequestUtil {
    private HttpRequestUtil() {}

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
