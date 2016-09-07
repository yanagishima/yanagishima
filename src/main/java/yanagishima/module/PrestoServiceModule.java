package yanagishima.module;

import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;
import yanagishima.service.PrestoServiceImpl;

import com.google.inject.AbstractModule;

public class PrestoServiceModule extends AbstractModule {

	private int jettyPort;

	private String webResourceDir;

	private String prestoCoordinatorServer;

	private String prestoRedirectServer;

	private String catalog;

	private String schema;

	private String user;

	private String source;
	
	private int selectLimit;

	private String auditHttpHeaderName;

	private String ikasanUrl;

	private String ikasanChannel;

	public PrestoServiceModule(int jettyPort, String webResourceDir,
			String prestoCoordinatorServer, String prestoRedirectServer, String catalog, String schema,
			String user, String source, int selectLimit, String auditHttpHeaderName, String ikasanUrl, String ikasanChannel) {
		this.jettyPort = jettyPort;
		this.webResourceDir = webResourceDir;
		this.prestoCoordinatorServer = prestoCoordinatorServer;
		this.prestoRedirectServer = prestoRedirectServer;
		this.catalog = catalog;
		this.schema = schema;
		this.user = user;
		this.source = source;
		this.selectLimit = selectLimit;
		this.auditHttpHeaderName = auditHttpHeaderName;
		this.ikasanUrl = ikasanUrl;
		this.ikasanChannel = ikasanChannel;
	}

	protected void configure() {
		bind(PrestoService.class).to(PrestoServiceImpl.class);
		bind(YanagishimaConfig.class)
				.toInstance(
						new YanagishimaConfig(jettyPort, webResourceDir,
								prestoCoordinatorServer, prestoRedirectServer, catalog, schema, user,
								source, selectLimit, auditHttpHeaderName, ikasanUrl, ikasanChannel));
	}
}
