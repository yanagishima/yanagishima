package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;
import yanagishima.util.Constants;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class DatasourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public DatasourceServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        Map<String, List<String>> datasourceEngineMap = yanagishimaConfig.getDatasourceEngineMap();
        if (yanagishimaConfig.isCheckDatasource()) {
            String header = HttpRequestUtil.getHeader(request, Constants.DATASOURCE_HEADER);
            if (header.equals("*")) {
                retVal.put("datasources", datasourceEngineMap);
            } else {
                List<String> headerDatasources = Arrays.asList(header.split(","));
                List<String> allowedDatasources = yanagishimaConfig.getDatasources().stream().filter(datasource -> headerDatasources.contains(datasource)).collect(Collectors.toList());
                for (String datasource : allowedDatasources) {
                    if(!datasourceEngineMap.containsKey(datasource)) {
                        datasourceEngineMap.remove(datasource);
                    }
                }
                retVal.put("datasources", datasourceEngineMap);
            }
        } else {
            retVal.put("datasources", datasourceEngineMap);
        }

        JsonUtil.writeJSON(response, retVal);
    }

}
