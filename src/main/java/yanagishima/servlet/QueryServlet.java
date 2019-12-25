package yanagishima.servlet;

import static io.prestosql.client.OkHttpUtil.basicAuth;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.geso.tinyorm.TinyORM;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;

@Singleton
public class QueryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int LIMIT = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final YanagishimaConfig config;
    private final TinyORM db;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Inject
    public QueryServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        String datasource = getRequiredParameter(request, "datasource");
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return;
        }
        String coordinatorServer = config.getPrestoCoordinatorServerOrNull(datasource);
        if (coordinatorServer == null) {
            writer.println("[]");
            return;
        }

        Optional<String> user = Optional.ofNullable(request.getParameter("user"));
        Optional<String> password = Optional.ofNullable(request.getParameter("password"));
        OkHttpClient client = httpClient;
        if (user.isPresent() && password.isPresent()) {
            if (user.get().isEmpty()) {
                writer.println(OBJECT_MAPPER.writeValueAsString(Map.of("error", "user is empty")));
                return;
            }
            OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
            clientBuilder.addInterceptor(basicAuth(user.get(), password.get()));
            client = clientBuilder.build();
        }

        Request prestoRequest = new Request.Builder().url(coordinatorServer + "/v1/query").build();
        String originalJson;
        try (Response prestoResponse = client.newCall(prestoRequest).execute()) {
            originalJson = prestoResponse.body().string();
            int code = prestoResponse.code();
            if (code != SC_OK) {
                writer.println(OBJECT_MAPPER.writeValueAsString(Map.of("code", code, "error", prestoResponse.message())));
                return;
            }
        }

        List<Map> list = OBJECT_MAPPER.readValue(originalJson, List.class);
        List<Map> runningList = list.stream().filter(m -> m.get("state").equals("RUNNING")).collect(Collectors.toList());
        List<Map> notRunningList = list.stream().filter(m -> !m.get("state").equals("RUNNING")).collect(Collectors.toList());
        runningList.sort((a, b) -> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));
        notRunningList.sort((a, b) -> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));

        List<Map> limitedList = new ArrayList<>();
        limitedList.addAll(runningList);
        limitedList.addAll(notRunningList.subList(0, Math.min(LIMIT, list.size()) - runningList.size()));

        List<String> queryIds = limitedList.stream().map(query -> (String) query.get("queryId")).collect(Collectors.toList());

        String placeholder = join(", ", nCopies(queryIds.size(), "?"));
        List<Query> queries = db.searchBySQL(Query.class,
                                             format("SELECT engine, query_id, fetch_result_time_string, query_string "
                                                    + "FROM query "
                                                    + "WHERE engine='presto' and datasource=\'%s\' and query_id IN (%s)",
                                                    datasource, placeholder),
                                             new ArrayList<>(queryIds));

        List<String> existDbQueryIds = new ArrayList<>();
        for (Query query : queries) {
            existDbQueryIds.add(query.getQueryId());
        }
        for (Map query : limitedList) {
            String queryId = (String) query.get("queryId");
            query.put("existdb", existDbQueryIds.contains(queryId));
        }
        String json = OBJECT_MAPPER.writeValueAsString(limitedList);
        writer.println(json);
    }
}
