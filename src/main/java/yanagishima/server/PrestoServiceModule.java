package yanagishima.server;

import com.google.inject.AbstractModule;

public class PrestoServiceModule extends AbstractModule {
	protected void configure() {
		bind(PrestoService.class).to(PrestoServiceImpl.class);
	}
}
