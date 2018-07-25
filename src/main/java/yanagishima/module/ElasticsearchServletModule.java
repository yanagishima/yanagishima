package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class ElasticsearchServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(ElasticsearchServlet.class);

		serve("/elasticsearch").with(ElasticsearchServlet.class);
		serve("/elasticsearchQueryStatus").with(ElasticsearchQueryStatusServlet.class);
	}
}
