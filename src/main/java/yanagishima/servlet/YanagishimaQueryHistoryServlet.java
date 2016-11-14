package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.row.Query;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Singleton
public class YanagishimaQueryHistoryServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(YanagishimaQueryHistoryServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
            queryOptional.ifPresent(queryStr -> {
                List<Query> queryList = db.search(Query.class).where("query_string LIKE '%' || ? || '%'", queryStr).orderBy("query_id DESC").limit(1000).execute();
                List<List<String>> rowDataList = new ArrayList<List<String>>();
                for (Query query : queryList) {
                    List<String> row = new ArrayList<>();
                    row.add(query.getQueryId());
                    row.add(query.getQueryString());
                    row.add(query.getFetchResultTimeString());
                    rowDataList.add(row);
                }
                retVal.put("headers", Arrays.asList("Id", "Query", "Time"));
                retVal.put("results", rowDataList);
            });


        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
