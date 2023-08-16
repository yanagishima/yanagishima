package yanagishima.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yanagishima.model.User;
import yanagishima.model.db.Comment;
import yanagishima.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository commentRepository;

  public List<Comment> getAll(String datasource, String engine, String content, String sort) {
    return commentRepository.findAllByDatasourceAndEngineAndContentContains(datasource, engine, content,
                                                                            Sort.by(sort).descending());
  }

  public Optional<Comment> get(String datasource, String engine, String queryId) {
    return commentRepository.findByDatasourceAndEngineAndQueryid(datasource, engine, queryId);
  }

  public Comment insert(String datasource, String engine, String queryId, User user, String content,
                        String updateTimeString) {
    Comment comment = new Comment();
    comment.setDatasource(datasource);
    comment.setEngine(engine);
    comment.setQueryid(queryId);
    comment.setUserid(user.getId());
    comment.setContent(content);
    comment.setLikeCount(0);
    comment.setUpdateTimeString(updateTimeString);
    return commentRepository.save(comment);
  }

  public void update(Comment comment, User user, String content, String updateTimeString) {
    comment.setUserid(user.getId());
    comment.setContent(content);
    comment.setUpdateTimeString(updateTimeString);
    commentRepository.save(comment);
  }

  public void update(Comment comment, int like) {
    comment.setLikeCount(like);
    commentRepository.save(comment);
  }

  @Transactional
  public void delete(String datasource, String engine, String queryId) {
    commentRepository.deleteByDatasourceAndEngineAndQueryid(datasource, engine, queryId);
  }
}
