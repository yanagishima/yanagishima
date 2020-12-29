package yanagishima.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Bookmark;
import yanagishima.model.dto.BookmarkCreateDto;
import yanagishima.model.dto.BookmarkDto;
import yanagishima.service.BookmarkService;

@Slf4j
@Api(tags = "bookmark")
@RestController
@RequiredArgsConstructor
public class BookmarkController {
  private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

  private final YanagishimaConfig config;
  private final BookmarkService bookmarkService;

  @DatasourceAuth
  @PostMapping("bookmark")
  public BookmarkCreateDto post(@RequestParam String datasource,
                                @RequestParam String query,
                                @RequestParam String engine,
                                @RequestParam(required = false) String title,
                                @RequestParam(required = false) String snippet,
                                HttpServletRequest request) {
    BookmarkCreateDto bookmarkCreateDto = new BookmarkCreateDto();
    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      Bookmark bookmark = bookmarkService.insert(datasource, query, title, engine, userName, snippet);
      bookmarkCreateDto.setBookmarkId(bookmark.getBookmarkId());
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkCreateDto.setError(e.getMessage());
    }
    return bookmarkCreateDto;
  }

  @DatasourceAuth
  @GetMapping("bookmark")
  public BookmarkDto get(@RequestParam String datasource, @RequestParam(name = "bookmark_id") String bookmarkId) {
    BookmarkDto bookmarkDto = new BookmarkDto();
    try {
      List<String> bookmarkIds = SPLITTER.splitToList(bookmarkId);
      if (bookmarkIds.isEmpty()) {
        bookmarkDto.setBookmarks(List.of());
        return bookmarkDto;
      }

      List<Integer> params = bookmarkIds.stream().map(Integer::parseInt).collect(Collectors.toList());
      List<Bookmark> bookmarks = bookmarkService.getAll(datasource, params);
      bookmarkDto.setBookmarks(bookmarks);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkDto.setError(e.getMessage());
    }
    return bookmarkDto;
  }

  @DatasourceAuth
  @DeleteMapping("bookmark")
  public BookmarkDto delete(@RequestParam String datasource,
                            @RequestParam String engine,
                            @RequestParam(name = "bookmark_id") int bookmarkId,
                            HttpServletRequest request) {
    BookmarkDto bookmarkDto = new BookmarkDto();
    try {
      bookmarkService.delete(bookmarkId);

      String user = request.getHeader(config.getAuditHttpHeaderName());
      List<Bookmark> bookmarks = bookmarkService.getAll(false, datasource, engine, user);
      bookmarkDto.setBookmarks(bookmarks);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkDto.setError(e.getMessage());
    }
    return bookmarkDto;
  }
}
