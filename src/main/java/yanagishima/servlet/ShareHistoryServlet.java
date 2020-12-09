package yanagishima.servlet;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Comment;
import yanagishima.model.db.Query;
import yanagishima.model.db.SessionProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static yanagishima.util.HistoryUtil.createHistoryResult;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Slf4j
@Singleton
public class ShareHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyOrm db;

    @Inject
    public ShareHistoryServlet(YanagishimaConfig config, TinyOrm db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> body = new HashMap<>();
        try {
            String publishId = getRequiredParameter(request, "publish_id");
            db.singlePublish("publish_id = ?", publishId).ifPresent(publish -> {
                String datasource = publish.getDatasource();
                body.put("datasource", datasource);
                String queryId = publish.getQueryId();
                body.put("queryid", queryId);
                Query query = db.singleQuery("query_id = ? AND datasource = ?", queryId, datasource).get();
                body.put("engine", query.getEngine());
                List<SessionProperty> sessionPropertyList = db.searchSessionProperties("datasource = ? AND engine = ? AND query_id = ?", datasource, query.getEngine(), queryId);
                createHistoryResult(body, config.getSelectLimit(), datasource, query, true, sessionPropertyList);
                Optional<Comment> comment = db.singleComment("datasource = ? AND engine = ? AND query_id = ?", datasource, query.getEngine(), queryId);
                body.put("comment", comment.orElse(null));
            });
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            body.put("error", e.getMessage());
        }
        writeJSON(response, body);
    }
}
