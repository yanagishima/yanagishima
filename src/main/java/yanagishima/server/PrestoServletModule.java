package yanagishima.server;

import com.google.inject.servlet.ServletModule;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(PrestoServlet.class);

		serve("/presto").with(PrestoServlet.class);

	}
}
