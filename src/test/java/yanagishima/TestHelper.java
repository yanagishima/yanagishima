package yanagishima;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static yanagishima.util.QueryEngine.elasticsearch;

import lombok.experimental.UtilityClass;
import yanagishima.util.QueryIdUtil;

@UtilityClass
public class TestHelper {
  private static final String TEST_DATASOURCE = "test-datasource";
  private static final String TEST_ENGINE = elasticsearch.name();

  public static String getQueryId() {
    return QueryIdUtil.generate(TEST_DATASOURCE, "SELECT 'test'", TEST_ENGINE);
  }

  public static String getPublishId() {
    return md5Hex(TEST_DATASOURCE + ";" + TEST_ENGINE + ";" + getQueryId());
  }
}
