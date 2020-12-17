package yanagishima.servlet;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.HttpRequestContext;
import yanagishima.model.db.Bookmark;
import yanagishima.model.dto.BookmarkCreateDto;
import yanagishima.model.dto.BookmarkDto;
import yanagishima.repository.TinyOrm;

@Slf4j
@RestController
public class BookmarkServlet {
  private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

  private final YanagishimaConfig config;
  private final TinyOrm db;

  @Inject
  public BookmarkServlet(Injector injector) {
    this.config = injector.getInstance(YanagishimaConfig.class);
    this.db = injector.getInstance(TinyOrm.class);
  }

  @PostMapping("bookmark")
  public BookmarkCreateDto post(@RequestParam String datasource,
                                @RequestParam String query,
                                @RequestParam String engine,
                                @RequestParam(required = false) String title,
                                @RequestParam(required = false) String snippet,
                                HttpServletRequest request, HttpServletResponse response) {
    BookmarkCreateDto bookmarkCreateDto = new BookmarkCreateDto();
    if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
      sendForbiddenError(response);
      return bookmarkCreateDto;
    }
    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      db.insert(Bookmark.class, value("datasource", datasource),
                value("query", query),
                value("title", title),
                value("engine", engine),
                value("user", userName),
                value("snippet", snippet));
      List<Bookmark> bookmarks;
      switch (config.getDatabaseType()) {
        case MYSQL:
          bookmarks = db.searchBySQL(Bookmark.class,
                                     "select bookmark_id, datasource, engine, query, title, user, snippet from bookmark where bookmark_id = last_insert_id()");
          break;
        case SQLITE:
          bookmarks = db.searchBySQL(Bookmark.class,
                                     "select bookmark_id, datasource, engine, query, title, user, snippet from bookmark where rowid = last_insert_rowid()");
          break;
        default:
          throw new IllegalArgumentException("Illegal database type: " + config.getDatabaseType());
      }
      checkState(bookmarks.size() == 1, "Too many bookmarks: " + bookmarks.size());
      bookmarkCreateDto.setBookmarkId(bookmarks.get(0).getBookmarkId());
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkCreateDto.setError(e.getMessage());
    }
    return bookmarkCreateDto;
  }

  @GetMapping("bookmark")
  public BookmarkDto get(@RequestParam String datasource, @RequestParam(name = "bookmark_id") String bookmarkId,
                         HttpServletRequest request, HttpServletResponse response) {
    HttpRequestContext context = new HttpRequestContext(request);
    BookmarkDto bookmarkDto = new BookmarkDto();
    if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
      sendForbiddenError(response);
      return bookmarkDto;
    }
    try {
      List<String> bookmarkIds = SPLITTER.splitToList(bookmarkId);
      if (bookmarkIds.isEmpty()) {
        bookmarkDto.setBookmarks(List.of());
        return bookmarkDto;
      }

      String placeholder = join(", ", nCopies(bookmarkIds.size(), "?"));
      List<Object> bookmarkParameters = bookmarkIds.stream().map(Integer::parseInt).collect(
          Collectors.toList());
      List<Bookmark> bookmarks = db.searchBySQL(Bookmark.class,
                                                "SELECT bookmark_id, datasource, engine, query, title, snippet "
                                                + "FROM bookmark "
                                                + "WHERE datasource=\'" + context.getDatasource()
                                                + "\' AND bookmark_id IN (" + placeholder + ")",
                                                bookmarkParameters);
      bookmarkDto.setBookmarks(bookmarks);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkDto.setError(e.getMessage());
    }
    return bookmarkDto;
  }

  @DeleteMapping("bookmark")
  public BookmarkDto delete(@RequestParam String datasource,
                            @RequestParam String engine,
                            @RequestParam(name = "bookmark_id") String bookmarkId,
                            HttpServletRequest request, HttpServletResponse response) {
    BookmarkDto bookmarkDto = new BookmarkDto();
    if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
      sendForbiddenError(response);
      return bookmarkDto;
    }
    try {
      db.deleteBookmark("bookmark_id = ?", bookmarkId);

      String userName = request.getHeader(config.getAuditHttpHeaderName());
      List<Bookmark> bookmarks = db.searchBookmarks("datasource = ? AND engine = ? AND user = ?",
                                                    datasource, engine, userName);
      bookmarkDto.setBookmarks(bookmarks);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkDto.setError(e.getMessage());
    }
    return bookmarkDto;
  }
}
