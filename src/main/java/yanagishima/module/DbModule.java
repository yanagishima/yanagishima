package yanagishima.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletScopes;
import me.geso.tinyorm.TinyORM;
import yanagishima.provider.ConnectionProvider;
import yanagishima.provider.TinyORMProvider;

import javax.sql.DataSource;
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