package yanagishima.repository;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;

public class TinyOrm {
  private final HikariDataSource dataSource;

  @Inject
  public TinyOrm(YanagishimaConfig config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getConnectionUrl());
    hikariConfig.setUsername(config.getConnectionUsername());
    hikariConfig.setPassword(config.getConnectionPassword());
    hikariConfig.setMaximumPoolSize(config.getConnectionMaxPoolSize() / 2); // Share with Spring Data JPA
    hikariConfig.setMaxLifetime(config.getConnectionMaxLifetime());

    dataSource = new HikariDataSource(hikariConfig);
  }

  public void executeQuery(String sql) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      tinyOrm.executeQuery(sql);
    }
  }

  public Connection getConnection() {
    return getTinyOrm().getConnection();
  }

  private TinyORM getTinyOrm() {
    try {
      TinyORM tinyORM = new TinyORM(dataSource.getConnection());
      tinyORM.setQueryTimeout(30);
      return tinyORM;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
