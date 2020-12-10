package yanagishima.servlet;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.row.Comment;
import yanagishima.model.HttpRequestContext;

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
import static java.util.Objects.requireNonNull;
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.JsonUtil.writeJSON;

@Slf4j
@Singleton
public class CommentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyOrm db;

    @Inject
    public CommentServlet(YanagishimaConfig config, TinyOrm db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpRequestContext context = new HttpRequestContext(request);
        Map<String, Object> responseBody = new HashMap<>();
        try {
            requireNonNull(context.getDatasource(), "datasource is null");
            requireNonNull(context.getEngine(), "engine is null");
            requireNonNull(context.getQueryId(), "queryid is null");

            if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
                sendForbiddenError(response);
                return;
            }

            String user = request.getHeader(config.getAuditHttpHeaderName());

            responseBody.put("datasource", context.getDatasource());
            responseBody.put("engine", context.getEngine());
            responseBody.put("queryid", context.getQueryId());
            responseBody.put("user", user);
            String updateTimeString = ZonedDateTime.now().toString();
            responseBody.put("updateTimeString", updateTimeString);

            if (context.getLike() == null) {
                requireNonNull(context.getContent(), "content is null");
                Optional<Comment> comment = db.singleComment("datasource = ? and engine = ? and query_id = ?",
                                                                           context.getDatasource(), context.getEngine(), context.getQueryId());
                if (comment.isPresent()) {
                    String updateSql = format("UPDATE comment SET user = '%s', content = '%s', update_time_string = '%s' "
                                              + "WHERE datasource = '%s' and engine = '%s' and query_id = '%s'",
                                              user, context.getContent(), updateTimeString, context.getDatasource(), context.getEngine(), context.getQueryId());
                    db.updateBySQL(updateSql);
                    responseBody.put("content", context.getContent());
                    responseBody.put("likeCount", comment.get().getLikeCount());
                } else {
                    db.insert(Comment.class, value("datasource", context.getDatasource()),
                       value("engine", context.getEngine()),
                       value("query_id", context.getQueryId()),
                       value("user", user),
                       value("content", context.getContent()),
                       value("like_count", 0),
                       value("update_time_string", updateTimeString));
                    responseBody.put("content", context.getContent());
                    responseBody.put("likeCount", 0);
                }
            } else {
                Comment likedComment = db.singleComment("datasource = ? and engine = ? and query_id = ?",
                                                                      context.getDatasource(), context.getEngine(), context.getQueryId()).get();
                int likeCount = 0;
                if (context.getLike().length() == 0) {
                    likeCount = likedComment.getLikeCount() + 1;
                } else {
                    likeCount = likedComment.getLikeCount() + Integer.parseInt(context.getLike());
                }
                String updateSql = format("UPDATE comment SET like_count=%d WHERE datasource = '%s' and engine = '%s' and query_id = '%s'",
                                          likeCount, context.getDatasource(), context.getEngine(), context.getQueryId());
                db.updateBySQL(updateSql);
                responseBody.put("likeCount", likeCount);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        HttpRequestContext context = new HttpRequestContext(request);
        Map<String, Object> responseBody = new HashMap<>();
        try {
            requireNonNull(context.getDatasource(), "datasource is null");
            requireNonNull(context.getEngine(), "engine is null");

            if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
                sendForbiddenError(response);
            }

            List<Comment> comments = new ArrayList<>();
            if (context.getQueryId() != null) {
                Optional<Comment> comment = db.singleComment("datasource = ? and engine = ? and query_id = ?",
                                                             context.getDatasource(), context.getEngine(), context.getQueryId());
                comment.ifPresent(comments::add);
            } else {
                String sort = context.getSort() != null ? context.getSort() : "update_time_string";
                comments = db.searchComments(sort + " DESC",
                                             "datasource = ? and engine = ? and content LIKE '%" + nullToEmpty(context.getSearch()) + "%'", context.getDatasource(), context.getEngine());
            }
            responseBody.put("comments", comments);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        HttpRequestContext context = new HttpRequestContext(request);
        Map<String, Object> responseBody = new HashMap<>();
        try {
            requireNonNull(context.getDatasource(), "datasource is null");
            requireNonNull(context.getEngine(), "engine is null");
            requireNonNull(context.getQueryId(), "queryid is null");

            if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
                sendForbiddenError(response);
            }

            db.deleteComment("datasource = ? and engine = ? and query_id = ?",
                             context.getDatasource(), context.getEngine(), context.getQueryId());
            responseBody.put("datasource", context.getDatasource());
            responseBody.put("engine", context.getEngine());
            responseBody.put("queryid", context.getQueryId());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }
}
