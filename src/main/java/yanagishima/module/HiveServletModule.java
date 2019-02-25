package yanagishima.module;

import com.google.inject.servlet.ServletModule;
import yanagishima.servlet.*;

public class HiveServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(HiveServlet.class);
		bind(HiveAsyncServlet.class);
		bind(YarnJobListServlet.class);
		bind(SparkJobListServlet.class);
		bind(KillHiveServlet.class);
		bind(HiveQueryStatusServlet.class);
		bind(HiveQueryDetailServlet.class);
		bind(HivePartitionServlet.class);
		bind(ConvertHiveServlet.class);

		serve("/hive").with(HiveServlet.class);
		serve("/spark").with(HiveServlet.class);
		serve("/hiveAsync").with(HiveAsyncServlet.class);
		serve("/sparkAsync").with(HiveAsyncServlet.class);
		serve("/yarnJobList").with(YarnJobListServlet.class);
		serve("/sparkJobList").with(SparkJobListServlet.class);
		serve("/killHive").with(KillHiveServlet.class);
		serve("/hiveQueryStatus").with(HiveQueryStatusServlet.class);
		serve("/sparkQueryStatus").with(HiveQueryStatusServlet.class);
		serve("/hiveQueryDetail").with(HiveQueryDetailServlet.class);
		serve("/sparkQueryDetail").with(HiveQueryDetailServlet.class);
		serve("/hivePartition").with(HivePartitionServlet.class);
		serve("/sparkPartition").with(HivePartitionServlet.class);
		serve("/convertHive").with(ConvertHiveServlet.class);

	}
}
