package yanagishima.servlet;

import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import yanagishima.model.HttpRequestContext;

@Singleton
@Deprecated
public class ConvertPrestoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpRequestContext context = new HttpRequestContext(request);
		Map<String, Object> responseBody = new HashMap<>();
		if (context.getQuery() != null) {
			responseBody.put("prestoQuery", toPrestoQuery(context.getQuery()));
		}
		writeJSON(response, responseBody);
	}

	private static String toPrestoQuery(String hiveQuery) {
		return hiveQuery.replace("get_json_object", "json_extract_scalar")
						.replace("lateral view explode", "cross join unnest");
	}
}
