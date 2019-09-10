package yanagishima.util;


import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static yanagishima.util.HttpRequestUtil.getRequiredHeader;

public class AccessControlUtil {

    public static boolean validateDatasource(HttpServletRequest request, String datasource) {

        String header = getRequiredHeader(request, Constants.DATASOURCE_HEADER);
        if(header.equals("*")) {
            return true;
        }
        List<String> headerDatasources = Arrays.asList(header.split(","));
        if(!headerDatasources.contains(datasource)) {
            return false;
        }
        return true;
    }
}
