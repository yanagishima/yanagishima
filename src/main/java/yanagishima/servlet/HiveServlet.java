package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.bean.HttpRequestContext;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.HiveQueryErrorException;
import yanagishima.result.HiveQueryResult;
import yanagishima.service.HiveService;
import yanagishima.util.AccessControlUtil;
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

import static java.util.Objects.requireNonNull;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class HiveServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveServlet.class);
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final HiveService hiveService;

    @Inject
    public HiveServlet(YanagishimaConfig config, HiveService hiveService) {
        this.config = config;
        this.hiveService = hiveService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpRequestContext context = new HttpRequestContext(request);
        HashMap<String, Object> retVal = new HashMap<>();
        Optional.ofNullable(context.getQuery()).ifPresent(query -> {
            try {
                requireNonNull(context.getDatasource(), "datasource is null");
                requireNonNull(context.getEngine(), "engine is null");

                String userName = null;
                Optional<String> hiveUser = Optional.ofNullable(context.getUser());
                Optional<String> hivePassword = Optional.ofNullable(context.getPassword());
                if(config.isUseAuditHttpHeaderName()) {
                    userName = request.getHeader(config.getAuditHttpHeaderName());
                } else {
                    if (hiveUser.isPresent() && hivePassword.isPresent()) {
                        userName = hiveUser.get();
                    }
                }
                if (config.isUserRequired() && userName == null) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (config.isCheckDatasource()) {
                    if (!AccessControlUtil.validateDatasource(request, context.getDatasource())) {
                        try {
                            response.sendError(SC_FORBIDDEN);
                            return;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (userName != null) {
                    LOGGER.info(String.format("%s executed %s in datasource=%s, engine=%s", userName, query, context.getDatasource(), context.getEngine()));
                }

                boolean storeFlag = Boolean.parseBoolean(Optional.ofNullable(context.getStore()).orElse("false"));
                int limit = config.getSelectLimit();
                try {
                    HiveQueryResult hiveQueryResult = hiveService.doQuery(context.getEngine(), context.getDatasource(), query, userName, hiveUser, hivePassword, storeFlag, limit);
                    String queryid = hiveQueryResult.getQueryId();
                    retVal.put("queryid", queryid);
                    retVal.put("headers", hiveQueryResult.getColumns());
                    if(query.startsWith("SHOW SCHEMAS")) {
                        List<String> invisibleDatabases = config.getInvisibleDatabases(context.getDatasource());
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
                        if(config.getMetadataServiceUrl(context.getDatasource()).isPresent()) {
                            String[] strings = query.substring("DESCRIBE ".length()).split("\\.");
                            String schema = strings[0];
                            String table = null;
                            if("hive".equals(context.getEngine())) {
                                table = strings[1].substring(1, strings[1].length() - 1);
                            } else if("spark".equals(context.getEngine())) {
                                table = strings[1];
                            } else {
                                throw new IllegalArgumentException(context.getEngine() + " is illegal");
                            }
                            MetadataUtil.setMetadata(config.getMetadataServiceUrl(context.getDatasource()).get(), retVal, schema, table, hiveQueryResult.getRecords());
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
        writeJSON(response, retVal);
    }
}
