package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class PrestoServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(PrestoServlet.class);
		bind(PrestoAsyncServlet.class);
		bind(QueryServlet.class);
		bind(PrestoKillServlet.class);
		bind(FormatSqlServlet.class);
		bind(HistoryServlet.class);
		bind(HistoryStatusServlet.class);
		bind(ShareHistoryServlet.class);
		bind(PublishServlet.class);
		bind(QueryDetailServlet.class);
		bind(DownloadServlet.class);
		bind(ShareDownloadServlet.class);
		bind(CsvDownloadServlet.class);
		bind(ShareCsvDownloadServlet.class);
		bind(QueryHistoryServlet.class);
		bind(QueryHistoryUserServlet.class);
		bind(DatasourceAuthServlet.class);
		bind(QueryStatusServlet.class);
		bind(BookmarkServlet.class);
		bind(BookmarkUserServlet.class);
		bind(ToValuesQueryServlet.class);
		bind(TableListServlet.class);
		bind(PrestoPartitionServlet.class);
		bind(CommentServlet.class);
		bind(ConvertPrestoServlet.class);
		bind(LabelServlet.class);
		bind(StarredSchemaServlet.class);
		bind(CheckPrestoQueryServlet.class);
		bind(HealthCheckServlet.class);

		serve("/presto").with(PrestoServlet.class);
		serve("/prestoAsync").with(PrestoAsyncServlet.class);
		serve("/query").with(QueryServlet.class);
		serve("/kill").with(PrestoKillServlet.class);
		serve("/format").with(FormatSqlServlet.class);
		serve("/history").with(HistoryServlet.class);
		serve("/historyStatus").with(HistoryStatusServlet.class);
		serve("/share/shareHistory").with(ShareHistoryServlet.class);
		serve("/publish").with(PublishServlet.class);
		serve("/queryDetail").with(QueryDetailServlet.class);
		serve("/download").with(DownloadServlet.class);
		serve("/share/download").with(ShareDownloadServlet.class);
		serve("/csvdownload").with(CsvDownloadServlet.class);
		serve("/share/csvdownload").with(ShareCsvDownloadServlet.class);
		serve("/queryHistory").with(QueryHistoryServlet.class);
		serve("/queryHistoryUser").with(QueryHistoryUserServlet.class);
		serve("/datasourceAuth").with(DatasourceAuthServlet.class);
		serve("/queryStatus").with(QueryStatusServlet.class);
		serve("/bookmark").with(BookmarkServlet.class);
		serve("/bookmarkUser").with(BookmarkUserServlet.class);
		serve("/toValuesQuery").with(ToValuesQueryServlet.class);
		serve("/tableList").with(TableListServlet.class);
		serve("/prestoPartition").with(PrestoPartitionServlet.class);
		serve("/comment").with(CommentServlet.class);
		serve("/convertPresto").with(ConvertPrestoServlet.class);
		serve("/label").with(LabelServlet.class);
		serve("/starredSchema").with(StarredSchemaServlet.class);
		serve("/checkPrestoQuery").with(CheckPrestoQueryServlet.class);
		serve("/healthCheck").with(HealthCheckServlet.class);
	}
}
