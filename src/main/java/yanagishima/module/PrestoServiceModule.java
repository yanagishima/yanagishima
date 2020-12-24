package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.config.YanagishimaConfig;

import java.util.Properties;

public class PrestoServiceModule extends AbstractModule {

	private final Properties properties;

	public PrestoServiceModule(Properties properties) {
		this.properties = properties;
	}

	@Override
	protected void configure() {
		bind(YanagishimaConfig.class).toInstance(new YanagishimaConfig(this.properties));
	}
}
