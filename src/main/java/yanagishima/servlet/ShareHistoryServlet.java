package yanagishima.servlet;

import static yanagishima.util.HistoryUtil.createHistoryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Comment;
import yanagishima.model.db.Query;
import yanagishima.model.db.SessionProperty;
import yanagishima.repository.TinyOrm;
import yanagishima.service.CommentService;
import yanagishima.service.PublishService;
import yanagishima.service.SessionPropertyService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShareHistoryServlet {
    private final PublishService publishService;
    private final SessionPropertyService sessionPropertyService;
    private final CommentService commentService;
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @GetMapping("share/shareHistory")
    public Map<String, Object> get(@RequestParam(name = "publish_id") String publishId) {
        Map<String, Object> body = new HashMap<>();
        try {
            publishService.get(publishId).ifPresent(publish -> {
                String datasource = publish.getDatasource();
                body.put("datasource", datasource);
                String queryId = publish.getQueryId();
                body.put("queryid", queryId);
                Query query = db.singleQuery("query_id = ? AND datasource = ?", queryId, datasource).get();
                body.put("engine", query.getEngine());
                List<SessionProperty> sessionPropertyList = sessionPropertyService.getAll(datasource, query.getEngine(), queryId);
                createHistoryResult(body, config.getSelectLimit(), datasource, query, true, sessionPropertyList);
                Optional<Comment> comment = commentService.get(datasource, query.getEngine(), queryId);
                body.put("comment", comment.orElse(null));
            });
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            body.put("error", e.getMessage());
        }
        return body;
    }
}
