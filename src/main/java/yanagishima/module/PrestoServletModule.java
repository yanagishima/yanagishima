package yanagishima.module;

import yanagishima.servlet.IndexServlet;
import yanagishima.servlet.PrestoServlet;

import com.google.inject.servlet.ServletModule;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(PrestoServlet.class);
		bind(IndexServlet.class);

		serve("/presto").with(PrestoServlet.class);
		serve("/").with(IndexServlet.class);

	}
}
