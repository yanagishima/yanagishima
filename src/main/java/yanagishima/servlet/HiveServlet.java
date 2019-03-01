package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.HiveQueryErrorException;
import yanagishima.result.HiveQueryResult;
import yanagishima.service.HiveService;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;
import yanagishima.util.MetadataUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class HiveServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(HiveServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    private final HiveService hiveService;

    @Inject
    public HiveServlet(YanagishimaConfig yanagishimaConfig, HiveService hiveService) {
        this.yanagishimaConfig = yanagishimaConfig;
        this.hiveService = hiveService;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
        queryOptional.ifPresent(query -> {
            try {
                String userName = null;
                Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
                Optional<String> hivePassword = Optional.ofNullable(request.getParameter("password"));
                if(yanagishimaConfig.isUseAuditHttpHeaderName()) {
                    userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
                } else {
                    if (hiveUser.isPresent() && hivePassword.isPresent()) {
                        userName = hiveUser.get();
                    }
                }
                if (yanagishimaConfig.isUserRequired() && userName == null) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                String datasource = HttpRequestUtil.getParam(request, "datasource");
                if (yanagishimaConfig.isCheckDatasource()) {
                    if (!AccessControlUtil.validateDatasource(request, datasource)) {
                        try {
                            response.sendError(SC_FORBIDDEN);
                            return;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                String engine = HttpRequestUtil.getParam(request, "engine");
                if (userName != null) {
                    LOGGER.info(String.format("%s executed %s in datasource=%s, engine=%s", userName, query, datasource, engine));
                }

                boolean storeFlag = Boolean.parseBoolean(Optional.ofNullable(request.getParameter("store")).orElse("false"));
                int limit = yanagishimaConfig.getSelectLimit();
                try {
                    HiveQueryResult hiveQueryResult = hiveService.doQuery(engine, datasource, query, userName, hiveUser, hivePassword, storeFlag, limit);
                    String queryid = hiveQueryResult.getQueryId();
                    retVal.put("queryid", queryid);
                    retVal.put("headers", hiveQueryResult.getColumns());
                    if(query.startsWith("SHOW SCHEMAS")) {
                        List<String> invisibleDatabases = yanagishimaConfig.getInvisibleDatabases(datasource);
                        retVal.put("results", hiveQueryResult.getRecords().stream().filter(list -> !invisibleDatabases.contains(list.get(0))).collect(Collectors.toList()));
                    } else {
                        retVal.put("results", hiveQueryResult.getRecords());
                    }
                    retVal.put("lineNumber", Integer.toString(hiveQueryResult.getLineNumber()));
                    retVal.put("rawDataSize", hiveQueryResult.getRawDataSize().toString());
                    Optional<String> warningMessageOptinal = Optional.ofNullable(hiveQueryResult.getWarningMessage());
                    warningMessageOptinal.ifPresent(warningMessage -> {
                        retVal.put("warn", warningMessage);
                    });
                    if(query.startsWith("DESCRIBE")) {
                        if(yanagishimaConfig.getMetadataServiceUrl(datasource).isPresent()) {
                            String[] strings = query.substring("DESCRIBE ".length()).split("\\.");
                            String schema = strings[0];
                            String table = null;
                            if(engine.equals("hive")) {
                                table = strings[1].substring(1, strings[1].length() - 1);
                            } else if(engine.equals("spark")) {
                                table = strings[1];
                            } else {
                                throw new IllegalArgumentException(engine + " is illegal");
                            }
                            MetadataUtil.setMetadata(yanagishimaConfig.getMetadataServiceUrl(datasource).get(), retVal, schema, table, hiveQueryResult.getRecords());
                        }
                    }
                } catch (HiveQueryErrorException e) {
                    LOGGER.error(e.getMessage());
                    retVal.put("queryid", e.getQueryId());
                    retVal.put("error", e.getCause().getMessage());
                }

            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                retVal.put("error", e.getMessage());
            }
        });


        JsonUtil.writeJSON(response, retVal);

    }
}
