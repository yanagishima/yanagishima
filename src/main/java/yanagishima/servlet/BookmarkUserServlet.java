package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Bookmark;
import yanagishima.model.dto.BookmarkDto;
import yanagishima.service.BookmarkService;

@Slf4j
@Api(tags = "bookmark")
@RestController
@RequiredArgsConstructor
public class BookmarkUserServlet {
  private final YanagishimaConfig config;
  private final BookmarkService bookmarkService;

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
      String user = request.getHeader(config.getAuditHttpHeaderName());
      List<Bookmark> bookmarks = bookmarkService.getAll(showAll, datasource, engine, user);
      bookmarkDto.setBookmarks(bookmarks);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      bookmarkDto.setError(e.getMessage());
    }
    return bookmarkDto;
  }
}
