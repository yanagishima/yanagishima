package yanagishima.service;

import static io.airlift.json.JsonCodec.jsonCodec;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;

import io.airlift.http.client.HttpClientConfig;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.json.JsonCodec;
import io.airlift.units.Duration;

import java.net.URI;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;

import com.facebook.presto.client.ClientSession;
import com.facebook.presto.client.Column;
import com.facebook.presto.client.QueryError;
import com.facebook.presto.client.QueryResults;
import com.facebook.presto.client.StatementClient;
import com.google.common.collect.Lists;
import yanagishima.row.Query;

public class PrestoServiceImpl implements PrestoService {

	private YanagishimaConfig yanagishimaConfig;

	private JettyHttpClient httpClient;

	@Inject
	private TinyORM db;

	@Inject
	public PrestoServiceImpl(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
		HttpClientConfig httpClientConfig = new HttpClientConfig().setConnectTimeout(new Duration(10, TimeUnit.SECONDS));
		this.httpClient = new JettyHttpClient(httpClientConfig);
	}

	@Override
	public PrestoQueryResult doQuery(String query) throws QueryErrorException {
		
		int limit = yanagishimaConfig.getSelectLimit();

		try (StatementClient client = getStatementClient(query)) {
			while (client.isValid() && (client.current().getData() == null)) {
				client.advance();
			}

			if ((!client.isFailed()) && (!client.isGone()) && (!client.isClosed())) {
				QueryResults results = client.isValid() ? client.current() : client.finalResults();
				String queryId = results.getId();
				db.insert(Query.class)
						.value("query_id", queryId)
						.value("fetch_result_time_string", ZonedDateTime.now().toString())
						.value("query_string", query)
						.execute();
				if (results.getUpdateType() != null) {
					PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
					prestoQueryResult.setQueryId(queryId);
					prestoQueryResult.setUpdateType(results.getUpdateType());
					return prestoQueryResult;
				} else if (results.getColumns() == null) {
					throw new QueryErrorException(new SQLException(format("Query %s has no columns\n", results.getId())));
				} else {
					PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
					prestoQueryResult.setQueryId(queryId);
					prestoQueryResult.setUpdateType(results.getUpdateType());
					 List<String> columns = Lists.transform(results.getColumns(), Column::getName);
					 prestoQueryResult.setColumns(columns);
					List<List<Object>> rowDataList = new ArrayList<List<Object>>();
					while (client.isValid()) {
						Iterable<List<Object>> data = client.current().getData();
						if (data != null) {
							data.forEach(row -> {
								List<Object> columnDataList = new ArrayList<>();
								List<Object> tmpColumnDataList = row.stream().collect(Collectors.toList());
								for (Object tmpColumnData : tmpColumnDataList) {
									if(tmpColumnData instanceof Long) {
										columnDataList.add(((Long) tmpColumnData).toString());
									} else {
										columnDataList.add(tmpColumnData);
									}
								}
								rowDataList.add(columnDataList);
							});
						}
						if(rowDataList.size() >= limit) {
							prestoQueryResult.setWarningMessage(String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rowDataList.size(), limit));
							break;
						}
						client.advance();
					}
					prestoQueryResult.setRecords(rowDataList);
					return prestoQueryResult;
				}
			}

			if (client.isClosed()) {
				throw new RuntimeException("Query aborted by user");
			} else if (client.isGone()) {
				throw new RuntimeException("Query is gone (server restarted?)");
			} else if (client.isFailed()) {
				throw resultsException(client.finalResults());
			}
			
		}
		throw new RuntimeException("should not reach");

	}

	private StatementClient getStatementClient(String query) {
		String prestoCoordinatorServer = yanagishimaConfig
				.getPrestoCoordinatorServer();
		String catalog = yanagishimaConfig.getCatalog();
		String schema = yanagishimaConfig.getSchema();
		String user = yanagishimaConfig.getUser();
		String source = yanagishimaConfig.getSource();

		JsonCodec<QueryResults> jsonCodec = jsonCodec(QueryResults.class);

		ClientSession clientSession = new ClientSession(
				URI.create(prestoCoordinatorServer), user, source, catalog,
				schema, TimeZone.getDefault().getID(), Locale.getDefault(),
				new HashMap<String, String>(), null, false, new Duration(2, MINUTES));
		return new StatementClient(httpClient, jsonCodec, clientSession, query);
	}

	private QueryErrorException resultsException(QueryResults results) {
		QueryError error = results.getError();
		String message = format("Query failed (#%s): %s", results.getId(), error.getMessage());
		Throwable cause = (error.getFailureInfo() == null) ? null : error.getFailureInfo().toException();
		return new QueryErrorException(error, new SQLException(message, error.getSqlState(), error.getErrorCode(), cause));
	}

}
