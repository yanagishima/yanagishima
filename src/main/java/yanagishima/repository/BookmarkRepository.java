package yanagishima.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.Bookmark;

@Repository
public interface BookmarkRepository extends CrudRepository<Bookmark, Long> {
  List<Bookmark> findAllByEngineAndUserid(String engine, String userid);

  List<Bookmark> findAllByDatasourceAndEngineAndUserid(String datasource, String engine, String userid);

  List<Bookmark> findAllByDatasourceAndBookmarkIdIn(String datasource, List<Integer> bookmarkIds);

  void deleteByBookmarkId(int bookmarkId);
}
