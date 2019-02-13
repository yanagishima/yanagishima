package yanagishima.servlet;

import com.facebook.presto.client.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class PrestoAsyncServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(PrestoAsyncServlet.class);

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	private final YanagishimaConfig yanagishimaConfig;

	@Inject
	public PrestoAsyncServlet(PrestoService prestoService, YanagishimaConfig yanagishimaConfig) {
		this.prestoService = prestoService;
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HashMap<String, Object> retVal = new HashMap<String, Object>();
		
		try {
			Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
			queryOptional.ifPresent(query -> {
				String userName = null;
				Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
				Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
				if(yanagishimaConfig.isUseAuditHttpHeaderName()) {
					userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
				} else {
					if (prestoUser.isPresent() && prestoPassword.isPresent()) {
						userName = prestoUser.get();
					}
				}
				if(yanagishimaConfig.isUserRequired() && userName == null) {
					try {
						response.sendError(SC_FORBIDDEN);
						return;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
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
				if(userName != null) {
					LOGGER.info(String.format("%s executed %s in %s", userName, query, datasource));
				}
				if (prestoUser.isPresent() && prestoPassword.isPresent()) {
					if(prestoUser.get().length() == 0) {
						retVal.put("error", "user is empty");
						JsonUtil.writeJSON(response, retVal);
						return;
					}
				}
				try {
					String queryid = prestoService.doQueryAsync(datasource, query, userName, prestoUser, prestoPassword);
					retVal.put("queryid", queryid);
				} catch (ClientException e) {
					if(prestoUser.isPresent()) {
						LOGGER.error(String.format("%s failed to be authenticated", prestoUser.get()));
					}
					LOGGER.error(e.getMessage(), e);
					retVal.put("error", e.getMessage());
				} catch (Throwable e) {
					LOGGER.error(e.getMessage(), e);
					retVal.put("error", e.getMessage());
				}
			});
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			retVal.put("error", e.getMessage());
		}

		JsonUtil.writeJSON(response, retVal);

	}

}
