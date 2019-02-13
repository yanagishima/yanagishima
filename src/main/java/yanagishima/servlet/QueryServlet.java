package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.facebook.presto.client.OkHttpUtil.basicAuth;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@Singleton
public class QueryServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	private TinyORM db;

	private static final int LIMIT = 100;

	private OkHttpClient httpClient = new OkHttpClient();

	@Inject
	public QueryServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
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
		String prestoCoordinatorServer = yanagishimaConfig.getPrestoCoordinatorServerOrNull(datasource);
		if(prestoCoordinatorServer == null) {
			writer.println("[]");
			return;
		}

		String originalJson = null;
		Request prestoRequest = new Request.Builder().url(prestoCoordinatorServer + "/v1/query").build();

		Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
		Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
		if (prestoUser.isPresent() && prestoPassword.isPresent()) {
			if(prestoUser.get().length() == 0) {
				HashMap<String, Object> retVal = new HashMap<String, Object>();
				retVal.put("error", "user is empty");
				ObjectMapper mapper = new ObjectMapper();
				String json = mapper.writeValueAsString(retVal);
				writer.println(json);
				return;
			}
			OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
			clientBuilder.addInterceptor(basicAuth(prestoUser.get(), prestoPassword.get()));
			try (Response prestoResponse = clientBuilder.build().newCall(prestoRequest).execute()) {
				originalJson = prestoResponse.body().string();
				int code = prestoResponse.code();
				if(code != SC_OK) {
					HashMap<String, Object> retVal = new HashMap<String, Object>();
					retVal.put("code", code);
					retVal.put("error", prestoResponse.message());
					ObjectMapper mapper = new ObjectMapper();
					String json = mapper.writeValueAsString(retVal);
					writer.println(json);
					return;
				}
			}
		} else {
			try (Response prestoResponse = httpClient.newCall(prestoRequest).execute()) {
				originalJson = prestoResponse.body().string();
				int code = prestoResponse.code();
				if(code != SC_OK) {
					HashMap<String, Object> retVal = new HashMap<String, Object>();
					retVal.put("code", code);
					retVal.put("error", prestoResponse.message());
					ObjectMapper mapper = new ObjectMapper();
					String json = mapper.writeValueAsString(retVal);
					writer.println(json);
					return;
				}

			}
		}

		ObjectMapper mapper = new ObjectMapper();
		List<Map> list = mapper.readValue(originalJson, List.class);
		List<Map> runningList = list.stream().filter(m -> m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		List<Map> notRunningList = list.stream().filter(m -> !m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		runningList.sort((a,b)-> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));
		notRunningList.sort((a,b)-> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));

		List<Map> limitedList = new ArrayList<>();
		limitedList.addAll(runningList);
		if(list.size() > LIMIT) {
			limitedList.addAll(notRunningList.subList(0, LIMIT - runningList.size()));
		} else {
			limitedList.addAll(notRunningList.subList(0, list.size() - runningList.size()));
		}

		List<String> queryidList = new ArrayList<>();
		for(Map m : limitedList) {
			queryidList.add((String)m.get("queryId"));
		}

		String placeholder = queryidList.stream().map(r -> "?").collect(Collectors.joining(", "));
		List<Query> queryList = db.searchBySQL(Query.class,
				"SELECT engine, query_id, fetch_result_time_string, query_string FROM query WHERE engine='presto' and datasource=\'" + datasource + "\' and query_id IN (" + placeholder + ")",
				queryidList.stream().collect(Collectors.toList()));

		List<String> existdbQueryidList = new ArrayList<>();
		for(Query query : queryList) {
			existdbQueryidList.add(query.getQueryId());
		}
		for(Map m : limitedList) {
			String queryid = (String)m.get("queryId");
			if(existdbQueryidList.contains(queryid)) {
				m.put("existdb", true);
			} else {
				m.put("existdb", false);
			}
		}
		String json = mapper.writeValueAsString(limitedList);
		writer.println(json);
	}

}
