package yanagishima.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class QueryIdUtil {
  private static final String FORMAT = "yyyyMMdd_HHmmss";

  public static String generate(String datasource, String query, String engine) {
    String yyyyMMddHHmmss = ZonedDateTime.now(ZoneId.of("GMT")).format(DateTimeFormatter.ofPattern(FORMAT));
    double rand = Math.floor(Math.random() * 10000);
    return yyyyMMddHHmmss + "_" + DigestUtils.md5Hex(
        datasource + ";" + query + ";" + engine + ";" + ZonedDateTime.now() + ";" + rand);
  }

  public static LocalDateTime datetimeOf(String queryId) {
    return LocalDateTime.parse(queryId.substring(0, FORMAT.length()), DateTimeFormatter.ofPattern(FORMAT));
  }
}
