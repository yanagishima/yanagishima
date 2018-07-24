package yanagishima.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class QueryIdUtil {

    public static String generate(String datasource, String query, String engine) {
        String yyyyMMddHHmmss = ZonedDateTime.now(ZoneId.of("GMT")).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        double rand = Math.floor(Math.random() * 10000);
        return yyyyMMddHHmmss + "_" + DigestUtils.md5Hex(datasource + ";" + query + ";" + engine + ";" + ZonedDateTime.now().toString() + ";" + String.valueOf(rand));
    }
}
