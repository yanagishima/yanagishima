package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Bookmark;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class BookmarkServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(BookmarkServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public BookmarkServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            String datasource = HttpRequestUtil.getParam(request, "datasource");
            if(yanagishimaConfig.isCheckDatasource()) {
                if(!AccessControlUtil.validateDatasource(request, datasource)) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String query = HttpRequestUtil.getParam(request, "query");
            String title = request.getParameter("title");
            String engine = HttpRequestUtil.getParam(request, "engine");
            db.insert(Bookmark.class).value("datasource", datasource).value("query", query).value("title", title).value("engine", engine).execute();
            List<Bookmark> bookmarkList = db.searchBySQL(Bookmark.class, "select bookmark_id, datasource, engine, query, title from bookmark where rowid = last_insert_rowid()");
            if(bookmarkList.size() == 1) {
                retVal.put("bookmark_id", bookmarkList.get(0).getBookmarkId());
            } else {
                retVal.put("error", "too many bookmarks = " + bookmarkList);
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            String datasource = HttpRequestUtil.getParam(request, "datasource");
            if(yanagishimaConfig.isCheckDatasource()) {
                if(!AccessControlUtil.validateDatasource(request, datasource)) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            String[] bookmarkIds = HttpRequestUtil.getParam(request, "bookmark_id").split(",");

            String placeholder = Arrays.stream(bookmarkIds).map(r -> "?").collect(Collectors.joining(", "));
            List<Bookmark> bookmarkList = db.searchBySQL(Bookmark.class,
                    "SELECT bookmark_id, datasource, engine, query, title FROM bookmark WHERE datasource=\'" + datasource + "\' and bookmark_id IN (" + placeholder + ")",
                    Arrays.stream(bookmarkIds).map(s -> Integer.parseInt(s)).collect(Collectors.toList()));

            List<Map> resultMapList = new ArrayList<>();
            for(Bookmark bookmark : bookmarkList) {
                Map<String, Object> m = new HashMap<>();
                m.put("bookmark_id", bookmark.getBookmarkId());
                m.put("datasource", bookmark.getDatasource());
                m.put("engine", bookmark.getEngine());
                m.put("query", bookmark.getQuery());
                m.put("title", bookmark.getTitle());
                resultMapList.add(m);
            }
            retVal.put("bookmarkList", resultMapList);
            
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
