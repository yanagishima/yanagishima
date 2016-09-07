package yanagishima.config;

public class YanagishimaConfig {
	
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

	public YanagishimaConfig(int jettyPort, String webResourceDir,
			String prestoCoordinatorServer, String prestoRedirectServer, String catalog, String schema, String user, String source, int selectLimit, String auditHttpHeaderName, String ikasanUrl, String ikasanChannel) {
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
	
	public int getJettyPort() {
		return jettyPort;
	}

	public String getWebResourceDir() {
		return webResourceDir;
	}

	public String getPrestoCoordinatorServer() {
		return prestoCoordinatorServer;
	}

	public String getPrestoRedirectServer() { return prestoRedirectServer; }

	public String getCatalog() {
		return catalog;
	}

	public String getSchema() {
		return schema;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getSource() {
		return source;
	}

	public int getSelectLimit() {
		return selectLimit;
	}

	public String getAuditHttpHeaderName() {
		return auditHttpHeaderName;
	}

	public String getIkasanUrl() { return ikasanUrl; }

	public String getIkasanChannel() { return ikasanChannel; }

}
