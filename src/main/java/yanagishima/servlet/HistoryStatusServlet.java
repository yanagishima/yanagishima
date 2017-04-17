package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.HistoryUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Singleton
public class HistoryStatusServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(HistoryStatusServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();
        retVal.put("status", "ng");

        try {
            Optional<String> queryidOptional = Optional.ofNullable(request.getParameter("queryid"));
            if(queryidOptional.isPresent()) {
                String datasource = Optional.ofNullable(request.getParameter("datasource")).get();
                Optional<Query> queryOptional = db.single(Query.class).where("query_id=? and datasource=?", queryidOptional.get(), datasource).execute();
                queryOptional.ifPresent(query -> {
                    retVal.put("status", "ok");
                });
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }



}
