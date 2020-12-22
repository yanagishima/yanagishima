package yanagishima.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.google.inject.Injector;

import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.service.OldPrestoService;
import yanagishima.service.PrestoService;

@Component
public class BeanProvider {
  private final PrestoService prestoService;
  private final OldPrestoService oldPrestoService;
  private final YanagishimaConfig config;
  private final TinyOrm db;

  @Autowired
  public BeanProvider(Injector injector) {
    this.prestoService = injector.getInstance(PrestoService.class);
    this.oldPrestoService = injector.getInstance(OldPrestoService.class);
    this.config = injector.getInstance(YanagishimaConfig.class);
    this.db = injector.getInstance(TinyOrm.class);
  }

  @Bean
  public PrestoService prestoService() {
    return prestoService;
  }

  @Bean
  public OldPrestoService oldPrestoService() {
    return oldPrestoService;
  }

  @Bean
  public YanagishimaConfig config() {
    return config;
  }

  @Bean
  public TinyOrm db() {
    return db;
  }
}
