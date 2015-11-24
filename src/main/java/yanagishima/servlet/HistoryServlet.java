package yanagishima.servlet;

import com.facebook.presto.sql.SqlFormatter;
import com.facebook.presto.sql.parser.ParsingException;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.Statement;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.row.Query;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Singleton
public class HistoryServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory
			.getLogger(HistoryServlet.class);

	private static final long serialVersionUID = 1L;

	@Inject
	private TinyORM db;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HashMap<String, Object> retVal = new HashMap<String, Object>();

		try {
			Optional<String> queryidOptional = Optional.ofNullable(request.getParameter("queryid"));
			queryidOptional.ifPresent(queryid -> {
				try {
					Optional<Query> queryOptional = db.single(Query.class).where("query_id=?", queryid).execute();
					queryOptional.ifPresent(query -> {
						retVal.put("queryString", query.getQueryString());
					});
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
