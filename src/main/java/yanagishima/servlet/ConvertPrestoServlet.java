package yanagishima.servlet;

import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class ConvertPrestoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Optional.ofNullable(request.getParameter("query")).ifPresent(query -> {
			String replacedPrestoQuery = query.replace("get_json_object", "json_extract_scalar").replace("lateral view explode", "cross join unnest");
			writeJSON(response, Map.of("prestoQuery", replacedPrestoQuery));
		});
	}
}
