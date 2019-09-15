package yanagishima.server;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import yanagishima.config.YanagishimaConfig;
import yanagishima.servlet.api.HiveToPrestoQueryConverter;
import yanagishima.servlet.api.PrestoSqlFormatter;
import yanagishima.servlet.api.PrestoToHiveQueryConverter;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class YanagishimaResourceConfig extends ResourceConfig {
    YanagishimaResourceConfig(YanagishimaConfig config) {
        register(PrestoSqlFormatter.class);
        register(HiveToPrestoQueryConverter.class);
        register(PrestoToHiveQueryConverter.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(PrestoSqlFormatter.class);
                bind(config).to(YanagishimaConfig.class);
            }
        });
    }
}
