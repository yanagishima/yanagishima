package yanagishima.service;

import static io.airlift.json.JsonCodec.jsonCodec;
import static java.lang.String.format;
import io.airlift.http.client.HttpClientConfig;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.json.JsonCodec;
import io.airlift.units.Duration;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import yanagishima.config.YanagishimaConfig;
import yanagishima.result.PrestoQueryResult;

import com.facebook.presto.client.ClientSession;
import com.facebook.presto.client.Column;
import com.facebook.presto.client.QueryError;
import com.facebook.presto.client.QueryResults;
import com.facebook.presto.client.StatementClient;
import com.google.common.collect.Lists;

public class PrestoServiceImpl implements PrestoService {

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	public PrestoServiceImpl(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	public PrestoQueryResult doQuery(String query) throws SQLException {

		try (StatementClient client = getStatementClient(query)) {
			while (client.isValid() && (client.current().getData() == null)) {
				client.advance();
			}

			if ((!client.isFailed()) && (!client.isGone())
					&& (!client.isClosed())) {
				QueryResults results = client.isValid() ? client.current()
						: client.finalResults();
				if (results.getUpdateType() != null) {
					PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
					prestoQueryResult.setUpdateType(results.getUpdateType());
					return prestoQueryResult;
				} else if (results.getColumns() == null) {
					throw new SQLException(format("Query %s has no columns\n",
							results.getId()));
				} else {
					PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
					prestoQueryResult.setUpdateType(results.getUpdateType());
					 List<String> columns = Lists.transform(results.getColumns(), Column::getName);
					 prestoQueryResult.setColumns(columns);
					List<List<Object>> rowDataList = new ArrayList<List<Object>>();
					while (client.isValid()) {
						Iterable<List<Object>> data = client.current().getData();
						client.advance();
						if (data != null) {
							data.forEach(row -> {
								List<Object> columnDataList = row.stream().collect(
										Collectors.toList());
								rowDataList.add(columnDataList);
							});
						}
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

		HttpClientConfig httpClientConfig = new HttpClientConfig()
				.setConnectTimeout(new Duration(10, TimeUnit.SECONDS));
		JettyHttpClient httpClient = new JettyHttpClient(httpClientConfig);
		JsonCodec<QueryResults> jsonCodec = jsonCodec(QueryResults.class);

		ClientSession clientSession = new ClientSession(
				URI.create(prestoCoordinatorServer), user, source, catalog,
				schema, TimeZone.getDefault().getID(), Locale.getDefault(),
				new HashMap<String, String>(), false);
		return new StatementClient(httpClient, jsonCodec, clientSession, query);
	}

	private SQLException resultsException(QueryResults results) {
		QueryError error = results.getError();
		String message = format("Query failed (#%s): %s", results.getId(),
				error.getMessage());
		Throwable cause = (error.getFailureInfo() == null) ? null : error
				.getFailureInfo().toException();
		return new SQLException(message, error.getSqlState(),
				error.getErrorCode(), cause);
	}

}
