package yanagishima.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static yanagishima.repository.TinyOrm.value;
import static yanagishima.server.YanagishimaServer.createTables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import yanagishima.config.YanagishimaConfig;
import yanagishima.config.YanagishimaConfig.DatabaseType;
import yanagishima.model.db.Comment;
import yanagishima.model.db.Label;
import yanagishima.model.db.Publish;
import yanagishima.model.db.Query;
import yanagishima.model.db.SessionProperty;
import yanagishima.model.db.StarredSchema;

public class TinyOrmTest {
  private static TinyOrm tinyOrm;

  @BeforeClass
  public static void beforeClass() {
    Properties properties = new Properties();
    YanagishimaConfig config = new YanagishimaConfig(properties);
    tinyOrm = new TinyOrm(config);
  }

  @Before
  public void setUp() throws Exception {
    dropTables();
    createTables(tinyOrm, DatabaseType.SQLITE);
  }

  @After
  public void tearDown() {
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
  public void testExecuteQuery() {
    tinyOrm.executeQuery("SELECT 1");
  }

  @Test
  public void testInsert() {
    assertThat(tinyOrm.singleLabel("datasource = ?", 1)).isEmpty();

    assertEquals(1, tinyOrm.insert(Label.class, value("datasource", "1")));
    assertThat(tinyOrm.singleLabel("datasource = ?", 1)).isPresent();

    assertEquals(1, tinyOrm.insert(Label.class, value("datasource", "2"), value("label_name", null)));
    assertThat(tinyOrm.singleLabel("datasource = ?", 2)).isPresent();
  }

  @Test
  public void testSingleComment() {
    assertEquals(Optional.empty(), tinyOrm.singleComment("1 = 2"));

    assertEquals(1, tinyOrm.insert(Comment.class,
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("like_count", "1")
                                   ));
    assertThat(tinyOrm.singleComment("datasource = ? and engine = ?", "test_datasource", "test_engine")).isPresent();
  }

  @Test
  public void testDeleteComment() {
    String orderBy = "1";
    assertThat(tinyOrm.searchComments(orderBy, "1 = 1")).isEmpty();

    assertEquals(1, tinyOrm.insert(Comment.class,
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("query_id", "1"),
                                   value("like_count", "1")
                                   ));
    assertEquals(1, tinyOrm.insert(Comment.class,
                                   value("datasource", "test_datasource2"),
                                   value("engine", "test_engine2"),
                                   value("query_id", "2"),
                                   value("like_count", "2")
                                   ));
    assertThat(tinyOrm.searchComments(orderBy, "1 = 1")).hasSize(2);

    tinyOrm.deleteComment("datasource = ? and engine = ? and query_id = ?", "test_datasource", "test_engine", "1");
    assertThat(tinyOrm.searchComments(orderBy, "1 = 1")).hasSize(1);
    assertThat(tinyOrm.searchComments(orderBy, "datasource = ?", "test_datasource")).hasSize(0);
    assertThat(tinyOrm.searchComments(orderBy, "datasource = ?", "test_datasource2")).hasSize(1);

    tinyOrm.deleteComment("datasource = ? and engine = ? and query_id = ?", "test_datasource2", "test_engine2", "2");
    assertThat(tinyOrm.searchComments(orderBy, "1 = 1")).hasSize(0);
  }

  @Test
  public void testSingleQuery() {
    assertEquals(Optional.empty(), tinyOrm.singleQuery("datasource = 'test_a'"));

    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    assertThat(tinyOrm.singleQuery("datasource = 'test_a'")).isPresent();
  }

  @Test
  public void testSingleLabel() {
    assertThat(tinyOrm.singleLabel("datasource = ?", 1)).isEmpty();

    assertEquals(1, tinyOrm.insert(Label.class, value("datasource", "1")));
    assertThat(tinyOrm.singleLabel("datasource = ?", 1)).isPresent();
  }

  @Test
  public void testSinglePublish() {
    assertThat(tinyOrm.singlePublish("publish_id = ?", 1)).isEmpty();

    assertEquals(1, tinyOrm.insert(Publish.class, value("publish_id", "1")));
    assertThat(tinyOrm.singlePublish("publish_id = ?", 1)).isPresent();
  }

  @Test
  public void testDeleteStarredSchema() {
    assertEquals(OptionalLong.of(0L), tinyOrm.queryForLong("SELECT count(*) FROM starred_schema"));

    assertEquals(1, tinyOrm.insert(StarredSchema.class,
                                   value("starred_schema_id", 1),
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("catalog", "test_catalog"),
                                   value("schema", "test_schema")
                                   ));
    assertEquals(1, tinyOrm.insert(StarredSchema.class,
                                   value("starred_schema_id", 2),
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("catalog", "test_catalog"),
                                   value("schema", "test_schema")
                                   ));

    assertEquals(OptionalLong.of(2L), tinyOrm.queryForLong("SELECT count(*) FROM starred_schema"));

    tinyOrm.deleteStarredSchema("starred_schema_id = ?", 1);
    assertEquals(OptionalLong.of(1L), tinyOrm.queryForLong("SELECT count(*) FROM starred_schema"));
    assertEquals(OptionalLong.of(1L), tinyOrm.queryForLong("SELECT count(*) FROM starred_schema "
                                                           + "WHERE starred_schema_id = 2"));

    tinyOrm.deleteStarredSchema("starred_schema_id = ?", 2);
    assertEquals(OptionalLong.of(0L), tinyOrm.queryForLong("SELECT count(*) FROM starred_schema"));
  }

  @Test
  public void testSearchComments() {
    String orderBy = "update_time_string DESC";
    assertThat(tinyOrm.searchComments(orderBy, "datasource = ?", 1)).isEmpty();

    assertEquals(1, tinyOrm.insert(Comment.class,
                                   value("datasource", "1"),
                                   value("update_time_string", "2019"),
                                   value("like_count", "100")));
    assertEquals(1, tinyOrm.insert(Comment.class,
                                   value("datasource", "2"),
                                   value("update_time_string", "2020"),
                                   value("like_count", "200")));

    assertThat(tinyOrm.searchComments(orderBy, "1 = 1"))
        .extracting(Comment::getUpdateTimeString)
        .containsExactly("2020", "2019");
    assertThat(tinyOrm.searchComments(orderBy, "datasource = ?", 1)).hasSize(1);
    assertThat(tinyOrm.searchComments(orderBy, "datasource = ?", 2)).hasSize(1);
  }

  @Test
  public void testSearchSessionProperties() {
    assertThat(tinyOrm.searchSessionProperties("session_property_id = ?", 1)).isEmpty();

    assertEquals(1, tinyOrm.insert(SessionProperty.class,
                                   value("session_property_id", "1"),
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("query_id", "test_query_id"),
                                   value("session_key", "session_key"),
                                   value("session_value", "session_session_value")));
    assertEquals(1, tinyOrm.insert(SessionProperty.class,
                                   value("session_property_id", "2"),
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("query_id", "test_query_id"),
                                   value("session_key", "test_session_key"),
                                   value("session_value", "test_session_value")));

    assertThat(tinyOrm.searchSessionProperties("1 = 1")).hasSize(2);
    assertThat(tinyOrm.searchSessionProperties("session_property_id = ?", 1)).hasSize(1);
    assertThat(tinyOrm.searchSessionProperties("session_property_id = ?", 2)).hasSize(1);
  }

  @Test
  public void testSearchStarredSchemas() {
    assertThat(tinyOrm.searchStarredSchemas("starred_schema_id = ?", 1)).isEmpty();

    assertEquals(1, tinyOrm.insert(StarredSchema.class,
                                   value("starred_schema_id", 1),
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("catalog", "test_catalog"),
                                   value("schema", "test_schema")));
    assertEquals(1, tinyOrm.insert(StarredSchema.class,
                                   value("starred_schema_id", 2),
                                   value("datasource", "test_datasource"),
                                   value("engine", "test_engine"),
                                   value("catalog", "test_catalog"),
                                   value("schema", "test_schema")));

    assertThat(tinyOrm.searchStarredSchemas("1 = 1")).hasSize(2);
    assertThat(tinyOrm.searchStarredSchemas("starred_schema_id = ?", 1)).hasSize(1);
    assertThat(tinyOrm.searchStarredSchemas("starred_schema_id = ?", 2)).hasSize(1);
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
  public void testSearchBySQLWithParams() {
    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    tinyOrm.insert(Query.class, value("datasource", "test_b"));

    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?)", List.of("test_a"))).hasSize(1);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?)", List.of("test_b"))).hasSize(1);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?, ?)", List.of("test_a", "test_b"))).hasSize(2);
    assertThat(tinyOrm.searchBySQL(Query.class, "SELECT * FROM query WHERE datasource IN (?)", List.of("not_found"))).isEmpty();
  }

  @Test
  public void testUpdateBySQL() {
    assertThat(tinyOrm.singleQuery("1 = 1")).isEmpty();

    tinyOrm.insert(Query.class, value("datasource", "test_a"), value("user",  "alice"));
    assertThat(tinyOrm.singleQuery("datasource = ? and user = ?", "test_a", "alice")).isPresent();

    tinyOrm.updateBySQL("UPDATE query SET user = 'bob'");
    assertThat(tinyOrm.singleQuery("datasource = ? and user = ?", "test_a", "alice")).isEmpty();
    assertThat(tinyOrm.singleQuery("datasource = ? and user = ?", "test_a", "bob")).isPresent();
  }

  @Test
  public void testQueryForLong() {
    assertEquals(OptionalLong.of(0L), tinyOrm.queryForLong("SELECT count(*) FROM query"));

    tinyOrm.insert(Query.class, value("datasource", "test_a"));
    assertEquals(OptionalLong.of(1L), tinyOrm.queryForLong("SELECT count(*) FROM query"));
  }

  @Test
  public void testCount() {
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
