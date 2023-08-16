package yanagishima.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yanagishima.model.User;
import yanagishima.model.db.Bookmark;
import yanagishima.repository.BookmarkRepository;

@Service
@RequiredArgsConstructor
public class BookmarkService {
  private final BookmarkRepository bookmarkRepository;

  public List<Bookmark> getAll(boolean showAll, String datasource, String engine, User user) {
    if (showAll) {
      return bookmarkRepository.findAllByEngineAndUserid(engine, user.getId());
    }
    return bookmarkRepository.findAllByDatasourceAndEngineAndUserid(datasource, engine, user.getId());
  }

  public List<Bookmark> getAll(String datasource, List<Integer> bookmarkIds) {
    return bookmarkRepository.findAllByDatasourceAndBookmarkIdIn(datasource, bookmarkIds);
  }

  public Bookmark insert(String datasource, String query, String title, String engine, User user,
                         String snippet) {
    Bookmark bookmark = new Bookmark();
    bookmark.setDatasource(datasource);
    bookmark.setQuery(query);
    bookmark.setTitle(title);
    bookmark.setEngine(engine);
    bookmark.setUserid(user.getId());
    bookmark.setSnippet(snippet);

    return bookmarkRepository.save(bookmark);
  }

  @Transactional
  public void delete(int bookmarkId) {
    bookmarkRepository.deleteByBookmarkId(bookmarkId);
  }
}
