package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class HiveServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(HiveQueryStatusServlet.class);

		serve("/hiveQueryStatus").with(HiveQueryStatusServlet.class);
		serve("/sparkQueryStatus").with(HiveQueryStatusServlet.class);
	}
}
