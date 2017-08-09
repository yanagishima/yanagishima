package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.HiveQueryErrorException;
import yanagishima.result.HiveQueryResult;
import yanagishima.row.Query;
import yanagishima.service.HiveService;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Optional;

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

                String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
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
                if (userName != null) {
                    LOGGER.info(String.format("%s executed %s in %s", userName, query, datasource));
                }

                boolean storeFlag = Boolean.parseBoolean(Optional.ofNullable(request.getParameter("store")).orElse("false"));
                int limit = yanagishimaConfig.getSelectLimit();
                try {
                    HiveQueryResult hiveQueryResult = hiveService.doQuery(datasource, query, userName, storeFlag, limit);
                    String queryid = hiveQueryResult.getQueryId();
                    retVal.put("queryid", queryid);
                    retVal.put("headers", hiveQueryResult.getColumns());
                    retVal.put("results", hiveQueryResult.getRecords());

                    retVal.put("lineNumber", Integer.toString(hiveQueryResult.getLineNumber()));
                    retVal.put("rawDataSize", hiveQueryResult.getRawDataSize().toString());
                    Optional<String> warningMessageOptinal = Optional.ofNullable(hiveQueryResult.getWarningMessage());
                    warningMessageOptinal.ifPresent(warningMessage -> {
                        retVal.put("warn", warningMessage);
                    });
                    Optional<Query> queryDataOptional = db.single(Query.class).where("query_id=? and datasource=?", queryid, datasource).execute();
                    queryDataOptional.ifPresent(queryData -> {
                        LocalDateTime submitTimeLdt = LocalDateTime.parse(queryid.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
                        String fetchResultTimeString = queryData.getFetchResultTimeString();
                        ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
                        long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
                        retVal.put("elapsedTimeMillis", elapsedTimeMillis);
                    });
                } catch (HiveQueryErrorException e) {
                    LOGGER.error(e.getMessage(), e);
                    retVal.put("queryid", e.getQueryId());
                    retVal.put("error", e.getMessage());
                }

            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                retVal.put("error", e.getMessage());
            }
        });


        JsonUtil.writeJSON(response, retVal);

    }
}
