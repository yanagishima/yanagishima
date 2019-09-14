package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import yanagishima.bean.HttpRequestContext;
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

import static java.util.Objects.requireNonNull;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class HiveQueryStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final YanagishimaConfig config;
	private final TinyORM db;

	@Inject
	public HiveQueryStatusServlet(YanagishimaConfig config, TinyORM db) {
		this.config = config;
		this.db = db;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpRequestContext context = new HttpRequestContext(request);
		requireNonNull(context.getDatasource(), "datasource is null");
		requireNonNull(context.getQueryId(), "queryid is null");
		requireNonNull(context.getEngine(), "engine is null");

		if(config.isCheckDatasource()) {
			if(!AccessControlUtil.validateDatasource(request, context.getDatasource())) {
				try {
					response.sendError(SC_FORBIDDEN);
					return;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		String resourceManagerUrl = config.getResourceManagerUrl(context.getDatasource());
		String userName = null;
		Optional<String> hiveUser = Optional.ofNullable(context.getUser());
		if(config.isUseAuditHttpHeaderName()) {
			userName = request.getHeader(config.getAuditHttpHeaderName());
		} else {
			if (hiveUser.isPresent()) {
				userName = hiveUser.get();
			}
		}

		Optional<Query> queryOptional = db.single(Query.class).where("query_id=? and datasource=? and engine=?", context.getQueryId(), context.getDatasource(), context.getEngine()).execute();
		if("hive".equals(context.getEngine())) {
			Optional<Map> applicationOptional = YarnUtil.getApplication(resourceManagerUrl, context.getQueryId(), userName, config.getResourceManagerBegin(context.getDatasource()));
			if(applicationOptional.isPresent()) {
				try {
					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					ObjectMapper mapper = new ObjectMapper();
					String json = mapper.writeValueAsString(applicationOptional.get());
					writer.println(json);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				if(!queryOptional.isPresent()) {
					HashMap<String, Object> retVal = new HashMap<String, Object>();
					retVal.put("state", "RUNNING");
					retVal.put("progress", 0);
					retVal.put("elapsedTime", 0);
					writeJSON(response, retVal);
				}
			}
		} else if("spark".equals(context.getEngine())) {
			HashMap<String, Object> retVal = new HashMap<String, Object>();
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

                String sparkJdbcApplicationId = SparkUtil.getSparkJdbcApplicationId(config.getSparkWebUrl(context.getDatasource()));
                List<Map> runningList = SparkUtil.getSparkRunningJobListWithProgress(resourceManagerUrl, sparkJdbcApplicationId);
                if(runningList.isEmpty()) {
                    retVal.put("progress", 0);
                } else {
                    List<SparkSqlJob> sparkSqlJobList = SparkUtil.getSparkSqlJobFromSqlserver(resourceManagerUrl, sparkJdbcApplicationId);
                    for(Map m : runningList) {
                        String groupId = (String)m.get("jobGroup");
                        for(SparkSqlJob ssj : sparkSqlJobList) {
                            if(ssj.getGroupId().equals(groupId) && ssj.getUser().equals(hiveUser.orElse(null)) && !ssj.getJobIds().isEmpty()) {
                                int numTasks = (int) m.get("numTasks");
                                int numCompletedTasks = (int) m.get("numCompletedTasks");
                                double progress = ((double) numCompletedTasks / numTasks) * 100;
                                retVal.put("progress", progress);
                                break;
                            }
                        }
                    }
                }

				LocalDateTime submitTimeLdt = LocalDateTime.parse(context.getQueryId().substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
				ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
				long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, ZonedDateTime.now(ZoneId.of("GMT")));
				retVal.put("elapsedTime", elapsedTimeMillis);
			}
			writeJSON(response, retVal);
		} else {
			throw new IllegalArgumentException(context.getEngine() + " is illegal");
		}
	}
}
