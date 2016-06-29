package yanagishima.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.facebook.presto.client.ErrorLocation;
import com.facebook.presto.client.QueryError;

import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;
import yanagishima.service.PrestoService;
import yanagishima.util.JsonUtil;

@Singleton
public class PrestoServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(PrestoServlet.class);

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	private final YanagishimaConfig yanagishimaConfig;

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
				String userData = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
				if(userData != null) {
					LOGGER.info(String.format("%s executed %s", userData, query));
				}
				try {
					PrestoQueryResult prestoQueryResult = prestoService.doQuery(query);
					retVal.put("queryid", prestoQueryResult.getQueryId());
					if (prestoQueryResult.getUpdateType() == null) {
						retVal.put("headers", prestoQueryResult.getColumns());
						retVal.put("results", prestoQueryResult.getRecords());
						Optional<String> warningMessageOptinal = Optional.ofNullable(prestoQueryResult.getWarningMessage());
						warningMessageOptinal.ifPresent(warningMessage -> {
							retVal.put("warn", warningMessage);
						});
					}
				} catch (QueryErrorException e) {
					LOGGER.error(e.getMessage(), e);
					Optional<QueryError> queryErrorOptional = Optional.ofNullable(e.getQueryError());
					queryErrorOptional.ifPresent(queryError -> {
						Optional<ErrorLocation> errorLocationOptional = Optional.ofNullable(queryError.getErrorLocation());
						errorLocationOptional.ifPresent(errorLocation -> {
							int errorLineNumber = errorLocation.getLineNumber();
							retVal.put("errorLineNumber", errorLineNumber);
						});
					});
					retVal.put("error", e.getCause().getMessage());
				}
			});
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			retVal.put("error", e.getMessage());
		}

		JsonUtil.writeJSON(response, retVal);

	}

}
