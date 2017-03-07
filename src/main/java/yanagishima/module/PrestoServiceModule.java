package yanagishima.module;

import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;
import yanagishima.service.PrestoServiceImpl;

import com.google.inject.AbstractModule;

import java.util.List;
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
