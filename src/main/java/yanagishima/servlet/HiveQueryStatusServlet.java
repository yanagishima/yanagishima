package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import yanagishima.bean.SparkSqlJob;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class HiveQueryStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final YanagishimaConfig config;
	private final TinyORM db;

	@Inject
	public HiveQueryStatusServlet(YanagishimaConfig config, TinyORM db) {
		this.config = config;
		this.db = db;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String datasource = getRequiredParameter(request, "datasource");
		if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
			sendForbiddenError(response);
			return;
		}
		String queryId = getRequiredParameter(request, "queryid");
		String resourceManagerUrl = config.getResourceManagerUrl(datasource);
		String user = null;
		Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
		if (config.isUseAuditHttpHeaderName()) {
			user = request.getHeader(config.getAuditHttpHeaderName());
		} else {
			if (hiveUser.isPresent()) {
				user = hiveUser.get();
			}
		}

		String engine = getRequiredParameter(request, "engine");
		Optional<Query> queryOptional = db.single(Query.class).where("query_id=? and datasource=? and engine=?", queryId, datasource, engine).execute();
		if (engine.equals("hive")) {
			Optional<Map> application = YarnUtil.getApplication(resourceManagerUrl, queryId, user, config.getResourceManagerBegin(datasource));
			if (application.isPresent()) {
				writeJSON(response, application.get());
				return;
			}
			if (queryOptional.isEmpty()) {
				writeJSON(response, Map.of("state", "RUNNING", "progress", 0, "elapsedTime", 0));
				return;
			}
		}
		if (engine.equals("spark")) {
			Map<String, Object> retVal = new HashMap<String, Object>();
			if (queryOptional.isPresent()) {
				if (queryOptional.get().getStatus().equals(Status.SUCCEED.name())) {
					retVal.put("state", "FINISHED");
				} else if (queryOptional.get().getStatus().equals(Status.FAILED.name())) {
					retVal.put("state", "FAILED");
				} else {
					throw new IllegalArgumentException(String.format("unknown status=%s", queryOptional.get().getStatus()));
				}
			} else {
				retVal.put("state", "RUNNING");

				String sparkJdbcApplicationId = SparkUtil.getSparkJdbcApplicationId(config.getSparkWebUrl(datasource));
				List<Map> runningList = SparkUtil.getSparkRunningJobListWithProgress(resourceManagerUrl, sparkJdbcApplicationId);
				if (runningList.isEmpty()) {
					retVal.put("progress", 0);
				} else {
					List<SparkSqlJob> sparkSqlJobList = SparkUtil.getSparkSqlJobFromSqlserver(resourceManagerUrl, sparkJdbcApplicationId);
					for (Map m : runningList) {
						String groupId = (String) m.get("jobGroup");
						for (SparkSqlJob ssj : sparkSqlJobList) {
							if (ssj.getGroupId().equals(groupId) && ssj.getUser().equals(hiveUser.orElse(null)) && !ssj.getJobIds().isEmpty()) {
								int numTasks = (int) m.get("numTasks");
								int numCompletedTasks = (int) m.get("numCompletedTasks");
								double progress = ((double) numCompletedTasks / numTasks) * 100;
								retVal.put("progress", progress);
								break;
							}
						}
					}
				}
				retVal.put("elapsedTime", toElapsedTimeMillis(queryId));
			}
			writeJSON(response, retVal);
		} else {
			throw new IllegalArgumentException(engine + " is illegal");
		}
	}

	private static long toElapsedTimeMillis(String queryId) {
		String pattern = "yyyyMMdd_HHmmss";
		LocalDateTime submitTimeLdt = LocalDateTime.parse(queryId.substring(0, pattern.length()), DateTimeFormatter.ofPattern(pattern));
		ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
		return ChronoUnit.MILLIS.between(submitTimeZdt, ZonedDateTime.now(ZoneId.of("GMT")));
	}
}
