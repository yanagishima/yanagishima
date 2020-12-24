package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(QueryHistoryServlet.class);
		bind(QueryHistoryUserServlet.class);
		bind(QueryStatusServlet.class);
		bind(LabelServlet.class);

		serve("/queryHistory").with(QueryHistoryServlet.class);
		serve("/queryHistoryUser").with(QueryHistoryUserServlet.class);
		serve("/queryStatus").with(QueryStatusServlet.class);
		serve("/label").with(LabelServlet.class);
	}
}
