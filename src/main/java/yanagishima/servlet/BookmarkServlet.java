package yanagishima.servlet;

import com.google.common.base.Splitter;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Bookmark;
import yanagishima.model.HttpRequestContext;

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
import static java.util.Objects.requireNonNull;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
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
        HttpRequestContext context = new HttpRequestContext(request);
        try {
            requireNonNull(context.getDatasource(), "datasource is null");
            requireNonNull(context.getQuery(), "query is null");
            requireNonNull(context.getEngine(), "engine is null");

            if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
                sendForbiddenError(response);
                return;
            }
            String userName = request.getHeader(config.getAuditHttpHeaderName());

            db.insert(Bookmark.class).value("datasource", context.getDatasource())
              .value("query", context.getQuery())
              .value("title", context.getTitle())
              .value("engine", context.getEngine())
              .value("user", userName)
              .value("snippet", context.getSnippet()).execute();
            List<Bookmark> bookmarks;
            switch (config.getDatabaseType()) {
                case MYSQL:
                    bookmarks = db.searchBySQL(Bookmark.class, "select bookmark_id, datasource, engine, query, title, user, snippet from bookmark where bookmark_id = last_insert_id()");
                    break;
                case SQLITE:
                    bookmarks = db.searchBySQL(Bookmark.class, "select bookmark_id, datasource, engine, query, title, user, snippet from bookmark where rowid = last_insert_rowid()");
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
        HttpRequestContext context = new HttpRequestContext(request);
        try {
            requireNonNull(context.getDatasource(), "datasource is null");
            if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
                sendForbiddenError(response);
                return;
            }
            List<String> bookmarkIds = SPLITTER.splitToList(requireNonNull(context.getBookmarkId(), "bookmark_id is null"));
            if (bookmarkIds.isEmpty()) {
                writeJSON(response, Map.of("bookmarkList", List.of()));
                return;
            }

            String placeholder = join(", ", nCopies(bookmarkIds.size(), "?"));
            List<Object> bookmarkParameters = bookmarkIds.stream().map(Integer::parseInt).collect(Collectors.toList());
            List<Bookmark> bookmarks = db.searchBySQL(Bookmark.class, "SELECT bookmark_id, datasource, engine, query, title, snippet "
                                                                      + "FROM bookmark "
                                                                      + "WHERE datasource=\'" + context.getDatasource() + "\' AND bookmark_id IN (" + placeholder + ")", bookmarkParameters);
            writeJSON(response, Map.of("bookmarkList", bookmarks));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        HttpRequestContext context = new HttpRequestContext(request);
        try {
            requireNonNull(context.getDatasource(), "datasource is null");
            requireNonNull(context.getBookmarkId(), "bookmark_id is null");

            if (config.isCheckDatasource() && !validateDatasource(request, context.getDatasource())) {
                sendForbiddenError(response);
                return;
            }

            db.single(Bookmark.class).where("bookmark_id = ?", context.getBookmarkId()).execute().ifPresent(Bookmark::delete);

            requireNonNull(context.getEngine(), "engine is null");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            List<Bookmark> bookmarks = db.search(Bookmark.class).where("datasource = ? AND engine = ? AND user = ?", context.getDatasource(), context.getEngine(), userName).execute();
            writeJSON(response, Map.of("bookmarkList", bookmarks));
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            writeJSON(response, Map.of("bookmarkList", Map.of("error", e.getMessage())));
        }
    }
}
