package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;
import yanagishima.service.PrestoServiceImpl;

import java.util.Properties;

public class PrestoServiceModule extends AbstractModule {

	private Properties properties;

	public PrestoServiceModule(Properties properties) {
		this.properties = properties;
	}

	protected void configure() {
		bind(PrestoService.class).to(PrestoServiceImpl.class);
		bind(YanagishimaConfig.class)
				.toInstance(
						new YanagishimaConfig(this.properties));
	}
}
