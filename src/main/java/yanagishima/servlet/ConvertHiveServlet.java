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
public class ConvertHiveServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Optional.ofNullable(request.getParameter("query")).ifPresent(query -> {
			String replacedHiveQuery = query.replace("json_extract_scalar", "get_json_object").replace("cross join unnest", "lateral view explode");
			writeJSON(response, Map.of("hiveQuery", replacedHiveQuery));
		});
	}
}
