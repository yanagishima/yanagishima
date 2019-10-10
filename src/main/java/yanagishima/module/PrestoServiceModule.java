package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.OldPrestoService;
import yanagishima.service.OldPrestoServiceImpl;
import yanagishima.service.PrestoService;
import yanagishima.service.PrestoServiceImpl;

import java.util.Properties;

public class PrestoServiceModule extends AbstractModule {

	private final Properties properties;

	public PrestoServiceModule(Properties properties) {
		this.properties = properties;
	}

	@Override
	protected void configure() {
		bind(PrestoService.class).to(PrestoServiceImpl.class);
		bind(OldPrestoService.class).to(OldPrestoServiceImpl.class);
		bind(YanagishimaConfig.class).toInstance(new YanagishimaConfig(this.properties));
	}
}
