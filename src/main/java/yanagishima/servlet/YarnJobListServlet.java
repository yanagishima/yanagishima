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
import yanagishima.util.YarnUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		String resourceManagerUrl = yanagishimaConfig.getResourceManagerUrl(datasource).get();
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		List<Map> yarnJobList = YarnUtil.getJobList(resourceManagerUrl);
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

		for(Map m : limitedList) {
			String name = (String)m.get("name");
			if(name.startsWith(YANAGISHIAM_HIVE_JOB_PREFIX)) {
				String queryId = name.substring(YANAGISHIAM_HIVE_JOB_PREFIX.length());
				Optional<Query> queryOptional = db.single(Query.class).where("engine='hive' and query_id=? and datasource=?", queryId, datasource).execute();
				if(queryOptional.isPresent()) {
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
