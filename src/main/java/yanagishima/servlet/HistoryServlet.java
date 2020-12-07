package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HistoryUtil.createHistoryResult;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.row.SessionProperty;

@Slf4j
@Singleton
public class HistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public HistoryServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        String queryId = request.getParameter("queryid");
        if (queryId == null) {
            writeJSON(response, responseBody);
            return;
        }

        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            String engine = request.getParameter("engine");
            Optional<Query> queryOptional;
            if (engine == null) {
                queryOptional = db.single(Query.class).where("query_id=? and datasource=?", queryId, datasource).execute();
            } else {
                queryOptional = db.single(Query.class).where("query_id=? and datasource=? and engine=?", queryId, datasource, engine).execute();
            }

            String user = request.getHeader(config.getAuditHttpHeaderName());
            Optional<Query> userQueryOptional = db.single(Query.class).where("query_id=? and datasource=? and user=?", queryId, datasource, user).execute();
            responseBody.put("editLabel", userQueryOptional.isPresent());

            queryOptional.ifPresent(query -> {
                responseBody.put("engine", query.getEngine());
                boolean resultVisible;
                if (config.isAllowOtherReadResult(datasource)) {
                    resultVisible = true;
                } else {
                    resultVisible = userQueryOptional.isPresent();
                }
                List<SessionProperty> sessionPropertyList = db.search(SessionProperty.class).where("datasource = ? AND engine = ? AND query_id = ?", datasource, engine, queryId).execute();
                createHistoryResult(responseBody, config.getSelectLimit(), datasource, query, resultVisible, sessionPropertyList);
            });
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }
}
