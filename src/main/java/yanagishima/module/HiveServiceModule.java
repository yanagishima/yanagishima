package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.HiveService;
import yanagishima.service.HiveServiceImpl;

import java.util.Properties;

public class HiveServiceModule extends AbstractModule {


    private Properties properties;

    public HiveServiceModule(Properties properties) {
        this.properties = properties;
    }

    protected void configure() {
        bind(HiveService.class).to(HiveServiceImpl.class);
    }
}
