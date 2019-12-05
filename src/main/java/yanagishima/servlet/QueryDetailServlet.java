package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

@Singleton
public class QueryDetailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final YanagishimaConfig config;

	@Inject
	public QueryDetailServlet(YanagishimaConfig config) {
		this.config = config;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String datasource = getRequiredParameter(request, "datasource");
		if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
			sendForbiddenError(response);
			return;
		}
		String redirectServer = config.getPrestoRedirectServer(datasource);
		response.sendRedirect(format("%s/query.html?%s", redirectServer, request.getParameter("queryid")));
	}
}
