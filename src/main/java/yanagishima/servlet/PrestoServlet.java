package yanagishima.servlet;

import io.prestosql.client.ClientException;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.model.presto.PrestoQueryResult;
import yanagishima.service.PrestoService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Slf4j
@Singleton
public class PrestoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;
	private final YanagishimaConfig config;

	@Inject
	public PrestoServlet(PrestoService prestoService, YanagishimaConfig config) {
		this.prestoService = prestoService;
		this.config = config;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> responseBody = new HashMap<>();

		try {
			Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
			queryOptional.ifPresent(query -> {
				String userName = getUsername(request);
				Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
				Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
				if (config.isUserRequired() && userName == null) {
					sendForbiddenError(response);
					return;
				}
				try {
					String datasource = getRequiredParameter(request, "datasource");
					String coordinatorServer = config.getPrestoCoordinatorServerOrNull(datasource);
					if (coordinatorServer == null) {
						writeJSON(response, responseBody);
						return;
					}
					if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
						sendForbiddenError(response);
						return;
					}
					if (userName != null) {
						log.info(format("%s executed %s in %s", userName, query, datasource));
					}
					boolean storeFlag = Boolean.parseBoolean(Optional.ofNullable(request.getParameter("store")).orElse("false"));
					if (prestoUser.isPresent() && prestoPassword.isPresent()) {
						if (prestoUser.get().length() == 0) {
							responseBody.put("error", "user is empty");
							writeJSON(response, responseBody);
							return;
						}
					}
					PrestoQueryResult prestoQueryResult;
					if (query.startsWith(YANAGISHIMA_COMMENT)) {
						prestoQueryResult = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword, storeFlag, Integer.MAX_VALUE);
					} else {
						prestoQueryResult = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword, storeFlag, config.getSelectLimit());
					}
					String queryId = prestoQueryResult.getQueryId();
					responseBody.put("queryid", queryId);
					if (prestoQueryResult.getUpdateType() == null) {
						responseBody.put("headers", prestoQueryResult.getColumns());

						if (query.startsWith(YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM")) {
							String catalog = query.substring((YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM").length()).trim();
							List<String> invisibleSchemas = config.getInvisibleSchemas(datasource, catalog);
							responseBody.put("results", prestoQueryResult.getRecords().stream().filter(list -> !invisibleSchemas.contains(list.get(0))).collect(Collectors.toList()));
						} else {
							responseBody.put("results", prestoQueryResult.getRecords());
						}
						responseBody.put("lineNumber", Integer.toString(prestoQueryResult.getLineNumber()));
						responseBody.put("rawDataSize", prestoQueryResult.getRawDataSize().toString());
						Optional<String> warningMessageOptinal = Optional.ofNullable(prestoQueryResult.getWarningMessage());
						warningMessageOptinal.ifPresent(warningMessage -> {
							responseBody.put("warn", warningMessage);
						});
					}
				} catch (QueryErrorException e) {
					log.warn(e.getCause().getMessage());
					responseBody.put("error", e.getCause().getMessage());
					responseBody.put("queryid", e.getQueryId());
				} catch (ClientException e) {
					if (prestoUser.isPresent()) {
						log.error(format("%s failed to be authenticated", prestoUser.get()));
					}
					log.error(e.getMessage(), e);
					responseBody.put("error", e.getMessage());
				} catch (Throwable e) {
					log.error(e.getMessage(), e);
					responseBody.put("error", e.getMessage());
				}
			});
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			responseBody.put("error", e.getMessage());
		}
		writeJSON(response, responseBody);
	}

	@Nullable
	private String getUsername(HttpServletRequest request) {
		if (config.isUseAuditHttpHeaderName()) {
			return request.getHeader(config.getAuditHttpHeaderName());
		}
		String user = request.getParameter("user");
		String password = request.getParameter("password");
		if (user != null && password != null) {
			return user;
		}
		return null;
	}
}
