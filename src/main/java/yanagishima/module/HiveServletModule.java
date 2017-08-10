package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class HiveServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(HiveServlet.class);
		bind(HiveAsyncServlet.class);
		bind(YarnJobListServlet.class);
		bind(KillHiveServlet.class);
		bind(HiveQueryStatusServlet.class);
		bind(HiveQueryDetailServlet.class);

		serve("/hive").with(HiveServlet.class);
		serve("/hiveAsync").with(HiveAsyncServlet.class);
		serve("/yarnJobList").with(YarnJobListServlet.class);
		serve("/killHive").with(KillHiveServlet.class);
		serve("/hiveQueryStatus").with(HiveQueryStatusServlet.class);
		serve("/hiveQueryDetail").with(HiveQueryDetailServlet.class);

	}
}
