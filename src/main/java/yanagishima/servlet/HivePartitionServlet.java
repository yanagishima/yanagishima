package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.result.HiveQueryResult;
import yanagishima.service.HiveService;
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

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class HivePartitionServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(HivePartitionServlet.class);

    private static final long serialVersionUID = 1L;

    private HiveService hiveService;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public HivePartitionServlet(HiveService hiveService, YanagishimaConfig yanagishimaConfig) {
        this.hiveService = hiveService;
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
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

            String userName = null;
            Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
            Optional<String> hivePassword = Optional.ofNullable(request.getParameter("password"));
            if(yanagishimaConfig.isUseAuditHttpHeaderName()) {
                userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            } else {
                if (hiveUser.isPresent() && hivePassword.isPresent()) {
                    userName = hiveUser.get();
                }
            }
            String engine = HttpRequestUtil.getParam(request, "engine");
            String schema = HttpRequestUtil.getParam(request, "schema");
            String table = HttpRequestUtil.getParam(request, "table");
            String partitionColumn = request.getParameter("partitionColumn");
            String partitionColumnType = request.getParameter("partitionColumnType");
            String partitionValue = request.getParameter("partitionValue");
            if (partitionColumn == null || partitionValue == null) {
                String query = String.format("SHOW PARTITIONS %s.`%s`", schema, table);
                HiveQueryResult hiveQueryResult = hiveService.doQuery(engine, datasource, query, userName, hiveUser, hivePassword, false, Integer.MAX_VALUE);
                Set<String> partitions = new TreeSet<>();
                List<List<String>> records = hiveQueryResult.getRecords();
                String cell = records.get(0).get(0);// part1=val1/part2=val2/part3=val3'...
                retVal.put("column", cell.split("/")[0].split("=")[0]);
                for (List<String> row : records) {
                    partitions.add(row.get(0).split("/")[0].split("=")[1]);
                }
                retVal.put("partitions", partitions);
            } else {
                String[] partitionColumnArray = partitionColumn.split(",");
                String[] partitionColumnTypeArray = partitionColumnType.split(",");
                String[] partitionValuesArray = partitionValue.split(",");
                if(partitionColumnArray.length != partitionValuesArray.length) {
                    throw new RuntimeException("The number of partitionColumn must be same as partitionValue");
                }
                List whereList = new ArrayList<>();
                for(int i=0; i<partitionColumnArray.length; i++) {
                    if(partitionColumnTypeArray[i].equals("string")) {
                        whereList.add(String.format("%s = '%s'", partitionColumnArray[i], partitionValuesArray[i]));
                    } else {
                        whereList.add(String.format("%s = %s", partitionColumnArray[i], partitionValuesArray[i]));
                    }
                }
                String query = String.format("SHOW PARTITIONS %s.`%s` PARTITION(%s)", schema, table, String.join(", ", whereList));
                HiveQueryResult hiveQueryResult = hiveService.doQuery(engine, datasource, query, userName, hiveUser, hivePassword, false, Integer.MAX_VALUE);
                List<List<String>> records = hiveQueryResult.getRecords();
                String cell = records.get(0).get(0);// part1=val1/part2=val2/part3=val3'...
                String[] keyValues = cell.split("/");
                int index = 0;
                for(String keyValue : keyValues) {
                    if(keyValue.split("=")[0].equals(partitionColumnArray[partitionColumnArray.length-1])) {
                        break;
                    }
                    index++;
                }
                retVal.put("column", keyValues[index+1].split("=")[0]);
                Set<String> partitions = new TreeSet<>();
                for (List<String> row : records) {
                    partitions.add(row.get(0).split("/")[index+1].split("=")[1]);
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
