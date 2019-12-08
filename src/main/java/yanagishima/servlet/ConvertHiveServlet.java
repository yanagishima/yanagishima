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

@Singleton
public class ConvertHiveServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> responseBody = new HashMap<>();
		String query = request.getParameter("query");
		if (query != null) {
			responseBody.put("hiveQuery", toHiveQuery(query));
		}
		writeJSON(response, responseBody);
	}

	private static String toHiveQuery(String prestoQuery) {
		return prestoQuery.replace("json_extract_scalar", "get_json_object")
						  .replace("cross join unnest", "lateral view explode");
	}
}
