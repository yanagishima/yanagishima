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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class BookmarkUserServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(BookmarkUserServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public BookmarkUserServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
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

            String engine = HttpRequestUtil.getParam(request, "engine");
            String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            List<Bookmark> bookmarkList = db.search(Bookmark.class).where("datasource = ? and engine = ? and user = ?", datasource, engine, userName).execute();

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
