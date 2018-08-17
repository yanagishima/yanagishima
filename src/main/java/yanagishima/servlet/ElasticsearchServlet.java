package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.ElasticsearchQueryErrorException;
import yanagishima.result.ElasticsearchQueryResult;
import yanagishima.row.Query;
import yanagishima.service.ElasticsearchService;
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
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

@Singleton
public class ElasticsearchServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServlet.class);

    private static final long serialVersionUID = 1L;

    private final ElasticsearchService elasticsearchService;

    private final YanagishimaConfig yanagishimaConfig;

    @Inject
    private TinyORM db;

    @Inject
    public ElasticsearchServlet(ElasticsearchService elasticsearchService, YanagishimaConfig yanagishimaConfig) {
        this.elasticsearchService = elasticsearchService;
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
            queryOptional.ifPresent(query -> {
                String userName = null;
                if (yanagishimaConfig.isUseAuditHttpHeaderName()) {
                    userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
                }
                if (yanagishimaConfig.isUserRequired() && userName == null) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
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
                    int limit;
                    if (query.startsWith(YANAGISHIMA_COMMENT)) {
                        limit = Integer.MAX_VALUE;
                    } else {
                        limit = yanagishimaConfig.getSelectLimit();
                    }
                    ElasticsearchQueryResult elasticsearchQueryResult = null;
                    if (request.getParameter("translate") == null) {
                        if (query.startsWith(YANAGISHIMA_COMMENT)) {
                            elasticsearchQueryResult = elasticsearchService.doQuery(datasource, query, userName, false, limit);
                        } else {
                            elasticsearchQueryResult = elasticsearchService.doQuery(datasource, query, userName, true, limit);
                        }
                    } else {
                        elasticsearchQueryResult = elasticsearchService.doTranslate(datasource, query, userName, true, limit);
                    }

                    String queryid = elasticsearchQueryResult.getQueryId();
                    retVal.put("queryid", queryid);
                    retVal.put("headers", elasticsearchQueryResult.getColumns());
                    retVal.put("results", elasticsearchQueryResult.getRecords());
                    retVal.put("lineNumber", Integer.toString(elasticsearchQueryResult.getLineNumber()));
                    retVal.put("rawDataSize", elasticsearchQueryResult.getRawDataSize().toString());
                    Optional<String> warningMessageOptinal = Optional.ofNullable(elasticsearchQueryResult.getWarningMessage());
                    warningMessageOptinal.ifPresent(warningMessage -> {
                        retVal.put("warn", warningMessage);
                    });
                    Optional<Query> queryDataOptional = db.single(Query.class).where("query_id=? and datasource=? and engine=?", queryid, datasource, "elasticsearch").execute();
                    queryDataOptional.ifPresent(queryData -> {
                        LocalDateTime submitTimeLdt = LocalDateTime.parse(queryid.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
                        String fetchResultTimeString = queryData.getFetchResultTimeString();
                        ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
                        long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
                        retVal.put("elapsedTimeMillis", elapsedTimeMillis);
                    });
                } catch (ElasticsearchQueryErrorException e) {
                    LOGGER.error(e.getMessage(), e);
                    retVal.put("queryid", e.getQueryId());
                    retVal.put("error", e.getCause().getMessage());
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                    retVal.put("error", e.getMessage());
                }
            });
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
