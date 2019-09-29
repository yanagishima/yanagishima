package yanagishima.servlet;

import com.google.common.base.Splitter;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Bookmark;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class BookmarkServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkServlet.class);
    private static final long serialVersionUID = 1L;
    private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public BookmarkServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            String query = getRequiredParameter(request, "query");
            String title = request.getParameter("title");
            String engine = getRequiredParameter(request, "engine");

            db.insert(Bookmark.class).value("datasource", datasource).value("query", query).value("title", title).value("engine", engine).value("user", userName).execute();
            List<Bookmark> bookmarks;
            switch (config.getDatabaseType()) {
                case MYSQL:
                    bookmarks = db.searchBySQL(Bookmark.class, "select bookmark_id, datasource, engine, query, title from bookmark where bookmark_id = last_insert_id()");
                    break;
                case SQLITE:
                    bookmarks = db.searchBySQL(Bookmark.class, "select bookmark_id, datasource, engine, query, title from bookmark where rowid = last_insert_rowid()");
                    break;
                default:
                    throw new IllegalArgumentException("Illegal database type: " + config.getDatabaseType());
            }
            checkState(bookmarks.size() == 1, "Too many bookmarks: " + bookmarks.size());
            writeJSON(response, Map.of("bookmark_id", bookmarks.get(0).getBookmarkId()));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            List<String> bookmarkIds = SPLITTER.splitToList(getRequiredParameter(request, "bookmark_id"));
            if (bookmarkIds.isEmpty()) {
                writeJSON(response, Map.of("bookmarkList", List.of()));
                return;
            }

            String placeholder = bookmarkIds.stream().map(r -> "?").collect(Collectors.joining(", "));
            List<Object> bookmarkParameters = bookmarkIds.stream().map(Integer::parseInt).collect(Collectors.toList());
            List<Bookmark> bookmarks = db.searchBySQL(Bookmark.class, "SELECT bookmark_id, datasource, engine, query, title FROM bookmark WHERE datasource=\'" + datasource + "\' AND bookmark_id IN (" + placeholder + ")", bookmarkParameters);
            writeJSON(response, Map.of("bookmarkList", bookmarks));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String bookmarkId = getRequiredParameter(request, "bookmark_id");
            db.single(Bookmark.class).where("bookmark_id = ?", bookmarkId).execute().ifPresent(Bookmark::delete);

            String engine = getRequiredParameter(request, "engine");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<Bookmark> bookmarks = db.search(Bookmark.class).where("datasource = ? AND engine = ? AND user = ?", datasource, engine, userName).execute();
            writeJSON(response, Map.of("bookmarkList", bookmarks));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("bookmarkList", Map.of("error", e.getMessage())));
        }
    }
}
