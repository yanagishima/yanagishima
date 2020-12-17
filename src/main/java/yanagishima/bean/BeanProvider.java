package yanagishima.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.google.inject.Injector;

import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;

@Component
public class BeanProvider {
  private final YanagishimaConfig config;
  private final TinyOrm db;

  @Autowired
  public BeanProvider(Injector injector) {
    this.config = injector.getInstance(YanagishimaConfig.class);
    this.db = injector.getInstance(TinyOrm.class);
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
