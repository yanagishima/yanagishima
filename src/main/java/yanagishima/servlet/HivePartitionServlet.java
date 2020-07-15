package yanagishima.servlet;

import static java.lang.String.format;
import static java.lang.String.join;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yanagishima.config.YanagishimaConfig;
import yanagishima.result.HiveQueryResult;
import yanagishima.service.HiveService;

@Singleton
public class HivePartitionServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(HivePartitionServlet.class);
    private static final long serialVersionUID = 1L;

    private final HiveService hiveService;
    private final YanagishimaConfig config;

    @Inject
    public HivePartitionServlet(HiveService hiveService, YanagishimaConfig config) {
        this.hiveService = hiveService;
        this.config = config;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();

        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String user = getUsername(request);
            Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
            Optional<String> hivePassword = Optional.ofNullable(request.getParameter("password"));
            String engine = getRequiredParameter(request, "engine");
            String schema = getRequiredParameter(request, "schema");
            String table = getRequiredParameter(request, "table");
            String partitionColumn = request.getParameter("partitionColumn");
            String partitionColumnType = request.getParameter("partitionColumnType");
            String partitionValue = request.getParameter("partitionValue");
            if (partitionColumn == null || partitionValue == null) {
                String query = format("SHOW PARTITIONS %s.`%s`", schema, table);
                if (user != null) {
                    LOG.info(format("%s executed %s in %s", user, query, datasource));
                }
                HiveQueryResult hiveQueryResult = hiveService.doQuery(engine, datasource, query, user, hiveUser, hivePassword, false, Integer.MAX_VALUE);
                Set<String> partitions = new TreeSet<>();
                List<List<String>> records = hiveQueryResult.getRecords();
                if (records.size() > 0) {
                    String cell = records.get(0).get(0); // part1=val1/part2=val2/part3=val3'...
                    responseBody.put("column", cell.split("/")[0].split("=")[0]);
                    for (List<String> row : records) {
                        partitions.add(row.get(0).split("/")[0].split("=")[1]);
                    }
                }
                responseBody.put("partitions", partitions);
            } else {
                String[] partitionColumns = partitionColumn.split(",");
                String[] partitionColumnTypes = partitionColumnType.split(",");
                String[] partitionValues = partitionValue.split(",");
                if (partitionColumns.length != partitionValues.length) {
                    throw new RuntimeException("The number of partitionColumn must be same as partitionValue");
                }
                List<String> whereList = new ArrayList<>();
                for (int i = 0; i < partitionColumns.length; i++) {
                    if (partitionColumnTypes[i].equals("string")) {
                        whereList.add(format("%s = '%s'", partitionColumns[i], partitionValues[i]));
                    } else {
                        whereList.add(format("%s = %s", partitionColumns[i], partitionValues[i]));
                    }
                }
                String query = format("SHOW PARTITIONS %s.`%s` PARTITION(%s)", schema, table, join(", ", whereList));
                if (user != null) {
                    LOG.info(format("%s executed %s in %s", user, query, datasource));
                }
                HiveQueryResult hiveQueryResult = hiveService.doQuery(engine, datasource, query, user, hiveUser, hivePassword, false, Integer.MAX_VALUE);
                List<List<String>> records = hiveQueryResult.getRecords();
                String cell = records.get(0).get(0); // part1=val1/part2=val2/part3=val3'...
                String[] keyValues = cell.split("/");
                int index = 0;
                for (String keyValue : keyValues) {
                    if (keyValue.split("=")[0].equals(partitionColumns[partitionColumns.length - 1])) {
                        break;
                    }
                    index++;
                }
                responseBody.put("column", keyValues[index + 1].split("=")[0]);
                Set<String> partitions = new TreeSet<>();
                for (List<String> row : records) {
                    partitions.add(row.get(0).split("/")[index + 1].split("=")[1]);
                }
                responseBody.put("partitions", partitions);
            }
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    private String getUsername(HttpServletRequest request) {
        if (config.isUseAuditHttpHeaderName()) {
            return request.getHeader(config.getAuditHttpHeaderName());
        }
        String user = request.getParameter("user");
        String password = request.getParameter("password");
        if (user != null && password != null) {
            return user;
        }
        return null;
    }
}
