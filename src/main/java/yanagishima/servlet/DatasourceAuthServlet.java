package yanagishima.servlet;

import static yanagishima.util.Constants.DATASOURCE_HEADER;
import static yanagishima.util.HttpRequestUtil.getRequiredHeader;
import static yanagishima.util.JsonUtil.writeJSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import yanagishima.config.YanagishimaConfig;

@Singleton
public class DatasourceAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;

    @Inject
    public DatasourceAuthServlet(YanagishimaConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        if (config.isCheckDatasource()) {
            String header = getRequiredHeader(request, DATASOURCE_HEADER);
            if (header.equals("*")) {
                responseBody.put("datasources", getDatasourceEngineList(config.getDatasources()));
            } else {
                List<String> headerDatasources = Arrays.asList(header.split(","));
                List<String> allowedDatasources = config.getDatasources().stream().filter(headerDatasources::contains).collect(Collectors.toList());
                responseBody.put("datasources", getDatasourceEngineList(allowedDatasources));
            }
        } else {
            responseBody.put("datasources", getDatasourceEngineList(config.getDatasources()));
        }
        writeJSON(response, responseBody);
    }

    private List<Map<String, Map<String, Object>>> getDatasourceEngineList(List<String> allowedDatasources) {
        List<Map<String, Map<String, Object>>> datasourceEngines = new ArrayList<>();
        for (String datasource : allowedDatasources) {
            List<String> engines = config.getEngines().stream()
                                         .filter(engine -> config.getDatasources(engine).contains(datasource))
                                         .collect(Collectors.toList());

            Map<String, Object> context = Map.of(
                    "engines", engines,
                    "auth", config.isAuth(datasource),
                    "metadataService", false, // Deprecated
                    "datetimePartitionHasHyphen", config.isDatatimePartitionHasHyphen(datasource));

            datasourceEngines.add(Map.of(datasource, context));
        }
        return datasourceEngines;
    }
}
