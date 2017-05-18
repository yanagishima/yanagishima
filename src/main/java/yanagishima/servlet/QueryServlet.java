package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import org.apache.http.client.fluent.Request;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class QueryServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	private TinyORM db;

	private static final int LIMIT = 100;

	@Inject
	public QueryServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String datasource = HttpRequestUtil.getParam(request, "datasource");
		AccessControlUtil.checkDatasource(request, datasource);
		String prestoCoordinatorServer = yanagishimaConfig
				.getPrestoCoordinatorServer(datasource);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String originalJson = Request.Get(prestoCoordinatorServer + "/v1/query")
				.execute().returnContent().asString(StandardCharsets.UTF_8);
		ObjectMapper mapper = new ObjectMapper();
		List<Map> list = mapper.readValue(originalJson, List.class);
		List<Map> runningList = list.stream().filter(m -> m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		List<Map> notRunningList = list.stream().filter(m -> !m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		runningList.sort((a,b)-> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));
		notRunningList.sort((a,b)-> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));

		List<Map> limitedList;
		if(list.size() > LIMIT) {
			limitedList = new ArrayList<>();
			limitedList.addAll(runningList);
			limitedList.addAll(notRunningList.subList(0, LIMIT - runningList.size()));
		} else {
			limitedList = list;
		}

		for(Map m : limitedList) {
			Optional<Query> queryOptional = db.single(Query.class).where("query_id=? and datasource=?", m.get("queryId"), datasource).execute();
			if(queryOptional.isPresent()) {
				m.put("existdb", true);
			} else {
				m.put("existdb", false);
			}
		}
		String json = mapper.writeValueAsString(limitedList);
		writer.println(json);
	}

}
