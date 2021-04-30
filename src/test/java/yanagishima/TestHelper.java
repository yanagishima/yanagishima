package yanagishima;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static yanagishima.util.QueryEngine.elasticsearch;

import lombok.experimental.UtilityClass;
import yanagishima.util.QueryIdUtil;

@UtilityClass
public class TestHelper {
  public static String getQueryId() {
    return QueryIdUtil.generate("test-datasource", "SELECT 'test'", elasticsearch.name());
  }

  public static String getPublishId() {
    return md5Hex("test-datasource" + ";" + "test-engine" + ";" + getQueryId());
  }
}
