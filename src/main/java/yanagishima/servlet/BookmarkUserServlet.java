package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Bookmark;
import yanagishima.model.HttpRequestContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class BookmarkUserServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkUserServlet.class);
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public BookmarkUserServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        HttpRequestContext context = new HttpRequestContext(request);
        try {
            requireNonNull(context.getDatasource(), "datasource is null");

            if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
                sendForbiddenError(response);
                return;
            }
            requireNonNull(context.getEngine(), "engine is null");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<Bookmark> bookmarks;
            if (context.isShowBookmarkAll()) {
                bookmarks = db.search(Bookmark.class).where("engine = ? AND user = ?", context.getEngine(), userName).execute();
            } else {
                bookmarks = db.search(Bookmark.class).where("datasource = ? AND engine = ? AND user = ?", context.getDatasource(), context.getEngine(), userName).execute();
            }
            writeJSON(response, Map.of("bookmarkList", bookmarks));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }
}
