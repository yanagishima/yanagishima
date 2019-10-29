package yanagishima.servlet;

import io.prestosql.client.ClientException;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;
import yanagishima.service.PrestoService;
import yanagishima.util.JsonUtil;
import yanagishima.util.MetadataUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

@Singleton
public class PrestoServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(PrestoServlet.class);

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	private final YanagishimaConfig yanagishimaConfig;

	@Inject
	private TinyORM db;

	@Inject
	public PrestoServlet(PrestoService prestoService, YanagishimaConfig yanagishimaConfig) {
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
				if (yanagishimaConfig.isUserRequired() && userName == null) {
					sendForbiddenError(response);
					return;
				}
				try {
					String datasource = getRequiredParameter(request, "datasource");
					String prestoCoordinatorServer = yanagishimaConfig.getPrestoCoordinatorServerOrNull(datasource);
					if(prestoCoordinatorServer == null) {
						JsonUtil.writeJSON(response, retVal);
						return;
					}
					if (yanagishimaConfig.isCheckDatasource() && !validateDatasource(request, datasource)) {
						sendForbiddenError(response);
						return;
					}
					if(userName != null) {
						LOGGER.info(String.format("%s executed %s in %s", userName, query, datasource));
					}
					boolean storeFlag = Boolean.parseBoolean(Optional.ofNullable(request.getParameter("store")).orElse("false"));
					if (prestoUser.isPresent() && prestoPassword.isPresent()) {
						if(prestoUser.get().length() == 0) {
							retVal.put("error", "user is empty");
							JsonUtil.writeJSON(response, retVal);
							return;
						}
					}
					PrestoQueryResult prestoQueryResult;
					if(query.startsWith(YANAGISHIMA_COMMENT)) {
						prestoQueryResult = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword, storeFlag, Integer.MAX_VALUE);
					} else {
						prestoQueryResult = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword, storeFlag, yanagishimaConfig.getSelectLimit());
					}
					String queryid = prestoQueryResult.getQueryId();
					retVal.put("queryid", queryid);
					if (prestoQueryResult.getUpdateType() == null) {
						retVal.put("headers", prestoQueryResult.getColumns());

						if(query.startsWith(YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM")) {
							String catalog = query.substring((YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM").length()).trim();
							List<String> invisibleSchemas = yanagishimaConfig.getInvisibleSchemas(datasource, catalog);
							retVal.put("results", prestoQueryResult.getRecords().stream().filter(list -> !invisibleSchemas.contains(list.get(0))).collect(Collectors.toList()));
						} else {
							retVal.put("results", prestoQueryResult.getRecords());
						}
						retVal.put("lineNumber", Integer.toString(prestoQueryResult.getLineNumber()));
						retVal.put("rawDataSize", prestoQueryResult.getRawDataSize().toString());
						Optional<String> warningMessageOptinal = Optional.ofNullable(prestoQueryResult.getWarningMessage());
						warningMessageOptinal.ifPresent(warningMessage -> {
							retVal.put("warn", warningMessage);
						});
						if(query.startsWith(YANAGISHIMA_COMMENT + "DESCRIBE")) {
							if(yanagishimaConfig.getMetadataServiceUrl(datasource).isPresent()) {
								String[] strings = query.substring(YANAGISHIMA_COMMENT.length() + "DESCRIBE ".length()).split("\\.");
								String schema = strings[1];
								String table = strings[2].substring(1, strings[2].length() - 1);
								MetadataUtil.setMetadata(yanagishimaConfig.getMetadataServiceUrl(datasource).get(), retVal, schema, table, prestoQueryResult.getRecords());
							}
						}
					}
				} catch (QueryErrorException e) {
					LOGGER.warn(e.getCause().getMessage());
					retVal.put("error", e.getCause().getMessage());
					retVal.put("queryid", e.getQueryId());
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
