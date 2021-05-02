package yanagishima.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.codec.digest.DigestUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class QueryIdUtil {
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  public static String generate(String datasource, String query, String engine) {
    String yyyyMMddHHmmss = ZonedDateTime.now(ZoneId.of("GMT")).format(
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    double rand = Math.floor(Math.random() * 10000);
    return yyyyMMddHHmmss + "_" + DigestUtils.md5Hex(
        datasource + ";" + query + ";" + engine + ";" + ZonedDateTime.now() + ";" + rand);
  }
}
