package yanagishima.controller;

import static java.util.Objects.requireNonNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.model.User;
import yanagishima.model.db.Comment;
import yanagishima.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @DatasourceAuth
  @PostMapping("comment")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam String engine,
                                  @RequestParam(name = "queryid") String queryId,
                                  @RequestParam Optional<Integer> like,
                                  @RequestParam(required = false) String content,
                                  User user) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      responseBody.put("datasource", datasource);
      responseBody.put("engine", engine);
      responseBody.put("queryid", queryId);
      responseBody.put("user", user);
      String updateTimeString = ZonedDateTime.now().toString();
      responseBody.put("updateTimeString", updateTimeString);

      if (like.isEmpty()) {
        requireNonNull(content, "content is null");
        Optional<Comment> comment = commentService.get(datasource, engine, queryId);
        if (comment.isPresent()) {
          commentService.update(comment.get(), user, content, updateTimeString);
          responseBody.put("content", content);
          responseBody.put("likeCount", comment.get().getLikeCount());
        } else {
          commentService.insert(datasource, engine, queryId, user, content, updateTimeString);
          responseBody.put("content", content);
          responseBody.put("likeCount", 0);
        }
      } else {
        Comment likedComment = commentService.get(datasource, engine, queryId).get();
        int likeCount = likedComment.getLikeCount() + like.get();
        commentService.update(likedComment, likeCount);
        responseBody.put("likeCount", likeCount);
      }
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  @DatasourceAuth
  @GetMapping("comment")
  public Map<String, Object> get(@RequestParam String datasource,
                                 @RequestParam String engine,
                                 @RequestParam(name = "queryid", required = false) String queryId,
                                 @RequestParam(defaultValue = "updateTimeString") String sort,
                                 @RequestParam(defaultValue = "") String search) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      List<Comment> comments = new ArrayList<>();
      if (queryId != null) {
        Optional<Comment> comment = commentService.get(datasource, engine, queryId);
        comment.ifPresent(comments::add);
      } else {
        comments = commentService.getAll(datasource, engine, search, sort);
      }
      responseBody.put("comments", comments);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  @DatasourceAuth
  @DeleteMapping("comment")
  public Map<String, Object> delete(@RequestParam String datasource,
                                    @RequestParam String engine,
                                    @RequestParam(name = "queryid") String queryId) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      commentService.delete(datasource, engine, queryId);
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
