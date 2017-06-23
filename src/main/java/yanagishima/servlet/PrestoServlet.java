package yanagishima.servlet;

import com.facebook.presto.client.ErrorLocation;
import com.facebook.presto.client.QueryError;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;
import yanagishima.row.Query;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

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
				String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
				if(yanagishimaConfig.isUserRequired() && userName == null) {
					try {
						response.sendError(SC_FORBIDDEN);
						return;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				try {
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
					boolean storeFlag = Boolean.parseBoolean(Optional.ofNullable(request.getParameter("store")).orElse("false"));
					int limit = yanagishimaConfig.getSelectLimit();
					PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, query, userName, storeFlag, limit);
					String queryid = prestoQueryResult.getQueryId();
					retVal.put("queryid", queryid);
					if (prestoQueryResult.getUpdateType() == null) {
						retVal.put("headers", prestoQueryResult.getColumns());

						if(query.toLowerCase().indexOf(YANAGISHIMA_COMMENT + "show schemas from") != -1) {
							String catalog = query.substring((YANAGISHIMA_COMMENT + "show schemas from").length()).trim();
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
						Optional<Query> queryDataOptional = db.single(Query.class).where("query_id=? and datasource=?", queryid, datasource).execute();
						queryDataOptional.ifPresent(queryData -> {
							LocalDateTime submitTimeLdt = LocalDateTime.parse(queryid.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
							ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
							String fetchResultTimeString = queryData.getFetchResultTimeString();
							ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
							long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
							retVal.put("elapsedTimeMillis", elapsedTimeMillis);
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
					retVal.put("queryid", e.getQueryId());
				}
			});
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			retVal.put("error", e.getMessage());
		}

		JsonUtil.writeJSON(response, retVal);

	}

}
