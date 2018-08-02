package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.YarnUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.Constants.YANAGISHIAM_HIVE_JOB_PREFIX;

@Singleton
public class YarnJobListServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(YarnJobListServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	private TinyORM db;

	private static final int LIMIT = 100;

	@Inject
	public YarnJobListServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

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
		String resourceManagerUrl = yanagishimaConfig.getResourceManagerUrl(datasource);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		List<Map> yarnJobList = YarnUtil.getJobList(resourceManagerUrl, yanagishimaConfig.getResourceManagerBegin(datasource));
		List<Map> runningList = yarnJobList.stream().filter(m -> m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		List<Map> notRunningList = yarnJobList.stream().filter(m -> !m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		runningList.sort((a,b)-> String.class.cast(b.get("id")).compareTo(String.class.cast(a.get("id"))));
		notRunningList.sort((a,b)-> String.class.cast(b.get("id")).compareTo(String.class.cast(a.get("id"))));

		List<Map> limitedList;
		if(yarnJobList.size() > LIMIT) {
			limitedList = new ArrayList<>();
			limitedList.addAll(runningList);
			limitedList.addAll(notRunningList.subList(0, LIMIT - runningList.size()));
		} else {
			limitedList = yarnJobList;
		}

		String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());

		List<String> queryidList = new ArrayList<>();
		for(Map m : limitedList) {
			String name = (String)m.get("name");
			if(name.startsWith(YANAGISHIAM_HIVE_JOB_PREFIX)) {
				if(userName == null) {
					String queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length());
					queryidList.add(queryId);
				} else {
					String queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length() + userName.length() + 1);
					queryidList.add(queryId);
				}

			}
		}

		String placeholder = queryidList.stream().map(r -> "?").collect(Collectors.joining(", "));
		List<Query> queryList = db.searchBySQL(Query.class,
				"SELECT engine, query_id, fetch_result_time_string, query_string FROM query WHERE engine='hive' and datasource=\'" + datasource + "\' and query_id IN (" + placeholder + ")",
				queryidList.stream().collect(Collectors.toList()));

		List<String> existdbQueryidList = new ArrayList<>();
		for(Query query : queryList) {
			existdbQueryidList.add(query.getQueryId());
		}

		for(Map m : limitedList) {
			String name = (String)m.get("name");
			if(name.startsWith(YANAGISHIAM_HIVE_JOB_PREFIX)) {
				String queryId = null;
				if(userName == null) {
					queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length());
				} else {
					queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length() + userName.length() + 1);
				}
				if(existdbQueryidList.contains(queryId)) {
					m.put("existdb", true);
				} else {
					m.put("existdb", false);
				}
			} else {
				m.put("existdb", false);
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(limitedList);
		writer.println(json);
	}

}
