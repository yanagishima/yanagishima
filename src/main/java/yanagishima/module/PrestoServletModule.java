package yanagishima.module;

import yanagishima.servlet.PrestoServlet;
import yanagishima.servlet.QueryServlet;

import com.google.inject.servlet.ServletModule;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(PrestoServlet.class);
		bind(QueryServlet.class);

		serve("/presto").with(PrestoServlet.class);
		serve("/query").with(QueryServlet.class);

	}
}
