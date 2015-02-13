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

import com.facebook.presto.client.ClientSession;
import com.facebook.presto.client.Column;
import com.facebook.presto.client.QueryError;
import com.facebook.presto.client.QueryResults;
import com.facebook.presto.client.StatementClient;

public class PrestoServiceImpl implements PrestoService {

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	public PrestoServiceImpl(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}
	
	@Override
	public List<String> getHeaders(String query) throws SQLException {
		try (StatementClient client = getStatementClient(query)) {
			List<Column> columns = getColumns(client);
			List<String> headers = columns.stream().map(column -> column.getName()).collect(Collectors.toList());
			return headers;
		}
	}

	@Override
	public List<List<Object>> doQuery(String query) {

		try (StatementClient client = getStatementClient(query)) {
			List<List<Object>> rowDataList = new ArrayList<List<Object>>();
			while (client.isValid()) {
				Iterable<List<Object>> data = client.current().getData();
				client.advance();
				if (data != null) {
					for (List<Object> row : data) {
						List<Object> columnDataList = new ArrayList<Object>();
						for (Object columnData : row) {
							columnDataList.add(columnData);
						}
						rowDataList.add(columnDataList);
					}
				}
			}
			return rowDataList;
		}
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
		return new StatementClient(httpClient,
				jsonCodec, clientSession, query);
	}

	private List<Column> getColumns(StatementClient client) throws SQLException {
		while (client.isValid()) {
			List<Column> columns = client.current().getColumns();
			if (columns != null) {
				return columns;
			}
			client.advance();
		}

		QueryResults results = client.finalResults();
		if (!client.isFailed()) {
			throw new SQLException(format("Query has no columns (#%s)",
					results.getId()));
		}
		throw resultsException(results);
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
