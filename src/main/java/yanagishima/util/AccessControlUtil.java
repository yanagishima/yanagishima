package yanagishima.util;


import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import yanagishima.util.Constants;

public class AccessControlUtil {

    public static void checkDatasource(HttpServletRequest request, String datasource) {

        String header = HttpRequestUtil.getHeader(request, Constants.DATASOURCE_HEADER);
        if(header.equals("*")) {
            return;
        }
        List<String> headerDatasources = Arrays.asList(header.split(","));
        if(!headerDatasources.contains(datasource)) {
            throw new RuntimeException("Denied access to " + datasource);
        }
    }

}