package yanagishima.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yanagishima.service.PrestoService;

@Singleton
public class PrestoServlet extends HttpServlet {
	
	private static Logger LOGGER = LoggerFactory
			.getLogger(PrestoServlet.class);

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	@Inject
	public PrestoServlet(PrestoService prestoService) {
		this.prestoService = prestoService;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Optional<String> queryOptional = Optional.ofNullable(request.getParameter("query"));
		queryOptional.ifPresent(query -> {

			HashMap<String, Object> retVal = new HashMap<String, Object>();
			try {
				List<List<Object>> rowDataList = prestoService.doQuery(query);
				retVal.put("results", rowDataList);
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
				retVal.put("error", e.getMessage());
			}
			
			try {
				writeJSON(response, retVal);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

	}

	private void writeJSON(HttpServletResponse resp, Object obj)
			throws IOException {
		resp.setContentType("application/json");
		ObjectMapper mapper = new ObjectMapper();
		OutputStream stream = resp.getOutputStream();
		mapper.writeValue(stream, obj);
	}
	
}
