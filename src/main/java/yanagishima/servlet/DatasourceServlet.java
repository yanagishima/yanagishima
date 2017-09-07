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

        if (yanagishimaConfig.isCheckDatasource()) {
            String header = HttpRequestUtil.getHeader(request, Constants.DATASOURCE_HEADER);
            if (header.equals("*")) {
                retVal.put("datasources", yanagishimaConfig.getDatasourceEngineList());
            } else {
                List<String> headerDatasources = Arrays.asList(header.split(","));
                List<String> allowedDatasources = yanagishimaConfig.getDatasources().stream().filter(datasource -> headerDatasources.contains(datasource)).collect(Collectors.toList());
                List<Map<String, List<String>>> datasourceEngineList = new ArrayList<>();
                for(String datasource : allowedDatasources) {
                    Map<String, List<String>> datasourceMap = new HashMap<>();
                    List<String> allEngines = yanagishimaConfig.getEngines();
                    List<String> engines = new ArrayList<>();
                    for(String engine : allEngines) {
                        if(yanagishimaConfig.getDatasources(engine).contains(datasource)) {
                            engines.add(engine);
                        }
                    }
                    datasourceMap.put(datasource, engines);
                    datasourceEngineList.add(datasourceMap);
                }
                retVal.put("datasources", datasourceEngineList);
            }
        } else {
            retVal.put("datasources", yanagishimaConfig.getDatasourceEngineList());
        }

        JsonUtil.writeJSON(response, retVal);
    }

}
