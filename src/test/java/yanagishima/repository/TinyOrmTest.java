package yanagishima.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.server.YanagishimaServer.createTables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import yanagishima.config.YanagishimaConfig;
import yanagishima.config.YanagishimaConfig.DatabaseType;
import yanagishima.model.db.Query;

class TinyOrmTest {
  private static TinyOrm tinyOrm;

  @BeforeAll
  static void beforeClass() {
    Properties properties = new Properties();
    YanagishimaConfig config = new YanagishimaConfig(properties);
    tinyOrm = new TinyOrm(config);
  }

  @BeforeEach
  void setUp() throws Exception {
    dropTables();
    createTables(tinyOrm, DatabaseType.SQLITE);
  }

  @AfterEach
  void tearDown() {
    dropTables();
  }

  private void dropTables() {
    dropTable("bookmark");
    dropTable("comment");
    dropTable("label");
    dropTable("publish");
    dropTable("starred_schema");
    dropTable("session_property");
    dropTable("query");
  }

  @Test
  void testExecuteQuery() {
    tinyOrm.executeQuery("SELECT 1");
  }

  @Test
  void testSingleQuery() {
    assertEquals(Optional.empty(), tinyOrm.singleQuery("datasource = 'test_a'"));

    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    assertThat(tinyOrm.singleQuery("datasource = 'test_a'")).isPresent();
  }

  @Test
  public void testSearchBySQL() {
    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    tinyOrm.insert(Query.class, value("datasource", "test_b"));

    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource = 'test_a'")).hasSize(1);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource = 'test_b'")).hasSize(1);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource = 'not_found'")).isEmpty();
  }

  @Test
  void testSearchBySQLWithParams() {
    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    tinyOrm.insert(Query.class, value("datasource", "test_b"));

    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?)", List.of("test_a"))).hasSize(1);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?)", List.of("test_b"))).hasSize(1);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?, ?)", List.of("test_a", "test_b"))).hasSize(2);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?)", List.of("not_found"))).isEmpty();
  }

  @Test
  void testQueryForLong() {
    assertEquals(OptionalLong.of(0L), tinyOrm.queryForLong("SELECT count(*) FROM query"));

    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    assertEquals(OptionalLong.of(1L), tinyOrm.queryForLong("SELECT count(*) FROM query"));
  }

  @Test
  void testCount() {
    assertEquals(0, tinyOrm.countQuery("1 = 1"));

    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    assertEquals(1, tinyOrm.countQuery("1 = 1"));
    assertEquals(1, tinyOrm.countQuery("datasource = ?", "test_a"));
  }

  private void dropTable(String table) {
    execute("DROP TABLE IF EXISTS " + table);
  }

  private void execute(String sql) {
    try (Connection connection = tinyOrm.getConnection();
         Statement statement = connection.createStatement()) {
      statement.execute(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
