package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.bean.SparkSqlJob;
import yanagishima.config.YanagishimaConfig;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.SparkUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class SparkJobListServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(SparkJobListServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	private TinyORM db;

	private static final int LIMIT = 100;

	@Inject
	public SparkJobListServlet(YanagishimaConfig yanagishimaConfig) {
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
		String sparkJdbcApplicationId = SparkUtil.getSparkJdbcApplicationId(yanagishimaConfig.getSparkWebUrl(datasource));
		List<Map> runningList = SparkUtil.getSparkRunningJobListWithProgress(resourceManagerUrl, sparkJdbcApplicationId);
		List<SparkSqlJob> sparkSqlJobList = SparkUtil.getSparkSqlJobFromSqlserver(resourceManagerUrl, sparkJdbcApplicationId);
		for(Map m : runningList) {
			String groupId = (String)m.get("jobGroup");
			for(SparkSqlJob ssj : sparkSqlJobList) {
				if(ssj.getGroupId().equals(groupId)) {
					m.put("jobIds", ssj.getJobIds());
					m.put("user", ssj.getUser());
					m.put("query", ssj.getStatement());
					m.put("duration", ssj.getDuration());
					break;
				}
			}
		}
		List<SparkSqlJob> notRunningList = sparkSqlJobList.stream().filter(j -> j.getJobIds().size() > 0).filter(j -> j.getState().equals("FINISHED") || j.getState().equals("FAILED")).sorted((a,b)-> b.getStartTime().compareTo(a.getStartTime())).limit(LIMIT).collect(Collectors.toList());;

		List<Map> limitedList = new ArrayList<>();;
		limitedList.addAll(runningList);
		for(SparkSqlJob ssj : notRunningList) {
			Map<String, Object> m = new HashMap<>();
			m.put("jobGroup", ssj.getGroupId());
			m.put("jobIds", ssj.getJobIds());
			m.put("user", ssj.getUser());
			m.put("query", ssj.getStatement());
			m.put("status", ssj.getState());
			m.put("submissionTime", ssj.getStartTime());
			m.put("duration", ssj.getDuration());
			limitedList.add(m);
		}

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(limitedList);
		writer.println(json);
	}

}
