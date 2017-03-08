package yanagishima.servlet;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class KillServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(KillServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	public KillServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Optional<String> queryIdOptinal = Optional.ofNullable(request.getParameter("queryId"));
		queryIdOptinal.ifPresent(queryId -> {
			String datasource = Optional.ofNullable(request.getParameter("datasource")).get();
			String prestoCoordinatorServer = yanagishimaConfig.getPrestoCoordinatorServer(datasource);
			try {
				Request.Delete(prestoCoordinatorServer + "/v1/query/" + queryId).execute();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

	}

}
