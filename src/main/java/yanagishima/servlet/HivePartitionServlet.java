package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.bean.HttpRequestContext;
import yanagishima.config.YanagishimaConfig;
import yanagishima.result.HiveQueryResult;
import yanagishima.service.HiveService;
import yanagishima.util.AccessControlUtil;

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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.requireNonNull;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class HivePartitionServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(HivePartitionServlet.class);
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
        HttpRequestContext context = new HttpRequestContext(request);
        HashMap<String, Object> retVal = new HashMap<>();

        try {
            requireNonNull(context.getDatasource(), "datasource is null");
            requireNonNull(context.getEngine(), "engine is null");
            requireNonNull(context.getSchema(), "schema is null");
            requireNonNull(context.getTable(), "table is null");

            if (config.isCheckDatasource()) {
                if (!AccessControlUtil.validateDatasource(request, context.getDatasource())) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String userName = null;
            Optional<String> hiveUser = Optional.ofNullable(context.getUser());
            Optional<String> hivePassword = Optional.ofNullable(context.getPassword());
            if(config.isUseAuditHttpHeaderName()) {
                userName = request.getHeader(config.getAuditHttpHeaderName());
            } else {
                if (hiveUser.isPresent() && hivePassword.isPresent()) {
                    userName = hiveUser.get();
                }
            }
            if (context.getPartitionColumns().isEmpty() || context.getPartitionValues().isEmpty()) {
                String query = String.format("SHOW PARTITIONS %s.`%s`", context.getSchema(), context.getTable());
                HiveQueryResult hiveQueryResult = hiveService.doQuery(context.getEngine(), context.getDatasource(), query, userName, hiveUser, hivePassword, false, Integer.MAX_VALUE);
                Set<String> partitions = new TreeSet<>();
                List<List<String>> records = hiveQueryResult.getRecords();
                String cell = records.get(0).get(0);// part1=val1/part2=val2/part3=val3'...
                retVal.put("column", cell.split("/")[0].split("=")[0]);
                for (List<String> row : records) {
                    partitions.add(row.get(0).split("/")[0].split("=")[1]);
                }
                retVal.put("partitions", partitions);
            } else {
                if(context.getPartitionColumns().size() != context.getPartitionValues().size()) {
                    throw new RuntimeException("The number of partitionColumn must be same as partitionValue");
                }
                List whereList = new ArrayList<>();
                for(int i = 0; i<context.getPartitionColumns().size(); i++) {
                    if(context.getPartitionColumnTypes().get(i).equals("string")) {
                        whereList.add(String.format("%s = '%s'", context.getPartitionColumns().get(i), context.getPartitionValues().get(i)));
                    } else {
                        whereList.add(String.format("%s = %s", context.getPartitionColumns().get(i), context.getPartitionValues().get(i)));
                    }
                }
                String query = String.format("SHOW PARTITIONS %s.`%s` PARTITION(%s)", context.getSchema(), context.getTable(), String.join(", ", whereList));
                HiveQueryResult hiveQueryResult = hiveService.doQuery(context.getEngine(), context.getDatasource(), query, userName, hiveUser, hivePassword, false, Integer.MAX_VALUE);
                List<List<String>> records = hiveQueryResult.getRecords();
                String cell = records.get(0).get(0);// part1=val1/part2=val2/part3=val3'...
                String[] keyValues = cell.split("/");
                int index = 0;
                for(String keyValue : keyValues) {
                    if(keyValue.split("=")[0].equals(context.getPartitionColumns().get(context.getPartitionColumns().size()-1))) {
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
        writeJSON(response, retVal);
    }
}
