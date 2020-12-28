package yanagishima.config;

import static yanagishima.server.YanagishimaServer.injector;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("yanagishima.repository")
public class DataConfig {
  private final YanagishimaConfig config = injector.getInstance(YanagishimaConfig.class);

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
    entityManagerFactory.setDataSource(dataSource());
    entityManagerFactory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    entityManagerFactory.setPackagesToScan("yanagishima.model.db");
    entityManagerFactory.setJpaProperties(hibernateProperties());
    return entityManagerFactory;
  }

  @Bean
  public DataSource dataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getConnectionUrl());
    hikariConfig.setUsername(config.getConnectionUsername());
    hikariConfig.setPassword(config.getConnectionPassword());
    hikariConfig.setMaximumPoolSize(config.getConnectionMaxPoolSize());
    hikariConfig.setMaxLifetime(config.getConnectionMaxLifetime());
    return new HikariDataSource(hikariConfig);
  }

  @Bean
  public JpaTransactionManager transactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
    return transactionManager;
  }

  private Properties hibernateProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
    return properties;
  }
}
