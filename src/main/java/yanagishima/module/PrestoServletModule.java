package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(PrestoKillServlet.class);
		bind(FormatSqlServlet.class);
		bind(CsvDownloadServlet.class);
		bind(QueryHistoryServlet.class);
		bind(QueryHistoryUserServlet.class);
		bind(DatasourceAuthServlet.class);
		bind(QueryStatusServlet.class);
		bind(CommentServlet.class);
		bind(LabelServlet.class);

		serve("/kill").with(PrestoKillServlet.class);
		serve("/format").with(FormatSqlServlet.class);
		serve("/csvdownload").with(CsvDownloadServlet.class);
		serve("/queryHistory").with(QueryHistoryServlet.class);
		serve("/queryHistoryUser").with(QueryHistoryUserServlet.class);
		serve("/datasourceAuth").with(DatasourceAuthServlet.class);
		serve("/queryStatus").with(QueryStatusServlet.class);
		serve("/comment").with(CommentServlet.class);
		serve("/label").with(LabelServlet.class);
	}
}
