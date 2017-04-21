package yanagishima.servlet;

import com.facebook.presto.client.ErrorLocation;
import com.facebook.presto.client.QueryError;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Singleton
public class PrestoServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(PrestoServlet.class);

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	private final YanagishimaConfig yanagishimaConfig;

	static final Summary requestLatency = Summary.build()
			.name("yanagishima_requests_latency_seconds")
			.help("Request latency in seconds.").register();
	static final Counter requestSuccesses = Counter.build()
			.name("yanagishima_requests_successes_total")
			.help("Request successes.").register();
	static final Counter requestFailures = Counter.build()
			.name("yanagishima_requests_failures_total")
			.help("Request failures.").register();

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

		Summary.Timer requestTimer = requestLatency.startTimer();
		
		HashMap<String, Object> retVal = new HashMap<String, Object>();
		
		try {
			Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
			queryOptional.ifPresent(query -> {
				String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
				try {
					String datasource = HttpRequestUtil.getParam(request, "datasource");
					AccessControlUtil.checkDatasource(request, datasource);
					if(userName != null) {
						LOGGER.info(String.format("%s executed %s in %s", userName, query, datasource));
					}
					PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, query, userName);
					String queryid = prestoQueryResult.getQueryId();
					retVal.put("queryid", queryid);
					if (prestoQueryResult.getUpdateType() == null) {
						retVal.put("headers", prestoQueryResult.getColumns());
						retVal.put("results", prestoQueryResult.getRecords());
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
					requestSuccesses.inc();
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
					requestFailures.inc();
				}
			});
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			retVal.put("error", e.getMessage());
			requestFailures.inc();
		} finally {
			requestTimer.observeDuration();
		}

		JsonUtil.writeJSON(response, retVal);

	}

}
