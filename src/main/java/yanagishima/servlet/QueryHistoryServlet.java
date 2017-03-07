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
public class QueryHistoryServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(QueryHistoryServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            Optional<String> limitOptional = Optional.ofNullable(request.getParameter("limit"));
            limitOptional.ifPresent(limitStr -> {
                String datasource = Optional.ofNullable(request.getParameter("datasource")).get();
                List<Query> queryList = db.search(Query.class).where("datasource=?", datasource).orderBy("query_id DESC").limit(Integer.parseInt(limitStr)).execute();
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
