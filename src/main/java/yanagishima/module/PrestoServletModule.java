package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(PrestoServlet.class);
		bind(QueryServlet.class);
		bind(KillServlet.class);
		bind(FormatSqlServlet.class);
		bind(HistoryServlet.class);
		bind(QueryDetailServlet.class);
		bind(DownloadServlet.class);
		bind(CsvDownloadServlet.class);
		bind(QueryHistoryServlet.class);
		bind(DatasourceServlet.class);


		serve("/presto").with(PrestoServlet.class);
		serve("/query").with(QueryServlet.class);
		serve("/kill").with(KillServlet.class);
		serve("/format").with(FormatSqlServlet.class);
		serve("/history").with(HistoryServlet.class);
		serve("/queryDetail").with(QueryDetailServlet.class);
		serve("/download").with(DownloadServlet.class);
		serve("/csvdownload").with(CsvDownloadServlet.class);
		serve("/queryHistory").with(QueryHistoryServlet.class);
		serve("/datasource").with(DatasourceServlet.class);

	}
}
