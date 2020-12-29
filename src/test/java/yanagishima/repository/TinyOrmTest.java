package yanagishima.repository;

import static yanagishima.server.YanagishimaServer.createTables;

import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import yanagishima.config.YanagishimaConfig;
import yanagishima.config.YanagishimaConfig.DatabaseType;

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
    createTables(tinyOrm, DatabaseType.SQLITE);
  }

  @Test
  void testExecuteQuery() {
    tinyOrm.executeQuery("SELECT 1");
  }
}
