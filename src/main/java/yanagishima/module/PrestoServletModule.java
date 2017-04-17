package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(PrestoServlet.class);
		bind(PrestoAsyncServlet.class);
		bind(QueryServlet.class);
		bind(KillServlet.class);
		bind(FormatSqlServlet.class);
		bind(HistoryServlet.class);
		bind(HistoryStatusServlet.class);
		bind(ShareHistoryServlet.class);
		bind(PublishServlet.class);
		bind(QueryDetailServlet.class);
		bind(DownloadServlet.class);
		bind(CsvDownloadServlet.class);
		bind(QueryHistoryServlet.class);
		bind(DatasourceServlet.class);
		bind(QueryStatusServlet.class);

		serve("/presto").with(PrestoServlet.class);
		serve("/prestoAsync").with(PrestoAsyncServlet.class);
		serve("/query").with(QueryServlet.class);
		serve("/kill").with(KillServlet.class);
		serve("/format").with(FormatSqlServlet.class);
		serve("/history").with(HistoryServlet.class);
		serve("/historyStatus").with(HistoryStatusServlet.class);
		serve("/public/shareHistory").with(ShareHistoryServlet.class);
		serve("/publish").with(PublishServlet.class);
		serve("/queryDetail").with(QueryDetailServlet.class);
		serve("/download").with(DownloadServlet.class);
		serve("/csvdownload").with(CsvDownloadServlet.class);
		serve("/queryHistory").with(QueryHistoryServlet.class);
		serve("/datasource").with(DatasourceServlet.class);
		serve("/queryStatus").with(QueryStatusServlet.class);

	}
}
