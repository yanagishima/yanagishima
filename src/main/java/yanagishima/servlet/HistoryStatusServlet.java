package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.util.HashMap;
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

@Slf4j
@Singleton
public class HistoryStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private YanagishimaConfig config;
    private TinyORM db;

    @Inject
    public HistoryStatusServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "ng");
        String queryId = request.getParameter("queryid");
        if (queryId == null) {
            writeJSON(response, responseBody);
            return;
        }

        try {
            String datasource = getRequiredParameter(request, "datasource");
            String engine = request.getParameter("engine");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            findQuery(datasource, engine, queryId).ifPresent(query -> responseBody.put("status", "ok"));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    private Optional<Query> findQuery(String datasource, String engine, String queryId) {
        if (engine == null) {
            return db.single(Query.class).where("query_id=? and datasource=?", queryId, datasource).execute();
        }
        return db.single(Query.class).where("query_id=? and datasource=? and engine=?", queryId, datasource, engine).execute();
    }
}
