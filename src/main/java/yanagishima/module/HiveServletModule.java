package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class HiveServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(YarnJobListServlet.class);
		bind(SparkJobListServlet.class);
		bind(HiveQueryStatusServlet.class);

		serve("/yarnJobList").with(YarnJobListServlet.class);
		serve("/sparkJobList").with(SparkJobListServlet.class);
		serve("/hiveQueryStatus").with(HiveQueryStatusServlet.class);
		serve("/sparkQueryStatus").with(HiveQueryStatusServlet.class);
	}
}
