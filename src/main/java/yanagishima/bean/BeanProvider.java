package yanagishima.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.google.inject.Injector;

import yanagishima.config.YanagishimaConfig;

@Component
public class BeanProvider {
  private final YanagishimaConfig config;

  @Autowired
  public BeanProvider(Injector injector) {
    this.config = injector.getInstance(YanagishimaConfig.class);
  }

  @Bean
  public YanagishimaConfig config() {
    return config;
  }
}
