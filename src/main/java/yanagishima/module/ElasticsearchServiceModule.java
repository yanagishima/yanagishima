package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.service.ElasticsearchService;
import yanagishima.service.ElasticsearchServiceImpl;

public class ElasticsearchServiceModule extends AbstractModule {
    public ElasticsearchServiceModule() {}

    @Override
    protected void configure() {
        bind(ElasticsearchService.class).to(ElasticsearchServiceImpl.class);
    }
}
