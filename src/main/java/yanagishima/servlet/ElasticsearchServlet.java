package yanagishima.servlet;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.ElasticsearchQueryErrorException;
import yanagishima.repository.TinyOrm;
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

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Slf4j
@Singleton
public class ElasticsearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ElasticsearchService elasticsearchService;
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @Inject
    public ElasticsearchServlet(ElasticsearchService elasticsearchService, YanagishimaConfig config, TinyOrm db) {
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
            String user = config.isUseAuditHttpHeaderName() ? request.getHeader(config.getAuditHttpHeaderName()) : null;
            if (config.isUserRequired() && user == null) {
                sendForbiddenError(response);
                return;
            }
            try {
                String datasource = getRequiredParameter(request, "datasource");
                if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                    sendForbiddenError(response);
                    return;
                }
                if (user != null) {
                    log.info(format("%s executed %s in %s", user, query, datasource));
                }
                ElasticsearchQueryResult queryResult = executeQuery(request, query, datasource, user);

                resnponseBody.put("queryid", queryResult.getQueryId());
                resnponseBody.put("headers", queryResult.getColumns());
                resnponseBody.put("results", queryResult.getRecords());
                resnponseBody.put("lineNumber", Integer.toString(queryResult.getLineNumber()));
                resnponseBody.put("rawDataSize", queryResult.getRawDataSize().toString());
                // TODO: Make ElasticsearchQueryResult.warningMessage Optional<String>
                Optional.ofNullable(queryResult.getWarningMessage()).ifPresent(warningMessage ->
                    resnponseBody.put("warn", warningMessage)
                );
                db.singleQuery("query_id=? and datasource=? and engine=?", queryResult.getQueryId(), datasource, "elasticsearch").ifPresent(queryData ->
                    resnponseBody.put("elapsedTimeMillis", toElapsedTimeMillis(queryResult.getQueryId(), queryData))
                );
            } catch (ElasticsearchQueryErrorException e) {
                log.error(e.getMessage(), e);
                resnponseBody.put("queryid", e.getQueryId());
                resnponseBody.put("error", e.getCause().getMessage());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                resnponseBody.put("error", e.getMessage());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
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
