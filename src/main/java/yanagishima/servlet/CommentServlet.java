package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Comment;
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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class CommentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommentServlet.class);
    private final YanagishimaConfig yanagishimaConfig;
    @Inject
    private TinyORM db;

    @Inject
    public CommentServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<>();

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

            String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            String engine = HttpRequestUtil.getParam(request, "engine");
            String queryId = HttpRequestUtil.getParam(request, "query_id");

            retVal.put("datasource", datasource);
            retVal.put("engine", engine);
            retVal.put("queryId", queryId);
            retVal.put("user", userName);
            String updateTimeString = ZonedDateTime.now().toString();
            retVal.put("updateTimeString", updateTimeString);

            String like = request.getParameter("like");
            if (like == null) {
                String comment = HttpRequestUtil.getParam(request, "comment");
                db.insert(Comment.class).value("datasource", datasource)
                        .value("engine", engine)
                        .value("query_id", queryId)
                        .value("user", userName)
                        .value("comment_text", comment)
                        .value("like_count", 0)
                        .value("update_time_string", updateTimeString)
                        .execute();
                retVal.put("comment", comment);
                retVal.put("like_count", 0);
            } else {
                Comment likedComment = db.single(Comment.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId).execute().get();
                int likeCount = likedComment.getLikeCount() + 1;
                String updateSql = String.format("UPDATE comment SET like_count=%d WHERE datasource = '%s' and engine = '%s' and query_id = '%s'", likeCount, datasource, engine, queryId);
                int updateCount = db.updateBySQL(updateSql);
                retVal.put("likeCount", likeCount);
                retVal.put("updateCount", updateCount);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<>();

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

            String engine = HttpRequestUtil.getParam(request, "engine");
            String search = request.getParameter("search");
            String sort = Optional.ofNullable(request.getParameter("sort")).orElse("update_time_string");

            List<Comment> comments = db.search(Comment.class).where("datasource = ? and engine = ? and comment_text LIKE '%" + Optional.ofNullable(search).orElse("") + "%'", datasource, engine).orderBy(sort + " DESC").execute();
            retVal.put("comments", comments);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }


    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<>();

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

            String engine = HttpRequestUtil.getParam(request, "engine");
            String queryId = HttpRequestUtil.getParam(request, "query_id");
            Comment deletedComment = db.single(Comment.class).where("datasource = ? and engine = ? and query_id = ?", datasource, engine, queryId).execute().get();
            deletedComment.delete();
            retVal.put("datasource", datasource);
            retVal.put("engine", engine);
            retVal.put("query_id", queryId);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
