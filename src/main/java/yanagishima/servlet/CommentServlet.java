package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Comment;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class CommentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServlet.class);

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public CommentServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String user = request.getHeader(config.getAuditHttpHeaderName());
            String engine = getRequiredParameter(request, "engine");
            String queryId = getRequiredParameter(request, "queryid");

            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
            responseBody.put("user", user);
            String updateTimeString = ZonedDateTime.now().toString();
            responseBody.put("updateTimeString", updateTimeString);

            String like = request.getParameter("like");
            if (like == null) {
                String content = getRequiredParameter(request, "content");
                Optional<Comment> comment = db.single(Comment.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId).execute();
                if (comment.isPresent()) {
                    String updateSql = format("UPDATE comment SET user = '%s', content = '%s', update_time_string = '%s' "
                                              + "WHERE datasource = '%s' and engine = '%s' and query_id = '%s'",
                                              user, content, updateTimeString, datasource, engine, queryId);
                    db.updateBySQL(updateSql);
                    responseBody.put("content", content);
                    responseBody.put("likeCount", comment.get().getLikeCount());
                } else {
                    db.insert(Comment.class).value("datasource", datasource)
                      .value("engine", engine)
                      .value("query_id", queryId)
                      .value("user", user)
                      .value("content", content)
                      .value("like_count", 0)
                      .value("update_time_string", updateTimeString)
                      .execute();
                    responseBody.put("content", content);
                    responseBody.put("likeCount", 0);
                }
            } else {
                Comment likedComment = db.single(Comment.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId).execute().get();
                int likeCount = 0;
                if (like.length() == 0) {
                    likeCount = likedComment.getLikeCount() + 1;
                } else {
                    likeCount = likedComment.getLikeCount() + Integer.parseInt(like);
                }
                String updateSql = format("UPDATE comment SET like_count=%d WHERE datasource = '%s' and engine = '%s' and query_id = '%s'", likeCount, datasource, engine, queryId);
                db.updateBySQL(updateSql);
                responseBody.put("likeCount", likeCount);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
            }

            String engine = getRequiredParameter(request, "engine");
            String queryId = request.getParameter("queryid");
            List<Comment> comments = new ArrayList<>();
            if (queryId != null) {
                Optional<Comment> comment = db.single(Comment.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId).execute();
                comment.ifPresent(comments::add);
            } else {
                String search = request.getParameter("search");
                String sort = Optional.ofNullable(request.getParameter("sort")).orElse("update_time_string");
                comments = db.search(Comment.class)
                             .where("datasource = ? and engine = ? and content LIKE '%" + nullToEmpty(search) + "%'", datasource, engine)
                             .orderBy(sort + " DESC")
                             .execute();
            }
            responseBody.put("comments", comments);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
            }

            String engine = getRequiredParameter(request, "engine");
            String queryId = getRequiredParameter(request, "queryid");
            Comment comment = db.single(Comment.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId).execute().get();
            comment.delete();
            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }
}
