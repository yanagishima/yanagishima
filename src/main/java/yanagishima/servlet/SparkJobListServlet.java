package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		List<Map> jobList = SparkUtil.getJobList(resourceManagerUrl, SparkUtil.getSparkJdbcApplicationId(yanagishimaConfig.getSparkWebUrl(datasource)));
		List<Map> runningList = jobList.stream().filter(m -> m.get("status").equals("RUNNING")).collect(Collectors.toList());;
		List<Map> notRunningList = jobList.stream().filter(m -> !m.get("status").equals("RUNNING")).collect(Collectors.toList());;
		runningList.sort((a,b)-> Integer.class.cast(b.get("jobId")) - Integer.class.cast(a.get("jobId")));
		notRunningList.sort((a,b)-> Integer.class.cast(b.get("jobId")) - Integer.class.cast(a.get("jobId")));

		List<Map> limitedList;
		if(jobList.size() > LIMIT) {
			limitedList = new ArrayList<>();
			limitedList.addAll(runningList);
			limitedList.addAll(notRunningList.subList(0, LIMIT - runningList.size()));
		} else {
			limitedList = jobList;
		}

		for(Map m : limitedList) {
			m.put("existdb", false);
		}
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(limitedList);
		writer.println(json);
	}

}
