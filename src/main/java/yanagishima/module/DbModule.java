package yanagishima.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import yanagishima.repository.TinyOrm;

public class DbModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TinyOrm.class).in(Scopes.SINGLETON);
    }
}
