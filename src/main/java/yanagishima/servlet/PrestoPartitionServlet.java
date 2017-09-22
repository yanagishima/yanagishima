package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.result.PrestoQueryResult;
import yanagishima.service.PrestoService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

@Singleton
public class PrestoPartitionServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(PrestoPartitionServlet.class);

    private static final long serialVersionUID = 1L;

    private PrestoService prestoService;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public PrestoPartitionServlet(PrestoService prestoService, YanagishimaConfig yanagishimaConfig) {
        this.prestoService = prestoService;
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {

            String datasource = HttpRequestUtil.getParam(request, "datasource");
            if (yanagishimaConfig.isCheckDatasource()) {
                if (!AccessControlUtil.validateDatasource(request, datasource)) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            String catalog = HttpRequestUtil.getParam(request, "catalog");
            String schema = HttpRequestUtil.getParam(request, "schema");
            String table = HttpRequestUtil.getParam(request, "table");
            String partitionColumn = request.getParameter("partitionColumn");
            String partitionValue = request.getParameter("partitionValue");
            if (partitionColumn == null || partitionValue == null) {
                String query = String.format("%sSHOW PARTITIONS FROM %s.%s.%s", YANAGISHIMA_COMMENT, catalog, schema, table);
                PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, query, userName, false, Integer.MAX_VALUE);
                retVal.put("column", prestoQueryResult.getColumns().get(0));
                Set<String> partitions = new TreeSet<>();
                List<List<String>> records = prestoQueryResult.getRecords();
                for (List<String> row : records) {
                    partitions.add(row.get(0));
                }
                retVal.put("partitions", partitions);
            } else {
                String query = String.format("%sSHOW PARTITIONS FROM %s.%s.%s WHERE %s='%s'", YANAGISHIMA_COMMENT, catalog, schema, table, partitionColumn, partitionValue);
                PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, query, userName, false, Integer.MAX_VALUE);
                List<String> columns = prestoQueryResult.getColumns();
                int index = 0;
                for (String column : columns) {
                    if (column.equals(partitionColumn)) {
                        break;
                    }
                    index++;
                }
                retVal.put("column", columns.get(index + 1));
                Set<String> partitions = new TreeSet<>();
                List<List<String>> records = prestoQueryResult.getRecords();
                for (List<String> row : records) {
                    partitions.add(row.get(index + 1));
                }
                retVal.put("partitions", partitions);
            }


        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
