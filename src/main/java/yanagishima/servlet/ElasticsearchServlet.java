package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.ElasticsearchQueryErrorException;
import yanagishima.result.ElasticsearchQueryResult;
import yanagishima.row.Query;
import yanagishima.service.ElasticsearchService;

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
import java.util.Map;
import java.util.Optional;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class ElasticsearchServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServlet.class);
    private static final long serialVersionUID = 1L;

    private final ElasticsearchService elasticsearchService;
    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public ElasticsearchServlet(ElasticsearchService elasticsearchService, YanagishimaConfig config, TinyORM db) {
        this.elasticsearchService = elasticsearchService;
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> resnponseBody = new HashMap<>();
        String query = request.getParameter("query");
        if (query == null) {
            writeJSON(response, resnponseBody);
            return;
        }

        try {
            String userName = null;
            if (config.isUseAuditHttpHeaderName()) {
                userName = request.getHeader(config.getAuditHttpHeaderName());
            }
            if (config.isUserRequired() && userName == null) {
                sendForbiddenError(response);
                return;
            }
            try {
                String datasource = getRequiredParameter(request, "datasource");
                if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                    sendForbiddenError(response);
                    return;
                }
                if (userName != null) {
                    LOGGER.info(String.format("%s executed %s in %s", userName, query, datasource));
                }
                ElasticsearchQueryResult queryResult = executeQuery(request, query, datasource, userName);

                resnponseBody.put("queryid", queryResult.getQueryId());
                resnponseBody.put("headers", queryResult.getColumns());
                resnponseBody.put("results", queryResult.getRecords());
                resnponseBody.put("lineNumber", Integer.toString(queryResult.getLineNumber()));
                resnponseBody.put("rawDataSize", queryResult.getRawDataSize().toString());
                // TODO: Make ElasticsearchQueryResult.warningMessage Optional<String>
                Optional.ofNullable(queryResult.getWarningMessage()).ifPresent(warningMessage ->
                    resnponseBody.put("warn", warningMessage)
                );
                db.single(Query.class).where("query_id=? and datasource=? and engine=?", queryResult.getQueryId(), datasource, "elasticsearch").execute().ifPresent(queryData ->
                    resnponseBody.put("elapsedTimeMillis", toElapsedTimeMillis(queryResult.getQueryId(), queryData))
                );
            } catch (ElasticsearchQueryErrorException e) {
                LOGGER.error(e.getMessage(), e);
                resnponseBody.put("queryid", e.getQueryId());
                resnponseBody.put("error", e.getCause().getMessage());
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                resnponseBody.put("error", e.getMessage());
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            resnponseBody.put("error", e.getMessage());
        }
        writeJSON(response, resnponseBody);
    }

    private ElasticsearchQueryResult executeQuery(HttpServletRequest request, String query, String datasource, String userName) throws ElasticsearchQueryErrorException {
        int limit = query.startsWith(YANAGISHIMA_COMMENT) ? Integer.MAX_VALUE : config.getSelectLimit();

        if (request.getParameter("translate") != null) {
            return elasticsearchService.doTranslate(datasource, query, userName, true, limit);
        }
        if (query.startsWith(YANAGISHIMA_COMMENT)) {
            return elasticsearchService.doQuery(datasource, query, userName, false, limit);
        }
        return elasticsearchService.doQuery(datasource, query, userName, true, limit);
    }

    private static long toElapsedTimeMillis(String queryId, Query query) {
        LocalDateTime submitTimeLdt = LocalDateTime.parse(queryId.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
        String fetchResultTimeString = query.getFetchResultTimeString();
        ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
        return ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
    }
}
