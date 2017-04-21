package yanagishima.util;


import javax.servlet.http.HttpServletRequest;

public class HttpRequestUtil {

    public static String getParam(HttpServletRequest request, String name) {
        String p = request.getParameter(name);
        if (p == null) {
            throw new RuntimeException("Missing required parameter '" + name + "'.");
        } else {
            return p;
        }
    }

    public static String getHeader(HttpServletRequest request, String name) {
        String p = request.getHeader(name);
        if (p == null) {
            throw new RuntimeException("Missing required header '" + name + "'.");
        } else {
            return p;
        }
    }
}
