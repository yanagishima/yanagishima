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

import yanagishima.result.PrestoQueryResult;
import yanagishima.service.PrestoService;
import yanagishima.util.JsonUtil;

@Singleton
public class PrestoServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(PrestoServlet.class);

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	@Inject
	public PrestoServlet(PrestoService prestoService) {
		this.prestoService = prestoService;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
		queryOptional.ifPresent(query -> {

			HashMap<String, Object> retVal = new HashMap<String, Object>();

			try {
				PrestoQueryResult prestoQueryResult = prestoService.doQuery(query);
				if (prestoQueryResult.getUpdateType() == null) {// select
					retVal.put("headers", prestoQueryResult.getColumns());
					retVal.put("results", prestoQueryResult.getRecords());
					Optional<String> warningMessageOptinal = Optional.ofNullable(prestoQueryResult.getWarningMessage());
					warningMessageOptinal.ifPresent(warningMessage -> {
						retVal.put("warn", warningMessage);
					});
				}
			} catch (Throwable e) {
				LOGGER.error(e.getMessage(), e);
				retVal.put("error", e.getMessage());
			}

			JsonUtil.writeJSON(response, retVal);

		});

	}

}
