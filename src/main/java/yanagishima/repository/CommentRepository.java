package yanagishima.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.Comment;
import yanagishima.model.db.CommentId;

@Repository
public interface CommentRepository extends CrudRepository<Comment, CommentId> {
  List<Comment> findAllByDatasourceAndEngineAndContentContains(String datasource, String engine, String content,
                                                               Sort sort);

  Optional<Comment> findByDatasourceAndEngineAndQueryid(String datasource, String engine, String queryid);

  void deleteByDatasourceAndEngineAndQueryid(String datasource, String engine, String queryid);
}
