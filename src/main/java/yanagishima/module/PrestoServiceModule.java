package yanagishima.module;

import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;
import yanagishima.service.PrestoServiceImpl;

import com.google.inject.AbstractModule;

public class PrestoServiceModule extends AbstractModule {
	
	private int jettyPort;
	
	private String webResourceDir;

	private String prestoCoordinatorServer;
	
	private String catalog;
	
	private String schema;
	
	public PrestoServiceModule(int jettyPort, String webResourceDir,
			String prestoCoordinatorServer, String catalog, String schema) {
		this.jettyPort = jettyPort;
		this.webResourceDir = webResourceDir;
		this.prestoCoordinatorServer = prestoCoordinatorServer;
		this.catalog = catalog;
		this.schema = schema;
	}
	
	protected void configure() {
		bind(PrestoService.class).to(PrestoServiceImpl.class);
		bind(YanagishimaConfig.class).toInstance(new YanagishimaConfig(jettyPort, webResourceDir, prestoCoordinatorServer, catalog, schema));
	}
}
