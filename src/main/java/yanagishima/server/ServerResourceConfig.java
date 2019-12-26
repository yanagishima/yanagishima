package yanagishima.server;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.google.inject.Injector;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.controller.ConvertPrestoController;

public class ServerResourceConfig extends ResourceConfig {
    public ServerResourceConfig(Injector injector) {
        register(ConvertPrestoController.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(injector.getInstance(YanagishimaConfig.class)).to(YanagishimaConfig.class);
                bind(injector.getInstance(TinyORM.class)).to(TinyORM.class);
            }
        });
    }
}
