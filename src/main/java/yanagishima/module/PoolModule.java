package yanagishima.module;

import com.google.inject.AbstractModule;
import yanagishima.pool.StatementPool;


public class PoolModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(StatementPool.class);
    }
}