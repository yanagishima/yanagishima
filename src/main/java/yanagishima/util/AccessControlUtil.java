package yanagishima.util;

import com.google.common.base.Splitter;

import javax.servlet.http.HttpServletRequest;

import static yanagishima.util.HttpRequestUtil.getRequiredHeader;

public final class AccessControlUtil {
    private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private AccessControlUtil() {}

    public static boolean validateDatasource(HttpServletRequest request, String datasource) {
        String header = getRequiredHeader(request, Constants.DATASOURCE_HEADER);
        if (header.equals("*")) {
            return true;
        }
        return SPLITTER.splitToList(header).contains(datasource);
    }
}
