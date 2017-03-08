package yanagishima.module;

import com.google.inject.AbstractModule;
import me.geso.tinyorm.TinyORM;
import yanagishima.provider.ConnectionProvider;
import yanagishima.provider.TinyORMProvider;

import java.sql.Connection;


public class DbModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(Connection.class)
                .toProvider(ConnectionProvider.class);

        bind(TinyORM.class)
                .toProvider(TinyORMProvider.class);
    }
}