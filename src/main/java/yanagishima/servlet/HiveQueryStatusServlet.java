package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import yanagishima.config.YanagishimaConfig;
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
import java.util.Map;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class HiveQueryStatusServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	public HiveQueryStatusServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
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
		String queryid = Optional.ofNullable(request.getParameter("queryid")).get();
		String resourceManagerUrl = yanagishimaConfig.getResourceManagerUrl(datasource).get();
		String userName = null;
		Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
		if(yanagishimaConfig.isUseAuditHttpHeaderName()) {
			userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
		} else {
			if (hiveUser.isPresent()) {
				userName = hiveUser.get();
			}
		}
		Optional<Map> applicationOptional = YarnUtil.getApplication(resourceManagerUrl, queryid, userName, yanagishimaConfig.getResourceManagerBegin(datasource));

		applicationOptional.ifPresent(application -> {
			try {
				response.setContentType("application/json");
				PrintWriter writer = response.getWriter();
				ObjectMapper mapper = new ObjectMapper();
				String json = mapper.writeValueAsString(application);
				writer.println(json);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
