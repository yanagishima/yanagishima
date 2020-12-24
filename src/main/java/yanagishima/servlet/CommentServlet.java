package yanagishima.servlet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Comment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentServlet {
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @PostMapping("comment")
    public Map<String, Object> post(@RequestParam String datasource,
                                    @RequestParam String engine,
                                    @RequestParam(name = "queryid") String queryId,
                                    @RequestParam Optional<Integer> like,
                                    @RequestParam(required = false) String content,
                                    HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }

            String user = request.getHeader(config.getAuditHttpHeaderName());

            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
            responseBody.put("user", user);
            String updateTimeString = ZonedDateTime.now().toString();
            responseBody.put("updateTimeString", updateTimeString);

            if (like.isEmpty()) {
                requireNonNull(content, "content is null");
                Optional<Comment> comment = db.singleComment("datasource = ? and engine = ? and query_id = ?",
                                                                           datasource, engine, queryId);
                if (comment.isPresent()) {
                    String updateSql = format("UPDATE comment SET user = '%s', content = '%s', update_time_string = '%s' "
                                              + "WHERE datasource = '%s' and engine = '%s' and query_id = '%s'",
                                              user, content, updateTimeString, datasource, engine, queryId);
                    db.updateBySQL(updateSql);
                    responseBody.put("content", content);
                    responseBody.put("likeCount", comment.get().getLikeCount());
                } else {
                    db.insert(Comment.class, value("datasource", datasource),
                       value("engine", engine),
                       value("query_id", queryId),
                       value("user", user),
                       value("content", content),
                       value("like_count", 0),
                       value("update_time_string", updateTimeString));
                    responseBody.put("content", content);
                    responseBody.put("likeCount", 0);
                }
            } else {
                Comment likedComment = db.singleComment("datasource = ? and engine = ? and query_id = ?",
                                                                      datasource, engine, queryId).get();
                int likeCount = likedComment.getLikeCount() + like.get();
                String updateSql = format("UPDATE comment SET like_count=%d WHERE datasource = '%s' and engine = '%s' and query_id = '%s'",
                                          likeCount, datasource, engine, queryId);
                db.updateBySQL(updateSql);
                responseBody.put("likeCount", likeCount);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }

    @GetMapping("comment")
    public Map<String, Object> get(@RequestParam String datasource,
                                   @RequestParam String engine,
                                   @RequestParam(name = "queryid", required = false) String queryId,
                                   @RequestParam(defaultValue = "update_time_string") String sort,
                                   @RequestParam(defaultValue = "") String search,
                                   HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
            }

            List<Comment> comments = new ArrayList<>();
            if (queryId != null) {
                Optional<Comment> comment = db.singleComment("datasource = ? and engine = ? and query_id = ?",
                                                             datasource, engine, queryId);
                comment.ifPresent(comments::add);
            } else {
                comments = db.searchComments(sort + " DESC",
                                             "datasource = ? and engine = ? and content LIKE '%" + search + "%'", datasource, engine);
            }
            responseBody.put("comments", comments);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }

    @DeleteMapping("comment")
    public Map<String, Object> delete(@RequestParam String datasource,
                                      @RequestParam String engine,
                                      @RequestParam(name = "queryid") String queryId,
                                      HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
            }

            db.deleteComment("datasource = ? and engine = ? and query_id = ?",
                             datasource, engine, queryId);
            responseBody.put("datasource", datasource);
            responseBody.put("engine", engine);
            responseBody.put("queryid", queryId);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }
}
