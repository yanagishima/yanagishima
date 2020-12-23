package yanagishima.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.geso.tinyorm.Row;
import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Comment;
import yanagishima.model.db.Label;
import yanagishima.model.db.Query;
import yanagishima.model.db.SessionProperty;

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

  public int insert(Class klass, Value... values) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.insert(klass).value(toMap(values)).execute();
    }
  }

  public Optional<Comment> singleComment(String query, Object... params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.single(Comment.class).where(query, params).execute();
    }
  }

  public void deleteComment(String query, Object... params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      tinyOrm.single(Comment.class).where(query, params).execute().ifPresent(tinyOrm::delete);
    }
  }

  public Optional<Query> singleQuery(String query, Object... params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.single(Query.class).where(query, params).execute();
    }
  }

  public Optional<Label> singleLabel(String query, Object... params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.single(Label.class).where(query, params).execute();
    }
  }

  public List<Comment> searchComments(String orderBy, String query, Object... params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.search(Comment.class).where(query, params).orderBy(orderBy).execute();
    }
  }

  public List<SessionProperty> searchSessionProperties(String query, Object... params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.search(SessionProperty.class).where(query, params).execute();
    }
  }

  public <T extends Row<?>> List<T> searchBySQL(Class<T> klass, String sql, List<Object> params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.searchBySQL(klass, sql, params);
    }
  }

  public <T extends Row<?>> List<T> searchBySQL(Class<T> klass, String sql) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.searchBySQL(klass, sql, Collections.emptyList());
    }
  }

  public int updateBySQL(String sql) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.updateBySQL(sql);
    }
  }

  public OptionalLong queryForLong(@NonNull final String sql) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.queryForLong(sql);
    }
  }

  public long countQuery(String query, Object... params) {
    try (TinyORM tinyOrm = getTinyOrm()) {
      return tinyOrm.count(Query.class).where(query, params).execute();
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

  @Getter
  @AllArgsConstructor
  public static class Value {
    private final String key;
    private final Object value;
  }

  public static Value value(String key, @Nullable Object value) {
    return new Value(key, value);
  }

  private static Map<String, Object> toMap(Value... values) {
    Map<String, Object> map = new HashMap<>();
    for (Value value : values) {
      map.put(value.getKey(), value.getValue());
    }
    return map;
  }
}
