package yanagishima.servlet;

import com.google.common.collect.ImmutableMap;
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
public class DatasourceAuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public DatasourceAuthServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        if (yanagishimaConfig.isCheckDatasource()) {
            String header = HttpRequestUtil.getHeader(request, Constants.DATASOURCE_HEADER);
            if (header.equals("*")) {
                retVal.put("datasources", getDatasourceEngineList(yanagishimaConfig.getDatasources()));
            } else {
                List<String> headerDatasources = Arrays.asList(header.split(","));
                List<String> allowedDatasources = yanagishimaConfig.getDatasources().stream().filter(datasource -> headerDatasources.contains(datasource)).collect(Collectors.toList());
                retVal.put("datasources", getDatasourceEngineList(allowedDatasources));
            }
        } else {
            retVal.put("datasources", getDatasourceEngineList(yanagishimaConfig.getDatasources()));
        }

        JsonUtil.writeJSON(response, retVal);
    }

    private List<Map<String, Map<String, Object>>> getDatasourceEngineList(List<String> allowedDatasources) {
        List<Map<String, Map<String, Object>>> datasourceEngineList = new ArrayList<>();
        for(String datasource : allowedDatasources) {
            Map<String, Map<String, Object>> datasourceMap = new HashMap<>();
            List<String> allEngines = yanagishimaConfig.getEngines();
            List<String> engines = new ArrayList<>();
            for(String engine : allEngines) {
                if(yanagishimaConfig.getDatasources(engine).contains(datasource)) {
                    engines.add(engine);
                }
            }
            datasourceMap.put(datasource, new ImmutableMap.Builder<String, Object>().put("engines", engines).put("auth", yanagishimaConfig.isAuth(datasource)).put("metadataService", yanagishimaConfig.isMetadataService(datasource)).build());
            datasourceEngineList.add(datasourceMap);
        }
        return datasourceEngineList;
    }

}
