package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.service.ElasticsearchService;
import yanagishima.service.ElasticsearchServiceImpl;

import java.util.Properties;

public class ElasticsearchServiceModule extends AbstractModule {
    private final Properties properties;

    public ElasticsearchServiceModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        bind(ElasticsearchService.class).to(ElasticsearchServiceImpl.class);
    }
}
