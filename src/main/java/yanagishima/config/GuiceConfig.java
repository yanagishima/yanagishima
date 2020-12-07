package yanagishima.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.inject.Injector;

import yanagishima.server.YanagishimaServer;

@Configuration
public class GuiceConfig {
  @Bean
  public Injector injector() {
    return YanagishimaServer.injector;
  }
}
