package yanagishima.servlet;

import com.facebook.presto.sql.SqlFormatter;
import com.facebook.presto.sql.parser.ParsingException;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.util.JsonUtil;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static com.facebook.presto.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DECIMAL;

@Singleton
public class FormatSqlServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory
			.getLogger(FormatSqlServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HashMap<String, Object> retVal = new HashMap<String, Object>();

		try {
			Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
			queryOptional.ifPresent(query -> {
				try {
					SqlParser sqlParser = new SqlParser();
					Statement statement = sqlParser.createStatement(query, new ParsingOptions(AS_DECIMAL));
					String formattedQuery = SqlFormatter.formatSql(statement, Optional.empty());
					retVal.put("formattedQuery", formattedQuery);
				} catch (ParsingException e) {
					retVal.put("errorLineNumber", e.getLineNumber());
					LOGGER.error(e.getMessage());
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
