package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.row.Query;
import yanagishima.util.Status;

@Singleton
public class ElasticsearchQueryStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyOrm db;

    @Inject
    public ElasticsearchQueryStatusServlet(YanagishimaConfig config, TinyOrm db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String datasource = getRequiredParameter(request, "datasource");
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
        }

        String queryId = getRequiredParameter(request, "queryid");
        Optional<Query> query = db.singleQuery("query_id=? and datasource=? and engine=?", queryId, datasource, "elasticsearch");
        String status = getStatus(query);
        writeJSON(response, Map.of("state", status));
    }

    private static String getStatus(Optional<Query> query) {
        if (query.isEmpty()) {
            return "RUNNING";
        }
        String status = query.get().getStatus();
        if (status.equals(Status.SUCCEED.name())) {
            return "FINISHED";
        }
        if (status.equals(Status.FAILED.name())) {
            return "FAILED";
        }
        throw new IllegalArgumentException(format("unknown status=%s", status));
    }
}
