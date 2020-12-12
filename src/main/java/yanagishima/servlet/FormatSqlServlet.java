package yanagishima.servlet;

import static io.prestosql.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DOUBLE;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.prestosql.sql.SqlFormatter;
import io.prestosql.sql.parser.ParsingException;
import io.prestosql.sql.parser.ParsingOptions;
import io.prestosql.sql.parser.SqlParser;
import io.prestosql.sql.tree.Statement;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class FormatSqlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> responseBody = new HashMap<>();
		try {
			String query = request.getParameter("query");
			if (query != null) {
				responseBody.put("formattedQuery", formatQuery(query));
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			if (e instanceof ParsingException) {
				responseBody.put("errorLineNumber", ((ParsingException) e).getLineNumber());
				responseBody.put("errorColumnNumber", ((ParsingException) e).getColumnNumber());
			}
			responseBody.put("error", e.getMessage());
		}
		writeJSON(response, responseBody);
	}

	private String formatQuery(String query) {
		SqlParser sqlParser = new SqlParser();
		Statement statement = sqlParser.createStatement(query, new ParsingOptions(AS_DOUBLE));
		return SqlFormatter.formatSql(statement);
	}
}
