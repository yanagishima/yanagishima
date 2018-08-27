package yanagishima.servlet;

import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;
import yanagishima.util.Status;

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
public class QueryHistoryServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(QueryHistoryServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public QueryHistoryServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
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
            String[] queryids = Optional.ofNullable(request.getParameter("queryids")).get().split(",");
            String label = request.getParameter("label");

            String placeholder = Arrays.stream(queryids).map(r -> "?").collect(Collectors.joining(", "));
            List<Query> queryList;
            if(label == null || label.length() == 0) {
                queryList = db.searchBySQL(Query.class,
                        "SELECT a.engine, a.query_id, a.fetch_result_time_string, a.query_string, a.status, a.elapsed_time_millis, a.result_file_size, a.linenumber, b.label_name AS label_name " +
                                "FROM query a LEFT OUTER JOIN label b on a.datasource = b.datasource AND a.engine = b.engine AND a.query_id = b.query_id WHERE a.datasource=\'" + datasource + "\' and a.query_id IN (" + placeholder + ")",
                        Arrays.stream(queryids).collect(Collectors.toList()));
            } else {
                queryList = db.searchBySQL(Query.class,
                        "SELECT a.engine, a.query_id, a.fetch_result_time_string, a.query_string, a.status, a.elapsed_time_millis, a.result_file_size, a.linenumber, b.label_name AS label_name " +
                                "FROM query a LEFT OUTER JOIN label b on a.datasource = b.datasource AND a.engine = b.engine AND a.query_id = b.query_id WHERE b.label_name = \'" + label + "\' and a.datasource=\'" + datasource + "\' and a.query_id IN (" + placeholder + ")",
                        Arrays.stream(queryids).collect(Collectors.toList()));
            }

            List<List<Object>> queryHistoryList = new ArrayList<List<Object>>();
            for (Query query : queryList) {
                if(query.getStatus().equals(Status.FAILED.name())) {
                    continue;
                }
                List<Object> row = new ArrayList<>();
                row.add(query.getQueryId());
                row.add(query.getQueryString());
                row.add(query.getElapsedTimeMillis());
                if(query.getResultFileSize() == null) {
                    row.add(null);
                } else {
                    DataSize rawDataSize = new DataSize(query.getResultFileSize(), DataSize.Unit.BYTE);
                    row.add(rawDataSize.convertToMostSuccinctDataSize().toString());
                }
                row.add(query.getEngine());
                row.add(query.getFetchResultTimeString());
                row.add(query.getLinenumber());
                row.add(query.getExtraColumn("label_name"));
                queryHistoryList.add(row);
            }
            retVal.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "labelName"));
            retVal.put("results", queryHistoryList);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
