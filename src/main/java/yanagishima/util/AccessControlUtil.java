package yanagishima.util;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static yanagishima.util.Constants.DATASOURCE_HEADER;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Splitter;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class AccessControlUtil {
  private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

  public static boolean validateDatasource(HttpServletRequest request, String datasource) {
    String header = request.getHeader(DATASOURCE_HEADER);
    if (header == null) {
      throw new IllegalArgumentException(format("Missing required header '%s'", DATASOURCE_HEADER));
    }
    if (header.equals("*")) {
      return true;
    }
    return SPLITTER.splitToList(header).contains(datasource);
  }

  public static void sendForbiddenError(HttpServletResponse response) {
    try {
      response.sendError(SC_FORBIDDEN);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
