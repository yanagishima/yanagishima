package yanagishima.servlet;

import static com.google.common.base.Preconditions.checkArgument;
import static io.prestosql.client.OkHttpUtil.basicAuth;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Objects.requireNonNull;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.presto.PrestoQueryResult;
import yanagishima.service.PrestoService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrestoPartitionServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PrestoService prestoService;
    private final YanagishimaConfig config;

    @PostMapping("prestoPartition")
    public Map<String, Object> post(@RequestParam String datasource,
                                    @RequestParam String catalog,
                                    @RequestParam String schema,
                                    @RequestParam String table,
                                    @RequestParam(required = false) String partitionColumn,
                                    @RequestParam(required = false) String partitionColumnType,
                                    @RequestParam(required = false) String partitionValue,
                                    @RequestParam(name = "user") Optional<String> prestoUser,
                                    @RequestParam(name = "password") Optional<String> prestoPassword,
                                    HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }

            String user = getUsername(request);
            Optional<String> webHdfsProxyUser = config.getWebhdfsProxyUser(datasource);
            Optional<String> webHdfsProxyPassword = config.getWebhdfsProxyPassword(datasource);
            if (partitionColumn == null || partitionValue == null) {
                Optional<String> webhdfsUrl = config.getWebhdfsUrl(datasource, catalog, schema, table);
                if (webhdfsUrl.isPresent()) {
                    if (webHdfsProxyUser.isPresent() && webHdfsProxyPassword.isPresent()) {
                        setFirstPartitionWithWebHdfs(responseBody, webHdfsProxyUser, webHdfsProxyPassword, webhdfsUrl.get());
                    } else {
                        setFirstPartitionWithWebHdfs(responseBody, prestoUser, prestoPassword, webhdfsUrl.get());
                    }
                } else {
                    String query = buildGetPartitionsQuery(datasource, catalog, schema, table);
                    if (user != null) {
                        log.info(format("%s executed %s in %s", user, query, datasource));
                    }
                    PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, query, user, prestoUser, prestoPassword, false, Integer.MAX_VALUE);
                    responseBody.put("column", prestoQueryResult.getColumns().get(0));
                    Set<String> partitions = new TreeSet<>();
                    List<List<String>> records = prestoQueryResult.getRecords();
                    for (List<String> row : records) {
                        String data = row.get(0);
                        if (data != null) {
                            partitions.add(data);
                        }
                    }
                    responseBody.put("partitions", partitions);
                }
            } else {
                String[] partitionColumnArray = partitionColumn.split(",");
                String[] partitionColumnTypeArray = partitionColumnType.split(",");
                String[] partitionValuesArray = partitionValue.split(",");
                if (partitionColumnArray.length != partitionValuesArray.length) {
                    throw new RuntimeException("The number of partitionColumn must be same as partitionValue");
                }

                Optional<String> webHdfsUrl = config.getWebhdfsUrl(datasource, catalog, schema, table);
                if (webHdfsUrl.isPresent()) {
                    List pathList = new ArrayList<>();
                    for (int i = 0; i < partitionColumnArray.length; i++) {
                        pathList.add(format("%s=%s", partitionColumnArray[i], partitionValuesArray[i]));
                    }
                    if (webHdfsProxyUser.isPresent() && webHdfsProxyPassword.isPresent()) {
                        setFirstPartitionWithWebHdfs(responseBody, webHdfsProxyUser, webHdfsProxyPassword, webHdfsUrl.get() + "/" + join("/", pathList));
                    } else {
                        setFirstPartitionWithWebHdfs(responseBody, prestoUser, prestoPassword, webHdfsUrl.get() + "/" + join("/", pathList));
                    }
                } else {
                    List<String> whereList = new ArrayList<>();
                    for (int i = 0; i < partitionColumnArray.length; i++) {
                        if (partitionColumnTypeArray[i].equals("varchar") || partitionColumnTypeArray[i].equals("string")) {
                            whereList.add(format("%s = '%s'", partitionColumnArray[i], partitionValuesArray[i]));
                        } else if (partitionColumnTypeArray[i].equals("date")) {
                            whereList.add(format("%s = DATE '%s'", partitionColumnArray[i], partitionValuesArray[i]));
                        } else {
                            whereList.add(format("%s = %s", partitionColumnArray[i], partitionValuesArray[i]));
                        }
                    }
                    String query = buildGetPartitionsQuery(datasource, catalog, schema, table, whereList);
                    if (user != null) {
                        log.info(format("%s executed %s in %s", user, query, datasource));
                    }
                    PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, query, user, prestoUser, prestoPassword, false, Integer.MAX_VALUE);
                    List<String> columns = prestoQueryResult.getColumns();
                    int index = 0;
                    for (String column : columns) {
                        if (column.equals(partitionColumnArray[partitionColumnArray.length - 1])) {
                            break;
                        }
                        index++;
                    }
                    responseBody.put("column", columns.get(index + 1));
                    Set<String> partitions = new TreeSet<>();
                    List<List<String>> records = prestoQueryResult.getRecords();
                    for (List<String> row : records) {
                        String partition = row.get(index + 1);
                        if (partition != null) {
                            partitions.add(partition);
                        }
                    }
                    responseBody.put("partitions", partitions);
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }

    private void setFirstPartitionWithWebHdfs(Map<String, Object> responseBody, Optional<String> prestoUser, Optional<String> prestoPassword, String webHdfsUrl) throws IOException {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (prestoUser.isPresent() && prestoPassword.isPresent()) {
            checkArgument(webHdfsUrl.startsWith("https"),
                    "Authentication using username/password requires HTTPS to be enabled");
            clientBuilder.addInterceptor(basicAuth(prestoUser.get(), prestoPassword.get()));
        }
        OkHttpClient client = clientBuilder.build();
        Request request = new Request.Builder().url(webHdfsUrl + "?op=LISTSTATUS").build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            requireNonNull(response.body(), "Response body is nothing");
            String json = response.body().string();
//                        "FileStatuses": {
//                        "FileStatus": [
//                        {
//                                "accessTime": 0,
//                                "blockSize": 0,
//                                "childrenNum": 271,
//                                "fileId": 43732679,
//                                "group": "hdfs",
//                                "length": 0,
//                                "modificationTime": 1527567948631,
//                                "owner": "hive",
//                                "pathSuffix": "hoge=piyo",
//                                "permission": "777",
//                                "replication": 0,
//                                "storagePolicy": 0,
//                                "type": "DIRECTORY"
//                        },
            Map map = OBJECT_MAPPER.readValue(json, Map.class);
            List<Map> partitionList = (List) ((Map) map.get("FileStatuses")).get("FileStatus");
            if (partitionList.size() > 0) {
                Set<String> partitions = new TreeSet<>();
                for (Map partition : partitionList) {
                    String data = (String) partition.get("pathSuffix");
                    if (data != null && data.contains("=")) {
                        responseBody.putIfAbsent("column", data.split("=")[0]);
                        partitions.add(data.split("=")[1]);
                    }
                }
                responseBody.put("partitions", partitions);
            }
        }
    }

    private String buildGetPartitionsQuery(String datasource, String catalog, String schema, String table) {
        if (config.isUseNewShowPartitions(datasource)) {
            return format("%sSELECT * FROM  %s.%s.\"%s$partitions\"", YANAGISHIMA_COMMENT, catalog, schema, table);
        }
        return format("%sSHOW PARTITIONS FROM %s.%s.\"%s\"", YANAGISHIMA_COMMENT, catalog, schema, table);
    }

    private String buildGetPartitionsQuery(String datasource, String catalog, String schema, String table, List<String> condition) {
        if (config.isUseNewShowPartitions(datasource)) {
            return format("%sSELECT * FROM  %s.%s.\"%s$partitions\" WHERE %s", YANAGISHIMA_COMMENT, catalog, schema, table, join(" AND ", condition));
        }
        return format("%sSHOW PARTITIONS FROM %s.%s.%s WHERE %s", YANAGISHIMA_COMMENT, catalog, schema, table, join(" AND ", condition));
    }

    @Nullable
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
