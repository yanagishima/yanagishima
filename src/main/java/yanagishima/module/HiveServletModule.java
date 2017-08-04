package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class HiveServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(HiveServlet.class);
		bind(HiveAsyncServlet.class);

		serve("/hive").with(HiveServlet.class);
		serve("/hiveAsync").with(HiveAsyncServlet.class);

	}
}
