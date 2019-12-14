package yanagishima.servlet;

import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIAM_HIVE_JOB_PREFIX;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.YarnUtil;

@Singleton
public class YarnJobListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final int LIMIT = 100;

	private final YanagishimaConfig config;
	private final TinyORM db;

	@Inject
	public YarnJobListServlet(YanagishimaConfig config, TinyORM db) {
		this.config = config;
		this.db = db;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String datasource = getRequiredParameter(request, "datasource");
		if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
			sendForbiddenError(response);
			return;
		}
		String resourceManagerUrl = config.getResourceManagerUrl(datasource);
		List<Map> yarnJobs = YarnUtil.getJobList(resourceManagerUrl, config.getResourceManagerBegin(datasource));
		List<Map> runningJobs = yarnJobs.stream().filter(m -> m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		List<Map> finishedJobs = yarnJobs.stream().filter(m -> !m.get("state").equals("RUNNING")).collect(Collectors.toList());;
		runningJobs.sort((a, b)-> String.class.cast(b.get("id")).compareTo(String.class.cast(a.get("id"))));
		finishedJobs.sort((a, b)-> String.class.cast(b.get("id")).compareTo(String.class.cast(a.get("id"))));

		List<Map> limitedList;
		if (yarnJobs.size() > LIMIT) {
			limitedList = new ArrayList<>();
			limitedList.addAll(runningJobs);
			limitedList.addAll(finishedJobs.subList(0, LIMIT - runningJobs.size()));
		} else {
			limitedList = yarnJobs;
		}

		String userName = request.getHeader(config.getAuditHttpHeaderName());

		List<String> queryIds = new ArrayList<>();
		for (Map job : limitedList) {
			String name = (String) job.get("name");
			if (name.startsWith(YANAGISHIAM_HIVE_JOB_PREFIX)) {
				if (userName == null) {
					String queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length());
					queryIds.add(queryId);
				} else {
					String queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length() + userName.length() + 1);
					queryIds.add(queryId);
				}

			}
		}

		List<String> existdbQueryidList = new ArrayList<>();
		if (!queryIds.isEmpty()) {
			String placeholder = join(", ", nCopies(queryIds.size(), "?"));
			List<Query> queryList = db.searchBySQL(Query.class,
					"SELECT engine, query_id, fetch_result_time_string, query_string FROM query WHERE engine='hive' and datasource=\'" + datasource + "\' and query_id IN (" + placeholder + ")",
					queryIds.stream().collect(Collectors.toList()));

			for (Query query : queryList) {
				existdbQueryidList.add(query.getQueryId());
			}
		}

		for (Map job : limitedList) {
			String name = (String)job.get("name");
			if (name.startsWith(YANAGISHIAM_HIVE_JOB_PREFIX)) {
				String queryId;
				if (userName == null) {
					queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length());
				} else {
					queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length() + userName.length() + 1);
				}
				if (existdbQueryidList.contains(queryId)) {
					job.put("existdb", true);
				} else {
					job.put("existdb", false);
				}
			} else {
				job.put("existdb", false);
			}
		}

		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String json = OBJECT_MAPPER.writeValueAsString(limitedList);
		writer.println(json);
	}
}
