package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.service.HiveService;
import yanagishima.service.HiveServiceImpl;

public class HiveServiceModule extends AbstractModule {
    public HiveServiceModule() { }

    @Override
    protected void configure() {
        bind(HiveService.class).to(HiveServiceImpl.class);
    }
}
