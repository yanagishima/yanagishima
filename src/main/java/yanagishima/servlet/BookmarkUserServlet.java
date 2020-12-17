package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Bookmark;
import yanagishima.model.dto.BookmarkDto;
import yanagishima.repository.TinyOrm;

@Slf4j
@RestController
public class BookmarkUserServlet {
  private final YanagishimaConfig config;
  private final TinyOrm db;

  @Inject
  public BookmarkUserServlet(Injector injector) {
    this.config = injector.getInstance(YanagishimaConfig.class);
    this.db = injector.getInstance(TinyOrm.class);
  }

  @GetMapping("bookmarkUser")
  protected BookmarkDto get(@RequestParam String datasource, @RequestParam String engine,
                            @RequestParam(name = "bookmarkAll", defaultValue = "false") boolean showAll,
                            HttpServletRequest request, HttpServletResponse response) {
    BookmarkDto bookmarkDto = new BookmarkDto();
    if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
      sendForbiddenError(response);
      return bookmarkDto;
    }

    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      List<Bookmark> bookmarks;
      if (showAll) {
        bookmarks = db.searchBookmarks("engine = ? AND user = ?", engine, userName);
      } else {
        bookmarks = db.searchBookmarks("datasource = ? AND engine = ? AND user = ?",
                                       datasource, engine, userName);
      }
      bookmarkDto.setBookmarks(bookmarks);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkDto.setError(e.getMessage());
    }
    return bookmarkDto;
  }
}
